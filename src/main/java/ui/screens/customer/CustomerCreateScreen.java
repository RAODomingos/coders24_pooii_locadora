package ui.screens.customer;

import dto.CreateCustomerDTO;
import enums.CustomerType;
import service.customer.CustomerService;
import ui.utils.Header;
import ui.core.Screen;
import ui.flow.FlowController;
import ui.utils.Input;
import ui.utils.Output;
import ui.utils.Result;
import ui.utils.ScreenUtils;

import java.util.Scanner;

public class CustomerCreateScreen extends Screen {
    private static final int MAX_LINE_LENGTH = 65;
    private final Scanner scanner;

    private final CustomerService customerService;

    protected String name = "";
    protected String phoneNumber = "";
    protected String documentId = "";
    protected CustomerType type;

    private int currentField = 0;

    public CustomerCreateScreen(FlowController flowController,
                                Scanner scanner, CustomerService customerService) {
        super(flowController);
        this.scanner = scanner;
        this.customerService = customerService;
    }

    @Override
    public void show() {
        do {
            ScreenUtils.clearScreen();

            Header.show("Editar Agência", null);

            if (type != null) {
                displayCustomerRegistration();
                Output.info("'V' para voltar campo, 'C' para cancelar o cadastro.");
            }

            switch (currentField) {
                case 0 -> {
                    String emptyLine = "║  " + " ".repeat(25) + "  ║";
                    String bottomLine = "╚══" + "═".repeat(25) + "══╝";

                    System.out.println(emptyLine);
                    for (CustomerType type : CustomerType.values()) {
                        System.out.printf("║  [ %d ] - %-15s    ║%n", type.ordinal(), type.getDescription());
                    }
                    System.out.println(emptyLine);
                    System.out.println(bottomLine);

                    Result<Integer> inputType = Input.getAsInt(scanner, "Tipo: ", false);
                    if (inputType.isFailure()) {
                        Output.error(inputType.getErrorMessage());
                        scanner.nextLine();
                        break;
                    }

                    if (inputType.getValue() < 0 || inputType.getValue() >= CustomerType.values().length) {
                        Output.error("Tipo de cliente inválido!");
                        scanner.nextLine();
                        break;
                    }
                    type = CustomerType.values()[inputType.getValue()];

                    currentField = 1;
                }
                case 1 -> {
                    String documentPrompt = type == CustomerType.INDIVIDUAL ? "CPF: " : type == CustomerType.LEGALENTITY ? "CNPJ: " : "Documento: ";

                    String documentInput = Input.getAsString(scanner, documentPrompt, false, false);
                    if (processInputCommands(documentInput)) {
                        break;
                    }
                    documentId = documentInput;
                    currentField = 2;
                }
                case 2 -> {
                    String inputName = Input.getAsString(scanner, "Nome: ", false, false);
                    if (processInputCommands(inputName)) {
                        break;
                    }
                    name = inputName;
                    currentField = 3;
                }
                case 3 -> {
                    String phoneInput = Input.getAsString(scanner, "Telefone: ", false, false);
                    if (processInputCommands(phoneInput)) {
                        break;
                    }
                    phoneNumber = phoneInput;
                    currentField = 4;
                }

                case 4 -> confirmRegistration();
            }
        } while (true);
    }

    private void displayCustomerRegistration() {
        String typeName = type != null ? type.name().isEmpty() ? "" : type.getDescription() : "";
        String documentPrompt = type == CustomerType.INDIVIDUAL ? "CPF: " : type == CustomerType.LEGALENTITY ? "CNPJ: " : "Documento: ";

        String[] fields = {
                "Tipo: " + typeName,
                documentPrompt + (documentId.isEmpty() ? "" : documentId),
                "Nome: " + (name.isEmpty() ? "" : name),
                "Telefone: " + (phoneNumber.isEmpty() ? "" : phoneNumber)
        };

        String emptyLine = "║    " + " ".repeat(MAX_LINE_LENGTH) + "    ║";
        String bottomLine = "╚════" + "═".repeat(MAX_LINE_LENGTH) + "════╝";

        System.out.println(emptyLine);
        for(String field : fields) {
            System.out.printf("║    %-65s    ║%n", field);
        }
        System.out.println(emptyLine);
        System.out.println(bottomLine);
    }

    private void confirmRegistration() {
        String input = Input.getAsString(scanner, "Confirma o cadastro? (S/n): ", true, false);
        input = input.isEmpty() ? "s" : input;

        if (input.equalsIgnoreCase("s")) {
            // Chamar o serviço de cadastro
            CreateCustomerDTO createCustomerDTO = new CreateCustomerDTO(
                    type, name, phoneNumber, documentId
            );

            try {
                customerService.createCustomer(createCustomerDTO);
            } catch (Exception e) {
                Output.error(e.getMessage());
                System.out.println("Cadastro cancelado.");
                scanner.nextLine();
                return;
            }

            System.out.println("Cadastro realizado com sucesso!");
        } else {
            System.out.println("Cadastro cancelado.");
        }
        scanner.nextLine();
        flowController.goBack();
    }

    private boolean processInputCommands(String input) {
        if (input.equalsIgnoreCase("v")) {
            if (currentField > 0) {
                currentField--;
            }
            return true;
        } else if (input.equalsIgnoreCase("c")) {
            cancelRegistration();
            return true;
        }
        return false;
    }

    private void cancelRegistration() {
        flowController.goBack();
    }

}
