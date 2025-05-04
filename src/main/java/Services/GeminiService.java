package Services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service pour interagir avec l'API Gemini pour la génération
 * de descriptions de cours.
 * Cette classe permet d'utiliser le modèle Gemini pour générer des descriptions
 * basées sur le nom du cours.
 */
public class GeminiService {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String CONFIG_FILE = "/config.properties";
    private static final String API_KEY_PROPERTY = "google.gemini.api.key";

    private final String apiKey;
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructeur du service Gemini.
     * Charge automatiquement la clé API depuis le fichier config.properties
     */
    public GeminiService() {
        this.apiKey = loadApiKey();
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Charge la clé API depuis le fichier de configuration
     * 
     * @return La clé API Gemini
     */
    private String loadApiKey() {
        Properties properties = new Properties();
        try (InputStream input = GeminiService.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Impossible de trouver " + CONFIG_FILE);
                return "";
            }
            properties.load(input);
            String apiKey = properties.getProperty(API_KEY_PROPERTY);
            if (apiKey == null || apiKey.isEmpty()) {
                // Utiliser la clé de recherche Google comme fallback temporaire
                apiKey = properties.getProperty("google.search.api.key", "");
                System.out.println(
                        "Clé API Gemini non trouvée, utilisation de la clé de recherche Google comme fallback temporaire");
            }
            return apiKey;
        } catch (IOException ex) {
            System.err.println("Erreur lors du chargement du fichier de configuration: " + ex.getMessage());
            return "";
        }
    }

    /**
     * Génère une description de cours basée sur le nom du cours.
     * 
     * @param courseName Le nom du cours
     * @param niveau     Le niveau du cours (débutant, intermédiaire, avancé)
     * @return La description générée
     * @throws IOException          En cas d'erreur de communication avec l'API
     * @throws InterruptedException En cas d'interruption de la requête
     */
    public String generateCourseDescription(String courseName, String niveau) throws IOException, InterruptedException {
        // Vérifier si la clé API est disponible
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("ERREUR: Clé API Gemini non disponible ou vide");
            return "Erreur: Clé API Gemini non disponible. Utilisation du mode hors ligne.";
        }

        System.out.println("INFO: Tentative de génération de description pour le cours '" + courseName
                + "' avec la clé API: " + apiKey.substring(0, 5) + "...");

        // Préparer le prompt pour Gemini
        String prompt = String.format(
                "Génère une description éducative détaillée en français pour un cours intitulé '%s' " +
                        "de niveau %s. La description doit être informative, engageante et adaptée à un contexte éducatif. "
                        +
                        "Elle doit faire entre 2 et 4 phrases et expliquer ce que les étudiants vont apprendre.",
                courseName, niveau);

        // Préparer les paramètres pour l'API
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        content.put("parts", new Object[] { parts });

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", new Object[] { content });

        // Convertir en JSON
        String jsonPayload = objectMapper.writeValueAsString(payload);
        System.out.println("INFO: Payload JSON envoyé à l'API: " + jsonPayload);

        // Préparer la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(60))
                .build();

        try {
            // Envoyer la requête et récupérer la réponse
            System.out.println("INFO: Envoi de la requête à l'API Gemini...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("INFO: Réponse reçue avec le code de statut: " + response.statusCode());

            // Vérifier le code de statut
            if (response.statusCode() == 200) {
                // Extraire la description de la réponse JSON
                try {
                    String responseBody = response.body();
                    System.out.println("INFO: Corps de la réponse: " + responseBody);

                    Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                    if (responseMap.containsKey("candidates")) {
                        // Correction du cast: utilisation de List au lieu de Object[]
                        List<?> candidatesList = (List<?>) responseMap.get("candidates");
                        if (!candidatesList.isEmpty()) {
                            Map<String, Object> candidate = (Map<String, Object>) candidatesList.get(0);
                            if (candidate.containsKey("content")) {
                                Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                                if (contentMap.containsKey("parts")) {
                                    // Correction du cast: utilisation de List au lieu de Object[]
                                    List<?> contentPartsList = (List<?>) contentMap.get("parts");
                                    if (!contentPartsList.isEmpty()) {
                                        Map<String, Object> part = (Map<String, Object>) contentPartsList.get(0);
                                        if (part.containsKey("text")) {
                                            String description = part.get("text").toString().trim();
                                            System.out.println("INFO: Description générée avec succès: "
                                                    + description.substring(0, Math.min(50, description.length()))
                                                    + "...");
                                            return description;
                                        } else {
                                            System.err.println(
                                                    "ERREUR: Clé 'text' non trouvée dans la partie de contenu");
                                        }
                                    } else {
                                        System.err.println("ERREUR: Liste 'parts' vide");
                                    }
                                } else {
                                    System.err.println("ERREUR: Clé 'parts' non trouvée dans le contenu");
                                }
                            } else {
                                System.err.println("ERREUR: Clé 'content' non trouvée dans le candidat");
                            }
                        } else {
                            System.err.println("ERREUR: Liste 'candidates' vide");
                        }
                    } else {
                        System.err.println("ERREUR: Clé 'candidates' non trouvée dans la réponse");
                        System.err.println("Contenu de la réponse: " + responseBody);
                    }
                    return "Erreur: Structure de réponse de l'API Gemini inattendue. Utilisation du mode hors ligne.";
                } catch (Exception e) {
                    System.err.println("ERREUR lors du traitement de la réponse JSON: " + e.getMessage());
                    e.printStackTrace();
                    return "Erreur: Impossible de traiter la réponse de l'API Gemini: " + e.getMessage();
                }
            } else {
                // Erreur HTTP
                System.err.println("ERREUR HTTP: " + response.statusCode());
                System.err.println("Corps de la réponse d'erreur: " + response.body());
                return "Erreur: L'API Gemini a retourné une erreur (code " + response.statusCode() + "): "
                        + response.body();
            }
        } catch (Exception e) {
            System.err.println(
                    "EXCEPTION lors de l'appel à l'API Gemini: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return "Erreur: Exception lors de l'appel à l'API Gemini: " + e.getMessage();
        }
    }

    /**
     * Méthode alternative pour générer une description sans API
     * Utilisée comme fallback si l'API n'est pas disponible
     * 
     * @param courseName Le nom du cours
     * @param niveau     Le niveau du cours
     * @return Une description générique basée sur le nom du cours
     */
    public String generateDescriptionOffline(String courseName, String niveau) {
        String courseLower = courseName.toLowerCase();
        String description;

        if (courseLower.contains("math") || courseLower.contains("mathématique")) {
            description = "Ce cours de mathématiques vous permettra de maîtriser les concepts fondamentaux et d'acquérir une solide compréhension des principes mathématiques. Vous développerez des compétences en résolution de problèmes et en raisonnement logique.";
        } else if (courseLower.contains("physique")) {
            description = "Ce cours de physique explore les lois fondamentales qui régissent notre univers. Vous étudierez les principes théoriques tout en réalisant des expériences pratiques pour consolider votre compréhension des phénomènes physiques.";
        } else if (courseLower.contains("chimie")) {
            description = "Ce cours de chimie vous initie aux principes fondamentaux de la matière et de ses transformations. Vous découvrirez les éléments, les réactions chimiques et leurs applications dans notre quotidien et dans l'industrie.";
        } else if (courseLower.contains("biologie")) {
            description = "Ce cours de biologie vous plonge dans l'étude fascinante du vivant. Vous explorerez les cellules, les organismes et les écosystèmes pour comprendre les mécanismes complexes qui soutiennent la vie sur Terre.";
        } else if (courseLower.contains("histoire")) {
            description = "Ce cours d'histoire vous invite à explorer les événements majeurs qui ont façonné notre monde. Vous analyserez les causes et conséquences des grands moments historiques et développerez une perspective critique sur le passé.";
        } else if (courseLower.contains("géographie")) {
            description = "Ce cours de géographie vous permet d'étudier les interactions entre l'homme et son environnement. Vous analyserez les phénomènes physiques, humains et économiques qui façonnent notre planète.";
        } else if (courseLower.contains("informatique") || courseLower.contains("programmation")) {
            description = "Ce cours d'informatique vous initie aux fondamentaux de la programmation et des systèmes informatiques. Vous développerez des compétences pratiques en résolution de problèmes et en conception de solutions logicielles.";
        } else if (courseLower.contains("langue") || courseLower.contains("français")
                || courseLower.contains("anglais")) {
            description = "Ce cours de langue vous permettra de développer vos compétences en communication écrite et orale. Vous enrichirez votre vocabulaire et approfondirez votre maîtrise des structures grammaticales.";
        } else if (courseLower.contains("art") || courseLower.contains("musique")) {
            description = "Ce cours artistique vous invite à explorer votre créativité et à développer vos compétences techniques. Vous découvrirez différentes formes d'expression et apprendrez à analyser et apprécier les œuvres.";
        } else if (courseLower.contains("économie") || courseLower.contains("gestion")) {
            description = "Ce cours d'économie vous initie aux principes fondamentaux qui régissent les marchés et les échanges. Vous analyserez les mécanismes économiques et développerez une compréhension des enjeux contemporains.";
        } else if (courseLower.contains("machine") || courseLower.contains("learning") || courseLower.contains("ml")
                || courseLower.contains("intelligence artificielle") || courseLower.contains("ia")) {
            description = "Ce cours de machine learning vous initie aux fondamentaux de l'apprentissage automatique et de l'intelligence artificielle. Vous explorerez les algorithmes essentiels, les techniques d'analyse de données et les méthodes d'entraînement de modèles prédictifs. Vous développerez des compétences pratiques en implémentation et évaluation de solutions d'IA appliquées à des problèmes concrets.";
        } else {
            description = "Ce cours de " + courseName
                    + " vous permettra d'acquérir des connaissances essentielles dans ce domaine d'étude. Vous développerez des compétences théoriques et pratiques qui vous seront utiles dans votre parcours académique et professionnel.";
        }

        // Adapter en fonction du niveau
        if (niveau.toLowerCase().contains("débutant") || niveau.toLowerCase().contains("debutant")) {
            description += " Aucun prérequis n'est nécessaire pour suivre ce cours de niveau débutant.";
        } else if (niveau.toLowerCase().contains("intermédiaire") || niveau.toLowerCase().contains("intermediaire")) {
            description += " Ce cours de niveau intermédiaire s'appuie sur des connaissances de base dans le domaine.";
        } else if (niveau.toLowerCase().contains("avancé") || niveau.toLowerCase().contains("avance")) {
            description += " Ce cours de niveau avancé approfondit des concepts complexes et s'adresse aux étudiants ayant déjà une solide formation dans la matière.";
        }

        return description;
    }
}