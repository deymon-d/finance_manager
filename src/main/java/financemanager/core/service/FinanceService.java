package financemanager.core.service;

import financemanager.core.model.*;
import financemanager.core.exception.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public class FinanceService {
    private final Map<String, User> users;
    private User currentUser;
    private final NotificationService notificationService;

    public FinanceService(NotificationService notificationService) {
        this.users = new HashMap<>();
        this.notificationService = notificationService;
    }

    public void initializeUsers(Map<String, User> loadedUsers) {
        users.clear();
        users.putAll(loadedUsers);
    }

    public void register(String login, String password) {
        String normalizedLogin = login.trim().toLowerCase();
        if (users.containsKey(normalizedLogin)) {
            throw new ValidationException("Пользователь с логином '" + login + "' уже существует");
        }

        User user = new User(login, password);
        users.put(user.getLogin(), user);
    }

    public void login(String login, String password) {
        User user = users.get(login.toLowerCase());
        if (user == null) {
            throw new UserNotFoundException("Пользователь с логином '" + login + "' не найден");
        }

        if (!user.verifyPassword(password)) {
            throw new ValidationException("Неверный пароль");
        }

        currentUser = user;
        notificationService.checkInitialNotifications(getWallet());
    }

    public void logout() {
        currentUser = null;
        notificationService.clearNotifications();
    }

    public void addIncome(String category, double amount, String description, LocalDate date) {
        checkUserLoggedIn();

        Transaction transaction = new Transaction(
                category, amount, Transaction.Type.INCOME, description
        );
        getWallet().addTransaction(transaction);
    }

    public void addExpense(String category, double amount, String description, LocalDate date) {
        checkUserLoggedIn();

        Transaction transaction = new Transaction(
                category, amount, Transaction.Type.EXPENSE, date, description
        );
        Wallet wallet = getWallet();
        wallet.addTransaction(transaction);

        notificationService.checkBudgetExceeded(wallet, category);
        notificationService.checkBalanceStatus(wallet);
        notificationService.checkBudgetThreshold(wallet, category);
    }

    public void clearTransactions() {
        checkUserLoggedIn();
        getWallet().clearTransactions();
    }

    public void setBudget(String category, double limit) {
        checkUserLoggedIn();
        getWallet().setBudget(category, limit);
    }

    public void updateBudget(String category, double newLimit) {
        checkUserLoggedIn();
        getWallet().updateBudget(category, newLimit);
    }

    public void removeBudget(String category) {
        checkUserLoggedIn();
        getWallet().removeBudget(category);
    }

    public void addCategory(String category) {
        checkUserLoggedIn();
        getWallet().addCategory(category);
    }

    public void removeCategory(String category) {
        checkUserLoggedIn();
        getWallet().removeCategory(category);
    }

    public void transfer(String toUserLogin, double amount, String description) {
        checkUserLoggedIn();

        User receiver = users.get(toUserLogin.toLowerCase());
        if (receiver == null) {
            throw new UserNotFoundException("Получатель с логином '" + toUserLogin + "' не найден");
        }

        if (amount <= 0) {
            throw new ValidationException("Сумма перевода должна быть положительной");
        }

        Wallet senderWallet = getWallet();
        if (senderWallet.getBalance() < amount) {
            throw new InsufficientFundsException("Недостаточно средств для перевода");
        }

        String senderLogin = currentUser.getLogin();
        String receiverLogin = receiver.getLogin();

        addExpense("Перевод пользователю " + receiverLogin, amount,
                description + " → " + receiverLogin, LocalDate.now());

        User sender = currentUser;

        currentUser = receiver;
        addIncome("Перевод от пользователя " + senderLogin, amount,
                description + " ← " + senderLogin, LocalDate.now());

        currentUser = sender;
    }

    public FinanceSummary getSummary() {
        checkUserLoggedIn();
        Wallet wallet = getWallet();

        return new FinanceSummary(
                wallet.getTotalIncome(),
                wallet.getTotalExpense(),
                wallet.getBalance(),
                wallet.getTransactions().size()
        );
    }

    public Map<String, CategorySummary> getCategorySummaries() {
        checkUserLoggedIn();
        Wallet wallet = getWallet();
        Map<String, CategorySummary> summaries = new HashMap<>();

        for (String category : wallet.getCategories()) {
            double income = wallet.getIncomeByCategory(category);
            double expense = wallet.getExpenseByCategory(category);
            Budget budget = wallet.getBudgets().get(category);

            summaries.put(category, new CategorySummary(category, income, expense, budget));
        }

        return summaries;
    }

    public Map<String, BudgetStatus> getBudgetStatuses() {
        checkUserLoggedIn();
        Map<String, BudgetStatus> statuses = new HashMap<>();
        Wallet wallet = getWallet();

        for (Budget budget : wallet.getBudgets().values()) {
            double spent = wallet.getExpenseByCategory(budget.getCategory());
            statuses.put(budget.getCategory(), new BudgetStatus(budget, spent));
        }

        return statuses;
    }

    public Map<String, Double> getExpensesBySelectedCategories(Set<String> categories) {
        checkUserLoggedIn();
        Wallet wallet = getWallet();

        Set<String> existingCategories = wallet.getCategories();
        Set<String> nonExisting = categories.stream()
                .filter(c -> !existingCategories.contains(c))
                .collect(Collectors.toSet());

        if (!nonExisting.isEmpty()) {
            throw new CategoryNotFoundException("Категории не найдены: " + String.join(", ", nonExisting));
        }

        return wallet.getExpensesByCategories(categories);
    }

    public Map<String, Double> getExpensesByPeriod(LocalDate startDate, LocalDate endDate) {
        checkUserLoggedIn();
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Начальная дата не может быть позже конечной");
        }

        return getWallet().getExpensesByPeriod(startDate, endDate);
    }

    public void importTransactions(List<Transaction> transactions) {
        checkUserLoggedIn();
        getWallet().importTransactions(transactions);
    }

    public static class FinanceSummary {
        public final double totalIncome;
        public final double totalExpense;
        public final double balance;
        public final int transactionCount;

        public FinanceSummary(double totalIncome, double totalExpense,
                              double balance, int transactionCount) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.balance = balance;
            this.transactionCount = transactionCount;
        }
    }

    public static class CategorySummary {
        public final String category;
        public final double totalIncome;
        public final double totalExpense;
        public final Budget budget;

        public CategorySummary(String category, double totalIncome,
                               double totalExpense, Budget budget) {
            this.category = category;
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.budget = budget;
        }
    }

    public static class BudgetStatus {
        public final String category;
        public final double limit;
        public final double spent;
        public final double remaining;
        public final double usagePercentage;
        public final boolean exceeded;
        public final boolean nearLimit;

        public BudgetStatus(Budget budget, double spent) {
            this.category = budget.getCategory();
            this.limit = budget.getLimit();
            this.spent = spent;
            this.remaining = limit - spent;
            this.usagePercentage = limit > 0 ? (spent / limit) * 100 : 0;
            this.exceeded = spent > limit;
            this.nearLimit = usagePercentage >= Budget.MAX_PERCENTAGE;
        }
    }

    public User getCurrentUser() { return currentUser; }
    public boolean isUserLoggedIn() { return currentUser != null; }
    public Wallet getWallet() {
        checkUserLoggedIn();
        return currentUser.getWallet();
    }
    public Map<String, User> getUsers() { return Collections.unmodifiableMap(users); }

    private void checkUserLoggedIn() {
        if (!isUserLoggedIn()) {
            throw new IllegalStateException("Пользователь не авторизован");
        }
    }
}
