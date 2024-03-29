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
package org.obm.sync.auth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obm.sync.calendar.CalendarInfo;

import fr.aliacom.obm.common.domain.ObmDomain;

public class AccessToken {

	private String userLogin;
	private String userDisplayName;
	private String userEmail;
	
	private String sessionId;
	private int obmId;
	private ObmDomain domain;
	private String origin;
	private boolean rootAccount;

	private Map<String, String> isoCodeToNameCache;

	private Map<String, String> serviceProps;

	private Collection<CalendarInfo> calendarRights;

	private MavenVersion version;
	private int conversationUid;

	public AccessToken(int obmId, String origin) {
		this.obmId = obmId;
		this.origin = origin;
		this.isoCodeToNameCache = new HashMap<String, String>();
		this.serviceProps = new HashMap<String, String>();
	}
	
	public ObmDomain getDomain() {
		return domain;
	}

	public void setDomain(ObmDomain domain) {
		this.domain = domain;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public int getObmId() {
		return obmId;
	}

	public void setObmId(int obmId) {
		this.obmId = obmId;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public Map<String, String> getIsoCodeToNameCache() {
		return isoCodeToNameCache;
	}

	public void setIsoCodeToNameCache(Map<String, String> isoCodeToNameCache) {
		this.isoCodeToNameCache = isoCodeToNameCache;
	}

	public String getServiceProperty(String key) {
		return serviceProps.get(key);
	}

	public void addServiceProperty(String key, String value) {
		serviceProps.put(key, value);
	}

	public Map<String, String> getServiceProperties() {
		return serviceProps;
	}

	public void setServiceProperties(Map<String, String> props) {
		this.serviceProps = props;
	}

	public Collection<CalendarInfo> getCalendarRights() {
		return calendarRights;
	}

	public void setCalendarRights(Collection<CalendarInfo> calendarRights) {
		this.calendarRights = calendarRights;
	}

	public void setVersion(MavenVersion version) {
		this.version = version;
	}

	public MavenVersion getVersion() {
		return version;
	}

	public void setRootAccount(boolean rootAccount) {
		this.rootAccount = rootAccount;
	}

	public boolean isRootAccount() {
		return rootAccount;
	}

	public int getConversationUid() {
		return conversationUid;
	}

	public void setConversationUid(int conversationUid) {
		this.conversationUid = conversationUid;
	}
	
	public String getUserWithDomain() {
		return userLogin + "@" + domain.getName();
	}

	public String getUserDisplayName() {
		return userDisplayName;
	}
	
	public void setUserDisplayName(String displayName) {
		this.userDisplayName = displayName;
	}
	
}
