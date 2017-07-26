Version Information
-------------------
RELEASE=4
VERSION=7.0
DATE=20060511
AUTHORITY=BAC

This release of the MEME Server should contain everything needed to operate MEME
tools and services.  Notable updates include:

 o assign_luis.csh automatically creates LUI assignment reports.
 
 o insert_attributes.csh added.  Used for adding default STYs after
   a test insertion.
  
 o load_mrdoc.csh and load_section.csh now properly handle
   changes in tty_class
   
 o matrixinit.pl now only reports "activities" for latest
   run, instead of for all runs.
   
 o meme.prop should be updated to include MedlineService
 
 o mail.pl script added to let any component send mail 

 o MEME_INTEGRITY_PROC updated msh MUI checks to use
   classes.source_cui instead of ATN='MUI' attributes
   
 o MEME_SOURCE_PROCESSING.map_obsolete_rels does a better
   job of accounting for "source replacement"
  
 o mthtm.pl updated to call set_preference after delete operations.
 
 o nightly_tasks.csh should be updated to NOT perform cui assignment
   Also, it should make a set_preference call
   
 o process_medline_data.csh properly analyzes coc_headings and coc_subheadings
 
 o SQL injection issues addressed.
      
See INSTALL.txt for installation instructions

Old Release Information
-----------------------
RELEASE=4
VERSION=6.0
DATE=20060331
AUTHORITY=BAC

This release of the MEME Server should contain everything needed to operate MEME
tools and services.  Notable updates include:

 o Attributes table now has attribute_name, attribute_value index
 
 o Default meme.prop_orig file was updated to remove properites relating to sending email
   as the new regime uses MID services instead of properties. 

 o Handle source replacement properly for SEMANTIC_TYPEs
 
 o New load_mrdoc.csh for testing MRDOC.RRF metadata load before getting too
   far into an insertion.

 o MEME_RANKS.set_ranks has only classes_flag set to YES by default.
 
 o MID Services enabled email configuration for CGI tools and MEME server.  Changes
   are backwards compatible.
 
 o Small bug fixes to Aux/Core data test suites and DT_PN3 test suite.
    
RELEASE=4
VERSION=5.0
DATE=20060314
AUTHORITY=BAC

This release of the MEME Server should contain everything needed to operate MEME
tools and services.  Notable updates include:

 o ActionEngine default implementation's logAction method was updated to
   throw an exception upon failure that would not include an unserializable
   object in the details (thus preventing it from being properly communicated
   back to the client).
 
 o Client test suites completely implemented.
 
 o Content view member representation was changed (see MRD notes).  
   MEMEConnection methods for maintianing content view members were updated
   to use the new logic.
   
 o load_section.csh calls were slightly re-ordered.  We discovered that large
   numbers of empty concepts were being created by insertions and determined
   that source replacement was causing it.  We now compute source replacement
   before running core_table_insert on concepts so that we know which concept
   ids the new atoms really are going to use.

 o Medline handling updated to analyze coc_headings and coc_subheadings after
   reloading.  Also, the start year was changed from 1910 to 1776.
 
 o memerun.pl was updated to return the correct status value from the
   java call, thus enabling the merge engine to properly report errors back
   up to the recipe script.
 
 o MergeEngineClient no longer requires the user to respond upon an error,
   it simply reports it and returns a non-zero status value.
 
 o MID Services default file was updated with a couple examples of email
   lists.  We hope to use this mechanism in the future to program the various 
   tools that have to send email to use this indirection instead of hard-coding
   them.
  
 o Update releasability routine of MEME_SOURCE_PROCESSING was updated
   to disable SRC relationships connected to the old-version SRC/V.. atoms.
   
 o xreports.pl handles -url parameters properly.  In particular it handles the
   case where a -url parameter is not used and the properties file does not
   contain any corresponding entries.

RELEASE=4
VERSION=4.0
DATE=20060214
AUTHORITY=BAC

This release of the MEME Server should contain everything needed to operate MEME
tools and services.  Notable updates include:

 o ActionClient/ActionService bug fix with respect to a parameter name passed by
   getWorkLogsByType.
   
 o Action synchronization implemented for OCCS parallel phase 4a operations
 
 o Integrity Check test suite completely implemented
 
 o MEME Client API mostly implemented
 
 o MEME_SOURCE_PROCESSING.resolve_stys has more efficient queries for Oracle 9i.a
 
 o MEME_SOURCE_PROCESSING.assign_string_ids now handles multiple languages in the
   same source_string_ui table better.
 
 o MEME_SOURCE_PROCESSING.insert_ranks now handles an additional parameter
   that prevents set_ranks from running.  The MEMEConnection can pass this
   switch when adding a termgroup to prevent it from recomputing the ranks
   if not desired.

 o MEMEConnection.java, MEME_OPERATIONS.sql, and meme_tables.sql were updated
   to accommodate 8 digit AUIs.

 o Molecular Action test suite completely implemented

RELEASE=4
VERSION=3.0
DATE=20060106
AUTHORITY=BAC

This release of the MEME Server should contain everything needed to operate MEME
tools and services.  Notable updates include:

 o MEMEToolkit has facilities for managing an "XML log"

 o MEME_INTEGRITY_PROC.sql bug fix for "styisa" procedure. Last bug fix left an
   errant quote character.
   
 o HTTPRequestClient logs XML documents being passed back/forth
 
 o increase/decrease font actions use ^+ and ^- accelerator keys.

 o MEME_SOURCE_PROCESSING.sql assign_meme_ids has a check to make sure 
   source_stringtab min(string_id) = 1 so that we do not re-assign string_ids in a 
   range that is too high.
   
 o LongAttributeMapper.java doesn't report an exception when stated text_total 
   doesn't equal the length of the full text value.  It reports the error so 
   adminstrators will see it but does not stop editing.

 o UTF-8 is handled more explicitly in these ways:
   . All perl scripts that use open now have "use open ':utf8';".  
   . All that open sockets now use binmode(SOCK,":utf8"). 
   . All .csh scripts that call anonymous perl scripts that use "open" 
     now use the "use open :utf8" pragma, also must import env.pl 
   . MEMEDataSourceFactory passes LANGUAGE and CHARSET properties to 
     Oracle JDBC driver to make sure UTF8 is read properly from the DB.
 
 o Release maintenance properly computes RMETA for sources that are updated 
   more than once per release cycle. Also, small bug in cui_history fixed.
   
RELEASE=4
VERSION=2.0
DATE=20051206
AUTHORITY=BAC

This release of the MEME Server should contain everything needed to operate MEME
tools and services.  Notable updates include:

 o MEME_INTEGRITY_PROC.sql bug fix for "styisa" procedure.

 o Change to mthtm.pl to insert MTH/TM atoms with suppressible=Y instead of E.

 o xreports.pl encodes URL values better, and also fixes a problem with -lat
   parameter that caused the logic to be reversed.

 o clean_regen_qa_data.sql added.  Used to refresh QA concepts.

 o Implementation of updated MRMAP spec with MTH_ attributes and SABs equal to 
   the SAB of the XM atom.  

 o Update to classes_to_strings.csh to handle UTF8 properly when using Perl5.6 
   or Perl 5.8.

 o MEME_SOURCE_PROCESSING.sql queries having to do with SEMANTIC_TYPE attributes 
   where updated to use table scans instead of index.  

 o meme_views.sql has an improved query for the "chemical concepts" view.

RELEASE=4
VERSION=1.0
DATE=20051028
AUTHORITY=BAC

This release of the MEME Server should contain everything needed to operate MEME
tools and services.  Notable updates include:

 o MEME_SOURCE_PROCESSING includes progress monitoring mechanism for
   long running insertion/recipe operations.  Also assign_ruis and
   assign_atuis do a better job with rels/atts attached to unreleasable
   atoms.
  
 o ReportsGenerator was updated to include RXCUI values in the reports
   and to also fix a bug with the links thta appear on meow.  It also now
   removes links for cases where no URL for a link type is specified (e.g.
   if no CUI link URL is specified, CUIs will not be linked).
   
 o dump_mid.pl does not overlap DBI connections, preventing a redundant error
 
 o MEME_OPERATIONS had some formatting cleanup and also now has a section
   to preserve last_assigned_cui assignments in cases where the LRC is not
   null and doesn't match the LAC.
   
 o New integrity check MGV_RXCUI has been created.  It prevents concepts from
   merging if they will bring togther two different RXCUIs that are not already
   merged.
 
 o Fix to MEME_INTEGRITY_PROC.sql styisa procedure.  It isn't being actively used
   but now it can be.
