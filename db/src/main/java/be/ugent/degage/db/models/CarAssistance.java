/* CarAssistance.java
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
public class CarAssistance {

	@Expose
  private String name;
	@Expose
	private LocalDate expiration;
	@Expose
	private CarAssistanceType type;
	@Expose
	private String contractNr;
	@Expose
	private Integer fileId;

	public CarAssistance(String name, LocalDate expiration, CarAssistanceType type, String contractNr, int fileId) {
    this.name = name;
		this.expiration = expiration;
		this.type = type;
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

	public CarAssistanceType getType() {
		return type;
	}
	public void setType(CarAssistanceType type) {
		this.type = type;
	}

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
	}
	
	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
}
