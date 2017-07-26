/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class provides package version information
 * for gov.nih.nlm.meme.sql package.
 *
 * <b>History</b>
 * <dl>
 * <dt>08/15/2005:</dt>
 * <dd>Suppressible implementation.
 * </dd>
 * <dt>06/17/2005:</dt>
 * <dd>{@link LongAttributeHandler} makes use of new mrd_stringtab table structure.
 * {@link MappingMapper} improved.
 * {@link MEMEConnection} includes small changes for better handling of map sets.
 * </dd>
 * <dt>03/25/2005:</dt>
 * <dd>{@link Ticket}'s getReportsTicket method now adds a data restriction
 * clause for attributes to ignore XMAP, XMAPTO and XMAPFROm attributes.
 * This should make reading map set concepts for Jekyll or the concept
 * reports considerably faster.
 * </dd>
 * <dt>03/11/2005:</dt>
 * <dd>{@link AttributeMapper} and {@link LongAttributeMapper} show the
 * source of a context when formatting it, not just the source of the atom
 * it is connected to.
 * {@link ActionEngine} code cleaned up a little bit.
 * {@link ResultSetIterator} handles errors better.
 * </dd>
 * <dt>01/25/2005:</dt>
 * <dd>
 * {@link ActionEngine} fix for stamping.  Read all concepts into a list
 * before approving.  This should prevent "fetch out of sequence" error.
 * {@link MEMEConnection} improvements to cui assignment algorithm.
 * {@link MIDConnection} reports better errors when a worklist/checklist
 * cannot be read properly.
 * </dd>
 * <dt>01/14/2005:</dt>
 * <dd>
 * {@link LongAttributeMapper} shows attribute_id in error messages.
 *
 * {@link MEMEConnectionQueries} uses a non-null tobereleased value when the
 * preferred atom on the other side of a rel cannot be found.
 *
 * {@link ValidatingActionEngine} does not try to re-read target concept
 * after undoing a split.
 *
 * Various mappers were updated to use java.util.Date objects instead
 * of java.sql.Date or java.sql.Timestamp objects.
 * </dd>
 * <dt>12/13/2004:</dt>
 * <dd>
 * {@link ActionEngine} has more rigorous methodology for handling actions and
 * inter-operating with MRD.
     * {@link MEMEDataSourceFactory} accepts a name like "apollo.lexical.com_apelon"
 * where it is machine/sid separated by _.  If this form exists without a _, it
 * is assumed that the machine has the suffix ".nlm.nih.gov".
     * {@link MEMEConnectionQueries} does not refer to <code>operations_queue</code>.
 * {@link MeshEntryTermMapper} ignores MSH translations.
 * {@link MIDDataSource} and {@link MEMEDataSource} now use <code>findActions</code>
 * instead of <code>findAuxiliaryDataActions</code>.  In fact, use of
 * AuxiliaryDataAction has been entirely deprecated.
     * {@link MEMEDataSource} and {@link MEMEConnection} also have deprecated use of
 * the aux data action.  Also code pertaining to work logs was moved to
 * {@link MEMEConnection}/{@link MEMEDataSource}.
 * </dd>
 * <dt>11/05/2004:</dt>
 * <dd>New {@link MIDActionEngine} to support the <tt>MEMEDataSourceAction</tt> and
 *     <tt>MIDDataSourceAction</tt>.
 *
 *     {@link ConceptMappingMapper} now reads from cui_map and does not pretend
 *     to be a {@link RelationshipMapper}.  this is a much cleaner solution.
 *
 *     {@link MEMEConnectionQueries} explicitly restricts atoms to dead='N'
 *     when reading "atoms with names".
 *
     *     {@link ActionEngine} has a much cleaner implementation.  It does a better
 *     job of logging SUI,ISUI,LUI,AUI assignments (and a better job of performing them).
 *     Some previous {@link MEMEConnection} methods that better belonged here are here.
 *
     *     {@link MEMEDataSourceFactory} assists in cacheing data at the right time.
 *
 *     {@link ValidatingActionEngine} now extends {@link MIDActionEngine}.
 *
 *     {@link MEMEDataSource} and {@link MIDDataSource} have many new methods
 *     for supporting all kinds of auxiliary table changes like editing editor
 *     preference information and new sources/termgroups.
 *
 *     {@link MEMEConnection} and {@link MIDConnection} implement all of
 *     these new methods.  In addition they use a more sophisticated
 *     cacheing architecture that segregates caches for different
 *     databases (allowing consistency across requests).  This is a VERY good
 *     improvement.
 *
 *     {@link AtomMapper} computes rank values instead of using RANK value
 *     from classes.  This is because classes will now use release termgroup
 *     ranks instead of editing termgroup ranks.
 *
 *     {@link AttributeMapper}and {@link RelationshipMapper} do
 *     a better job with SG_MEME_ID, SG_MEME_DATA_TYPE
 *     info.  A bug where attributes were being added twice was fixed.
 *
 * </dd>
 * <dt>08/19/2004:</dt>
 * <dd>{@link MEMEDataSource} and {@link MEMEConnection} upgraded to suport new
 * concept mapping API.  New class {@link ConceptMappingMapper} for reading
 * concept mappings.  {@link MEMEConnection} does more to close statements upon errors,
 * support the Content View API better.
 * </dd>
 * <dt>07/20/2004:</dt>
 * <dd>{@link RelationshipMapper} does a better job of connecting non-ENG atoms
 * to ENG atoms.
 * {@link MEMEDataSource} and {@link MEMEConnection} now support <code>setContentView</code>.
 * Also, adding a content view now assigns a unique id from max tab.
 * </dd>
 * <dt>07/09/2004:</dt>
 * <dd>{@link LongAttributeHandler} does a better job of handling errors.
     * New {@link MEMEDataSource} methods and {@link MEMEConnection} implementations
 * for handling inserts/removals/changes of content views and content view members.
 * Also, bug fix for the code which handles reading the inverse of a relationship.
 * </dd>
 * <dt>06/14/2004:</dt>
 * <dd>{@link ActionEngine} does a better job of closing cursors.
 *     {@link MEMEConnection} also does a better job of closing cursors and now
 *     supports reading of inverse relationships (by rel id).  {@link MEMEConnectionQueries}
 *     was updated to support this function as well.
 *     A small {@link MeshEntryTermMapper} change was made to only attempt mapping
 *     for current version MSH atoms.
 * </dd>
 * <dt>05/06/2004:</dt>
 *          <dd>{@link Ticket} and {@link MEMEConnection} now support
 *           parameters for explicitly including
 *           or excluding languages when reading atoms from the MID.
 *           {@link ActionEngine} and {@link MEMEConnection} have a bug fix
 *          to prevent deadlocking problem.
 *          {@link MEMEConnectionQueries} fixes a problem causing duplicate
 *          context rels to appear on concept reports.  Now we sort unique
 *          away by ignoring relationship_id when reading context rels.
 *          {@link MIDConnection} and {@link MEMEConnection} were updated
 *          to cache missing relationship attribute values from a new database.
 *          Javadocs for a few classes were cleaned up (especially those things
 *          used with {@link DataWriter}).
 *          </dd>
 * <dt>04/21/2004:</dt>
 *          <dd>All calls to <code>new Ticket()</code> were converted to
 *          <code>Ticket.getEmptyTicket()</code>.
 *          </dd>
 * <dt>04/19/2004:</dt>
 *          <dd>Bug fix to {@link ContextRelationshipMapper} to add rels as
 *          context relationships instead of as regular relationships.
 *          Bug fix to {@link AttributeMapper} to connect atom to attribute.
 *          {@link DataWriter} sets the fetch size to increase performance.
 *          {@link ResultSetIterator} handles exceptions better.
 *          {@link RelationshipMapper} connects translation atoms.
 *          {@link MEMEConnection} now fully supports non-ENG atoms.
 *          {@link Ticket} now supports reading non-ENG atoms (and sets it
 *          to <code>true</code> for actions ticket).
 *          </dd>
 * <dt>03/17/2004:</dt>
 *          <dd>{@link EnhancedConnection} now supports the ability to set/restore
 *          the sort and hash area sizes.  {@link AttributeMapper} and
     *          {@link RelationshipMapper} were upgraded to support complete mapping
 *          of their respective core data types, including finding the things
     *          they are attached to.  The {@link MEMEConnection} was updated to take
 *          advantage of this in the various <code>populateXXX</code> methods.
 *          </dd>
 * <dt>02/20/2004:</dt>
 *          <dd>{@link MEMEConnection} and {@link MEMEConnectionQueries} better
 *          handle the reading of language from classes, and better management
 *          of reading attributes/relationships attached to non-ENG atoms.
     *          Bug fix in {@link Ticket} for <code>ClassCastException</code>.  This
 *          fix lead to a patch released 20040219.
 *          </dd>
 * <dt>02/06/2004:</dt>
     *          <dd>Most of the changes in this release are here.  They include: <ul>
     *          <li>{@link MIDConnection} and {@link MEMEConnection} no longer leave
 *             open cursors.</li>
     *          <li>New mappers: {@link ConceptMapper} and {@link MapSetMapper}.</li>
     *          <li>{@link MEMEConnection} now uses a consistent way to build queries
 *              for reading core data.</li>
 *          <li>{@link MEMEConnection} now fully supports mappers for all core
 *              data types, in <code>get</code> and <code>populate</code> methods. </li>
 *          <li>{@link ContextRelationshipMapper}, {@link AttributeMapper} and
 *               {@link RelationshipMapper} are more
 *              flexible with respect to whether or not to read CONCEPT_ID and
 *              ATOM_ID fields.</li>
 *          <li>{@link Ticket} was cleaned up and now treats all core data objects
     *              consistently (no special exceptions for atoms). Also new options
 *              for specifying start/end (to read a range) are aupported</li>
 *          <li>{@link MEMEConnection} was updated to support all of the new
 *              changes and core data client methods.</li>
 *          </ul>
 *          </dd>
 * <dt>01/20/2004:</dt>
 *          <dd>{@link ActionEngine} and {@link ValidatingActionEngine} were modified
 *          to handle/throw {@link gov.nih.nlm.meme.exception.StaleDataException}.
 *          A method for locking concepts (and related concepts) was added to
 *          {@link MEMEDataSource} and {@link MEMEConnection}. {@link MEMEConnection}
 *          now also sets the read timestamp of a concept when it is populated.
 *          {@link MIDConnection} now only reads worklist and checklist names
 *          where the tables exist. {@link ActionEngine} has undergone a number
 *          of changes, including: better javadoc comments, better handling of
     *          null attribute values when computing md5 hash, implementation of new
     *          locking mechanism for molecular actions, validation of data freshness
 *          during molecular actions, better locking for undo/redo actions.
 *          </dd>
 * <dt>12/29/2003:</dt>
 *          <dd>{@link ActionEngine} was passing RUI for source_rui, ATUI for
 *          source_atui and AUI for source_atui.  We will have to correct
 *          the bogus data.
 *          </dd>
 * <dt>12/15/2003:</dt>
 *          <dd>Changes to {@link MEMEDataSource} and {@link MEMEConnection},
 *          {@link Ticket} and {@link MappingMapper} to support the read-access
 *          of mapping objects in the MID.
 *          </dd>
 * <dt>12/04/2003:</dt>
 *          <dd>Cosmetic change to {@link MEMEConnection} javadocs.</dd>
 * <dt>12/01/2003:</dt>
     *          <dd>{@link ActionEngine} now checks nullability of all fields before
 *          calling aproc_insert actions. {@link MEMEDataSource} and
 *          {@link MEMEConnection} now have a method to remove events from
 *          the <code>operations_queue</code> table (<code>removeFeedbackEvent</code>).
 *          The {@link MEMEConnection} was also updated to improve some error
 *          messages.  Finally {@link MappingMapper} was implemented to read
 *          mapping information properly.
 *          </dd>
 * <dt>11/07/2003:</dt>
 *          <dd>{@link ActionEngine} does not read the correct concept status
 *          when inserting rels. {@link MEMEConnectionQueries}, {@link
 *          RelationshipMapper}, and {@link AttributeMapper} now handle
 *          the additional relationship/attribute fields (e.g. source_rui).
 *          </dd>
 * <dt>10/22/2003:</dt>
 *          <dd>{@link ActionEngine} reads the correct concept status
 *          before processing batch inserts.  This is required because the
 *          various insert actions now don't read concept info.
 * <dt>10/22/2003:</dt>
 *          <dd>{@link MEMEConnectionQueries} and {@link AtomMapper} now
 *          properly read source_cui, source_dui, and source_aui.  We should
 *          make similar changes to support new relationships and attributes fields.
 *          {@link ActionEngine} also inserts atom LUIs using the base string.
 *          </dd>
 * <dt>10/15/2003:</dt>
 *          <dd>{@link MEMEDataSource} and {@link MEMEConnection} now throw
 *          DataSourceException if the user name cannot be determined.  This
 *          precipitated changes to {@link ActionEngine} as well. Also,
 *          {@link MEMEConnection} was not failing when trying to populate
 *          a non-existent attribute_id, this was fixed.
 *          </dd>
 * <dt>10/01/2003:</dt>
 *          <dd>{@link ActionEngine} supports new MEME_APROCS procedures
 *           This means that the new aprocs must be released with the
 *           new code, or we release MEME_APROCS first, let it settle,
 *           then release this code.  It also no longer manually assigns
 *           the AUI in the code, but lets MEME_APROCS handle it.
 *           {@link SourceAtomMapper}, {@link SourceAttributeMapper},
 *           and {@link SourceRelationshipMapper} were also extended
 *           to support the additional fields parameters.
 *           </dd>
 * <dt>09/05/2003:</dt>
 *          <dd>{@link MEMEConnectionQueries} was updated to read atomic
 *          actions and order by atomic action id.  {@link ActionEngine}
 *          was enabled to pass the MD5 hash of the attribute value
 *          to the corresponding atomic action (when inserting an attribute).
 *          This was temporarily disabled until the proper MEME_APROCS could
 *          be released.
 *          </dd>
 * <dt>08/25/2003:</dt>
 *          <dd>{@link MIDConnection} was returning concept worklists and
 *          checklists with duplicate concepts.  The queries were rewritten
 *          to produce lists without duplicates.
 *          </dd>
 * <dt>08/18/2003:</dt>
 *          <dd>{@link MEMEConnection} was fixed to ensure that the related
     *          atom of S level rels is ALWAYS set, even if an action ticket is used.
 *          {@link MIDConnection} fixed to read concept information for
 *          dead concepts (for action reports).
     *          {@link MolecularActionQueryBuilder} now builds action lookup queries
 *          to find cases where the concept_id is the target id.
 *          </dd>
 * <dt>08/06/2003:</dt>
 *          <dd>{@link MEMEConnectionQueries} was updated to read the releasability
 *          of the "related atom" of an "atom level" relationship.  This allows
 *          the {@link gov.nih.nlm.meme.server.ReportsGenerator} to show that ostensibly "releasable"
     *          relationships are actually not if they are connected to unreleasable
 *          atoms.  {@link PreferredAtomMapper} was upgraded to look for
 *          the new tobereleased field in the queries and set it in the atom
 *          object being mapped to.  There were several changes to {@link ActionEngine}.
     *          First, we fixed a bug in how AUIs for NLM02 atoms were being assigned
     *          (so that it matches <code>MEME_SOURCE_PROCESSING.assign_auis</code>.
 *          Second, when dealing with native identifiers, the code now better
 *          supports null qualifier values.  Third, a bug in the logic for
 *          deciding when to recalculate the preferred atom of a concept was
 *          fixed.  Finally, some poorly chosen method names were changed
 *          (private methods).
 *          </dd>
 * <dt>07/16/2003:</dt>
 *          <dd>{@link MEMEConnectionQueries}, {@link Ticket}, and
 *          {@link MEMEConnection} were updated to read
 *          {@link gov.nih.nlm.meme.common.ContextPath} information.
 *          <code>DoseFormMapper</code> was fixed to correctly read the
 *          phase type.  {@link ActionEngine} only recomputes concept
 *          preferred atom if it needs to be recomputed.
 *          {@link ContextPathMapper} is used to map the context
 *          path query to atom objects.
 *          </dd>
 * <dt>06/27/2003:</dt>
     *          <dd>The <code>getAtomChecklist</code>, <code>getAtomWorklist</code>,
     *          <code>getConceptChecklist</code>, and <code>getConceptWorklist</code>
 *          methods should now return every concept on the worklist (in
 *          {@link MIDConnection}.  The queries were changed for the
 *          atom worklist/checklist to get every atom in every concept
 *          represented by the atoms on a worklist/checklist, instead of just
 *          getting every atom on the worklist in its current concept.
 *          </dd>
 * <dt>06/20/2003:</dt>
 *          <dd><code>DoseFormMapper</code> uses the context parent treenumbers to
 *          determine if a dose form is liquid or solid, we changed parent treenumbers
     *          to use AUIs, so we now updated the mapper to also use auis instead of
 *          atom ids.
 *          </dd>
 * <dt>06/19/2003:</dt>
 *          <dd>{@link MIDConnection} has additional support for integrity system.
     *          {@link ActionEngine} uses language when assigning string identifiers.
 *          </dd>
 * <dt>06/06/2003:</dt>
 *          <dd>{@link MIDConnection} and {@link MIDDataSource} support better
 *          interface with the integrity objects.  {@link ActionEngine} tracks
 *          transaction id for batch actions better and also handles
 *          relationship sg_* parameters better.
 *          </dd>
 * <dt>05/16/2003:</dt>
 *          <dd>{@link SourceAttributeMapper} and {@link SourceRelationshipMapper}
     *          are sg_id enabled.  The {@link Ticket} supports a switch for reading
 *          non-english atoms.  The MEMEConnectionQuery queries for
 *          reading classes were updated to support non-ENG rows.  {@link MEMEConnection}
 *          was updated to read/not read non-english atoms based on the ticket
 *          switch (<code>populateConcept</code> was updated).  {@link ActionEngine}
 *          was fixed to correctly support <code>aproc_insert_attribute</code>.
 *          </dd>
 * <dt>05/08/2003:</dt>
 *          <dd>Expanded support for "native identifiers".  The {@link AttributeMapper},
 *          {@link RelationshipMapper}, {@link ActionEngine},
 *          and {@link MEMEConnectionQueries} now all deal
     *          with these fields. {@link MEMEDataSource} and {@link MEMEConnection}
 *          now have the <code>findMolecularAction</code> methods instead of {@link MIDDataSource}
 *          and {@link MIDConnection}.  Additionally, we added <code>getApplicationVectors</code>
     *          and <code>getOverrideVectors</code> methods to {@link MIDConnection}
 *          and {@link MEMEConnection}.  Also, clustering of worklists and
 *          checklists was fixed in {@link MIDConnection}.
 * <dt>04/25/2003:</dt>
 *          <dd>{@link MIDConnection} and {@link MEMEConnection} now make use
 *          of MissingDataException.  A {@link MEMEConnection} bug
 *          in <code>populateAtom</code> which was causing the RxNorm status
 *          to be improperly set was fixed.  The {@link gov.nih.nlm.meme.common.CoreData}
 *          <code>getSourceIdentifier</code> method was renamed to be
 *          <code>getSrcIdentifier</code> and this change was propagated to
 *          {@link ActionEngine}.
 *          </dd>
 * <dt>04/09/2003:</dt>
 *          <dd>{@link AttributeMapper} was fixed to correctly create
 *          {@link gov.nih.nlm.meme.common.ConceptSemanticType}s.
 *          Changes to {@link MEMEConnection} to correctly populate
 *          concepts and atoms with self-referential relationships.
 *          Streamlining for performance.
 *          </dd>
 * <dt>04/01/2003:</dt>
 *          <dd>{@link ActionEngine} was cleaned up to prevent null pointer
 *          exceptions.  A variety of mappers were implemented including
 *          {@link PreferredAtomMapper}, {@link RelationshipMapper},
 *          {@link AttributeMapper}, {@link LongAttributeMapper}, {@link
 *          ContextRelationshipMapper}.  The {@link MEMEConnection} methods
 *          for populating core data were standardized to use the mapping
 *          functions instead of defining ad-hoc mapping functions.  Also
 *          reading of relationships with names was made more efficient
 *          with additional queries in {@link MEMEConnectionQueries} involving
 *          an outer join.
 *          </dd>
 * <dt>03/19/2003:</dt>
 *          <dd>{@link ActionEngine} was made more stable with respect to
 *          calling MEME_APROCS.  In particular, when translating objects
 *          to MEME_APROCS function parameters, we perform a null-check first
 *          to ensure that {@link NullPointerException}s are not thrown.
 *          {@link MEMEConnection} small bug fix to find methods to support
 *          <code>insert.pl</code>.
 *          {@link DataWriter} uses a higher prefetch value for queries,
 *          this is mostly for MRD.
 *          </dd>
 * <dt>03/05/2003:</dt>
 *          <dd>
 *          Extended the {@link Ticket} notion of a {@link AtomMapper}
 *          to apply to attributes and relationships as well.  We implemented
 *          "source" mappers which will read from a table with a limited set
 *          of fields designed to be inserted from (i.e. insert.pl fields).
 *
 *          {@link MEMEConnection} was upgraded to take advantage of the
 *          ticket changes, and in particular now supports fining/reading of
 *          information from arbitrary tables/queries instead of the static
 *          core tables, although the default ticket still supports that.
 *
 *          {@link ResultSetMapper} and {@link ResultSetIterator} were
 *          updated to support mapping more in the style of the core data
 *          mappers.  This affects MRD code somewhat.
 *          </dd>
 * <dt>11/13/2002:</dt>
 *          <dd> There are four major areas of development:
 *               <ol>
 *                 <li>Methods were added to support lookup of molecular
 *                     actions by search parameters.  This is to support
 *                     generic "action harvester" functionality.</li>
 *                 <li>Methods were also added to support the looking up
 *                     of "dead" core data.  This supports the ability to
 *                     generate detailed action reports that indicate
 *                     exactly what it is that an action did.</li>
 *                 <li>Support for the  RxNorm "branded" data model,
 *                     however these changes will not be released until the
 *                     next version.</li>
 *                 <li>Support for AUI maintenance.</li>
 *               </ol>
 *               These development efforts contributed to some significant
 *               changes in the builder classes used to create SQL queries
 *               for the variuos find* functions of MID/MEME data sources.
 *          </dd>
 * <dt>08/28/2002:</dt>
 *          <dd> This relese brings a large number of changes.
 *               The {@link AtomMapper} architecture was engineered
 *               to support a variety of context-specific atom classes.
 *               Most notably, the <code>RxNormComponentMapper</code>,
 *               <code>RxNormMapper</code>} and <code>DoseFormMapper</code>
 *               support data structures used in RxNorm editing.
 *
 *               The {@link MIDConnection} was significantly upgraded
 *               to support a variety of worklist and finder functions
 *               to support the new client APIs.  Included in this
 *               work was the development of a set of {@link QueryBuilder}
 *               objects for mapping search parameters to queries.
 *
 *               Most other changes involve bug fixes, refinements
 *               and additional changes to support the new client
 *               side APIs.
 * </dd>
 * <dt>06/20/2002:</dt>
 *          <dd> {@link MEMEConnection} was fixed to faithfully
 *               represnt sources of relationships, even if they
 *               are things like E-... These are given all
 *               of the attributes of MTH but an SAB of E-...
 *               The connection was also upgraded with a variety
 *               of <code>findXXX</code> methods that return
 *               iteraters over queries allowing applications to
 *               lookup data without having to read it all into memory.
 *               A rich mechanism for populating atoms outside
 *               independently of a concept was built.  This is to
 *               support applications like the RxNorm editor.
 *
 *               Queries were split out of {@link MEMEConnection}
 *               and put into {@link MEMEConnectionQueries}. The
 *               Queries for reading atom-level information were
 *               added.  Queries for reading relationships were
 *               changed to allow self-referential (at concept level)
 *               relationships to be read in both directions so
 *               they could be connected to both atoms.
 *
 *               {@link ActionEngine} was implemented and debugged.
 *               This implementation supports the molecular actions.
 *
 *               {@link Ticket} was moved from <code>common</code>
 *               package.  This was to support the whole mechanism
 *               for handing the {@link AtomMapper}, which allows
 *               source-specific atom classes to be created when
 *               reading an atom/concept.  This mechanism should
 *               eventually be extended to all core data.
 *
 * </dd>
 * <dt>05/13/2002:</dt>
 *          <dd> {@link MEMEDataSource} and {@link MEMEConnection}
 *               were upgraded to support {@link ActionEngine}
 *               implementation.  Also, we changed the cacheing
 *               strategy for sources and termgroups in {@link MEMEConnection}
 *               so that an application can request a source that
 *               does not exist in the default database and no
 *               weird exception is thrown.
 * </dd>
 * <dt>04/22/2002:</dt>
 *          <dd> Significant changes to {@link MEMEConnection} and
 *               {@link MEMEDataSource} to provide enhanced access
 *               to the data.  Also, changes made by TTN to support
 *               MRD were incorporated into MEMEConnection.
 *               {@link MEMEDataSourceFactory} no longer requires
 *               the existence of the various -jdbc service names
 *               to operate, it will accept "editing-db" or
 *               "oa_mid2003" style mid service names.
 * </dd>
 * <dt>04/04/2002:</dt>
 *          <dd> {@link MEMEDataSourceFactory} was setting data source
 *               names to things like "oa.nlm.nih.gov_mid2003". This was
 *               fixed.
 *               {@link MEMEConnection} and {@link EnhancedConnection} were
 *               thorougly reviewed to make sure that all opened statements
 *               are explicitly closed.
 * </dd>
 * <dt>03/26/2002:</dt>
 *          <dd> {@link MEMEDataSource} and {@link MEMEConnection} were
 *               extended with <code>add/removeRelationshipAttribute</code>.
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
  private final static String package_name = "gov.nih.nlm.meme.sql";

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
   * @return the {@link String} representation of package current version
   * authority.
   */
  public static String getAuthority() {
    return version_authority;
  }

  /**
   * Returns the package current version date.
   * @return the {@link Date} representation of package current version
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
   * @return the {@link String} representation of package current name.
   */
  public static String getPackage() {
    return package_name;
  }

  /**
   * Returns the package current version information.
   * @return the {@link String} representation of package current
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
   * @param argv An array of argument.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.meme.sql.Version.getVersionInformation());
  }
}
