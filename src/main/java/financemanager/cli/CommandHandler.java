package financemanager.cli;


import financemanager.core.service.*;
import financemanager.infrastructure.export.*;
import financemanager.infrastructure.storage.*;
import financemanager.infrastructure.validation.*;
import financemanager.core.model.Transaction;

import java.time.LocalDate;
import java.util.*;

public class CommandHandler {
    private final FinanceService financeService;
    private final StorageService storageService;
    private final CsvExportService csvExportService;
    private final JsonExportService jsonExportService;
    private final NotificationService notificationService;
    private boolean running;
    private Scanner scanner;

    public CommandHandler() {
        this.notificationService = new NotificationService();
        this.financeService = new FinanceService(this.notificationService);
        this.storageService = new JsonFileService();
        this.csvExportService = new CsvExportService();
        this.jsonExportService = new JsonExportService();
        this.running = true;
        this.scanner = new Scanner(System.in);

        loadData();
    }

    private void loadData() {
        try {
            Map<String, financemanager.core.model.User> users = storageService.loadUsers();
            financeService.initializeUsers(users);
            ConsolePrinter.printSuccess("Данные загружены успешно. Зарегистрировано пользователей: " + users.size());
        } catch (Exception e) {
            ConsolePrinter.printError("Не удалось загрузить данные: " + e.getCause());
            ConsolePrinter.printInfo("Будет создана новая база данных");
        }
    }

    private void saveData() {
        try {
            storageService.saveUsers(financeService.getUsers());
            ConsolePrinter.printSuccess("Данные сохранены успешно");
        } catch (Exception e) {
            ConsolePrinter.printError("Не удалось сохранить данные: " + e.getMessage());
        }
    }

    public void start() {
        ConsolePrinter.printWelcome();

        while (running) {
            try {
                if (!financeService.isUserLoggedIn()) {
                    showAuthMenu();
                } else {
                    showMainMenu();
                }
            } catch (Exception e) {
                ConsolePrinter.printError(e.getMessage());
            }
        }

        scanner.close();
    }

    private void showAuthMenu() {
        System.out.println("\n[Аутентификация]");
        System.out.println("1. " + Command.REGISTER.getDescription() + " (" + Command.REGISTER.getCommand() + ")");
        System.out.println("2. " + Command.LOGIN.getDescription() + " (" + Command.LOGIN.getCommand() + ")");
        System.out.println("3. " + Command.EXIT.getDescription() + " (" + Command.EXIT.getCommand() + ")");
        System.out.println("4. " + Command.HELP.getDescription() + " (" + Command.HELP.getCommand() + ")");
        System.out.print("> ");

        String input = scanner.nextLine().trim();
        Command cmd = Command.fromString(input);

        if (cmd == null) {
            ConsolePrinter.printError("Неизвестная команда. Введите 'help' для справки");
            return;
        }

        switch (cmd) {
            case REGISTER -> register();
            case LOGIN -> login();
            case EXIT -> exit();
            case HELP -> ConsolePrinter.printHelp();
            default -> ConsolePrinter.printError("Эта команда недоступна в меню аутентификации");
        }
    }

    private void showMainMenu() {
        ConsolePrinter.printUserInfo(
                financeService.getCurrentUser().getLogin(),
                financeService.getWallet().getBalance()
        );

        List<String> notifications = notificationService.getNotifications();
        ConsolePrinter.printNotifications(notifications);
        notificationService.clearNotifications();

        System.out.println("\n[Главное меню]");
        System.out.print("Введите команду (help - справка): ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        String[] parts = input.split("\\s+", 2);
        Command cmd = Command.fromString(parts[0]);

        if (cmd == null) {
            ConsolePrinter.printError("Неизвестная команда: " + parts[0]);
            return;
        }

        String args = parts.length > 1 ? parts[1] : "";

        try {
            switch (cmd) {
                case LOGOUT -> logout();
                case ADD_INCOME -> addTransaction(true, args);
                case ADD_EXPENSE -> addTransaction(false, args);
                case SET_BUDGET -> setBudget(args);
                case UPDATE_BUDGET -> updateBudget(args);
                case REMOVE_BUDGET -> removeBudget(args);
                case ADD_CATEGORY -> addCategory(args);
                case REMOVE_CATEGORY -> removeCategory(args);
                case SUMMARY -> showSummary();
                case BUDGETS -> showBudgets();
                case CATEGORIES -> showCategories();
                case EXPENSES -> showExpensesByCategories(args);
                case PERIOD -> showExpensesByPeriod(args);
                case EXPORT_CSV -> exportToCsv(args);
                case EXPORT_JSON -> exportToJson(args);
                case IMPORT_CSV -> importFromCsv(args);
                case IMPORT_JSON -> importFromJson(args);
                case TRANSFER -> transfer(args);
                case HELP -> ConsolePrinter.printHelp();
                case EXIT -> exit();
                case TRANSACTIONS -> showTransactions();
                case CLEAR -> clearTransactions();
                default -> ConsolePrinter.printError("Команда не реализована");
            }
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void register() {
        System.out.print("Логин: ");
        String login = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        try {
            InputValidator.validateLogin(login);
            InputValidator.validatePassword(password);

            financeService.register(login, password);
            ConsolePrinter.printSuccess("Регистрация успешна! Теперь войдите в систему.");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void login() {
        System.out.print("Логин: ");
        String login = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        try {
            financeService.login(login, password);
            ConsolePrinter.printSuccess("Вход выполнен успешно!");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void logout() {
        saveData();
        financeService.logout();
        ConsolePrinter.printSuccess("Вы вышли из системы");
    }

    private void addTransaction(boolean isIncome, String args) {
        String type = isIncome ? "доход" : "расход";

        System.out.print("Категория " + type + "а: ");
        String category = scanner.nextLine().trim();

        System.out.print("Сумма: ");
        String amountStr = scanner.nextLine().trim();

        System.out.print("Описание (необязательно): ");
        String description = scanner.nextLine().trim();

        System.out.print("Дата (YYYY-MM-DD, Enter для сегодня): ");
        String dateStr = scanner.nextLine().trim();

        try {
            InputValidator.validateCategory(category);
            double amount = InputValidator.parseAndValidateAmount(amountStr);
            LocalDate date = InputValidator.parseDate(dateStr);

            if (isIncome) {
                financeService.addIncome(category, amount, description, date);
            } else {
                financeService.addExpense(category, amount, description, date);
            }

            ConsolePrinter.printSuccess(type + " добавлен!");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void setBudget(String args) {
        String category = promptForCategory("Введите категорию для бюджета: ");

        System.out.print("Лимит бюджета: ");
        String limitStr = scanner.nextLine().trim();

        try {
            double limit = InputValidator.parseAndValidateAmount(limitStr);
            financeService.setBudget(category, limit);
            ConsolePrinter.printSuccess("Бюджет установлен!");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void updateBudget(String args) {
        String category = promptForCategory("Введите категорию для обновления бюджета: ");

        System.out.print("Новый лимит: ");
        String limitStr = scanner.nextLine().trim();

        try {
            double limit = InputValidator.parseAndValidateAmount(limitStr);
            financeService.updateBudget(category, limit);
            ConsolePrinter.printSuccess("Бюджет обновлен!");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void removeBudget(String args) {
        String category = promptForCategory("Введите категорию для удаления бюджета: ");

        try {
            financeService.removeBudget(category);
            ConsolePrinter.printSuccess("Бюджет удален!");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void addCategory(String args) {
        System.out.print("Название категории: ");
        String category = scanner.nextLine().trim();

        try {
            InputValidator.validateCategory(category);
            financeService.addCategory(category);
            ConsolePrinter.printSuccess("Категория добавлена!");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void removeCategory(String args) {
        String category = promptForCategory("Введите категорию для удаления: ");

        try {
            financeService.removeCategory(category);
            ConsolePrinter.printSuccess("Категория удалена!");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void showSummary() {
        try {
            FinanceService.FinanceSummary summary = financeService.getSummary();
            ConsolePrinter.printSummary(summary);
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void showBudgets() {
        try {
            Map<String, FinanceService.BudgetStatus> statuses = financeService.getBudgetStatuses();
            if (statuses.isEmpty()) {
                ConsolePrinter.printInfo("Бюджеты не установлены");
                return;
            }
            ConsolePrinter.printBudgetStatus(statuses);
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void showCategories() {
        try {
            Map<String, FinanceService.CategorySummary> summaries = financeService.getCategorySummaries();
            if (summaries.isEmpty()) {
                ConsolePrinter.printInfo("Нет данных по категориям");
                return;
            }
            ConsolePrinter.printCategorySummary(summaries);
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void showExpensesByCategories(String args) {
        System.out.println("Введите категории через запятую: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            ConsolePrinter.printError("Не указаны категории");
            return;
        }

        Set<String> categories = new HashSet<>(Arrays.asList(input.split(",")));
        categories.removeIf(String::isEmpty);

        try {
            Map<String, Double> expenses = financeService.getExpensesBySelectedCategories(categories);

            if (expenses.isEmpty()) {
                ConsolePrinter.printInfo("Нет расходов по выбранным категориям");
                return;
            }

            ConsolePrinter.printExpensesByCategories(expenses);

        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void showExpensesByPeriod(String args) {
        System.out.print("Начальная дата (YYYY-MM-DD): ");
        String startStr = scanner.nextLine().trim();

        System.out.print("Конечная дата (YYYY-MM-DD): ");
        String endStr = scanner.nextLine().trim();

        try {
            LocalDate start = InputValidator.parseDate(startStr);
            LocalDate end = InputValidator.parseDate(endStr);
            InputValidator.validateDateRange(start, end);

            Map<String, Double> expenses = financeService.getExpensesByPeriod(start, end);

            if (expenses.isEmpty()) {
                ConsolePrinter.printInfo("Нет расходов за указанный период");
                return;
            }

            ConsolePrinter.printExpensesByPeriod(expenses, start, end);

        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void showTransactions() {
        try {
            List<financemanager.core.model.Transaction> transactions =
                    financeService.getWallet().getTransactions();
            ConsolePrinter.printTransactions(transactions);
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void clearTransactions() {
        financeService.clearTransactions();
    }

    private void exportToCsv(String args) {
        String fileName = args.isEmpty() ?
                financeService.getCurrentUser().getLogin() + "_" + LocalDate.now() : args;

        try {
            List<Transaction> transactions = financeService.getWallet().getTransactions();
            String filePath = csvExportService.exportTransactions(transactions, fileName);

            ConsolePrinter.printSuccess("Данные экспортированы в файл: " + filePath);
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void exportToJson(String args) {
        String fileName = args.isEmpty() ?
                financeService.getCurrentUser().getLogin() + "_" + LocalDate.now() : args;

        try {
            List<Transaction> transactions = financeService.getWallet().getTransactions();
            String filePath = jsonExportService.exportTransactions(transactions, fileName);

            ConsolePrinter.printSuccess("Данные экспортированы в файл: " + filePath);
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void importFromCsv(String args) {
        System.out.print("Путь к CSV файлу: ");
        String filePath = scanner.nextLine().trim();

        try {
            List<Transaction> transactions = csvExportService.importTransactions(filePath);
            financeService.importTransactions(transactions);
            ConsolePrinter.printSuccess("Импортировано " + transactions.size() + " транзакций");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void importFromJson(String args) {
        System.out.print("Путь к JSON файлу: ");
        String filePath = scanner.nextLine().trim();

        try {
            List<Transaction> transactions = jsonExportService.importTransactions(filePath);
            financeService.importTransactions(transactions);
            ConsolePrinter.printSuccess("Импортировано " + transactions.size() + " транзакций");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void transfer(String args) {
        System.out.print("Логин получателя: ");
        String toUser = scanner.nextLine().trim();

        System.out.print("Сумма перевода: ");
        String amountStr = scanner.nextLine().trim();

        System.out.print("Описание: ");
        String description = scanner.nextLine().trim();

        try {
            double amount = InputValidator.parseAndValidateAmount(amountStr);
            financeService.transfer(toUser, amount, description);
            ConsolePrinter.printSuccess("Перевод выполнен успешно!");
        } catch (Exception e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void exit() {
        saveData();
        running = false;
        ConsolePrinter.printSuccess("До свидания!");
    }

    private String promptForCategory(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
