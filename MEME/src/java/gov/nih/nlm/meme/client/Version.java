/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class provides package version information
 * for gov.nih.nlm.meme.client package.
 *
 * <b>History</b>
 * <dl>
 * <dt>08/15/2005:</dt>
 * <dd>{@link ClientConstants} for MIDSVCS_HOST and MIDSVCS_PORT were changed to
 * interact properly with normalized environment implementation.
 * </dd>
 * <dt>06/17/2005:</dt>
 * <dd>Many updates to method javadoc comments, and some formatting optimizations.
 * {@link MappingClient} has additional methods for the viewer/editor application.
 * </dd>
 * <dt>03/11/2005:</dt>
 * <dd>{@link InsertionClient} supports initial recipe step implementation.
 * </dd>
 * <dt>01/25/2005:</dt>
 * <dd>
 * {@link ActionClient} does a better job of reporting transaction/work_ids to
 * the server. (fixes a bug with undo.pl)
 * {@link HTTPRequestClient} uses more stable mechanism for talking to server.
 * It attempts to try again upon failures and to reconnect to the server to
     * receive a response if a request got through.  This should address most of NLM
 * network issues.
 * {@link MaintenanceClient} supports an "exec" call.
 * {@link
 * </dd>
 * <dt>01/15/2004:</dt>
 * <dd>
 * Improvements to {@link ActionClient} so that it passes work_id of batch
 * actions through to the MBA call.  Also passes "force" parameters through
 * to server side.  Additionally supports undo/redo of any logged action.
 *
 * Changes to {@link AuxiliaryDataClient} and {@link EditingClient} to use
 * transaction and work log objects instead of ids when requesting activity
 * logs and errors.
 *
 * Improvements to {@link MaintenanceClient}, including support for logging
 * progress, resetting progress, logging operations, running matrix initializer
 * and CUI assignment.
 *
 * </dd>
 * <dt>12/13/2004:</dt>
 * <dd>
 * {@link AuxiliaryDataClient} has <tt>setTermgroup</tt>
 * {@link WorklistClient} main method moved to test suite.
 * {@link ActionClient} has <tt>getWorkLogsByType</tt> and <tt>getErrors</tt>.
 * {@link MaintenanceClient} supports assigning cuis, executing arbitrary queries,
 * executing arbitrary scripts, and loading tables.
 * {@link AdminClient} supports <tt>setSystemStatus</tt>.
 * </dd>
 * <dt>11/05/2004:</dt>
 * <dd>SO_TIMEOUT in {@link HTTPRequestClient} was causing client to have to wait for a long time before
 * figuring out that it could not connect to the server.  This settin was
 * removed so that it will fail faster.
 *
 * New {@link MEMERelaEditorKit} method for adding RELA values (to support all fields).
 * Now supported on server side with an action.
 *
 * {@link ActionClient} now provides access to {@link gov.nih.nlm.meme.action.WorkLog}
 * and {@link gov.nih.nlm.meme.action.Activity}
 * objects based on <tt>work_id</tt> and <tt>transaction_id</tt> values.
 *
     * {@link ConceptMappingClient} now supports read and write methods.  Implements
 * functions on the cui_map table.
 *
 * {@link AuxiliaryDataClient} now supports editing of sources, termgroups,
 * languages, editor preferences, and the semantic type list.
 *
 * {@link EditingClient} now supports all new methods of sub-clients.
 *
 * </dd>
 * <dt>09/19/2004:</dt>
 * <dd>Improvements/changes to {@link ContentViewClient}. New {@link ConceptMappingClient}.
 * {@link HTTPRequestClient} uses 30 min SO_TIMEOUT.  {@link ClientAPI} initiates/terminates
 * sessions with null mid service which should solve the problem with the data source pool
 * overflow.
 * </dd>
 * <dt>07/20/2004:</dt>
 * <dd>Small bug fix for {@link AdminClient}<code>.getTransactionLog</code>, it should now
 * properly use a data source.  {@link ContentViewClient} was updated to have <code>setContentView</code>
 * method.
 * </dd>
 * <dt>07/09/2004:</dt>
 * <dd>New {@link ContentViewClient} for managing content views.  We will build
 * a web application to leverage this.
 * </dd>
 * <dt>06/14/2004:</dt>
 * <dd>{@link CoreDataClient} has <code>getInverseRelationship</code>
 *     {@link EditingClient} updated to reflect new {@link CoreDataClient}
 *     method.
 * </dd>
 * <dt>05/06/2004:</dt>
 *          <dd>{@link CoreDataClient}, {@link ReportsClient}, and {@link EditingClient} now support
 *           a switch to include or exclude a set of languages (represented as strings)
 *           for reading content from the MID.
 *          </dd>
 * <dt>04/21/2004:</dt>
 *          <dd>{@link FinderClient} and {@link EditingClient} support
 *          <code>restrictByReleasable</code>
 *          </dd>
 * <dt>04/19/2004:</dt>
 *          <dd>{@link CoreDataClient} and {@link ReportsClient} now support
 *          the reading of non-ENG atoms (by default) and supply a mechanism
 *          for toggling between reading/not reading them.
 *          </dd>
 * <dt>03/17/2004:</dt>
 *          <dd>{@link EditingClient} was upgraded to correctly support
 *          the setting of host/port parameters.  {@link HTTPRequestClient}
 *          uses a buffered writer for the socket.  And RxNormClient
 *          works better if the worklist length is 0.
 *          </dd>
 * <dt>02/06/2004:</dt>
 *          <dd>{@link MappingClient} now fully implements its API, providing
 *          functions for deleting, creating, and editing
 *          {@link gov.nih.nlm.meme.common.MapSet}s,
 *          {@link gov.nih.nlm.meme.common.Mapping}s, and
 *          {@link gov.nih.nlm.meme.common.MapObject}s and their attributes.
     *           {@link CoreDataClient} supports richer set of methods for accessing
 *          core data, including a method for obtaining only sections of the
 *          relationships, instead of the entire amount.  It also now supports
 *          methods for directly accessing all types of core data.
 *          </dd>
 * <dt>01/16/2004:</dt>
 *          <dd>{@link ActionClient} now throws {@link gov.nih.nlm.meme.exception.StaleDataException}.
 *          {@link MergeEngineClient} has a better progress listener (prints out timestamps).
 *          {@link MappingClient} is now fully implemented, with the ability to create
 *          edit and delete {@link gov.nih.nlm.meme.common.MapSet} information.
 *          </dd>
 * <dt>12/29/2003:</dt>
 *          <dd>{@link MappingClient} supports create/delete methods.
 *          {@link MergeEngineClient} connects a progress listener and cleans
 *          up the log and progress methods a bit.
 *          </dd>
 * <dt>12/19/2003:</dt>
 *          <dd>Architecture for passing {@link ClientProgressEvent}s from
 *          the server back to the client is now in place. {@link ClientProgressListener}
 *          was added. {@link MEMERequestClient} and {@link ClientAPI} now
 *          support <code>addClientProgressListener</code> and
 *           <code>removeClientProgressListener</code>.
 *          {@link ReportsClient} was also updated to handle the new option
 *          which allows you to restrict the REVIEWED RELATIONSHIPS view to
 *          a particular number of rows (also supported by xreports.pl).
 *          {@link ReportsClient} supports the new option to restrict
 *          the view of REVIEWED relationships in a concept report.
 *          {@link MappingClient} can now provide read access to mappings.
 *          </dd>
 * <dt>12/15/2003:</dt>
 *          <dd>Updated javadocs for some clients.
 *          {@link HTTPRequestClient} was not totally thread-safe in that it
 *          was re-using serializer objects across requests.  We simply made
 *          these things local variables (which should carry little performance cost)
 *          and now multiple threads should be able to simultaneously access the client
 *          APIs without problems. {@link ClientAPI} calls <code>setSessionId()</code>
 *          now when initiating requests, allowing subclasses to override the method
 *          and add functionality to an <code>initiateSession()</code> request.
 *          {@link MappingClient} was implemented and provides read access
 *          to mappings within the MID.
 *          </dd>
 * <dt>12/04/2003:</dt>
 *          <dd>{@link AdminClient}, {@link ChangeServerDialog},
 *          {@link MergeEngineClient}, {@link PrintConceptReportAction},
 *          and RxNormClient have updated javadoc comments.
 *          {@link CoreDataClient} provides access to dead data elements.
 *          {@link ActionClient} gets passed back more information from
 *          the server than before, and comments were updated to reflect
 *          the fact that the authority should be set when using this client.
 *          {@link EditingClient} was updated to reflect {@link ActionClient}
 *          and {@link CoreDataClient} changes..
 *          The {@link ClientAPI} was upgraded to support control over
     *          the session timeout and all sub-clients now support the functionality
 *          via inheritance (there is a <code>setTimeout</code> method).
 *          </dd>
 * <dt>11/07/2003:</dt>
 *          <dd>{@link ActionClient}.processAction(MolecularAction) now sets
 *          the molecule_id of the molecular action.  {@link PrintConceptReportAction}
 *          now strips out the links from reports before printing, to increase
 *          printing performance.
 *          </dd>
 * <dt>10/31/2003:</dt>
 *          <dd>Fixed {@link TestReportFrame} copy/paste problem.
 *          {@link EditingClient} was updated to reflect the various
 *          sub-clients that it implements.
 *          </dd>
 * <dt>10/22/2003:</dt>
 *          <dd>{@link PrintConceptReportAction} was mistakenly printing only
 *          one page per sheet of paper, this was upgraded to 2.
 *          </dd>
 * <dt>10/16/2003:</dt>
     *          <dd>{@link TestReportFrame} and {@link PrintConceptReportAction} now
 *          are connected to printing API.
 *          </dd>
 * <dt>09/08/2003:</dt>
 *          <dd>The {@link ActionClient} main method was improved so that
 *          it properly handles errors and returns an error code.  This
 *          is to allow code like batch.pl to properly catch errors.  The
 *          client was also extended to attach a violations vector to
 *          an action passed to <code>processAction(MolecularAction)</code> so that
 *          the client-side can have access to integrity warnings.
 *          </dd>
 * <dt>08/25/2003:</dt>
 *          <dd>{@link ChangeServerDialog} changed to prevent duplicates in
 *          server list.  {@link PrintConceptReportAction} changed to
 *          fix formatting of links. {@link TestReportFrame} is now
 *          a {@link gov.nih.nlm.swing.GlassPaneListener}.  Also, we
 *          fixed the refresh bug in {@link PrintConceptReportAction}
 *          while the print dialog is open.
 *          </dd>
 * <dt>08/19/2003:</dt>
 *          <dd>{@link ActionClient} bug fix, it properly handles the
 *          IntegrityViolationException now.  Vlad reported a problem
 *          with the <code>processAction(MolecularAction)</code> method
 *          always reporting an exception (array out of bounds), this fixes
 *          the problem.  {@link PrintConceptReportAction} properly handles
 *          the new non-pre tag HTML concept reports.
 *          {@link AdminClient} has a new method for requesting the server
 *          switch the default mid service.
 *          </dd>
 * <dt>08/11/2003:</dt>
     *          <dd>{@link ActionClient} throws IntegrityViolationException now when
 *          processing molecular actions (making it easier for Jekyll to handle it).
 *          {@link ReportsClient} now returns {@link gov.nih.nlm.meme.action.ActionReport}
 *          objects which encapsulate the data involved in an action, and supports
 *          the old-style access via <code>getActionReportDocument()</code>.
 *          </dd>
 * <dt>08/01/2003:</dt>
 *          <dd>{@link ActionClient}'s <code>main</code> method was updated
 *          to support <code>$MEME_HOME/bin/{un,re}do.pl</code>.
 *          The {@link PrintConceptReportAction} and {@link TestReportFrame}
 *          were updated to support correct printing of the concept reports
 *          from Jekyll.
 *          </dd>
 * <dt>07/16/2003:</dt>
 *          <dd>{@link TestReportFrame} scrolls to top of report explicitly,
 *          when any of the <Code>setXXX</code> methods are called.
 *          {@link TestFinderFrame} no longer has synchronized <code>setResults</code>
 *          method, this was causing display refresh problems on the sun.
 *          </dd>
 * <dt>06/27/2003:</dt>
 *          <dd>Bug fix in {@link ActionClient}.
 *          </dd>
 * <dt>06/06/2003:</dt>
     *          <dd>{@link ActionClient} now sets the relationship_id, attribute_id,
 *          or atom_id when inserting this data.  It also sets the target
 *          id of a MolecularSplitAction and the source id of a MolecularInsertConceptAction.
 *          RxNormClient does a slightly better job at handling
 *          concept worklists where there are > worklist_size*2 rows in the
 *          worklist that comes back (due to CDs being connected to >1 SCD).
 *          </dd>
 * <dt>06/06/2003:</dt>
 *          <dd>Minor bug in {@link EditingClient} fixed.
 *          {@link AuxiliaryDataClient} now provides better access
 *          to integrity system. {@link AdminClient} supports
 *          <code>killServer</code> method to kill the server
 *          even if other threads are running. {@link ClientToolkit}
 *          has an additional initialize method that takes a properties
 *          object instead of reading from a file.
 *          {@link TestFinderFrame} given additional logic in <code>main</code>
 *          method to support java web start.
 *          </dd>
 * <dt>05/08/2003:</dt>
 *          <dd>RxNormClient has better error reporting.
 *          </dd>
 * <dt>05/08/2003:</dt>
     *          <dd>{@link MergeEngineClient} no longer manages access to its session
 *          logs internally, it uses {@link gov.nih.nlm.meme.server.AdminService}.
 *          RxNormClient was upgraded to support faster worklist reads.
 *          {@link AdminClient} was upgraded to support access to server logs,
 *          session logs, and transacton logs (all with the head/tail API).
 *          </dd>
 * <dt>04/25/2003:</dt>
 *          <dd>{@link ActionClient} was updated to fix a bug in communicating the
 *          target_id back to the application. {@link CoreDataClient} was
 *          updated to throw MissingDataExceptions from the various </code>getConcept</code>
 *          methods, this is so client applications can safely handle this type of
 *          error.  {@link EditingClient} the <code>getCurrentEnglishSource</code> methods
     *          where renamed <code>getCurrentEnglishSource<b>s</b></code>. Finally,
 *          the RxNormClient was given two additional methods for reading
 *          worklists in a parallelized way.
 *          All clients were updated to cast exceptions returned by the
 *          server to {@link gov.nih.nlm.meme.exception.MEMEException} instead
 *          of to {@link gov.nih.nlm.meme.exception.ClientException}.
 *          </dd>
 * <dt>04/09/2003:</dt>
 *          <dd>{@link CoreDataClient} has now a <code>populateContextRelationships</code>
 *          method for reading context rels when so desired.  Debugging <code>System.out.println</code>
 *          statements where cleaned up. RxNormClient now has
 *          methods for reading the worklist like an iterator.  These methods
 *          support making multiple requests to the server to prevent it
 *          from timing out, and eventually can be extended to support reading
 *          different segments in parallel to maximize performance. {@link ActionClient}
 *          now feeds-back the target id of molecular actions and correctly populates
 *          the target concept (where used).
 *          </dd>
 * <dt>04/01/2003:</dt>
 *          <dd>
 *          RxNormClient has access to the worklist
 *          contents, allowing large worklists to be read without error.
 *          {@link EditingClient} supports a <code>getCurrentEnglishSources</code>
 *          method as does {@link AuxiliaryDataClient}. {@link CoreDataClient}
 *          was reconfigured to provide faster access to core data.  Reading
 *          a concept now leaves out the relationships, but a
 *          <code>populateRelationships</code> method allows rels to be
 *          read when needed (also for context relationships).
 *          </dd>
 * <dt>03/19/2003:</dt>
 *          <dd>{@link TestReportFrame} was given a "Print" button.
 *          {@link ActionClient} main method was cleaned up to make it
 *          easier to call.
 *          </dd>
 * <dt>03/05/2003:</dt>
 *          <dd>
 *          GUI Actions {@link ChangeServerAction} and {@link ChangeDataSourceAction}
 *          were added to support GUI applications (along with the supporting classes).
 *          {@link TestFinderFrame was updated to serve as an example}.
 *
     *          {@link ActionClient} was extended to support batch and macro actions.
 *
     *          {@link MergeEngineClient} and {@link ActionClient} have main methods
 *          that interact cleanly with the new scripts: <code>insert.pl</code>,
 *          <code>batch.pl</code>, and <code>merge.pl</code>.  This is one part
 *          of the effort to migrate completely from MEME3 to MEME4.
 *
 *          {@link AuxiliaryDataClient} was given a <code>getNextIdentifierForType</code>
 *          method for obtaining new transaction ids.
 *          </dd>
 * <dt>01/13/2003:</dt>
 *          <dd>The {@link AdminClient} and {@link WorklistClient} were
 *          updated to support action harvester and admin web applications.
 *          </dd>
 * <dt>11/13/2002:</dt>
 *          <dd>the {@link FinderClient} was upgraded to support action
 *          lookups and the {@link ReportsClient} was upgraded to support
 *          the action harveseter (action and editing reports).
 *          </dd>
 * <dt>08/28/2002:</dt>
 *          <dd>Official release of the editing APIs, including:
 *              {@link ActionClient}, {@link AuxiliaryDataClient}
 *              {@link CoreDataClient}, {@link FinderClient},
 *              {@link WorklistClient}, and the super-client
 *              {@link EditingClient}.  The {@link EditingClient}
 *              is intended as a single access point for all
 *              MEME editing services.  It should serve as an API
 *              that supplies all of the needs of a prospective
 *              Java MEME editing interface.
 *
 *              A small GUI application {@link TestFinderFrame}
 *              was relased to leverage the APIs.
 *
 *              Finally, this is the official first release of the
 *              RxNormClient used by the RxNorm editor
 *              to edit SCD graphs.
 * </dd>
 * <dt>05/13/2002:</dt>
 *          <dd> {@link MEMERelaEditorKit} was upgraded to support
 *               actions on relationships spanning concept ids.
 * </dd>
 * <dt>04/22/2002:</dt>
 *          <dd> {@link AuxiliaryDataClient} was added.  It provides
 *               client access to non-core mid data.
 * </dd>
 * <dt>03/26/2002:</dt>
 *          <dd> {@link ReportsClient} was extended to support styles.
 *               This class now has the full functionality available
 *               to <code>$MEME_HOME/bin/xreports.pl</code>.<p>
 *               Also, {@link MEMERelaEditorKit} was extended with a
 *               <code>insertRelationshipAttribute</code> method that
 *               allows it to add RELAs and their inverses to the system.
 * </dd>
 * <dt>03/05/2002:</dt>
 *          <dd> {@link AdminClient} was added to consolidate administrative
 *               services.  LogClient was deprecated and made to just
 *               call {@link AdminClient} methods. {@link ReportsClient}
 *               nas an additional method that supports
 * </dd>
 *
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
  private final static String version_date = "28-Aug-2002 00:00:00";
  private final static String package_name = "gov.nih.nlm.meme.client";

  //
  // Methods
  //

  /**
   * Returns the package current release.
   * @return An <code>int</code> representation of package current release.
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
   * @return An object {@link String} representation of package current version
   * authority.
   */
  public static String getAuthority() {
    return version_authority;
  }

  /**
   * Returns the package current version date.
   * @return An object {@link Date} representation of package current version
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
   * @return An object {@link String} representation of package current name.
   */
  public static String getPackage() {
    return package_name;
  }

  /**
   * Returns the package current version information.
   * @return An object {@link String} representation of package current
   * version information.
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
   * @param argv An array of arguments.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.meme.client.Version.getVersionInformation());
  }
}
