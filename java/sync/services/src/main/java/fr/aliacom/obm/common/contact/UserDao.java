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
package fr.aliacom.obm.common.contact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obm.configuration.ContactConfiguration;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Email;
import org.obm.sync.exception.ContactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.ObmHelper;

@Singleton
public class UserDao {

	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
	private final ObmHelper obmHelper;
	private final ContactConfiguration contactConfiguration;
	
	@Inject
	private UserDao(ContactConfiguration contactConfiguration, ObmHelper obmHelper) {
		this.contactConfiguration = contactConfiguration;
		this.obmHelper = obmHelper;
	}

	/**
	 * Find updated user since timestamp
	 * @throws SQLException 
	 */
	public ContactUpdates findUpdatedUsers(Date timestamp, AccessToken at) throws SQLException {
		ContactUpdates cu = new ContactUpdates();

		String q = "SELECT userobm_id, userobm_login, userobm_firstname, userobm_lastname, "
				+ "userobm_email, userobm_commonname, userentity_entity_id, domain_name "
				+ "from UserObm "
				+ "INNER JOIN UserEntity on userobm_id=userentity_user_id "
				+ "INNER JOIN Domain on userobm_domain_id=domain_id "
				+ "WHERE userobm_archive != 1 and userobm_domain_id="
				+ at.getDomain().getId() + " and userobm_hidden != 1 ";
		if (timestamp != null) {
			q += " and (userobm_timecreate >= ? or userobm_timeupdate >= ? )";
		}

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			List<Contact> contacts = new ArrayList<Contact>();
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			if (timestamp != null) {
				ps.setTimestamp(1, new Timestamp(timestamp.getTime()));
				ps.setTimestamp(2, new Timestamp(timestamp.getTime()));
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				contacts.add(loadUser(rs));
			}
			rs.close();
			rs = null;
			
			cu.setContacts(contacts);
			
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return cu;
	}

	private Contact loadUser(ResultSet rs) throws SQLException {
		Contact c = new Contact();
		c.setFolderId(contactConfiguration.getAddressBookUserId());
		
		c.setUid(rs.getInt("userobm_id"));
		c.setFirstname(rs.getString("userobm_firstname"));
		c.setLastname(rs.getString("userobm_lastname"));
		c.setCommonname(rs.getString("userobm_commonname"));
		c.addEmail("INTERNET;X-OBM-Ref1", new Email(getEmail(rs
				.getString("userobm_email"), rs.getString("domain_name"))));
		return c;
	}

	private String getEmail(String string, String domain) {
		String[] mails = string.split("\r\n");
		String m = mails[0];
		if (m.length() == 0) {
			return null;
		}
		if (!m.contains("@")) {
			return m + "@" + domain;
		}
		return m;
	}

	public Set<Integer> findRemovalCandidates(Date d, AccessToken at) throws SQLException {
		Set<Integer> ret = new HashSet<Integer>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;

		// FIXME add domain_id to deleted users
		String q = "SELECT deleteduser_user_id FROM DeletedUser ";
		if (d != null) {
			q += " WHERE deleteduser_timestamp >= ?";
		}
		q += " UNION ";
		q += "SELECT userobm_id FROM UserObm where (userobm_archive=1 OR userobm_hidden=1) AND userobm_domain_id="
				+ at.getDomain().getId();
		if (d != null) {
			q += " AND userobm_timeupdate >= ? ";
		}
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			if (d != null) {
				ps.setTimestamp(1, new Timestamp(d.getTime()));
				ps.setTimestamp(2, new Timestamp(d.getTime()));
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getInt(1));
			}

		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
	}

	public String getUserDomain(String login) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String userDomain = null;
		try {
			con = obmHelper.getConnection();

			String query;
			query = "SELECT domain_name" + " FROM UserObm, Domain WHERE"
					+ " userobm_domain_id = domain_id AND userobm_login = ?";

			ps = con.prepareStatement(query);

			ps.setString(1, login);
			rs = ps.executeQuery();

			if (rs.next()) {
				do {
					userDomain = rs.getString(1);
				} while (rs.next());
			}

		} catch (SQLException e) {
			logger.error("Could not find user domain", e);
		} finally {
			try {
				obmHelper.cleanup(con, ps, rs);
			} catch (Exception e) {
				logger.error("Could not clean up jdbc stuff");
			}
		}
		return userDomain;
	}

	public Contact findUserObmContact(AccessToken token, Integer userId) throws SQLException, ContactNotFoundException {
		String sql = "SELECT userobm_id, userobm_login, userobm_firstname, userobm_lastname, "
				+ "userobm_email, userobm_commonname, userentity_entity_id, domain_name "
				+ "from UserObm "
				+ "INNER JOIN UserEntity on userobm_id=userentity_user_id "
				+ "INNER JOIN Domain on userobm_domain_id=domain_id "
				+ "WHERE userobm_archive != 1 and userobm_domain_id="
				+ token.getDomain().getId() + " and userobm_hidden != 1 and userobm_id = ?";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();
		
			if (rs.next()) {
				return loadUser(rs);
			}
			throw new ContactNotFoundException("Contact user obm not found.", userId);
		
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}

}
