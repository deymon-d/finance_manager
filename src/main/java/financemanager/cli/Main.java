package financemanager.cli;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
public class Main {
    public static void main(String[] args) {
        try {
            PrintStream utf8PrintStreamOut = new PrintStream(System.out, true, StandardCharsets.UTF_8.name());
            PrintStream utf8PrintStreamErr = new PrintStream(System.err, true, StandardCharsets.UTF_8.name());
            System.setOut(utf8PrintStreamOut);
            System.setErr(utf8PrintStreamErr);
            CommandHandler commandHandler = new CommandHandler();
            commandHandler.start();
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}