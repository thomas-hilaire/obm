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




/**
 * AlertQuotaFormater formater for display warning quota mail 
 * 
 * @uses IFormater
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 LINAGORA GSO
 * @author Benoît Caudesaygues <benoit.caudesaygues@linagora.com> 
 */
class AlertQuotaFormater implements IFormater{
  private $_fields;
  private $limit = 90;
  
  /**
   * Constructor 
   * 
   * @access public
   * @return void
   */
  public function __construct() {
    $this->_fields = array();
  }

  /**
   * Add $field to the field to format 
   * 
   * @param mixed $field 
   * @access public
   * @return void
   */
  public function addField($field) {
    $this->_fields[] = $field;
  }

  /**
   *  
   * @see IFormater::format 
   **/
  public function format($object) {
    global $l_warn_quota;
    $quota = $object->mail_quota;

    $use = $object->mail_quota_use;
    $percent = $this->percentQuota($use,$quota);

    $line = '';
    foreach($this->_fields as $field) {
       $line .=self::escapeField($object->$field).";";
    }
    if($percent != '---') {
      $line .= $percent."%";
      if($percent > $this->limit) {
        $line .= ';'.self::escapeField($l_warn_quota);
      }
    } else {
      $line .= $percent;
    }
    $line .= ";\n";
    return $line;
  }

  /**
   * @see IFormater::getHeader
   */
  public function getHeader() {
    require "obminclude/lang/".$_SESSION['set_lang']."/report.inc";
    $head = '';
    foreach($this->_fields as $field) {
      if(isset(${"l_".$field}))
        $head .= self::escapeField(${"l_".$field}).";";
      else
        $head .= $field.";";
    }
    $head .= self::escapeField($l_used_percent).";";
    $head .= self::escapeField($l_warn).";";
    $head .= "\n";
    return $head;
  }

  /**
   * @see IFormater::getFooter
   */
  public function getFooter() {
    return '';
  }

  private function percentQuota($use,$quota) {
    if($quota != 0)
      $percent = round((($use * 100)/$quota),2);
    else
      $percent = '---';
    return $percent;
  }
  /**
   * Escape a field
   * 
   * @param mixed $field 
   * @static
   * @access private
   * @return void
   */
  private static function escapeField($field) {
    return '"'.addcslashes($field,";\\\"").'"';
  }
}
