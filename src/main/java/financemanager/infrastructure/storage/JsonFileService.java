package financemanager.infrastructure.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import financemanager.core.model.User;
import financemanager.core.model.Wallet;
import financemanager.core.model.Transaction;
import financemanager.core.model.Budget;
import financemanager.infrastructure.json_models.UserData;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class JsonFileService implements StorageService {
    private static final String DATA_DIR = "data/";
    private static final String USERS_FILE = "users.json";
    private final ObjectMapper objectMapper;

    public JsonFileService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);

        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для данных", e);
        }
    }

    @Override
    public void saveUsers(Map<String, User> users) {
        try {
            String filePath = DATA_DIR + USERS_FILE;
            List<UserData> userDataList = new ArrayList<>();

            for (User user : users.values()) {
                userDataList.add(new UserData(user));
            }

            objectMapper.writeValue(new File(filePath), userDataList);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения пользователей", e);
        }
    }

    @Override
    public Map<String, User> loadUsers() {
        File file = new File(DATA_DIR + USERS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try {
            List<UserData> userDataList = objectMapper.readValue(file,
                    new TypeReference<List<UserData>>() {});

            Map<String, User> users = new HashMap<>();
            for (UserData userData : userDataList) {
                User user = userData.toUser();
                users.put(user.getLogin(), user);
            }

            return users;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки пользователей", e);
        }
    }

    @Override
    public void saveUserData(String userId, Object data) {
        try {
            String filePath = DATA_DIR + userId + "_data.json";
            objectMapper.writeValue(new File(filePath), data);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения данных пользователя", e);
        }
    }

    @Override
    public <T> T loadUserData(String userId, Class<T> type) {
        File file = new File(DATA_DIR + userId + "_data.json");
        if (!file.exists()) {
            return null;
        }

        try {
            return objectMapper.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки данных пользователя", e);
        }
    }

}