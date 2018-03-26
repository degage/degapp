package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.FileDAO;
import be.ugent.degage.db.dao.PaymentDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.dao.InvoiceDAO;
import be.ugent.degage.db.dao.CodasDAO;
import be.ugent.degage.db.dao.PaymentInvoiceDAO;
import be.ugent.degage.db.models.*;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.DataAccessException;
import com.google.common.primitives.Ints;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import notifiers.Notifier;
import play.mvc.Controller;
import play.mvc.Result;
import schedulers.joblessprocesses.JoblessProcess;
import schedulers.joblessprocesses.JoblessProcessFactory;
import schedulers.joblessprocesses.JoblessProcess.JoblessProcessType;
import views.html.payments.*;
import controllers.util.Pagination;
import play.data.validation.Constraints;
import java.time.LocalDate;
import java.text.DateFormat;
import java.util.Date;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.*;
import play.data.Form;
import controllers.util.FileHelper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import db.CurrentUser;

public class Codas extends Controller {

  public static final float AMOUNT_DEVIATION = 2f;

    /**
     * @param page         The page in the codaslist
     * @param ascInt       An integer representing ascending (1) or descending (0)
     * @param orderBy      A field representing the field to order on
     * @param searchString A string with form field1:value1,field2:value2 representing the fields to filter on
     * @return A partial page with a table of payments of the corresponding page
     */
    @AllowRoles
    @InjectContext
    public static Result showCodasPage(int page, int pageSize, int ascInt, String orderBy, String searchString) {

        return ok(views.html.payments.codaspage.render(
                DataAccess.getInjectedContext().getCodasDAO().listCodasPage(
                        FilterField.stringToField(orderBy, FilterField.NUMBER),
                        Pagination.parseBoolean(ascInt),
                        page, pageSize,
                        Pagination.parseFilter(searchString)
                )
        ));
    }

    @AllowRoles
    @InjectContext
    public static Result uploadCoda() {
        //also fetch history of past uploaded codas
        DataAccessContext context = DataAccess.getInjectedContext();
        CodasDAO dao = context.getCodasDAO();
        Iterable<Coda> codas = dao.listAllCodas();

        return ok(uploadCoda.render(codas));
    }

    @AllowRoles
    @InjectContext
    public static Result uploadCodaPost() {
      File file = FileHelper.getFileFromRequest("coda", FileHelper.CODA_CONTENT_TYPES, "uploads.codas", 0);
      DataAccessContext context = DataAccess.getInjectedContext();
      try {
        int numPayments = processCoda(file);
        flash("success", "Bestand werd geüpload. Er werden " + numPayments + " betalingen aangemaakt.");
        
        JoblessProcessFactory.getProcess(JoblessProcessType.MEMBERSHIP_UPDATE).runProcess(context);

      } catch (Exception e) {
          e.printStackTrace();
          // flash("danger", "Er ging iets mis. Er werden " + numPayments + " nieuwe betalingen aangemaakt, daarna liep het mis.");
          flash("danger", "Er ging iets mis. Misschien werd dit codabestand al eerder geüpload?");
      }
      return redirect(routes.Codas.uploadCoda());
    }

    @InjectContext
    public static int processCoda(File file) throws Exception {
      //Path absolutePath = Paths.get(FileHelper.getUploadFolder()).resolve(file.getPath().toString());
      //String currentDirectory = new java.io.File(".").getAbsolutePath();
      //Path absolutePath = Paths.get(currentDirectory + FileHelper.UPLOAD_FOLDER).resolve(file.getPath().toString());
      String codaFilename = Controller.request().body().asMultipartFormData().getFile("coda").getFilename();

      DataAccessContext context = DataAccess.getInjectedContext();
      UserDAO userDao = context.getUserDAO();
      InvoiceDAO invoiceDao = context.getInvoiceDAO();
      PaymentDAO paymentDao = context.getPaymentDAO();
      PaymentInvoiceDAO paymentInvoiceDao = context.getPaymentInvoiceDAO();
      CodasDAO codasDao = context.getCodasDAO();
      int numPayments = 0;

      ArrayList<Payment> paymentList = new ArrayList();
      ArrayList<Invoice> invoiceList = new ArrayList();

      //store the coda filename, date and user who uploaded it

      codasDao.createCoda(new Coda(LocalDate.now(), codaFilename, userDao.getUser(CurrentUser.getId())));

      // if (file == null) {
      //     flash("danger", "Je moet een bestand kiezen");
      //     return redirect(routes.Codas.uploadCoda());
      // } else if (file.getContentType() == null) {
      //     flash("danger", "Verkeerd bestandstype opgegeven. Enkel coda files zijn toegelaten.");
      //     return redirect(routes.Codas.uploadCoda());
      // } else if (file != null) {
      //
      // }

      BufferedReader reader = new BufferedReader(new FileReader(Paths.get(FileHelper.UPLOAD_FOLDER, file.getPath()).toFile()));
        int userChangedFree = 0;
        int userChangedStruct = 0;
         String line = null;
         int number = 0;
         int paymentId = 0;
         int userId = 0;
         float amount = 0;
         String structuredCommunication = null;
         String comment = null;
         LocalDate date = null;
         String bank = null;
         String accountNumber = null;
         PaymentStatus status = PaymentStatus.valueOf("CHANGE");
         PaymentDebitType debitType = PaymentDebitType.valueOf("CREDIT");
         String currency = null;
         String name = null;
         String address = null;
         String path = file.getPath();
         String filename = codaFilename;
         Payment payment = null;
         Invoice invoice = null;
         boolean debit = true;
         Payment.Builder paymentBuilder = null;
         while ((line = reader.readLine()) != null) {
             if (line.substring(0,1).equals("2")) {
                  if (line.substring(1,2).equals("1")) {
                      if (paymentBuilder != null){
                          //code duplication
                          payment = paymentBuilder.build();
                          paymentList.add(payment);
                          invoiceList.add(invoice);
                          number = 0;
                          userId = 0;
                          amount = 0;
                          structuredCommunication = null;
                          comment = null;
                          date = null;
                          bank = null;
                          accountNumber = null;
                          status = PaymentStatus.valueOf("CHANGE");
                          debitType = PaymentDebitType.valueOf("CREDIT");
                          currency = null;
                          name = null;
                          address = null;
                          path = file.getPath();
                          filename = codaFilename;
                          payment = null;
                          invoice = null;
                          debit = false;
                      }
                      number = Integer.parseInt(line.substring(3,6));
                      debit = line.substring(31,32).equals("1");
                      debitType = debit ? PaymentDebitType.valueOf("DEBIT") : PaymentDebitType.valueOf("CREDIT");
                      String amountStr = line.substring(32,44) + "." + line.substring(44,47);
                      amount = Float.parseFloat(amountStr);
                      if (debit) {
                        amount = amount * (-1);
                      }
                      if (line.substring(61,62).equals("1")) {
                          structuredCommunication = "+++" + line.substring(65,68) + "/" + line.substring(68,72) + "/" + line.substring(72,77) + "+++";
                          comment = null;
                      } else if (line.substring(60,61).equals("0")) {
                          comment = line.substring(62,115);
                          structuredCommunication = null;
                      }
                      String dateStr = "20" + line.substring(119,121) + "-" + line.substring(117,119) + "-" + line.substring(115,117);
                      date = LocalDate.parse(dateStr);
                  } else if (line.substring(1,2).equals("2")) {
                      bank = line.substring(98,106);
                  } else if (line.substring(1,2).equals("3")) {
                      accountNumber = line.substring(10,26);
                      name = line.substring(47,80);
                      currency = line.substring(44,47);
                      paymentBuilder = new Payment.Builder(number, date, accountNumber);
                      paymentBuilder.amount(amount);
                      paymentBuilder.comment(comment);
                      paymentBuilder.structuredCommunication(structuredCommunication);
                      paymentBuilder.bank(bank);
                      paymentBuilder.currency(currency);
                      paymentBuilder.name(name);
                      paymentBuilder.filename(filename);
                      paymentBuilder.debitType(debitType);
                      if (invoiceDao.getInvoiceByStructComm(structuredCommunication) != null) {
                          if (invoiceDao.getInvoiceByStructComm(structuredCommunication).getStructuredCommunication().equals(structuredCommunication)) {
                              if ((invoiceDao.getInvoiceByStructComm(structuredCommunication).getAmount() - AMOUNT_DEVIATION) <= amount) {
                                  status = PaymentStatus.valueOf("OK");
                              } else {
                                  status = PaymentStatus.valueOf("CHANGE");
                              }
                              invoice = invoiceDao.getInvoiceByStructComm(structuredCommunication);
                              userId = invoice.getUserId();
                              userChangedStruct++;
                          }
                      } else if ((invoiceDao.getInvoiceByStructComm(structuredCommunication) == null)) {
                          //try to find the invoice number in the comment
                          Invoice tmp = null;
                          if (comment != null) {
                              tmp = invoiceDao.findInvoiceByComment(comment);
                          }

                          if (tmp != null) {
                              if (tmp.getAmount() - AMOUNT_DEVIATION <= amount) {
                                  status = PaymentStatus.valueOf("OK");
                              } else {
                                  status = PaymentStatus.valueOf("CHANGE");
                              }
                              invoice = tmp;
                              userId = invoice.getUserId();
                              //TODO check amount
                          } else {
                              status = PaymentStatus.valueOf("UNASSIGNED");
                          }

                      } else {
                          status = PaymentStatus.valueOf("CHANGE");
                      }
                      paymentBuilder.status(status);
                      if ((userId == 0) && (userDao.getUserByAccountNumber(accountNumber) != null)) {
                          userId = userDao.getUserByAccountNumber(accountNumber).getId();
                      }
                      paymentBuilder.userId(userId);
                  }
              } else if (line.substring(0,1).equals("3")) {
                      if (line.substring(1,2).equals("2")) {
                          if (line.substring(10,80).trim().equals("BE")) {
                              address = line.substring(10,80).trim();
                          } else {
                              address = line.substring(10,80);
                      }
                      paymentBuilder.address(address);
                  }
              }
         }

          //code duplication
          payment = paymentBuilder.build();
              paymentList.add(payment);
              invoiceList.add(invoice);

          //add all payments to the db
          assert(paymentList.size() == invoiceList.size());
          for (int i = 0; i < paymentList.size(); i++) {
              Payment p = paymentList.get(i);
              Invoice inv = invoiceList.get(i);
              // add hashes to payment
              Payment.Builder pb = new Payment.Builder(p);

              if (i>0 && paymentList.get(i-1) != null) {
                pb.previousHash(paymentList.get(i-1).getCurrentHash());
              }
              if (i<paymentList.size()-1 && paymentList.get(i+1) != null) {
                pb.nextHash(paymentList.get(i+1).getCurrentHash());
              }
              Payment paymentWithHashes = pb.build();
              if (paymentDao.checkUniquePayment(paymentWithHashes)) {
                paymentId = paymentDao.createPayment(paymentWithHashes);
                if (paymentWithHashes != null && inv != null) {
                    Payment.Builder newBuilder = new Payment.Builder(paymentWithHashes);
                    newBuilder.id(paymentId);
                    Payment paymentCopy = newBuilder.build();

                    if (paymentCopy.getStatus() == PaymentStatus.OK) {
                      Invoice.Builder invoiceBuilder = new Invoice.Builder(inv);
                      invoiceBuilder.status(InvoiceStatus.PAID).paymentDate(payment.getDate());
                      DataAccess.getInjectedContext().getInvoiceDAO().updateInvoice(invoiceBuilder.build());
                    }

                    paymentInvoiceDao.createPaymentInvoiceRelationship(paymentCopy, inv);
                    context.getReminderDAO().setInvoiceRemindersPaid(inv);
                }
                numPayments++;
              }
          }

          return numPayments;
    }

}
