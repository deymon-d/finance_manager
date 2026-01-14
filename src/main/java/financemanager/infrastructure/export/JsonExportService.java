package financemanager.infrastructure.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import financemanager.core.model.Transaction;
import financemanager.infrastructure.json_models.TransactionData;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class JsonExportService implements ExportService {
    private final ObjectMapper objectMapper;

    public JsonExportService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String exportTransactions(List<Transaction> transactions, String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "transactions_" + LocalDate.now();
        }

        String filePath = "exports/" + fileName + ".json";

        try {
            Files.createDirectories(Paths.get("exports"));

            objectMapper.writeValue(new File(filePath), transactions.stream().map(TransactionData::new).collect(Collectors.toList()));

            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка экспорта в JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> importTransactions(String filePath) {
        try {
            TransactionData[] transactionsArray = objectMapper.readValue(new File(filePath), TransactionData[].class);
            return Arrays.stream(transactionsArray).toList().stream().map(TransactionData::restoreToTransaction).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка импорта из JSON: " + e.getMessage(), e);
        }
    }
}