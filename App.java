import java.util.Scanner;

public class App {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserManager userManager = new UserManager();

    public static void main(String[] args) {
        while (true) {
            if (!userManager.isLoggedIn()) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private static void showLoginMenu() {
        System.out.println("\n===== English Speaking Practice =====");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> handleRegister();
            case "2" -> handleLogin();
            case "3" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void showMainMenu() {
        System.out.println("\n===== Welcome, " + userManager.getCurrentUser() + " =====");
        System.out.println("1. Reading Test");
        System.out.println("2. Conversation Practice");
        System.out.println("3. Logout");
        System.out.print("Choose: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> readingTest();
            case "2" -> conversationPractice();
            case "3" -> {
                userManager.logout();
                System.out.println("Logged out.");
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void handleRegister() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (userManager.register(username, password)) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private static void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (userManager.login(username, password)) {
            System.out.println("Login successful! Welcome " + username + ".");
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private static void readingTest() {
        ReadingPractice practice = new ReadingPractice();
        practice.start(scanner);
    }

    private static void conversationPractice() {
        ConversationPractice practice = new ConversationPractice();
        practice.start(scanner);
    }
}
