package financemanager.infrastructure.json_models;

import financemanager.core.model.Budget;
import financemanager.core.model.Transaction;
import financemanager.core.model.Wallet;

import java.util.*;
import java.util.stream.Collectors;

public class WalletData {
    private String userId;
    private double balance;
    private List<TransactionData> transactions;
    private Map<String, BudgetData> budgets;
    private Set<String> categories;

    public WalletData() {}

    public WalletData(Wallet wallet) {
        this.userId = wallet.getUserId();
        this.balance = wallet.getBalance();
        this.transactions = wallet.getTransactions().stream().map(TransactionData::new).collect(Collectors.toList());
        this.budgets = new HashMap<String, BudgetData>();
        wallet.getBudgets().forEach((s, budget) -> this.budgets.put(s, new BudgetData(budget)));
        this.categories = new HashSet<>(wallet.getCategories());
    }

    public void restoreToWallet(Wallet wallet) {
        try {
            java.lang.reflect.Field balanceField = Wallet.class.getDeclaredField("balance");
            balanceField.setAccessible(true);
            balanceField.set(wallet, balance);

            java.lang.reflect.Field transactionsField = Wallet.class.getDeclaredField("transactions");
            transactionsField.setAccessible(true);
            transactionsField.set(wallet, new ArrayList<>(transactions.stream().map(TransactionData::restoreToTransaction).toList()));

            java.lang.reflect.Field budgetsField = Wallet.class.getDeclaredField("budgets");
            budgetsField.setAccessible(true);
            Map<String, Budget> walletBudgets = new HashMap<String, Budget>();
            budgets.forEach((s, budgetData) -> walletBudgets.put(s, budgetData.restoreToBudget()));
            budgetsField.set(wallet, walletBudgets);

            java.lang.reflect.Field categoriesField = Wallet.class.getDeclaredField("categories");
            categoriesField.setAccessible(true);
            categoriesField.set(wallet, new HashSet<>(categories));

            for (TransactionData transactionData : transactions) {
                Transaction transaction = transactionData.restoreToTransaction();
                if (transaction.isExpense()) {
                    Budget budget = walletBudgets.get(transaction.getCategory());
                    if (budget != null) {
                        budget.addSpending(transaction.getAmount());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка восстановления кошелька", e);
        }
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public List<TransactionData> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionData> transactions) { this.transactions = transactions; }
    public Map<String, BudgetData> getBudgets() { return budgets; }
    public void setBudgets(Map<String, BudgetData> budgets) { this.budgets = budgets; }
    public Set<String> getCategories() { return categories; }
    public void setCategories(Set<String> categories) { this.categories = categories; }
}