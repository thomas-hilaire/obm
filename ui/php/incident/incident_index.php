<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : incident_index.php                                           //
//     - Desc : Incident Index File                                          //
// 2002-03-14 : Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
//  $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)     -- search fields  -- show the Incident search form
// - search              -- search fields  -- show the result set of search
// - new                 -- $param_contract-- show the new Incident form
// - detailconsult       -- $param_incident-- show the Incident detail
// - detailupdate        -- $param_incident-- show the Incident detail form
// - insert              -- form fields    -- insert the Incident 
// - update              -- form fields    -- update the Incident
// - delete              -- $param_incident-- delete the Incident
// - priority_insert     -- form fields    -- insert the category
// - priority_update     -- form fields    -- update the category
// - priority_checklink  --                -- check if category is used
// - priority_delete     -- $sel_cat1      -- delete the category
// - status_insert       -- form fields    -- insert the category
// - status_update       -- form fields    -- update the category
// - status_checklink    --                -- check if category is used
// - status_delete       -- $sel_cat1      -- delete the category
// - category1_insert    -- form fields    -- insert the category
// - category1_update    -- form fields    -- update the category
// - category1_checklink --                -- check if category is used
// - category1_delete    -- $sel_cat1      -- delete the category
// - category2_insert    -- form fields    -- insert the category
// - category2_update    -- form fields    -- update the category
// - category2_checklink --                -- check if category is used
// - category2_delete    -- $sel_cat2      -- delete the category
// - display             --                -- display, set display parameters
// - dispref_display     --                -- update one field display value
// - dispref_level       --                -- update one field display position
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "incident";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_incident_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("incident_query.inc");
require("incident_display.inc");
require_once("incident_js.inc");
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/javascript/calendar_js.inc");

$uid = $auth->auth["uid"];

if ($action == "") $action = "index";
update_last_visit("incident", $params["incident_id"], $action);
get_incident_action();
$perm->check_permissions($module, $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_incident_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_incident_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_incident_search_form($params);
  $display["result"] = dis_incident_search_list($params);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_incident_form($action,$params);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($params["incident_id"] > 0) {
    $display["detail"] = dis_incident_consult($params);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($params["incident_id"] > 0) {
    $display["detailInfo"] = display_record_info($inc_q);
    $display["detail"] = dis_incident_form($action,$params);
  } else {
    $display["msg"] = display_err_msg($l_err_reference);
  }
  
} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_incident_form($params)) {
    $params["incident_id"] = run_query_incident_insert($params);
    if ($params["incident_id"] > 0) {
      $display["msg"] = display_ok_msg("$l_incident : $l_insert_ok");
      $display["detail"] = dis_incident_consult($params);
    } else {
      $display["msg"] = display_err_msg("$l_incident : $l_insert_error");
      $display["detail"] = dis_incident_form($action,$params);
    }
  } else {
    $display["msg"] = display_warn_msg($err_msg);
    $display["detail"] = dis_incident_form($action,$params);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_incident_form($params)) {
    $ret = run_query_incident_update($params);
    if ($ret) {
      $display["msg"] = display_ok_msg("$l_incident : $l_update_ok");
      $display["detail"] = dis_incident_consult($params);
    } else {
      $display["msg"] = display_error_msg("$l_incident : $l_update_error");
      $display["detail"] = dis_incident_form($action,$params);
    }
  } else {
    $display["msg"] = display_warn_msg($err_msg);
    $display["detail"] = dis_incident_form($action,$params);
  }
 
} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_incident($params["incident_id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_incident($params["incident_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_incident_consult($params);
  }

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_incident($params["incident_id"])) {
    $retour = run_query_incident_delete($params["incident_id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_incident : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_incident : $l_delete_error");
    }
    $display["search"] = dis_incident_search_form($params);
    if ($set_display == "yes") {
      $display["result"] = dis_incident_search_list($params);
    } else {
      $display["msg"] .= display_info_msg($l_no_display);
    }
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_incident_consult($params);
  }

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_incident_admin_index();

} elseif ($action == "priority_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("incident", "priority", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_priority : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_priority : $l_insert_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "priority_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("incident", "priority", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_priority : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_priority : $l_update_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "priority_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("incident", "priority", $params, "mono");

} elseif ($action == "priority_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("incident", "priority", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_priority : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_priority : $l_delete_error");   
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "status_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("incident", "status", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_status : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_status : $l_insert_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "status_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("incident", "status", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_status : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_status : $l_update_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "status_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("incident", "status", $params, "mono");

} elseif ($action == "status_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("incident", "status", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_status : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_status : $l_delete_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "category1_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("incident", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_insert_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "category1_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("incident", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_update_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "category1_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("incident", "category1", $params, "mono");

} elseif ($action == "category1_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("incident", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_delete_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "category2_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("incident", "category2", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category2 : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category2 : $l_insert_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "category2_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("incident", "category2", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category2 : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category2 : $l_update_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "category2_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("incident", "category2", $params, "mono");

} elseif ($action == "category2_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("incident", "category2", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category2 : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category2 : $l_delete_error");
  }
  $display["detail"] .= dis_incident_admin_index();

} elseif ($action == "document_add") {
///////////////////////////////////////////////////////////////////////////////
  $params["incident_id"] = $params["ext_id"];
  if ($params["doc_nb"] > 0) {
    $nb = run_query_global_insert_documents($params, "incident");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  $display["detail"] = dis_incident_consult($params);

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "incident", 1);
  $display["detail"] = dis_incident_display_pref($prefs); 

} else if($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "incident", 1);
  $display["detail"] = dis_incident_display_pref($prefs);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "incident", 1);
  $display["detail"] = dis_incident_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
update_incident_action();
$display["head"] = display_head($l_incident);
$display["header"] = display_menu($module);
$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Contact parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_incident_params() {
  global $tf_color, $contract_new_id;
  
  // Get global params
  $params = get_global_params("Incident");
  
  get_global_params_document($params);

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Incident actions
///////////////////////////////////////////////////////////////////////////////
function get_incident_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_admin, $l_header_display;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

//  Index
  $actions["incident"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/incident/incident_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                	);

//  Search
  $actions["incident"]["search"] = array (
    'Url'      => "$path/incident/incident_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                	);

//  New
  $actions["incident"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/incident/incident_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','index','detailconsult', 'insert', 'update','display','delete') 
                    		       );

//  Detail Consult
  $actions["incident"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/incident/incident_index.php?action=detailconsult&amp;incident_id=".$params["incident_id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate')
                                	       );

//  Detail Update
  $actions["incident"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/incident/incident_index.php?action=detailupdate&amp;incident_id=".$params["incident_id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update')
                                     	        );

//  Insert
  $actions["incident"]["insert"] = array (
    'Url'      => "$path/incident/incident_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

//  Update
  $actions["incident"]["update"] = array (
    'Url'      => "$path/incident/incident_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

  // Check Delete
  $actions["incident"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/incident/incident_index.php?action=check_delete&amp;incident_id=".$params["incident_id"],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                     );

//  Delete
  $actions["incident"]["delete"] = array (
    'Url'      => "$path/incident/incident_index.php?action=delete&amp;incident_id=".$params["incident_id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );
//  Admin
  $actions["incident"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/incident/incident_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

//  Priority insert
  $actions["incident"]["priority_insert"] = array (
    'Url'      => "$path/incident/incident_index.php?action=priority_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Priority update
  $actions["incident"]["priority_update"] = array (
    'Url'      => "$path/incident/incident_index.php?action=priority_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Priority Check Link
  $actions["incident"]["priority_checklink"] = array (
    'Url'      => "$path/incident/incident_index.php?action=priority_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Priority delete
  $actions["incident"]["priority_delete"] = array (
    'Url'      => "$path/incident/incident_index.php?action=priority_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Status insert
  $actions["incident"]["status_insert"] = array (
    'Url'      => "$path/incident/incident_index.php?action=status_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Status update
  $actions["incident"]["status_update"] = array (
    'Url'      => "$path/incident/incident_index.php?action=status_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Status Check Link
  $actions["incident"]["status_checklink"] = array (
    'Url'      => "$path/incident/incident_index.php?action=status_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Status delete
  $actions["incident"]["status_delete"] = array (
    'Url'      => "$path/incident/incident_index.php?action=status_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category1 insert
  $actions["incident"]["category1_insert"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category1_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category1 update
  $actions["incident"]["category1_update"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category1_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category1 Check Link
  $actions["incident"]["category1_checklink"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category1_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category1 delete
  $actions["incident"]["category1_delete"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category1_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category2 insert
  $actions["incident"]["category2_insert"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category2_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category2 update
  $actions["incident"]["category2_update"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category2_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category2 Check Link
  $actions["incident"]["category2_checklink"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category2_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

//  Category2 delete
  $actions["incident"]["category2_delete"] = array (
    'Url'      => "$path/incident/incident_index.php?action=category2_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Document add
  $actions["incident"]["document_add"] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
						);

//  Display
  $actions["incident"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/incident/incident_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all') 
                                      	   );

//  Display Preference
  $actions["incident"]["dispref_display"] = array (
     'Url'      => "$path/incident/incident_index.php?action=dispref_display",
     'Right'    => $cright_read,
     'Condition'=> array ('None') 
                                      	   );

//  Display level
  $actions["incident"]["dispref_level"] = array (
     'Url'      => "$path/incident/incident_index.php?action=dispref_level",
     'Right'    => $cright_read,
     'Condition'=> array ('None') 
                                      	   );

}


///////////////////////////////////////////////////////////////////////////////
// Incident Actions URL updates (after processing, before displaying menu)  
///////////////////////////////////////////////////////////////////////////////
function update_incident_action() {
  global $params, $actions, $path;
  
  $id = $params["incident_id"];
  if ($id > 0) {
    // Detail Consult
    $actions["incident"]["detailconsult"]['Url'] = "$path/incident/incident_index.php?action=detailconsult&amp;incident_id=$id";
    $actions["incident"]["detailconsult"]['Condition'][] = 'insert';
    
    // Detail Update
    $actions["incident"]["detailupdate"]['Url'] = "$path/incident/incident_index.php?action=detailupdate&amp;incident_id=$id";
    $actions["incident"]["detailupdate"]['Condition'][] = 'insert';
    
    // Check Delete
    $actions["incident"]["check_delete"]['Url'] = "$path/incident/incident_index.php?action=check_delete&amp;incident_id=$id";
    $actions["incident"]["check_delete"]['Condition'][] = 'insert';
  }

}


?>
