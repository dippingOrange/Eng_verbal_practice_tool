import java.util.*;

public class ConversationPractice {
    private static final Map<String, String> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("1", "你是一位面试官，用英语进行技术面试，每次问一个问题，等用户回答后再继续");
        SCENARIOS.put("2", "你是一位友善的英语母语者，和用户聊日常话题，像朋友一样自然对话");
        SCENARIOS.put("3", "你是商店店员，顾客（用户）需要帮助，用清晰简单的英语提供帮助");
    }

    public void start(Scanner scanner) {
        try {
            System.out.println("\n--- Choose a Conversation Scenario ---");
            for (Map.Entry<String, String> entry : SCENARIOS.entrySet()) {
                String label = switch (entry.getKey()) {
                    case "1" -> "Interview";
                    case "2" -> "Friend Chat";
                    case "3" -> "Asking for Help";
                    default -> entry.getKey();
                };
                System.out.println(entry.getKey() + ". " + label);
            }
            System.out.print("Choose: ");
            String choice = scanner.nextLine();

            String scenarioPrompt = SCENARIOS.get(choice);
            if (scenarioPrompt == null) {
                System.out.println("Invalid choice.");
                return;
            }

            ApiClient api = new ApiClient();
            String systemPrompt = scenarioPrompt + "\nKeep responses concise (2-3 sentences).";

            System.out.println("\n--- Starting conversation (type 'exit' to end) ---");
            String firstMessage = switch (choice) {
                case "1" -> "I'm ready for the interview. Please start.";
                case "2" -> "Hi! How are you doing today?";
                case "3" -> "Hello, I need some help please.";
                default -> "Hello!";
            };
            System.out.println("\nAI: " + api.callApi(systemPrompt, firstMessage));

            while (true) {
                System.out.print("\nYou: ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) break;

                String response = api.callApi(systemPrompt, input);
                System.out.println("AI: " + response);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
