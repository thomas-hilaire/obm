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
package org.obm.sync.client.setting;

import java.util.Map;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.locators.Locator;
import org.obm.sync.services.ISetting;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.SettingItemsParser;
import org.obm.sync.setting.VacationSettings;
import org.w3c.dom.Document;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SettingClient extends AbstractClientImpl implements ISetting {

	private final SettingItemsParser respParser;
	private final Locator locator;

	@Inject
	private SettingClient(SyncClientException syncClientException, Locator locator) {
		super(syncClientException);
		this.locator = locator;
		this.respParser = new SettingItemsParser();
	}

	@Override
	public Map<String, String> getSettings(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, "/setting/getSettings", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListSettings(doc);
	}

	@Override
	public void setVacationSettings(AccessToken token, VacationSettings vs) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("enabled", "" + vs.isEnabled());
		if (vs.isEnabled()) {
			if (vs.getStart() != null) {
				params.put("start", "" + vs.getStart().getTime());
			}
			if (vs.getEnd() != null) {
				params.put("end", "" + vs.getEnd().getTime());
			}
			params.put("text", "" + vs.getText());
		}
		Document doc = execute(token, "/setting/setVacationSettings", params);
		exceptionFactory.checkServerFaultException(doc);
	}

	@Override
	public void setEmailForwarding(AccessToken token, ForwardingSettings fs) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("enabled", "" + fs.isEnabled());
		if (fs.getEmail() != null && fs.isEnabled()) {
			params.put("email", fs.getEmail());
		}
		params.put("localCopy", "" + fs.isLocalCopy());
		Document doc = execute(token, "/setting/setEmailForwarding", params);
		exceptionFactory.checkServerFaultException(doc);
	}

	@Override
	public ForwardingSettings getEmailForwarding(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, "/setting/getEmailForwarding", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseForwarding(doc);
	}

	@Override
	public VacationSettings getVacationSettings(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, "/setting/getVacationSettings", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseVacation(doc);
	}
	
	@Override
	protected Locator getLocator() {
		return locator;
	}
	
}
