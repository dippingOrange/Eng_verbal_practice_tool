import java.io.*;
import java.util.*;

public class UserManager {
    private static final String FILE_PATH = "users.txt";
    private String currentUser = null;

    public boolean register(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }
        if (findUser(username) != null) {
            return false; // 用户名已存在
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(username + "\t" + password);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error writing user file: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String username, String password) {
        String stored = findUser(username);
        if (stored != null && stored.equals(password)) {
            currentUser = username;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    private String findUser(String username) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", 2);
                if (parts.length == 2 && parts[0].equals(username)) {
                    return parts[1];
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user file: " + e.getMessage());
        }
        return null;
    }
}
