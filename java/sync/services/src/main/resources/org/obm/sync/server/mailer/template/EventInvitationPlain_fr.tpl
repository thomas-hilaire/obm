Message automatique envoyé par OBM
------------------------------------------------------------------
NOUVEAU RENDEZ-VOUS !
------------------------------------------------------------------

Vous êtes invité à participer à ce rendez-vous

du              : ${start}

au              : ${end}

sujet           : ${subject}

lieu            : ${location}

organisateur    : ${organizer}

créé par        : ${creator}


:: Pour accepter : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: Pour plus de détails : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
