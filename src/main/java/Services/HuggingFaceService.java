package Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service pour interagir avec l'API Hugging Face Inference pour la génération
 * de résumés.
 * Cette classe permet d'utiliser des modèles de résumé de texte hébergés sur
 * Hugging Face.
 */
public class HuggingFaceService {

    private static final String API_URL = "https://api-inference.huggingface.co/models/";
    private static final String DEFAULT_MODEL = "facebook/bart-large-cnn"; // Modèle de résumé par défaut

    private final String apiToken;
    private final String modelId;
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructeur avec modèle par défaut.
     * 
     * @param apiToken Le token d'API Hugging Face (créez-en un sur
     *                 huggingface.co/settings/tokens)
     */
    public HuggingFaceService(String apiToken) {
        this(apiToken, DEFAULT_MODEL);
    }

    /**
     * Constructeur avec modèle personnalisé.
     * 
     * @param apiToken Le token d'API Hugging Face
     * @param modelId  L'identifiant du modèle à utiliser (ex:
     *                 "facebook/bart-large-cnn")
     */
    public HuggingFaceService(String apiToken, String modelId) {
        this.apiToken = apiToken;
        this.modelId = modelId;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Génère un résumé à partir d'un texte en utilisant l'API Hugging Face.
     * 
     * @param text      Le texte à résumer
     * @param maxLength Longueur maximale du résumé (en tokens)
     * @param minLength Longueur minimale du résumé (en tokens)
     * @return Le résumé généré
     * @throws IOException          En cas d'erreur de communication avec l'API
     * @throws InterruptedException En cas d'interruption de la requête
     */
    public String generateSummary(String text, int maxLength, int minLength) throws IOException, InterruptedException {
        // Préparer les paramètres pour l'API
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", text);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_length", maxLength);
        parameters.put("min_length", minLength);
        parameters.put("do_sample", false); // Désactiver l'échantillonnage pour des résultats plus déterministes
        payload.put("parameters", parameters);

        // Convertir en JSON
        String jsonPayload = objectMapper.writeValueAsString(payload);

        // Préparer la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + modelId))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(60))
                .build();

        // Envoyer la requête et récupérer la réponse
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Vérifier le code de statut
        if (response.statusCode() == 200) {
            // Extraire le résumé de la réponse JSON
            try {
                // La réponse est un tableau JSON, nous prenons le premier élément
                String[] results = objectMapper.readValue(response.body(), String[].class);
                if (results.length > 0) {
                    return results[0].trim();
                }
                return "";
            } catch (Exception e) {
                // Si le format est différent, essayons un autre format de réponse
                try {
                    Map<String, Object> resultMap = objectMapper.readValue(response.body(), Map.class);
                    if (resultMap.containsKey("summary_text")) {
                        return resultMap.get("summary_text").toString().trim();
                    }
                    return response.body(); // Retourner la réponse brute si on ne peut pas l'analyser
                } catch (Exception ex) {
                    return response.body(); // Retourner la réponse brute
                }
            }
        } else if (response.statusCode() == 503) {
            // Le modèle est en cours de chargement, attendre et réessayer
            try {
                Map<String, Object> errorMap = objectMapper.readValue(response.body(), Map.class);
                if (errorMap.containsKey("estimated_time")) {
                    double waitTime = Double.parseDouble(errorMap.get("estimated_time").toString());
                    Thread.sleep((long) (waitTime * 1000));
                    return generateSummary(text, maxLength, minLength); // Réessayer
                }
            } catch (Exception e) {
                // Ignorer et continuer
            }

            // Attendre 5 secondes par défaut
            Thread.sleep(5000);
            return generateSummary(text, maxLength, minLength); // Réessayer
        } else {
            // Erreur
            throw new IOException("Erreur API Hugging Face: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Génère un résumé adapté au type demandé.
     * 
     * @param text       Le texte à résumer
     * @param resumeType Le type de résumé (Concis, Points clés, Détaillé, etc.)
     * @return Le résumé généré
     * @throws IOException          En cas d'erreur de communication avec l'API
     * @throws InterruptedException En cas d'interruption de la requête
     */
    public String generateResumeByType(String text, String resumeType) throws IOException, InterruptedException {
        // Adapter les paramètres en fonction du type de résumé
        int maxLength = 150; // Valeur par défaut
        int minLength = 50; // Valeur par défaut

        // Ajouter une instruction spécifique en fonction du type de résumé
        String instruction = "";

        switch (resumeType) {
            case "Concis":
                maxLength = 100;
                minLength = 30;
                instruction = "Résume ce texte de manière très concise en français. Inclus uniquement les informations essentielles.";
                break;
            case "Points clés":
                maxLength = 200;
                minLength = 50;
                instruction = "Extrais les points clés de ce texte sous forme de liste à puces en français.";
                break;
            case "Détaillé":
                maxLength = 300;
                minLength = 100;
                instruction = "Fais un résumé détaillé de ce texte en français, en conservant les informations importantes.";
                break;
            case "Pour débutants":
                maxLength = 250;
                minLength = 80;
                instruction = "Résume ce texte en français avec un vocabulaire simple pour des débutants. Explique les concepts complexes.";
                break;
            case "Académique":
                maxLength = 350;
                minLength = 120;
                instruction = "Fais un résumé académique de ce texte en français, en mettant l'accent sur la méthodologie et les concepts théoriques.";
                break;
        }

        // Ajouter l'instruction au début du texte pour guider le modèle
        String textWithInstruction = instruction + "\n\n" + text;

        // Générer le résumé
        String summary = generateSummary(textWithInstruction, maxLength, minLength);

        // Formater le résumé en fonction du type
        StringBuilder formattedSummary = new StringBuilder();
        formattedSummary.append("=== Résumé ").append(resumeType).append(" ===\n\n");

        // Ajouter le résumé généré
        formattedSummary.append(summary);

        return formattedSummary.toString();
    }
}