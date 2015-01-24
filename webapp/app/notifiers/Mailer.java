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

/**
 * Created by Stefaan Vermassen on 16/02/14.
 */
public class Mailer {

    private static final String NO_REPLY_ADDRESS = "Degage webapplicatie <noreply@degage.be>";
        // international characters not admitted here ??

    /**
     * Send an email
     * @param to Email address of the intended recipient
     * @param subject Subject of the email
     * @param text Text-version of the email
     */
    private static void sendMail (String to, String subject, String text, String html) {
        if (Play.isDev() || "yes".equalsIgnoreCase(ConfigurationHelper.getConfigurationString("smtp.block"))) {
            // TODO: make mails pluggable
            System.err.println("To: " + to);
            System.err.println("Subject: " + subject);
            System.err.println(html);
        } else {
            Email email = new Email();
            email.setCharset("utf-8");
            email.setSubject(subject); // play plugin does not encode
            email.setFrom(NO_REPLY_ADDRESS);
            email.addTo(to);

            if (text != null) {
                email.setBodyText(text);
            }
            if (html != null) {
                email.setBodyHtml(html);
            }
            MailerPlugin.send(email);
        }

    }

    /**
     * Sends mails with the given bodies. Adds signatures.
     */
    public static void sendMail (String to, String subjectKey, Txt text, Html html) {
        sendMail(to, Messages.get("subject." + subjectKey),
                text.body().trim() + views.txt.messages.signature.render().body().trim(),
                html.body().trim() + views.html.messages.signature.render().body().trim()
        );
    }
}
