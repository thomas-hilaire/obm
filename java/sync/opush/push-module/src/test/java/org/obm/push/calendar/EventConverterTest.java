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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.push.EventConverter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.User;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.User.Factory;
import org.obm.push.protocol.data.CalendarDecoder;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import fr.aliacom.obm.common.domain.ObmDomain;

public class EventConverterTest {

	private EventConverter eventConverter;
	private CalendarDecoder decoder;
	
	@Before
	public void init() {
		this.eventConverter = new EventConverter();
		this.decoder = new CalendarDecoder();
	}

	@Test
	public void testAttendeesWithNoOrganizerInNewEventStream() throws SAXException, IOException, FactoryConfigurationError {
		String loginAtDomain = "jribiera@obm.lng.org";
		BackendSession backendSession = buildBackendSession(loginAtDomain);
		
		IApplicationData data = getApplicationData("HTC-Windows-Mobile-6.1-new_event.xml");
		Event event = eventConverter.convert(backendSession, null, (MSEvent) data, true);
		
		Attendee organizer = event.findOrganizer();
		List<Attendee> attendees = listAttendeesWithoutOrganizer(organizer, event);  
		
		assertNotNull(event);
		assertEquals("Windows Mobile 6.1 - HTC", event.getTitle());
		
		checkOrganizer(backendSession.getCredentials().getUser().getEmail(), organizer);
		
		assertThat(event.getAttendees()).hasSize(4);
		assertThat(attendees).hasSize(3).excludes(organizer);
		checkAttendeeParticipationState(attendees);
	}
	
	@Test
	public void testAttendeesWithOrganizerEmailInNewEventStream() throws SAXException, IOException, FactoryConfigurationError {
		String loginAtDomain = "jribier@obm.lng.org";
		BackendSession backendSession = buildBackendSession(loginAtDomain);
		
		IApplicationData data = getApplicationData("Galaxy-S-Android-2.3.4-new_event.xml");
		Event event = eventConverter.convert(backendSession, null, (MSEvent) data, true);

		Attendee organizer = event.findOrganizer();
		List<Attendee> attendees = listAttendeesWithoutOrganizer(organizer, event); 
		
		Assert.assertNotNull(event);
		Assert.assertEquals("Android 2.3.4 - Galaxy S", event.getTitle());

		checkOrganizer("jribiera@obm.lng.org", organizer);
		
		assertThat(event.getAttendees()).hasSize(4);
		assertThat(attendees).hasSize(3).excludes(organizer);
		checkAttendeeParticipationState(attendees);
	}

	@Ignore("FIXME for OBMFULL-2728")
	@Test
	public void testConvertUpdateOneOnlyExceptionEvent() throws SAXException, IOException, FactoryConfigurationError {
		String UID = "cfe4645e-4168-102f-be5e-0015176f7922";
		IApplicationData  oldData = getApplicationData("samecase/new-event-with-exception.xml");
		Event oldEvent = eventConverter.convert(buildBackendSession("jribiera@obm.lng.org"), null, (MSEvent) oldData, true);
		
		IApplicationData  data = getApplicationData("samecase/update-one-exception-of-same-event.xml");

		Event event = eventConverter.convert(buildBackendSession("jribiera@obm.lng.org"), oldEvent, (MSEvent) data, true);
		Event excptEvtUpd = event.getRecurrence().getEventExceptions().get(0);

		
		Assertions.assertThat(event.getExtId())
		.isNotNull()
		.isInstanceOf(EventExtId.class)
		.isEqualTo(new EventExtId(UID));
		
		Assertions.assertThat(excptEvtUpd.getExtId())
		.isNotNull()
		.isInstanceOf(EventExtId.class)
		.isEqualTo(new EventExtId(UID));
	}
	
	@Test
	public void testOwnerloginIsNotEqualsToOwnerEmail() throws SAXException, IOException, FactoryConfigurationError {
		String loginAtDomain = "LOGIN@obm.lng.org";
		String email = "EMAIL@obm.lng.org";
		String displayName = "displayName";

		Credentials credentials = new Credentials( 
				Factory.create().createUser(loginAtDomain, email, displayName), "password", new ObmDomain());
		BackendSession backendSession = buildBackendSession(credentials);
		
		IApplicationData data = getApplicationData("OBMFULL-2907.xml");
		Event event = eventConverter.convert(backendSession, null, (MSEvent) data, true);
		
		Attendee organizer = event.findOrganizer();
		
		Assertions.assertThat(loginAtDomain).isNotEqualTo(organizer.getEmail());
		Assertions.assertThat(email).isEqualTo(organizer.getEmail());
		
		Assertions.assertThat(loginAtDomain).isNotEqualTo(event.getOwnerEmail());
		Assertions.assertThat(email).isEqualTo(event.getOwnerEmail());
	}
	
	private List<Attendee> listAttendeesWithoutOrganizer(Attendee organizer, Event event) {
		List<Attendee> attendees = Lists.newArrayList(event.getAttendees());
		attendees.remove(organizer);
		return attendees;
	}
	
	private void checkOrganizer(String email, Attendee organizer) {
		Assert.assertNotNull(organizer);
		Assert.assertEquals(email, organizer.getEmail());
		Assert.assertNull(organizer.getDisplayName());
		Assert.assertEquals(ParticipationState.ACCEPTED, organizer.getState());
		Assert.assertEquals(ParticipationRole.REQ, organizer.getParticipationRole());
		Assert.assertTrue(organizer.isOrganizer());
	}

	private void checkAttendeeParticipationState(List<Attendee> attendeesListWithoutOrganizer) {
		for (Attendee attendee: attendeesListWithoutOrganizer) {
			Assert.assertEquals(ParticipationState.NEEDSACTION, attendee.getState());
			Assert.assertFalse(attendee.isOrganizer());
		}
	}
	
	private IApplicationData getApplicationData(String filename) throws SAXException, IOException, FactoryConfigurationError {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("xml/" + filename); 
		Document document = DOMUtils.parse(inputStream);
		return decoder.decode(document.getDocumentElement()); 
	}
	
	private BackendSession buildBackendSession(String userId) {
		User user = Factory.create().createUser(userId, "email@domain", "displayName");
		return buildBackendSession(new Credentials(user, "test", new ObmDomain()));
	}
	
	private BackendSession buildBackendSession(Credentials credentials) {
		BackendSession bs = new BackendSession(credentials,
				"Sync", new Device(1, "devType", "devId", new Properties()), new BigDecimal("12.5"));
		return bs;
	}
	
}
