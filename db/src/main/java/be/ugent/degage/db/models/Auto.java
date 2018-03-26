package be.ugent.degage.db.models;

import com.google.gson.annotations.Expose;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Auto {
  @Expose
  private final int id;
  @Expose
  private final String name;
  @Expose
  private final CarStatus status;
  @Expose
  private final String email;
  @Expose
  private final String type;
  @Expose
  private final String brand;
  @Expose
  private final int locationId;
  @Expose
  private final Address location;
  @Expose
  private final int seats;
  @Expose
  private final int doors;
  @Expose
  private final int year;
  @Expose
  private final boolean manual;
  @Expose
  private final boolean gps;
  @Expose
  private final boolean hook;
  @Expose
  private final CarFuel fuel;
  @Expose
  private final int fuelEconomy;
  @Expose
  private final int estimatedValue;
  @Expose
  private final int ownerAnnualKm;
  @Expose
  private final int ownerUserId;
  @Expose
  private final String comments;
  @Expose
  private final boolean active;
  @Expose
  private final int imagesId;
  @Expose
  private final LocalDate createdAt;
  @Expose
  private final LocalDate updatedAt;
  @Expose
  private final int deprec;
  @Expose
  private final int deprecLimit;
  @Expose
  private final int deprecLast;
  @Expose
  private final LocalDate startSharing;
  @Expose
  private final LocalDate endSharing;
  @Expose
  private final int contractFileId;
  @Expose
  private final LocalDate contract;
  @Expose
  private final int carAgreedValue;
  @Expose
  private final CarInsurance insurance;
  @Expose
  private final CarAssistance assistance;
  @Expose
  private final CarParkingcard parkingcard;

  public static class Builder {
    private int id;
    private String name;
    private CarStatus status;
    private String email;
    private String type;
    private String brand;
    private int locationId;
    private Address location;
    private int seats;
    private int doors;
    private int year;
    private boolean manual;
    private boolean gps;
    private boolean hook;
    private CarFuel fuel;
    private int fuelEconomy;
    private int estimatedValue;
    private int ownerAnnualKm;
    private final int ownerUserId;
    private String comments;
    private boolean active;
    private int imagesId = -1;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private int deprec;
    private int deprecLimit;
    private int deprecLast;
    private LocalDate startSharing;
    private LocalDate endSharing;
    private int contractFileId;
    private LocalDate contract;
    private int carAgreedValue;
    private CarInsurance insurance;
    private CarAssistance assistance;
    private CarParkingcard parkingcard;
    
    public Builder(int id, int ownerUserId) {
        this.id = id;
        this.ownerUserId = ownerUserId;
    }

    public Builder(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public Builder(Auto auto){
      this.id = auto.id;
      this.name = auto.name;
      this.status = auto.status;
      this.email = auto.email;
      this.type = auto.type;

      this.brand = auto.brand;
      this.locationId = auto.locationId;
      this.location = auto.location;
      this.seats = auto.seats;
      this.doors = auto.doors;
      this.year = auto.year;

      this.manual = auto.manual;
      this.gps = auto.gps;
      this.hook = auto.hook;
      this.fuel = auto.fuel;
      this.fuelEconomy = auto.fuelEconomy;

      this.estimatedValue = auto.estimatedValue;
      this.ownerAnnualKm = auto.ownerAnnualKm;
      this.ownerUserId = auto.ownerUserId;
      this.comments = auto.comments;
      this.active = auto.active;

      this.imagesId = auto.imagesId;
      this.createdAt = auto.createdAt;
      this.updatedAt = auto.updatedAt;
      this.deprec = auto.deprec;
      this.deprecLimit = auto.deprecLimit;

      this.deprecLast = auto.deprecLast;
      this.startSharing = auto.startSharing;
      this.endSharing = auto.endSharing;

      this.contractFileId = auto.contractFileId;
      this.contract = auto.contract;
      this.carAgreedValue = auto.carAgreedValue;
      this.insurance = auto.insurance;
      this.assistance = auto.assistance;
      this.parkingcard = auto.parkingcard;
    }

    public Builder name(String val)
    { name = val; return this; }
    public Builder status(CarStatus val)
    { status = val; return this; }
    public Builder email(String val)
    { email = val; return this; }
    public Builder type(String val)
    { type = val; return this; }

    public Builder brand(String val)
    { brand = val; return this; }
    public Builder locationId(int val)
    { locationId = val; return this; }
    public Builder location(Address val)
    { location = val; return this; }
    public Builder seats(int val)
    { seats = val; return this; }
    public Builder doors(int val)
    { doors = val; return this; }
    public Builder year(int val)
    { year = val; return this; }

    public Builder manual(boolean val)
    { manual = val; return this; }
    public Builder gps(boolean val)
    { gps = val; return this; }
    public Builder hook(boolean val)
    { hook = val; return this; }
    public Builder fuel(CarFuel val)
    { fuel = val; return this; }
    public Builder fuelEconomy(int val)
    { fuelEconomy = val; return this; }

    public Builder estimatedValue(int val)
    { estimatedValue = val; return this; }
    public Builder ownerAnnualKm(int val)
    { ownerAnnualKm = val; return this; }
    public Builder comments(String val)
    { comments = val; return this; }
    public Builder active(boolean val)
    { active = val; return this; }

    public Builder imagesId(int val)
    { imagesId = val; return this; }
    public Builder createdAt(LocalDate val)
    { createdAt = val; return this; }
    public Builder updatedAt(LocalDate val)
    { updatedAt = val; return this; }
    public Builder deprec(int val)
    { deprec = val; return this; }
    public Builder deprecLimit(int val)
    { deprecLimit = val; return this; }

    public Builder deprecLast(int val)
    { deprecLast = val; return this; }
    public Builder startSharing(LocalDate val)
    { startSharing = val; return this; }
    public Builder endSharing(LocalDate val)
    { endSharing = val; return this; }

    public Builder contractFileId(int val)
    { contractFileId = val; return this; }
    public Builder contract(LocalDate val)
    { contract = val; return this; }
    public Builder carAgreedValue(int val)
    { carAgreedValue = val; return this; }
    public Builder insurance(CarInsurance val)
    { insurance = val; return this; }
    public Builder assistance(CarAssistance val)
    { assistance = val; return this; }
    public Builder parkingcard(CarParkingcard val)
    { parkingcard = val; return this; }

    public Auto build() {
        return new Auto(this);
    }
  }

  private Auto(Builder builder){
    id              = builder.id;
    name            = builder.name;
    status          = builder.status;
    email           = builder.email;
    type            = builder.type;

    brand           = builder.brand;
    location        = builder.location;
    locationId        = builder.locationId;
    seats           = builder.seats;
    doors           = builder.doors;
    year            = builder.year;

    manual          = builder.manual;
    gps             = builder.gps;
    hook            = builder.hook;
    fuel            = builder.fuel;
    fuelEconomy    = builder.fuelEconomy;

    estimatedValue  = builder.estimatedValue;
    ownerAnnualKm = builder.ownerAnnualKm;
    ownerUserId   = builder.ownerUserId;
    comments        = builder.comments;
    active          = builder.active;

    imagesId       = builder.imagesId;
    createdAt      = builder.createdAt;
    updatedAt      = builder.updatedAt;
    deprec          = builder.deprec;
    deprecLimit    = builder.deprecLimit;

    deprecLast     = builder.deprecLast;
    startSharing   = builder.startSharing;
    endSharing     = builder.endSharing;

    contractFileId = builder.contractFileId;
    contract       = builder.contract;
    carAgreedValue = builder.carAgreedValue;
    insurance      = builder.insurance;
    assistance     = builder.assistance;
    parkingcard    = builder.parkingcard;
  }

  public int getCarId() { return id;}
  public String getCarName() { return name;}
  public CarStatus getCarStatus() { return status;}
  public String getCarEmail() { return email ;}
  public String getCarType() { return type;}

  public String getCarBrand() { return brand;}
  public int getCarLocationId() { return locationId;}
  public Address getCarLocation() { return location;}
  public int getCarSeats() { return seats;}
  public int getCarDoors() { return doors;}
  public int getCarYear() { return year;}

  public boolean getCarManual() { return manual;}
  public boolean getCarGps() { return gps;}
  public boolean getCarHook() { return hook;}
  public CarFuel getCarFuel() { return fuel;}
  public int getCarFuelEconomy() { return fuelEconomy;}

  public int getCarEstimatedValue() { return estimatedValue;}
  public int getCarOwnerAnnualKm() { return ownerAnnualKm;}
  public int getCarOwnerUserId() { return ownerUserId;}
  public String getCarComments() { return comments;}
  public boolean getCarActive() { return active;}

  public int getCarImagesId() { return imagesId;}
  public LocalDate getCarCreatedAt() { return createdAt;}
  public LocalDate getCarUpdatedAt() { return updatedAt;}
  public int getCarDeprec() { return deprec;}
  public int getCarDeprecLimit() { return deprecLimit;}

  public int getCarDeprecLast() { return deprecLast;}
  public LocalDate getCarStartSharing() { return startSharing;}
  public LocalDate getCarEndSharing() { return endSharing;}

  public int getContractFileId() { return contractFileId; }
  public LocalDate getContract() { return contract; }
  public int getCarAgreedValue() { return carAgreedValue; }
  public CarInsurance getInsurance() { return insurance;}
  public CarAssistance getAssistance() { return assistance;}
  public CarParkingcard getParkingCard() { return parkingcard;}

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ID: ")
    .append(id)
    .append(", NAME:")
    .append(name)
    .append(", STATUS:")
    .append(status)
    .append(", EMAIL:")
    .append(email)
    .append(", TYPE:")
    .append(type)
    .append(", BRAND:")
    .append(brand)
    .append(", LOCATION ID:")
    .append(locationId)
    .append(", LOCATION:")
    .append(location)
    .append(", SEATS:")
    .append(seats)
    .append(", DOORS:")
    .append(doors)
    .append(", YEAR:")
    .append(year)
    .append(", MANUAL:")
    .append(manual)
    .append(", GPS:")
    .append(gps)
    .append(", HOOK:")
    .append(hook)
    .append(", FUEL:")
    .append(fuel)
    .append(", FUEL ECONOMY:")
    .append(fuelEconomy)
    .append(", EST VALUE:")
    .append(estimatedValue)
    .append(", OWNER ANNUAL KM:")
    .append(ownerAnnualKm)
    .append(", OWNER USER ID:")
    .append(ownerUserId)
    .append(", COMMENTS:")
    .append(comments)
    .append(", ACTIVE:")
    .append(active)
    .append(", IMAGES ID:")
    .append(imagesId)
    .append(", CREATED AT:")
    .append(createdAt)
    .append(", UPDATED AT:")
    .append(updatedAt)
    .append(", DEPREC:")
    .append(deprec)
    .append(", DEPREC LIMIT:")
    .append(deprecLimit)
    .append(", DEPREC LAST:")
    .append(deprecLast)
    .append(", START SHARING:")
    .append(startSharing)
    .append(", END SHARING:")
    .append(endSharing)
    .append(", CONTRACT FILE ID:")
    .append(contractFileId)
    .append(", CONTRACT:")
    .append(contract)
    .append(", CAR AGREED VALUE:")
    .append(carAgreedValue)
    .append(", INSURANCE:")
    .append(insurance)
    .append(", ASSISTANCE:")
    .append(assistance)
    .append(", PARKINGCARD:")
    .append(parkingcard);
    return sb.toString();
  }

  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;

    Auto auto = (Auto) object;

    if (id != auto.id) return false;
    if (getCarName() != auto.getCarName()) return false;
    if (getCarStatus() != auto.getCarStatus()) return false;
    if (getCarEmail() != auto.getCarEmail()) return false;
    if (getCarType() != auto.getCarType()) return false;
    if (getCarBrand() != auto.getCarBrand()) return false;
    if (getCarLocationId() != auto.getCarLocationId()) return false;
    if (getCarSeats() != auto.getCarSeats()) return false;
    if (getCarDoors() != auto.getCarDoors()) return  false;
    if (getCarYear() != auto.getCarYear()) return false;
    if (getCarManual() != auto.getCarManual()) return false;
    if (getCarGps() != auto.getCarGps()) return false;
    if (getCarHook() != auto.getCarHook()) return false;
    if (getCarFuel() != auto.getCarFuel()) return false;
    if (getCarFuelEconomy() != auto.getCarFuelEconomy()) return false;
    if (getCarEstimatedValue() != auto.getCarEstimatedValue()) return false;
    if (getCarOwnerAnnualKm() != auto.getCarOwnerAnnualKm()) return false;
    if (getCarOwnerUserId() != auto.getCarOwnerUserId()) return false;
    if (getCarComments() != auto.getCarComments()) return false;
    if (getCarActive() != auto.getCarActive()) return false;
    //if (getimagesId() != auto.getimagesId()) return false;
    //if (getcreatedAt() != auto.getcreatedAt()) return false;
    //if (getupdatedAt() != auto.getupdatedAt()) return false;
    if (getCarDeprec() != auto.getCarDeprec()) return false;
    if (getCarDeprecLimit() != auto.getCarDeprecLimit()) return false;
    if (getCarDeprecLast() != auto.getCarDeprecLast()) return false;
    if (getCarStartSharing() != auto.getCarStartSharing()) return false;
    if (getCarEndSharing() != auto.getCarEndSharing()) return false;

    return true;
  }
}
