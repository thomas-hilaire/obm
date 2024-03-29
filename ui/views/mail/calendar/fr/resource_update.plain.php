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


?>
Message automatique envoyé par OBM
------------------------------------------------------------------
MODIFICATION D'UNE RESERVATION DE RESSOURCE !
------------------------------------------------------------------

Le rendez-vous <?php echo $title; ?>, initialement prévu du <?php echo $old_start; ?> au <?php echo $old_end; ?>, (lieu : <?php echo $old_location; ?>),
a été modifié et se déroulera du <?php echo $start; ?> au <?php echo $end; ?>, (lieu : <?php echo $location; ?>).

:: Pour plus de détails : 
<?php echo $this->host; ?>/calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>


:: Pour accepter les modifications :
<?php echo $this->host; ?>/calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser les modifications : 
<?php echo $this->host; ?>/calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=DECLINED
