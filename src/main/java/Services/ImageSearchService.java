package Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageSearchService {

    private static String API_KEY;
    private static String SEARCH_ENGINE_ID;

    static {
        try {
            // Charger les propriétés depuis le fichier de configuration
            java.util.Properties props = new java.util.Properties();
            props.load(ImageSearchService.class.getResourceAsStream("/config.properties"));

            // Initialiser les clés API
            API_KEY = props.getProperty("google.search.api.key");
            SEARCH_ENGINE_ID = props.getProperty("google.search.engine.id");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la configuration: " + e.getMessage());
            // Initialiser avec des valeurs par défaut en cas d'erreur
            API_KEY = "VOTRE_CLE_API";
            SEARCH_ENGINE_ID = "VOTRE_SEARCH_ENGINE_ID";
        }
    }

    /**
     * Recherche une image en fonction du nom de la matière
     * 
     * @param nomMatiere Le nom de la matière pour laquelle rechercher une image
     * @return L'URL de l'image trouvée, ou null si aucune image n'est trouvée
     */
    public static String searchImageForMatiere(String nomMatiere) {
        try {
            // Si l'API n'est pas configurée, utiliser une méthode alternative
            if (API_KEY == null || API_KEY.equals("VOTRE_CLE_API") || SEARCH_ENGINE_ID == null
                    || SEARCH_ENGINE_ID.equals("VOTRE_SEARCH_ENGINE_ID")) {
                return searchImageAlternative(nomMatiere);
            }

            // Encoder le terme de recherche
            String encodedQuery = URLEncoder.encode(nomMatiere + " cours éducation", StandardCharsets.UTF_8.toString());

            // Construire l'URL de l'API
            String urlString = "https://www.googleapis.com/customsearch/v1?q=" + encodedQuery +
                    "&key=" + API_KEY +
                    "&cx=" + SEARCH_ENGINE_ID +
                    "&searchType=image" +
                    "&imgSize=medium" +
                    "&num=1";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Extraire l'URL de l'image de la réponse JSON
                Pattern pattern = Pattern.compile("\"link\":\s*\"(https?://[^\"]+)\"");
                Matcher matcher = pattern.matcher(response.toString());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la recherche d'image: " + e.getMessage());
        }

        // En cas d'échec, utiliser la méthode alternative
        return searchImageAlternative(nomMatiere);
    }

    /**
     * Méthode alternative pour obtenir une image basée sur le nom de la matière
     * Cette méthode est utilisée si l'API Google n'est pas configurée
     * 
     * @param nomMatiere Le nom de la matière
     * @return Une URL d'image par défaut basée sur le nom de la matière
     */
    private static String searchImageAlternative(String nomMatiere) {
        // Normaliser le nom de la matière pour la recherche
        String normalizedName = nomMatiere.toLowerCase().trim();

        // Catégories d'images par défaut basées sur des mots-clés courants
        if (normalizedName.contains("math") || normalizedName.contains("mathématique")) {
            return "https://cdn.pixabay.com/photo/2015/11/15/07/47/geometry-1044090_960_720.jpg";
        } else if (normalizedName.contains("physique")) {
            return "https://cdn.pixabay.com/photo/2018/02/26/10/11/nuclear-3182970_960_720.jpg";
        } else if (normalizedName.contains("chimie")) {
            return "https://cdn.pixabay.com/photo/2019/09/17/18/48/laboratory-4484216_960_720.jpg";
        } else if (normalizedName.contains("biologie")) {
            return "https://cdn.pixabay.com/photo/2018/07/15/10/44/dna-3539309_960_720.jpg";
        } else if (normalizedName.contains("histoire")) {
            return "https://cdn.pixabay.com/photo/2016/06/01/06/26/open-book-1428428_960_720.jpg";
        } else if (normalizedName.contains("géographie")) {
            return "https://cdn.pixabay.com/photo/2016/10/12/02/54/globe-1733860_960_720.jpg";
        } else if (normalizedName.contains("informatique") || normalizedName.contains("programmation")) {
            return "https://cdn.pixabay.com/photo/2016/11/19/14/00/code-1839406_960_720.jpg";
        } else if (normalizedName.contains("langue") || normalizedName.contains("français")
                || normalizedName.contains("anglais")) {
            return "https://cdn.pixabay.com/photo/2015/07/28/22/11/student-865073_960_720.jpg";
        } else if (normalizedName.contains("art") || normalizedName.contains("musique")) {
            return "https://cdn.pixabay.com/photo/2016/11/18/13/03/brush-1834621_960_720.jpg";
        } else if (normalizedName.contains("économie") || normalizedName.contains("gestion")) {
            return "https://cdn.pixabay.com/photo/2017/12/04/10/31/business-2996754_960_720.jpg";
        } else if (normalizedName.contains("philosophie")) {
            return "https://cdn.pixabay.com/photo/2016/03/27/19/32/book-1283865_960_720.jpg";
        } else if (normalizedName.contains("sport") || normalizedName.contains("éducation physique")) {
            return "https://cdn.pixabay.com/photo/2017/08/07/14/02/people-2604149_960_720.jpg";
        } else if (normalizedName.contains("technologie")) {
            return "https://cdn.pixabay.com/photo/2018/05/08/08/44/artificial-intelligence-3382507_960_720.jpg";
        } else if (normalizedName.contains("littérature")) {
            return "https://cdn.pixabay.com/photo/2016/09/10/17/18/book-1659717_960_720.jpg";
        } else {
            // Image par défaut pour les autres matières
            return "https://cdn.pixabay.com/photo/2015/11/19/21/10/knowledge-1052010_960_720.jpg";
        }
    }
}