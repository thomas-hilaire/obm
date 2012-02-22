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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.User;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.IllegalMSEventStateException;
import org.obm.push.utils.DateUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventMeetingStatus;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class MSEventToObmEventConverterTest {

	private MSEventToObmEventConverterImpl converter;

	private User user;
	
	@Before
	public void setUp() {
		converter = new MSEventToObmEventConverterImpl();
		String mailbox = "user@domain";
	    user = User.Factory.create()
				.createUser(mailbox, mailbox, null);
	}

	@Ignore("We should find in OBMEvent the corresponding UID of MSEvent")
	@Test
	public void testConvertAttributeUID() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withUid(new MSEventUid("{81412D3C-2A24-4E9D-B20E-11F7BBE92799}"))
				.build();
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeAllDayFalse() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(false)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayTrue() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeAllDayFalseNeedStartTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(false)
				.build();

		convertToOBMEvent(msEvent);
	}
	
	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeAllDayNullNeedStartTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(null)
				.build();

		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeBusyStatusFree() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(CalendarBusyStatus.FREE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusBusy() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(CalendarBusyStatus.BUSY)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusOutOfOffice() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.UNAVAILABLE)
				.withSubject("Any Subject")
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}
	
	@Test
	public void testConvertAttributeBusyStatusTentative() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(CalendarBusyStatus.TENTATIVE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeCategoryEmpty() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(new ArrayList<String>())
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getCategory()).isNull();
	}

	@Test
	public void testConvertAttributeCategoryIsFirst() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(Lists.newArrayList("category1", "category2"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getCategory()).isEqualTo("category1");
	}

	@Test
	public void testConvertAttributeCategoryNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getCategory()).isNull();
	}
	
	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeCategoryBeyondThreeHundred() throws ConversionException {
		String[] tooMuchCategories = new String[301];
		Arrays.fill(tooMuchCategories, "a category");
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(Arrays.asList(tooMuchCategories))
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeDtStampJustCreated() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(date("2004-12-10T11:15:10Z"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimeCreate()).isEqualTo(msEvent.getDtStamp());
		Assertions.assertThat(convertedEvent.getTimeUpdate()).isEqualTo(msEvent.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampAlreadyCreated() throws ConversionException {
		Date previousDtStampDate = date("2004-12-11T11:15:10Z");
		Event editingEvent = new Event();
		editingEvent.setTimeCreate(previousDtStampDate);
		editingEvent.setTimeUpdate(previousDtStampDate);
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(date("2004-12-10T12:15:10Z"))
				.build();
		
		Event convertedEvent = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		Assertions.assertThat(convertedEvent.getTimeCreate()).isEqualTo(previousDtStampDate);
		Assertions.assertThat(convertedEvent.getTimeUpdate()).isEqualTo(msEvent.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getTimeCreate()).isNull();
		Assertions.assertThat(convertedEvent.getTimeUpdate()).isNull();
	}

	@Test
	public void testConvertAttributeDescription() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDescription("any description")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getDescription()).isEqualTo(msEvent.getDescription());
	}
	
	@Test
	public void testConvertAttributeDescriptionNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDescription(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getDescription()).isNull();
	}

	@Test
	public void testConvertAttributeTimezoneSpecific() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withTimeZone(TimeZone.getTimeZone("America/Tijuana"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimezoneName()).isEqualTo("America/Tijuana");
	}
	
	@Test
	public void testConvertAttributeTimezoneNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withTimeZone(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimezoneName()).isNull();
	}
	
	@Test
	public void testConvertAttributeLocation() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation("Any location")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getLocation()).isEqualTo(msEvent.getLocation());
	}
	
	@Test
	public void testConvertAttributeLocationNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getLocation()).isNull();
	}

	@Test
	public void testConvertAttributeSubject() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTitle()).isEqualTo(msEvent.getSubject());
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeSubjectEmpty() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeSubjectNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject(null)
				.build();
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeReminder() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withReminder(150)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		int reminderInSecond = minuteToSecond(msEvent.getReminder());
		Assertions.assertThat(convertedEvent.getAlert()).isEqualTo(reminderInSecond);
	}

	@Test
	public void testConvertAttributeReminderZero() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withReminder(0)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getAlert()).isEqualTo(0);
	}

	@Test
	public void testConvertAttributeReminderNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withReminder(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getAlert()).isNull();
	}

	@Test
	public void testConvertAttributeSensitivityNormal() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.NORMAL)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}
	
	@Test
	public void testConvertAttributeSensitivityConfidential() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.CONFIDENTIAL)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityPersonal() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.PERSONAL)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityPrivate() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.PRIVATE)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}
	
	@Test
	public void testConvertAttributeOrganizerNameOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerName("Any Name")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isEqualTo(msEvent.getOrganizerName());
	}

	@Test
	public void testConvertAttributeOrganizerNameOnlyNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerName(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isNull();
	}

	@Test
	public void testConvertAttributeOrganizerEmailOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerEmail("email@domain")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isNull();
		Assertions.assertThat(convertedEventOrganizer.getEmail()).isEqualTo(msEvent.getOrganizerEmail());
	}

	@Test
	public void testConvertAttributeOrganizerEmailOnlyNullGetFromSession() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerEmail(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isNull();
		Assertions.assertThat(convertedEventOrganizer.getEmail()).isEqualTo(user.getEmail());
	}
	
	@Test
	public void testConvertAttributeOrganizerNameAndEmail() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerName("Any Name")
				.withOrganizerEmail("user@domain")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isEqualTo(msEvent.getOrganizerName());
		Assertions.assertThat(convertedEventOrganizer.getEmail()).isEqualTo(msEvent.getOrganizerEmail());
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeStartTimeOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeStartTimeNullOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeNullOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withEndTime(null)
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeNullAndStartTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(null)
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeStartAndEndTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		Event converted = convertToOBMEvent(msEvent);

		Assertions.assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		Assertions.assertThat(converted.getEndDate()).isEqualTo(msEvent.getEndTime());
	}

	@Test
	public void testConvertAttributeMeetingStatusIsInMeeting() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusIsNotInMeeting() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.build();
	
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_NOT_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceledAndReceived() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED)
				.build();
	
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingReceived() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceled() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED);
	}

	@Test
	public void testConvertAttributeMeetingStatusNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(null)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isNull();
	}

	@Test
	public void testCalculatedAttributeDurationByStartAndEndTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		Event converted = convertToOBMEvent(msEvent);

		Assertions.assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		Assertions.assertThat(converted.getEndDate()).isEqualTo(msEvent.getEndTime());
		Assertions.assertThat(converted.getDuration()).isEqualTo(getOneYearInSecond());
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Date midnightOfDay = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEvent.getStartTime());
		Assertions.assertThat(converted.getStartDate()).isEqualTo(midnightOfDay);
		Assertions.assertThat(converted.getDuration()).isEqualTo(Ints.checkedCast(getOneDayInSecond()));
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayWhenHasEndTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T12:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Date midnightOfDay = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEvent.getStartTime());
		Assertions.assertThat(converted.getStartDate()).isEqualTo(midnightOfDay);
		Assertions.assertThat(converted.getDuration()).isEqualTo(Ints.checkedCast(getOneDayInSecond()));
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
