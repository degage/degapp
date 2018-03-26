
package be.ugent.degage.db.models;

import com.google.gson.annotations.Expose;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import  java.util.ArrayList;


public class CarApproval {
  @Expose
  private final int carApprovalId;
  @Expose
  private final int carId;
  @Expose
  private final int infoSessionId;
  @Expose
  private final LocalDate submissionDate;
  @Expose
  private final LocalDate date;
  @Expose
  private final ApprovalStatus status;
  @Expose
  private final String userMessage;
  @Expose
  private final String adminMessage;  
  @Expose
  private final Auto car;
  @Expose
  private final EnrollmentStatus enrollmentStatus;  
  @Expose
  private final User user;
  @Expose
  private final User admin;

  public static class Builder {
      // Required parameters

      // Optional parameters - initialized to default values
      private int carApprovalId;
      private int carId;
      private int infoSessionId;
      private LocalDate submissionDate;
      private LocalDate date;
      private ApprovalStatus status;
      private String userMessage;
      private String adminMessage;
      private Auto car;
      private User user;
      private User admin;
      private EnrollmentStatus enrollmentStatus;

      public Builder() {
      }

      public Builder(CarApproval carApproval) {
          this.carApprovalId = carApproval.carApprovalId;
          this.carId = carApproval.carId;
          this.date  = carApproval.date;
          this.submissionDate = carApproval.submissionDate;
          this.status = carApproval.status;
          this.userMessage = carApproval.userMessage;
          this.adminMessage = carApproval.adminMessage;
          this.car = carApproval.car;
          this.user = carApproval.user;
          this.admin = carApproval.admin;
          this.infoSessionId = carApproval.infoSessionId;
          this.enrollmentStatus = carApproval.enrollmentStatus;
      }

      public Builder id(int val)
          { carApprovalId = val; return this; }
      public Builder carId(Integer val)
          { carId = val; return this; }
      public Builder infoSessionId(Integer val)
          { infoSessionId = val; return this; }
      public Builder date(LocalDate val)
          { date = val; return this; }
      public Builder status(ApprovalStatus val)
          { status = val; return this; }
      public Builder submissionDate(LocalDate val)
          { submissionDate = val; return this; }
      public Builder userMessage(String val)
          { userMessage = val; return this; }
      public Builder adminMessage(String val)
          { adminMessage = val; return this; }
      public Builder car(Auto val)
      { car = val; return this; }
      public Builder user(User val)
      { user = val; return this; }      
      public Builder admin(User val)
      { admin = val; return this; }
      public Builder enrollmentStatus(EnrollmentStatus val)
      { enrollmentStatus = val; return this; }

      public int hashResult() {
        ZoneId zoneId = ZoneId.of("Europe/Oslo");
        int result = 13;
        result = 31 * result + (date != null ? (int)(date.atStartOfDay(zoneId).toEpochSecond()) : 0);
        result = 31 * result + carApprovalId;
        result = 31 * result + carId;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (submissionDate != null ? submissionDate.hashCode() : 0);
        result = 31 * result + (userMessage != null ? userMessage.hashCode() : 0);
        result = 31 * result + (adminMessage != null ? adminMessage.hashCode() : 0);

        return result;
      }


      public CarApproval build() {
          return new CarApproval(this);
      }
  }

  private CarApproval(Builder builder) {
      carApprovalId           = builder.carApprovalId;
      carId                   = builder.carId;
      date                    = builder.date;
      submissionDate          = builder.submissionDate;
      status                  = builder.status;
      userMessage             = builder.userMessage;
      adminMessage            = builder.adminMessage;
      car                     = builder.car;
      user                    = builder.user;
      admin                   = builder.admin;
      infoSessionId           = builder.infoSessionId;
      enrollmentStatus        = builder.enrollmentStatus;
  }

  public int getId() { return carApprovalId; }
  public int getCarApprovalId() { return carApprovalId; }
  public LocalDate getSubmissionDate() { return submissionDate; }
  public LocalDate getDate() { return date; }
  public int getCarId() { return carId; }
  public String getUserMessage() { return userMessage; }
  public String getAdminMessage() { return adminMessage; }
  public Auto getCar() { return car; }
  public User getUser() { return user; }
  public User getAdmin() { return admin; }
  public int getInfoSessionId() { return infoSessionId; }
  public EnrollmentStatus getEnrollmentStatus() { return enrollmentStatus; }
  public ApprovalStatus getStatus() { return status; }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("CAR APPROVAL: ")
      .append("id:")
      .append(carApprovalId)
      .append(", carId:")
      .append(carId)
      .append(", date:")
      .append(date)
      .append(", submissionDate:")
      .append(submissionDate)
      .append(", userMessage:")
      .append(userMessage)
      .append(", adminMessage:")
      .append(adminMessage)
      .append(", enrollmentStatus:")
      .append(enrollmentStatus)
      .append(", infosession ID:")
      .append(infoSessionId);
      
    return sb.toString();
  }

  public boolean equals(Object object) {
      if (this == object) return true;
      if (object == null || getClass() != object.getClass()) return false;
      if (!super.equals(object)) return false;

      CarApproval carApproval = (CarApproval) object;

      if (carApprovalId != carApproval.carApprovalId) return false;
      if (getStatus() != carApproval.getStatus()) return false;
      if (getCarId() != carApproval.getCarId()) return false;
      if (getDate() != null ? !getDate().equals(carApproval.getDate()) : carApproval.getDate() != null) return false;
      if (getSubmissionDate() != null ? !getSubmissionDate().equals(carApproval.getSubmissionDate()) : carApproval.getSubmissionDate() != null) return false;
      if (getUserMessage() != null ? !getUserMessage().equals(carApproval.getUserMessage()) : carApproval.getUserMessage() != null) return false;
      if (getAdminMessage() != null ? !getAdminMessage().equals(carApproval.getAdminMessage()) : carApproval.getAdminMessage() != null)
          return false;
      if (getStatus() != null ? !getStatus().equals(carApproval.getStatus()) : carApproval.getStatus() != null) return false;

      return true;
  }

  public int hashCode() {
      int result = 13;
      result = 31 * result + carApprovalId;
      result = 31 * result + getCarId();
      result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
      result = 31 * result + (getSubmissionDate() != null ? getSubmissionDate().hashCode() : 0);
      result = 31 * result + (getUserMessage() != null ? getUserMessage().hashCode() : 0);
      result = 31 * result + (getAdminMessage() != null ? getAdminMessage().hashCode() : 0);
      result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
      return result;
  }

}
