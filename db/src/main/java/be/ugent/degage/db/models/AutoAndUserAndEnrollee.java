
package be.ugent.degage.db.models;

import java.time.LocalDate;
import com.google.gson.annotations.Expose;

public class AutoAndUserAndEnrollee {

  @Expose
  private Auto auto;
  @Expose
  private User user;
  @Expose
  private int infosessionId;
  @Expose
  private EnrollmentStatus enrollmentStatus;

  public static class Builder {
    // Required parameters
    private Auto auto;
    private User user;
    private int infosessionId;
    private EnrollmentStatus enrollmentStatus;

    public Builder () {
    }

    public Builder auto(Auto val)
    { auto = val;      return this; }
    public Builder user(User val)
    { user = val;           return this; }
    public Builder infosessionId(int val)
    { infosessionId = val;  return this; }
    public Builder enrollmentStatus(EnrollmentStatus val)
    { enrollmentStatus = val;  return this; }

    public AutoAndUserAndEnrollee build() {
      return new AutoAndUserAndEnrollee(this);
  }

  }

  private AutoAndUserAndEnrollee(Builder builder) {
    auto     = builder.auto;
    user        = builder.user;
    infosessionId        = builder.infosessionId;
    enrollmentStatus        = builder.enrollmentStatus;
  }

  public Auto getAuto() {
      return auto;
  }

  public User getUser() {
      return user;
  }

  public int getInfosessionId() {
    return infosessionId;
  }

  public EnrollmentStatus getEnrollmentStatus() {
    return enrollmentStatus;
  }

}
