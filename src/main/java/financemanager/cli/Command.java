package financemanager.cli;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Command {
    // Аутентификация
    REGISTER("register", "Регистрация нового пользователя"),
    LOGIN("login", "Вход в систему"),
    LOGOUT("logout", "Выход из системы"),

    // Транзакции
    ADD_INCOME("income", "Добавить доход"),
    ADD_EXPENSE("expense", "Добавить расход"),

    // Категории и бюджеты
    SET_BUDGET("set-budget", "Установить бюджет для категории"),
    UPDATE_BUDGET("update-budget", "Обновить бюджет"),
    REMOVE_BUDGET("remove-budget", "Удалить бюджет"),
    ADD_CATEGORY("add-category", "Добавить категорию"),
    REMOVE_CATEGORY("remove-category", "Удалить категорию"),

    // Статистика и отчеты
    SUMMARY("summary", "Общая статистика"),
    BUDGETS("budgets", "Статус бюджетов"),
    CATEGORIES("categories", "Статистика по категориям"),
    EXPENSES("expenses", "Расходы по выбранным категориям"),
    PERIOD("period", "Расходы за период"),
    TRANSACTIONS("transactions", "Показать все транзакции"),
    CLEAR("clear", "Очистить транзакции"),

    // Экспорт/импорт
    EXPORT_CSV("export-csv", "Экспорт транзакций в CSV"),
    EXPORT_JSON("export-json", "Экспорт транзакций в JSON"),
    IMPORT_CSV("import-csv", "Импорт транзакций из CSV"),
    IMPORT_JSON("import-json", "Импорт транзакций в JSON"),

    // Переводы
    TRANSFER("transfer", "Перевод пользователю"),

    // Системные
    HELP("help", "Показать справку"),
    EXIT("exit", "Выход из приложения");

    private final String command;
    private final String description;

    Command(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public static Command fromString(String text) {
        if (text == null) { return null; }

        for (Command cmd : Command.values()) {
            if (cmd.command.equalsIgnoreCase(text)) {
                return cmd;
            }
        }
        return null;
    }

    public static String getAllCommands() {
        return Arrays.stream(Command.values())
                .map(cmd -> String.format("%-15s - %s", cmd.getCommand(), cmd.getDescription()))
                .collect(Collectors.joining("\n"));
    }
}