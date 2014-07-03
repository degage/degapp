package be.ugent.degage.db.models;

public class CarDetails {

	private int id;
	private String licensePlate;
	private String registration;
	private int chasisNumber;
	
	public CarDetails(int id, String licensePlate, int chasisNumber){
		this(id,licensePlate,null,chasisNumber);
	}
	
	public CarDetails(int id, String licensePlate, String registration, int chasisNumber) {
		this.id = id;
		this.licensePlate = licensePlate;
		this.registration = registration;
		this.chasisNumber = chasisNumber;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLicensePlate() {
		return licensePlate;
	}
	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}
	public String getRegistration() {
		return registration;
	}
	public void setRegistration(String registration) {
		this.registration = registration;
	}
	public int getChasisNumber() {
		return chasisNumber;
	}
	public void setChasisNumber(int chasisNumber) {
		this.chasisNumber = chasisNumber;
	}
	
	
}
