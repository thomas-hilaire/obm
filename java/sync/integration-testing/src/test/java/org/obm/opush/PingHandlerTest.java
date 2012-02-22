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
package org.obm.opush;


import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.obm.opush.IntegrationPushTestUtils.mockMonitoredCollectionDao;
import static org.obm.opush.IntegrationTestUtils.buildCalendarCollectionPath;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.expectUserCollectionsNeverChange;
import static org.obm.opush.IntegrationTestUtils.replayMocks;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.TransformerException;

import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.HearbeatDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

public class PingHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(PingHandlerTestModule.class);

	@Inject @PortNumber int port;
	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;

	private List<OpushUser> fakeTestUsers;

	@Before
	public void init() {
		fakeTestUsers = Arrays.asList(singleUserFixture.jaures);
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
	}

	@Test
	public void testInterval() throws Exception {
		testHeartbeatInterval(5, 5, 5);
	}

	
	@Test
	public void testMinInterval() throws Exception {
		testHeartbeatInterval(1, 5, 5);
	}
	
	@Test
	public void test3BlockingClient() throws Exception {
		prepareMockNoChange();

		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);

		ThreadPoolExecutor threadPoolExecutor = 
				new ThreadPoolExecutor(20, 20, 1,TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

		Stopwatch stopwatch = new Stopwatch().start();
		
		List<Future<Document>> futures = new ArrayList<Future<Document>>();
		for (int i = 0; i < 4; ++i) {
			 futures.add(queuePingCommand(opClient, threadPoolExecutor));	
		}
		
		for (Future<Document> f: futures) {
			Document response = f.get();
			checkNoChangeResponse(response);
		}
		
		checkExecutionTime(2, 5, stopwatch);
	}

	@Test
	public void testPushNotificationOnBackendChangeShort() throws Exception {
		prepareMockHasChanges(1);

		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		Document document = buildPingCommand(20);
		Stopwatch stopwatch = new Stopwatch().start();
		
		Document response = opClient.postXml("Ping", document, "Ping", null, false);
		
		checkExecutionTime(5, 1, stopwatch);
		checkHasChangeResponse(response);
	}
	
	@Test
	public void testPushNotificationOnBackendChangeLong() throws Exception {
		prepareMockHasChanges(2);

		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		Document document = buildPingCommand(20);
		Stopwatch stopwatch = new Stopwatch().start();
		
		Document response = opClient.postXml("Ping", document, "Ping", null, false);
		
		checkExecutionTime(5, 6, stopwatch);
		checkHasChangeResponse(response);
	}
	
	private void prepareMockNoChange() throws DaoException, CollectionNotFoundException, 
			ProcessingEmailException, UnexpectedObmSyncServerException, AuthFault, ConversionException {
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockForPingNeeds();
		mockForNoChangePing();
		replayMocks(classToInstanceMap);
	}

	private void prepareMockHasChanges(int noChangeIterationCount) throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, AuthFault, ConversionException {
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockForPingNeeds();
		mockForCalendarHasChangePing(noChangeIterationCount);
		replayMocks(classToInstanceMap);
	}
	
	public void testHeartbeatInterval(int heartbeatInterval, int delta, int expected) throws Exception {
		prepareMockNoChange();
		
		opushServer.start();
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		Document document = buildPingCommand(heartbeatInterval);
		Stopwatch stopwatch = new Stopwatch().start();
		
		Document response = opClient.postXml("Ping", document, "Ping", null, false);
		
		checkExecutionTime(delta, expected, stopwatch);
		checkNoChangeResponse(response);
	}

	private void checkExecutionTime(int delta, int expected,
			Stopwatch stopwatch) {
		stopwatch.stop();
		long elapsedTime = stopwatch.elapsedTime(TimeUnit.SECONDS);
		Assertions.assertThat(elapsedTime)
			.isGreaterThanOrEqualTo(expected)
			.isLessThan(expected + delta);
	}

	private void checkNoChangeResponse(Document response)
			throws TransformerException {
		Assertions.assertThat(DOMUtils.serialise(response))
			.isEqualTo(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<Ping><Status>1</Status><Folders/></Ping>");
	}
	
	private void checkHasChangeResponse(Document response) throws TransformerException {
		Assertions.assertThat(DOMUtils.serialise(response))
			.isEqualTo(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><Ping>" +
					"<Status>2</Status>" +
					"<Folders><Folder>1432</Folder></Folders></Ping>");
	}

	private void mockForPingNeeds() throws DaoException {
		MonitoredCollectionDao monitoredCollectionDao = classToInstanceMap.get(MonitoredCollectionDao.class);
		mockMonitoredCollectionDao(monitoredCollectionDao);
		HearbeatDao heartbeatDao = classToInstanceMap.get(HearbeatDao.class);
		mockHeartbeatDao(heartbeatDao);
	}
	
	private void mockForNoChangePing() throws DaoException, CollectionNotFoundException,
			ProcessingEmailException, UnexpectedObmSyncServerException, ConversionException {
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		mockCalendarBackendHasNoChange(calendarBackend);

		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers);
	}

	private void mockForCalendarHasChangePing(int noChangeIterationCount) 
			throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException,
			ProcessingEmailException, ConversionException {
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);

		mockCollectionDaoHasChange(collectionDao, noChangeIterationCount);
		mockCalendarBackendHasContentChanges(calendarBackend);
	}

	private void mockCollectionDaoHasChange(CollectionDao collectionDao, int noChangeIterationCount) 
			throws DaoException, CollectionNotFoundException {
		Date dateFirstSyncFromASSpecs = new Date(0);
		Date dateWhenChangesAppear = new Date();
		int collectionNoChangeIterationCount = noChangeIterationCount;
		int collectionIdWhereChangesAppear = 1234;

		expectCollectionDaoUnchangeForXIteration(collectionDao, dateFirstSyncFromASSpecs, collectionNoChangeIterationCount);

		for (OpushUser user : fakeTestUsers) {
			String collectionPathWhereChangesAppear = buildCalendarCollectionPath(user);
			
			SyncState syncState = new SyncState("sync state");
			expect(collectionDao.lastKnownState(anyObject(Device.class), anyInt())).andReturn(syncState).once();

			expect(collectionDao.getCollectionPath(anyInt())).andReturn(collectionPathWhereChangesAppear).anyTimes();

			ChangedCollections hasChangesCollections = buildSyncCollectionWithChanges(
					dateWhenChangesAppear, collectionIdWhereChangesAppear, collectionPathWhereChangesAppear);
			expect(collectionDao.getCalendarChangedCollections(dateFirstSyncFromASSpecs)).andReturn(hasChangesCollections).once();
		}
	}
	
	private void expectCollectionDaoUnchangeForXIteration(CollectionDao collectionDao, Date activeSyncSpecFirstSyncDate, 
			int noChangeIterationCount) throws DaoException {
		SyncState syncState = new SyncState("sync state");
		expect(collectionDao.lastKnownState(anyObject(Device.class), anyInt())).andReturn(syncState).times(noChangeIterationCount);
		ChangedCollections noChangeCollections = new ChangedCollections(activeSyncSpecFirstSyncDate, ImmutableSet.<SyncCollection>of());
		expect(collectionDao.getContactChangedCollections(activeSyncSpecFirstSyncDate)).andReturn(noChangeCollections).anyTimes();
		expect(collectionDao.getCalendarChangedCollections(activeSyncSpecFirstSyncDate)).andReturn(noChangeCollections).times(noChangeIterationCount);
	}

	private void mockCalendarBackendHasContentChanges(CalendarBackend calendarBackend)
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ProcessingEmailException, ConversionException {
		
		expect(calendarBackend.getPIMDataType()).andReturn(PIMDataType.CALENDAR).anyTimes();
		expect(calendarBackend.getItemEstimateSize(
				anyObject(BackendSession.class), 
				anyObject(FilterType.class),
				anyInt(),
				anyObject(SyncState.class)))
			.andReturn(1).times(2);
	}

	private void mockCalendarBackendHasNoChange(CalendarBackend calendarBackend) 
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ProcessingEmailException, ConversionException {
		
		expect(calendarBackend.getItemEstimateSize(
				anyObject(BackendSession.class), 
				anyObject(FilterType.class),
				anyInt(),
				anyObject(SyncState.class)))
			.andReturn(0).anyTimes();
	}

	private Future<Document> queuePingCommand(final OPClient opClient,
			ThreadPoolExecutor threadPoolExecutor) {
		return threadPoolExecutor.submit(new Callable<Document>() {
			@Override
			public Document call() throws Exception {
				Document document = buildPingCommand(5);
				return opClient.postXml("Ping", document, "Ping", null, false);
			}
		});
	}

	private void mockHeartbeatDao(HearbeatDao heartbeatDao) throws DaoException {
		heartbeatDao.updateLastHearbeat(anyObject(Device.class), anyLong());
		expectLastCall().anyTimes();
	}
	
	private ChangedCollections buildSyncCollectionWithChanges(Date dateWhenChangesAppear, 
			int collectionIdWhereChangesAppear, String collectionPathWhereChangesAppear) {
		SyncCollection calendarCollection = new SyncCollection(collectionIdWhereChangesAppear, collectionPathWhereChangesAppear);
		ChangedCollections calendarHasChangeCollections = new ChangedCollections(dateWhenChangesAppear , ImmutableSet.<SyncCollection>of(calendarCollection));
		return calendarHasChangeCollections;
	}

	private Document buildPingCommand(int heartbeatInterval)
			throws SAXException, IOException {
		return DOMUtils.parse("<Ping>"
				+ "<HeartbeatInterval>"
				+ heartbeatInterval
				+ "</HeartbeatInterval>"
				+ "<Folders>"
				+ "<Folder>"
				+ "<Id>1432</Id>"
				+ "</Folder>"
				+ "</Folders>"
				+ "</Ping>");
	}
}
