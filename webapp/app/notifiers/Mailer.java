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
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import play.Play;

/**
 * Created by Stefaan Vermassen on 16/02/14.
 */
public class Mailer {

    // TODO: do not use static here!
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
            // TODO: use different mailer instead of logging
            // Logger.debug("Sent mail: " + html);
        }
    }

    public static void sendText(String text) {
        mail.send(text);
    }

    public static String getEmailAndNameFormatted(String name, String email) {
        return name + " <" + email + ">";
    }
}
