
package be.ugent.degage.db.models;

import com.google.gson.annotations.Expose;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import  java.util.ArrayList;


public class Property {
  @Expose
  private final int propertyId;
  @Expose
  private final String key;
  @Expose
  private final String value;

  public static class Builder {
      // Required parameters
      private final String key;

      // Optional parameters - initialized to default values
      private int propertyId;
      private String value = "";

      public Builder(String key) {
          this.key = key;
      }

      public Builder(Property property) {
          this.propertyId = property.propertyId;
          this.key = property.key;
          this.value  = property.value;
      }

      public Builder id(int val)
          { propertyId = val; return this; }
      public Builder value(String val)
          { value = val; return this; }

      public int hashResult() {
        int result = 13;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);

        return result;
      }

      public Property build() {
          return new Property(this);
      }
  }

  private Property(Builder builder) {
      propertyId              = builder.propertyId;
      key                     = builder.key;
      value                   = builder.value;
  }

  public int getId() { return propertyId; }
  public int getPropertyId() { return propertyId; }
  public String getKey() { return key; }
  public String getValue() { return value; }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("PROPERTY: ")
      .append("id:")
      .append(propertyId)
      .append(", key:")
      .append(key)
      .append(", value:")
      .append(value);
    return sb.toString();
  }

  public boolean equals(Object object) {
      if (this == object) return true;
      if (object == null || getClass() != object.getClass()) return false;
      if (!super.equals(object)) return false;

      Property property = (Property) object;

      if (propertyId != property.propertyId) return false;
      if (getKey() != property.getKey()) return false;
      if (getValue() != property.getValue()) return false;

      return true;
  }

  public int hashCode() {
      int result = 13;
      result = 31 * result + propertyId;
      result = 31 * result + (getKey() != null ? getKey().hashCode() : 0);
      result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
      return result;
  }

}
