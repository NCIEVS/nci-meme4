Version Information
-------------------
RELEASE=4
VERSION=8.0
DATE=20060808
AUTHORITY=BAC

This release of the MEOW-MEME environment should contain everything needed to 
operate MEME client tools and services.  Notable updates include:

 o CODE_SOURCE and CODE_ROOT_SOURCE support added to mapping_errors.cgi

 o error_report.cgi has a small change to prevent certain apache errors caused
   by an empty procedure call.  This does not constitute a functional change
      
 
See INSTALL.txt for installation instructions

Old Release Information
-----------------------
RELEASE=4
VERSION=7.0
DATE=20060627
AUTHORITY=BAC

This release of the MEOW-MEME environment should contain everything needed to 
operate MEME client tools and services.  Notable updates include:

 o New MEDLINE in MID processing interface is ready.

 o SourceMetadataReport can be directed to non-default server and now
   shows database name.
 
 o Upgraded db_request.cgi.  It now tracks states of the various database
   and allows for an easy email-URL mechanism for responding to requests.
   If used properly, it can also track the most recent state of each DB.
 
 o release_maintenance.cgi has slight improvement for identifying "merged" 
   concepts.

RELEASE=4
VERSION=6.0
DATE=20060511
AUTHORITY=BAC

This release of the MEOW-MEME environment should contain everything needed to 
operate MEME client tools and services.  Notable updates include:

 o MEDLINE processing webapp for MID is now available.

 o mid_maintenance.cgi now has matrix "updater" functionality

 o release_maintenance.cgi Handle_aui_history now handles case where
   legacy CUI2 values are now wrong. Handle_atx_cui_map has more explicit 
   handling of aui prefix/length for t_delcui_$$ query

 o SQL injection issues addressed.

RELEASE=4
VERSION=5.0
DATE=20060331
AUTHORITY=BAC

This release of the MEOW-MEME environment should contain everything needed to 
operate MEME client tools and services.  Notable updates include:

 o db_request.cgi and sos.cgi updated to use the new MID Services email entries.

 o Release Maintenance bug fix for accessing "last release rank" computation.

 o Removed MEDLINE entries from MRDOC editor.

RELEASE=4
VERSION=4.0
DATE=20060314
AUTHORITY=BAC

This release of the MEOW-MEME environment should contain everything needed to 
operate MEME client tools and services.  Notable updates include:

 o DA, MR, and ST ATUI computations now use CUI instead of CONCEPT_ID and take 
   into account the attribute values.  This is handled by release_maintenance.cgi

 o Last release rank is maintained now by a release_maintenance.cgi operation.
 
RELEASE=4
VERSION=3.0
DATE=20060214
AUTHORITY=BAC

This release of the MEOW-MEME environment should contain everything needed to 
operate MEME client tools and services.  Notable updates include:

 o default db_request.cgi updated to include reg@msdinc.com

 o mapping_errors.cgi updated to support ROOT_SOURCE_AUI

 o SIMS.cgi now builds summary pages with links to insertion reports and inversion 
   proposals

 o release_maintenance.cgi "Handle_prod_mid_cleanup" procedure updated for better
   handling of attributes and relationships rebuilds, more liberal query for
   checking max CUI, and now including all sources whose "normalized" source
   is "current" in the current list.

 o release_maintenance.cgi "Handle_sims_info" updated so that MTH_UMLSMAPSETSEPRATOR
   is not listed as an MTH attribute.

RELEASE=4
VERSION=2.0
DATE=20051206
AUTHORITY=BAC

This release of the MEOW-MEME environment should contain everything needed to 
operate MEME client tools and services.  Notable updates include:

 o Changes to release_maintenance.cgi to better handle cui_history data.
   Don't allow released=N to participate in merges, handle splits more 
   explicitly, etc

 o Implementation of updated MRMAP spec with MTH_ attributes and SABs equal to 
   the SAB of the XM atom.  This affected MRMAP viewer/editor pages

 o Medline .jsp pages updated to accommodate change in processing with respect
   to release date and start date
          
 o Moved .xsl files from the java directory into the src/xsl dir

RELEASE=4
VERSION=1.0
DATE=20051028
AUTHORITY=BAC

This release of the MEOW-MEME environment should contain everything needed to 
operate MEME client tools and services.  Notable updates include:

 o release_maintenance.cgi has been debugged with respect to operations that
   manage cui/aui history, sims_info, production mid cleanup, and the process
   for setting last_release_cui values.  Also, does a better job of
   maintaining sims_info.meta_ver during pre production.

 o Some webapps changes were made.  A new JSP was created to take over
   the remaining task of code_map.cgi which was to produce descriptions of
   the various codes where needed.  This CGI is no longer used so the webapp
   was needed to take over its role.
   
 o The /webapps-meme-editors web application was created to contain applications
   used by editors (so that they can be separately authenticated).  At present
   the only app is the change password form.
   
 o default nlmumls_l@list.nih.gov addresses were replaced with the correct one
   (nlmumls-l@list.nih.gov) in the db_request.cgi
   
 o $MEME_HOME/etc in the dist_meow.zip now has a build.xml that can be used if
   ant is installed to auto-build the tables documentation into the
   web server document root at $MEME_HOME/www.
