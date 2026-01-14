package financemanager.core.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userId;
    private double balance;
    private final List<Transaction> transactions;
    private final Map<String, Budget> budgets;
    private final Set<String> categories;

    public Wallet(String userId) {
        this.userId = userId;
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
        this.budgets = new HashMap<>();
        this.categories = new HashSet<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);

        if (transaction.isIncome()) {
            balance += transaction.getAmount();
        } else {
            balance -= transaction.getAmount();
            Budget budget = budgets.get(transaction.getCategory());
            if (budget != null) {
                budget.addSpending(transaction.getAmount());
            }
        }

        categories.add(transaction.getCategory());
    }

    public void setBudget(String category, double limit) {
        if (budgets.get(category) != null) {
            throw new IllegalArgumentException("Бюджет для категории '" + category + "' уже существует");
        }
        Budget budget = new Budget(category, limit);
        budget.addSpending(getTransactionsByCategory(category).stream().mapToDouble(Transaction::getAmount).sum());
        budgets.put(category, budget);
        categories.add(category);
    }

    public void updateBudget(String category, double newLimit) {
        Budget budget = budgets.get(category);
        if (budget == null) {
            throw new IllegalArgumentException("Бюджет для категории '" + category + "' не найден");
        }
        budget.updateLimit(newLimit);
    }

    public void removeBudget(String category) {
        budgets.remove(category);
    }

    public void addCategory(String category) {
        categories.add(category);
    }

    public void removeCategory(String category) {
        if (hasTransactionsInCategory(category)) {
            throw new IllegalStateException("Нельзя удалить категорию, так как в ней есть транзакции");
        }
        categories.remove(category);
        budgets.remove(category);
    }

    public void clearTransactions() {
        this.transactions.clear();
        this.balance = 0.0;
        budgets.forEach((key, value) -> value.resetSpent());
    }

    public boolean hasTransactionsInCategory(String category) {
        return transactions.stream()
                .anyMatch(t -> t.getCategory().equals(category));
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(Transaction::isIncome)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return transactions.stream()
                .filter(Transaction::isExpense)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getIncomeByCategory(String category) {
        return transactions.stream()
                .filter(t -> t.isIncome() && t.getCategory().equals(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getExpenseByCategory(String category) {
        return transactions.stream()
                .filter(t -> t.isExpense() && t.getCategory().equals(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public Map<String, Double> getExpensesByCategories(Set<String> selectedCategories) {
        return transactions.stream()
                .filter(t -> t.isExpense() && selectedCategories.contains(t.getCategory()))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public Map<String, Double> getExpensesByPeriod(LocalDate start, LocalDate end) {
        return transactions.stream()
                .filter(t -> t.isExpense() &&
                        !t.getDate().isBefore(start) &&
                        !t.getDate().isAfter(end))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public List<Transaction> getTransactionsByCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public String getUserId() { return userId; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public Map<String, Budget> getBudgets() { return Collections.unmodifiableMap(budgets); }
    public Set<String> getCategories() { return Collections.unmodifiableSet(categories); }

    public void importTransactions(List<Transaction> importedTransactions) {
        for (Transaction transaction : importedTransactions) {
            addTransaction(transaction);
        }
    }
}
