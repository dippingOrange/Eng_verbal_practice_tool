import java.io.*;
import java.util.*;

public class ReadingPractice {
    private static final String TEXTS_FILE = "texts.txt";

    public void start(Scanner scanner) {
        try {
            ApiClient api = new ApiClient();
            String passage = getRandomPassage();
            if (passage == null) {
                System.out.println("No texts found in " + TEXTS_FILE);
                return;
            }

            System.out.println("\n--- Read the following passage aloud ---");
            System.out.println(passage);
            System.out.println("\nType the passage above (simulating speech):");
            String input = scanner.nextLine();

            String systemPrompt = "You are an English pronunciation coach. Evaluate the user's reading and return JSON only: " +
                    "{\"score\": <0-100>, \"feedback\": \"...\", \"tips\": \"...\"}";
            String userMessage = "Passage: " + passage + "\n\nMy reading: " + input;

            System.out.println("\nEvaluating...");
            String response = api.callApi(systemPrompt, userMessage);
            System.out.println("\n--- Evaluation Result ---");
            System.out.println(response);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private String getRandomPassage() {
        List<String> lines = new ArrayList<>();
        File file = new File(TEXTS_FILE);
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            StringBuilder passage = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (passage.length() > 0) {
                        lines.add(passage.toString().trim());
                        passage.setLength(0);
                    }
                } else {
                    passage.append(line).append(" ");
                }
            }
            if (passage.length() > 0) {
                lines.add(passage.toString().trim());
            }
        } catch (IOException e) {
            System.out.println("Error reading texts: " + e.getMessage());
            return null;
        }
        if (lines.isEmpty()) return null;
        return lines.get(new Random().nextInt(lines.size()));
    }
}
