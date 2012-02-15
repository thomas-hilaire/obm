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
package org.obm.push.task;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSTask;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;

import com.google.common.base.Objects;

/**
 * Convert events between OBM-Sync object model & Microsoft object model
 */
public class TaskConverter {

	private static final int TASK_IMPORTANCE_NORMAL = 1;

	public IApplicationData convert(Event e) {
		MSTask mse = new MSTask();

		mse.setSubject(e.getTitle());
		mse.setDescription(e.getDescription());

		if (e.getPriority() <= 1) {
			mse.setImportance(0);
		} else if (e.getPriority() > 1 && e.getPriority() <= 3) {
			mse.setImportance(1);
		} else {
			mse.setImportance(2);
		}
		mse.setSensitivity(e.getPrivacy() == EventPrivacy.PUBLIC ? CalendarSensitivity.NORMAL
				: CalendarSensitivity.PRIVATE);

		Date dateTimeEnd = new Date(e.getStartDate().getTime() + e.getDuration()
				* 1000);

		mse.setUtcDueDate(dateTimeEnd);
		mse.setDueDate(getDateInTimeZone(dateTimeEnd, e.getTimezoneName()));

		// if (e.getAlert() != null && e.getAlert() != 0) {
		// mse.setReminderSet(true);
		// Calendar cal = Calendar.getInstance();
		// cal.setTime(e.getDate());
		// cal.add(Calendar.SECOND, e.getAlert());
		// mse.setReminderTime(cal.getTime());
		// }

		mse.setStartDate(getDateInTimeZone(e.getStartDate(), e.getTimezoneName()));
		mse.setUtcStartDate(e.getStartDate());
		mse.setRecurrence(getRecurrence(e.getRecurrence()));

		return mse;
	}

	private Date getDateInTimeZone(Date currentDate, String timeZoneId) {
		Calendar mbCal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		mbCal.setTimeInMillis(currentDate.getTime());

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, mbCal.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, mbCal.get(Calendar.MONTH));
		cal.set(Calendar.DAY_OF_MONTH, mbCal.get(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, mbCal.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, mbCal.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, mbCal.get(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, mbCal.get(Calendar.MILLISECOND));

		return cal.getTime();
	}

	private MSRecurrence getRecurrence(EventRecurrence recurrence) {
		if (recurrence.getKind() == RecurrenceKind.none) {
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
		case none:
			r.setType(null);
			break;
		}
		r.setUntil(recurrence.getEnd());

		r.setInterval(recurrence.getFrequence());

		return r;
	}

	private String getDays(Set<RecurrenceDayOfWeek> dayOfWeek) {
		StringBuilder sb = new StringBuilder();
		if (dayOfWeek == null) {
			return "0000000";
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.SUNDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.MONDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.TUESDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.WEDNESDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.THURSDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.FRIDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.SATURDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		return sb.toString();
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

	public Event convert(BackendSession bs, Event oldEvent, MSTask task, Boolean isObmInternalEvent) {
		Event e = new Event();
		e.setInternalEvent(isObmInternalEvent);
		if (oldEvent != null) {
			e.setExtId(oldEvent.getExtId());
			for (Attendee att : oldEvent.getAttendees()) {
				if (task.getComplete()) {
					att.setPercent(100);
				} else if (att.getPercent() >= 100) {
					att.setPercent(0);
				}
				e.addAttendee(att);
			}
		} else {
			Attendee att = convertAttendee(bs, null);
			if (task.getComplete()) {
				att.setPercent(100);
			}
			e.addAttendee(att);
		}
		e.setType(EventType.VTODO);
		e.setTitle(task.getSubject());

		e.setDescription(task.getDescription());
		if (task.getUtcStartDate() != null) {
			e.setStartDate(task.getUtcStartDate());
			e.setTimezoneName("GMT");
		} else {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR, 0);
			e.setStartDate(cal.getTime());
		}

		int importance = Objects.firstNonNull(task.getImportance(), TASK_IMPORTANCE_NORMAL);
		switch (importance) {
		case 0:
			e.setPriority(1);
			break;
		case 2:
			e.setPriority(5);
			break;
		default:
		case 1:
			e.setPriority(3);
			break;
		}
		if (task.getReminderSet()) {
			long alert = Math.abs(task.getUtcStartDate().getTime()
					- task.getReminderTime().getTime());
			e.setAlert((int) alert);
		}

		e.setPrivacy(privacy(oldEvent, task.getSensitivity()));

		if (task.getUtcDueDate() != null) {
			long durmili = Math.abs(task.getUtcStartDate().getTime()
					- task.getUtcDueDate().getTime());
			e.setDuration((int) durmili / 1000);
		}
		e.setRecurrence(getRecurrence(task));

		return e;
	}

	private EventPrivacy privacy(Event oldEvent, CalendarSensitivity sensitivity) {
		if (sensitivity == null) {
			return oldEvent != null ? oldEvent.getPrivacy() : EventPrivacy.PUBLIC;
		}
		switch (sensitivity) {
		case CONFIDENTIAL:
		case PERSONAL:
		case PRIVATE:
			return EventPrivacy.PRIVATE;
		case NORMAL:
		default:
			return EventPrivacy.PUBLIC;
		}
	}

	private EventRecurrence getRecurrence(MSTask mst) {
		if (mst.getRecurrence() == null) {
			return null;
		}
		MSRecurrence pr = mst.getRecurrence();
		EventRecurrence or = new EventRecurrence();
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

		int multiply = 0;
		switch (pr.getType()) {
		case DAILY:
			or.setKind(RecurrenceKind.daily);
			or.setDays(getDays(pr.getDayOfWeek()));
			multiply = Calendar.DAY_OF_MONTH;
			break;
		case MONTHLY:
			or.setKind(RecurrenceKind.monthlybydate);
			multiply = Calendar.MONTH;
			break;
		case MONTHLY_NDAY:
			or.setKind(RecurrenceKind.monthlybyday);
			multiply = Calendar.MONTH;
			break;
		case WEEKLY:
			or.setKind(RecurrenceKind.weekly);
			or.setDays(getDays(pr.getDayOfWeek()));
			multiply = Calendar.WEEK_OF_YEAR;
			break;
		case YEARLY:
			or.setKind(RecurrenceKind.yearly);
			cal.setTimeInMillis(mst.getUtcStartDate().getTime());
			cal.set(Calendar.DAY_OF_MONTH, pr.getDayOfMonth());
			cal.set(Calendar.MONTH, pr.getMonthOfYear() - 1);
			mst.setUtcStartDate(cal.getTime());
			or.setFrequence(1);
			multiply = Calendar.YEAR;
			break;
		case YEARLY_NDAY:
			or.setKind(RecurrenceKind.yearly);
			multiply = Calendar.YEAR;
			break;
		}

		// interval
		if (pr.getInterval() != null) {
			or.setFrequence(pr.getInterval());
		}

		// occurence or end date
		Date endDate = null;
		if (pr.getOccurrences() != null && pr.getOccurrences() > 0) {
			cal.setTimeInMillis(pr.getStart().getTime());
			cal.add(multiply, pr.getOccurrences() - 1);
			endDate = new Date(cal.getTimeInMillis());
		} else {
			endDate = pr.getUntil();
		}
		or.setEnd(endDate);

		return or;
	}

	private Attendee convertAttendee(BackendSession bs, Event oldEvent) {
		ParticipationState oldState = ParticipationState.NEEDSACTION;
		if (oldEvent != null) {
			for (Attendee oldAtt : oldEvent.getAttendees()) {
				if (oldAtt.getEmail().equals(bs.getCredentials().getUser().getEmail())) {
					oldState = oldAtt.getState();
					break;
				}
			}
		}
		Attendee ret = new Attendee();
		ret.setEmail(bs.getCredentials().getUser().getEmail());
		ret.setParticipationRole(ParticipationRole.REQ);
		ret.setState(status(oldState, AttendeeStatus.ACCEPT));
		return ret;
	}

	private ParticipationState status(ParticipationState oldParticipationState,
			AttendeeStatus attendeeStatus) {
		if (attendeeStatus == null) {
			return oldParticipationState;
		}
		switch (attendeeStatus) {
		case DECLINE:
			return ParticipationState.DECLINED;
		case NOT_RESPONDED:
		case RESPONSE_UNKNOWN:
		case TENTATIVE:
			return ParticipationState.NEEDSACTION;
		default:
		case ACCEPT:
			return ParticipationState.ACCEPTED;
		}
	}
}
