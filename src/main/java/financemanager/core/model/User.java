package financemanager.core.model;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String login;
    private final String passwordHash;
    private final Wallet wallet;

    public User(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        this.login = login.trim().toLowerCase();
        this.passwordHash = hashPassword(password);
        this.wallet = new Wallet(this.login);
    }

    private String hashPassword(String password) {
        int hash = password.hashCode();
        return Integer.toHexString(hash);
    }

    public boolean verifyPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public Wallet getWallet() { return wallet; }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        User user = (User) o;
        return login.equals(user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "User{login='" + login + "'}";
    }
}