package financemanager.infrastructure.json_models;

import financemanager.core.model.User;

public class UserData {
    private String login;
    private String passwordHash;
    private WalletData wallet;

    public UserData() {}

    public UserData(User user) {
        this.login = user.getLogin();
        this.passwordHash = user.getPasswordHash();
        this.wallet = new WalletData(user.getWallet());
    }

    public User toUser() {
        User user = new User(login, "dummy");

        try {
            java.lang.reflect.Field passwordHashField = User.class.getDeclaredField("passwordHash");
            passwordHashField.setAccessible(true);
            passwordHashField.set(user, passwordHash);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка восстановления пользователя", e);
        }

        if (wallet != null) {
            wallet.restoreToWallet(user.getWallet());
        }

        return user;
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public WalletData getWallet() { return wallet; }
    public void setWallet(WalletData wallet) { this.wallet = wallet; }
}