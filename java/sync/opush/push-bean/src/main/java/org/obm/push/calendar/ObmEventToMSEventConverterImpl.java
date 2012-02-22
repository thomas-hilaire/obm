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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventCommon;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.User;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

@Singleton
public class ObmEventToMSEventConverterImpl implements ObmEventToMSEventConverter {

	@Override
	public MSEvent convert(Event e, MSEventUid uid, User user) {
		MSEvent mse = new MSEvent();

		fillEventCommonProperties(e, mse);
		
		mse.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		appendAttendeesAndOrganizer(e, mse, user);
		mse.setUid(uid);
		mse.setRecurrence(getRecurrence(e.getRecurrence()));
		mse.setExceptions(getException(e.getRecurrence()));
		mse.setExtId(e.getExtId());
		mse.setObmId(e.getObmId());
		mse.setObmSequence(e.getSequence());
		appendDtStamp(mse, e);
		return mse;
	}

	private void fillEventCommonProperties(Event e, MSEventCommon mse) {
		mse.setSubject(e.getTitle());
		mse.setDescription(e.getDescription());
		mse.setLocation(e.getLocation());
		mse.setStartTime(e.getStartDate());
	
		Date endtTime = endTime(e.getStartDate().getTime(), e.getDuration());
		mse.setEndTime(endtTime);
		
		mse.setAllDayEvent(e.isAllday());
		

		mse.setReminder(reminder(e));
		mse.setBusyStatus(busyStatus(e.getOpacity()));
		mse.setSensitivity(sensitivity(e.getPrivacy()));

		mse.setCategories(category(e));
		mse.setMeetingStatus(CalendarMeetingStatus.IS_A_MEETING);
	}

	private Date endTime(long startTime, int duration) {
		if (duration > 0) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			c.setTimeInMillis(startTime);
			c.add(Calendar.SECOND, duration);
			return c.getTime();
		}
		throw new IllegalArgumentException("Duration must to be positive value");
	}
	
	private List<String> category(Event e) {
		if (e.getCategory() != null) {
			return Lists.newArrayList(e.getCategory());
		} else {
			return null;
		}
	}

	private Integer reminder(Event e) {
		Integer alert = e.getAlert();
		if (alert == null) {
			return null;
		} else {
			return alert / 60;
		}
	}

	private MSEventException convertException(Event exception) {
		MSEventException msEventException = new MSEventException();
		fillEventCommonProperties(exception, msEventException);
		msEventException.setExceptionStartTime(exception.getRecurrenceId());

		return msEventException;
	}
	
	private void appendAttendeesAndOrganizer(Event e, MSEvent mse, User user) {
		String userEmail = user.getEmail();
		boolean hasOrganizer = false;
		for (Attendee at: e.getAttendees()) {
			if (at.isOrganizer()) {
				hasOrganizer = true;
				appendOrganizer(mse, at);
			} 
			if (!hasOrganizer && userEmail.equalsIgnoreCase(at.getEmail())) {
				appendOrganizer(mse, at);
			}
			mse.addAttendee(convertAttendee(at));
		}
	}

	private void appendOrganizer(MSEvent mse, Attendee at) {
		mse.setOrganizerName(at.getDisplayName());
		mse.setOrganizerEmail(at.getEmail());		
	}

	private void appendDtStamp(MSEvent mse, Event e) {
		mse.setDtStamp(Objects.firstNonNull(e.getTimeUpdate(), new Date()));
	}

	@VisibleForTesting CalendarSensitivity sensitivity(EventPrivacy privacy) {
		Preconditions.checkNotNull(privacy);
		switch (privacy) {
		case PRIVATE:
			return CalendarSensitivity.PRIVATE;
		case PUBLIC:
			return CalendarSensitivity.NORMAL;
		}
		throw new IllegalArgumentException("EventPrivacy " + privacy + " can't be converted to MSEvent property");
	}

	@VisibleForTesting CalendarBusyStatus busyStatus(EventOpacity opacity) {
		Preconditions.checkNotNull(opacity);
		switch (opacity) {
		case TRANSPARENT:
			return CalendarBusyStatus.FREE;
		case OPAQUE:
			return CalendarBusyStatus.BUSY;
		}
		throw new IllegalArgumentException("EventOpacity " + opacity + " can't be converted to MSEvent property");
	}
	
	private MSAttendee convertAttendee(Attendee at) {
		MSAttendee msa = new MSAttendee();

		msa.setAttendeeStatus(status(at.getState()));
		msa.setEmail(at.getEmail());
		msa.setName(at.getDisplayName());
		msa.setAttendeeType(participationRole(at.getParticipationRole()));

		return msa;
	}

	@VisibleForTesting AttendeeStatus status(ParticipationState state) {
		Preconditions.checkNotNull(state);
		switch (state) {
		case DECLINED:
			return AttendeeStatus.DECLINE;
		case NEEDSACTION:
			return AttendeeStatus.NOT_RESPONDED;
		case TENTATIVE:
			return AttendeeStatus.TENTATIVE;
		case ACCEPTED:
			return AttendeeStatus.ACCEPT;
		default:
		case COMPLETED:
		case DELEGATED:
		case INPROGRESS:
			return AttendeeStatus.RESPONSE_UNKNOWN;
		}
	}

	@VisibleForTesting AttendeeType participationRole(ParticipationRole role) {
		Preconditions.checkNotNull(role);
		switch (role) {
		case REQ:
		case CHAIR:
			return AttendeeType.REQUIRED;
		case NON:
		case OPT:
			return AttendeeType.OPTIONAL;
		}
		throw new IllegalArgumentException("ParticipationRole " + role + " can't be converted to MSEvent property");
	}

	private MSRecurrence getRecurrence(EventRecurrence recurrence) {
		if (recurrence == null || recurrence.getKind() == RecurrenceKind.none) {
			return null;
		}

		MSRecurrence r = new MSRecurrence();
		switch (recurrence.getKind()) {
		case daily:
			r.setType(RecurrenceType.DAILY);
			break;
		case monthlybydate:
			r.setType(RecurrenceType.MONTHLY);
			break;
		case monthlybyday:
			r.setType(RecurrenceType.MONTHLY_NDAY);
			break;
		case weekly:
			r.setType(RecurrenceType.WEEKLY);
			r.setDayOfWeek(daysOfWeek(recurrence.getDays()));
			break;
		case yearly:
			r.setType(RecurrenceType.YEARLY);
			break;
		case yearlybyday:
			r.setType(RecurrenceType.YEARLY_NDAY);
			break;
		case none:
			r.setType(null);
			break;
		}
		r.setUntil(recurrence.getEnd());

		r.setInterval(getInterval(recurrence));

		return r;
	}

	private int getInterval(EventRecurrence recurrence) {
		if (recurrence.frequencyIsSpecified()) {
			return recurrence.getFrequence();
		} else {
			return ACTIVESYNC_DEFAULT_FREQUENCY;
		}
	}

	private Set<RecurrenceDayOfWeek> daysOfWeek(String string) {
		char[] days = string.toCharArray();
		Set<RecurrenceDayOfWeek> daysList = new HashSet<RecurrenceDayOfWeek>();
		int i = 0;
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.SUNDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.MONDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.TUESDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.WEDNESDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.THURSDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.FRIDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.SATURDAY);
		}

		return daysList;
	}
	

	private List<MSEventException> getException(EventRecurrence recurrence) {
		List<MSEventException> ret = Lists.newArrayList();
		if (recurrence == null || recurrence.getKind() == RecurrenceKind.none) {
			return ret;
		}
		
		for (Date excp : recurrence.getExceptions()) {
			MSEventException e = deletionException(excp);
			ret.add(e);
		}

		for (Event exception : recurrence.getEventExceptions()) {
			MSEventException e = convertException(exception);
			ret.add(e);
		}
		return ret;
	}

	private MSEventException deletionException(Date excp) {
		MSEventException e = new MSEventException();
		e.setDeleted(true);
		e.setExceptionStartTime(excp);
		return e;
	}

}
