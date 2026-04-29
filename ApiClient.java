import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public class ApiClient {
    private final String endpoint;
    private final String apiKey;
    private final String model;
    private final HttpClient client;

    public ApiClient() throws IOException {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        }
        this.endpoint = props.getProperty("api.endpoint");
        this.apiKey = props.getProperty("api.key");
        this.model = props.getProperty("api.model", "deepseek-chat");
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String callApi(String systemPrompt, String userMessage) throws IOException, InterruptedException {
        String json = buildJsonBody(systemPrompt, userMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return extractContent(response.body());
    }

    private String buildJsonBody(String systemPrompt, String userMessage) {
        return "{\n" +
                "  \"model\": \"" + model + "\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escapeJson(userMessage) + "\"}\n" +
                "  ]\n" +
                "}";
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String extractContent(String responseBody) {
        String key = "\"content\":\"";
        int start = responseBody.indexOf(key);
        if (start == -1) return "Error: could not parse response";
        start += key.length();
        StringBuilder content = new StringBuilder();
        int i = start;
        while (i < responseBody.length()) {
            char c = responseBody.charAt(i);
            if (c == '\\') {
                if (i + 1 < responseBody.length()) {
                    char next = responseBody.charAt(i + 1);
                    switch (next) {
                        case 'n' -> content.append('\n');
                        case 't' -> content.append('\t');
                        case 'r' -> content.append('\r');
                        case '\\' -> content.append('\\');
                        case '"' -> content.append('"');
                        default -> { content.append(c); content.append(next); }
                    }
                    i += 2;
                } else {
                    content.append(c);
                    i++;
                }
            } else if (c == '"') {
                break; // 遇到未转义的引号 → content 字符串结束
            } else {
                content.append(c);
                i++;
            }
        }
        return content.toString();
    }
}
