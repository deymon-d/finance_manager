package financemanager.infrastructure.export;

import financemanager.core.model.Transaction;
import java.util.List;

public interface ExportService {
    String exportTransactions(List<Transaction> transactions, String fileName);
    List<Transaction> importTransactions(String filePath);
}