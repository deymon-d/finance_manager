package financemanager.cli;

import financemanager.core.service.FinanceService;
import java.time.LocalDate;
import java.util.*;

public class ConsolePrinter {

    public static void printWelcome() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║      СИСТЕМА УПРАВЛЕНИЯ ЛИЧНЫМИ ФИНАНСАМИ              ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
    }

    public static void printUserInfo(String login, double balance) {
        System.out.println("┌──────────────────────────────────────────────────────┐");
        System.out.printf("│ Пользователь: %-40s │\n", login);
        System.out.printf("│ Баланс: %-44.2f │\n", balance);
        System.out.println("└──────────────────────────────────────────────────────┘\n");
    }

    public static void printSummary(FinanceService.FinanceSummary summary) {
        System.out.println("\nОБЩАЯ СТАТИСТИКА");
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.printf("Общий доход:     %45.2f\n", summary.totalIncome);
        System.out.printf("Общий расход:    %45.2f\n", summary.totalExpense);
        System.out.printf("Текущий баланс:  %45.2f\n", summary.balance);
        System.out.printf("Количество операций: %39d\n", summary.transactionCount);
        System.out.println("══════════════════════════════════════════════════════════\n");
    }

    public static void printCategorySummary(Map<String, FinanceService.CategorySummary> summaries) {
        if (summaries.isEmpty()) {
            printInfo("Нет данных по категориям");
            return;
        }

        System.out.println("\nСТАТИСТИКА ПО КАТЕГОРИЯМ");
        System.out.println("══════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-25s %15s %15s %15s\n", "Категория", "Доходы", "Расходы", "Бюджет");
        System.out.println("──────────────────────────────────────────────────────────────────────────────");

        for (FinanceService.CategorySummary summary : summaries.values()) {
            String budgetStr = summary.budget != null ?
                    String.format("%.2f", summary.budget.getLimit()) : "—";
            System.out.printf("%-25s %15.2f %15.2f %15s\n",
                    summary.category,
                    summary.totalIncome,
                    summary.totalExpense,
                    budgetStr
            );
        }

        System.out.println("══════════════════════════════════════════════════════════════════════════════\n");
    }

    public static void printBudgetStatus(Map<String, FinanceService.BudgetStatus> statuses) {
        if (statuses.isEmpty()) {
            printInfo("Бюджеты не установлены");
            return;
        }

        System.out.println("\nСТАТУС БЮДЖЕТОВ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-20s %-8s %12s %12s %12s %15s\n",
                "Категория", "Статус", "Лимит", "Потрачено", "Остаток", "Использовано");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

        for (FinanceService.BudgetStatus status : statuses.values()) {
            String statusIcon;
            if (status.exceeded) {
                statusIcon = "ПРЕВЫШЕН";
            } else if (status.nearLimit) {
                statusIcon = "БЛИЗКО";
            } else {
                statusIcon = "НОРМА";
            }

            System.out.printf("%-20s %-8s %12.2f %12.2f %12.2f %15s\n",
                    status.category,
                    statusIcon,
                    status.limit,
                    status.spent,
                    status.remaining,
                    String.format("%.1f%%", status.usagePercentage)
            );
        }

        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════\n");
    }

    public static void printExpensesByCategories(Map<String, Double> expenses) {
        if (expenses.isEmpty()) {
            printInfo("Нет расходов по выбранным категориям");
            return;
        }

        System.out.println("\nРАСХОДЫ ПО КАТЕГОРИЯМ");
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.printf("%-30s %20s\n", "Категория", "Сумма расходов");
        System.out.println("──────────────────────────────────────────────────────────");

        double total = 0;
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            System.out.printf("%-30s %20.2f\n", entry.getKey(), entry.getValue());
            total += entry.getValue();
        }

        System.out.println("──────────────────────────────────────────────────────────");
        System.out.printf("%-30s %20.2f\n", "ИТОГО:", total);
        System.out.println("══════════════════════════════════════════════════════════\n");
    }

    public static void printExpensesByPeriod(Map<String, Double> expenses, LocalDate start, LocalDate end) {
        if (expenses.isEmpty()) {
            printInfo("Нет расходов за указанный период");
            return;
        }

        System.out.println("\nРАСХОДЫ ЗА ПЕРИОД " + start + " - " + end);
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.printf("%-30s %20s\n", "Категория", "Сумма расходов");
        System.out.println("──────────────────────────────────────────────────────────");

        double total = 0;
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            System.out.printf("%-30s %20.2f\n", entry.getKey(), entry.getValue());
            total += entry.getValue();
        }

        System.out.println("──────────────────────────────────────────────────────────");
        System.out.printf("%-30s %20.2f\n", "ИТОГО:", total);
        System.out.println("══════════════════════════════════════════════════════════\n");
    }

    public static void printTransactions(List<financemanager.core.model.Transaction> transactions) {
        if (transactions.isEmpty()) {
            printInfo("Нет транзакций");
            return;
        }

        System.out.println("\nПОСЛЕДНИЕ ТРАНЗАКЦИИ");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-12s %-20s %-15s %12s %-30s\n",
                "Дата", "Категория", "Тип", "Сумма", "Описание");
        System.out.println("───────────────────────────────────────────────────────────────────────────────────────────");

        for (financemanager.core.model.Transaction t : transactions) {
            String type = t.isIncome() ? "Доход" : "Расход";
            String amountStr = String.format("%.2f", t.getAmount());

            System.out.printf("%-12s %-20s %-15s %12s %-30s\n",
                    t.getDate(),
                    t.getCategory(),
                    type,
                    amountStr,
                    t.getDescription()
            );
        }

        System.out.println("═══════════════════════════════════════════════════════════════════════════════════════════\n");
    }

    public static void printNotifications(List<String> notifications) {
        if (!notifications.isEmpty()) {
            System.out.println("\nУВЕДОМЛЕНИЯ");
            System.out.println("══════════════════════════════════════════════════════════");
            for (String notification : notifications) {
                System.out.println("• " + notification);
            }
            System.out.println("══════════════════════════════════════════════════════════\n");
        }
    }

    public static void printSuccess(String message) {
        System.out.println("УСПЕХ: " + message);
    }

    public static void printError(String message) {
        System.out.println("ОШИБКА: " + message);
    }

    public static void printInfo(String message) {
        System.out.println("ИНФО: " + message);
    }

    public static void printHelp() {
        System.out.println("\nСПРАВКА ПО КОМАНДАМ");
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("Аутентификация:");
        System.out.println("  register    - Регистрация нового пользователя");
        System.out.println("  login       - Вход в систему");
        System.out.println("  logout      - Выход из системы");
        System.out.println();
        System.out.println("Транзакции:");
        System.out.println("  income      - Добавить доход");
        System.out.println("  expense     - Добавить расход");
        System.out.println();
        System.out.println("Категории и бюджеты:");
        System.out.println("  set-budget  - Установить бюджет для категории");
        System.out.println("  update-budget - Обновить бюджет");
        System.out.println("  remove-budget - Удалить бюджет");
        System.out.println("  add-category - Добавить категорию");
        System.out.println("  remove-category - Удалить категорию");
        System.out.println();
        System.out.println("Статистика и отчеты:");
        System.out.println("  summary     - Общая статистика");
        System.out.println("  budgets     - Статус бюджетов");
        System.out.println("  categories  - Статистика по категориям");
        System.out.println("  expenses    - Расходы по выбранным категориям");
        System.out.println("  period      - Расходы за период");
        System.out.println("  transactions - Показать все транзакции");
        System.out.println("  clear       - Очистить все транзакции");
        System.out.println();
        System.out.println("Экспорт/импорт:");
        System.out.println("  export-csv  - Экспорт транзакций в CSV");
        System.out.println("  export-json - Экспорт транзакций в JSON");
        System.out.println("  import-csv  - Импорт транзакций из CSV");
        System.out.println("  import-json  - Импорт транзакций из JSON");
        System.out.println();
        System.out.println("Переводы:");
        System.out.println("  transfer    - Перевод пользователю");
        System.out.println();
        System.out.println("Системные:");
        System.out.println("  help        - Показать справку");
        System.out.println("  exit        - Выход из приложения");
        System.out.println("══════════════════════════════════════════════════════════\n");
    }
}