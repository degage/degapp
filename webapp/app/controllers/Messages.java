/* Messages.java
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

package controllers;

import be.ugent.degage.db.dao.MessageDAO;
import be.ugent.degage.db.models.Message;
import be.ugent.degage.db.models.UserHeader;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.notifiers.addmessage;
import views.html.notifiers.addmsgowner;
import views.html.notifiers.messages;
import views.html.notifiers.messagespage;
import views.html.notifiers.reply;

import java.util.Collections;
import java.util.List;

public class Messages extends Controller {

    public static class BasicMessageData {
        @Constraints.Required
        public String subject;

        @Constraints.Required
        public String body;
    }


    public static class MessageToOwnerData extends BasicMessageData {

        public Integer carId;

        @Constraints.Required
        public String carIdAsString;

        // TODO: create a specific constraint
        public List<ValidationError> validate() {
            if (carId == null || carId == 0) {
                // needed for those cases where a string is input which does not correspond with a real car
                return Collections.singletonList(new ValidationError("carId", "Gelieve een auto te selecteren"));
            } else {
                return null;
            }
        }
    }

    /**
     * Class implementing a model wrapped in a form.
     * This model is used during the submission of a new message.
     */
    public static class MessageToUserData extends BasicMessageData {

        public Integer userId;

        @Constraints.Required
        public String userIdAsString;

        // TODO: create a specific constraint
        public List<ValidationError> validate() {
            if (userId == null || userId == 0) {
                // needed for those cases where a string is input which does not correspond with a real person
                return Collections.singletonList(new ValidationError("userId", "Gelieve een bestemmeling te selecteren"));
            } else {
                return null;
            }
        }
    }

    /**
     * Method: GET
     *
     * @return index page containing all the received messages of a specific user
     */
    @AllowRoles({})
    @InjectContext
    public static Result showMessages() {
        return ok(messages.render());
    }

    @AllowRoles({})
    @InjectContext
    public static Result showReceivedMessagesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        return ok(messagespage.render(
                DataAccess.getInjectedContext().getMessageDAO().listMessagesTo(CurrentUser.getId(), page, pageSize),
                true)
        );
    }

    @AllowRoles({})
    @InjectContext
    public static Result showSentMessagesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        return ok(messagespage.render(
                DataAccess.getInjectedContext().getMessageDAO().listMessagesFrom(CurrentUser.getId(), page, pageSize),
                false)
        );
    }

    /**
     * Method: GET
     *
     * @return a new message form
     */

    @AllowRoles
    @InjectContext
    public static Result newMessage() {
        return ok(addmessage.render(Form.form(MessageToUserData.class)));
    }

    @AllowRoles
    @InjectContext
    public static Result newMessageToOwner() {
        return ok(addmsgowner.render(Form.form(MessageToOwnerData.class)));
    }

    /**
     * Method: GET
     *
     * @return a new message form, filled in with details for replying to the given message
     */

    @AllowRoles({})
    @InjectContext
    public static Result reply(int messageId) {
        // TODO: check whether message was meant for ths user (hint: add sender id paraneter to getReplyHeader
        Message message = DataAccess.getInjectedContext().getMessageDAO().getReplyHeader(messageId);
        UserHeader initialReceiver = message.getUser();
        BasicMessageData data = new BasicMessageData();
        data.subject = message.getSubject();
        if (!data.subject.startsWith("Re: ")) {
            data.subject = "Re: " + data.subject;
        }
        return ok(reply.render(Form.form(BasicMessageData.class).fill(data), messageId, initialReceiver.getFullName()));
    }

    @AllowRoles({})
    @InjectContext
    public static Result sendReplyTo(int messageId) {
        // TODO: check whether message was meant for this user.
        Form<BasicMessageData> form = Form.form(BasicMessageData.class).bindFromRequest();
        Message message = DataAccess.getInjectedContext().getMessageDAO().getReplyHeader(messageId);
        UserHeader initialReceiver = message.getUser();
        if (form.hasErrors()) {
            return ok(reply.render(form, messageId, initialReceiver.getFullName()));
        } else {
            return sendMessage(form.get(), initialReceiver.getId());
        }
    }

    /**
     * Method: POST
     * <p>
     * Creates a new message based on submitted form data
     *
     * @return the messages index list
     */

    @AllowRoles
    @InjectContext
    public static Result createNewMessage() {
        Form<MessageToUserData> form = Form.form(MessageToUserData.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(addmessage.render(form));
        } else {
            MessageToUserData data = form.get();
            return sendMessage(data, data.userId);
        }
    }

    @AllowRoles
    @InjectContext
    public static Result sendMessageToOwner() {
        Form<MessageToOwnerData> form = Form.form(MessageToOwnerData.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(addmsgowner.render(form));
        } else {
            MessageToOwnerData data = form.get();
            return sendMessage(
                    data,
                    DataAccess.getInjectedContext().getCarDAO().getCar(data.carId).getOwner().getId()
            );
        }
    }

    // must be used with injected context
    private static Result sendMessage(BasicMessageData data, int receiverId) {
        MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();
        dao.createMessage(CurrentUser.getId(), receiverId, data.subject, data.body);
        DataProvider.getCommunicationProvider().invalidateMessages(receiverId); // invalidate the message
        DataProvider.getCommunicationProvider().invalidateMessageNumber(receiverId);
        return redirect(routes.Messages.showMessages());
    }

    /**
     * Method: GET
     *
     * @param messageId Id of the message that has to be marked as read
     * @return message index page
     */
    @AllowRoles({})
    @InjectContext
    public static Result markMessageAsRead(int messageId) {
        MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();
        dao.markMessageAsRead(messageId);
        DataProvider.getCommunicationProvider().invalidateMessages(CurrentUser.getId());
        return redirect(routes.Messages.showMessages());
    }

    /**
     * Method: GET
     *
     * @return message index page
     */
    @AllowRoles({})
    @InjectContext
    public static Result markAllMessagesAsRead() {
        MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();
        dao.markAllMessagesAsRead(CurrentUser.getId());
        DataProvider.getCommunicationProvider().invalidateMessages(CurrentUser.getId());
        return redirect(routes.Messages.showMessages());
    }
}
