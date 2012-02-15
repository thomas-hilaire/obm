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

import java.util.List;

import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventCommon;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.tnefconverter.RTFUtils;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

public class CalendarDecoder extends Decoder implements IDataDecoder {

	@Override
	public IApplicationData decode(Element syncData) {
		Element containerNode;
		MSEvent calendar = new MSEvent();

		// Main attributes
		calendar.setOrganizerName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "OrganizerName")));
		calendar.setOrganizerEmail(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "OrganizerEmail")));
		calendar.setUid(
				new MSEventUid(parseDOMString(DOMUtils.getUniqueElement(syncData, "UID"))));
		
		calendar.setTimeZone(parseDOMTimeZone(DOMUtils.getUniqueElement(
				syncData, "TimeZone")));

		setEventCalendar(calendar, syncData);

		// Components attributes

		// Attendees

		containerNode = DOMUtils.getUniqueElement(syncData, "Attendees");
		if (containerNode != null) {
			// <Attendee>
			// <AttendeeName>noIn TheDatabase</AttendeeName>
			// <AttendeeEmail>notin@mydb.com</AttendeeEmail>
			// <AttendeeType>1</AttendeeType>
			// </Attendee>
			// <Attendee>
			// <AttendeeName>Fff Tt</AttendeeName>
			// <AttendeeEmail>ff@tt.com</AttendeeEmail>
			// <AttendeeType>2</AttendeeType>
			// </Attendee>

			NodeList attendeesNodeList = containerNode.getElementsByTagName("Attendee");
			if (attendeesNodeList != null) {
				
				for (int i = 0, n = attendeesNodeList.getLength(); i < n; i += 1) {
					
					MSAttendee attendee = new MSAttendee();
					Element attendeeEl = (Element) attendeesNodeList.item(i);

					String email = parseDOMString(DOMUtils.getUniqueElement(
							attendeeEl, "Email"));
					if (email == null || "".equals(email)) {
						email = parseDOMString(DOMUtils.getUniqueElement(attendeeEl,
								"AttendeeEmail"));
					}
					attendee.setEmail(email);

					String name = parseDOMString(DOMUtils.getUniqueElement(attendeeEl,
							"Name"));
					if (name == null || "".equals(name)) {
						name = parseDOMString(DOMUtils.getUniqueElement(attendeeEl,
								"AttendeeName"));
					}
					attendee.setName(name);

					switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(attendeeEl,
							"AttendeeStatus"))) {
					case 0:
						attendee.setAttendeeStatus(AttendeeStatus.RESPONSE_UNKNOWN);
						break;
					case 2:
						attendee.setAttendeeStatus(AttendeeStatus.TENTATIVE);
						break;
					case 3:
						attendee.setAttendeeStatus(AttendeeStatus.ACCEPT);
						break;
					case 4:
						attendee.setAttendeeStatus(AttendeeStatus.DECLINE);
						break;
					case 5:
						attendee.setAttendeeStatus(AttendeeStatus.NOT_RESPONDED);
						break;
					}

					if (attendee.getAttendeeStatus() != null) {
						logger.info("parse attendeeStatus: "
								+ attendee.getAttendeeStatus());
					}

					switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(attendeeEl,
							"AttendeeType"))) {
					case 1:
						attendee.setAttendeeType(AttendeeType.REQUIRED);
						break;
					case 2:
						attendee.setAttendeeType(AttendeeType.OPTIONAL);
						break;
					case 3:
						attendee.setAttendeeType(AttendeeType.RESOURCE);
						break;
					}

					if (attendee.getAttendeeType() != null) {
						logger.info("parse attendeeType: "
								+ attendee.getAttendeeType());
					}
					calendar.addAttendee(attendee);
				}
			}	
		}
			
		// Exceptions
		containerNode = DOMUtils.getUniqueElement(syncData, "Exceptions");
		if (containerNode != null) {
		
			NodeList exceptionsNodeList = containerNode.getElementsByTagName("Exception");
			if (exceptionsNodeList != null) {

				int exceptionsListLength = exceptionsNodeList.getLength();
				final List<MSEventException> exceptions = Lists.newArrayListWithExpectedSize(exceptionsListLength);
				for (int i = 0; i < exceptionsListLength; i += 1) {
					Element subnode = (Element) exceptionsNodeList.item(i);
					MSEventException exception = buildEventException(subnode);
					exceptions.add(exception);
				}
				
				calendar.setExceptions(exceptions);
			}
		}

		// Recurrence
		containerNode = DOMUtils.getUniqueElement(syncData, "Recurrence");
		if (containerNode != null) {
			logger.info("decode recurrence");
			MSRecurrence recurrence = new MSRecurrence();

			recurrence.setUntil(parseDOMDate(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceUntil")));
			recurrence.setWeekOfMonth(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceWeekOfMonth")));
			recurrence.setMonthOfYear(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceMonthOfYear")));
			recurrence.setDayOfMonth(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceDayOfMonth")));
			recurrence.setOccurrences(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceOccurrences")));
			recurrence.setInterval(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceInterval")));
			Integer i = parseDOMInt(DOMUtils.getUniqueElement(containerNode,
					"RecurrenceDayOfWeek"));
			if (i != null) {
				recurrence.setDayOfWeek(RecurrenceDayOfWeek.fromInt(i));
			}

			switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(containerNode,
					"RecurrenceType"))) {
			case 0:
				recurrence.setType(RecurrenceType.DAILY);
				break;
			case 1:
				recurrence.setType(RecurrenceType.WEEKLY);
				break;
			case 2:
				recurrence.setType(RecurrenceType.MONTHLY);
				break;
			case 3:
				recurrence.setType(RecurrenceType.MONTHLY_NDAY);
				break;
			case 5:
				recurrence.setType(RecurrenceType.YEARLY);
				break;
			case 6:
				recurrence.setType(RecurrenceType.YEARLY_NDAY);
				break;
			}

			if (recurrence.getType() != null) {
				logger.info("parse type: " + recurrence.getType());
			}

			calendar.setRecurrence(recurrence);
		}

		return calendar;
	}

	private MSEventException buildEventException(Element dom) {
		MSEventException exception = new MSEventException();
		fillEventCommon(exception, dom);
		exception.setDeleted(parseDOMInt2Boolean(DOMUtils.getUniqueElement(dom, "ExceptionIsDeleted")));
		exception.setExceptionStartTime(parseDOMDate(DOMUtils.getUniqueElement(dom, "ExceptionStartTime")));
		return exception;
	}
	
	private void fillEventCommon(MSEventCommon common, Element dom) {
		common.setLocation(parseDOMString(DOMUtils.getUniqueElement(dom, "Location")));
		Element body = DOMUtils.getUniqueElement(dom, "Body");
		common.setDescription(decodeBody(body));
		common.setDtStamp(parseDOMDate(DOMUtils.getUniqueElement(dom, "DTStamp")));
		common.setSubject(parseDOMString(DOMUtils.getUniqueElement(dom, "Subject")));
		common.setEndTime(parseDOMDate(DOMUtils.getUniqueElement(dom, "EndTime")));
		common.setStartTime(parseDOMDate(DOMUtils.getUniqueElement(dom, "StartTime")));
		common.setAllDayEvent(parseDOMInt2Boolean(DOMUtils.getUniqueElement(dom, "AllDayEvent")));
		common.setReminder(parseDOMInt(DOMUtils.getUniqueElement(dom, "ReminderMinsBefore")));
		common.setCategories(parseDOMStringCollection(DOMUtils.getUniqueElement(dom, "Categories"), "Category"));
		common.setBusyStatus(getCalendarBusyStatus(dom));
		common.setSensitivity(getCalendarSensitivity(dom));
		common.setMeetingStatus(getMeetingStatus(dom));
	}
	
	private void setEventCalendar(MSEvent event, Element dom) {
		fillEventCommon(event, dom);
	}

	private String decodeBody(Element body) {
		if (body != null) {
			Element data = DOMUtils.getUniqueElement(body, "Data");
			if (data != null) {
				Type bodyType = Type.fromInt(Integer.parseInt(DOMUtils
						.getUniqueElement(body, "Type").getTextContent()));
				String txt = data.getTextContent();
				if (bodyType == Type.PLAIN_TEXT) {
					return data.getTextContent();
				} else if (bodyType == Type.RTF) {
					return RTFUtils.extractB64CompressedRTF(txt);
				} else {
					logger.warn("Unsupported body type: " + bodyType + "\n"
							+ txt);
				}
			}
			Element rtf = DOMUtils.getUniqueElement(body, "Compressed_RTF");
			if (rtf != null) {
				String txt = rtf.getTextContent();
				return RTFUtils.extractB64CompressedRTF(txt);
			}
		}
		return null;
	}

	private CalendarBusyStatus getCalendarBusyStatus(Element domSource) {
		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource,
				"BusyStatus"))) {
		case 0:
			return CalendarBusyStatus.FREE;
		case 1:
			return CalendarBusyStatus.TENTATIVE;
		case 2:
			return CalendarBusyStatus.BUSY;
		case 3:
			return CalendarBusyStatus.OUT_OF_OFFICE;
		}
		return null;
	}

	private CalendarSensitivity getCalendarSensitivity(Element domSource) {
		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource,
				"Sensitivity"))) {
		case 0:
			return CalendarSensitivity.NORMAL;
		case 1:
			return CalendarSensitivity.PERSONAL;
		case 2:
			return CalendarSensitivity.PRIVATE;
		case 3:
			return CalendarSensitivity.CONFIDENTIAL;
		}
		return null;
	}

	private CalendarMeetingStatus getMeetingStatus(Element domSource) {
		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource,
				"MeetingStatus"))) {
		case 0:
			return CalendarMeetingStatus.IS_NOT_A_MEETING;
		case 1:
			return CalendarMeetingStatus.IS_A_MEETING;
		case 3:
			return CalendarMeetingStatus.MEETING_RECEIVED;
		case 5:
			return CalendarMeetingStatus.MEETING_IS_CANCELED;
		case 7:
			return CalendarMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED;
		}
		return null;
	}
}
