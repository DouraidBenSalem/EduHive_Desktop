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
            
            // Améliorer les termes de recherche en fonction du nom de la matière
            String searchTerm = enrichSearchTerm(nomMatiere);
            
            // Encoder le terme de recherche
            String encodedQuery = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());

            // Construire l'URL de l'API avec des paramètres améliorés
            String urlString = "https://www.googleapis.com/customsearch/v1?q=" + encodedQuery +
                    "&key=" + API_KEY +
                    "&cx=" + SEARCH_ENGINE_ID +
                    "&searchType=image" +
                    "&imgSize=large" +  // Préférer des images de meilleure qualité
                    "&imgType=photo" +  // Préférer des photos plutôt que des cliparts
                    "&safe=active" +    // Filtrer le contenu inapproprié
                    "&num=3";           // Récupérer plusieurs résultats pour avoir plus de choix

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);  // Timeout de 5 secondes
            connection.setReadTimeout(5000);     // Timeout de lecture de 5 secondes

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Extraire les URLs des images de la réponse JSON
                Pattern pattern = Pattern.compile("\"link\":\s*\"(https?://[^\"]+)\"");
                Matcher matcher = pattern.matcher(response.toString());
                
                // Liste pour stocker les URLs trouvées
                java.util.List<String> imageUrls = new java.util.ArrayList<>();
                
                // Collecter toutes les URLs trouvées
                while (matcher.find()) {
                    imageUrls.add(matcher.group(1));
                }
                
                // Si des URLs ont été trouvées, retourner la première
                if (!imageUrls.isEmpty()) {
                    return imageUrls.get(0);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la recherche d'image: " + e.getMessage());
        }

        // En cas d'échec, utiliser la méthode alternative
        return searchImageAlternative(nomMatiere);
    }
    
    /**
     * Enrichit le terme de recherche en fonction du nom de la matière
     * pour obtenir des résultats plus pertinents
     * 
     * @param nomMatiere Le nom de la matière
     * @return Un terme de recherche enrichi
     */
    private static String enrichSearchTerm(String nomMatiere) {
        String normalizedName = nomMatiere.toLowerCase().trim();
        String searchTerm = nomMatiere;
        
        // Ajouter des termes spécifiques en fonction de la catégorie de matière
        if (normalizedName.contains("math") || normalizedName.contains("algèbre") || 
            normalizedName.contains("géométrie") || normalizedName.contains("calcul")) {
            searchTerm += " mathématiques formules éducation illustration";
        } 
        else if (normalizedName.contains("physique") || normalizedName.contains("mécanique") || 
                 normalizedName.contains("électricité")) {
            searchTerm += " physique science laboratoire éducation";
        }
        else if (normalizedName.contains("chimie") || normalizedName.contains("organique") || 
                 normalizedName.contains("biochimie")) {
            searchTerm += " chimie molécule laboratoire science éducation";
        }
        else if (normalizedName.contains("biologie") || normalizedName.contains("anatomie") || 
                 normalizedName.contains("génétique")) {
            searchTerm += " biologie science cellule ADN éducation";
        }
        else if (normalizedName.contains("histoire") || normalizedName.contains("géographie")) {
            searchTerm += " histoire livre ancien carte éducation";
        }
        else if (normalizedName.contains("informatique") || normalizedName.contains("programmation") || 
                 normalizedName.contains("algorithme")) {
            searchTerm += " informatique code programmation technologie éducation";
        }
        else if (normalizedName.contains("langue") || normalizedName.contains("français") || 
                 normalizedName.contains("anglais") || normalizedName.contains("espagnol")) {
            searchTerm += " langue apprentissage communication international éducation";
        }
        else if (normalizedName.contains("art") || normalizedName.contains("musique") || 
                 normalizedName.contains("peinture")) {
            searchTerm += " art créativité expression culture éducation";
        }
        else if (normalizedName.contains("économie") || normalizedName.contains("gestion") || 
                 normalizedName.contains("finance")) {
            searchTerm += " économie business graphique analyse éducation";
        }
        else if (normalizedName.contains("philosophie") || normalizedName.contains("éthique")) {
            searchTerm += " philosophie pensée réflexion livre éducation";
        }
        else if (normalizedName.contains("sport") || normalizedName.contains("éducation physique")) {
            searchTerm += " sport activité physique santé éducation";
        }
        else {
            // Terme générique pour les autres matières
            searchTerm += " éducation cours apprentissage illustration";
        }
        
        return searchTerm;
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

        // Structure de données pour stocker les correspondances entre mots-clés et URLs
        // d'images
        java.util.Map<String, String> imageMap = new java.util.HashMap<>();

        // Mathématiques et disciplines connexes
        imageMap.put("math", "https://cdn.pixabay.com/photo/2015/11/15/07/47/geometry-1044090_960_720.jpg");
        imageMap.put("mathématique", "https://cdn.pixabay.com/photo/2015/11/15/07/47/geometry-1044090_960_720.jpg");
        imageMap.put("algèbre", "https://cdn.pixabay.com/photo/2015/11/03/09/03/mathematics-1019790_960_720.jpg");
        imageMap.put("géométrie", "https://cdn.pixabay.com/photo/2017/01/31/23/42/geometric-2028092_960_720.jpg");
        imageMap.put("calcul", "https://cdn.pixabay.com/photo/2016/11/18/18/03/calculator-1836292_960_720.jpg");
        imageMap.put("statistique", "https://cdn.pixabay.com/photo/2017/08/30/07/56/money-2696228_960_720.jpg");

        // Sciences physiques
        imageMap.put("physique", "https://cdn.pixabay.com/photo/2018/02/26/10/11/nuclear-3182970_960_720.jpg");
        imageMap.put("mécanique", "https://cdn.pixabay.com/photo/2017/08/10/05/10/gear-2618160_960_720.jpg");
        imageMap.put("électricité", "https://cdn.pixabay.com/photo/2017/01/31/13/40/light-2023911_960_720.jpg");
        imageMap.put("électronique", "https://cdn.pixabay.com/photo/2017/03/23/12/32/arduino-2168193_960_720.jpg");
        imageMap.put("optique", "https://cdn.pixabay.com/photo/2016/06/29/22/02/glasses-1487195_960_720.jpg");

        // Chimie
        imageMap.put("chimie", "https://cdn.pixabay.com/photo/2019/09/17/18/48/laboratory-4484216_960_720.jpg");
        imageMap.put("organique", "https://cdn.pixabay.com/photo/2018/09/28/19/07/molecule-3709419_960_720.jpg");
        imageMap.put("biochimie", "https://cdn.pixabay.com/photo/2019/04/08/20/26/dna-4112788_960_720.jpg");

        // Biologie et sciences naturelles
        imageMap.put("biologie", "https://cdn.pixabay.com/photo/2018/07/15/10/44/dna-3539309_960_720.jpg");
        imageMap.put("anatomie", "https://cdn.pixabay.com/photo/2016/11/09/15/27/dna-1811955_960_720.jpg");
        imageMap.put("génétique", "https://cdn.pixabay.com/photo/2018/07/15/10/44/dna-3539309_960_720.jpg");
        imageMap.put("botanique", "https://cdn.pixabay.com/photo/2015/12/01/20/28/green-1072828_960_720.jpg");
        imageMap.put("zoologie", "https://cdn.pixabay.com/photo/2017/01/14/12/59/iceland-1979445_960_720.jpg");
        imageMap.put("écologie", "https://cdn.pixabay.com/photo/2015/12/01/20/28/road-1072823_960_720.jpg");

        // Sciences humaines
        imageMap.put("histoire", "https://cdn.pixabay.com/photo/2016/06/01/06/26/open-book-1428428_960_720.jpg");
        imageMap.put("géographie", "https://cdn.pixabay.com/photo/2016/10/12/02/54/globe-1733860_960_720.jpg");
        imageMap.put("sociologie", "https://cdn.pixabay.com/photo/2017/08/30/12/45/girl-2696947_960_720.jpg");
        imageMap.put("psychologie", "https://cdn.pixabay.com/photo/2016/11/08/05/29/surgery-1807541_960_720.jpg");
        imageMap.put("anthropologie", "https://cdn.pixabay.com/photo/2016/11/23/15/32/pedestrians-1853552_960_720.jpg");

        // Informatique et technologies
        imageMap.put("informatique", "https://cdn.pixabay.com/photo/2016/11/19/14/00/code-1839406_960_720.jpg");
        imageMap.put("programmation", "https://cdn.pixabay.com/photo/2016/11/30/20/58/programming-1873854_960_720.jpg");
        imageMap.put("algorithme", "https://cdn.pixabay.com/photo/2017/05/10/19/29/robot-2301646_960_720.jpg");
        imageMap.put("réseau", "https://cdn.pixabay.com/photo/2017/12/22/08/01/networking-3033203_960_720.jpg");
        imageMap.put("base de données", "https://cdn.pixabay.com/photo/2017/06/14/16/20/network-2402637_960_720.jpg");
        imageMap.put("web", "https://cdn.pixabay.com/photo/2016/12/19/08/39/mobile-phone-1917737_960_720.jpg");
        imageMap.put("java", "https://cdn.pixabay.com/photo/2015/04/23/17/41/javascript-736400_960_720.png");
        imageMap.put("python", "https://cdn.pixabay.com/photo/2017/01/31/15/33/python-2025598_960_720.png");

        // Langues et littérature
        imageMap.put("langue", "https://cdn.pixabay.com/photo/2015/07/28/22/11/student-865073_960_720.jpg");
        imageMap.put("français", "https://cdn.pixabay.com/photo/2018/01/14/23/12/eiffel-tower-3082066_960_720.jpg");
        imageMap.put("anglais", "https://cdn.pixabay.com/photo/2015/11/06/13/29/union-jack-1027898_960_720.jpg");
        imageMap.put("espagnol", "https://cdn.pixabay.com/photo/2017/08/27/17/43/spain-2686694_960_720.jpg");
        imageMap.put("allemand", "https://cdn.pixabay.com/photo/2018/01/23/23/36/flag-3102627_960_720.jpg");
        imageMap.put("arabe", "https://cdn.pixabay.com/photo/2017/08/30/22/53/lamp-2698770_960_720.jpg");
        imageMap.put("littérature", "https://cdn.pixabay.com/photo/2016/09/10/17/18/book-1659717_960_720.jpg");
        imageMap.put("grammaire", "https://cdn.pixabay.com/photo/2015/11/19/21/14/glasses-1052023_960_720.jpg");

        // Arts et culture
        imageMap.put("art", "https://cdn.pixabay.com/photo/2016/11/18/13/03/brush-1834621_960_720.jpg");
        imageMap.put("musique", "https://cdn.pixabay.com/photo/2015/05/07/11/02/guitar-756326_960_720.jpg");
        imageMap.put("peinture", "https://cdn.pixabay.com/photo/2016/11/18/13/03/brush-1834621_960_720.jpg");
        imageMap.put("dessin", "https://cdn.pixabay.com/photo/2017/08/10/02/05/tiles-shapes-2617112_960_720.jpg");
        imageMap.put("théâtre", "https://cdn.pixabay.com/photo/2016/11/22/19/15/audience-1850119_960_720.jpg");
        imageMap.put("cinéma", "https://cdn.pixabay.com/photo/2016/11/29/06/18/home-theater-1867900_960_720.jpg");

        // Économie et gestion
        imageMap.put("économie", "https://cdn.pixabay.com/photo/2017/12/04/10/31/business-2996754_960_720.jpg");
        imageMap.put("gestion", "https://cdn.pixabay.com/photo/2015/01/09/11/08/startup-594090_960_720.jpg");
        imageMap.put("finance", "https://cdn.pixabay.com/photo/2016/11/27/21/42/stock-1863880_960_720.jpg");
        imageMap.put("marketing", "https://cdn.pixabay.com/photo/2018/03/27/21/43/startup-3267505_960_720.jpg");
        imageMap.put("comptabilité", "https://cdn.pixabay.com/photo/2016/11/18/17/20/chart-1835840_960_720.jpg");

        // Philosophie et pensée
        imageMap.put("philosophie", "https://cdn.pixabay.com/photo/2016/03/27/19/32/book-1283865_960_720.jpg");
        imageMap.put("éthique", "https://cdn.pixabay.com/photo/2016/11/18/16/59/architecture-1835647_960_720.jpg");
        imageMap.put("logique", "https://cdn.pixabay.com/photo/2017/03/28/12/11/chairs-2181960_960_720.jpg");

        // Éducation physique et sports
        imageMap.put("sport", "https://cdn.pixabay.com/photo/2017/08/07/14/02/people-2604149_960_720.jpg");
        imageMap.put("éducation physique",
                "https://cdn.pixabay.com/photo/2014/11/17/13/17/crossfit-534615_960_720.jpg");
        imageMap.put("football", "https://cdn.pixabay.com/photo/2016/06/15/18/12/football-1459278_960_720.jpg");
        imageMap.put("basketball", "https://cdn.pixabay.com/photo/2016/11/29/03/53/athletes-1867185_960_720.jpg");
        imageMap.put("tennis", "https://cdn.pixabay.com/photo/2016/08/14/19/32/tennis-1593110_960_720.jpg");

        // Technologie et ingénierie
        imageMap.put("technologie",
                "https://cdn.pixabay.com/photo/2018/05/08/08/44/artificial-intelligence-3382507_960_720.jpg");
        imageMap.put("ingénierie", "https://cdn.pixabay.com/photo/2017/05/10/19/29/robot-2301646_960_720.jpg");
        imageMap.put("robotique", "https://cdn.pixabay.com/photo/2017/05/10/19/29/robot-2301646_960_720.jpg");
        imageMap.put("mécanique", "https://cdn.pixabay.com/photo/2015/07/11/14/53/plumbing-840835_960_720.jpg");

        // Médecine et santé
        imageMap.put("médecine", "https://cdn.pixabay.com/photo/2016/11/09/15/27/dna-1811955_960_720.jpg");
        imageMap.put("santé", "https://cdn.pixabay.com/photo/2014/12/10/20/56/medical-563427_960_720.jpg");
        imageMap.put("pharmacie", "https://cdn.pixabay.com/photo/2016/11/23/15/14/bottles-1853440_960_720.jpg");

        // Recherche du meilleur match
        for (java.util.Map.Entry<String, String> entry : imageMap.entrySet()) {
            if (normalizedName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Si aucun mot-clé ne correspond, utiliser une image par défaut basée sur la
        // première lettre
        char firstChar = normalizedName.isEmpty() ? 'a' : normalizedName.charAt(0);

        if (firstChar >= 'a' && firstChar <= 'c') {
            return "https://cdn.pixabay.com/photo/2015/07/31/11/45/library-869061_960_720.jpg";
        } else if (firstChar >= 'd' && firstChar <= 'f') {
            return "https://cdn.pixabay.com/photo/2015/11/19/21/10/knowledge-1052010_960_720.jpg";
        } else if (firstChar >= 'g' && firstChar <= 'i') {
            return "https://cdn.pixabay.com/photo/2016/01/19/01/42/library-1147815_960_720.jpg";
        } else if (firstChar >= 'j' && firstChar <= 'l') {
            return "https://cdn.pixabay.com/photo/2017/02/01/11/30/doors-2029667_960_720.jpg";
        } else if (firstChar >= 'm' && firstChar <= 'o') {
            return "https://cdn.pixabay.com/photo/2016/03/26/22/21/books-1281581_960_720.jpg";
        } else if (firstChar >= 'p' && firstChar <= 'r') {
            return "https://cdn.pixabay.com/photo/2015/11/19/21/11/book-1052014_960_720.jpg";
        } else if (firstChar >= 's' && firstChar <= 'u') {
            return "https://cdn.pixabay.com/photo/2016/09/08/22/43/books-1655783_960_720.jpg";
        } else {
            return "https://cdn.pixabay.com/photo/2015/07/31/11/45/library-869061_960_720.jpg";
        }
    }
}