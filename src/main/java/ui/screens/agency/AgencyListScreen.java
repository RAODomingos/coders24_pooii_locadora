package ui.screens.agency;

import model.agency.Agency;
import service.agency.AgencyService;
import ui.utils.Header;
import ui.core.Screen;
import ui.flow.FlowController;
import ui.utils.Input;
import ui.utils.Output;
import ui.utils.ScreenUtils;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class AgencyListScreen extends Screen {
    private static final int MAX_LINE_LENGTH = 65;
    private final Scanner scanner;
    private final AgencyService agencyService;

    private final boolean isModal;

    private Agency selectedAgency;
    private boolean agencySelected = false;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 2;

    private String searchQuery = "";
    private List<Agency> filteredAgencies;

    private String errorMessage = "";

    public AgencyListScreen(
            FlowController flowController, Scanner scanner, AgencyService agencyService, boolean isModal) {
        super(flowController);
        this.scanner = scanner;
        this.agencyService = agencyService;
        this.isModal = isModal;
    }

    @Override
    public void show() {
        List<Agency> agencies = agencyService.findAllAgencies();
        this.filteredAgencies = agencies;

        do {
            ScreenUtils.clearScreen();

            listPaginatedAgencies(filteredAgencies, currentPage);

            Output.info("'X' para voltar");

            String promptMessage = isModal ? "Selecione uma agência pelo número ou utilize os comandos acima: "
                    : "Utilize os comandos de navegação: ";

            displayPendingMessages();
            String input = Input.getAsString(scanner, promptMessage, true, false);

            if (processInputCommands(input, agencies)) {
                break;
            }

        } while (!agencySelected);
    }

    private void displayPendingMessages() {
        if (!errorMessage.isEmpty()) {
            Output.error(errorMessage);
            // Esperar o usuário pressionar Enter para continuar
            Input.getAsString(scanner, "Pressione Enter para continuar...", true, false);
            errorMessage = ""; // Limpa o erro após o usuário confirmar
        }
    }

    private void listPaginatedAgencies(List<Agency> agencies, int page) {
        if (isModal) {
            Header.show("Selecione uma agência para continuar...", null);
        } else {
            Header.show("Lista de Agências", null);
        }

        if (!searchQuery.isEmpty()) {
            System.out.println("║  Filtro: " + searchQuery);
        }

        if (agencies.isEmpty()) {
            Output.info("Nenhuma agência encontrada.\n");
            return;
        }

        int totalPages = (int) Math.ceil((double) agencies.size() / PAGE_SIZE);
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, agencies.size());

        String emptyLine = "║    " + " ".repeat(MAX_LINE_LENGTH) + "    ║";
        String bottomLine = "╚════" + "═".repeat(MAX_LINE_LENGTH) + "════╝";

        System.out.println(emptyLine);
        System.out.printf("║ %-3s │ %-23s │ %-23s │ %-13s ║%n", "Nº", "Nome", "Endereço", "Telefone");
        System.out.println("╟─────┼─────────────────────────┼─────────────────────────┼───────────────╢");

        for (int i = start; i < end; i++) {
            Agency agency = agencies.get(i);
            System.out.printf("║ %-3d │ %-23s │ %-23s │ %-13s ║%n",
                    (i + 1),
                    limitString(agency.getName(), 23),
                    limitString(agency.getAddress(), 23),
                    limitString(agency.getPhone(), 13));
        }

        System.out.println(emptyLine);
        System.out.println(bottomLine);

        System.out.println("\nPágina " + (page + 1) + " de " + totalPages + "\n");

        Output.info("'F' para filtrar, 'L' para limpar filtro.");
        Output.info("'A' para avançar página, 'V' para voltar página");
    }

    private String limitString(String str, int maxLength) {
        if (str.length() > maxLength) {
            return str.substring(0, maxLength - 3) + "...";
        }
        return str;
    }

    private boolean processInputCommands(String input, List<Agency> agencies) {
        switch (input.toLowerCase()) {
            case "v":
                if (currentPage > 0) {
                    currentPage--;
                }
                break;
            case "a":
                if (currentPage < (int) Math.ceil((double) filteredAgencies.size() / PAGE_SIZE) - 1) {
                    currentPage++;
                }
                break;
            case "f":
                if (filteredAgencies.isEmpty())
                    break;

                searchAgencies(agencies);
                break;
            case "l":
                if (!searchQuery.isEmpty()) {
                    searchQuery = "";
                    filteredAgencies = agencies;
                    currentPage = 0;
                }
                break;
            case "x":
                back();
                return true;
            default:
                if (isModal) {
                    try {
                        int agencyIndex = Integer.parseInt(input) - 1;
                        if (agencyIndex >= 0 && agencyIndex < filteredAgencies.size()) {
                            selectAgency(filteredAgencies.get(agencyIndex));
                            agencySelected = true;
                            return true;
                        } else {
                            System.out.println("Opção inválida.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida. Tente novamente.");
                    }
                }
                return false;
        }
        return false;
    }

    private void searchAgencies(List<Agency> agencies) {

        searchQuery = Input.getAsString(scanner, "Filtrar por nome da agência: ", true, false);
        searchQuery = searchQuery.trim().toLowerCase();

        filteredAgencies = agencies.stream()
                .filter(agency -> agency.getName().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());
        currentPage = 0;

        if (filteredAgencies.isEmpty()) {
            errorMessage = "Nenhuma agência encontrada com o nome: " + searchQuery;
            searchQuery = "";
            filteredAgencies = agencies;
        }
    }

    private void selectAgency(Agency agency) {
        this.selectedAgency = agency;
        this.agencySelected = true;
    }

    private void back() {
        flowController.goBack();
    }

    public Agency getSelectedAgency() {
        return selectedAgency;
    }
}
