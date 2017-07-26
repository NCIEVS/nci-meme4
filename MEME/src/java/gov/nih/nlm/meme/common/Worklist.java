/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Worklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Generically represents a worklist used
 * for workflow management.  This class roughly
 * represents the <code>meow.wms_worklist_info</code>
 * table.
 *
 * @author MEME Group
 */

public class Worklist extends ArrayList {

  //
  // Fields
  //
  protected String name = null;
  protected String description = null;
  protected String status = null;
  protected EditorPreferences editor_pref = null;
  protected String created_by = null;
  protected String stamped_by = null;

  // dates
  protected Date create_date = null;
  protected Date assign_date = null;
  protected Date return_date = null;
  protected Date stamp_date = null;

  // elapsed times
  protected long edit_time = 0;
  protected long stamp_time = 0;

  // contents
  protected List clusters = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link Worklist}.
   */
  public Worklist() {
    super();
    clusters = new ArrayList();
  };

  //
  // Accessor Methods
  //

  /**
   * Returns the worklist name.
   * @return the worklist name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the worklist name.
   * @param name the worklist name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the worklist description.
   * @return the worklist description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the worklist description.
   * @param description the worklist description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the worklist status.
   * @return the worklist status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the worklist status.
   * @param status the worklist status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Returns the preferences of the editor assigned to this worklist.
   * @return the preferences of the editor assigned to this worklist
   */
  public EditorPreferences getEditorPreferences() {
    return editor_pref;
  }

  /**
   * Sets the preferences of the editor assigned to this worklist.
   * Sets ep the preferences of the editor assigned to this worklist
   * @param ep the {@link EditorPreferences}
   */
  public void setEditorPreferences(EditorPreferences ep) {
    this.editor_pref = ep;
  }

  /**
   * Returns who created the worklist.
   * @return who created the worklist
   */
  public String getCreatedBy() {
    return created_by;
  }

  /**
   * Sets the authority responsible for creating the worklist.
   * @param created_by the authority responsible for creating the worklist
   */
  public void setCreatedBy(String created_by) {
    this.created_by = created_by;
  }

  /**
   * Returns who stamped the worklist.
   * @return who stamped the worklist
   */
  public String getStampedBy() {
    return stamped_by;
  }

  /**
   * Sets the authority responsible for stamping the worklist.
   * @param stamped_by the authority responsible for creating the worklist
   */
  public void setStampedBy(String stamped_by) {
    this.stamped_by = stamped_by;
  }

  /**
   * Returns the creation {@link Date}.
   * @return the creation {@link Date}
   */
  public Date getCreationDate() {
    return create_date;
  }

  /**
   * Sets the creation {@link Date}.
   * @param create_date the creation {@link Date}
   */
  public void setCreationDate(Date create_date) {
    this.create_date = create_date;
  }

  /**
   * Returns the assign {@link Date}.
   * @return the assign {@link Date}
   */
  public Date getAssignDate() {
    return assign_date;
  }

  /**
   * Sets the assign {@link Date}.
   * @param assign_date the assign {@link Date}.
   */
  public void setAssignDate(Date assign_date) {
    this.assign_date = assign_date;
  }

  /**
   * Returns the return {@link Date}.
   * @return the return {@link Date}
   */
  public Date getReturnDate() {
    return return_date;
  }

  /**
   * Sets the return {@link Date}.
   * @param return_date the return {@link Date}
   */
  public void setReturnDate(Date return_date) {
    this.return_date = return_date;
  }

  /**
   * Returns the stamping {@link Date}.
   * @return the stamping {@link Date}
   */
  public Date getStampingDate() {
    return stamp_date;
  }

  /**
   * Sets the stamping {@link Date}
   * @param stamp_date the stamping {@link Date}
   */
  public void setStampingDate(Date stamp_date) {
    this.stamp_date = stamp_date;
  }

  //
  // Cluster methods
  //

  /**
   * Returns all {@link Cluster}s.
   * @return all {@link Cluster}s
   */
  public List getClusters() {
    Collections.sort(clusters);
    return clusters;
  }

  /**
   * Returns an iterator over the {@link Cluster}s.
   * @return an iterator over the {@link Cluster}s
   */
  public Iterator getClusterIterator() {
    Collections.sort(clusters);
    return clusters.iterator();
  }

  /**
   * Adds the specified {@link Cluster}.
   * @param cluster the {@link Cluster} to add
   */
  public void addCluster(Cluster cluster) {
    clusters.add(cluster);
    addAll(cluster);
  }

  /**
   * Removes the specified {@link Cluster}.
   * @param cluster the {@link Cluster} to remove
   */
  public void removeCluster(Cluster cluster) {
    clusters.remove(cluster);
    removeAll(cluster);
  }

}
