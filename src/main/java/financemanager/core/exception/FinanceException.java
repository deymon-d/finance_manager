package financemanager.core.exception;

public class FinanceException extends RuntimeException {
    public FinanceException(String message) {
        super(message);
    }

    public FinanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
