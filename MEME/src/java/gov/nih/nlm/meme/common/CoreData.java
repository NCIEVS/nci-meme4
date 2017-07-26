/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  CoreData
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.meme.action.LoggedAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

/**
 * Generically represents
 * <a href="/MEME/Training/glossary.html#core_data">core data</a>.
 *
 * @see Authority
 * @see Identifier
 * @see LoggedAction
 * @see Source
 *
 * @author MEME Group
 */

public interface CoreData extends Rankable, Comparable {

  //
  // Field Values
  //

  /**
   * Field value indicating a state of released as approved.
   */
  public final static char FV_RELEASED_AS_APPROVED = 'A';

  /**
   * Field value indicating a state of released as unreviewed.
   */
  public final static char FV_RELEASED_AS_UNREVIEWED = 'U';

  /**
   * Field value indicating a state of not having been released.
   */
  public final static char FV_NOT_RELEASED = 'N';

  /**
   * Field value indicating editor review.
   */
  public final static char FV_STATUS_REVIEWED = 'R';

  /**
   * Field value indicating that an editor has not and will not review.
   */
  public final static char FV_STATUS_UNREVIEWED = 'U';

  /**
   * Field value indicating that an editor must review.
   */
  public final static char FV_STATUS_NEEDS_REVIEW = 'N';

  /**
   * Field value indicating demotion.
   */
  public final static char FV_STATUS_DEMOTED = 'D';

  /**
   * Field value indicating embryo
   */
  public final static char FV_STATUS_EMBRYO = 'E';

  /**
   * Field value indicating releasability.
   */
  public final static char FV_RELEASABLE = 'Y';

  /**
   * Field value indicating weak releasability.
   */
  public final static char FV_WEAKLY_RELEASABLE = 'y';

  /**
   * Field value indicating weak unreleasability.
   */
  public final static char FV_WEAKLY_UNRELEASABLE = 'n';

  /**
   * Field value indicating unreleasability.
   */
  public final static char FV_UNRELEASABLE = 'N';

  /**
   * Field value indicating a level of 'C'.
   */
  public final static char FV_MTH_ASSERTED = 'C';

  /**
   * Field value indicating a level of 'S'.
   */
  public final static char FV_SOURCE_ASSERTED = 'S';

  /**
   * Returns the {@link Source}.
   * @return the {@link Source}
   */
  public Source getSource();

  /**
   * Sets the {@link Source}.
   * @param source the {@link Source}
   */
  public void setSource(Source source);

  /**
   * Indicates whether or not this data was generated.
   * @return <code>true</code> if it has been, <code>false</code> otherwise
   */
  public boolean isGenerated();

  /**
   * Sets the generated flag.
   * @param flag the generated flag
   */
  public void setGenerated(boolean flag);

  /**
   * Indicates whether or not this core data has been deleted.
   * @return <code>true</code> if this data element has been deleted,
   *         <code>false</code> otherwise
   */
  public boolean isDead();

  /**
   * Sets the dead flag.
   * @param flag the dead flag
   */
  public void setDead(boolean flag);

  /**
   * Indicates whether or not this data has been reviewed.
   * @return <code>true</code> if it has been, <code>false</code> otherwise
   */
  public boolean isReviewed();

  /**
   * Same as {@link #isReviewed()}.
   * @return <code>true</code> if it has been, <code>false</code> otherwise
   */
  public boolean isApproved();

  /**
   * Indicates whether or not this data is unreviewed.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isUnreviewed();

  /**
   * Indicates whether or not this data requires review.
   * @return <code>true</code> if it does, <code>false</code> otherwise
   */
  public boolean needsReview();

  /**
   * Indicates whether or not this data has been demoted.
   * @return <code>true</code> if it has, <code>false</code> otherwise
   */
  public boolean isDemoted();

  /**
   * Indicates whether or not this data is an embryo.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isEmbryo();

  /**
   * Sets the status. This value is used to drive the
   * methods {@link #isReviewed()}, {@link #isUnreviewed()},
   * {@link #needsReview()}, {@link #isDemoted()},
   * and {@link #isEmbryo()}.
   * @param status a <code>char</code> representation of the status
   */
  public void setStatus(char status);

  /**
   * Returns the status value.
   * @return the status value
   */
  public char getStatus();

  /**
   * Returns the {@link Authority}.
   * @return the {@link Authority}
   */
  public Authority getAuthority();

  /**
   * Sets the {@link Authority}.
   * @param authority the {@link Authority}
   */
  public void setAuthority(Authority authority);

  /**
   * Returns the timestamp.
   * @return the {@link Date} timestamp
   */
  public Date getTimestamp();

  /**
   * Sets the timestamp.
   * @param timestamp the {@link Date} timestamp
   */
  public void setTimestamp(Date timestamp);

  /**
   * Returns the insertion {@link Date}.
   * @return the insertion {@link Date}
   */
  public Date getInsertionDate();

  /**
   * Sets the insertion {@link Date}.
   * @param insertion_date the insertion {@link Date}
   */
  public void setInsertionDate(Date insertion_date);

  /**
   * Indicates whether or not this data was released as approved
   * in the previous release.
   * @return <code>true</code> if it was, <code>false</code> otherwise
   */
  public boolean wasReleasedAsApproved();

  /**
   * Indicates whether or not this data was released as unreviewed
   * in the previous release.
   * @return <code>true</code> if it was, <code>false</code> otherwise
   */
  public boolean wasReleasedAsUnreviewed();

  /**
   * Indicates whether or not this data was released in the previous
   * release.
   * @return <code>true</code> if it was, <code>false</code> otherwise
   */
  public boolean wasReleased();

  /**
   * Indicates whether or not this data is releasable.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isReleasable();

  /**
   * Sets the released field.  This value is used to drive
   * the {@link #wasReleased()}, {@link CoreData#wasReleasedAsUnreviewed()},
   * and {@link CoreData#wasReleasedAsApproved()} methods.
   * @param released a <code>char</code> representation of the released field
   */
  public void setReleased(char released);

  /**
   * Returns the released value.
   * @return the released value
   */
  public char getReleased();

  /**
   * Indicates whether or not this data is weakly releasable.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isWeaklyReleasable();

  /**
   * Indicates whether or not this data is weakly unreleasable.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isWeaklyUnreleasable();

  /**
   * Indicates whether or not this data is unreleasable.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isUnreleasable();

  /**
   * Sets the tobereleased field.  This value is used to drive
   * the {@link #isWeaklyReleasable()}, {@link #isWeaklyUnreleasable()},
   * {@link #isReleasable()}, and {@link #isUnreleasable()} methods.
   * @param tobereleased a <code>char</code> representation of tobereleased
   */
  public void setTobereleased(char tobereleased);

  /**
   * Returns the tobereleased value.
   * @return the tobereleased value
   */
  public char getTobereleased();

  /**
   * Returns the last {@link LoggedAction} to affect this data.
   * @return the last {@link LoggedAction} to affect this data
   */
  public LoggedAction getLastAction();

  /**
   * Sets the last action.
   * @param last_action the last {@link LoggedAction} to affect this data
   */
  public void setLastAction(LoggedAction last_action);

  /**
   * Indicates whether or not this data is suppressible.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isSuppressible();

  /**
   * Sets the suppressibility value.
   * @param suppressible the suppressibility value
   */
  public void setSuppressible(String suppressible);

  /**
   * Return the suppressibility value.
   * @return the suppressibility value
   */
  public String getSuppressible();

  /**
   * Indicates whether or not this data is MTH asserted.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isMTHAsserted();

  /**
   * Indicates whether or not this data is connected to an {@link Concept}.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isConceptLevel();

  /**
   * Indicates whether or not this data is asserted by a {@link Source}.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isSourceAsserted();

  /**
   * Determines whether or not this data is connected to an {@link Atom}.
   * @return <code>true</code> if it is, <code>false</code> otherwise
   */
  public boolean isAtomLevel();

  /**
   * Sets the level.  This value is used to drive the
   * {@link #isMTHAsserted()}, {@link #isSourceAsserted()},
   * {@link #isAtomLevel()}, and {@link #isConceptLevel()}
   * methods.
   * @param level a <code>char</code> representation of level
   */
  public void setLevel(char level);

  /**
   * Returns the level value.
   * @return the level value
   */
  public char getLevel();

  /**
   * Returns the source {@link Identifier}.  This is the identifier
   * attached to this element in the <code>.src</code> file used
   * to insert it.
   * @return the <code>.src</code> file {@link Identifier}
   */
  public Identifier getSourceIdentifier();

  /**
   * Sets the source {@link Identifier}.
   * @param identifier the source {@link Identifier}
   */
  public void setSourceIdentifier(Identifier identifier);

  /**
   * Returns the source {@link Identifier}.  This is the identifier
   * attached to this element in the <code>.src</code> file used
   * to insert it.
   * @return the <code>.src</code> file {@link Identifier}
   */
  public Identifier getSrcIdentifier();

  /**
   * Sets the source {@link Identifier}.
   * @param identifier the source {@link Identifier}
   */
  public void setSrcIdentifier(Identifier identifier);

  /**
   * Returns the <i>MID</i> {@link Identifier}.  This is the identifier used
   * within the <i>MID</i> to track this data element.
   * @return the <i>MID</i> {@link Identifier}
   */
  public Identifier getIdentifier();

  /**
   * Sets the <i>MID</i> identifier.
   * @param identifier the <i>MID</i> {@link Identifier}
   */
  public void setIdentifier(Identifier identifier);

  /**
   * Returns the cluster {@link Identifier}.  This is the identifier used
   * within a {@link Worklist} or {@link Checklist} to group
   * core data elements tobether.
   * @return the cluster {@link Identifier}
   */
  public Identifier getClusterIdentifier();

  /**
   * Sets the cluster {@link Identifier}.
   * @param identifier the cluster {@link Identifier}
   */
  public void setClusterIdentifier(Identifier identifier);

  /**
   * Returns the {@link Identifier} that this core data is connected
   * to from the source inverter's point of view.  This
   * corresponds to the <code>sg_id</code> field associated
   * with <code>attributes</code> and the <code>sg_id_1</code> field
   * associated with <code>relationships</code>.
   * @return the native {@link Identifier}
   */
  public NativeIdentifier getNativeIdentifier();

  /**
   * Sets the {@link Identifier} that this core data is connectd
   * to from the source inverter's point of view.
   * @param identifier the native {@link Identifier}
   */
  public void setNativeIdentifier(NativeIdentifier identifier);

  /**
   * Adds the specified attribute.
   * @param attribute the {@link Attribute} to add
   */
  public void addAttribute(Attribute attribute);

  /**
   * Removes the specified attribute.
   * @param attribute the {@link Attribute} to remove
   */
  public void removeAttribute(Attribute attribute);

  /**
   * Returns the attributes of this relationship's atom.
   * @return an {@link Attribute}<code>[]</code>
   */
  public Attribute[] getAttributes();

  /**
   * Clears the attributes.
   */
  public void clearAttributes();

  /**
   * Returns the {@link Attribute}s matching the specified name.
   * @param name the attribute name
   * @return the {@link Attribute}<code>[]</code> of attributes with the
   *         specified name
   */
  public Attribute[] getAttributesByName(String name);

  /**
   * Returns the {@link Attribute}s matching the specified names.
   * @param names a {@link String}<code>[]</code> of attribute names
   * @return the {@link Attribute}<code>[]</code> of attributes with the
   *         specified names
   */
  public Attribute[] getAttributesByNames(String[] names);

  /**
   * Returns a subset of the attributes of this atom, as indicated
   * by the specified restrictor.
   * @param restrictor a {@link CoreDataRestrictor}
   *                   designed to restrict {@link Attribute}s
   * @return an {@link Attribute}<code>[]</code> of attributes matching
   *         the restrictor criteria
   */
  public Attribute[] getRestrictedAttributes(CoreDataRestrictor restrictor);

  /**
   * Returns the attributes of this atom as sorted by
   * the specified {@link Comparator}..
   * @param comparator the {@link Comparator}
   * @return a sorted {@link Attribute}<code>[]</code>
   */
  public Attribute[] getSortedAttributes(Comparator comparator);

  /**
   * Returns the formatted CONTEXT attributes of this atom.
       * @return an {@link Attribute}<code>[]</code> of formatted CONTEXT attributes
   */
  public Attribute[] getFormattedContexts();

  /**
   * Adds the specified relationship.
   * @param relationship the {@link Relationship} to add
   */
  public void addRelationship(Relationship relationship);

  /**
   * Removes the specified relationship.
   * @param relationship the {@link Relationship} to remove
   */
  public void removeRelationship(Relationship relationship);

  /**
   * Clears the relationships.
   */
  public void clearRelationships();

  /**
   * Returns the relationships.
   * @return a {@link Relationship}<code>[]</code>
   */
  public Relationship[] getRelationships();

  /**
   * Returns a subset of the relationships of this atom, as indicated
   * by the specified restrictor.
   * @param restrictor a {@link CoreDataRestrictor}
   *                   designed to restrict {@link Relationship}s
   * @return n {@link Relationship}<code>[]</code> of relationships matching
   *         the restrictor criteria
   */
  public Relationship[] getRestrictedRelationships(CoreDataRestrictor
      restrictor);

  /**
       * Returns the {@link Relationship}s sorted by the specified {@link Comparator}.
   * @param comparator the {@link Comparator} used to sort
   * @return a sorted {@link Relationship}<code>[]</code>
   */
  public Relationship[] getSortedRelationships(Comparator comparator);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of
   * {@link CoreData} interface.
   */
  public class Default implements CoreData {

    //
    // Fields
    //

    protected Source source = null;
    protected Authority authority = null;
    protected LoggedAction last_action = null;
    protected Identifier src_id = null;
    protected Identifier source_id = null;
    protected Identifier meme_id = null;
    protected NativeIdentifier sg_id = null;
    protected Identifier cluster_id = null;
    protected Rank rank = null;
    protected boolean generated = false;
    protected boolean dead = false;
    protected String suppressible = "N";
    protected char status;
    protected char released;
    protected char tobereleased;
    protected char level;
    protected Date timestamp;
    protected Date insertion_date;
    protected ArrayList attributes = null;
    protected ArrayList relationships = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty {@link CoreData} object.
     */
    public Default() {
      this.rank = Rank.EMPTY_RANK;
    }

    //
    // Implementation of CoreData interface
    //

    /**
     * Implements {@link CoreData#getSource()}.
     */
    public Source getSource() {
      return source;
    }

    /**
     * Implements {@link CoreData#setSource(Source)}.
     */
    public void setSource(Source source) {
      this.source = source;
    }

    /**
     * Implements {@link CoreData#isGenerated()}.
     */
    public boolean isGenerated() {
      return generated;
    }

    /**
     * Implements {@link CoreData#setGenerated(boolean)}.
     */
    public void setGenerated(boolean flag) {
      this.generated = flag;
    }

    /**
     * Implements {@link CoreData#isDead()}.
     */
    public boolean isDead() {
      return dead;
    }

    /**
     * Implements {@link CoreData#setDead(boolean)}.
     */
    public void setDead(boolean flag) {
      this.dead = flag;
    }

    /**
     * Implements {@link CoreData#isReviewed()}.
     */
    public boolean isReviewed() {
      return status == FV_STATUS_REVIEWED;
    }

    /**
     * Implements {@link CoreData#isApproved()}.
     */
    public boolean isApproved() {
      return isReviewed();
    }

    /**
     * Implements {@link CoreData#isUnreviewed()}.
     */
    public boolean isUnreviewed() {
      return status == FV_STATUS_UNREVIEWED;
    }

    /**
     * Implements {@link CoreData#needsReview()}.
     */
    public boolean needsReview() {
      return status == FV_STATUS_NEEDS_REVIEW;
    }

    /**
     * Implements {@link CoreData#isDemoted()}.
     */
    public boolean isDemoted() {
      return status == FV_STATUS_DEMOTED;
    }

    /**
     * Implements {@link CoreData#isEmbryo()}.
     */
    public boolean isEmbryo() {
      return status == FV_STATUS_EMBRYO;
    }

    /**
     * Implements {@link CoreData#getStatus()}.
     * @return the status.
     */
    public char getStatus() {
      return status;
    }

    /**
     * Implements {@link CoreData#setStatus(char)}.
     */
    public void setStatus(char status) {
      this.status = status;
    }

    /**
     * Implements {@link CoreData#getAuthority()}.
     */
    public Authority getAuthority() {
      return authority;
    }

    /**
     * Implements {@link CoreData#setAuthority(Authority)}.
     */
    public void setAuthority(Authority authority) {
      this.authority = authority;
    }

    /**
     * Implements {@link CoreData#getTimestamp()}.
     */
    public Date getTimestamp() {
      return timestamp;
    }

    /**
     * Implements {@link CoreData#setTimestamp(Date)}.
     */
    public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
    }

    /**
     * Implements {@link CoreData#getInsertionDate()}.
     */
    public Date getInsertionDate() {
      return insertion_date;
    }

    /**
     * Implements {@link CoreData#setInsertionDate(Date)}.
     */
    public void setInsertionDate(Date insertion_date) {
      this.insertion_date = insertion_date;
    }

    /**
     * Implements {@link CoreData#wasReleasedAsApproved()}.
     */
    public boolean wasReleasedAsApproved() {
      return released == FV_RELEASED_AS_APPROVED;
    }

    /**
     * Implements {@link CoreData#wasReleasedAsUnreviewed()}.
     */
    public boolean wasReleasedAsUnreviewed() {
      return released == FV_RELEASED_AS_UNREVIEWED;
    }

    /**
     * Implements {@link CoreData#wasReleased()}.
     */
    public boolean wasReleased() {
      return released != FV_NOT_RELEASED;
    }

    /**
     * Implements {@link CoreData#setReleased(char)}.
     */
    public void setReleased(char released) {
      this.released = released;
    }

    /**
     * Implements {@link CoreData#getReleased()}.
     */
    public char getReleased() {
      return released;
    }

    /**
     * Implements {@link CoreData#isReleasable()}.
     */
    public boolean isReleasable() {
      return (tobereleased == FV_WEAKLY_RELEASABLE ||
              tobereleased == FV_RELEASABLE);
    }

    /**
     * Implements {@link CoreData#isWeaklyReleasable()}.
     */
    public boolean isWeaklyReleasable() {
      return tobereleased == FV_WEAKLY_RELEASABLE;
    }

    /**
     * Implements {@link CoreData#isWeaklyUnreleasable()}.
     */
    public boolean isWeaklyUnreleasable() {
      return tobereleased == FV_WEAKLY_UNRELEASABLE;
    }

    /**
     * Implements {@link CoreData#isUnreleasable()}.
     */
    public boolean isUnreleasable() {
      return (tobereleased == FV_WEAKLY_UNRELEASABLE ||
              tobereleased == FV_UNRELEASABLE);
    }

    /**
     * Implements {@link CoreData#setTobereleased(char)}.
     */
    public void setTobereleased(char tobereleased) {
      this.tobereleased = tobereleased;
    }

    /**
     * Implements {@link CoreData#getTobereleased()}.
     */
    public char getTobereleased() {
      return tobereleased;
    }

    /**
     * Implements {@link CoreData#getLastAction()}.
     */
    public LoggedAction getLastAction() {
      return last_action;
    }

    /**
     * Implements {@link CoreData#setLastAction(LoggedAction)}.
     */
    public void setLastAction(LoggedAction last_action) {
      this.last_action = last_action;
    }

    /**
     * Implements {@link CoreData#isSuppressible()}.
     */
    public boolean isSuppressible() {
      return (suppressible != null && !suppressible.equals("N"));
    }

    /**
     * Implements {@link CoreData#setSuppressible(String)}.
     */
    public void setSuppressible(String suppressible) {
      this.suppressible = suppressible;
    }

    /**
     * Implements {@link CoreData#getSuppressible()}.
     */
    public String getSuppressible() {
      return suppressible;
    }

    /**
     * Implements {@link CoreData#isMTHAsserted()}.
     */
    public boolean isMTHAsserted() {
      return level == FV_MTH_ASSERTED;
    }

    /**
     * Implements {@link CoreData#isConceptLevel()}.
     */
    public boolean isConceptLevel() {
      return level == FV_MTH_ASSERTED;
    }

    /**
     * Implements {@link CoreData#isSourceAsserted()}.
     */
    public boolean isSourceAsserted() {
      return level == FV_SOURCE_ASSERTED;
    }

    /**
     * Implements {@link CoreData#isAtomLevel()}.
     */
    public boolean isAtomLevel() {
      return (level == FV_SOURCE_ASSERTED || level == 'P');
    }

    /**
     * Implements {@link CoreData#setLevel(char)}.
     */
    public void setLevel(char level) {
      this.level = level;
    }

    /**
     * Implements {@link CoreData#getLevel()}.
     */
    public char getLevel() {
      return level;
    }

    /**
     * Implements {@link CoreData#getSourceIdentifier()}.
     */
    public Identifier getSourceIdentifier() {
      return source_id;
    }

    /**
     * Implements {@link CoreData#setSourceIdentifier(Identifier)}.
     */
    public void setSourceIdentifier(Identifier identifier) {
      this.source_id = identifier;
    }

    /**
     * Implements {@link CoreData#getSrcIdentifier()}.
     */
    public Identifier getSrcIdentifier() {
      return src_id;
    }

    /**
     * Implements {@link CoreData#setSrcIdentifier(Identifier)}.
     */
    public void setSrcIdentifier(Identifier identifier) {
      this.src_id = identifier;
    }

    /**
     * Implements {@link CoreData#getIdentifier()}.
     */
    public Identifier getIdentifier() {
      return meme_id;
    }

    /**
     * Implements {@link CoreData#setIdentifier(Identifier)}.
     */
    public void setIdentifier(Identifier identifier) {
      this.meme_id = identifier;
    }

    /**
     * Implements {@link CoreData#getClusterIdentifier()}.
     */
    public Identifier getClusterIdentifier() {
      return cluster_id;
    }

    /**
     * Implements {@link CoreData#setClusterIdentifier(Identifier)}.
     */
    public void setClusterIdentifier(Identifier identifier) {
      this.cluster_id = identifier;
    }

    /**
     * Implements {@link CoreData#getNativeIdentifier()}.
     */
    public NativeIdentifier getNativeIdentifier() {
      return sg_id;
    }

    /**
     * Implements {@link CoreData#setNativeIdentifier(NativeIdentifier)}.
     */
    public void setNativeIdentifier(NativeIdentifier identifier) {
      this.sg_id = identifier;
    }

    /**
     * Implements {@link CoreData#addAttribute(Attribute)}.
     */
    public void addAttribute(Attribute attribute) {
      if (attributes == null) {
        attributes = new ArrayList();
      }
      attributes.add(attribute);
    }

    /**
     * Implements {@link CoreData#removeAttribute(Attribute)}.
     */
    public void removeAttribute(Attribute attribute) {
      if (attributes == null) {
        return;
      }
      attributes.remove(attribute);
    }

    /**
     * Implements {@link CoreData#clearAttributes()}.
     */
    public void clearAttributes() {
      if (attributes != null) {
        attributes.clear();
      }
    }

    /**
     * Implements {@link CoreData#getAttributes()}.
     */
    public Attribute[] getAttributes() {
      if (attributes == null) {
        return new Attribute[0];
      } else {
        return (Attribute[]) attributes.toArray(new Attribute[] {});
      }
    }

    /**
     * Implements {@link CoreData#getAttributesByName(String)}.
     */
    public Attribute[] getAttributesByName(String name) {
      if (attributes == null) {
        return new Attribute[0];
      } else {
        return getRestrictedAttributes(new ByNameAttributeRestrictor(name));
      }
    }

    /**
     * Implements {@link CoreData#getAttributesByNames(String[])}.
     */
    public Attribute[] getAttributesByNames(String[] names) {
      if (attributes == null) {
        return new Attribute[0];
      } else {
        return getRestrictedAttributes(new ByNameAttributeRestrictor(names));
      }
    }

    /**
     * Implements {@link CoreData#getRestrictedAttributes(CoreDataRestrictor)}.
     */
    public Attribute[] getRestrictedAttributes(CoreDataRestrictor restrictor) {
      if (attributes == null) {
        return new Attribute[0];
      }
      ArrayList ra = new ArrayList();
      Iterator iterator = attributes.iterator();
      while (iterator.hasNext()) {
        Attribute attribute = (Attribute) iterator.next();
        if (restrictor.keep(attribute)) {
          ra.add(attribute);
        }
      }
      Collections.sort(ra, restrictor);
      return (Attribute[]) ra.toArray(new Attribute[] {});
    }

    /**
     * Implements {@link CoreData#getSortedAttributes(Comparator)}.
     */
    public Attribute[] getSortedAttributes(Comparator comparator) {
      if (attributes == null) {
        return new Attribute[0];
      }
      Attribute[] arrays =
          (Attribute[]) attributes.toArray(new Attribute[] {});
      Arrays.sort(arrays, comparator);
      return arrays;
    }

    /**
     * Implements {@link CoreData#getFormattedContexts()}.
     */
    public Attribute[] getFormattedContexts() {
      if (attributes == null) {
        return new Attribute[0];
      }
      ArrayList contexts = new ArrayList();
      Iterator iterator = attributes.iterator();
      while (iterator.hasNext()) {
        Attribute attribute = (Attribute) iterator.next();
        if (attribute.getName().equals(Attribute.CONTEXT)) {
          contexts.add(attribute);
        }
      }
      return (Attribute[]) contexts.toArray(new Attribute[] {});
    }

    /**
     * Implements {@link CoreData#addRelationship(Relationship)}.
     */
    public void addRelationship(Relationship relationship) {
      if (relationships == null) {
        relationships = new ArrayList();
      }
      relationships.add(relationship);
    }

    /**
     * Implements {@link CoreData#removeRelationship(Relationship)}.
     */
    public void removeRelationship(Relationship relationship) {
      if (relationships == null) {
        return;
      }
      relationships.remove(relationship);
    }

    /**
     * Implements {@link CoreData#clearRelationships()}.
     */
    public void clearRelationships() {
      if (relationships != null) {
        relationships.clear();
      }
    }

    /**
     * Implements {@link CoreData#getRelationships()}.
     */
    public Relationship[] getRelationships() {
      if (relationships == null) {
        return new Relationship[0];
      } else {
        return (Relationship[]) relationships.toArray(new Relationship[] {});
      }
    }

    /**
         * Implements {@link CoreData#getRestrictedRelationships(CoreDataRestrictor)}.
     */
    public Relationship[] getRestrictedRelationships(CoreDataRestrictor
        restrictor) {
      if (relationships == null) {
        return new Relationship[0];
      }
      ArrayList rr = new ArrayList();
      Iterator iterator = relationships.iterator();
      while (iterator.hasNext()) {
        Relationship relationship = (Relationship) iterator.next();
        if (restrictor.keep(relationship)) {
          rr.add(relationship);
        }
      }
      Collections.sort(rr, restrictor);
      return (Relationship[]) rr.toArray(new Relationship[] {});
    }

    /**
     * Implements {@link CoreData#getSortedRelationships(Comparator)}.
     */
    public Relationship[] getSortedRelationships(Comparator comparator) {
      if (relationships == null) {
        return new Relationship[0];
      }
      Relationship[] arrays =
          (Relationship[]) relationships.toArray(new Relationship[] {});
      Arrays.sort(arrays, comparator);
      return arrays;
    }

    //
    // Implementation of Rankable interface
    //

    /**
     * Implements {@link Rankable#getRank()}.
     */
    public Rank getRank() {
      return rank;
    }

    /**
     * Implements {@link Rankable#setRank(Rank)}.
     */
    public void setRank(Rank rank) {
      this.rank = rank;
    }

    //
    // Implementation of Comparable interface
    //

    /**
     * This implements {@link Comparable#compareTo(Object)}.
     */
    public int compareTo(Object object) {
      CoreData cd = (CoreData) object;

      // Compare ranks
      int i = rank.compareTo(cd.getRank());

      // If ranks are equal, compare identifiers
      // and the higher one wins.
      if (i == 0 && meme_id != null) {
        return meme_id.intValue() - cd.getIdentifier().intValue();
      }

      return i;
    }

    //
    // Implementation of Object class
    //

    /**
     * Returns an <code>int</code> hashcode.
     * @return an <code>int</code> hashcode
     */
    public int hashCode() {
      return (getIdentifier() == null) ? 0 : getIdentifier().hashCode();
    }

    /**
     * Implements an equals function based on {@link Identifier}.
     * @return <code>true</code> if the objects are equal,
     *         <code>false</codE> otherwise
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof CoreData))) {
        return false;
      }
      return ( (CoreData) object).getIdentifier().equals(getIdentifier());
    }

  }
}
