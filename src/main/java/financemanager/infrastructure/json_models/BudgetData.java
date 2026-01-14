package financemanager.infrastructure.json_models;

import financemanager.core.model.Budget;

public class BudgetData {
    private String category;
    private double limit;

    public BudgetData() {}

    public BudgetData(Budget budget) {
        this.category = budget.getCategory();
        this.limit = budget.getLimit();
    }

    public Budget restoreToBudget() {
        return new Budget(category, limit);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }
}