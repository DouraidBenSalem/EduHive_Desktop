package Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeminiAIService {
    private static final String API_KEY = "AIzaSyC5FaOVq8k8A23AlvGoM-dA19O6_BbDAns";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public String generateContent(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format("{"
                    + "\"contents\": [{"
                    + "\"parts\": [{"
                    + "\"text\": \"%s\""
                    + "}]"
                    + "}],"
                    + "\"generationConfig\": {"
                    + "\"temperature\": 0.7,"
                    + "\"topK\": 40,"
                    + "\"topP\": 0.95,"
                    + "\"maxOutputTokens\": 1024"
                    + "}"
                    + "}", prompt.replace("\"", "\\\""));

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Log the response for debugging
            System.out.println("Response: " + response.toString());
            
            // Parse and extract the text from the response
            String responseText = response.toString();
            if (responseText.contains("\"text\":")) {
                int textStart = responseText.indexOf("\"text\": \"") + 9;
                int textEnd = responseText.indexOf("\"", textStart);
                String text = responseText.substring(textStart, textEnd);
                return text.replace("\\n", "\n");
            } else {
                throw new RuntimeException("Unexpected response format: " + responseText);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error generating content: " + e.getMessage());
        }
    }
}
