package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.MessageDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.Message;
import be.ugent.degage.db.models.User;
import controllers.Security.RoleSecured;
import controllers.util.Pagination;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import providers.DataProvider;
import views.html.notifiers.addmessage;
import views.html.notifiers.messages;
import views.html.notifiers.messagespage;

import java.util.List;

public class Messages extends Controller {

    /**
     * Class implementing a model wrapped in a form.
     * This model is used during the submission of a new message.
     */
    public static class MessageCreationModel {
        public String subject;
        public String body;
        public Integer userId;

        public String validate() {
            if (userId == null || userId == 0 || "".equals(subject) || "".equals(body))
                return "Vul alle velden in";

            return null;
        }

    }

    public static int AUTOCOMPLETE_MAX = 10;


    /**
     * Method: GET
     *
     * @return index page containing all the received messages of a specific user
     */
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result showMessages() {
        return ok(messages.render());
    }

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result showReceivedMessagesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        User user = DataProvider.getUserProvider().getUser();
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        filter.putValue(FilterField.MESSAGE_RECEIVER_ID, user.getId() + "");
        return ok(messageList(page, pageSize, field, asc, filter));
    }

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result showSentMessagesPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {
        User user = DataProvider.getUserProvider().getUser();
        FilterField field = FilterField.stringToField(orderBy);

        boolean asc = Pagination.parseBoolean(ascInt);
        Filter filter = Pagination.parseFilter(searchString);

        filter.putValue(FilterField.MESSAGE_SENDER_ID, user.getId() + "");
        return ok(messageList(page, pageSize, field, asc, filter));
    }

    // used in injected context
    private static Html messageList(int page, int pageSize, FilterField orderBy, boolean asc, Filter filter) {
        MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();
        List<Message> messageList = dao.getMessageList(orderBy, asc, page, pageSize, filter);

        int amountOfResults = dao.getAmountOfMessages(filter);
        int amountOfPages = (int) Math.ceil(amountOfResults / (double) pageSize);

        return messagespage.render(messageList, page, amountOfResults, amountOfPages);
    }

    /**
     * Method: GET
     *
     * @return a new message form
     */

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result newMessage() {
        Form<MessageCreationModel> editForm = Form.form(MessageCreationModel.class);
        return ok(addmessage.render(editForm));
    }

    /**
     * Method: GET
     *
     * @return a new message form, with the user already filled in, for reply purposes
     */

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result reply(int userId) {
        UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
        User user = dao.getUser(userId); // TODO: full user needed?
        if (user == null) {
            flash("danger", "GebruikersID " + userId + " bestaat niet.");
            return redirect(routes.Messages.showMessages());
        }
        MessageCreationModel model = new MessageCreationModel();
        model.userId = user.getId();
        Form<MessageCreationModel> editForm = Form.form(MessageCreationModel.class);
        return ok(addmessage.render(editForm.fill(model)));

    }


    /**
     * Method: POST
     * <p>
     * Creates a new message based on submitted form data
     *
     * @return the messages index list
     */

    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result createNewMessage() {
        Form<MessageCreationModel> createForm = Form.form(MessageCreationModel.class).bindFromRequest();
        if (createForm.hasErrors()) {
            return badRequest(addmessage.render(createForm));
        } else {

            DataAccessContext context = DataAccess.getInjectedContext();
            MessageDAO dao = context.getMessageDAO();

            User sender = DataProvider.getUserProvider().getUser();
            User receiver = context.getUserDAO().getUserPartial(createForm.get().userId);
            Message mes = dao.createMessage(sender, receiver, createForm.get().subject, createForm.get().body);
            DataProvider.getCommunicationProvider().invalidateMessages(receiver.getId()); // invalidate the message
            DataProvider.getCommunicationProvider().invalidateMessageNumber(receiver.getId());
            if (mes != null) {
                return redirect(
                        routes.Messages.showMessages() // return to infosession list
                );
            } else {
                createForm.error("Failed to create message in database. Contact administrator.");
                return badRequest(addmessage.render(createForm));
            }
        }
    }

    /**
     * Method: GET
     *
     * @param messageId Id of the message that has to be marked as read
     * @return message index page
     */
    @RoleSecured.RoleAuthenticated()
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
    @RoleSecured.RoleAuthenticated()
    @InjectContext
    public static Result markAllMessagesAsRead() {
        User user = DataProvider.getUserProvider().getUser();
        MessageDAO dao = DataAccess.getInjectedContext().getMessageDAO();
        dao.markAllMessagesAsRead(user.getId());
        DataProvider.getCommunicationProvider().invalidateMessages(user.getId());
        return redirect(routes.Messages.showMessages());
    }
}
