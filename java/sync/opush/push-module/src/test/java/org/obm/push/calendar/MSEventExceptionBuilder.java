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
package org.obm.push.calendar;

import java.util.Date;
import java.util.List;

import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSEventException;

public class MSEventExceptionBuilder {

	private final MSEventException msEventException;

	public MSEventExceptionBuilder() {
		this.msEventException = new MSEventException();
	}

	public MSEventExceptionBuilder withLocation(String location) {
		this.msEventException.setLocation(location);
		return this;
	}

	public MSEventExceptionBuilder withSubject(String subject) {
		this.msEventException.setSubject(subject);
		return this;
	}

	public MSEventExceptionBuilder withDescription(String description) {
		this.msEventException.setDescription(description);
		return this;
	}

	public MSEventExceptionBuilder withDtStamp(Date dtStamp) {
		this.msEventException.setDtStamp(dtStamp);
		return this;
	}

	public MSEventExceptionBuilder withEndTime(Date endTime) {
		this.msEventException.setEndTime(endTime);
		return this;
	}

	public MSEventExceptionBuilder withStartTime(Date startTime) {
		this.msEventException.setStartTime(startTime);
		return this;
	}

	public MSEventExceptionBuilder withAllDayEvent(Boolean allDayEvent) {
		this.msEventException.setAllDayEvent(allDayEvent);
		return this;
	}

	public MSEventExceptionBuilder withBusyStatus(CalendarBusyStatus busyStatus) {
		this.msEventException.setBusyStatus(busyStatus);
		return this;
	}

	public MSEventExceptionBuilder withSensitivity(CalendarSensitivity sensitivity) {
		this.msEventException.setSensitivity(sensitivity);
		return this;
	}

	public MSEventExceptionBuilder withMeetingStatus(CalendarMeetingStatus meetingStatus) {
		this.msEventException.setMeetingStatus(meetingStatus);
		return this;
	}

	public MSEventExceptionBuilder withReminder(Integer reminder) {
		this.msEventException.setReminder(reminder);
		return this;
	}

	public MSEventExceptionBuilder withCategories(List<String> categories) {
		this.msEventException.setCategories(categories);
		return this;
	}

	public MSEventExceptionBuilder withExceptionStartTime(Date exceptionStartTime) {
		this.msEventException.setExceptionStartTime(exceptionStartTime);
		return this;
	}

	public MSEventExceptionBuilder withDeleted(boolean deletedException) {
		this.msEventException.setDeleted(deletedException);
		return this;
	}

	public MSEventException build() {
		return msEventException;
	}

}
