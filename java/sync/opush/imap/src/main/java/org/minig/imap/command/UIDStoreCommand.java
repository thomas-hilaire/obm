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

package org.minig.imap.command;

import java.util.Collection;
import java.util.List;

import org.minig.imap.FlagsList;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;

public class UIDStoreCommand extends Command<Boolean> {

	private Collection<Long> uids;
	private FlagsList fl;
	private boolean set;

	public UIDStoreCommand(Collection<Long> uids, FlagsList fl, boolean set) {
		this.uids = uids;
		this.fl = fl;
		this.set = set;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		IMAPResponse ok = rs.get(rs.size() - 1);
		data = ok.isOk();
		if (logger.isDebugEnabled()) {
			logger.debug(ok.getPayload());
		}
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID STORE " + MessageSet.asString(uids) + " "
				+ (set ? "+" : "-") + "FLAGS.SILENT " + fl.toString();

		if (logger.isDebugEnabled()) {
			logger.debug("cmd: " + cmd);
		}

		return new CommandArgument(cmd, null);
	}

}
