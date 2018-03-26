
package be.ugent.degage.db.models;

import java.time.LocalDate;
import com.google.gson.annotations.Expose;

public class AutoAndUser {

      @Expose
      private Auto auto;
      @Expose
      private UserHeaderShort user;

      public AutoAndUser(Auto auto, UserHeaderShort user) {
          this.auto = auto;
          this.user = user;
      }

      public Auto getAuto() {
          return auto;
      }

      public UserHeaderShort getUser() {
          return user;
      }

  }
