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
package org.obm.push.bean.autodiscover;

import com.google.common.base.Objects;

public class AutodiscoverRequest {

	private final String emailAddress;
	private final String acceptableResponseSchema;
	
	public AutodiscoverRequest(String emailAddress, String acceptableResponseSchema) {
		this.emailAddress = emailAddress;
		this.acceptableResponseSchema = acceptableResponseSchema;
	}
	
	/**
	 * Is used to identify the user's mailbox in the network
	 *
	 * @return the SMTP e-mail address of the user
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	
	/**
	 * Example : "http://schemas.microsoft.com/exchange/autodiscover/mobilesync/responseschema/2006".
	 *
	 * @return schema in which the server MUST send the response
	 */
	public String getAcceptableResponseSchema() {
		return acceptableResponseSchema;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(emailAddress, acceptableResponseSchema);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof AutodiscoverRequest) {
			AutodiscoverRequest that = (AutodiscoverRequest) object;
			return Objects.equal(this.emailAddress, that.emailAddress)
				&& Objects.equal(this.acceptableResponseSchema, that.acceptableResponseSchema);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("emailAddress", emailAddress)
			.add("acceptableResponseSchema", acceptableResponseSchema)
			.toString();
	}
	
}