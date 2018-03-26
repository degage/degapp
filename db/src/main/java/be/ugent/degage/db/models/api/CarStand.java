package be.ugent.degage.db.models.api;

import com.google.gson.annotations.Expose;

public class CarStand {
  @Expose
  private final int carId;
  @Expose
  private final String name;
  @Expose
  private final float latitude;
  @Expose
  private final float longitude;
  @Expose
  private final String fuelType;

  public static class Builder {
      // Required parameters
      private final int carId;

      // Optional parameters - initialized to default values
      private String name      = "";
      private float latitude   = 0;
      private float longitude  = 0;
      private String fuelType  = "";

      public Builder(int carId) {
          this.carId = carId;
      }

      public Builder name(String val)
          { name = val;      return this; }
      public Builder latitude(float val)
          { latitude = val;           return this; }
      public Builder longitude(float val)
          { longitude = val;  return this; }
      public Builder fuelType(String val)
          { fuelType = val;  return this; }

      public CarStand build() {
          return new CarStand(this);
      }
  }

  public int getCarId() { return carId; }
  public float getLatitude() { return latitude; }
  public float getLongitude() { return longitude; }
  public String getName() { return name; }
  public String getFuelType() { return fuelType; }

  private CarStand(Builder builder) {
      carId       = builder.carId;
      name        = builder.name;
      latitude    = builder.latitude;
      longitude   = builder.longitude;
      fuelType    = builder.fuelType;
  }
}
