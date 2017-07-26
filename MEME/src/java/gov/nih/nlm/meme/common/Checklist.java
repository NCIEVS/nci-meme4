/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Checklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Generically represents a checklist used for workflow management.
 * This class roughly represents the <code>meow.ems_checklist_info</code>
 * table.
 *
 * @author MEME Group
 */

public class Checklist extends ArrayList {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//
  // Fields
  //
  protected String name = null;
  protected String owner = null;
  protected String bin_name = null;
  protected String bin_type = null;

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
   * Instantiates an empty {@link Checklist}
   */
  public Checklist() {
    super();
    clusters = new ArrayList();
  };

  //
  // Accessor Methods
  //

  /**
   * Returns the checklist name.
   * @return the checklist name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the checklist name.
   * @param name the checklist name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the checklist owner.
   * @return the checklist owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Sets the checklist owner.
   * @param owner the checklist owner
   */
  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   * Returns the checklist bin name.
   * @return the checklist bin name
   */
  public String getBinName() {
    return bin_name;
  }

  /**
   * Sets the checklist bin name.
   * @param bin_name the checklist bin name
   */
  public void setBinName(String bin_name) {
    this.bin_name = bin_name;
  }

  /**
   * Indicates wheter or not this is a "QA" bin.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isQABin() {
    return bin_type.equals("QA");
  }

  /**
   * Indicates wheter or not this is a "ME" bin.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isMEBin() {
    return bin_type.equals("ME");
  }

  /**
   * Indicates wheter or not this is a "AH" bin.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAHBin() {
    return bin_type.equals("AH");
  }

  /**
   * Returns the checklist bin type.
   * @return the checklist bin type
   */
  public String getBinType() {
    return bin_type;
  }

  /**
   * Sets the checklist bin type.
   * @param bin_type the checklist bin type
   */
  public void setBinType(String bin_type) {
    this.bin_type = bin_type;
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

  //
  // Cluster methods
  //

  /**
   * Returns a list of clusters. If the checklist has not been
   * clustered, then each cluster returned will have just one element.
   * @return a {@link List} of {@link Cluster}s
   */
  public List getClusters() {
    Collections.sort(clusters);
    return clusters;
  }

  /**
   * Returns an iterator over the clusters.
   * @return an iterator over the clusters
   */
  public Iterator getClusterIterator() {
    Collections.sort(clusters);
    return clusters.iterator();
  }

  /**
   * Add a {@link Cluster} to the checklist.
   * @param cluster the {@link Cluster} to add
   */
  public void addCluster(Cluster cluster) {
    clusters.add(cluster);
    addAll(cluster);
  }

  /**
   * Remove the specified {@link Cluster} from the checklist.
   * @param cluster the {@link Cluster} to remove
   */
  public void removeCluster(Cluster cluster) {
    clusters.remove(cluster);
    removeAll(cluster);
  }

}