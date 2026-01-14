package financemanager.unit;

import financemanager.core.model.Budget;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {

    @Test
    void testBudgetCreation() {
        Budget budget = new Budget("Food", 10000);

        assertEquals("Food", budget.getCategory());
        assertEquals(10000, budget.getLimit());
        assertEquals(0, budget.getSpent());
        assertEquals(10000, budget.getRemaining());
        assertEquals(0, budget.getUsagePercentage());
        assertFalse(budget.isExceeded());
        assertFalse(budget.isNearLimit());
    }

    @Test
    void testAddSpending() {
        Budget budget = new Budget("Food", 10000);
        budget.addSpending(3000);

        assertEquals(3000, budget.getSpent());
        assertEquals(7000, budget.getRemaining());
        assertEquals(30.0, budget.getUsagePercentage());
        assertFalse(budget.isExceeded());
        assertFalse(budget.isNearLimit());
    }

    @Test
    void testBudgetExceeded() {
        Budget budget = new Budget("Food", 5000);
        budget.addSpending(6000);

        assertTrue(budget.isExceeded());
        assertEquals(-1000, budget.getRemaining());
    }

    @Test
    void testBudgetNearLimit() {
        Budget budget = new Budget("Food", 5000);
        budget.addSpending(4000); // 80%

        assertTrue(budget.isNearLimit());
        assertFalse(budget.isExceeded());
        assertEquals(80.0, budget.getUsagePercentage());
    }

    @Test
    void testUpdateLimit() {
        Budget budget = new Budget("Food", 5000);
        budget.addSpending(3000);
        budget.updateLimit(10000);

        assertEquals(10000, budget.getLimit());
        assertEquals(3000, budget.getSpent());
        assertEquals(7000, budget.getRemaining());
    }

    @Test
    void testInvalidBudgetCreation() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Budget("", 1000);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Budget("Food", -1000);
        });
    }

    @Test
    void testInvalidSpending() {
        Budget budget = new Budget("Food", 1000);
        assertThrows(IllegalArgumentException.class, () -> {
            budget.addSpending(-100);
        });
    }
}
