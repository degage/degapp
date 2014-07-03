package notifiers;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import play.Logger;
import play.Play;

/**
 * Created by Stefaan Vermassen on 16/02/14.
 */
public class Mailer {
    private static MailerAPI mail = play.Play.application()
            .plugin(MailerPlugin.class).email();

    public static MailerAPI getMail() {
        return mail;
    }

    public static void setSubject(String subject) {
        mail.setSubject(subject);
    }

    public static void setSubject(String pattern, String subjects) {
        mail.setSubject(pattern, subjects);
    }

    // TODO: rename to setRecipient and check whether this is called only once for each mail message - same with addFrom
    public static void addRecipient(String recipient) {
        mail.setRecipient(recipient);
    }

    public static void addRecipient(String... recipients) {
        mail.setRecipient(recipients);
    }

    public static void addFrom(String from) {
        mail.setFrom(from);
    }

    public static void send(String html) {
        if(!Play.isDev()) {
            mail.sendHtml(html);
        } else {
            Logger.debug("Sent mail: " + html);
        }
    }

    public static void sendText(String text) {
        mail.send(text);
    }

    public static String getEmailAndNameFormatted(String name, String email) {
        return name + " <" + email + ">";
    }
}
