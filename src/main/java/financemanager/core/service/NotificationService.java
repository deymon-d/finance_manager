package financemanager.core.service;

import financemanager.core.model.Wallet;
import financemanager.core.model.Budget;
import java.util.*;

public class NotificationService {
    private final List<String> notifications;

    public NotificationService() {
        this.notifications = new ArrayList<>();
    }

    public void checkBudgetExceeded(Wallet wallet, String category) {
        Budget budget = wallet.getBudgets().get(category);
        if (budget != null && budget.isExceeded()) {
            String message = String.format(
                    "ПРЕВЫШЕНИЕ БЮДЖЕТА! Категория: '%s'. Потрачено: %.2f, Лимит: %.2f, Перерасход: %.2f",
                    category, budget.getSpent(), budget.getLimit(), budget.getSpent() - budget.getLimit()
            );
            addNotification(message);
        }
    }

    public void checkBudgetThreshold(Wallet wallet, String category) {
        Budget budget = wallet.getBudgets().get(category);
        if (budget != null && budget.isNearLimit() && !budget.isExceeded()) {
            String message = String.format(
                    "Близко к лимиту! Категория: '%s'. Использовано: %.1f%% (%.2f из %.2f)",
                    category, budget.getUsagePercentage(), budget.getSpent(), budget.getLimit()
            );
            addNotification(message);
        }
    }

    public void checkBalanceStatus(Wallet wallet) {
        double balance = wallet.getBalance();
        if (balance < 0) {
            addNotification("ОТРИЦАТЕЛЬНЫЙ БАЛАНС! Текущий баланс: " + balance);
        }
    }

    public void checkInitialNotifications(Wallet wallet) {
        for (Budget budget : wallet.getBudgets().values()) {
            checkBudgetExceeded(wallet, budget.getCategory());
            checkBudgetThreshold(wallet, budget.getCategory());
        }

        checkBalanceStatus(wallet);
    }

    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void clearNotifications() {
        notifications.clear();
    }

    private void addNotification(String message) {
        notifications.add(message);
    }
}
