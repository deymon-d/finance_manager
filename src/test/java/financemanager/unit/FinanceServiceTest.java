package financemanager.unit;

import financemanager.core.service.*;
import financemanager.core.exception.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FinanceServiceTest {
    private FinanceService financeService;

    @BeforeAll
    void setUpAll() {
        NotificationService notificationService = new NotificationService();
        financeService = new FinanceService(notificationService);
    }

    @BeforeEach
    void setUp() {
        financeService.logout();
    }

    @Test
    void testUserRegistrationAndLogin() {
        financeService.register("user1", "password123");
        financeService.login("user1", "password123");

        assertNotNull(financeService.getCurrentUser());
        assertEquals("user1", financeService.getCurrentUser().getLogin());
    }

    @Test
    void testDuplicateRegistration() {
        financeService.register("user2", "password123");
        assertThrows(ValidationException.class, () -> {
            financeService.register("user2", "different");
        });
    }

    @Test
    void testLoginWithWrongPassword() {
        financeService.register("user3", "password123");
        assertThrows(ValidationException.class, () -> {
            financeService.login("user3", "wrong");
        });
    }

    @Test
    void testLoginNonExistentUser() {
        assertThrows(UserNotFoundException.class, () -> {
            financeService.login("nonexistent", "password123");
        });
    }

    @Test
    void testAddIncomeAndExpense() {
        financeService.register("user4", "password123");
        financeService.login("user4", "password123");

        financeService.addIncome("Salary", 50000, "Monthly", LocalDate.now());
        financeService.addExpense("Food", 3000, "Restaurant", LocalDate.now());

        FinanceService.FinanceSummary summary = financeService.getSummary();
        assertEquals(50000, summary.totalIncome);
        assertEquals(3000, summary.totalExpense);
        assertEquals(47000, summary.balance);
    }

    @Test
    void testSetAndGetBudget() {
        financeService.register("user5", "password123");
        financeService.login("user5", "password123");

        financeService.setBudget("Food", 10000);
        financeService.addExpense("Food", 3000, "Restaurant", LocalDate.now());

        var budgetStatus = financeService.getBudgetStatuses();
        assertTrue(budgetStatus.containsKey("Food"));
        assertEquals(10000, budgetStatus.get("Food").limit);
        assertEquals(3000, budgetStatus.get("Food").spent);
        assertEquals(7000, budgetStatus.get("Food").remaining);
        assertFalse(budgetStatus.get("Food").exceeded);
        assertFalse(budgetStatus.get("Food").nearLimit);
    }

    @Test
    void testBudgetExceeded() {
        financeService.register("user6", "password123");
        financeService.login("user6", "password123");

        financeService.setBudget("Food", 5000);
        financeService.addExpense("Food", 6000, "Restaurant", LocalDate.now());

        var budgetStatus = financeService.getBudgetStatuses();
        assertTrue(budgetStatus.get("Food").exceeded);
        assertEquals(-1000, budgetStatus.get("Food").remaining);
    }

    @Test
    void testBudgetNearLimit() {
        financeService.register("user7", "password123");
        financeService.login("user7", "password123");

        financeService.setBudget("Food", 5000);
        financeService.addExpense("Food", 4000, "Restaurant", LocalDate.now());

        var budgetStatus = financeService.getBudgetStatuses();
        assertTrue(budgetStatus.get("Food").nearLimit);
        assertEquals(80.0, budgetStatus.get("Food").usagePercentage);
        assertFalse(budgetStatus.get("Food").exceeded);
    }

    @Test
    void testTransferBetweenUsers() {
        financeService.register("sender1", "password123");
        financeService.register("receiver1", "password1234");

        financeService.login("sender1", "password123");
        financeService.addIncome("Salary", 50000, "", LocalDate.now());
        financeService.transfer("receiver1", 10000, "Gift");

        FinanceService.FinanceSummary senderSummary = financeService.getSummary();
        assertEquals(40000, senderSummary.balance);

        financeService.logout();
        financeService.login("receiver1", "password1234");

        FinanceService.FinanceSummary receiverSummary = financeService.getSummary();
        assertEquals(10000, receiverSummary.balance);
    }

    @Test
    void testTransferInsufficientFunds() {
        financeService.register("sender2", "password123");
        financeService.register("receiver2", "password1234");

        financeService.login("sender2", "password123");
        financeService.addIncome("Salary", 1000, "", LocalDate.now());

        assertThrows(InsufficientFundsException.class, () -> {
            financeService.transfer("receiver2", 2000, "Loan");
        });
    }

    @Test
    void testTransferToNonExistentUser() {
        financeService.register("sender3", "password123");
        financeService.login("sender3", "password123");
        financeService.addIncome("Salary", 5000, "", LocalDate.now());

        assertThrows(UserNotFoundException.class, () -> {
            financeService.transfer("nonexistent", 1000, "Test");
        });
    }

    @Test
    void testGetExpensesBySelectedCategories() {
        financeService.register("user8", "password123");
        financeService.login("user8", "password123");

        financeService.addIncome("Salary", 100000, "", LocalDate.now());
        financeService.addExpense("Food", 15000, "", LocalDate.now());
        financeService.addExpense("Transport", 5000, "", LocalDate.now());
        financeService.addExpense("Food", 5000, "", LocalDate.now());

        var expenses = financeService.getExpensesBySelectedCategories(
                java.util.Set.of("Food", "Transport"));

        assertEquals(2, expenses.size());
        assertEquals(20000, expenses.get("Food"));
        assertEquals(5000, expenses.get("Transport"));
    }

    @Test
    void testCategoryNotFound() {
        financeService.register("user9", "password123");
        financeService.login("user9", "password123");

        financeService.addIncome("Salary", 10000, "", LocalDate.now());

        assertThrows(CategoryNotFoundException.class, () -> {
            financeService.getExpensesBySelectedCategories(java.util.Set.of("NonExistent"));
        });
    }

    @Test
    void testGetExpensesByPeriod() {
        financeService.register("user10", "password123");
        financeService.login("user10", "password123");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        financeService.addIncome("Salary", 50000, "", today);
        financeService.addExpense("Food", 1000, "Lunch", today);
        financeService.addExpense("Transport", 500, "Bus", yesterday);

        var expensesToday = financeService.getExpensesByPeriod(today, today);
        assertEquals(1, expensesToday.size());
        assertEquals(1000, expensesToday.get("Food"));

        var expensesTwoDays = financeService.getExpensesByPeriod(yesterday, tomorrow);
        assertEquals(2, expensesTwoDays.size());
    }

    @Test
    void testInvalidDateRange() {
        financeService.register("user11", "password123");
        financeService.login("user11", "password123");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        assertThrows(ValidationException.class, () -> {
            financeService.getExpensesByPeriod(today, yesterday);
        });
    }

    @Test
    void testAddAndRemoveCategory() {
        financeService.register("user12", "password123");
        financeService.login("user12", "password123");

        financeService.addCategory("TestCategory");
        assertTrue(financeService.getWallet().getCategories().contains("TestCategory"));

        financeService.removeCategory("TestCategory");
        assertFalse(financeService.getWallet().getCategories().contains("TestCategory"));
    }

    @Test
    void testRemoveCategoryWithTransactions() {
        financeService.register("user13", "password123");
        financeService.login("user13", "password123");

        financeService.addIncome("Salary", 10000, "", LocalDate.now());
        financeService.addExpense("Food", 1000, "Lunch", LocalDate.now());

        assertThrows(IllegalStateException.class, () -> {
            financeService.removeCategory("Food");
        });
    }

    @Test
    void testUpdateBudget() {
        financeService.register("user14", "password123");
        financeService.login("user14", "password123");

        financeService.setBudget("Food", 5000);
        financeService.updateBudget("Food", 8000);

        var budgetStatus = financeService.getBudgetStatuses();
        assertEquals(8000, budgetStatus.get("Food").limit);
    }

    @Test
    void testRemoveBudget() {
        financeService.register("user15", "password123");
        financeService.login("user15", "password123");

        financeService.setBudget("Food", 5000);
        assertTrue(financeService.getBudgetStatuses().containsKey("Food"));

        financeService.removeBudget("Food");
        assertFalse(financeService.getBudgetStatuses().containsKey("Food"));
    }

    @Test
    void testLogout() {
        financeService.register("user16", "password123");
        financeService.login("user16", "password123");

        assertNotNull(financeService.getCurrentUser());

        financeService.logout();

        assertNull(financeService.getCurrentUser());
        assertThrows(IllegalStateException.class, () -> {
            financeService.getSummary();
        });
    }

    @Test
    void testMultipleUsersIndependentWallets() {
        financeService.register("userA", "password123");
        financeService.login("userA", "password123");
        financeService.addIncome("Salary", 50000, "", LocalDate.now());
        financeService.addExpense("Food", 10000, "", LocalDate.now());
        double balanceA = financeService.getWallet().getBalance();

        financeService.logout();
        financeService.register("userB", "password1234");
        financeService.login("userB", "password1234");
        financeService.addIncome("Business", 100000, "", LocalDate.now());
        financeService.addExpense("Car", 30000, "", LocalDate.now());
        double balanceB = financeService.getWallet().getBalance();

        assertEquals(40000, balanceA);
        assertEquals(70000, balanceB);
        assertNotEquals(balanceA, balanceB);
    }
}