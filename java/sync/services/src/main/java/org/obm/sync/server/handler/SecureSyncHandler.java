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
package org.obm.sync.server.handler;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.server.ParametersSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import fr.aliacom.obm.common.session.SessionManagement;

public abstract class SecureSyncHandler implements ISyncHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private SessionManagement sessions;

	protected SecureSyncHandler(SessionManagement sessionManagement) {
		sessions = sessionManagement;
	}

	protected String p(ParametersSource params, String name) {
		return params.getParameter(name);
	}

	protected int i(ParametersSource params, String name, int defaultValue) {
		String text = params.getParameter(name);
		if (Strings.isNullOrEmpty(text)) {
			return defaultValue;
		} else {
			return Integer.valueOf(text);
		}
	}

	private AccessToken getToken(ParametersSource params) {
		AccessToken at = new AccessToken(0, "unused");
		at.setSessionId(params.getParameter("sid"));
		return at;
	}

	protected AccessToken getCheckedToken(ParametersSource params)
			throws AuthFault {
		AccessToken token = getToken(params);
		sessions.checkToken(token);
		return token;
	}

}
