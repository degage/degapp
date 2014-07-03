package be.ugent.degage.db.models;

import org.joda.time.DateTime;

import java.util.Date;

public class CarInsurance {

	private Integer id;
    private String name;
	private Date expiration;
	private Integer bonusMalus;
	private Integer polisNr;

    public CarInsurance(String name, Date expiration, Integer bonusMalus, Integer polisNr) {
        this(null, name, expiration, bonusMalus, polisNr);
    }
	
	public CarInsurance(Integer id, String name, Date expiration, Integer bonusMalus, Integer polisNr) {
		this.id=id;
        this.name = name;
		this.expiration = expiration;
		this.bonusMalus = bonusMalus;
		this.polisNr = polisNr;
	}
	
	
	public Date getExpiration() {
		return expiration;
	}
	public Integer getPolisNr() {
		return polisNr;
	}

	public void setPolisNr(Integer polisNr) {
		this.polisNr = polisNr;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
	public Integer getBonusMalus() {
		return bonusMalus;
	}
	public void setBonusMalus(Integer bonusMalus) {
		this.bonusMalus = bonusMalus;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
