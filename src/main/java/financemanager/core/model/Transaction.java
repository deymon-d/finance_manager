package financemanager.core.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String category;
    private final double amount;
    private final Type type;
    private final String description;
    private final LocalDate date;

    public enum Type {
        INCOME("Доход"),
        EXPENSE("Расход");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Transaction(String category, double amount, Type type, String description) {
        this(UUID.randomUUID().toString(), category, amount, type,
                LocalDate.now(), description);
    }

    public Transaction(String category, double amount, Type type, LocalDate date, String description) {
        this(UUID.randomUUID().toString(), category, amount, type, date, description);
    }

    public Transaction(String id, String category, double amount, Type type,
                       LocalDate date, String description) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория не может быть пустой");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }

        this.id = id;
        this.category = category.trim();
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.description = description != null ? description.trim() : "";
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public Type getType() {
        return type;
    }


    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public boolean isIncome() {
        return type == Type.INCOME;
    }

    public boolean isExpense() {
        return type == Type.EXPENSE;
    }

    @Override
    public String toString() {
        return String.format("%s: %s - %.2f (%s)",
                date, category, amount, type.getDisplayName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Transaction that = (Transaction) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}