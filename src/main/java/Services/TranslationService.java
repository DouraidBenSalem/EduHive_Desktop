package Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class TranslationService {
    private static final String MYMEMORY_API_URL = "https://api.mymemory.translated.net/get";
    private static final String API_EMAIL = "votre.email@votredomaine.com";

    public String translate(String text, String targetLang) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Nettoyer le texte d'entrée
        text = cleanHtmlTags(text);
        
        // Déterminer le code de langue
        String langCode = switch (targetLang.toLowerCase()) {
            case "french" -> "fr";
            case "spanish" -> "es";
            case "english" -> "en";
            case "italian" -> "it";
            default -> null;
        };
        
        if (langCode == null) {
            return text; // Retourner le texte original si la langue n'est pas supportée
        }

        try {
            String encodedText = java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
            String urlStr = String.format("%s?q=%s&langpair=en|%s&de=%s",
                MYMEMORY_API_URL, encodedText, langCode, API_EMAIL);

            URI uri = new URI(urlStr);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.has("responseData")) {
                        JSONObject responseData = jsonResponse.getJSONObject("responseData");
                        if (responseData.has("translatedText")) {
                            return responseData.getString("translatedText");
                        }
                    }
                }
            }
            
            System.err.println("Erreur HTTP: " + responseCode);
            return text;
            
        } catch (Exception e) {
            System.err.println("Erreur de traduction: " + e.getMessage());
            return text;
        }
    }

    private static String cleanHtmlTags(String text) {
        if (text == null) return "";
        // Supprimer les balises HTML
        text = text.replaceAll("<[^>]*>", "");
        // Remplacer les entités HTML courantes
        text = text.replaceAll("&amp;", "&")
                   .replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">")
                   .replaceAll("&quot;", "\"")
                   .replaceAll("&apos;", "'")
                   .replaceAll("&#39;", "'");
        // Supprimer les espaces multiples
        return text.replaceAll("\\s+", " ").trim();
    }
}