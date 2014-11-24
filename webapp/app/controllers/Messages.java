package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.MessageDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.Message;
import be.ugent.degage.db.models.User;
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
import views.html.notifiers.messages;
import views.html.notifiers.messagespage;

import java.util.Arrays;
import java.util.List;

public class Messages extends Controller {

    /**
     * Class implementing a model wrapped in a form.
     * This model is used during the submission of a new message.
     */
    public static class MessageCreationModel {
        @Constraints.Required
        public String subject;

        @Constraints.Required
        public String body;

        public Integer userId;

        // TODO: create a specific constraint
        public List<ValidationError> validate() {
            if (userId == null || userId == 0) {
                return Arrays.asList(new ValidationError ("userId", "Gelieve een bestemmeling aan te geven"));
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
    @AllowRoles
    @InjectContext
    public static Result showMessages() {
        return ok(messages.render());
    }

    @AllowRoles
    @InjectContext
    public static Result showReceivedMessagesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        int userId = CurrentUser.getId();
        MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();
        return ok(messagespage.render(
                        dao.listMessagesTo(userId, page, pageSize),
                        page,
                        dao.countMessagesTo(userId),
                        (int) Math.ceil(dao.countMessagesFrom(userId) / (double) pageSize),
                        true)
        );
    }

    @AllowRoles
    @InjectContext
    public static Result showSentMessagesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        int userId = CurrentUser.getId();
        MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();
        return ok(messagespage.render(
                        dao.listMessagesFrom(userId, page, pageSize),
                        page,
                        dao.countMessagesFrom(userId),
                        (int) Math.ceil(dao.countMessagesFrom(userId) / (double) pageSize),
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
        return ok(addmessage.render(Form.form(MessageCreationModel.class), null));
    }

    /**
     * Method: GET
     *
     * @return a new message form, filled in with details for replying to the given message
     */

    @AllowRoles
    @InjectContext
    public static Result reply(int messageId) {
        Message message = DataAccess.getInjectedContext().getMessageDAO().getReplyHeader(messageId);
        UserHeader initialReceiver = message.getUser();
        MessageCreationModel model = new MessageCreationModel();
        model.userId = initialReceiver.getId();
        model.subject = message.getSubject();
        if (!model.subject.startsWith("Re: ")) {
            model.subject = "Re: " + model.subject;
        }
        return ok(addmessage.render(Form.form(MessageCreationModel.class).fill(model), initialReceiver));
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
        Form<MessageCreationModel> createForm = Form.form(MessageCreationModel.class).bindFromRequest();
        if (createForm.hasErrors()) {
            // retrieve the user id

            String userIdString  = createForm.data().get("userId");
            if (userIdString != null && ! userIdString.isEmpty()) {
                int userId = Integer.parseInt(userIdString);
                if (userId != 0) {
                    UserHeader initialReceiver = DataAccess.getInjectedContext().getUserDAO().getUserHeader(Integer.parseInt(userIdString));
                    return ok(addmessage.render(createForm, initialReceiver));
                }
            }
            return ok(addmessage.render(createForm, null));
        } else {

            MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();

            int receiverId = createForm.get().userId;
            dao.createMessage(CurrentUser.getId(), receiverId, createForm.get().subject, createForm.get().body);
            DataProvider.getCommunicationProvider().invalidateMessages(receiverId); // invalidate the message
            DataProvider.getCommunicationProvider().invalidateMessageNumber(receiverId);
            return redirect(
                    routes.Messages.showMessages() // return to message list
            );
        }
    }

    /**
     * Method: GET
     *
     * @param messageId Id of the message that has to be marked as read
     * @return message index page
     */
    @AllowRoles
    @InjectContext
    public static Result markMessageAsRead(int messageId) {
        User user = DataProvider.getUserProvider().getUser();
        MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();
        dao.markMessageAsRead(messageId);
        DataProvider.getCommunicationProvider().invalidateMessages(user.getId());
        return redirect(routes.Messages.showMessages());
    }

    /**
     * Method: GET
     *
     * @return message index page
     */
    @AllowRoles
    @InjectContext
    public static Result markAllMessagesAsRead() {
        User user = DataProvider.getUserProvider().getUser();
        MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();
        dao.markAllMessagesAsRead(user.getId());
        DataProvider.getCommunicationProvider().invalidateMessages(user.getId());
        return redirect(routes.Messages.showMessages());
    }
}
