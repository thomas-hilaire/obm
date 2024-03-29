<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : host_query.inc                                               //
//     - Desc : host query File                                              //
// 2004-09-09 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


/**
 * Host Search query execution 
 *
 * @param mixed $host : host search criteria
 * @return database object with search result
 */
function run_query_host_search($host) {
  global $cdg_sql, $ctu_sql_limit;
  
  $sql_order_dir = $host['sql_order_dir'];
  $sql_order_field = $host['sql_order_field'];
  $multidomain = sql_multidomain('host');

  $name = sql_search_text_parse($host['name']);
  $delegation = sql_search_text_parse($host['delegation']);
  $ip = sql_search_text_parse($host['ip']);
  $samba = $host['samba'];
  $smtp_in = $host['smtp_in'];
  $smtp_out = $host['smtp_out'];
  $obm_sync = $host['obm_sync'];
  $solr = $host['solr'];
  $imap_frontend = $host['imap_frontend'];
  $monitor = $host['monitor'];
  $imap = $host['imap'];
  $backup_ftp = $host['backup_ftp'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,'host_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type,'host_timecreate', 'timecreate');

  $where = '(1=1)';
  if ($name != '') {
    $where .= " AND host_name $like '$name%'";
  }
  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
    $where .= " AND host_delegation $like '$delegation%'";
  }
  if ($ip != '') {
    $where .= " AND host_ip $like '%$ip%'";
  }
  if ($samba != '') {
    $where .= ' AND samba.service_id IS NOT NULL';
  }
  if ($smtp_in != '') {
    $where .= ' AND smtp_in.service_id IS NOT NULL';
  }
  if ($smtp_out != '') {
    $where .= ' AND smtp_out.service_id IS NOT NULL';
  }
  if ($obm_sync != '') {
    $where .= ' AND obm_sync.service_id IS NOT NULL';
  }
  if ($solr != '') {
    $where .= ' AND solr.service_id IS NOT NULL';
  }
  if ($imap_frontend != '') {
    $where .= ' AND imap_frontend.service_id IS NOT NULL';
  }
  if ($monitor != '') {
    $where .= ' AND monitor.service_id IS NOT NULL';
  }
  if ($imap != '') {
    $where .= ' AND imap.service_id IS NOT NULL';
  }
  if ($backup_ftp != '') {
    $where .= ' AND backup_ftp.service_id IS NOT NULL';
  }
  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'host_name';
  $orderq .= " ORDER BY $order $sql_order_dir";

  $join = "INNER JOIN HostEntity ON hostentity_host_id = host_id
      LEFT JOIN UserObm as A ON host_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON host_userupdate=B.userobm_id
      LEFT JOIN Service as samba ON samba.service_service = 'samba' AND samba.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as imap ON imap.service_service = 'imap' AND imap.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as smtp_in ON smtp_in.service_service = 'smtp_in' AND smtp_in.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as smtp_out ON smtp_out.service_service = 'smtp_out' AND smtp_out.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as obm_sync ON obm_sync.service_service = 'obm_sync' AND obm_sync.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as solr ON solr.service_service = 'solr' AND solr.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as imap_frontend ON imap_frontend.service_service = 'imap_frontend' AND imap_frontend.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as monitor ON monitor.service_service = 'monitor' AND monitor.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as backup_ftp ON backup_ftp.service_service = 'backup_ftp' AND backup_ftp.service_entity_id = hostentity_entity_id";

  $multidomain = sql_multidomain('host');
  $query = "SELECT DISTINCT Host.*,
      samba.service_id as host_samba,
      imap.service_id as host_imap,
      smtp_in.service_id as host_smtp_in,
      smtp_out.service_id as host_smtp_out,
      obm_sync.service_id as host_obm_sync,
      solr.service_id as host_solr,
      monitor.service_id as host_monitor,
      backup_ftp.service_id as host_backup_ftp,
      host_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    FROM Host
    $join
    $whereq $multidomain 
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(distinct host_id) From Host $join $whereq $multidomain");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_host_search()');
    $obm_q->xquery($query);
  }

  return $obm_q;
}


/**
 * Host detail query execution
 *
 * @param mixed $id : host id
 * @return database object with host info
 */
function run_query_host_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type,'host_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type,'host_timecreate', 'timecreate');
  $multidomain = sql_multidomain('host');
  $id = sql_parse_id($id, true);
  $query = "SELECT *,
      hostentity_entity_id as host_entity_id,
      samba.service_id as host_samba,
      imap.service_id as host_imap,
      smtp_in.service_id as host_smtp_in,
      smtp_out.service_id as host_smtp_out,
      obm_sync.service_id as host_obm_sync,
      solr.service_id as host_solr,
      imap_frontend.service_id as host_imap_frontend,
      monitor.service_id as host_monitor,
      backup_ftp.service_id as host_backup_ftp,
      $timecreate,
      $timeupdate,
      uo.userobm_id as userobm_id,
      uo.userobm_login as userobm_login,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Host
      INNER JOIN HostEntity ON hostentity_host_id = host_id
      LEFT JOIN UserObm as c ON host_usercreate=c.userobm_id
      LEFT JOIN UserObm as u ON host_userupdate=u.userobm_id
      LEFT JOIN UserObm as uo ON host_id=uo.userobm_host_id
      LEFT JOIN Service as samba ON samba.service_service = 'samba' AND samba.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as imap ON imap.service_service = 'imap' AND imap.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as smtp_in ON smtp_in.service_service = 'smtp_in' AND smtp_in.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as smtp_out ON smtp_out.service_service = 'smtp_out' AND smtp_out.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as obm_sync ON obm_sync.service_service = 'obm_sync' AND obm_sync.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as solr ON solr.service_service = 'solr' AND solr.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as imap_frontend ON imap_frontend.service_service = 'imap_frontend' AND imap_frontend.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as monitor ON monitor.service_service = 'monitor' AND monitor.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as backup_ftp ON backup_ftp.service_service = 'backup_ftp' AND backup_ftp.service_entity_id = hostentity_entity_id
      LEFT JOIN Domain ON host_domain_id = domain_id
    WHERE host_id $id 
      $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_host_detail()');
  $obm_q->query($query) ;
  $obm_q->next_record();

  return $obm_q;
}


/**
 * Query execution : host insertion
 *
 * @param mixed $host : host hash info
 * @return true if insert ok
 */
function run_query_host_insert($host) {
  global $obm, $cdg_sql, $cg_gid_host;

  $domain_id = $obm['domain_id'];
  $name = $host['name'];
  $ip = $host['ip'];
  $fqdn = $host['fqdn'];
  $samba = sql_parse_int($host['samba']);
  $smtp_in = sql_parse_int($host['smtp_in']);
  $smtp_out = sql_parse_int($host['smtp_out']);
  $obm_sync = sql_parse_int($host['obm_sync']);
  $solr = sql_parse_int($host['solr']);
  $imap_frontend = sql_parse_int($host['imap_frontend']);
  $monitor = sql_parse_int($host['monitor']);
  $imap = sql_parse_int($host['imap']);
  $backup_ftp = sql_parse_int($host['backup_ftp']);
  $list = $host['web_list'];
  $desc = $host['desc'];

  // Postitionnement du l'UID et le GID
  $host_uid = get_first_user_free_uid();
  $host_gid = $cg_gid_host;

  if (isset($host['domain_id']) && $obm['domain_global']) {
    $domain_id = sql_parse_id($host['domain_id']);
  } else {
    $domain_id = sql_parse_id($obm['domain_id']);
  }

  // Delegation handling
  $q_delegation = of_delegation_query_insert_clauses('host_delegation', ($host['delegation']));

  $query = "INSERT INTO Host (host_timeupdate,
    host_timecreate,
    host_userupdate,
    host_usercreate,
    host_domain_id,
    host_uid,
    host_gid,
    host_name
    $q_delegation[field],
    host_ip,
    host_fqdn,
    host_description
    )
  VALUES (null,
    NOW(),
    null,
    $obm[uid],
    $domain_id,
    '$host_uid',
    '$host_gid',
    '$name'
    $q_delegation[value],
    '$ip',
    '$fqdn',
    '$desc'
    )";

  display_debug_msg($query, $cdg_sql, 'run_query_host_insert(1)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Mise a jour du lien user s'il a ete précisé
  $user_id = sql_parse_id($host['user_id'], true);
  $id = $obm_q->lastid();
  $entity_id = of_entity_insert($GLOBALS['module'], $id);    
  if($samba > 0) {
    of_service_insert('samba', $entity_id); 
  }
  if($smtp_in > 0) {
    of_service_insert('smtp_in', $entity_id); 
  }
  if($smtp_out > 0) {
    of_service_insert('smtp_out', $entity_id); 
  }
  if($obm_sync > 0) {
    of_service_insert('obm_sync', $entity_id); 
  }
  if($solr > 0) {
    of_service_insert('solr', $entity_id); 
  }
  if($imap_frontend > 0) {
    of_service_insert('imap_frontend', $entity_id); 
  }
  if($monitor > 0) {
    of_service_insert('monitor', $entity_id); 
  }
  if($imap > 0) {
    of_service_insert('imap', $entity_id); 
  }
  if($backup_ftp > 0) {
    of_service_insert('backup_ftp', $entity_id); 
    of_service_property_insert('backup_ftp', 'login', $entity_id, $host['backup_ftp_login']);
    of_service_property_insert('backup_ftp', 'password', $entity_id, $host['backup_ftp_password']);
    of_service_property_insert('backup_ftp', 'root', $entity_id, $host['backup_ftp_root']);
  }
  if ($retour && $user_id != 0) {

    $host_id = sql_parse_id($id);

    $multidomain = sql_multidomain('userobm');
    $query = "UPDATE UserObm 
    SET userobm_timeupdate=NOW(),
        userobm_userupdate=$obm[uid],
        userobm_host_id=$host_id
    WHERE
        userobm_id $user_id $multidomain";

    display_debug_msg($query, $cdg_sql, 'run_query_host_insert(3)');
    $retour = $obm_q->query($query);
  }
 
  if ( !(write_munin_conf()) ){
	$retour = false;
  }

  return $retour;
}


/**
 * Host Update query execution
 *
 * @param mixed $host : host hash info
 * @return true is update OK
 */
function run_query_host_update($host) {
  global $obm, $cdg_sql, $cmd_update_ftp_entity;

  $id = sql_parse_id($host['host_id'], true);
  $name = $host['name'];
  $ip = $host['ip'];
  $fqdn = $host['fqdn'];
  $samba = sql_parse_int($host['samba']);
  $smtp_in = sql_parse_int($host['smtp_in']);
  $smtp_out = sql_parse_int($host['smtp_out']);
  $obm_sync = sql_parse_int($host['obm_sync']);
  $solr = sql_parse_int($host['solr']);
  $imap_frontend = sql_parse_int($host['imap_frontend']);
  $monitor = sql_parse_int($host['monitor']);
  $imap = sql_parse_int($host['imap']);
  $backup_ftp = sql_parse_int($host['backup_ftp']);
  $desc = $host['desc'];
  $multidomain = sql_multidomain('host');

  // Delegation handling
  $q_delegation = of_delegation_query_update_clause('host_delegation', ($host['delegation']));

  $query = "UPDATE Host SET
    host_timeupdate=NOW(),
    host_userupdate=$obm[uid],
    host_name='$name',
    host_ip='$ip',
    host_fqdn='$fqdn',
    host_description='$desc'
    $q_delegation
  WHERE host_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_host_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Mise a jour du lien user s'il a ete précisé
  $user_id = sql_parse_id($host['user_id'], true);
  if ($retour) {
    $entity_id = of_entity_get($GLOBALS['module'], $host['host_id']);
    if($samba > 0) {
      of_service_insert('samba', $entity_id); 
    } else {
      of_service_delete('samba', $entity_id); 
    }
    if($smtp_in > 0) {
      of_service_insert('smtp_in', $entity_id); 
    } else {
      of_service_delete('smtp_in', $entity_id); 
    }
    if($smtp_out > 0) {
      of_service_insert('smtp_out', $entity_id); 
    } else {
      of_service_delete('smtp_out', $entity_id); 
    }
    if($obm_sync > 0) {
      of_service_insert('obm_sync', $entity_id); 
    } else {
      of_service_delete('obm_sync', $entity_id); 
    }
    if($solr > 0) {
      of_service_insert('solr', $entity_id); 
    } else {
      of_service_delete('solr', $entity_id); 
    }
    if($imap_frontend > 0) {
      of_service_insert('imap_frontend', $entity_id); 
    } else {
      of_service_delete('imap_frontend', $entity_id); 
    }
    if($monitor > 0) {
      of_service_insert('monitor', $entity_id); 
    } else {
      of_service_delete('monitor', $entity_id); 
    }
    if($imap > 0) {
      of_service_insert('imap', $entity_id); 
    } else {
      of_service_delete('imap', $entity_id); 
    }
    of_service_property_delete_all('backup_ftp', $entity_id);
    if($backup_ftp > 0) {
      of_service_insert('backup_ftp', $entity_id);
      of_service_property_insert('backup_ftp', 'login', $entity_id, $host['backup_ftp_login']);
      of_service_property_insert('backup_ftp', 'password', $entity_id, $host['backup_ftp_password']);
      of_service_property_insert('backup_ftp', 'root', $entity_id, $host['backup_ftp_root']);
	  // get domain id of host
      $query = "SELECT host_domain_id 
                FROM Host 
                WHERE host_id $id";
      $retour = $obm_q->query($query);
      $obm_q->next_record();
      $domain_id = $obm_q->f('host_domain_id');
      if ($domain_id) {
         $cmd = 'echo "host:'.$name.'" | '.$cmd_update_ftp_entity.' '.$domain_id ;
         display_debug_msg($cmd, $cdg_exe, 'run_query_host_update()');
         exec($cmd) ;
      }
    } else {
      of_service_delete('backup_ftp', $entity_id); 
    }
    // Suppression de l'ancien lien user=>host
    $query = "UPDATE UserObm 
    SET userobm_timeupdate=NOW(),
        userobm_userupdate='$obm[uid]',
        userobm_host_id='0'
    WHERE
        userobm_host_id $id ";
    $retour = $obm_q->query($query);

    if ($retour && $user_id != 0) {
      $id = sql_parse_id($id);
      // Creation du nouveau lien user=>host
      $query = "UPDATE UserObm 
      SET userobm_timeupdate=NOW(),
          userobm_userupdate=$obm[uid],
          userobm_host_id=$id
      WHERE
          userobm_id $user_id ";

      display_debug_msg($query, $cdg_sql, 'run_query_host_update()');
      $retour = $obm_q->query($query);
    }
  }
  if ( !(write_munin_conf()) ){
	$retour = false;
  }
  return $retour;

}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : host id
///////////////////////////////////////////////////////////////////////////////
function run_query_host_delete($p_id) {
  global $obm, $cdg_sql;

  // Delete the Host
  $multidomain = sql_multidomain('host');
  of_entity_delete($GLOBALS['module'], $p_id);
  $sel_id = sql_parse_id($p_id, true);
  $query = "DELETE FROM Host WHERE host_id $sel_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_host_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // If this host exists in Prod table, Insert it in Deleted table
  $query = "SELECT host_id,
    host_delegation,
    host_domain_id
  FROM P_Host
  WHERE host_id
    $sel_id
    AND host_id NOT IN (SELECT deleted_entity_id FROM Deleted WHERE deleted_table = 'Host')";
  display_debug_msg($query, $cdg_sql, 'run_query_host_delete(auto)');
  $obm_q->query($query);
  $obm_q->next_record();
  $delegation = $obm_q->f('host_delegation');
  $domain_id = $obm_q->f('host_domain_id');

  if ($obm_q->num_rows() > 0) {
    $uid = sql_parse_id($obm['uid']);
    $ins_id = sql_parse_id($p_id);
    $domain_id = sql_parse_id($domain_id);

    $query = "INSERT INTO Deleted (
      deleted_domain_id,
      deleted_user_id,
      deleted_delegation,
      deleted_table,
      deleted_entity_id,
      deleted_timestamp)
    VALUES (
      $domain_id,
      $uid,
      '$delegation',
      'Host',
      $ins_id,
      NOW())";
    display_debug_msg($query, $cdg_sql, 'run_query_host_delete(auto)');
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return the host infos
// Parameters:
//   - $id : host id
///////////////////////////////////////////////////////////////////////////////
function get_host_info($id) {
  global $cdg_sql;

  if ($id == '') {
    return false;
  }
  $id = sql_parse_id($id, true);
  $multidomain = sql_multidomain('host');
  $query = "SELECT Host.*,
    samba.service_id as host_samba,
      imap.service_id as host_imap,
      smtp_in.service_id as host_smtp_in,
      smtp_out.service_id as host_smtp_out,
      solr.service_id as host_solr,
      obm_sync.service_id as host_obm_sync,
      imap_frontend.service_id as host_imap_frontend,
      monitor.service_id as host_monitor,
      backup_ftp.service_id as host_backup_ftp
    FROM Host
      INNER JOIN HostEntity ON hostentity_host_id = host_id
      LEFT JOIN Service as samba ON samba.service_service = 'samba' AND samba.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as imap ON imap.service_service = 'imap' AND imap.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as smtp_in ON smtp_in.service_service = 'smtp_in' AND smtp_in.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as smtp_out ON smtp_out.service_service = 'smtp_out' AND smtp_out.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as obm_sync ON obm_sync.service_service = 'obm_sync' AND obm_sync.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as solr ON solr.service_service = 'solr' AND solr.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as imap_frontend ON imap_frontend.service_service = 'imap_frontend' AND imap_frontend.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as monitor ON monitor.service_service = 'monitor' AND monitor.service_entity_id = hostentity_entity_id
      LEFT JOIN Service as backup_ftp ON backup_ftp.service_service = 'backup_ftp' AND backup_ftp.service_entity_id = hostentity_entity_id
    WHERE host_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_host_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $h['name'] = $obm_q->f('host_name');
  $h['domain_id'] = $obm_q->f('host_domain_id');
  $h['delegation'] = $obm_q->f('host_delegation');
  $h['ip'] = $obm_q->f('host_ip');
  $h['fqdn'] = $obm_q->f('host_fqdn');
  $h['samba'] = $obm_q->f('host_samba');
  $h['smtp_in'] = $obm_q->f('host_smtp_in');
  $h['smtp_out'] = $obm_q->f('host_smtp_out');
  $h['obm_sync'] = $obm_q->f('host_obm_sync');
  $h['solr'] = $obm_q->f('host_solr');
  $h['imap_frontend'] = $obm_q->f('host_imap_frontend');
  $h['monitor'] = $obm_q->f('host_monitor');
  $h['imap'] = $obm_q->f('host_imap');
  $h['backup_ftp'] = $obm_q->f('host_backup_ftp');
  $h['desc'] = $obm_q->f('host_description');

  return $h;
}


function get_host_delegations($ip) {
  global $cdg_sql;
  $multidomain = sql_multidomain('host');
  $query = "SELECT host_delegation 
    FROM Host
    WHERE host_ip='$ip' $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_host_name_exists()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $delegations = array();
  while ($obm_q->next_record()) {
    $delegations[] = $obm_q->f('host_delegation');
  }
  return ($delegations);
}

///////////////////////////////////////////////////////////////////////////////
// Check if a host (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : host id to exclude
// Returns:
//   - true (if a host exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_host_name_exists($name, $id='') {
  global $cdg_sql;

  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "host_id $id AND";
  }

  $multidomain = sql_multidomain('host');
  $query = "SELECT host_id, host_name
    FROM Host
    WHERE $where_id host_name='$name' $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_host_name_exists()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the hosts which matches the name or the desc
// except the one given (update mode)
// Parameters:
//   - $id   : host id
//   - $name : host name
//   - $desc : host desc
///////////////////////////////////////////////////////////////////////////////
function run_query_check_host($id, $name, $desc) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "host_id $id AND";
  }

  // If name is short, we test equality, else similarity
  if (strlen($name) > 2) {
    $wname = "host_name like '%$name%'";
  } else {
    $wname = "host_name = '$name'";
  }

  // If desc is short, we test equality, else similarity
  if (trim($desc) != '') {
    if (strlen($desc) > 2) {
      $wdesc = "host_description like '%$desc%'";
    } else {
      $wdesc = "host_description = '$desc'";
    }
  }
  if ($wdesc != '') $wdesc = "OR $wdesc";

  $multidomain = sql_multidomain('host');
  $query = "SELECT DISTINCT host_id, host_name, host_description
     FROM Host
     WHERE $where_id
       ($wname $wdesc) $multidomain ";

  display_debug_msg($query, $cdg_sql, 'run_query_check_host()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Host context checking (same hosts exists ?)
// Parameters:
//   - $id     : host id
//   - $host[] : host values
//     keys used : name, desc
// Returns:
//   - Host Database object with host of similar hosts
///////////////////////////////////////////////////////////////////////////////
function check_host_can_delete($id) {
  global $cdg_sql, $err;

  $delete_ok = true;
  $query = "
    SELECT count(*) as count FROM ServiceProperty 
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    WHERE serviceproperty_service = 'mail' AND serviceproperty_property IN ('imap', 'smtp_in', 'smtp_out')
    AND serviceproperty_value = '$id'";
  $obm_q = new DB_OBM;  
  display_debug_msg($query, $cdg_sql, 'check_host_can_delete');
  $obm_q->query($query);
  $obm_q->next_record();
  if ($obm_q->f('count') != 0) {
    $delete_ok = false;
    $err['msg'] .= $GLOBALS['l_cant_delete'];
  }

  $query = "
    SELECT count(*) as count FROM ServiceProperty 
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    WHERE serviceproperty_service = 'sync' AND serviceproperty_property IN ('obm_sync')
    AND serviceproperty_value = '$id'";
  $obm_q = new DB_OBM;  
  display_debug_msg($query, $cdg_sql, 'check_host_can_delete');
  $obm_q->query($query);
  $obm_q->next_record();
  if ($obm_q->f('count') != 0) {
    $delete_ok = $delete_ok && true;
  } 

  // Check that user has delegation rights for this host
  if (! check_host_update_rights(array('host_id' => $id))) {
    $delete_ok = false;
  }  

  return $delete_ok;
}

function write_munin_conf(){
  global $cdg_sql, $err;
  $muninFile = "/etc/munin/munin.conf";
  $muninContentConf = "#This is generate by OBM\n".
	"dbdir /var/lib/munin\n".
	"htmldir /var/www/munin\n".
	"logdir /var/log/munin\n".
	"rundir /var/run/munin\n".
	"tmpldir /etc/munin/templates\n\n";

  $query = "
    SELECT DISTINCT host_fqdn, host_ip, host_id, hostentity_host_id, hostentity_entity_id, service_entity_id from Host
	  LEFT JOIN HostEntity ON host_id=hostentity_host_id
	  LEFT JOIN Service ON hostentity_entity_id=service_entity_id
	  WHERE service_service = 'monitor' 
		AND host_fqdn != '' AND host_ip != ''
		AND host_ip is not null AND host_fqdn is not null";
   $obm_q = new DB_OBM;
   display_debug_msg($query, $cdg_sql, 'write_munin_conf');
   $obm_q->query($query);
   while ($obm_q->next_record()) {
     $muninContentConf .= 
    	"[".$obm_q->f('host_fqdn')."]\n".
		"\taddress ".$obm_q->f('host_ip')."\n".
		"\tuse_node_name yes\n";
   }
    if (file_exists($muninFile)) {
      if($fh=fopen($muninFile, 'w')) {
        fwrite($fh, $muninContentConf);
        fclose($fh);
        return true;
      }else{
        return false;
      }
    }else{
      return true;
    }

}

///////////////////////////////////////////////////////////////////////////////
// Host context checking (same hosts exists ?)
// Parameters:
//   - $id     : host id
//   - $host[] : host values
//     keys used : name, desc
// Returns:
//   - Host Database object with host of similar hosts
///////////////////////////////////////////////////////////////////////////////
function check_host_context($id, $host) {
  global $cdg_sql;

  $name = $host['name'];
  $desc = $host['desc'];

  // return the hosts with same name or desc
  $g_q = run_query_check_host($id, $name, $desc);

  return $g_q;
}


/**
 * check_host_update_rights
 * Host specific rights checking (eg: delegation)
 *
 * @param array $params paramaters hash
 * @return bool true or false
 */
function check_host_update_rights($params) {
  global $err, $cgp_use, $l_error_delegation;

  $id = $params['host_id'];

  if ($id > 0) {

    // Check if delegation is used
    if ($cgp_use['property']['delegation']) {
      $h = get_host_info($id);
      // Check delegation update right
      if (! of_delegation_check_update_rights($params, $h['delegation'])) {
	$err['msg'] .= $l_error_delegation;
	return false;
      }
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Host Form Data checking and formatting
// Parameters:
//   - $host[]  : values checked
//     keys used : name, desc
///////////////////////////////////////////////////////////////////////////////
function check_host_data_form($host) {
  global $php_regexp_hostname, $l_err_host_name_empty, $l_err_host_name_exist;
  global $cgp_use, $err, $l_err_host_name;
  global $php_regexp_ip, $l_err_host_ip, $l_err_host_ip_exist,$php_regexp_fqdn;
  global $l_err_host_ip_not_in_lan, $l_err_firewall_perms, $l_err_web_all;
  global $l_err_host_windows_not_global, $l_err_host_fqdn, $l_err_host_monitoring;
  
  $id = $host['host_id'];
  $name = $host['name'];
  $ip = $host['ip'];
  $samba = $host['samba'];
  $fqdn = $host['fqdn'];
  $web_list = $host['web_list'];
  $web_all = $host['web_all'];
  $monitor = $host['monitor'];
  $backup_ftp = $host['backup_ftp'];
  $delegation = $host['delegation'];

  if (!empty($backup_ftp)) {
    if (empty($host['backup_ftp_login'])) {
      $err['msg'] = $GLOBALS['l_err_host_backup_ftp_login_empty'];
      $err['field'] = 'backup_ftp_login';
      return false;
    }
    if (empty($host['backup_ftp_password'])) {
      $err['msg'] = $GLOBALS['l_err_host_backup_ftp_password_empty'];
      $err['field'] = 'backup_ftp_password';
      return false;
    }
    if (empty($host['backup_ftp_root'])) {
      $err['msg'] = $GLOBALS['l_err_host_backup_ftp_root_empty'];
      $err['field'] = 'backup_ftp_root';
      return false;
    }
  }

  if ($id > 0) {
    $h = get_host_info($id);
    $domain_id = $h['domain_id'];
  } else {
    $domain_id = $host['domain_id'];
  }

  // MANDATORY: Host name not empty
  if (trim($name) == '') {
    $err['msg'] = $l_err_host_name_empty;
    $err['field'] = 'name';
    return false;
  }

  // MANDATORY: Host name unique
  if (get_host_name_exists($name, $id)) {
    $err['msg'] = "$l_err_host_name_exist ($name)";
    $err['field'] = 'name';
    return false;
  }

  // Host name correct
  if (($name != '') && (! preg_match($php_regexp_hostname, $name))) {
    $err['msg'] = "$name : $l_err_host_name";
    $err['field'] = 'name';
    return false;
  }

  // If IP not empty, must not already exists
  if ($ip != '') {
    // Host IP correct
    if (! preg_match($php_regexp_ip, $ip)) {
      $err['msg'] = " $ip : $l_err_host_ip";
      $err['field'] = 'ip';
      return false;
    }

    $ips = get_ip_used($id);
    $delegations = get_host_delegations($ip);
    // IP address not already used
    // for FTP servers, we can have two similar ips but with not same delegation (and not empty delegation)
    if (array_key_exists($ip, $ips) && (empty($backup_ftp) || empty($delegation) || in_array($delegation, $delegations))) {
      $err['msg'] = "$l_err_host_ip_exist : $ip (" . $ips["$ip"] . ")";
      $err['field'] = 'ip';
      return false;
    }
    // IP is in a network :
    if ($cgp_use['service']['network']) {
      if (!is_ip_in_an_internal_network($ip)) {
        $err['msg'] = "$l_err_host_ip_not_in_lan : $ip";
        $err['field'] = 'ip';
        return false;
      }
    }
  }

  // Host FQDN correct
  if (trim($fqdn) != '' && !preg_match($php_regexp_fqdn, $fqdn)) {
    $err['msg'] = " $fqdn : $l_err_host_fqdn";
    $err['field'] = 'fqdn';
    return false;
  }

  // XXXXXXX verifier ici + fichiers de langue
  $d = of_domain_get_domain_infos($domain_id, true);
  // MANDATORY: If windows host, domain can not be global (0)
  if ($samba == '1' && $d['global'] == '1') {
    $err['msg'] = "$l_err_host_windows_not_global";
    $err['field'] = 'samba';
    return false;
  }

  // Les contraintes sur la delegation ne sont pas valables pour le domaine global
  if (!$d['global'] && $cgp_use['property']['delegation']) {
    if(!of_delegation_check_data($host['delegation'], $d['delegation'])) {
      $err['field'] = 'delegation';
      return false;
    }
  } 
  //ip + fqdn to add host monitor
  if ( $monitor == '1' && ( $fqdn == '' || $ip == '') ){
	$err['field'] = 'supervision' ;
	$err['msg'] = "$l_err_host_monitoring";
	return false;
  }
  return true;
}

/**
 * get_host_graph_list
 * return an array of graph
 *
 * $host_fqdn string of host fqdn
 * @return array $graph_list
 */
function get_host_graph_list($host_fqdn) {
  
  $host_domain_explode = explode('.', $host_fqdn, 2 );
  $host_domain = $host_domain_explode[1];
  $host_name = $host_domain_explode[0];
  $munin_folder = '/var/www/munin/'.$host_domain;
  
  $graph_list = array();
  if (is_dir($munin_folder) ) {
    if ($munin_handler = opendir($munin_folder)) {
      while (($file = readdir($munin_handler)) !==false) {
        if (preg_match('/'.$host_fqdn.'.*day.*/', $file)){
          $desc_stat_extension = explode('-day.png', $file, -1);
          $desc_stat =  explode('-', $desc_stat_extension[0]);
          array_push( $graph_list, end($desc_stat));
        }
      }
    }
    sort($graph_list);
    return $graph_list;
  }else{
    return $graph_list;
  }
}

