-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.8.9 to 0.9.0                     //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='0.9.0' where obminfo_name='db_version';


-------------------------------------------------------------------------------
-- Global Preferences update
-------------------------------------------------------------------------------
DELETE FROM GlobalPref where globalpref_option='document_path';


-------------------------------------------------------------------------------
-- Update Contract tables
-------------------------------------------------------------------------------

-- Update Contract table

ALTER TABLE Contract ADD COLUMN contract_datesignature date DEFAULT 'NULL'; 
ALTER TABLE Contract ADD COLUMN contract_daterenew date DEFAULT 'NULL';
ALTER TABLE Contract ADD COLUMN contract_datecancel date DEFAULT 'NULL';
ALTER TABLE Contract ADD COLUMN contract_priority_id integer DEFAULT '0' NOT NULL;
ALTER TABLE Contract ADD COLUMN contract_status_id integer DEFAULT '0' NOT NULL;
ALTER TABLE Contract ADD COLUMN contract_kind integer DEFAULT '0' NULL;
ALTER TABLE Contract ADD COLUMN contract_format integer DEFAULT '0' NULL;
ALTER TABLE Contract ADD COLUMN contract_ticketnumber integer DEFAULT '0' NULL;
ALTER TABLE Contract ADD COLUMN contract_duration integer DEFAULT '0' NULL;
ALTER TABLE Contract ADD COLUMN contract_autorenewal integer DEFAULT '0' NULL;
ALTER TABLE Contract ADD COLUMN contract_privacy integer DEFAULT '0' NULL;


--
-- New table 'ContractPriority'
--
CREATE TABLE ContractPriority (
  contractpriority_id          serial,
  contractpriority_timeupdate  timestamp,
  contractpriority_timecreate  timestamp,
  contractpriority_userupdate  integer DEFAULT NULL,
  contractpriority_usercreate  integer DEFAULT NULL,
  contractpriority_color       varchar(6) DEFAULT NULL,
  contractpriority_order       integer DEFAULT NULL,
  contractpriority_label       varchar(64) DEFAULT NULL,
  PRIMARY KEY (contractpriority_id)
);

--
-- Dumping data for table 'ContractPriority'
--
INSERT INTO ContractPriority (contractpriority_color, contractpriority_order, contractpriority_label) VALUES ('FF0000', 1, 'Hight');
INSERT INTO ContractPriority (contractpriority_color, contractpriority_order, contractpriority_label) VALUES ('FFA0A0', 2, 'Normal');
INSERT INTO ContractPriority (contractpriority_color, contractpriority_order, contractpriority_label) VALUES ('FFF0F0', 3, 'Low');


--
-- New table 'ContractStatus'
--
CREATE TABLE ContractStatus (
  contractstatus_id     	serial,
  contractstatus_timeupdate  	timestamp,
  contractstatus_timecreate  	timestamp,
  contractstatus_userupdate  	integer DEFAULT	NULL,
  contractstatus_usercreate  	integer DEFAULT	NULL,
  contractstatus_order  	integer DEFAULT	NULL,
  contractstatus_label  	varchar(64) DEFAULT NULL,
PRIMARY KEY (contractstatus_id)
);

--
-- Dumping data for table 'ContractStatus'
--
INSERT INTO ContractStatus (contractstatus_order, contractstatus_label) VALUES (1, 'Open');
INSERT INTO ContractStatus (contractstatus_order, contractstatus_label) VALUES (2, 'Close');


-------------------------------------------------------------------------------
-- Update Incident tables
-------------------------------------------------------------------------------

ALTER TABLE Incident ADD COLUMN incident_cat1_id integer DEFAULT NULL;

ALTER TABLE Incident ADD COLUMN incident_comment text;
ALTER TABLE Incident ALTER COLUMN incident_comment SET DEFAULT NULL;
UPDATE Incident set incident_comment = incident_description;
ALTER TABLE Incident DROP COLUMN incident_description;

--
-- New table 'IncidentCategory1'
--
CREATE TABLE IncidentCategory1 (
  incidentcategory1_id          serial,
  incidentcategory1_timeupdate  timestamp,
  incidentcategory1_timecreate  timestamp,
  incidentcategory1_userupdate  integer DEFAULT NULL,
  incidentcategory1_usercreate  integer DEFAULT NULL,
  incidentcategory1_order       integer,
  incidentcategory1_label       varchar(32) DEFAULT NULL,
  PRIMARY KEY (incidentcategory1_id)
);

--
-- Dumping data for table 'IncidentCategory1'
--
INSERT INTO IncidentCategory1 (incidentcategory1_order, incidentcategory1_label) VALUES (1, 'By email / phone');
INSERT INTO IncidentCategory1 (incidentcategory1_order, incidentcategory1_label) VALUES (2, 'On site');
