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
package org.obm.push.protocol.data;

import org.obm.push.bean.IApplicationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class EncoderFactory {

	private static final Logger logger = LoggerFactory.getLogger(EncoderFactory.class);
	
	private final Provider<CalendarEncoder> calendarProvider;
	private final Provider<ContactEncoder> contactProvider;
	private final Provider<TaskEncoder> taskEncoder;
	private final Provider<EmailEncoder> emailEncoder;

	@Inject
	private EncoderFactory(Provider<CalendarEncoder> calendarProvider,
			Provider<ContactEncoder> contactProvider,
			Provider<TaskEncoder> taskEncoder,
			Provider<EmailEncoder> emailEncoder) {
		super();
		this.calendarProvider = calendarProvider;
		this.contactProvider = contactProvider;
		this.taskEncoder = taskEncoder;
		this.emailEncoder = emailEncoder;
	}
	
	public IDataEncoder getEncoder(IApplicationData data) {
		if (data != null) {
			switch (data.getType()) {

			case CALENDAR:
				return calendarProvider.get();

			case CONTACTS:
				return contactProvider.get();

			case TASKS:
				return taskEncoder.get();

			case EMAIL:
			default:
				return emailEncoder.get();
			}
		} else {
			logger.warn("TRY TO ENCODE NULL OBJECT");
			return null;
		}
	}

}
