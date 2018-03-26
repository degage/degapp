/* CarInsurance.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 *
 * This file is part of the Degage Web Application
 *
 * Corresponding author (see also AUTHORS.txt)
 *
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 *
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.models;

import java.time.LocalDate;
import com.google.gson.annotations.Expose;

public class CarInsurance {

  @Expose
    private String name;
    @Expose
	private LocalDate expiration;
  @Expose
	private String bonusMalus;
  @Expose
	private String polisNr;

  @Expose
	private LocalDate startInsurancePolicy;
  @Expose
	private Integer startBonusMalus;
  @Expose
	private boolean civilLiability;
  @Expose
	private boolean legalCounsel;
  @Expose
	private boolean driverInsurance;
  @Expose
	private boolean materialDamage;
  @Expose
	private Integer valueExclusiveVAT;
  @Expose
	private Integer exemption; //vrijstelling
  @Expose
	private boolean glassBreakage;
  @Expose
	private boolean theft;
	@Expose
	private String insuranceNameBefore;
	@Expose
	private Integer insuranceFileId;
	@Expose
	private Integer greenCardFileId;

	public CarInsurance(String name, LocalDate expiration, String bonusMalus, String polisNr, LocalDate startInsurancePolicy, Integer startBonusMalus, boolean civilLiability, boolean legalCounsel, boolean driverInsurance, boolean materialDamage, Integer valueExclusiveVAT, Integer exemption, boolean glassBreakage, boolean theft, String insuranceNameBefore, Integer insuranceFileId, Integer greenCardFileId) {
		this.name = name;
		this.expiration = expiration;
		this.bonusMalus = bonusMalus;
		this.polisNr = polisNr;
		this.startInsurancePolicy = startInsurancePolicy;
		this.startBonusMalus = startBonusMalus;
		this.civilLiability = civilLiability;
		this.legalCounsel = legalCounsel;
		this.driverInsurance = driverInsurance;
		this.materialDamage = materialDamage;
		this.valueExclusiveVAT = valueExclusiveVAT;
		this.exemption = exemption;
		this.glassBreakage = glassBreakage;
		this.theft = theft;
		this.insuranceNameBefore = insuranceNameBefore;
		this.insuranceFileId = insuranceFileId;
		this.greenCardFileId = greenCardFileId;
	}

	public CarInsurance(String name, LocalDate expiration, String bonusMalus, String polisNr) {
    this.name = name;
		this.expiration = expiration;
		this.bonusMalus = bonusMalus;
		this.polisNr = polisNr;
	}

	public CarInsurance(String insuranceNameBefore, LocalDate expiration) {
		this.insuranceNameBefore = insuranceNameBefore;
		this.expiration = expiration;
	}	

	public LocalDate getExpiration() {
		return expiration;
	}
	public String getPolisNr() {
		return polisNr;
	}

	public void setPolisNr(String polisNr) {
		this.polisNr = polisNr;
	}

	public void setExpiration(LocalDate expiration) {
		this.expiration = expiration;
	}
	public String getBonusMalus() {
		return bonusMalus;
	}
	public void setBonusMalus(String bonusMalus) {
		this.bonusMalus = bonusMalus;
	}

	public String getName() {
			return name;
	}

	public void setName(String name) {
			this.name = name;
	}

	public LocalDate getStartInsurancePolicy() {
		return startInsurancePolicy;
	}

	public void setStartInsurancePolicy(LocalDate startInsurancePolicy) {
		this.startInsurancePolicy = startInsurancePolicy;
	}

	public Integer getStartBonusMalus() {
		return startBonusMalus;
	}

	public void setStartBonusMalus(Integer startBonusMalus) {
		this.startBonusMalus = startBonusMalus;
	}

	public boolean isCivilLiability() {
		return civilLiability;
	}

	public void setCivilLiability(boolean civilLiability) {
		this.civilLiability = civilLiability;
	}

	public boolean isLegalCounsel() {
		return legalCounsel;
	}

	public void setLegalCounsel(boolean legalCounsel) {
		this.legalCounsel = legalCounsel;
	}

	public boolean isDriverInsurance() {
		return driverInsurance;
	}

	public void setDriverInsurance(boolean driverInsurance) {
		this.driverInsurance = driverInsurance;
	}

	public boolean isMaterialDamage() {
		return materialDamage;
	}

	public void setMaterialDamage(boolean materialDamage) {
		this.materialDamage = materialDamage;
	}

	public Integer getValueExclusiveVAT() {
		return valueExclusiveVAT;
	}

	public void setValueExclusiveVAT(Integer valueExclusiveVAT) {
		this.valueExclusiveVAT = valueExclusiveVAT;
	}

	public Integer getExemption() {
		return exemption;
	}

	public void setExemption(Integer exemption) {
		this.exemption = exemption;
	}

	public boolean isGlassBreakage() {
		return glassBreakage;
	}

	public void setGlassBreakage(boolean glassBreakage) {
		this.glassBreakage = glassBreakage;
	}

	public boolean isTheft() {
		return theft;
	}

	public void setTheft(boolean theft) {
		this.theft = theft;
	}

	public String getInsuranceNameBefore() {
		return insuranceNameBefore;
	}

	public void setInsuranceNameBefore(String insuranceNameBefore) {
		this.insuranceNameBefore = insuranceNameBefore;
	}

	public Integer getInsuranceFileId() {
		return insuranceFileId;
	}

	public void setInsuranceFileId(Integer insuranceFileId) {
		this.insuranceFileId = insuranceFileId;
	}

	public Integer getGreenCardFileId() {
		return greenCardFileId;
	}

	public void setGreenCardFileId(Integer greenCardFileId) {
		this.greenCardFileId = greenCardFileId;
	}
}
