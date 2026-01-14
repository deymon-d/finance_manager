package financemanager.infrastructure.json_models;

import financemanager.core.model.Transaction;

import java.time.LocalDate;

public class TransactionData {
    private String id;
    private String category;
    private double amount;
    private Transaction.Type type;
    private String description;
    private LocalDate date;

    public TransactionData() {}

    public TransactionData(Transaction transaction) {
        this.id = transaction.getId();
        this.category = transaction.getCategory();
        this.amount = transaction.getAmount();
        this.type = transaction.getType();
        this.description = transaction.getDescription();
        this.date = transaction.getDate();
    }

    public Transaction restoreToTransaction() {
        return new Transaction(id, category, amount, type, date, description);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Transaction.Type getType() {
        return type;
    }

    public void setType(Transaction.Type type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}