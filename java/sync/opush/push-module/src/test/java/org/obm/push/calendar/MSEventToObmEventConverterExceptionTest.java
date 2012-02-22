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


import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.User;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.IllegalMSEventExceptionStateException;
import org.obm.push.exception.IllegalMSEventStateException;
import org.obm.push.utils.DateUtils;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventMeetingStatus;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MSEventToObmEventConverterExceptionTest {

	private MSEventToObmEventConverterImpl converter;

	private User user;
	
	@Before
	public void setUp() {
		converter = new MSEventToObmEventConverterImpl();
		String mailbox = "user@domain";
	    user = User.Factory.create()
				.createUser(mailbox, mailbox, null);
	}

	@Test
	public void testConvertAttributeSubjectWhenSpecified() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("The subject of exception")
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSubject("The parent subject");
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTitle()).isEqualTo(msEventException.getSubject());
	}

	@Test
	public void testConvertAttributeSubjectEmptyGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("")
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSubject("The parent subject");

		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTitle()).isEqualTo(msEvent.getSubject());
	}

	@Test
	public void testConvertAttributeSubjectNull() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSubject("The parent subject");
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTitle()).isEqualTo(msEvent.getSubject());
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeStartTimeOnly() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeStartTimeNullOnly() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(null)
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeOnly() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeNullOnly() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withEndTime(null)
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeNullAndStartTime() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(null)
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeStartAndEndTime() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);

		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getStartDate()).isEqualTo(msEventException.getStartTime());
		Assertions.assertThat(exception.getEndDate()).isEqualTo(msEventException.getEndTime());
	}

	@Test
	public void testConvertAttributeDtStampJustCreated() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDtStamp(date("2004-12-10T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTimeCreate()).isEqualTo(msEventException.getDtStamp());
		Assertions.assertThat(exception.getTimeUpdate()).isEqualTo(msEventException.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampAlreadyCreated() throws ConversionException {
		Date previousDtStampDate = date("2004-12-11T11:15:10Z");
		Event editingEvent = new Event();
		editingEvent.setTimeCreate(previousDtStampDate);
		editingEvent.setTimeUpdate(previousDtStampDate);

		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDtStamp(date("2004-12-10T11:15:10Z"))
				.build();
		
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(date("2004-12-10T12:15:10Z"))
				.withRecurrence(simpleDailyRecurrence(RecurrenceType.DAILY, RecurrenceDayOfWeek.FRIDAY))
				.withExceptions(Lists.newArrayList(msEventException))
				.build();
		
		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getTimeCreate()).isEqualTo(previousDtStampDate);
		Assertions.assertThat(exception.getTimeUpdate()).isEqualTo(msEventException.getDtStamp());
	}

	@Test(expected=IllegalMSEventExceptionStateException.class)
	public void testConvertAttributeDtStampInExceptionIsRequired() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDtStamp(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventExceptionStateException.class)
	public void testConvertAttributeDtStampInExceptionDontGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDtStamp(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setDtStamp(date("2004-12-10T12:15:10Z"));
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeMeetingStatusIsInMeeting() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusIsNotInMeeting() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_NOT_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceledAndReceived() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingReceived() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceled() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED);
	}

	@Test(expected=IllegalMSEventExceptionStateException.class)
	public void testConvertAttributeMeetingStatusIsRequired() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventExceptionStateException.class)
	public void testConvertAttributeMeetingStatusDontGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withMeetingStatus(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING);
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeDeletedTrue() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDeleted(true)
				.build();

		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(simpleDailyRecurrence(RecurrenceType.DAILY, RecurrenceDayOfWeek.FRIDAY))
				.withExceptions(Lists.newArrayList(msEventException))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Date[] exceptions = convertedEvent.getRecurrence().getExceptions();
		List<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		Assertions.assertThat(exceptions).hasSize(1);
		Assertions.assertThat(exceptions).containsOnly(msEventException.getExceptionStartTime());
		Assertions.assertThat(eventExceptions).isEmpty();
	}

	@Test
	public void testConvertAttributeDeletedFalse() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withDeleted(false)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Date[] exceptions = convertedEvent.getRecurrence().getExceptions();
		List<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		Assertions.assertThat(exceptions).isEmpty();
		Assertions.assertThat(eventExceptions).hasSize(1);
		Assertions.assertThat(eventExceptions.get(0).getRecurrenceId()).isEqualTo(msEventException.getExceptionStartTime());
	}

	@Test
	public void testCalculatedAttributeDurationByStartAndEndTime() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.getStartDate()).isEqualTo(msEventException.getStartTime());
		Assertions.assertThat(eventException.getEndDate()).isEqualTo(msEventException.getEndTime());
		Assertions.assertThat(eventException.getDuration()).isEqualTo(getOneYearInSecond());
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayOnly() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Event eventException = exceptionOf(convertedEvent);
		Date earlyMidnight = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEventException.getStartTime());
		Assertions.assertThat(eventException.getStartDate()).isEqualTo(earlyMidnight);
		Assertions.assertThat(eventException.getDuration()).isEqualTo(getOneDayInSecond());
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayWhenHasEndTime() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T12:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Event eventException = exceptionOf(convertedEvent);
		Date earlyMidnight = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEventException.getStartTime());
		Assertions.assertThat(eventException.getStartDate()).isEqualTo(earlyMidnight);
		Assertions.assertThat(eventException.getDuration()).isEqualTo(getOneDayInSecond());
	}

	@Test
	public void testConvertAttributeAllDayFalse() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(false)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayTrue() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNull() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isFalse();
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeAllDayFalseNeedStartTime() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(false)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}
	
	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeAllDayNullNeedStartTime() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeAllDayNullGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setAllDayEvent(true);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNullAndParentNullToo() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setAllDayEvent(null);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayMakeEventStartMidnightAndFinishAtMidnight() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setAllDayEvent(null);
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Date earlyMidnight = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEventException.getStartTime());
		Date lateMidnight = org.obm.push.utils.DateUtils.getMidnightOfDayLate(msEventException.getStartTime());
		Event eventException = exceptionOf(convertedEvent);
		Assertions.assertThat(eventException.getStartDate()).isEqualTo(earlyMidnight);
		Assertions.assertThat(eventException.getEndDate()).isEqualTo(lateMidnight);
	}

	@Test
	public void testConvertAttributeBusyStatusFree() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.FREE)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusBusy() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.BUSY)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusOutOfOffice() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.UNAVAILABLE)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}
	
	@Test
	public void testConvertAttributeBusyStatusTentative() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.TENTATIVE)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusNullGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setBusyStatus(CalendarBusyStatus.FREE);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}


	@Test
	public void testConvertAttributeBusyStatusNullAndParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setBusyStatus(CalendarBusyStatus.FREE);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusNullAndParentNullToo() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withBusyStatus(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setBusyStatus(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeCategoryEmpty() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(Collections.<String>emptyList())
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getCategory()).isNull();
	}

	@Test
	public void testConvertAttributeCategoryIsFirst() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(Lists.newArrayList("category1", "category2"))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getCategory()).isEqualTo("category1");
	}

	@Test
	public void testConvertAttributeCategoryNullGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setCategories(Lists.newArrayList("category1", "category2"));
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getCategory()).isEqualTo("category1");
	}
	
	@Test
	public void testConvertAttributeCategoryNullAndParentNullToo() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(null)
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setCategories(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getCategory()).isNull();
	}
	
	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeCategoryBeyondThreeHundred() throws ConversionException {
		String[] tooMuchCategories = new String[301];
		Arrays.fill(tooMuchCategories, "a category");
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-10-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withCategories(Arrays.asList(tooMuchCategories))
				.build();
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeReminder() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(150)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setReminder(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		int reminderInSecond = minuteToSecond(msEventException.getReminder());
		Assertions.assertThat(exception.getAlert()).isEqualTo(reminderInSecond);
	}

	@Test
	public void testConvertAttributeReminderZero() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(0)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setReminder(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getAlert()).isEqualTo(0);
	}

	@Test
	public void testConvertAttributeReminderNull() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setReminder(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getAlert()).isNull();
	}

	@Test
	public void testConvertAttributeReminderNullGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setReminder(180);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		int reminderInSecond = minuteToSecond(msEvent.getReminder());
		Assertions.assertThat(exception.getAlert()).isEqualTo(reminderInSecond);
	}

	@Test
	public void testConvertAttributeReminderNotNullDontGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withReminder(100)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		int reminderInSecond = minuteToSecond(msEventException.getReminder());
		Assertions.assertThat(exception.getAlert()).isEqualTo(reminderInSecond);
	}
	
	@Test
	public void testConvertAttributeSensitivityNormal() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(CalendarSensitivity.NORMAL)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}
	
	@Test
	public void testConvertAttributeSensitivityConfidential() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(CalendarSensitivity.CONFIDENTIAL)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityPersonal() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(CalendarSensitivity.PERSONAL)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityPrivate() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(CalendarSensitivity.PRIVATE)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityNullAndParentNullTooIsPublic() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSensitivity(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}

	@Test
	public void testConvertAttributeSensitivityNullGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withSensitivity(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setSensitivity(CalendarSensitivity.CONFIDENTIAL);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeLocation() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation("Any location")
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getLocation()).isEqualTo(msEventException.getLocation());
	}
	
	@Test
	public void testConvertAttributeLocationNullAndParentNullToo() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setLocation(null);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getLocation()).isNull();
	}
	
	@Test
	public void testConvertAttributeLocationNullGetFromParent() throws ConversionException {
		MSEventException msEventException = new MSEventExceptionBuilder()
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withExceptionStartTime(date("2004-10-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation(null)
				.build();
		
		MSEvent msEvent = makeMSEventWithException(msEventException);
		msEvent.setLocation("Any location");
		
		Event converted = convertToOBMEvent(msEvent);
		
		Event exception = exceptionOf(converted);
		Assertions.assertThat(exception.getLocation()).isEqualTo(msEvent.getLocation());
	}

	private MSEvent makeMSEventWithException(MSEventException exception) {
		MSEvent msEvent = new MSEventBuilder()
				.withDtStamp(date("2004-12-11T11:15:10Z"))
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(simpleDailyRecurrence(RecurrenceType.DAILY, RecurrenceDayOfWeek.FRIDAY))
				.withExceptions(Lists.newArrayList(exception))
				.build();

		return msEvent;
	}
	
	private MSRecurrence simpleDailyRecurrence(RecurrenceType type, RecurrenceDayOfWeek day) {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setInterval(1);
		recurrence.setType(type);
		recurrence.setDayOfWeek(EnumSet.of(day));
		return recurrence;
	}

	private Event exceptionOf(Event convertedEvent) {
		return Iterables.getOnlyElement(convertedEvent.getRecurrence().getEventExceptions());
	}

	private Event convertToOBMEvent(MSEvent msEvent) throws ConversionException {
		return convertToOBMEventWithEditingEvent(msEvent, null);
	}
	
	private Event convertToOBMEventWithEditingEvent(MSEvent msEvent, Event editingEvent) throws ConversionException {
		return converter.convert(user, editingEvent, msEvent, false);
	}
	
	private Date date(String date) {
		return org.obm.DateUtils.date(date);
	}

	private int getOneDayInSecond() {
		return (int) DateUtils.daysToSeconds(1);
	}

	private int getOneYearInSecond() {
		return (int) DateUtils.yearsToSeconds(1);
	}

	private int minuteToSecond(int minutes) {
		return (int) DateUtils.minutesToSeconds(minutes);
	}
}
