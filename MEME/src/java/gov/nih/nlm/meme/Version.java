/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.meme;

import gov.nih.nlm.meme.exception.BadValueException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class provides package version information for
 * gov.nih.nlm.meme package.
 * 
 * <b>History:</b>
 * <dl>
 * <dt>10/28/2005</dt>
 * <dd> Very few java changes for this release. Most changes were to accommodate
 *      the new attributes spec for MRMAP.  This affected the various map set
 *      objects and mapping functions.
 * </dd>
 * <dt>10/28/2005</dt>
 * <dd> First official release after adopting the new model. Here are changes to
 * the Java code compiled from the MEME, MRD, and MEOW README.txt pages.
 * <ul>
 * <li>ReportsGenerator was updated to include RXCUI values in the reports and
 * to also fix a bug with the links thta appear on meow. It also now removes
 * links for cases where no URL for a link type is specified (e.g. if no CUI
 * link URL is specified, CUIs will not be linked).</li>
 * <li>Some webapps changes were made. A new JSP was created to take over the
 * remaining task of code_map.cgi which was to produce descriptions of the
 * various codes where needed. This CGI is no longer used so the webapp was
 * needed to take over its role.</li>
 * <li>New integrity check MGV_RXCUI has been created. It prevents concepts
 * from merging if they will bring togther two different RXCUIs that are not
 * already merged.</li>
 * <li>Fixes to MedlineHandler to eliminate the medline.prop file parameter.</li>
 * <li>MRDActionEngine was created and is now returned by MRDConnection's
 * getActionEngine method.  This was needed to allow the adding of a new
 * MEDLINE pattern.</li>
 * </ul>
 * </dd>
 * <dt>08/15/2005</dt>
 * <dd>Suppressible implementation, improvements to XML representation. Also,
 * better use of environment for {@link MIDServices}. </dd>
 * <dt>06/17/2005</dt>
 * <dd>Changes include:
 * <ol>
 * <li>Considerable improvement to javadoc comments.</li>
 * <li>Improvements to the "Mappings" API to support the webapp viewer/editor</li>
 * <li>Improvements to concept reports API to support xreports.pl extensions</li>
 * <li>Continued work on Recipe API.</li>
 * </ol>
 * </dd>
 * <dt>03/25/2005</dt>
 * <dd>There are only a small number of changes in this release. They include:
 * <ol>
 * <li>The termgroup object now defines a natural sort ordering)</li>
 * <li>Server thread pool now catches all exceptions and allows threads to be
 * returned to pool</li>
 * <li>Server does a better job of reporting REJECTED requests</li>
 * <li>Server runs garbage collection and tracks free memory in the log</li>
 * <li>Default ticket for reading concepts and generating reports ignores XMAP%
 * attributes, should improve performance</li>
 * <li>Attribute mappers show source of contexts, not just source of atoms
 * connected to concepts</li>
 * <li>An exception leak in ResultSetIterator was fixed</li>
 * </ol>
 * </dd>
 * <dt>01/25/2005</dt>
 * <dd>This release was made to address 2 issues: 1. Improved CUI assignment
 * algorithm (should correctly work for all actions now) 2. Better network
 * stability. Better support for all 3 kinds of network failures (connect,
 * write, read). </dd>
 * <dt>01/14/2005</dt>
 * <dd> Variety of changes and bug fixes mostly focused on tightening the undo
 * and redo mechanisms for the new action model. Only a few changes here will
 * apply to editors/Jekyll. Changes include,
 * <ol>
 * <li>Completion of action model for undoing logged actions. Now, every action
 * defines its own inverse and can be undone by performing the inverse of the
 * action. This alleviates the need for an explicit "undo" action. To perform a
 * "redo", all you have to do is get the inverse of the "undo" action.
 * <li>Some actions are inherently not undoable and these return as their
 * inverse an action that performs no operation.
 * <li>CONTEXTS section of the concept report was moved below RELATIONSHIPS.
 * <li>SAB for DEF and SOS attributes will now show up on concept report.
 * <li>A bug that happens when the preferred atom of a related concept is not
 * set was fixed.
 * <li>Improved support for transaction and work identifiers.
 * </ol>
 * 
 * </dd>
 * <dt>12/13/2004</dt>
 * <dd>This release continues our major overhaul of the MEME4 action model. It
 * addresses a number of bugs and improvements including support in reports for
 * concept definitions and MUIs attached. This version also supports cui
 * assignment and logging of actions (if switches in system status are on).
 * </dd>
 * <dt>11/05/2004</dt>
 * <dd> This release includes a variety of important upgrades. These include:
 * <ol>
 * <li>A re-worked and expanded action model that places all actions now in the
 * <tt>gov.nih.nlm.meme.action</tt> package instead of in
 * <tt>gov.nih.nlm.meme.common</tt>. Old actions are still supported but hvae
 * been deprecated.</li>
 * <li>Expanded actions now support a wide variety of additional editing on the
 * auxiliary tables like source_rank and termgroup_rank. This development effort
 * will eventually support all web-tool and source insertion operations,
 * allowing all MEME4 changes to be simply and easily logged for MRD
 * synchronization.</li>
 * <li>The data source architecture now has a more sophisticated cacheing
 * architecture that allows data to be separately cached for each database being
 * connected to. If a connection to a new data source is requested, a cache is
 * first populated and then is maintained for the life of the server.</li>
 * <li>Some minor bug fixes are included.</li>
 * <li>New client support for functions to support MRSAB, MRDOC, MRCOLS/FILES
 * editors.</li>
 * <li>Eventually *all* cgi applications on MEOW will be replaced by webapps
 * supported by MEME4.</li>
 * </ol>
 * </dd>
 * <dt>08/19/2004</dt>
 * <dd>This release includes a variety of fixes and upgrades including:
 * <ol>
 * <li>Initial concept mapping API.</li>
 * <li>Content view API updated to support latest data model.</li>
 * <li>Socket interaction improved: better buffering, longer SO_TIMEOUT.</li>
 * <li>Includes bug fixes for versions 47.1-47.3.</li>
 * <li>Fix for the data source overflow problems.</li>
 * <li>Javadoc improvements.</li>
 * </ol>
 * </dd>
 * <dt>07/20/2004</dt>
 * <dd>This release is basically a re-release of the 7/09 version because it
 * was not properly released. It also includes a number of minor fixes. </dd>
 * <dt>07/09/2004</dt>
 * <dd>This release addresses a number of minor bugs and issues.
 * <ol>
 * <li>Minor changes to {@link MEMEToolkit} and
 * {@link gov.nih.nlm.swing.SwingToolkit} and
 * {@link gov.nih.nlm.util.SystemToolkit}.</li>
 * <li>Content view implementation (common objects, client, service).</li>
 * <li>Upgrades to actions to suport more functionality and fix a split bug.</li>
 * <li>Small upgrade to MGV_M to allow NEC merges among SNOMEDCT,RCD,SNMI.</li>
 * <li>Small upgrade to MEDLINE XML handler to support all bad date formats.</li>
 * </ol>
 * </dd>
 * 
 * <dt>06/14/2004</dt>
 * <dd>This release addresses a number of minor bugs and issues.
 * <ol>
 * provides access to the inverse RUI value.</li>
 * <li>Minor changes to the molecular "change" actions, also for test suites.</li>
 * <li>New integrity check: MGV_STY to prevent STY pairs from merging.</li>
 * <li>Web-graphs should now report avg,min,max elapsed time in milliseconds
 * not seconds.</li>
 * <li>Small RxNorm bug (causing the "B 10 ml") was fixed.</li>
 * <li>Concept reports support concept level SOS and DEFINITION.</li>
 * <li>Better management of closing cursors when DB is busy</li>
 * </ol>
 * </dd>
 * <dt>05/07/2004</dt>
 * <dd>Three main reasons for release:
 * <ol>
 * <li>Support reading any list of languages</li>
 * <li>Bug fix for deadlock issue and some minor parameter issues</li>
 * <li>Cache RELA values better</li>
 * </ol>
 * </dd>
 * <dt>04/22/2004</dt>
 * <dd>Release due to version 44.1 versionitis issue. </dd>
 * <dt>04/21/2004</dt>
 * <dd>More comprehensive support for reading/not reading non-ENG data.
 * FinderClient supports <code>restrictByReleasable</code> </dd>
 * <dt>04/19/2004</dt>
 * <dd>This release includes all of the previous intermediate patch releases
 * plus two other important improvements. It now fully supports
 * display/manipulation of non-ENG atoms and it provides more consistent
 * handling of errors (so as to prevent all problems that might cause the server
 * to be unexpectedly killed). </dd>
 * <dt>03/30/2004</dt>
 * <dd>Preferred concept name display (in reports) bug fix. </dd>
 * <dt>03/29/2004</dt>
 * <dd>RxNorm bug fix. </dd>
 * <dt>03/23/2004</dt>
 * <dd>Bug fix for {@link gov.nih.nlm.meme.common.Concept#clear()} and
 * {@link gov.nih.nlm.meme.common.Atom#clear()}. </dd>
 * <dt>03/17/2004</dt>
 * <dd>Mostly minor changes. Includes bug fixes that were handled by patches.
 * Major improvements include: ability to handle attributes as attached to
 * relationships (or any other core data type), ability to configure some DB
 * parameters (sort area size/ hash area size), and a more stable
 * {@link gov.nih.nlm.meme.client.EditingClient}. Also a minor bug fix for
 * <code>gov.nih.nlm.meme.server.RxNormService</code>. </dd>
 * <dt>02/20/2004</dt>
 * <dd>This release is mostly for bug fixes. It fixes a problem with the
 * {@link gov.nih.nlm.meme.sql.Ticket} which was causing class cast exceptions
 * (for RxNorm editor work), and a problem with a query that was preventing
 * CONCEPT_NOTEs from being properly read. The main substantive changes are that
 * this release has better support for non-english Atoms (and attributes/rels
 * attached to them), and supports attributes connected to relationships. The
 * next version will fully support these things, and the MID will have the data
 * to enable it. One other notable addition is
 * {@link gov.nih.nlm.util.BrowserLauncher} for launching a URL in the "user
 * default browser" in a (pseudo) platform-independent way. </dd>
 * <dt>02/06/2004</dt>
 * <dd>This release is primarily for server side upgrades. The major client
 * side upgrade is to {@link gov.nih.nlm.meme.client.CoreDataClient} which now
 * supports a much wider range of data access methods. Importantly, it allows a
 * client application to read concept relationships in segments (say 0 to 1000)
 * instead of reading all at once, which should improve client-side editing.
 * Additionally, "relationship attributes" such as SNOMEDCT REFINABILITY are not
 * read upon a "get concept" request which should improve the performance of
 * reading large concepts. The major server side improvements include:
 * <ol>
 * <li>Bug fixes included in recent patches.</li>
 * <li>Overhaul of XML parser to more cleanly and effeciently handle
 * "primitive" data.</li>
 * <li>Server side support for enhanced
 * {@link gov.nih.nlm.meme.client.CoreDataClient} functions</li>
 * <li>Overhaul of how {@link gov.nih.nlm.meme.sql.MEMEConnection} reads core
 * data. This leads to efficiency, code clarity, and enhanced functionality.</li>
 * <li>Other minor upgrades. See the various sub=package version classes for
 * more details.</li>
 * </ol> . </dd>
 * <dt>01/20/2004</dt>
 * <dd>This release contains a number of bug fixes and performance
 * enhancements. It also includes improved Javadoc documentation for many of the
 * classes. Important specific changes include:
 * <ol>
 * <li>Freshness of data is now validated before molecular actions are
 * performed.</li>
 * <li>The molecular action locking mechanism was made more efficient, and
 * coordinated with the undo/redo locking mechanism.</li>
 * <li>The mapping client can now fully support editing of map set information.</li>
 * <li>Small {@link gov.nih.nlm.meme.action.MolecularMergeAction} bug was fixed
 * (should prevent some MEME4 errors).</li>
 * <li>Actions more aggressively re-read data to prevent errors.</li>
 * <li>New exception: {@link gov.nih.nlm.meme.exception.StaleDataException}.</li>
 * <li>Small server bug fix to prevent recurring null pointer exception.</li>
 * </ol> . </dd>
 * <dt>12/29/2003</dt>
 * <dd>Bug fix release. Changes to {@link gov.nih.nlm.meme.common.CoreData} and
 * to {@link gov.nih.nlm.meme.sql.ActionEngine} fix a problem with confusing
 * RUI, source_rui, SRC identifier, and relationship_id. This was partially a
 * problem for attributes and atoms as well. Additionally, the merge engine was
 * updated to make use of the progress reporting mechanism. We will test it with
 * the next test insertion. </dd>
 * <dt>12/19/2003</dt>
 * <dd>This release includes some improvements over the last one. Most notably:
 * <ol>
 * <li>Fix to RxNorm code to prevent duplicate ingredient relationships</li>
 * <li>Ability to restrict REVIEWED RELS view on concept reports</li>
 * <li>Mapping implementation updated to reflect most recent MRMAP proposal</li>
 * <li>Implementation of a progress monitor ability so that server can
 * communicate progress events back to the client.</li>
 * </ol> . </dd>
 * <dt>12/15/2003</dt>
 * <dd>This is a fairly small release that adds some new functionality and
 * cleans up a few threading issues.
 * <ol>
 * <li>Implementation of the initial mapping API ({@link gov.nih.nlm.meme.client.MappingClient}.</li>
 * <li>Client side {@link gov.nih.nlm.meme.client.HTTPRequestClient} is now
 * thread safe, allowing multiple simultaneous requests to the same client to be
 * made by separate threads without any problems.</li>
 * <li>The {@link gov.nih.nlm.meme.server.SessionContext} implementation on the
 * server side is now also thread safe, allowing multiple requests to access the
 * server using the same sesson id at the same time without any problems.</li>
 * <li>Javadoc updates.</li>
 * <li>Minor other cosmetic changes, like small changes to the way an action
 * report document looks.</li>
 * </ol>
 * abc </dd>
 * <dt>12/04/2003</dt>
 * <dd>Two reasons for this release:
 * <ol>
 * <li>Bug fix to {@link gov.nih.nlm.meme.server.SessionTimeoutThread} causing
 * null pointer exceptions</li>
 * <li>Implementation of Move integrity checks</li>
 * </ol>
 * </dd>
 * <dt>12/01/2003</dt>
 * <dd>This is a small release containing mostly small changes to things like
 * error messages. Some other notable changes include:
 * <ol>
 * <li>Ability to control the session timeout</li>
 * <li>Ability for a session to manage simultaneous requests</li>
 * <li>Our concurrency testing was updated to use revised actions</li>
 * <li>Much of the documentation was improved</li>
 * <li>Error messages were improved</li>
 * </ol>
 * </dd>
 * <dt>11/07/2003</dt>
 * <dd>This release includes upgrades and bug fixes. 1. The printing now seems
 * to be fast and can run in a headless environment. 2. Queries for
 * relationships/attributes can now look up new fields (e.g. source_rui). 3. The
 * molecular actions now read precisely when the need to and not when the do
 * not, and the comments were cleaned up. 4. Initial objects for editing
 * Mappings were created. 5. Error messages from the server should now include
 * authentication user (to track down which editor is generating errors). </dd>
 * <dt>10/31/2003</dt>
 * <dd>Several changes: 1. copy-paste problem for report frame fixed. 2. Bugs
 * in the optimized actions fixed (and javadocs updated) 3. Title-bars in
 * printing are not subjected to scaling. 4. RxNorm duplicate component problem
 * solved. </dd>
 * <dt>10/23/2003</dt>
 * <dd>Fix for {@link gov.nih.nlm.meme.action.MolecularMergeAction} in the
 * <code>getConflictingRelationships</code> method. Additionally, the
 * {@link gov.nih.nlm.util.Enscript} object was updated to support overrides of
 * the scaling factors. </dd>
 * <dt>10/22/2003</dt>
 * <dd>Additional exec methods in {@link MEMEToolkit}. Optimized molecular
 * actions. SNOMEDCT concept status appears on concept report. And
 * {@link gov.nih.nlm.meme.client.PrintConceptReportAction} now prints 2 pages
 * per sheet. </dd>
 * <dt>10/16/2003</dt>
 * <dd>Fixes session-tracking bug. Supports HTML printing. </dd>
 * <dt>10/01/2003</dt>
 * <dd>There are two primary changes for this release.
 * <ol>
 * <li>Pass violations vectors back to client correctly.</li>
 * <li>Support new MEME_APROCS.</li>
 * </ol>
 * </dd>
 * <dt>09/08/2003</dt>
 * <dd>Code to calculate conflicting relationships during merges was updated.
 * The client used by batch.pl reports errors more completely. "Apply to
 * concepts" was improved, and TRD lexical tags are now inserted as releasable.
 * Atomic actions are now read in atomic action id order so that redo and undo
 * can function properly (fixes jekyll problem). The
 * {@link gov.nih.nlm.util.SystemToolkit} was updated to generate MD5 hash
 * values (for a variety of character sets). </dd>
 * <dt>09/02/2003</dt>
 * <dd>Fix to <code>gov.nih.nlm.meme.server.RxNormService</code> in response
 * to Laura's 9/02 message "RXNORM Apply to All not working". </dd>
 * <dt>08/27/2003</dt>
 * <dd>Fixed DT_I3B again</dd>
 * <dt>08/25/2003</dt>
 * <dd> There are a few important changes in this release.
 * <ol>
 * <li>The RxNorm server side code does a better job of identifying precise vs.
 * base ingredients. This only mattered in the previous versions if the precise
 * ingredient were shorter than the base.</li>
 * <li>The DT_I3B integrity check was fixed.</li>
 * <li>The problem of duplicate concepts on checklists/worklists was fixed</li>
 * <li>The SortableJTable supports a <code>resetSort()</code> method to reset
 * the state of the table to perform no sorting operations.</li>
 * <li>All input streams, output streams use UTF-8 encoding</li>
 * <li>When printing concept reports, the font size is a little bigger</li>
 * <li>There are optimizations to the XML parsing to allow jdk1.4.2 to work
 * better</li>
 * </ol>
 * </dd>
 * <dt>08/19/2003</dt>
 * <dd>This release contains a variety of improvements and bug fixes. Important
 * highlights are:
 * <ol>
 * <li>The TestReportFrame will correctly scale and print concept reports
 * (including wrapping text). The concept reports will SNOMEDID and CTV3ID
 * legacy codes.</li>
 * <li>The server now supports a function to reset the data source pool to
 * point to a different database</li>
 * <li>The move action properly handles demotions, and the merge had a small
 * bug fix.</li>
 * <li>The ActionReport had a bug fix.</li>
 * <li>The MergeEngineService strips off ENG- from the source when inserting
 * demotions.</li>
 * <li>Bug fixes to SQL api to correctly read all actions and to ensure that
 * atom_id_2 is always set when a relationship is read</li>
 * 
 * </dd>
 * <dt>08/11/2003</dt>
 * <dd>The main reason for this release is to fix an RxNorm bug that causes
 * precise ingredients to be added to the wrong concept by the RxNorm editor. It
 * also adds a small fix for Jekyll to prevent a concept from being "merged"
 * with itself. </dd>
 * <dt>08/01/2003</dt>
 * <dd>This release upgrades RxNorm to include the new SBDF and SCDF atoms in
 * the graph structure maintained by
 * <code>gov.nih.nlm.meme.server.RxNormService</code>. Bug fixes to things
 * like maintaining precedence, and handling null editing authorities in the
 * concept reports, and inability to correctly print concept reports from Jekyll
 * were fixed. </dd>
 * <dt>07/16/2003</dt>
 * <dd>The major reason for this release is to fix a couple of RxNorm problems
 * (problem prevent dose froms from being properly read, and problem with "apply
 * to concept" not properly removing everything) and to fix problems Vlad was
 * having with the {@link gov.nih.nlm.meme.client.TestReportFrame}. It also
 * contains implementations of code for handling
 * {@link gov.nih.nlm.meme.common.ContextPath} objects. </dd>
 * <dt>06/27/2003</dt>
 * <dd>There are only two changes. A bug fix for Vlad to allow the split action
 * to work again. Changes to the handling of checklists/worklists to address
 * issues with what winds up being on the worklists/checklists. </dd>
 * <dt>06/19/2003</dt>
 * <dd>A variety of changes. Some bug fixes to the connection objects related
 * to inserting new atoms, and a revised algorithm for connecting RxNorm graphs
 * to the ingredient level (all NLM02 atoms). Finally, there are a few
 * fixes/upgrades to better support Jekyll. </dd>
 * <dt>06/06/2003</dt>
 * <dd>{@link MEMEToolkit} has additional initialize methods to allow
 * properties to be set without using a properties file. Assorted other changes
 * are described in the other version classes. </dd>
 * <dt>05/21/2003</dt>
 * <dd>Released to fix RxNorm problem but also fixes a couple of minor
 * molecular action logic issues. </dd>
 * <dt>05/16/2003</dt>
 * <dd>Changes are detailed, as usual in sub-package <code>Version</code>
 * classes.
 * <ul>
 * <li>Fixes to RxNorm problems.</li>
 * <li>Graphing logic enhancements</li>
 * <li>Improve progress monitoring.</li>
 * <li>Fixes for Jekyll/vlad problems</li>
 * </ul>
 * </dd>
 * <dt>05/12/2003</dt>
 * <dd>Numerous changes. Main areas of interest are:
 * <ul>
 * <li>Improvements to RxNorm performance.</li>
 * <li>Additional {@link gov.nih.nlm.meme.client.AuxiliaryDataClient} methods</li>
 * <li>CGI/XML optimizations.</li>
 * <li>Better graphs</li>
 * <li>Increased support for recent schema changes</li>
 * <li>Fixes for Jekyll/vlad problems</li>
 * </ul>
 * </dd>
 * <dt>04/25/2003</dt>
 * <dd>Again, there were numerous changes each more fully specified in the
 * sub-package Version classes. Summary:
 * <ul>
 * <li>Exception types are preserved now on the client side. However, in
 * general the clients still only throw
 * {@link gov.nih.nlm.meme.exception.MEMEException}s, but this will gradually
 * change, as in {@link gov.nih.nlm.meme.client.CoreDataClient}. </li>
 * <li>Bug fixes for Vlad:
 * <ul>
 * <li>actions should no longer throw these aproc_change_status errors</li>
 * <li>the {@link gov.nih.nlm.meme.client.ActionClient} now handles the "target
 * id" of molecular actions without a null pointer exception</li>
 * <li>the change relationship action no longer allows "change rel name".
 * </ul>
 * <li>Additions to the "server administration" tool, specifically it now
 * generates an number of graphs to track editing and performance in the system.
 * <li>A class implementing a sample action sequence was converted into a
 * service, allowing us to simulate multiple editors editing sample content.
 * This is part of a future performance monitoring suite.
 * </ul>
 * </dd>
 * <dt>04/09/2003</dt>
 * <dd>There are a variety of changes which are more fully specified in the
 * sub-package Version classes. Summary:
 * <ul>
 * <li>MEME4 uses UTF-8 character encoding in XML documents instead of USASCII.
 * <li>Bug fixes for Jekyll:
 * <ul>
 * <li><code>Concept.getSemanticTypes</code> works now </li>
 * <li><code>Atom.getFormattedContexts</code> works now </li>
 * <li>after performing a <code>MolecularSplitAction</code> the target
 * concept can be recovered by calling <code>getTarget()</code> on the action
 * itself. However, it is not a populated concept so you must re-read it by
 * using {@link gov.nih.nlm.meme.client.CoreDataClient}.
 * </ul>
 * <li>RxNorm performance improvements.
 * <li>Read-relationships performance improvements (including improvements to
 * reports code, the large concept "Serum" 114833 now can be read in under a
 * minute with subsetquent reads taking under 30 seconds.</li>
 * <li>Minor other c hanges</li>
 * </ul>
 * </dd>
 * <dt>04/01/2003</dt>
 * <dd>There are a variety of changes which are more fully specified in the
 * sub-package Version classes. Summary:
 * <ul>
 * <li>RxNorm client/server support access to large worklists.</li>
 * <li>CoreDataClient supports more efficient access to data.</li>
 * <li>Minor bug fixes to exception, integrity, and xml classes.</li>
 * <li>The merge actions supports a method to identify <i>conflicting
 * relationships</i>.</li>
 * <li>The {@link gov.nih.nlm.meme.sql.MEMEConnection} supports more consistent
 * access to core data, and a more efficient mechanism for reading relationships
 * with names. </li>
 * </ul>
 * </dd>
 * <dt>03/19/2003</dt>
 * <dd> The main changes here are:
 * <ul>
 * <li>Faster checklist/worklist loading (we only read concept id and preferred
 * atom name).</li>
 * <li>Batch insert fix.</li>
 * <li>Stamping implementation (accompanied by
 * <code>$MEME_HOME/bin/stamping.pl</code></li>
 * <li>ObjectXMLSerializer upgrade. This is the first part of an attempt to
 * addres the reading of large concepts by the editing interface.</li>
 * </ul>
 * </dd>
 * <dt>03/06/2003</dt>
 * <dd>Many changes, mostly to support source insertion operations and bug
 * fixes. See sub version objects for more details. </dt>
 * <dt>01/15/2003:</dt>
 * <dd>Minor change to {@link gov.nih.nlm.meme.client.AdminClient} to support
 * retrieving of server version information on the client side and to check
 * whether or not the server is validating actions. Minor change to
 * {@link MIDServices} to support the looking up of a list of MEME Application
 * Server hosts and ports. </dd>
 * <dt>01/13/2003:</DT>
 * <dd>Only minor changes to support the latest RxNorm editor. Some changes
 * were made to <code>gov.nih.nlm.meme.common.RxNormBrandedAtom</code> and the
 * <code>gov.nih.nlm.meme.server.RxNormService</code>. Notably, the service
 * was updated to better service the branded normal form case, and a bug was
 * fixed in the atom class that was duplicating data.
 * 
 * A method was also added to {@link gov.nih.nlm.meme.client.WorklistClient} to
 * support the reading of only current worklists instead of all. This was to
 * support the "Worklist Lookup" functionality of the action harvester. Changes
 * were also made to {@link gov.nih.nlm.meme.server.WorklistService}.
 * 
 * Method were added to {@link gov.nih.nlm.meme.client.AdminClient} to check
 * whether or not molecular/atomic action validation is enabled. This entailed
 * additional code in {@link gov.nih.nlm.meme.server.AdminService} as well.
 * These changes support the new server administration web application.</dd>
 * <dt>12/11/2002:</DT>
 * <dd>Supports branded RxNorm forms. Small changes to
 * {@link gov.nih.nlm.meme.sql.ActionEngine} to handle assign AUIs to atoms with
 * null codes. Change to {@link gov.nih.nlm.meme.server.MEMEApplicationServer}
 * to guarantee that failed processes rollback their datasource connections
 * before returning control to the client. This should prevent MAX_TAB lockouts
 * on MEME4 failures. </dd>
 * <dt>12/04/2002:</dt>
 * <dd>ResultsSetIterator closes statements AND result sets</dd>
 * <dt>12/02/2002:</dt>
 * <dd>Fixes the bug from last release that was preventing concept reports from
 * being generated. Talks to suresh's new socket server protocol and leverages
 * lvg-server-host and lvg-server-port mid service names to do it. better AUI
 * maintenance. </dd>
 * <dt>11/14/2002:</dt>
 * <dd>Most of the changes in this release are to code that supports our web
 * development efforts, specifically with respect to "action harvester"
 * functionality. However, support for MEME4 maintenance of AUIs was included in
 * the action engine as were small bug fixes/upgrades in other areas.
 * Significant development was undertaken with respect to RxNorm, but this
 * release will include RxNorm classes from the previous release as the NLM is
 * not ready to start using the new RxNorm editor. </dd>
 * <dt>09/24/2002:</dt>
 * <dd>This release is primarily to make a few fixes to the RxNorm code to
 * support the new HL7 source. </dd>
 * <dt>08/28/2002:</dt>
 * <dd>There are three main purposes for this release.
 * <ol>
 * <li>The <code>gov.nih.nlm.meme.server.RxNormService</code> and.
 * <code>gov.nih.nlm.meme.client.RxNormClient</code> code.
 * <li>A fix to the {@link gov.nih.nlm.meme.server.HTTPRequestListener} to
 * (hopefully) fix this problem of the server on oc refusing to accept
 * connections.
 * <li>Implementation of a variety of client-side APIs for an editing
 * interface. See {@link gov.nih.nlm.meme.client.Version}.
 * </ol>
 * </dd>
 * <dt>06/20/2002:</dt>
 * <dd>{@link MIDServices} was modified to use properties for the mid services
 * and lvg services hosts/port.
 * 
 * This release contains a significant number of upgrades contained in the
 * sub-packages. See the sub-package <code>Version</code> classes for more
 * details. </dd>
 * <dt>05/13/2002:</dt>
 * <dd>This release is primarily to provide additional
 * {@link gov.nih.nlm.meme.client.MEMERelaEditorKit} functionality. Look here or
 * more details: {@link gov.nih.nlm.meme.server.Version},
 * {@link gov.nih.nlm.meme.client.Version}.
 * 
 * {@link MIDServices} was also upgraded to used "-db" oriented names instead of
 * "-tns" names. </dd>
 * <dt>04/22/2002:</dt>
 * <dd>This release is primarily to make a few fixes to the reports generator,
 * described here: {@link gov.nih.nlm.meme.server.Version}. </dd>
 * <dt>04/04/2002:</dt>
 * <dd>Added
 * {@link MEMEToolkit#logCommentToBuffer(String,boolean,StringBuffer)}. Code
 * was released to support MEMERelaEditorKit and Reports fixes.
 * 
 * </dd>
 * <dt>03/26/2002:</dt>
 * <dd>Release includes upgrades to client, server, sql, and common packages to
 * support extended reports. </dd>
 * <dt>03/06/2002:</dt>
 * <dd>FieldedStringTokenizer was optimized for performance. MIDServices was
 * expanded to provide LVG services. </dd>
 * <dt>03/07/2002:</dt>
 * <dd>3rd Release.</dd>
 * <dt>01/31/2002:</dt>
 * <dd>2nd Release.</dd>
 * <dt>11/01/2001:</dt>
 * <dd>1st Release.</dd>
 * </dl>
 * 
 * @see gov.nih.nlm.meme.client.Version
 * @see gov.nih.nlm.meme.common.Version
 * @see gov.nih.nlm.meme.exception.Version
 * @see gov.nih.nlm.meme.integrity.Version
 * @see gov.nih.nlm.meme.server.Version
 * @see gov.nih.nlm.meme.sql.Version
 * @see gov.nih.nlm.meme.xml.Version
 * 
 * @author MEME Group
 */

public class Version {

  //
  // Fields
  //

  private final static int release = 4;

  private final static double version = 60.0;

  private final static String version_authority = "MEME Group";

  private final static String version_date = "31-Mar-2006 00:00:00";

  private final static String package_name = "gov.nih.nlm.meme";

  //
  // Methods
  //

  /**
   * Returns the package current release.
   * 
   * @return An <code>int</code> representation of package current release.
   */
  public static int getRelease() {
    return release;
  }

  /**
   * Returns the package current version.
   * 
   * @return A <code>double</code> representation of package current version.
   */
  public static double getVersion() {
    return version;
  }

  /**
   * Returns the package current version authority.
   * 
   * @return An object {@link String} representation of package current version
   *         authority.
   */
  public static String getAuthority() {
    return version_authority;
  }

  /**
   * Returns the package current version date.
   * 
   * @return An object {@link Date} representation of package current version
   *         date.
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
   * 
   * @return An object {@link String} representation of package current name.
   */
  public static String getPackage() {
    return package_name;
  }

  /**
   * Returns the package current version information.
   * 
   * @return An object {@link String} representation of package current version
   *         information.
   */
  public static String getVersionInformation() {
    return getPackage() + ": " + getRelease() + "." + getVersion() + ", "
        + getDate() + " (" + getAuthority() + ")";
  }

  //
  // Main
  //

  /**
   * This can be used to print package version information on the command line.
   * 
   * @param argv
   *          An array of string argument.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.meme.Version.getVersionInformation());
  }
}
