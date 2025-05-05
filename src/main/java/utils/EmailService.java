package utils;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;

import java.util.*;

public class EmailService {

    private static final String BREVO_API_KEY = "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";
    private static final String FROM_EMAIL = "douraid7d@gmail.com";
    private static final String FROM_NAME = "EduHive";

    private static ApiClient getApiClient() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(BREVO_API_KEY);
        return defaultClient;
    }

    public static void sendQuizResultEmail(String toEmail, String userName, int score, int correctAnswers, int totalQuestions) {
        try {
       
            String logoUrl = "https://raw.githubusercontent.com/DouraidBenSalem/EduHive_Desktop/main/src/main/resources/style_css/images/logo.png";


            String completionBadge = score >= 10 ?
                    "<div style='padding: 12px 24px; background: linear-gradient(135deg, #4CAF50, #8BC34A); color: white; font-size: 20px; font-weight: bold; border-radius: 8px; display: inline-block; box-shadow: 0 6px 15px rgba(0,0,0,0.2); text-shadow: 1px 1px 3px rgba(0,0,0,0.3); min-width: 200px;'>🎯Quizz Complété</div>" :
                    "<div style='padding: 12px 24px; background: linear-gradient(135deg, #FF9800, #FFB74D); color: white; font-size: 20px; font-weight: bold; border-radius: 8px; display: inline-block; box-shadow: 0 6px 15px rgba(0,0,0,0.2); text-shadow: 1px 1px 3px rgba(0,0,0,0.3); min-width: 200px;'>⚠️Quizz Incomplet</div>";

            String motivationBadge = score >= 16 ?
                    "<div style='padding: 12px 24px; background: linear-gradient(135deg, #2196F3, #64B5F6); color: white; font-size: 20px; font-weight: bold; border-radius: 8px; display: inline-block; box-shadow: 0 6px 15px rgba(0,0,0,0.2); text-shadow: 1px 1px 3px rgba(0,0,0,0.3); min-width: 200px;'>🏆 Excellente Motivation</div>" :
                    score >= 12 ?
                            "<div style='padding: 12px 24px; background: linear-gradient(135deg, #FFEB3B, #FFF176); color: black; font-size: 20px; font-weight: bold; border-radius: 8px; display: inline-block; box-shadow: 0 6px 15px rgba(0,0,0,0.2); text-shadow: 1px 1px 2px rgba(0,0,0,0.2); min-width: 200px;'>💪 Bonne Motivation</div>" :
                            "<div style='padding: 12px 24px; background: linear-gradient(135deg, #FFC107, #FFD54F); color: black; font-size: 20px; font-weight: bold; border-radius: 8px; display: inline-block; box-shadow: 0 6px 15px rgba(0,0,0,0.2); text-shadow: 1px 1px 2px rgba(0,0,0,0.2); min-width: 200px;'>🚀 Continuez !</div>";
            String htmlContent = String.format(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f2f4f8; padding: 20px; border-radius: 12px;'>"
                            + "<div style='text-align: center; margin-bottom: 20px;'>"
                            + "<img src='%s' alt='EduHive Logo' style='width: 120px; height: auto;'>"
                            + "</div>"
                            + "<div style='background-color: #ffffff; padding: 30px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>"
                            + "<h2 style='color: #3f51b5;'>Salut %s 👋,</h2>"
                            + "<p style='color: #555;'>Merci d'avoir passé un quiz avec EduHive ! Voici votre résultat :</p>"

                            + "<div style='margin: 20px 0; text-align: center;'>"
                            + "<h3 style='color: %s; font-size: 36px;'>Score : %d/20</h3>"
                            + "<p style='font-size: 18px;'>%d réponses correctes sur %d questions.</p>"
                            + "<p style='font-size: 18px;'>Félicitations ! Voici les badges que vous avez remportés !</p>"
                            + "</div>"

                            + "<div style='text-align: center; margin: 30px 0;'>"
                            + completionBadge
                            + "<div style='height: 15px;'></div>"
                            + motivationBadge
                            + "</div>"

                            + "<p style='color: #777; font-style: italic; text-align: center;'>%s</p>"

                            + "<div style='margin-top: 30px; text-align: center;'>"
                            + "<a href='https://github.com/DouraidBenSalem/EduHive_Desktop' "
                            + "style='display: inline-block; padding: 12px 24px; background-color: #3f51b5; color: #ffffff; text-decoration: none; border-radius: 25px; font-weight: bold;'>Découvrir plus</a>"
                            + "</div>"

                            + "</div>"
                            + "<div style='text-align: center; font-size: 12px; color: #aaa; margin-top: 20px;'>"
                            + "<p>© 2025 EduHive - Plateforme éducative</p>"
                            + "</div>"
                            + "</div>",
                    logoUrl,
                    userName,
                    getScoreColor(score),
                    score,
                    correctAnswers,
                    totalQuestions,
                    getEncouragement(score)
            );

     
            TransactionalEmailsApi api = new TransactionalEmailsApi(getApiClient());

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(FROM_EMAIL);
            sender.setName(FROM_NAME);

            List<SendSmtpEmailTo> toList = new ArrayList<>();
            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(toEmail);
            to.setName(userName);
            toList.add(to);

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(toList);
            sendSmtpEmail.setHtmlContent(htmlContent);
            sendSmtpEmail.setSubject("🎓 Vos Résultats de Quiz - EduHive");

            CreateSmtpEmail response = api.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ Email envoyé avec succès via Brevo. Message ID: " + response.getMessageId());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getEncouragement(int score) {
        if (score >= 16) {
            return "Excellent travail ! Vous maîtrisez parfaitement le sujet !";
        } else if (score >= 12) {
            return "Très bon résultat ! Continuez sur cette voie !";
        } else if (score >= 8) {
            return "Vous êtes sur la bonne voie. Un peu plus de pratique et vous y arriverez !";
        } else {
            return "Ne vous découragez pas ! Chaque erreur est une opportunité d'apprentissage.";
        }
    }

    private static String getScoreColor(int score) {
        if (score >= 16) {
            return "#4CAF50";
        } else if (score >= 10) {
            return "#FFC107";
        } else {
            return "#F44336";
        }
    }
}
