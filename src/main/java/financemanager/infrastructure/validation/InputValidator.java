package financemanager.infrastructure.validation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{4,}$");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[\\p{L}0-9\\s_\\-]{1,50}$");
    private static final double MAX_AMOUNT = 1_000_000_000;

    public static void validateLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (!LOGIN_PATTERN.matcher(login).matches()) {
            throw new IllegalArgumentException(
                    "Логин должен содержать 3-20 символов (буквы, цифры, подчеркивания)"
            );
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 4 символа");
        }
    }

    public static void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категория не может быть пустой");
        }
        if (!CATEGORY_PATTERN.matcher(category).matches()) {
            throw new IllegalArgumentException(
                    "Название категории должно содержать 1-50 символов (буквы, цифры, пробелы, дефисы)"
            );
        }
    }

    public static void validateAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Сумма не может быть пустой");
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                throw new IllegalArgumentException("Сумма должна быть положительной");
            }
            if (amount > MAX_AMOUNT) {
                throw new IllegalArgumentException("Сумма слишком большая");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Сумма должна быть числом");
        }
    }

    public static double parseAndValidateAmount(String amountStr) {
        validateAmount(amountStr);
        return Double.parseDouble(amountStr);
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты. Используйте YYYY-MM-DD");
        }
    }

    public static void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Даты не могут быть null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Начальная дата не может быть позже конечной");
        }
    }
}