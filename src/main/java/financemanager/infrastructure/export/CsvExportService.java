package financemanager.infrastructure.export;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import financemanager.core.model.Transaction;
import org.apache.commons.csv.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvExportService implements ExportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public String exportTransactions(List<Transaction> transactions, String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "transactions_" + LocalDate.now();
        }

        String filePath = "exports/" + fileName + ".csv";

        try {
            Files.createDirectories(Paths.get("exports"));

            try (Writer writer = new FileWriter(filePath);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                         .withHeader("ID", "Дата", "Категория", "Тип", "Сумма", "Описание"))) {

                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                DecimalFormat df = new DecimalFormat("#.##", symbols);

                for (Transaction t : transactions) {
                    csvPrinter.printRecord(
                            t.getId(),
                            t.getDate().format(DATE_FORMATTER),
                            t.getCategory(),
                            t.getType().getDisplayName(),
                            df.format(t.getAmount()),
                            t.getDescription()
                    );
                }

                csvPrinter.flush();
            }

            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка экспорта в CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> importTransactions(String filePath) {
        List<Transaction> transactions = new ArrayList<>();

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    String id = record.isMapped("ID") ? record.get("ID") : UUID.randomUUID().toString();
                    LocalDate date = LocalDate.parse(record.get("Дата"), DATE_FORMATTER);
                    String category = record.get("Категория");
                    String typeStr = record.get("Тип");
                    double amount = Double.parseDouble(record.get("Сумма"));
                    String description = record.isMapped("Описание") ? record.get("Описание") : "";

                    Transaction.Type type;
                    if (typeStr.equalsIgnoreCase("доход") || typeStr.equalsIgnoreCase("income")) {
                        type = Transaction.Type.INCOME;
                    } else if (typeStr.equalsIgnoreCase("расход") || typeStr.equalsIgnoreCase("expense")) {
                        type = Transaction.Type.EXPENSE;
                    } else {
                        throw new IllegalArgumentException("Неизвестный тип транзакции: " + typeStr);
                    }

                    Transaction transaction = new Transaction(id, category, amount, type, date, description);
                    transactions.add(transaction);

                } catch (Exception e) {
                    System.err.println("Ошибка обработки строки " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка импорта из CSV: " + e.getMessage(), e);
        }

        return transactions;
    }
}
