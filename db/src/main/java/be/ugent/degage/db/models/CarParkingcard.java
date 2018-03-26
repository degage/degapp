/* CarParkingcard.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 *
 * This file is part of the Degage Web Application
 *
 * Corresponding author (see also AUTHORS.txt)
 *
 * Emmanuel Isebaert
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

public class CarParkingcard {

  @Expose
  private String city;
  @Expose
	private LocalDate expiration;
  @Expose
	private String zones;
  @Expose
	private String contractNr;
	@Expose
	private Integer fileId;

	public CarParkingcard(String city, LocalDate expiration, String zones, String contractNr, int fileId) {
		this.city = city;
		this.expiration = expiration;
		this.zones = zones;
		this.contractNr = contractNr;
		this.fileId = fileId;
	}


	public LocalDate getExpiration() {
		return expiration;
	}
	public void setExpiration(LocalDate expiration) {
		this.expiration = expiration;
	}

	public String getContractNr() {
		return contractNr;
	}
	public void setContractNr(String contractNr) {
		this.contractNr = contractNr;
	}

	public String getZones() {
		return zones;
	}
	public void setZones(String zones) {
		this.zones = zones;
	}

	public String getCity() {
			return city;
	}
	public void setCity(String city) {
			this.city = city;
	}

	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(Integer fileId) {
		this.fileId = fileId;
	}
}
