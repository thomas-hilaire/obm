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
package org.obm.sync.calendar;

import static org.obm.DateUtils.date;
import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class EventTest {

	@Test
	public void testIsEventInThePast1970() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.set(1970, 0, 0);
		event.setStartDate(calendar.getTime());
		Assert.assertTrue(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneDayLess() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		event.setStartDate(calendar.getTime());
		Assert.assertTrue(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneHourLess() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -1);
		event.setStartDate(calendar.getTime());
		Assert.assertTrue(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneSecondLess() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, -1);
		event.setStartDate(calendar.getTime());
		Assert.assertTrue(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneMinuteMore() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 1);
		event.setStartDate(calendar.getTime());
		Assert.assertFalse(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneYearMore() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		event.setStartDate(calendar.getTime());
		Assert.assertFalse(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastStartInThePastEndInFuture() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -10);
		event.setStartDate(calendar.getTime());
		event.setDuration(3600);
		Assert.assertFalse(event.isEventInThePast());
	}
	
	@Test
	public void testIsEventInThePastOneOccurrenceInFuture() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		event.setStartDate(calendar.getTime());
		event.setDuration(3600);
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		calendar.add(Calendar.MONTH, 1);
		recurrence.setEnd(calendar.getTime());
		event.setRecurrence(recurrence);
		Assert.assertFalse(event.isEventInThePast());
	}
	
	@Test
	public void testEventModifiedNullTimestamp() {
		Event event = new Event();
		event.setTimeCreate(new Date());
		boolean modified = event.modifiedSince(null);
		Assert.assertEquals(true, modified);
	}
	
	@Test
	public void testEventModifiedZeroTimestamp() {
		Event event = new Event();
		event.setTimeCreate(new Date());
		boolean modified = event.modifiedSince(new Date(0));
		Assert.assertEquals(true, modified);
	}
	
	@Test
	public void testEventModifiedTimestampBeforeCreateTimestamp() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		event.setTimeCreate(calendar.getTime());
		calendar.add(Calendar.MONTH, -1);	
		boolean modified = event.modifiedSince(calendar.getTime());
		Assert.assertEquals(true, modified);
	}
	
	@Test
	public void testEventModifiedTimestampAfterCreateTimestampBeforeUpdateTimestamp() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		event.setTimeCreate(calendar.getTime());
		calendar.add(Calendar.MONTH, 2);
		event.setTimeUpdate(calendar.getTime());
		calendar.add(Calendar.MONTH, -1);
		boolean modified = event.modifiedSince(calendar.getTime());
		Assert.assertEquals(true, modified);
	}
	
	@Test
	public void testEventModifiedTimestampAfterCreateTimestampAfterUpdateTimestamp() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		event.setTimeCreate(calendar.getTime());
		calendar.add(Calendar.MONTH, 1);
		event.setTimeUpdate(calendar.getTime());
		calendar.add(Calendar.MONTH, 1);
		boolean modified = event.modifiedSince(calendar.getTime());
		Assert.assertEquals(false, modified);
	}
	
	@Test
	public void testEventModifiedTimestampAfterCreateTimestamp() {
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		event.setTimeCreate(calendar.getTime());
		calendar.add(Calendar.MONTH, 1);	
		boolean modified = event.modifiedSince(calendar.getTime());
		Assert.assertEquals(false, modified);
	}
	
	@Test
	public void testRecurrentEventHasAModifiedException(){
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		Date dateStart = calendar.getTime();
		Calendar calendarUpdate = calendar;
		calendarUpdate.add(Calendar.HOUR, 1);
		event.setTimeCreate(dateStart);
		event.setStartDate(dateStart);
		
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		
		List<Event> eventExceptions = new LinkedList<Event>();
		Event exception = event.clone();
		exception.setTimeCreate(dateStart);
		exception.setTimeUpdate(calendarUpdate.getTime());
		eventExceptions.add(exception);
		
		recurrence.setEventExceptions(eventExceptions);		
		event.setRecurrence(recurrence);
		
		Assert.assertEquals(true, event.exceptionModifiedSince(dateStart));
	}
	
	@Test
	public void testRecurrentEventHasNotAnyModifiedException(){
		Event event = new Event();
		Calendar calendar = Calendar.getInstance();
		Date dateStart = calendar.getTime();
		Calendar calendarUpdate = calendar;
		calendarUpdate.add(Calendar.HOUR, 1);
		event.setTimeCreate(dateStart);
		event.setStartDate(dateStart);
		
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		event.setRecurrence(recurrence);
		
		Assert.assertEquals(false, event.exceptionModifiedSince(dateStart));
	}
	
	@Test
	public void testEventClone() {
		Event newEvent = createOneEvent(5);
		Event clone = newEvent.clone();
		Assert.assertEquals(clone, newEvent);
	}
	
	@Test
	public void testModifiedEventClone() {
		Event newEvent = createOneEvent(5);
		Event clone = newEvent.clone();
		clone.setDescription("event updated");
		Assert.assertNotSame(clone, newEvent);
	}
	
	@Test
	public void testEventRecurrenceClone() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createRecurrence(newEvent);
		newEvent.setRecurrence(recurrence);
		
		Event eventCloned = newEvent.clone();
		Assert.assertEquals(newEvent, eventCloned);
	}
	
	@Test
	public void testModifiedEventRecurrenceClone() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createRecurrence(newEvent);
		recurrence.setFrequence(10);
		newEvent.setRecurrence(recurrence);
		
		Event eventCloned = newEvent.clone();
		EventRecurrence recurrenceCloned = eventCloned.getRecurrence();
		recurrenceCloned.setFrequence(25);
		
		Assert.assertNotSame(newEvent, eventCloned);
	}
	
	@Test
	public void testHasNotImportantChangesEvent() {
		Event newEvent = createOneEvent(5);
		Event updateEvent = newEvent.clone();
		Assert.assertFalse(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasImportantChangesEventRecurrence() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createRecurrence(newEvent);
		newEvent.setRecurrence(recurrence);
	
		Event updateEvent = newEvent.clone();
		updateEvent.setDescription("Date updated");
		Calendar calendarUpdate = Calendar.getInstance();
		calendarUpdate.add(Calendar.HOUR, 4);
		updateEvent.setStartDate(calendarUpdate.getTime());
		
		Assert.assertTrue(updateEvent.hasImportantChanges(newEvent));
	}

	@Test
	public void testHasNotImportantChangesEventRecurrence() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createRecurrence(newEvent);
		newEvent.setRecurrence(recurrence);
	
		Event updateEvent = newEvent.clone();
		updateEvent.setDescription("there are not important changes");
		Assert.assertFalse(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasNotImportantChangesEventAdddingAttendees() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createRecurrence(newEvent);
		newEvent.setRecurrence(recurrence);
	
		Event updateEvent = newEvent.clone();
		updateEvent.setAttendees(createAttendees(8));
		
		Assert.assertFalse(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasImportantChangesEventWithAddingAttendeesAndUpdatedLocation() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createRecurrence(newEvent);
		newEvent.setRecurrence(recurrence);
	
		Event updateEvent = newEvent.clone();
		updateEvent.setAttendees(createAttendees(6));
		updateEvent.setLocation("to Paris");
		
		Assert.assertTrue(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasImportantChangesEventWithUpdatedRecurrence() {
		Event newEvent = createOneEvent(5);
		EventRecurrence recurrence = createRecurrence(newEvent);
		newEvent.setRecurrence(recurrence);
	
		Event updateEvent = newEvent.clone();
		EventRecurrence updateRecurrence = updateEvent.getRecurrence();
		
		Calendar calendarUpdate = Calendar.getInstance();
		calendarUpdate.add(Calendar.HOUR, 4);
		updateRecurrence.setEnd(calendarUpdate.getTime());
		
		Assert.assertTrue(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasImportantChangesEventWithUpdatedOneEventOfRecurrence() {
		Event newEvent = createOneEvent(5);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 10);
		Event one = newEvent.clone();
		one.setStartDate(cal.getTime());
		
		cal.add(Calendar.HOUR, 2);
		Event two = newEvent.clone();
		two.setStartDate(cal.getTime());
		
		EventRecurrence recurrence = createRecurrence(newEvent);
		newEvent.setRecurrence(recurrence);
		recurrence.getEventExceptions().add(one);
		recurrence.getEventExceptions().add(two);
		
		Event updateEvent = newEvent.clone();
		EventRecurrence updateRecurrence = updateEvent.getRecurrence();
		
		Calendar calendarUpdate = Calendar.getInstance();
		calendarUpdate.add(Calendar.HOUR, 4);
		updateRecurrence.getEventExceptions().get(0).setStartDate(calendarUpdate.getTime());
		
		Assert.assertTrue(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasNotImportantChangesEventWithNullImportantParameter() {
		Event newEvent = createOneEvent(5);
		newEvent.setLocation(null);
		Event updateEvent = newEvent.clone();
		Assert.assertFalse(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasNotImportantChangesEventWithEmptyImportantChange() {
		Event newEvent = createOneEvent(5);
		newEvent.setLocation("");
		Event updateEvent = newEvent.clone();
		Assert.assertFalse(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasImportantChangesEventWithNullImportantChangeToEventCloned() {
		Event newEvent = createOneEvent(5);
		Event updateEvent = newEvent.clone();
		updateEvent.setLocation(null);
		Assert.assertTrue(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasImportantChangesEventWithEmptyImportantChangeToEventCloned() {
		Event newEvent = createOneEvent(5);
		Event updateEvent = newEvent.clone();
		updateEvent.setLocation("");
		Assert.assertTrue(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasNotImportantChangesEventCompareEmptyAndNullImportantChange() {
		Event newEvent = createOneEvent(5);
		newEvent.setLocation(null);
		Event updateEvent = newEvent.clone();
		updateEvent.setLocation("");
		Assert.assertFalse(updateEvent.hasImportantChanges(newEvent));
	}
	
	@Test
	public void testHasChangesOnAllEventAttributesExceptedExceptionWithoutChanges() {
		Event ev1 = createOneEvent(3);
		Event ev2 = ev1.clone();
		
		boolean change = ev1.hasChangesOnEventAttributesExceptedEventException(ev2);
		
		Assert.assertFalse(change);
	}
	
	@Test
	public void testHasChangesOnAllEventAttributesExceptedExceptionWithChanges() {
		Event ev1 = createOneEvent(3);
		Event ev2 = ev1.clone();
		
		ev2.setTitle("Change: "+ev2.getTitle());
		
		boolean change = ev1.hasChangesOnEventAttributesExceptedEventException(ev2);
		
		Assert.assertTrue(change);
	}
	
	@Test
	public void testHasImportantChangesExceptedEventExceptionWithoutChanges() {
		Event ev1 = createOneEvent(3);
		Event ev2 = ev1.clone();
		
		EventRecurrence recurrence = createRecurrence(ev1);
		ev2.setRecurrence(recurrence);
		
		boolean change = ev1.hasImportantChangesExceptedEventException(ev2);
		
		Assert.assertFalse(change);
	}
	
	@Test
	public void testHasImportantChangesExceptedEventExceptionWithChanges() {
		Event ev1 = createOneEvent(3);
		Event ev2 = ev1.clone();
		
		ev2.setLocation("a new location");
		
		boolean change = ev1.hasImportantChangesExceptedEventException(ev2);
		
		Assert.assertTrue(change);
	}
	
	@Test
	public void getEventInstanceWithRecurrenceIdWithoutExistedException(){
		Event ev1 = createOneEvent(3);
		Calendar cal = Calendar.getInstance();
		cal.setTime(ev1.getStartDate());
		cal.set(2012, 12, 12);
		Date recurrenceId = cal.getTime();
		
		
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setFrequence(1);
		ev1.setRecurrence(recurrence);
		
		Event instance = ev1.getEventInstanceWithRecurrenceId(recurrenceId);
		
		Assert.assertNotNull(instance);
		Assert.assertEquals(recurrenceId, instance.getStartDate());
		Assert.assertEquals(recurrenceId, instance.getRecurrenceId());
		Assert.assertEquals(RecurrenceKind.none, instance.getRecurrence().getKind());
	}
	
	@Test
	public void getEventInstanceWithRecurrenceIdWithException(){
		Calendar cal = Calendar.getInstance();
		cal.set(2012, 12, 12);
		Date recurrenceId = cal.getTime();
		
		Event ev1 = createOneEvent(3);
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setFrequence(1);
		Event exception = createOneEvent(2);
		exception.setRecurrenceId(recurrenceId);
		recurrence.addEventException(exception);		
		ev1.setRecurrence(recurrence);

		Event instance = ev1.getEventInstanceWithRecurrenceId(recurrenceId);
		
		Assert.assertNotNull(instance);
		Assert.assertEquals(exception, instance);
	}

	@Test
	public void testIsNotRecurrentForNullEventRecurrence(){
		Event ev1 = createOneEvent(1);
		Assert.assertFalse(ev1.isRecurrent());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAlertNegativeInteger() {
		Event event = new Event();
		event.setAlert(-10);
	}
	
	@Test
	public void testAlertNullInteger() {
		Event event = new Event();
		event.setAlert(null);
		assertThat(event.getAlert()).isNull();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNegativeDuration() {
		Event event = new Event();
		event.setDuration(-10);
	}
	
	@Test
	public void testDurationThenAllDay() {
		Event event = new Event();
		event.setDuration(10);
		event.setAllday(true);
		int secondsInADay = 3600*24;
		assertThat(event.getDuration()).isEqualTo(secondsInADay);
		assertThat(event.isAllday()).isEqualTo(true);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testAllDayThenDuration() {
		Event event = new Event();
		event.setAllday(true);
		event.setDuration(10);
	}
	
	@Test
	public void testDurationThenAllDayThenNotAllDay() {
		Event event = new Event();
		event.setDuration(10);
		event.setAllday(true);
		event.setAllday(false);
		assertThat(event.getDuration()).isEqualTo(10);
		assertThat(event.isAllday()).isEqualTo(false);
	}
	
	@Test
	public void testEndDateAllDay() {
		Event event = new Event();
		event.setAllday(true);
		event.setStartDate(date("2004-12-11T11:15:10"));
		assertThat(event.getEndDate()).isEqualTo(date("2004-12-12T11:15:10"));
	}
	
	@Test
	public void testEndDateNullStartDateAndAllDay() {
		Event event = new Event();
		event.setAllday(true);
		assertThat(event.getEndDate()).isNull();
	}
	
	@Test
	public void testEndDateNullStartDate() {
		Event event = new Event();
		event.setDuration(10);
		assertThat(event.getEndDate()).isNull();
	}
	
	@Test
	public void testEndDate() {
		Event event = new Event();
		event.setDuration(120);
		event.setStartDate(date("2004-12-11T11:15:10Z"));
		assertThat(event.getEndDate()).isEqualTo(date("2004-12-11T11:17:10Z"));
	}
	
	@Test
	public void testEndDateUndefinedDuration() {
		Event event = new Event();
		event.setStartDate(date("2004-12-11T11:15:10Z"));
		assertThat(event.getEndDate()).isEqualTo(date("2004-12-11T11:15:10Z"));
	}
	
	private Event createOneEvent(int nbAttendees) {
		Event event = new Event();
		event.setAlert(10);
		event.setAllday(true);
		event.setAttendees( createAttendees(nbAttendees) );
		event.setCategory("category");
		event.setStartDate(new Date());
		event.setDescription("description");
		event.setExtId(new EventExtId("1"));
		event.setInternalEvent(true);
		event.setLocation("location");
		event.setOpacity(EventOpacity.OPAQUE);
		event.setOwner("owner");
		event.setOwnerDisplayName("owner displayname");
		event.setOwnerEmail("owner email");
		event.setPriority(1);
		event.setPrivacy(EventPrivacy.PRIVATE);
		event.setSequence(0);
		event.setRecurrenceId(new Date());
		event.setTimeCreate(new Date());
		event.setTimeUpdate(new Date());
		event.setTimezoneName("timeZone");
		event.setTitle("title");
		event.setType(EventType.VEVENT);
		event.setUid(new EventObmId(1));
		return event;
	}
	
	private List<Attendee> createAttendees(int count) {
		List<Attendee> attendees = new ArrayList<Attendee>();
		for (int i = 0; i < count; i++) {
			Attendee attendee = new Attendee();
			attendee.setCanWriteOnCalendar(false);
			attendee.setDisplayName("DisplayName" + i);
			attendee.setEmail("email" + i + "@email.com");
			attendee.setObmUser(true);
			attendee.setOrganizer(isOrganizer(i));
			attendee.setPercent(1);
			attendee.setParticipationRole(ParticipationRole.REQ);
			attendee.setState(ParticipationState.NEEDSACTION);
		}
		return attendees;
	}
	
	private boolean isOrganizer(int id) {
		if (id == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	private EventRecurrence createRecurrence(Event event) {
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setDays("1100111");
		recurrence.setEnd(new Date());
		recurrence.setFrequence(1);
		List<Event> events = new LinkedList<Event>();
		events.add(event.clone());
		recurrence.setEventExceptions(events);
		return recurrence;
	}
	
}
