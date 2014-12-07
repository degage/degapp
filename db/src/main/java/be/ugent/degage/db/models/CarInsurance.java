package be.ugent.degage.db.models;

import java.time.LocalDate;

public class CarInsurance {

    private String name;
	private LocalDate expiration;
	private Integer bonusMalus;
	private Integer polisNr;

	public CarInsurance(String name, LocalDate expiration, Integer bonusMalus, Integer polisNr) {
        this.name = name;
		this.expiration = expiration;
		this.bonusMalus = bonusMalus;
		this.polisNr = polisNr;
	}
	
	
	public LocalDate getExpiration() {
		return expiration;
	}
	public Integer getPolisNr() {
		return polisNr;
	}

	public void setPolisNr(Integer polisNr) {
		this.polisNr = polisNr;
	}

	public void setExpiration(LocalDate expiration) {
		this.expiration = expiration;
	}
	public Integer getBonusMalus() {
		return bonusMalus;
	}
	public void setBonusMalus(Integer bonusMalus) {
		this.bonusMalus = bonusMalus;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
