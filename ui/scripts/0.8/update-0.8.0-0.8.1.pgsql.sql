-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.8.0 to 0.8.1                     //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update UserObmPref table
-------------------------------------------------------------------------------
-- Change option set_todo
UPDATE UserObmPref set userobmpref_value='todo_priority' where userobmpref_option='set_todo';
