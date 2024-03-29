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
package org.obm.push.protocol.bean;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.SyncCollection;
import org.obm.push.protocol.data.EncoderFactory;

public class SyncResponse {

	public static class SyncCollectionResponse {
		private final SyncCollection syncCollection;
		private List<ItemChange> itemChanges;
		private List<ItemChange> itemChangesDeletion;
		private boolean syncStatevalid;
		private String allocateNewSyncKey;
		
		public SyncCollectionResponse(SyncCollection syncCollection) {
			this.syncCollection = syncCollection;
		}

		public SyncCollection getSyncCollection() {
			return syncCollection;
		}
		public void setSyncStateValid(boolean syncStateValid) {
			syncStatevalid = syncStateValid;
		}
		public boolean isSyncStatevalid() {
			return syncStatevalid;
		}
		public void setItemChanges(List<ItemChange> itemChanges) {
			this.itemChanges = itemChanges;
		}
		public List<ItemChange> getItemChanges() {
			return itemChanges;
		}
		public void setNewSyncKey(String allocateNewSyncKey) {
			this.allocateNewSyncKey = allocateNewSyncKey;
		}
		public String getAllocateNewSyncKey() {
			return allocateNewSyncKey;
		}
		public void setItemChangesDeletion(List<ItemChange> itemChangesDeletion) {
			this.itemChangesDeletion = itemChangesDeletion;
		}
		public List<ItemChange> getItemChangesDeletion() {
			return itemChangesDeletion;
		}
	}
	
	private final Collection<SyncCollectionResponse> collectionResponses;
	private final BackendSession backendSession;
	private final EncoderFactory encoderFactory;
	private final Map<String, String> processedClientIds;
	
	public SyncResponse(Collection<SyncCollectionResponse> collectionResponses, BackendSession bs, EncoderFactory encoderFactory, Map<String, String> processedClientIds) {
		this.collectionResponses = collectionResponses;
		this.backendSession = bs;
		this.encoderFactory = encoderFactory;
		this.processedClientIds = processedClientIds;
	}
	
	public SyncResponse() {
		this(null, null, null, null);
	}

	public BackendSession getBackendSession() {
		return backendSession;
	}
	
	public EncoderFactory getEncoderFactory() {
		return encoderFactory;
	}
	
	public Collection<SyncCollectionResponse> getCollectionResponses() {
		return collectionResponses;
	}
	
	public Map<String, String> getProcessedClientIds() {
		return processedClientIds;
	}
	
}
