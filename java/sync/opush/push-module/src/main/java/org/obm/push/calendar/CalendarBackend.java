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

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.obm.push.backend.DataDelta;
import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathUtils;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.IllegalMSEventStateException;
import org.obm.push.exception.MSObjectException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.service.EventService;
import org.obm.push.service.impl.MappingService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.CalendarType;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.items.EventChanges;
import org.obm.sync.services.ICalendar;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class CalendarBackend extends ObmSyncBackend implements PIMBackend {

	private final EventConverter eventConverter;
	private final EventService eventService;
	private final ICalendar calendarClient;

	@Inject
	private CalendarBackend(MappingService mappingService, 
			@Named(CalendarType.CALENDAR) ICalendar calendarClient, 
			EventConverter eventConverter, 
			EventService eventService,
			LoginService login) {
		super(mappingService, login);
		this.calendarClient = calendarClient;
		this.eventConverter = eventConverter;
		this.eventService = eventService;
	}

	private String getDefaultCalendarName(BackendSession bs) {
		return "obm:\\\\" + bs.getUser().getLoginAtDomain() + "\\calendar\\"
				+ bs.getUser().getLoginAtDomain();
	}
	
	@Override
	public PIMDataType getPIMDataType() {
		return PIMDataType.CALENDAR;
	}
	
	public List<ItemChange> getHierarchyChanges(BackendSession bs) 
			throws DaoException, UnexpectedObmSyncServerException {

		if (!bs.checkHint("hint.multipleCalendars", false)) {
			return getDefaultCalendarItemChange(bs);
		} else {
			return getCalendarList(bs);
		}
	}

	private List<ItemChange> getCalendarList(BackendSession bs) throws DaoException, UnexpectedObmSyncServerException {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		AccessToken token = login(bs);
		try {
			CalendarInfo[] cals = calendarClient.listCalendars(token);
			String domain = bs.getUser().getDomain();
			for (CalendarInfo ci : cals) {
				ItemChange ic = new ItemChange();
				String col = CollectionPathUtils.buildCollectionPath(
						bs, PIMDataType.CALENDAR, ci.getUid() + domain);
				Integer collectionId = mappingService.getCollectionIdFor(bs.getDevice(), col);
				ic.setServerId(mappingService.collectionIdToString(collectionId));
				ic.setParentId("0");
				ic.setDisplayName(ci.getMail() + " calendar");
				if (bs.getUser().getLoginAtDomain().equalsIgnoreCase(ci.getMail())) {
					ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
				} else {
					ic.setItemType(FolderType.USER_CREATED_CALENDAR_FOLDER);
				}
				ret.add(ic);
			}
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage());
		} finally {
			logout(token);
		}
		return ret;
	}

	private List<ItemChange> getDefaultCalendarItemChange(BackendSession bs) throws DaoException {
		
		ItemChange ic = new ItemChange();
		String col = getDefaultCalendarName(bs);
		String serverId = "";
		try {
			Integer collectionId = mappingService.getCollectionIdFor(bs.getDevice(), col);
			serverId = mappingService.collectionIdToString(collectionId);
		} catch (CollectionNotFoundException e) {
			serverId = mappingService.createCollectionMapping(bs.getDevice(), col);
			ic.setIsNew(true);
		}
		ic.setServerId(serverId);
		ic.setParentId("0");
		ic.setDisplayName(bs.getUser().getLoginAtDomain() + " calendar");
		ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
		return ImmutableList.of(ic);
	}

	@Override
	public int getItemEstimateSize(BackendSession bs, FilterType filterType,
			Integer collectionId, SyncState state)
			throws CollectionNotFoundException, ProcessingEmailException,
			DaoException, UnexpectedObmSyncServerException {
		DataDelta dataDelta = getChanged(bs, state, filterType, collectionId);
		return dataDelta.getItemEstimateSize();
	}
	
	@Override
	public DataDelta getChanged(BackendSession bs, SyncState state,
			FilterType filterType, Integer collectionId) throws DaoException,
			CollectionNotFoundException, UnexpectedObmSyncServerException,
			ProcessingEmailException {
		
		AccessToken token = login(bs);
		
		String collectionPath = mappingService.getCollectionPathFor(collectionId);
		String calendar = parseCalendarName(collectionPath);

		state.updatingLastSync(filterType);
		try {
			
			EventChanges changes = null;
			if (state.isLastSyncFiltred()) {
				changes = calendarClient.getSyncEventDate(token, calendar, state.getLastSync());
			} else {
				changes = calendarClient.getSync(token, calendar, state.getLastSync());
			}
			
			logger.info("Event changes [ {} ]", changes.getUpdated().length);
			logger.info("Event changes LastSync [ {} ]", changes.getLastSync().toString());
		
			DataDelta delta = 
					buildDataDelta(bs, collectionId, token, changes);
			
			logger.info("getContentChanges( {}, {}, lastSync = {} ) => {}",
				new Object[]{calendar, collectionPath, state.getLastSync(), delta.statistics()});
			
			return delta;
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	private DataDelta buildDataDelta(BackendSession bs, Integer collectionId,
			AccessToken token, EventChanges changes) throws ServerFault,
			DaoException {
		final String userEmail = calendarClient.getUserEmail(token);
		Preconditions.checkNotNull(userEmail, "User has no email address");
		
		List<ItemChange> additions = addOrUpdateEventFilter(changes.getUpdated(), userEmail, collectionId, bs);
		List<ItemChange> deletions = removeEventFilter(changes.getUpdated(), changes.getRemoved(), userEmail, collectionId);
		Date syncDate = changes.getLastSync();
		
		return new DataDelta(additions, deletions, syncDate);
	}

	private List<ItemChange> addOrUpdateEventFilter(Event[] events, String userEmail, 
			Integer collectionId, BackendSession bs) throws DaoException {
		
		List<ItemChange> items = Lists.newArrayList();
		for (final Event event : events) {
			if (checkIfEventCanBeAdded(event, userEmail) && event.getRecurrenceId() == null) {
				String serverId = getServerIdFor(collectionId, event.getObmId());
				ItemChange change = createItemChangeToAddFromEvent(bs, event, serverId);
				items.add(change);
			}	
		}
		return items;
	}
	
	private List<ItemChange> removeEventFilter(Event[] events, EventObmId[] eventsIdRemoved, 
			String userEmail, Integer collectionId) {
		
		List<ItemChange> deletions = Lists.newArrayList();
		for (final Event event : events) {
			if (!checkIfEventCanBeAdded(event, userEmail)) {
				deletions.add(getItemChange(collectionId, event.getObmId()));
			}			
		}
		
		for (final EventObmId eventIdRemove : eventsIdRemoved) {
			deletions.add(getItemChange(collectionId, eventIdRemove));
		}
		return deletions;
	}
	
	private ItemChange getItemChange(Integer collectionId,	EventObmId eventIdRemove) {
		return mappingService.getItemChange(collectionId, eventIdRemove.serializeToString());
	}

	private boolean checkIfEventCanBeAdded(Event event, String userEmail) {
		for (final Attendee attendee : event.getAttendees()) {
			if (userEmail.equals(attendee.getEmail()) && 
					ParticipationState.DECLINED.equals(attendee.getState())) {
				return false;
			}
		}
		return true;
	}
	
	private String parseCalendarName(String collectionPath) {
		// parse obm:\\thomas@zz.com\calendar\sylvaing@zz.com
		int slash = collectionPath.lastIndexOf("\\");
		int at = collectionPath.lastIndexOf("@");
		return collectionPath.substring(slash + 1, at);
	}

	private ItemChange createItemChangeToAddFromEvent(final BackendSession bs, final Event event, String serverId) throws DaoException {
		IApplicationData ev = eventService.convertEventToMSEvent(bs, event);
		ItemChange ic = new ItemChangeBuilder()
			.withServerId(serverId)
			.withApplicationData(ev)
			.build();
		return ic;
	}

	private String getServerIdFor(Integer collectionId, EventObmId uid) {
		return mappingService.getServerIdFor(collectionId, uid.serializeToString());
	}

	@Override
	public String createOrUpdate(BackendSession bs, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws CollectionNotFoundException, ProcessingEmailException, 
			DaoException, UnexpectedObmSyncServerException, ItemNotFoundException, MSObjectException {

		AccessToken token = login(bs);
		
		String collectionPath = mappingService.getCollectionPathFor(collectionId);
		logger.info("createOrUpdate( collectionPath = {}, serverId = {} )", new Object[]{collectionPath, serverId});
		
		EventObmId eventId = null;
		Event event = null;
		try {
			
			Event oldEvent = null;
			if (serverId != null) {
				eventId = convertServerIdToEventObmId(serverId);
				oldEvent = calendarClient.getEventFromId(token, bs.getUser().getLoginAtDomain(), eventId);	
			}

			boolean isInternal = EventConverter.isInternalEvent(oldEvent, true);
			event = convertMSObjectToObmObject(bs, data, oldEvent, isInternal);

			if (eventId != null) {
				event.setUid(eventId);
				setSequence(oldEvent, event);
				updateCalendarEntity(calendarClient, token, collectionPath, oldEvent, event);
			} else {
				eventId = createCalendarEntity(bs, calendarClient, token, collectionPath, event, data);
			}
				
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} catch (EventAlreadyExistException e) {
			eventId = getEventIdFromExtId(token, collectionPath, calendarClient, event);
		} catch (EventNotFoundException e) {
			throw new ItemNotFoundException(e);
		} finally {
			logout(token);
		}
		
		return getServerIdFor(collectionId, eventId);
	}

	private void updateCalendarEntity(ICalendar cc, AccessToken token, String collectionPath, Event old, Event event) throws ServerFault {
		if (event.getExtId() == null || event.getExtId().getExtId() == null) {
			event.setExtId(old.getExtId());
		}
		cc.modifyEvent(token, parseCalendarName(collectionPath), event, true, true);
	}

	private void setSequence(Event oldEvent, Event event) {
		if (event.hasImportantChanges(oldEvent)) {
			event.setSequence(oldEvent.getSequence() + 1);
		} else {
			event.setSequence(oldEvent.getSequence());
		}
	}

	private EventObmId createCalendarEntity(BackendSession bs, ICalendar cc,
			AccessToken token, String collectionPath, Event event, IApplicationData data)
			throws ServerFault, EventAlreadyExistException, DaoException {
		switch (event.getType()) {
		case VEVENT:
			return createEvent(bs, cc, token, collectionPath, event, (MSEvent) data);
		case VTODO:
			return createTodo(cc, token, collectionPath, event);
		default:
			throw new InvalidParameterException("unsupported type " + event.getType());
		}
	}

	private EventObmId createTodo(ICalendar cc,
			AccessToken token, String collectionPath, Event event)
			throws ServerFault, EventAlreadyExistException {
		return cc.createEvent(token, parseCalendarName(collectionPath), event, true);
	}

	private EventObmId createEvent(BackendSession bs, ICalendar cc,
			AccessToken token, String collectionPath, Event event, MSEvent msEvent)
			throws ServerFault, EventAlreadyExistException, DaoException {
		EventExtId eventExtId = generateExtId();
		event.setExtId(eventExtId);
		EventObmId eventId = cc.createEvent(token, parseCalendarName(collectionPath), event, true);
		eventService.trackEventObmIdMSEventUidTranslation(eventId, msEvent.getUid(), bs.getDevice());
		return eventId;
	}

	private EventExtId generateExtId() {
		UUID uuid = UUID.randomUUID();
		return new EventExtId(uuid.toString());
	}
	
	private EventObmId convertServerIdToEventObmId(String serverId) {
		int idx = serverId.lastIndexOf(":");
		return new EventObmId(serverId.substring(idx + 1));
	}

	private Event convertMSObjectToObmObject(BackendSession bs,
			IApplicationData data, Event oldEvent, boolean isInternal) throws IllegalMSEventStateException {
		return eventConverter.convert(bs, oldEvent, (MSEvent) data, isInternal);
	}
	
	private EventObmId getEventIdFromExtId(AccessToken token, String collectionPath, ICalendar cc, Event event)
			throws UnexpectedObmSyncServerException {
		
		try {
			return cc.getEventObmIdFromExtId(token, parseCalendarName(collectionPath), event.getExtId());
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} catch (EventNotFoundException e) {
			logger.info(e.getMessage());
		}
		return null;
	}

	@Override
	public void delete(BackendSession bs, Integer collectionId, String serverId, Boolean moveToTrash) 
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException, ItemNotFoundException {
		
		String collectionPath = mappingService.getCollectionPathFor(collectionId);
		if (serverId != null) {

			AccessToken token = login(bs);
			try {
				logger.info("Delete event serverId {}", serverId);
				//FIXME: not transactional
				String calendarName = parseCalendarName(collectionPath);
				Event evr = getEventFromServerId(token, calendarName, serverId);
				calendarClient.removeEventById(token, calendarName, evr.getObmId(), evr.getSequence(), true);
			} catch (ServerFault e) {
				throw new UnexpectedObmSyncServerException(e);
			} catch (EventNotFoundException e) {
				throw new ItemNotFoundException(e);
			} catch (org.obm.sync.NotAllowedException e) {
				logger.error(e.getMessage(), e);
			} finally {
				logout(token);
			}
		}
	}

	public String handleMeetingResponse(BackendSession bs, MSEmail invitation, AttendeeStatus status) 
			throws UnexpectedObmSyncServerException, CollectionNotFoundException, DaoException,
			ItemNotFoundException, IllegalMSEventStateException {
		
		MSEvent event = invitation.getInvitation();
		AccessToken at = login(bs);
		try {
			logger.info("handleMeetingResponse = {}", event.getObmId());
			Event obmEvent = createOrModifyInvitationEvent(bs, event, at);
			event.setObmId(obmEvent.getObmId());
			event.setObmSequence(obmEvent.getSequence());
			return updateUserStatus(bs, event, status, calendarClient, at);
		} catch (UnexpectedObmSyncServerException e) {
			throw e;
		} catch (EventNotFoundException e) {
			throw new ItemNotFoundException(e);
		} finally {
			logout(at);
		}
	}

	private Event createOrModifyInvitationEvent(BackendSession bs, MSEvent event, AccessToken at) 
			throws UnexpectedObmSyncServerException, EventNotFoundException, IllegalMSEventStateException {
		
		try {
			Event obmEvent = getEventFromExtId(bs, event, at);
			
			boolean isInternal = EventConverter.isInternalEvent(obmEvent, false);
			Event newEvent = convertMSObjectToObmObject(bs, event, null, isInternal);
			
			if (obmEvent == null) {
				
				EventObmId id = null;
				try {
					id = calendarClient.createEvent(at, bs.getUser().getLoginAtDomain(), newEvent, isInternal);
				} catch (EventAlreadyExistException e) {
					throw new UnexpectedObmSyncServerException("it's not possible because getEventFromExtId == null");
				}
				logger.info("createOrModifyInvitationEvent : create new event {}", event.getObmId());
				return calendarClient.getEventFromId(at, bs.getUser().getLoginAtDomain(), id);
				
			} else {
			
				newEvent.setUid(obmEvent.getObmId());
				newEvent.setSequence(obmEvent.getSequence());
				if(!obmEvent.isInternalEvent()){
					logger.info("createOrModifyInvitationEvent : update event {}", event.getObmId());
					obmEvent = calendarClient.modifyEvent(at, bs.getUser().getLoginAtDomain(), newEvent, true, false);
				}
				return obmEvent;
			}	
			
		} catch (ServerFault fault) {
			throw new UnexpectedObmSyncServerException(fault);
		}		
	}

	private Event getEventFromExtId(BackendSession bs, MSEvent event, AccessToken at) 
			throws ServerFault {
		try {
			return calendarClient.getEventFromExtId(at, bs.getUser().getLoginAtDomain(), event.getExtId());
		} catch (EventNotFoundException e) {
			logger.info(e.getMessage());
		}
		return null;
	}

	private String updateUserStatus(BackendSession bs, MSEvent msEvent, AttendeeStatus status, ICalendar calCli,
			AccessToken at) throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException {
		
		logger.info("update user status[ {} in calendar ]", status.toString());
		ParticipationState participationStatus = EventConverter.getParticipationState(null, status);
		try {
			calCli.changeParticipationState(at, bs.getUser().getLoginAtDomain(), msEvent.getExtId(), participationStatus, msEvent.getObmSequence(), true);
			Integer collectionId = mappingService.getCollectionIdFor(bs.getDevice(), getDefaultCalendarName(bs));
			return getServerIdFor(collectionId, msEvent.getObmId());
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		}
	}

	@Override
	public List<ItemChange> fetch(BackendSession bs, List<String> itemIds)
			throws CollectionNotFoundException, DaoException,
			ProcessingEmailException, UnexpectedObmSyncServerException {
	
		List<ItemChange> ret = new LinkedList<ItemChange>();
		AccessToken token = login(bs);
		for (String serverId : itemIds) {
			try {
				Event event = getEventFromServerId(token, bs.getUser().getLoginAtDomain(), serverId);
				if (event != null) {
					ItemChange ic = createItemChangeToAddFromEvent(bs, event, serverId);
					ret.add(ic);
				}
			} catch (EventNotFoundException e) {
				logger.error("event from serverId {} not found.", serverId);
			} catch (ServerFault e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		logout(token);	
		return ret;
	}
	
	private Event getEventFromServerId(AccessToken token, String calendar, String serverId) throws ServerFault, EventNotFoundException {
		Integer itemId = mappingService.getItemIdFromServerId(serverId);
		if (itemId == null) {
			return null;
		}
		return calendarClient.getEventFromId(token, calendar, new EventObmId(itemId));
	}

	@Override
	public String move(BackendSession bs, String srcFolder, String dstFolder,
			String messageId) throws CollectionNotFoundException,
			ProcessingEmailException {
		return null;
	}

	@Override
	public void emptyFolderContent(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws NotAllowedException {
		throw new NotAllowedException(
				"emptyFolderContent is only supported for emails, collection was "
						+ collectionPath);
	}

}
