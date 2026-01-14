package financemanager.core.model;

import java.io.Serializable;
import java.util.Objects;

public class Budget implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double MAX_PERCENTAGE = 80;

    private final String category;
    private double limit;
    private double spent;

    public Budget(String category, double limit) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория не может быть пустой");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Лимит бюджета не может быть отрицательным");
        }

        this.category = category.trim();
        this.limit = limit;
        this.spent = 0.0;
    }

    public void addSpending(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Сумма расхода не может быть отрицательной");
        }
        this.spent += amount;
    }

    public void updateLimit(double newLimit) {
        if (newLimit < 0) {
            throw new IllegalArgumentException("Лимит бюджета не может быть отрицательным");
        }
        this.limit = newLimit;
    }

    public void resetSpent() {
        this.spent = 0.0;
    }

    public String getCategory() { return category; }
    public double getLimit() { return limit; }
    public double getSpent() { return spent; }
    public double getRemaining() { return limit - spent; }
    public double getUsagePercentage() {
        return limit > 0 ? (spent / limit) * 100 : 0;
    }
    public boolean isExceeded() { return spent > limit; }
    public boolean isNearLimit() { return getUsagePercentage() >= MAX_PERCENTAGE; }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Budget budget = (Budget) o;
        return category.equals(budget.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category);
    }

    @Override
    public String toString() {
        return String.format("Budget{category='%s', limit=%.2f, spent=%.2f}",
                category, limit, spent);
    }
}