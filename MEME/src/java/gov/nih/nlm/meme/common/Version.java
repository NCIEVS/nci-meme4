/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class provides package version information
 * for gov.nih.nlm.meme.common package.
 *
 *
 * <b>History</b>
 * <dl>
 * <dt>08/15/2005:</dt>
 * <dd>Suppressible implementation. {@link CoreData} has a default value of "N" for suppressible.
 * </dd>
 * <dt>06/17/2005:</dt>
 * <dd>Many changes to javadocs.  Small changes to {@link MapSet} to support
 * editor/viewer.
 * </dd>
 * <dt>03/11/2005:</dt>
 * <dd>{@link Termgroup} is {@link Comparable}
 * </dd>
 * <dt>01/25/2005:</dt>
 * <dd>{@link Concept}'s <coce>getCUIs</code> method reports atom last_assigned_cui
 * values as well as last_release_cui  values.
 * </dd>
 * <dt>12/30/2004:</dt>
 * <dd>Minor documentation changes, no real updates.
 * </dd>
 * <dt>12/13/2004:</dt>
 * <dd>
 * Many main methods were moved to a test suite freeing up individual
 * classes from doing their own testing.
 * {@link Termgroup} supports <code>get/setNotes</code>.
 * {@link LoggedError} for <code>meme_errors</code> table.
 * {@link Source} supports all <code>sims_info</code> fields.
 * {@link ATUI}, {@link AUI}, {@link CUI}, {@link LUI},
 * {@link ISUI}, {@link RUI}, {@link SUI}, {@link StringIdentifier}
 * were changed to have more relaxed restrictions and to have <codE>int</code>
 * constructors.
 * {@link Concept} updated so that "current MSH main heading" logic does
 * not apply to MSH translation atoms.
 * </dd>
 * <dt>09/19/2004:</dt>
 * <dd>
 *   Major change was an overhaul of the molecular and atomic actions.
 *   All were moved to the gov.nih.nlm.meme.action package and the
 *   classes in this package were updated ot extend those other action
 *   package classes for backwards compatability.  Each was deprecated.
 *
 *   Many classes underwent minor documentation changes.
 *
 *   {@link Source} was expanded to include all fields required for
 *   MRSAB editing.
 *
 *   {@link Language} object has get/setISOAbbreviation() methods to fully
 *   support adding new languages.
 *
 *   New {@link MetaCode} and {@link MetaProperty} objects to represent
 *   <tt>code_map</tt> and <tt>meme_properties</tt> tables.
 *
 *   {@link Termgroup} was expanded to include all fields required for
 *    adding new termgroups during a source insertion.
 *
 *   {@link ContentView} was expanded to include additional fields. Also
 *   extended to support SUIs in the list.
 * </dd>
 * <dd>{@link ContentView} was updated to reflect changes suggested by WTH.
     * Some other javadoc was cleaned up.  Fixes to {@link gov.nih.nlm.meme.action.MolecularMergeAction} and
 * MolecularSplitAction to restore the constructors were made.
 * </dd>
 * <dt>07/20/2004:</dt>
 * <dd>MolecularSplitAction and MolecularMoveAction now move
 * non-ENG atoms connected to ENG atoms if they are in the same concept and
 * the ENG atom is moving as part of the split or move action.
 * {@link ContentView} updated to report whether or not it is auto-generated
 * using a query.
 * </dd>
 * <dt>07/09/2004:</dt>
 * <dd>Several of the actions have been updated to fully accomodate our testing suites.
     * These include: MolecularChangeAtomAction, MolecularMoveAction,
 * MolecularChangeAttributeAction, MolecularSplitAction,
 * MolecularChangeRelationshipAction.  The change actions now support
 * a much wider set of functionality. The move action supports moving concept
 * level rels and attributes, and the split action includes a bug fix.
 * New classes: {@link ContentView} and {@link ContentViewMember} to support
 * the new content view API.  Update to {@link Relationship} object to track
 * whether or not it represents the "inverse" of what is in the MID.
 * </dd>
 * <dt>06/14/2004:</dt>
 * <dd>MolecularChangeAtomAction supports changing source, termgroup, code.
 *     MolecularChangeAttributeAction supports changing source, attribute name,
 *     and attribute level.
 *     RxNormWorklistEntry now indicates whether or not the SCD name is
 *     the "dummy" name.
 * </dd>
 * <dt>04/19/2004:</dt>
 *          <dd>Bug in {@link Concept} fixed (clears relationships when clear is called).
 *          MolecularDeleteAtomAction deletes translation atoms when an ENG atom
 *          is deleted. MolecularSplitAction and MolecularMoveAction
 *          move translation atoms when the ENG atom is moved. {@link Atom} now has
 *          methods for tracking translation atoms.
 *          </dd>
 * <dt>03/17/2004:</dt>
     *          <dd>{@link CoreData} updated to now support adding of attributes and
 *          relationships so that any core data element can have attributes
 *          or relationships attached to it.  {@link NativeIdentifier} was upgraded
 *          to track the actual core data element that it represents.
 *          </dd>
 * <dt>02/20/2004:</dt>
 *          <dd>MolecularDeleteRelationshipAction  now supports deleting
 *           of attributes connected to the relationship.  {@link Relationship}
 *           now supports the attaching of attributes. {@link CUISource},
 *           {@link CodeSource} and {@link CodeTermgroup} now implement the extended
 *           {@link NativeIdentifier} API (which includes sg_meme_id, sg_meme_data_type).
 *          </dd>
 * <dt>02/05/2004:</dt>
 *          <dd>{@link CoreData} has better support for {@link NativeIdentifier}s.
 *          {@link Relationship} also has better support for {@link NativeIdentifier}s.
 *          {@link MapSet} supports <code>containsMapTo</codE> and <code>containsMapFrom</code>.
 *          MolecularAction has a better <code>toString</code> method.
 *          {@link NativeIdentifier} has extentions to allow it to be treated like
 *           a primitive for serialization purposes.  {@link CodeSource}, {@link CUISource},
 *          and {@link CodeTermgroup} were all changed to extend NativeIdentifier.  The
 *          class was generally updated to be more useful.
 *          </dd>
 * <dt>01/16/2004:</dt>
 *          <dd>Fixed a MolecularMergeAction bug relating to a hole in perform action *          <dd>Fixed a MolecularMergeAction bug relating to a hole in perform action
 *          logic when there are multiple demotions and concept level relationships involved.
 *          Changed some other actions to more aggressively read source concept (specifically
 *          for handling cases where concept status will change).
 *          </dd>
 * <dt>01/08/2004:</dt>
 *          <dd>MolecularMergeAction<code>.getConflictingRelationships()</code>
 *          now explicitly ignores SFO/LFO rels as they are releasable now.
 *          MolecularAction has a new <code>lockRelatedConcepts</code> method
 *          used by the action engine in a new locking scheme.  Many molecular actions were updated
 *          to support the new method.
 *
 *          {@link Concept} now tracks a read timestamp so we can identify
 *          when a client is using stale data.
 *          </dd>
 * <dt>12/29/2003:</dt>
 *          <dd>{@link CoreData} was conflating "source id" and "src id", this
 *          was fixed.
 *          </dd>
 * <dt>12/19/2003:</dt>
 *          <dd>{@link ISUI}, {@link LUI}, and {@link SUI} were updated to
 *          extend {@link StringIdentifier} instead of just {@link Identifier}.
 *          {@link MapSet} and {@link MapObject} were updated to reflect the
 *          final MRMAP proposal.  New classes {@link Locator}, {@link ConceptMapping},
 *          and {@link Coocurrence} were added (as new {@link CoreData} types).
 *          </dd>
 * <dt>12/15/2003:</dt>
 *          <dd>The mapping architecture was cleaned up a bit to make it work.
 *          It will change again with the next release based on the 12/8 MRMAP
 *          discussions.
 *          </dd>
 * <dt>12/04/2003:</dt>
 *          <dd>{@link MapSet}<code>.getIdentifier</code> -> <code>getMapSetIdentifier</code>.
 *          MolecularMoveAction applies {@link gov.nih.nlm.meme.integrity.MoveInhibitor}s now.
     *          MolecularInsertRelationshipAction reads the source concept if
 *          a SFO/LFO (only editor inserted source level rel) is inserted.  This should
 *          stop the Jekyll errors surrounding changing concept status.
 *          </dd>
 * <dt>12/01/2003:</dt>
     *          <dd>{@link Mapping}, {@link MapObject}, and {@link MapSet} were more
 *          fully fleshed out.  Javadoc comments on how to use
 *          MolecularInsertRelationshipAction were updated.
 *          {@link PasswordAuthentication} was given a <code>toString()</code>
 *          method to better report error messages.
 *          </dd>
 * <dt>11/07/2003:</dt>
 *          <dd>Molecular actions were cleaned up one last time.  Now, when
 *          reading of concepts needs to happen it does and the documentation matches.
 *          Also, initial classes for mapping were installed.
 *          </dd>
 * <dt>10/31/2003:</dt>
 *          <dd>Fixed bugs in actions.
 *          </dd>
 * <dt>10/23/2003:</dt>
 *          <dd>Fixed MolecularMergeAction<code>.getConflictingRelationships()</code>.
 *          </dd>
 * <dt>10/22/2003:</dt>
 *          <dd>Fixed the various molecular actions which do not need to read the concept
 *          before performing the action.
 *          </dd>
 * <dt>10/01/2003:</dt>
 *          <dd>Fixed the RxNormComponentAtom <code>isExpressedByBaseIngredient</code>
 *          method to choose longest of two matchiing ingredients.  Before it
 *          was choosing the shortest. MolecularMergeAction only looks
 *          at releasable relationships when computing the set of conflicting rels.
 *          </dd>
 * <dt>09/05/2003:</dt>
 *          <dd>MolecularMergeAction now correctly implements
 *          the <code>getConflictingRelationships</code> method, returning
 *          conflicts among C (or MSH S) level relationships.
 *          </dd>
 * <dt>08/25/2003:</dt>
 *          <dd>RxNormComponentAtom had logic that would allow the
 *          "base" and "precise" ingredients to get mixed up (if the precise
 *          had a shorter name), even if the precise was marked with a
 *          "precise_ingredient_of" rel.  We fleshed out the logic to allow
 *          precise ingredients to be better differentiated from base ingredients.
 *          </dd>
 * <dt>08/19/2003:</dt>
 *          <dd>Bug fix to ActionReport.
 *              Bug fix to MolecularMoveAction to properly handle P level rels.
 *              Bug fix to MolecularMergeAction regarding changing of
 *              relationship status more than once.
 *              Refined RxNormAtom and RxNormWorklistEntry
 *              logic.
 *          </dd>
 * <dt>08/11/2003:</dt>
 *          <dd>New ActionReport object, for obtaining data about actions.
 *          RxNormAtom minor bug fix to how it tracks drug and form atom.
 *          MolecularMergeAction will not allow merging of same concept
     *          into itself.  {@link ReportStyle} supports the LEGACY CODE(S) section
 *          of the concept reports.
 *          </dd>
 * <dt>08/01/2003:</dt>
 *          <dd>RxNormWorklistEntry was created to facilitate processing
 *          of RxNorm content (in <tt>gov.nih.nlm.meme.server.RxNormService</tt>).
 *          RxNormAtom was augmented to track the RxNorm SCDF and SBDF
 *          atoms via the <code>getDrugAndFormAtom()</code> method.  Some
 *          action classes were adapted to better handle a null authority
 *          value (which should never happen).
 *          </dd>
 * <dt>07/16/2003:</dt>
 *          <dd>{@link ContextPath} updated to contain a list of atoms
 *          to the root of the context tree.
 *          </dd>
 * <dt>06/19/2003:</dt>
 *          <dd>MolecularSplitAction has an additional flag to
 *          toggle when semantic types should be copyied during a split.
 *          </dd>
 * <dt>06/06/2003:</dt>
 *          <dd>{@link Coocurrence} added. Minor fixes to javadocs.
 *          </dd>
 * <dt>05/21/2003:</dt>
 *          <dd>{@link ReportsRelationshipRestrictor} had a bug preventing
 *          status N P level rels from showing up if there were any matching
 *          rels (this was discovered during the CSP2003 test insertion).  The
 *          problem was fixed.  MolecularInsertRelationshipAction
 *          had incomplete logic allowing self-referential C and P level rels
 *          to be inserted into a concept.
 *          </dd>
 * <dt>05/16/2003:</dt>
 *          <dd>{@link Concept} deals better with CONTEXT attributes.
 *          The various actions were updated to ensure that each would
 *          unapprove the concept if the change status flag was set.
 *          MolecularSplitAction deals better with inserting
 *          the split rel.
 *          </dd>
 * <dt>05/09/2003:</dt>
 *          <dd>{@link Atom} fixes including: better default for meme_string variable (internal),
 *          additional fields (source concept id, source descriptor id) and fix
 *          for the way formatted contexts are handled.  MolecularInsertRelationshipAction
 *          now supports full logic (C level rels delete other c level rels, etc.)
 *          </dd>
 * <dt>04/25/2003:</dt>
 *          <dd>Javadoc comments were updated for some classes.  The
 *          MolecularInsertAtomAction, MolecularInsertAttributeAction,
 *          and MolecularInsertRelationshipAction were updated to
 *          read the source concept into memory before performing the action.
 *          This fixes a <code>aproc_change_status</code> bug in Jekyll.
 *          MolecularChangeRelationshipAction was similarly updated.
 *          Three new classes: {@link ATUI}, {@link RUI}, and {@link
 *          RelationshipGroupRestrictor}.
 *          </dd>
 * <dt>04/09/2003:</dt>
     *          <dd>{@link Concept} and {@link Atom} were changed slightly so they do
 *          not perform a check when adding rels to see if they are already there.
 *          This vastly improves the performance of reading relationships
 *          for large concepts, although it required tweaking of the logic
 *          that decides when to add rels to concepts/atoms.  The fundamental
 *          issue is that when concept_id_1=concept_id_2, the relationship
 *          should be added to both atom_id_1 and atom_id_2 but only once
 *          to the concept. {@link Atom} got <code>clearContextRelationships</code>
 *          and <code>clearRelationships</code> methods.
 *          </dd>
 * <dt>04/01/2003:</dt>
 *          <dd>{@link Source} has a <code>getLanguage</code> method.
 *          MolecularMergeAction has a <code>getConflictingRelationships</code>
 *          method. RxNormComponentAtom no longer requires that the
 *          precise ingredient be longer than the base ingredient.
 *          MolecularSplitAction no longer attempts to check merge inhibitors.
 *          </dd>
 * <dt>03/19/2003:</dt>
 *          <dd>
 *          Certain common objects were optimized with respect to their
 *          XML representation.  Part of this involved adding methods to
 *          these classes to convert from a string to the object itself.
 *          For example {@link Authority.Default#newAuthority(String)}.
 *          The objects affected are {@link AUI}, {@link Authority},
 *          {@link Code}, {@link CUI}, {@link Identifier}, {@link ISUI}
     *          {@link LUI}, {@link Rank}, {@link StringIdentifier}, and {@link SUI}.
 *          Improved javadoc documentation.  Also a few new classes
 *          including BatchMolecularTransaction and
 *          MacroMolecularAction and their parent interface,
 *          BatchAction.
 *          </dd>
 * <dt>01/13/2003:</dt>
 *          <dd> Changes to RxNormBrandedAtom to fix a bug
     *          where it was duplicating data by re-implementing superclass methods.
 *          </dd>
 * <dt>11/13/2002:</dt>
 *          <dd> Changes include addition of {@link AUI} as a clas
 *               and its inclusion into the {@link Atom} API.  Other
 *               packages contain changes to support this new field.
 *               Additionally, there is enhanced support for
 *               RxNorm such as RxNormBrandedAtom but these
 *               changes will not appear until the next version.
 *           </dd>
 * <dt>08/28/2002:</dt>
 *          <dd> The most significant changes here were the addition
 *               of {@link Atom} subclasses for supporting RxNorm
 *               editing, and the development of a number of classes
 *               for supporting {@link Checklist} and {@link Worklist}
 *               management.  Refinement of other core classes occurred
 *               as well.
 * </dd>
 * <dt>06/20/2002:</dt>
 *          <dd> Atomic and Molecular action classes have been created.
 *
 *               Initial {@link Worklist} and {@link Cluster} classes
 *               have been written to support the worklist system.
 *
 *               {@link AtomFact} and {@link MergeFact} and associated
 *               classes were written to support the merge engine.
 *
 *               RxNormAtom and RxNormComponentAtom were
 *               created to represent NLM02/SCD and NLM02/SCDC atoms.
 *               These classes plus {@link MeshEntryTerm} comprise a set
 *               of source-specific atom classes that encapsulate
 *               functionality specific to those sources.  This is the
 *               beginnings of a system to support source editing in the MID.
 *
 *          </dd>
 * <dt>03/26/2002:</dt>
 *          <dd> {@link ReportStyle} was created to support dynamic
 *              and flexible styles for concept reports.  This class
 *              is used both by {@link gov.nih.nlm.meme.client.ReportsClient} and  bye
 *              {@link gov.nih.nlm.meme.server.ReportsGenerator}. <p>
 *              {@link SearchParameter} was created to support
 *              <i>finder</i> functionality.<p>
 *              The actions API was more fully specified, and in
 *              particular, the hierarchy of actions was
 *              designed, from WorkLog down to
 *              AtomicAction.<p>
 *              Semantic Type problem was settled by creating
 *              {@link SemanticType} and {@link ConceptSemanticType}.
 * </dd>
 * <dt>11/01/2001:</dt><dd>1st Release.</dd>
 * </dl>
 *
 * @author  MEME Group
 */

public class Version {

  //
  // Fields
  //

  private final static int release = 4;
  private final static double version = 0.0;
  private final static String version_authority = "MEME Group";
  private final static String version_date = "28-Aug-2002 00:00:00";
  private final static String package_name = "gov.nih.nlm.meme.common";

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
   * @param argv An array of argument.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.meme.common.Version.getVersionInformation());
  }
}
