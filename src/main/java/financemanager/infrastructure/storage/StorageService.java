package financemanager.infrastructure.storage;

import financemanager.core.model.User;
import java.util.Map;

public interface StorageService {
    void saveUsers(Map<String, User> users);
    Map<String, User> loadUsers();

    void saveUserData(String userId, Object data);
    <T> T loadUserData(String userId, Class<T> type);
}