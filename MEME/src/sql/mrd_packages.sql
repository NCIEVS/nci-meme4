/*****************************************************************************
*
* File:  $MRD_HOME/etc/sql/mrd_packages.sql
* Author:  Stephanie Halbeisen
*
* Remarks:  This script loads the packages in dependency order
*
* 04/22/2005 2.1.0: Release 2
* 08/01/2000 1.1.1:  load MEME packages not MRD ones
* 6/12/2000  1.1.0:  First version
*
* Version Info:
*   Release 2
*   Version 1.0
*   BAC (08/01/2000)
* 
*****************************************************************************/

-- First load register_version procedure:
CREATE OR REPLACE PROCEDURE register_version (
   release		       IN VARCHAR2,
   version		       IN VARCHAR2,
   timestamp		       IN DATE := SYSDATE,
   authority		       IN VARCHAR2,
   object_name		       IN VARCHAR2,
   comments		       IN VARCHAR2,
   enforce_flag 	       IN VARCHAR2,
   current_version	       IN VARCHAR2
)
AS
BEGIN

   DELETE FROM application_versions
   WHERE release = register_version.release
     AND version = register_version.version
     AND object_name = register_version.object_name;

   IF register_version.current_version = 'Y' THEN
      UPDATE application_versions
      SET current_version = 'N'
      WHERE object_name = register_version.object_name;
   END IF;

   INSERT INTO application_versions(
      release, version, timestamp, authority,
      object_name, comments, enforce_flag, current_version)
   VALUES (
      register_version.release, register_version.version,
      register_version.timestamp, register_version.authority,
      register_version.object_name, register_version.comments,
      register_version.enforce_flag, register_version.current_version);

   COMMIT;

END;
/

@@$MRD_HOME/etc/sql/MEME_CONSTANTS.sql
@@$MRD_HOME/etc/sql/MEME_UTILITY.sql
@@$MRD_HOME/etc/sql/MEME_SYSTEM.sql
@@$MRD_HOME/etc/sql/MEME_RANKS.sql
@@$MRD_HOME/etc/sql/MEME_APROCS.sql
@@$MRD_HOME/etc/sql/MEME_OPERATIONS.sql
@@$MRD_HOME/etc/sql/MEME_BATCH_ACTIONS.sql
@@$MRD_HOME/etc/sql/MEME_SOURCE_PROCESSING.sql
@@$MRD_HOME/etc/sql/MRD_OPERATIONS.sql
@@$MRD_HOME/etc/sql/MRD_RELEASE_OPERATIONS.sql


