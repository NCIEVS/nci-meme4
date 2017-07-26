/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class provides package version information
 * for gov.nih.nlm.meme.server package.
 *
 * <b>History</b>
 * <dl>
 * <dt>08/15/2005</dt>
 * <dd>Suppressible implementation.
 * </dd>
 * <dt>06/17/2005</dt>
 * <dd>Improved {@link MappingService}.
 * {@link ReportsGenerator} now supports additional parameters for URLs in HTML style reports.
 * </dd>
 * <dt>03/25/2005</dt>
 * <dd>{@link ThreadPool} handles more exceptional cases, allowing * <dd>{@link ThreadPool} handles more exceptional cases, allowing
 * the thread to return to the pool.
 * {@link HTTPRequestListener} better reports rejected requests.
 * {@link SessionTimeoutThread} now runs the garbage collector once
 * per minute and writes the total free server memory to the log once per
     * five minutes. <code>InsertionService</code> has upgrades, not important here.
 * </dd>
 * <dt>03/11/2005</dt>
 * <dd>{@link ReportsGenerator} correctly handles action reports using
 * new model for undo.  New <code>InsertionService</code>.
 * </dd>
 * <dt>01/25/2005</dt>
 * <dd> * <dd>
 * {@link SessionContext} tracks latest request.
 * {@link HTTPRequestListener} sets request_id for reconnect requests.
 * {@link MEMEApplicationServer} supports a new special type of request
 * for reconnecting a current client call with a previous request.  This
 * is to assist in recovering from network failures.
 *
 * </dd>
 * <dt>01/14/2005</dt>
 * <dd>
 * {@link ActionSequences} uses <code>getInverseAction</code> to undo
 * molecular actions.
 *
 * {@link ActionService} supports passing work_id from batch/macro actions
 * through to MBA procedures.  Also supports force undo/redo of molecular
 * actions and generic undo/redo of any logged action.
 *
 * {@link AuxiliaryDataService} uses objects instead of identifiers when
 * making calls to get activity logs and errors for transactions and work logs.
 *
 * {@link MaintenanceService} supports more functions, and a work_id parameter
 * for assign cui actions.
 *
 * {@link ReportsGenerator} will show SAB for definitions and SOS attributes.
 * Also, now reports CONTEXT attributes below RELATIONSHIPS.
 *
 * </dd>
 * <dt>12/13/2004</dt>
 * <dd>
 * {@link ActionService} supports getting {@link gov.nih.nlm.meme.action.WorkLog} objects by type.
 * {@link AdminService} supports get/set operations for system status info.
 * {@link AuxiliaryDataService} supports <code>setTermgroup</code>.
 * {@link MaintenanceService} supports a variety of maintenance tasks from
 * running queries or scripts to assigning CUIs or running the matrix initiailzer.
 * This should eventually support all of the $MEME_HOME/bin/ scripts.
     * {@link ReportsGenerator} support concept level definitions and SOS attributes,
 * and better support for foreign MUIs.
 * </dd>
 *
 * <dt>11/05/2004</dt>
 * <dd>Minor documentation changes.
 *
 *    Services updated where needed to use new action model.
 *
 *    {@link MEMERelaEditorService} has new method for inserting RELA that
 *    includes rank info.
 *
 *    {@link ActionService} supports accessing of work and activity logs.
 *
 *    {@link ConceptMappingService} supports new methods for adding/removing
 *    MRCUI entries.
 *
 *    {@link AuxiliaryDataService} supports new methods for editing
 *    Sources, Termgroups, Editor information, Languages, and semantic types.
 *
 *    {@link ReportsGenerator} has minor fix to deal with "missing MUI" issue.
 * </dd>
 * <dt>09/19/2004</dt>
 * <dd>{@link FinderService} does a better job with words that norm to nothing.
 * {@link ContentViewService} updated to new data model.  New service:
 * {@link ConceptMappingService}.  {@link HTTPRequestListener} buffers input/output
 * with bigger buffer and has improved javadoc.  {@link MEMEApplicationServer}
 * does a better job of handling data sources when sessions are terminated.
 * </dd>
 * <dt>07/20/2004</dt>
 * <dd>{@link ContentViewService} supports the <code>setContentView</code>
 * function.
 * </dd>
 * <dt>07/09/2004</dt>
 * <dd>New {@link ContentViewService} for managing content view changes.
 * </dd>
 * <dt>06/14/2004</dt>
 * <dd>{@link ReportsGenerator} supports concept level DEFINITION and SOS attributes.
 *     <code>RxNormService</code> fixes a bug that allowed dummy SCD names to be added as RxNorm graphs.
 *     {@link CoreDataService} now supports the client side get inverse relationship function.
 *     {@link ActivityMonitor} should track performance statistics in milliseconds instead of seconds.
 * </dd>
 * <dt>05/06/2004</dt>
 *         <dd>{@link CoreDataService} and {@link ReportsGenerator} now support
 *         explicitly including or excluding a list of languages.
 *         Minor changes to {@link FinderService} and {@link ActivityMonitor}
     *         to handle exceptions from MEME server based on bad parameter passing.
 *         </dd>
 * <dt>04/19/2004</dt>
 *         <dd>{@link FinderService} supports searches based on releasability.
 *         </dd>
 * <dt>04/19/2004</dt>
 *         <dd>{@link CoreDataService} now supports reading/not reading non-ENG atoms.
 *         {@link ReportsGenerator} now supports reading/not reading non-ENG atoms for
 *          report purposes.  {@link ActionService} now uses passed in transaction id and
 *          work id values.
 *
 *         {@link ThreadPool} errors now not fatal.
 *         <code>RxNormService</code> no longer handles its own errors and its errors are now not fatal.
 *         {@link ActionSequences} no longer handles its own errors and its errors are now not fatal.
 *         </dd>
 * <dt>03/17/2004</dt>
     *         <dd>Undo and redo operations now support authorities (instead of null
     *         authority).  <code>RxNormService</code> now finds ingredients based on
 *         case insensitive strings instead of case sensitive ones.
 *         Finally, {@link ActivityMonitor} makes use of sort area size and
 *          hash area size.
 *         <code>RxNormService</code> bug fix.
 *         </dd>
 * <dt>02/19/2004</dt>
 *         <dd>{@link MergeEngineService} now handles molecule_id and work_id better.
 *              Small bug fix {@link ReportsGenerator}.
 *         </dd>
 * <dt>02/06/2004</dt>
 *         <dd><code>RxNormService</code> now handles
 *         {@link gov.nih.nlm.meme.exception.StaleDataException} better.
 *         {@link CoreDataService} extended to support new client functions.
 *         Of particular interest is the ability to read relationships and
 *         context relationships in pieces instead of all at once.
 *         {@link HTTPRequestListener} does not reuse serializer objects,
 *         it also provides better handling of exceptions that are difficult
 *         to serialize and user passwords will not appear in the log anymore
 *         upon authentication requests.
 *         </dd>
 * <dt>01/20/2004</dt>
 *         <dd>{@link MEMEApplicationServer} first checks that context object
 *         is not null (in <code>processRequest</code>) before attempting to
 *         synchronize on it.
 *         </dd>
 * <dt>12/29/2003</dt>
 *         <dd>{@link MergeEngineService} uses progress mechanism.
 *         </dd>
 * <dt>12/19/2003</dt>
     *         <dd>{@link MIDDataSourcePool} compares data source names in lowercase
 *         when deciding if a data source is returnable.
 *         </dd>
 * <dt>12/16/2003</dt>
     *         <dd>In response to an error about not being able to read the username
     *         of a data source, we fixed a final problem that occurrs when a session
 *         is manually terminated.  Because the SessionContext.terminateSession method
 *         was not setting the session data source to null, it was allowing the data
 *         source to be closed, and then later accessed (thus causing the problem).
 *         <code>RxNormService</code> was also updated to properly handle the ingredient
 *         level relationships allowing an editor to simply change a precise ingredient
 *         without leaving duplicate rels laying around.
 *         {@link ReportsGenerator} now supports a restriction on the number of
 *         reviewed relationships shown (requested by TPW) .
 *         </dd>
 * <dt>12/15/2003</dt>
 *         <dd>{@link ReportsGenerator} has improved code for generating action
 *         documnts. {@link MappingService} is now implemented and tested.
 *         <code>RxNormService</code> better manages ingredient-level relationships.
 *         {@link SessionContext} now tracks requests and data-sources in a
     *         thread-specific way, but still supports the notion of a context using
 *         the same data source connection across non-simultaneous requests.
 *         {@link MEMEApplicationServer} was updated to make use of the new
 *         {@link SessionContext} architecture.  These changes should allow
 *         the server to be completely thread-safe, allowing many requests to
     *         all access the server using the same session_id without any problems.
 *         </dd>
 *
 * <dt>12/04/2003</dt>
     *         <dd>Small change to {@link MEMEApplicationServer} to avoid a potential
 *         null pointer exception, and another change to make it use
 *         {@link gov.nih.nlm.meme.exception.ExpiredSessionException}.
 *         Fix to {@link Statistics} so that when
 *         the {@link SessionTimeoutThread} terminates a session, the requests
 *         can be properly found without throwing null pointer exceptions.
 *         </dd>
 * <dt>12/01/2003</dt>
     *         <dd>{@link ActionService} passes back more information to the client,
 *         namely the <code>molecule_id</code>. Also the undo/redo procedures
 *         were updated to perform better if the action to undo/redo is null.
     *         {@link ActionSequences} was fixed, recent changes to molecular actions
 *         broke it.  {@link CoreDataService} now provides access to dead
 *         information.  {@link MEMEApplicationServer} was updated to allow
 *         multiple requests to be processed under the same session at the
 *         same time, this included additions to rework how the session removes
 *         the request object when processing is finished.
 *         Before, the "current request" was saved as part of
 *         the "session context" object, which meant that if two requests with
 *         the same session id entered the system at the same time, there would
 *         be contention for setting the "current request" variable.  This was
     *         fixed by saving all requests in the session context hashmap and keying
 *         it on <code>Thread.currentThread()</code>. {@link SessionContext}
 *         was updated to reflect these changes, as well as supporting -1
 *         as a timeout value meaning "no timeout".
 *         </dd>
 * <dt>11/07/2003</dt>
 *         <dd>{@link MEMEApplicationServer} and {@link HTTPRequestListener}
     *         now include authentication information in error messages, so it should
 *         be easier to see what editor is having a problem. {@link ActionService}
 *         returns the molecule_id to the action client.
 *         </dd>
 * <dt>10/22/2003</dt>
 *         <dd>A bug in <code>RxNormService</code> was allowing duplicate
 *         components under just the right circumstances.  This was fixed.
 *         </dd>
 * <dt>10/22/2003</dt>
 *         <dd>{@link ReportsGenerator} shows SNOMEDCT concept status.
 *         </dd>
 * <dt>10/15/2003</dt>
 *         <dd>{@link HTTPRequestListener} does a better job of reporting
 *         unexpected exceptions, and it no longer sends email on
 *         "broken pipe" exceptions. {@link MEMEApplicationServer} handles
 *         the case where a session changes to a new data source in which
 *         case the old one should be returned.  We also handle the case
 *         where an exception occurs while handling a request with an active
 *         session;  this was fixed to prevent the data source from being
 *         prematurely returned to the pool.
 *         </dd>
 * <dt>10/01/2003</dt>
 *         <dd>{@link ReportsGenerator} had a minor change to the DEF and SOS
 *         sections, an extra line break was added.
 *         </dd>
 * <dt>09/23/2003</dt>
 *         <dd>{@link HTTPRequestListener} was changed to better handle
 *         exceptions, but one cost is that it no longer emails when errors
 *         occur.  This was fixed.  {@link ActionService} was upgraded to
 *         pass back a violations vector upon a molecular "do".
 *         </dd>
 * <dt>09/05/2003</dt>
 *         <dd><code>RxNormService</code> was not populating rx norm atoms
 *         before removing them in the <code>removeGraph</code> method.
 *         This caused a problem with the RxNorm editor "Apply To Concept"
 *         functionality.  The problem was fixed by always populating
 *         the atom before removing the graph.  Also, LEXICAL_TAG attributes
 *         should be inserted as releasable if the value is TRD.
 *         {@link ReportsGenerator} only uses &amp;nbsp; if there is more
 *         than one space.
 *         </dd>
 * <dt>08/25/2003</dt>
 *         <dd>{@link ReportsGenerator} was changed to no longer print the
 *          leading spaces before concept ids in the relationships section
 *          (for HTML reports). a better solution will be sought.
 *         </dd>
 * <dt>08/19/2003</dt>
 *         <dd>{@link MergeEngineService} strips off the ENG- when inserting
 *         demotions for a MID merging set.
     *         <code>RxNormService</code> includes a small bug fix to the only change
 *         to a graph to be an addition of an SY where one was missing.
 *         {@link ReportsGenerator} no longer generates concept reports with
 *         <pre> tags, instead it uses a monospace type with &nbsp; characters
 *         to allow line wrapping in the reports.
 *         {@link ActionService} and {@link MIDDataSourcePool} now support
 *         the ability to switch the "default" mid. Either an actual db or
 *         a mid service may be specified.
 *         </dd>
 * <dt>08/11/2003</dt>
 *         <dd><code>RxNormService</code> had a bug fix relating to the precise
 *         ingredients being inserted into the wrong concept.
 *         {@link ReportsGenerator} now produces
 *         {@link gov.nih.nlm.meme.action.ActionReport} objects, and
 *         it includes legacy SNOMEDID and CTV3ID codes on the reports.
 *         </dd>
 * <dt>08/01/2003</dt>
 *         <dd>{@link ReportsGenerator} was updated to show relationships
 *         as unreleasable if the atoms they are connected to are unreleasable.
 *         A bug in the <code>getActionReport</code> method was also fixed
 *         allowing action reports for things like the INSERT action of a
 *         relationship that is now deleted to still work properly.
 *         <code>RxNormService</code> was upgraded to support the SBDF and SCDF atoms.
 *         Additionally, we rewrote the <code>applyChanges</code> method to
 *         streamline the operation, and we extended what is allowed to
 *         appear on the RxNorm editor worklist frame to include unreleasable
     *         atoms and NLM02/OCD and NLM02/OBD atoms.  Since "hide unconnected CDs"
 *         is available, there is no risk of too many things being on the worklist.
     *         <code>RxNormService</code> also handles the case of an NLM02/SY having
 *         a dose form different from its NLM02/SBD without failing.
 *         </dd>
 * <dt>07/16/2003</dt>
 *         <dd>Minor changes to {@link ReportsGenerator}.
 *         Better use of date formats in {@link AdminService} for
 *         transaction logs.  <code>RxNormService</code> has a couple
 *         of bug fixes to parseInt "" as 0, and also apply to
 *         concept really gets rid of EVERYTHING that's
 *         <code>gov.nih.nlm.meme.common.RxNormAtom</code>.
 *
 *         </dd>
 * <dt>06/19/2003</dt>
 *         <dd>{@link gov.nih.nlm.meme.client.AdminClient} looks up the concept ids involved in a
 *         molecular action when generating a transaction log report.
 *         <code>RxNormService</code> will remove unreleasable normal forms
 *         (and their graphs) when "apply to concept" is used.  Additionally
 *         the ingredient lookup routines ensure that graphs are connected
 *         to NLM02 owned ingredient atoms (NLM02/IN,NLM02/BN) which match
 *         the string of the highest ranking non-MTH/MM,TM atom in the concept.
 *         {@link ActionService} reports more information about ids of newly
 *         inserted data back to the client so it is available for use by the
 *         editing interface.
 *         </dd>
 * <dt>06/06/2003</dt>
 *         <dd>{@link AuxiliaryDataService} supports better access to
 *         integrity system objects. {@link HTTPRequestListener} passes the
 *         request a reference to the socket writer, for services like
 *         {@link AdminService} which can shutdown the server.
 *         {@link AdminService} supports a kill server function that
 *         does not wait for threads to finish running. {@link ServerToolkit}
 *         was upgraded to support the {@link AdminService} changes.
 *         {@link SessionContext} no longer tracks the socket writer,
 *         now it is tracked by the request object. <code>RxNormService</code>
 *         looks up NLM02 only ingredients, and NLM02 only brand names.
 *         {@link ReportsGenerator} now returns a sensible message if
 *         <code>getActionReport</code> is passed a bogus id.
 *         {@link ActionSequences} updated to reflect progress correctly
 *         in sequence 2, and the log produced shows concept ids for the
 *         actions performed.  {@link ActionService} reports transaction
 *         ids better for batch actions.
 *         </dd>
 * <dt>05/21/2003</dt>
 *          <dd>{@link ReportsGenerator} was updated to show the sources of
 *          both atoms involved in a LEXICAL_RELATIONSHIP. Any services
 *          that handle CGI style requests now report HTTP "Expires" headers
 *          to prevent clients from cacheing data inappropriately.
 *          These include: {@link ActivityMonitor}, {@link AdminService},
 *          {@link CGIShutdownServer}.
 *          <code>RxNormService</code> always returns worklist data for a
 *          CD atom, even if there is a problem reading the SCD subgraph.
 *          {@link ActionSequences} now record progress information.
 *          </dd>
 * <dt>05/16/2003</dt>
 *          <dd>{@link MergeEngineService} sets <code>released</code> when
 *          inserting demotions.  {@link ReportsGenerator} shows
 *          the source of both atoms involved in lexical relationships.
     *          <code>RxNormService</code> no longer creates atoms with E-XXX sources
 *          instead it uses NLM02. {@link ActivityMonitor} builds "by source"
 *          graphs for "for count" and "for time" graphs.  It also returns
 *          an image indicating that no data is available for queries without
 *          data.  {@link ActionSequences} was reorganized and enabled
 *          with progress information. {@link HTTPRequestListener} deals
 *          better with unexpected exceptions and also handles CGI style
 *          requests with session ids better.  {@link AdminService} supports
 *          additional functions to redirect to a progress image.
 *          </dd>
 * <dt>05/08/2003</dt>
 *          <dd>{@link MEMERelaEditorService} was upgraded to support sg_ids in
 *          the core tables.  <code>RxNormService</code> was upgraded to make reads
 *          of worklists faster. {@link MergeEngineService} no longer directly
 *          supports retrieval of logs, that was handed off to {@link AdminService}.
 *          {@link HTTPRequestListener} reports better error messages when
     *          unexpected errors occur.  {@link AdminService} supports "server log",
 *          "session log" and "transaction log" retrieval.  {@link ActivityMonitor}
 *          supports a much richer set of graphs. {@link AuxiliaryDataService} lets
 *          you retrieve all integrity vectors instead of just named ones (this
 *          is for a future webapp for editing integrity checks}.
 *          </dd>
 * <dt>04/25/2003</dt>
     *          <dd>{@link HTTPRequestListener} now preserves the original exception
 *          type instead of sending back {@link gov.nih.nlm.meme.exception.ClientException}s.
 *          Also, we improved the handling of CGI style arguments and cause
     *          any exception while reading the input document to be properly logged.
 *          {@link ActivityMonitor} now supports a wide variety of graphs.
 *          {@link ActionSequences} was re-written as a MEMEServiceRequest.
 *          This will be part of a future db performance testing suite.
 *          </dd>
 * <dt>04/09/2003</dt>
 *          <dd><code>RxNormService</code> was upgraded to support the client-side
 *          iterator methods.  In particular, worklists can now be read in
 *          pieces, whether from a concept or a named worklist.  This allows
     *          more efficient access to the worklists.  {@link HTTPRequestListener}
 *          allows CGI-sytle arguments to have just "name=" with a blank value.
 *          This is useful when you want to select the default data source.
 *          {@link ActivityMonitor} was added, currently it just generates
 *          graphs of editor activity by month, although is envisioned as
 *          the component which will monitor <i>MID</i> performance.
 *          The {@link CoreDataService} supports the client side
 *          <code>populateContextRelationships</code> functionality.
 *          {@link ActionService} feeds back the target identifier from
 *          molecular actions performed, thus allowing the client to
 *          recover the target of a split.
 *          </dd>
 * <dt>04/01/2003</dt>
 *          <dd><code>RxNormService</code> supports reading partial worklists (this
 *          enables the iterator worklist access provided by the clinet).
 *          {@link CoreDataService} supports the new
 *          {@link gov.nih.nlm.meme.client.CoreDataClient} API.
 *          {@link HTTPRequestListener} now supports GET style requests
 *          as well as POST style requests.  The Writer is no longer
 *          passed to the {@link MEMEApplicationServer} <code>processRequest</code>
 *          method, instead, an ouptut stream is directly passed to the request
 *          if it is a cgi-style request.  This precipitated a minor change in
 *          {@link CGIShutdownServer}.  These last few changes were to support
 *          the server's ability to directly return image files in response
 *          to requests from web browsers, to enable interactive graphs
 *          of server and MID activity.
 *          </dd>
 * <dt>03/19/2003</dt>
     *          <dd>Change to {@link ReportsGenerator} to show RX_NORM_STATUS values
 *          as they appear instead of translating them (e.g. U instead of Underspecified).
     *          Optimizations to <code>RxNormService</code> and bug fix for "Apply to
 *          Concept" functionality.
     *          {@link ActionService} code for "Approve Concept" was added to support
 *          stamping.  It also creates a log compatable with the old-style
 *          <code>mproc_change_status.pl</code> script.
 *          </dd>
 * <dt>03/05/2003</dt>
 *          <dd>
 *          Bug fix to {@link MEMERelaEditorService} to repair a deadlock problem.
 *
 *          Fix to {@link CoreDataService} to use a full ticket when reading
 *          concept information.  Vlad had a hack for this in his code.
 *
 *          {@link ActionService} was upgraded to support "batch" and "macro"
 *          actions through the client.
 *
 *          {@link MEMEApplicationServer} and {@link MEMEApplicationService}
 *          now support the notion of "reentrant" applications that can have
 *          only one instantiation running at a given time.  The actual implementation
     *          must be handled bye the service itself through <code>isRunning</code>
 *          and <code>isReentrant</code> methods.
 *
 *          {@link MergeEngineService} was more fully tested and is considered
 *          ready for use in a test-insertion environment.
 *
 *          {@link ReportsGenerator} bug fixes.
 *
 *          <code>RxNormService</code> upgrades.  In particular to support an
 *          "Apply to concept" function that cleans up all unconnected/bad
 *          SCDs, instead of just the ones on the worklist.
 *
 *          </dd>
 * <dt>01/13/2003:</dt>
 *          <dd>The {@link WorklistService} and {@link AdminService} were
 *          updated to support functionality required by the action
 *          harvester and admin applications.
 *          The <code>RxNormService</code> was updated to support additional
 *          RxNorm functionality, specifically with respect to maintaining
 *          branded RxNorm graphs.
 *          </dd>
 * <dt>11/13/2002:</dt>
 *          <dd> The {@link ReportsGenerator} was upgraded to contain
 *               support for the action harvester.  Specifically it
 *               has methods to generate a simple action report for a
 *               molecule_id, and support to generate a summary editing
 *               report by calling <code>$MEME_HOME/bin/editing.csh</code>.
 *               Significant upgrades to {@link FinderService} were also made
 *               to provide the ability to explore the action logs.  Functions
 *               for finding molecular/atomic actions were added.  Much of the
 *               support for this functionality is implmented in the <code>sql</code>
 *               package. Minor bugs in the session handling code were also fixed.
 *          </dd>
 * <dt>08/28/2002:</dt>
 *          <dd> {@link HTTPRequestListener} uses a SO_TIMEOUT setting
 *               of one hour to avoid problems with accept().  If
 *               no requests are made within an hour, the accept() call
 *               is interrupted and re-started.
 *
 *               Most of the development focused on the RxNorm editing
 *               API <code>RxNormService</code> and the various services for
 *               supporting client side editing. These include:
 *               {@link ActionService}, {@link AuxiliaryDataService}
 *               {@link CoreDataService}, {@link FinderService},
 *               {@link WorklistService}.
 *
 *               There were modifications and refinement to some other
 *               services as well.
 *          </dd>
 * <dt>06/20/2002:</dt>
 *          <dd> {@link ReportsGenerator} uses real source values for
 *               C level rels instead of just MTH.  This is to support
 *               showing of NLM03 for Vladimir's purposes.
 *               {@link HTTPRequestListener} ensures that server-side
 *               exceptions do not attempt to inform the user (and thus
 *               require input from the server side).  This improves
 *               stability of the server during error handling.
 *          </dd>
 * <dt>5/13/2002:</dt>
 *          <dd> {@link HTTPRequestListener} was using the same
 *               instance of the serializer across different threads.
 *               The serializer was moved into the request handler class
 *               so that each thread has access to its own serializer.
 *               {@link MEMERelaEditorService} was upgraded to support
 *               manipulation of relationships between atom_ids, CUIs,
 *               and concept_ids.
 *          </dd>
 * <dt>4/22/2002:</dt>
 *          <dd> {@link ReportsGenerator} was fixed in a few ways.
 *               (a). logic for when to show context relationships
 *               was corrected, (b). STY display was fixed, (c.)
 *               status N stys were not showing up, this was fixed,
 *               (d) hyperlinks on concept_ids should have #report appended
 *               to them, (e) HTML character entities &lt;, &gt; and &amp;
 *               are used instead of the actual characters in the report
 *               so that everything works out.
 *               New service: {@link AuxiliaryDataService}.  It provides
 *               access to data source data.
 *          </dd>
 * <dt>04/04/2002:</dt>
 *          <dd> {@link MEMERelaEditorService} was made thread safe in
 *               response to a database hanging problem.  It was also
 *               updated to ensure that all statement handles are
 *               explicitly closed.
 *               {@link ReportsGenerator} was incorrectly handling STY,
 *               DEF, and SOS sections of the report.
 *          </dd>
 * <dt>03/26/2002:</dt>
 *          <dd> {@link MEMERelaEditorService} was extended to support
 *               the {@link gov.nih.nlm.meme.client.MEMERelaEditorKit#insertRelationshipAttribute(String,String)}
 *               functionality.<p>
 *               {@link gov.nih.nlm.meme.server.ReportsGenerator} was extended to support all
 *               of the <code>xreports.pl</code> and {@link gov.nih.nlm.meme.client.ReportsClient}
 *               options.<p>
 *               The {@link ServerThread} API was created to represent
 *               bootstrap classes that run in separate threads after
 *               initialization.<p>
 *               Some {@link MEMEApplicationServer} functionality
     *               was made <code>protected</code> instead of <code>private</code>
 *               so that the <code>MRDApplicationServer</code> can override it.<p>
 *               <code>DataSourcePool</code> became {@link MIDDataSourcePool}.
 *               This was to support an <code>MRDDataSourcePool</code> on the
 *               <code>gov.nih.nlm.mrd</code> side of things.<p>
 *               {@link SessionTimeoutThread} was written as a means
 *               to retire active sessions.  It is not currently used, though.
 *
 * </dd>
 * <dt>11/01/2001:</dt><dd>1st Release.</dd>
 * </dl>
 *
 * @author MEME Group
 */

public class Version {

  //
  // Fields
  //

  private final static int release = 4;
  private final static double version = 0.0;
  private final static String version_authority = "MEME Group";
  private final static String version_date = "28-aug-2002 00:00:00";
  private final static String package_name = "gov.nih.nlm.meme.server";

  //
  // Methods
  //

  /**
   * Returns the package current release.
   * @return the package current release
   */
  public static int getRelease() {
    return release;
  }

  /**
   * Returns the package current version.
   * @return A <code>double</code> representation of package current version.
   */
  public static double getVersion() {
    return version;
  }

  /**
   * Returns the package current version authority.
   * @return the package current version authority
   * authority.
   */
  public static String getAuthority() {
    return version_authority;
  }

  /**
   * Returns the package current version date.
   * @return the package current version date.
   * date.
   */
  public static Date getDate() {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    Date date = null;
    try {
      date = formatter.parse(version_date);
    } catch (Exception e) {
      BadValueException bve = new BadValueException(
          "Badly formatted version date.");
      bve.setDetail("version_date", version_date);
      MEMEToolkit.handleError(bve);
    }
    return date;
  }

  /**
   * Returns the package current package name.
   * @return the package current package name
   */
  public static String getPackage() {
    return package_name;
  }

  /**
   * Returns the package current version information.
   * @return the package current version information
   */
  public static String getVersionInformation() {
    return getPackage() + ": " + getRelease() + "." + getVersion() + ", "
        + getDate() + " (" + getAuthority() + ")";
  }

  //
  // Main
  //

  /**
   * This can be used to print package version information
   * on the command line.
   * @param argv An array of argument.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.meme.server.Version.getVersionInformation());
  }
}
