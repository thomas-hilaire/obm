-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.8.1 to 0.8.2                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Update Contact table
-------------------------------------------------------------------------------
-- Add column : contact_service
ALTER TABLE Contact ADD column contact_service varchar(64) after contact_firstname;

-- Add column : contact_email2
ALTER TABLE Contact ADD column contact_email2 varchar(128) after contact_email;

-------------------------------------------------------------------------------
-- Update List table
-------------------------------------------------------------------------------
-- Add column : list_mailing_ok
ALTER TABLE List ADD column list_mailing_ok int(1) after list_email;

------------------------------------------------------------------------------
-- Update Contract table
-------------------------------------------------------------------------------
-- Add column : contract_archive
ALTER TABLE Contact ADD column contract_archive int(1) after contact_comment;

------------------------------------------------------------------------------
