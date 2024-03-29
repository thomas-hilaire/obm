/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.bean;

import com.google.common.base.Objects;

public class SearchResult {

	private String displayName;
	private String alias;
	private String emailAddress;
	private String firstName;
	private String lastName;
	private String Phone;
	private String Office;
	private String Title;
	private String Company;
	private String HomePhone;
	private String MobilePhone;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhone() {
		return Phone;
	}

	public void setPhone(String phone) {
		Phone = phone;
	}

	public String getOffice() {
		return Office;
	}

	public void setOffice(String office) {
		Office = office;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getCompany() {
		return Company;
	}

	public void setCompany(String company) {
		Company = company;
	}

	public String getHomePhone() {
		return HomePhone;
	}

	public void setHomePhone(String homePhone) {
		HomePhone = homePhone;
	}

	public String getMobilePhone() {
		return MobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		MobilePhone = mobilePhone;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(displayName, alias, emailAddress, firstName, lastName, 
				Phone, Office, Title, Company, HomePhone, MobilePhone);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SearchResult) {
			SearchResult that = (SearchResult) object;
			return Objects.equal(this.displayName, that.displayName)
				&& Objects.equal(this.alias, that.alias)
				&& Objects.equal(this.emailAddress, that.emailAddress)
				&& Objects.equal(this.firstName, that.firstName)
				&& Objects.equal(this.lastName, that.lastName)
				&& Objects.equal(this.Phone, that.Phone)
				&& Objects.equal(this.Office, that.Office)
				&& Objects.equal(this.Title, that.Title)
				&& Objects.equal(this.Company, that.Company)
				&& Objects.equal(this.HomePhone, that.HomePhone)
				&& Objects.equal(this.MobilePhone, that.MobilePhone);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("displayName", displayName)
			.add("alias", alias)
			.add("emailAddress", emailAddress)
			.add("firstName", firstName)
			.add("lastName", lastName)
			.add("Phone", Phone)
			.add("Office", Office)
			.add("Title", Title)
			.add("Company", Company)
			.add("HomePhone", HomePhone)
			.add("MobilePhone", MobilePhone)
			.toString();
	}
	
}
