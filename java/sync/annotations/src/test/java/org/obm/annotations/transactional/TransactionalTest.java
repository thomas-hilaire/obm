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
package org.obm.annotations.transactional;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.matcher.Matchers;


public class TransactionalTest {

	public abstract static class AbstractTestClass {
		@Transactional
		protected abstract void parentAndChildWithTransactionalTag();
		@Transactional
		protected abstract void parentWithTransactionalTagAndChildWithout();
		protected abstract void childWithTransactionalTagAndParentWithout();
	}
	
	public static class TestClass extends AbstractTestClass {
		@Transactional(propagation=Propagation.REQUIRED)
		public void successfullMethod() {
			//success !!
		}
		
		@Transactional(propagation=Propagation.REQUIRED)
		public void throwingRuntimeExceptionMethod() {
			throw new RuntimeException();
		}
		
		@Transactional(noRollbackOn=RuntimeException.class, propagation=Propagation.REQUIRED)
		public void throwingRuntimeExceptionButNoRollbackMethod() {
			throw new RuntimeException();
		}
		
		@Transactional(propagation=Propagation.REQUIRED)
		public void subThrowingRuntimeExceptionMethod() {
			throwingRuntimeExceptionMethod();
		}
		
		@Transactional(propagation=Propagation.REQUIRED)
		public void subRuntimeExceptionButNoRollbackMethod() {
			throwingRuntimeExceptionButNoRollbackMethod();
		}
		
		@Transactional(propagation=Propagation.REQUIRED)
		public void subMethod() {
			successfullMethod();
		}

		@Transactional(propagation=Propagation.REQUIRED)
		@Override
		protected void parentAndChildWithTransactionalTag() {
			//success !!
		}
		
		@Override
		protected void parentWithTransactionalTagAndChildWithout() {
			//success !!
		}

		@Transactional(propagation=Propagation.REQUIRED)
		@Override
		protected void childWithTransactionalTagAndParentWithout() {
			//success !!
		}
		
		@Transactional(propagation=Propagation.REQUIRES_NEW)
		protected void simpleNested() {
			//success !!
		}
		
		@Transactional(propagation=Propagation.REQUIRED)
		public void requiredWithSuccessfulNestedSubMethod() {
			simpleNested();
		}
		
		@Transactional(propagation=Propagation.REQUIRES_NEW)
		public void nestedThrowingRuntimeException(){
			throw new RuntimeException();
		}
		
		@Transactional(propagation=Propagation.REQUIRES_NEW, noRollbackOn=RuntimeException.class)
		public void nestedThrowingRuntimeExceptionButNoRollbackMethod(){
			throw new RuntimeException();
		}
		
		@Transactional(propagation=Propagation.REQUIRED)
		public void requiredWithNestedSubThrowingRuntimeExceptionMethod() {
			nestedThrowingRuntimeException();
		}
		
		@Transactional(propagation=Propagation.REQUIRED)
		public void requiredThrowingRuntimeExceptionAfterNestedSubMethod() {
			simpleNested();
			throw new RuntimeException();
		}
		
		@Transactional(propagation=Propagation.REQUIRED, noRollbackOn=RuntimeException.class)
		public void requiredWithNestedSubThrowingRuntimeExceptionButNoRollbackMethod() {
			nestedThrowingRuntimeException();
		}
		
		@Transactional(propagation=Propagation.REQUIRED, noRollbackOn=RuntimeException.class)
		public void requiredThrowingRuntimeExceptionButNoRollbackAfterNestedSubMethod() {
			simpleNested();
			throw new RuntimeException();
		}
	}
	
	private TestClass createTestClass(final Provider<TransactionManager> provider) {
		Injector injector = Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(TransactionManager.class).toProvider(provider);
				TransactionalInterceptor transactionalInterceptor = new TransactionalInterceptor();
				bindInterceptor(Matchers.any(), 
						Matchers.annotatedWith(Transactional.class), 
						transactionalInterceptor);
				requestInjection(transactionalInterceptor);
			}
		});
		
		return injector.getInstance(TestClass.class);
	}
	
	private <T> Provider<T> getProvider(final T obj) {
		return new Provider<T>() {
			@Override
			public T get() {
				return obj;
			}
		};
	}
	
	@Test
	public void testOneTransaction() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.commit();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_COMMITTED)).anyTimes();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.successfullMethod();
		EasyMock.verify(transaction);
	}

	@Test
	public void testSub() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION)).once();
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(5);
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.subMethod();
		EasyMock.verify(transaction);
	}
	
	@Test
	public void testRollback() throws NotSupportedException, SystemException, SecurityException, IllegalStateException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.rollback();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.throwingRuntimeExceptionMethod();	
		} catch (RuntimeException e) {
			return;
		} finally {
			EasyMock.verify(transaction);
		}
		Assert.fail("RuntimeExceptionExpected");
	}

	@Test
	public void testSubRollback() throws NotSupportedException, SystemException, SecurityException, IllegalStateException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION)).once();
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.rollback();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.subThrowingRuntimeExceptionMethod();
		} catch (RuntimeException e) {
			return;
		} finally {
			EasyMock.verify(transaction);
		}
		Assert.fail("RuntimeExceptionExpected");
	}
	
	@Test(expected=RuntimeException.class)
	public void testRollbackException() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.throwingRuntimeExceptionButNoRollbackMethod();	
		} finally {
			EasyMock.verify(transaction);
		}
	}
	
	@Test(expected=RuntimeException.class)
	public void testSubRollbackException() throws NotSupportedException, SystemException, SecurityException, IllegalStateException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.rollback();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.subRuntimeExceptionButNoRollbackMethod();	
		} finally {
			EasyMock.verify(transaction);
		}
	}
	
	@Test
	public void parentAndChildWithTransactionalTag() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.parentAndChildWithTransactionalTag();
		EasyMock.verify(transaction);
	}
	
	@Test
	public void parentWithTransactionalTagAndChildWithout() throws SecurityException, IllegalStateException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.parentWithTransactionalTagAndChildWithout();
		EasyMock.verify(transaction);
	}
	
	@Test
	public void childWithTransactionalTagAndParentWithout() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.childWithTransactionalTagAndParentWithout();
		EasyMock.verify(transaction);
	}
	
	@Test
	public void testOneNestedTransaction() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.simpleNested();
		EasyMock.verify(transaction);
	}
	
	@Test
	public void requiredWithNestedChild() throws SystemException, NotSupportedException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException{
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.commit();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.requiredWithSuccessfulNestedSubMethod();
		EasyMock.verify(transaction);
	}
	
	@Test
	public void simpleNestedRollback() throws NotSupportedException, SystemException, SecurityException, IllegalStateException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.rollback();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.nestedThrowingRuntimeException();	
		} catch (RuntimeException e) {
			return;
		} finally {
			EasyMock.verify(transaction);
		}
		Assert.fail("RuntimeExceptionExpected");
	}
	
	@Test(expected=RuntimeException.class)
	public void nestedNoRollbackException() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.nestedThrowingRuntimeExceptionButNoRollbackMethod();	
		} finally {
			EasyMock.verify(transaction);
		}
	}
	
	@Test(expected=RuntimeException.class)
	public void requiredWithNestedChildThrowingRuntimeException() throws SystemException, NotSupportedException, SecurityException, IllegalStateException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.rollback();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.rollback();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.requiredWithNestedSubThrowingRuntimeExceptionMethod();
		EasyMock.verify(transaction);
	}
	
	@Test(expected=RuntimeException.class)
	public void requiredThrowingRuntimeExceptionAfterNestedChild() throws SystemException, NotSupportedException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.commit();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(2);
		transaction.rollback();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.requiredThrowingRuntimeExceptionAfterNestedSubMethod();
		EasyMock.verify(transaction);
	}
	
	@Test(expected=RuntimeException.class)
	public void requiredWithNestedSubThrowingRuntimeExceptionButNoRollback() throws SystemException, NotSupportedException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.rollback();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(2);
		
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.requiredWithNestedSubThrowingRuntimeExceptionButNoRollbackMethod();
		EasyMock.verify(transaction);
	}
	
	@Test(expected=RuntimeException.class)
	public void requiredThrowingRuntimeExceptionButNoRollbackAfterNestedSub() throws SystemException, NotSupportedException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		TransactionManager transaction = EasyMock.createStrictMock(TransactionManager.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.begin();
		EasyMock.expectLastCall().once();

		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(1);
		transaction.commit();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(2);
		
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.requiredThrowingRuntimeExceptionButNoRollbackAfterNestedSubMethod();
		EasyMock.verify(transaction);
	}
	
}
