package ui.screens.rental;

import exceptions.DataInputInterruptedException;
import service.agency.AgencyService;
import service.customer.CustomerService;
import service.rental.RentalService;
import service.vehicle.VehicleService;
import ui.utils.Header;
import ui.core.Screen;
import ui.flow.FlowController;
import ui.utils.Input;
import ui.utils.Output;
import ui.utils.Result;
import ui.utils.ScreenUtils;

import java.util.Scanner;

public class RentalMenuScreen extends Screen {
    private static final int MAX_LINE_LENGTH = 65;
    private final Scanner scanner;

    private final AgencyService agencyService;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final RentalService rentalService;

    private String errorMessage = "";

    public RentalMenuScreen(FlowController flowController,
                            Scanner scanner, AgencyService agencyService, CustomerService customerService, VehicleService vehicleService, RentalService rentalService) {
        super(flowController);
        this.scanner = scanner;
        this.agencyService = agencyService;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.rentalService = rentalService;
    }

    @Override
    public void show() {
        Result<Integer> option;
        do {
            ScreenUtils.clearScreen();
            displayMenuOptions();
            displayPendingMessages();

            option = getUserOption();
            if (option.isFailure()) {
                errorMessage = option.getErrorMessage();
                continue;
            }

            handleMenuOption(option.getValue());

            if (option.getValue() == 0) break;
        } while (true);

    }

    private void handleMenuOption(int option) {
        switch (option) {
            case 1 ->
                    navigateTo(new RentalCreateScreen(flowController, scanner, agencyService, vehicleService, customerService, rentalService));
            case 2 -> navigateTo(new RentalCloseScreen(flowController, scanner, agencyService, rentalService));
            case 3 -> navigateTo(new OpenRentalListScreen(flowController, scanner, rentalService, false));
            case 4 -> navigateTo(new ClosedRentalListScreen(flowController, scanner, rentalService, false));
            case 0 -> flowController.goBack();
            default -> errorMessage = "Opção inválida! Por favor, informe uma opção do menu...";
        }
    }

    private void displayPendingMessages() {
        if (!errorMessage.isEmpty()) {
            Output.error(errorMessage);
            errorMessage = "";
        }
    }

    private Result<Integer> getUserOption() {
        try {
            return Input.getAsInt(scanner, "Escolha uma opção: ", false);
        } catch (DataInputInterruptedException e) {
            return Result.fail(e.getMessage());
        }
    }

    private void displayMenuOptions() {
        Header.show("Menu Locações", null);

        String[] fields = {
                "[ 1 ] - Nova Locação",
                "[ 2 ] - Encerrar Locação",
                "[ 3 ] - Visualizar Locações Ativas",
                "[ 4 ] - Visualizar Locações Encerradas",
                "[ 0 ] - Voltar"
        };

        String emptyLine = "║    " + " ".repeat(MAX_LINE_LENGTH) + "    ║";
        String bottomLine = "╚════" + "═".repeat(MAX_LINE_LENGTH) + "════╝";

        System.out.println(emptyLine);
        for (String field : fields) {
            System.out.printf("║    %-65s    ║%n", field);
        }
        System.out.println(emptyLine);
        System.out.println(bottomLine);
    }

    private void navigateTo(Screen screen) {
        flowController.goTo(screen);
    }
}
