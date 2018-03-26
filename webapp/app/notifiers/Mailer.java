/* Mailer.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 *
 * This file is part of the Degage Web Application
 *
 * Corresponding author (see also AUTHORS.txt)
 *
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 *
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package notifiers;
import controllers.util.ConfigurationHelper;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.Play;
import play.twirl.api.Html;
import play.twirl.api.Txt;
import java.util.Map;

/**
 * Created by Stefaan Vermassen on 16/02/14.
 */
public class Mailer {

    private static final String NO_REPLY_ADDRESS = "Degage webapplicatie <noreply@degage.be>";
        // international characters not admitted here ??


    /**
     * Composes an email object with the given parameters. 
     * @param to Email address of the intended recipient
     * @param subject Subject of the email
     * @param text Text-version of the email
     * @param bcc Email address of the intended recipient as bcc
     * 
     */
    private static Email composeMail (String to, String subject, String text, String html, String bcc, byte[] data, String fileName,  String mimeType){
            Email email = new Email();
            email.setCharset("utf-8");
            email.setSubject(subject); // play plugin does not encode
            email.setFrom(NO_REPLY_ADDRESS);
            email.addTo(to);

            if (bcc != null) {
              email.addBcc(bcc);
            }

            if (text != null) {
                email.setBodyText(text);
            }
            if (html != null) {
                email.setBodyHtml(html);
            }
             if( isValidAttachmentProperties(data, fileName, mimeType)){
                email.addAttachment(fileName, data, mimeType);
            }
            return email;
    }

    /**
     * Sends an email object with an attachment
     * @param to Email address of the intended recipient
     * @param subject Subject of the email
     * @param text Text-version of the email
     * @param bcc Email address of the intended recipient as bcc
     * @param data Byte data of the attachment file
     * @param fileName Name of the attachment file
     * @param mimeType Attachment file mimetype for the mail
     * 
     */
    private static void sendMail(String to, String subject, String text, String html, String bcc, byte[] data, String fileName,  String mimeType){
        if (Play.isDev() || "yes".equalsIgnoreCase(ConfigurationHelper.getConfigurationString("smtp.block"))) {
            // TODO: make mails pluggable
            String filler = new String(new char[30]).replace("\0", "-");
            System.err.println("To: " + to);
            System.err.println("Subject: " + subject);
            System.err.println(html);
            System.err.println(filler);
            System.err.println(text);
            System.err.println(filler);
            System.err.println(fileName);
            System.err.println(mimeType);
            System.err.println("Attachment data: " + ((data != null)? data.length : 0 )+ " bytes");
        } else {
            Email email = composeMail(to, subject, text, html, bcc, data,  fileName, mimeType);
            MailerPlugin.send(email);
        }
        
    }

    private static boolean isValidAttachmentProperties ( byte[] data, String fileName,  String mimeType){
        return data != null && data.length != 0 && fileName != null && !fileName.isEmpty() && mimeType != null && !mimeType.isEmpty(); 
    }

   /**
     * Send an email
     * @param to Email address of the intended recipient
     * @param subject Subject of the email
     * @param text Text-version of the email
     * @param bcc Email address of the intended recipient as bcc
     */
    private static void sendMail (String to, String subject, String text, String html, String bcc) {
        sendMail(to, subject, text, html, bcc, null, null, null);
    }

    /**
     * Send an email
     * @param to Email address of the intended recipient
     * @param subject Subject of the email
     * @param text Text-version of the email
     */
    private static void sendMail (String to, String subject, String text, String html) {
      sendMail(to, subject, text, html, null);
    }


    /**
     * Sends mails with the given bodies. Adds signatures.
     */
    public static void sendMailWithSubjectKey(String to, String subjectKey, Txt text, Html html, Map<String, String> signatureProps) {
        sendMail (to, Messages.get("subject." + subjectKey), text, html, signatureProps);
    }

    public static void sendMail (String to, String subject, Txt text, Html html, Map<String, String> signatureProps) {
        String link = signatureProps.get("mail_signature_link");
        String image = signatureProps.get("mail_signature_image");
        sendMail(to, subject,
                text.body().trim() + views.txt.messages.signature.render().body().trim(),
                html.body().trim() + views.html.messages.signature.render(link, image).body().trim()
        );
    }


    public static void sendMail(String to, String subject, Txt text, Html html, String bcc, Map<String, String> signatureProps, byte[] data, String fileName, String mimeType){
        String link = signatureProps.get("mail_signature_link");
        String image = signatureProps.get("mail_signature_image");
        sendMail(to, subject,
                text.body().trim() + views.txt.messages.signature.render().body().trim(),
                html.body().trim() + views.html.messages.signature.render(link, image).body().trim(),
                bcc,
                data,
                fileName,
                mimeType
        );
    }

    public static void sendMail(String to, String subject, Txt text, Html html,  Map<String, String> signatureProps, byte[] data, String fileName, String mimeType){
        String link = signatureProps.get("mail_signature_link");
        String image = signatureProps.get("mail_signature_image");
        sendMail(to, subject,
                text.body().trim() + views.txt.messages.signature.render().body().trim(),
                html.body().trim() + views.html.messages.signature.render(link, image).body().trim(),
                null,
                data,
                fileName,
                mimeType
        );
    }
    public static void sendMail (String to, String subject, Txt text, Html html, String bcc, Map<String, String> signatureProps) {
        String link = signatureProps.get("mail_signature_link");
        String image = signatureProps.get("mail_signature_image");
        sendMail(to, subject,
                text.body().trim() + views.txt.messages.signature.render().body().trim(),
                html.body().trim() + views.html.messages.signature.render(link, image).body().trim(),
                bcc
        );
    }

}
