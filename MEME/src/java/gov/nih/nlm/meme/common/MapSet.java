/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MapSet
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generically represents a collection of {@link Mapping}s.
 *
 * @author MEME Group
 */

public interface MapSet
    extends Concept {

  /**
   * Sets the "from" {@link Source} (FROMVSAB).
   * @param from_source the "from" {@link Source}
   */
  public void setFromSource(Source from_source);

  /**
   * Returns the "from" {@link Source} (FROMVSAB).
   * @return the "from" {@link Source}
   */
  public Source getFromSource();

  /**
   * Sets the "to" {@link Source} (TOVSAB).
   * @param to_source the "to" {@link Source}
   */
  public void setToSource(Source to_source);

  /**
   * Returns the "to" {@link Source} (TOVSAB).
   * @return the "to" {@link Source}
   */
  public Source getToSource();

  /**
   * Sets the {@link Source} (MAPSETVSAB).
   * @param source {@link Source}
   */
  public void setMapSetSource(Source source);

  /**
   * Returns the {@link Source} (MAPSETVSAB).
   * @return the {@link Source}
   */
  public Source getMapSetSource();

  /**
   * Returns the type (MAPSETTYPE).
   * @return the type
   */
  public String getType();

  /**
   * Returns the map set {@link Identifier} (MAPSETSID).
   * @return the map set {@link Identifier}
   */
  public Identifier getMapSetIdentifier();

  /**
   * Returns the name (MAPSETNAME).
   * @return the name
   */
  public String getName();

  /**
   * Indicates whether or not it is from exhaustive (MTH_MAPFROMEXHAUSTIVE).
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isFromExhaustive();

  /**
   * Indicates whether or not it is from exhaustive (MTH_MAPFROMEXHAUSTIVE).
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean getIsFromExhaustive();

  /**
   * Indicates whether or not it is to exhaustive (MTH_MAPTOEXHAUSTIVE).
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isToExhaustive();

  /**
   * Indicates whether or not it is to exhaustive (MTH_MAPTOEXHAUSTIVE).
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean getIsToExhaustive();

  /**
   * Returns the "from" complexity (MTH_MAPFROMCOMPLEXITY).
   * @return the "from" complexity
   */
  public String getFromComplexity();

  /**
   * Returns the "to" complexity (MTH_MAPTOCOMPLEXITY).
   * @return the "to" complexity
   */
  public String getToComplexity();

  /**
   * Returns the map set complexity (MTH_MAPSETCOMPLEXITY).
   * @return the map set complexity
   */
  public String getMapSetComplexity();

  /**
   * Returns the description (SOS).
   * @return the description
   */
  public String getDescription();

  /**
   * Returns the value.
   * @param key the key to get the value.
   * @return the value
   */
  public String getValue(String key);

  /**
   * Returns the {@link Mapping}s.
   * @return the {@link Mapping}s
   */
  public Mapping[] getMappings();

  /**
   * Returns the "map to" {@link MapObject}s.
   * @return the "map to" {@link MapObject}s
   */
  public MapObject[] getMapTo();

  /**
   * Returns the "map from" {@link MapObject}s.
   * @return the "map from" {@link MapObject}s.
   */
  public MapObject[] getMapFrom();

  /**
   * Indicates whether or not the list contains the specified {@link MapObject}
   * @param mo the {@link MapObject}
   * @return <code>true</code> if so,
   * <code>false</code> otherwise.
   */
  public boolean containsMapTo(MapObject mo);

  /**
   * Indicates whether or not the list contains the specified {@link MapObject}
   * @param mo the {@link MapObject}
   * @return <code>true</code> if so,
   * <code>false</code> otherwise.
   */
  public boolean containsMapFrom(MapObject mo);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link MapSet} interface.
   */
  public class Default
      extends Concept.Default implements MapSet {

    //
    // Fields
    //

    private Source from_source = null;
    private Source to_source = null;
    private Source source = null;
    private List mappings = new ArrayList();
    private Set map_to = new HashSet();
    private Set map_from = new HashSet();

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link MapSet}.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates a default {@link Concept} with the
     * specified concept identifier.
     * @param concept_id an <code>int</code> representation of the concept id
     */
    public Default(int concept_id) {
      super(concept_id);
    }

    //
    // Implementation of MapSet interface
    //

    /**
     * Implements {@link MapSet#setFromSource(Source)}.
     */
    public void setFromSource(Source from_source) {
      this.from_source = from_source;
    }

    /**
     * Implements {@link MapSet#getFromSource()}.
     */
    public Source getFromSource() {
      return from_source;
    }

    /**
     * Implements {@link MapSet#setToSource(Source)}.
     */
    public void setToSource(Source to_source) {
      this.to_source = to_source;
    }

    /**
     * Implements {@link MapSet#getToSource()}.
     */
    public Source getToSource() {
      return to_source;
    }

    /**
     * Implements {@link MapSet#setMapSetSource(Source)}.
     */
    public void setMapSetSource(Source source) {
      this.source = source;
    }

    /**
     * Implements {@link MapSet#getMapSetSource()}.
     */
    public Source getMapSetSource() {
      return source;
    }

    /**
     * Implements {@link MapSet#getType()}.
     */
    public String getType() {
      Attribute[] atts = getAttributesByName("MAPSETTYPE");
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return atts[i].getValue();
        }
      }
      return null;
    }

    /**
     * Implements {@link MapSet#getMapSetIdentifier()}.
     */
    public Identifier getMapSetIdentifier() {
      Attribute[] atts = getAttributesByName("MAPSETSID");
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return new Identifier.Default(atts[i].getValue());
        }
      }
      return null;
    }

    /**
     * Implements {@link MapSet#getName()}.
     */
    public String getName() {
      Attribute[] atts = getAttributesByName("MAPSETNAME");
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return atts[i].getValue();
        }
      }
      return null;
    }

    /**
     * Implements {@link MapSet#isFromExhaustive()}.
     */
    public boolean isFromExhaustive() {
      Attribute[] atts = getAttributesByName("MTH_MAPFROMEXHAUSTIVE");
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return atts[i].getValue().equals("Y");
        }
      }
      return false;
    }

    /**
     * Implements {@link MapSet#getIsFromExhaustive()}.
     */
    public boolean getIsFromExhaustive() {
      return isFromExhaustive();
    }
    /**
     * Implements {@link MapSet#isToExhaustive()}.
     */
    public boolean isToExhaustive() {
      Attribute[] atts = getAttributesByName("MTH_MAPTOEXHAUSTIVE");
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return atts[i].getValue().equals("Y");
        }
      }
      return false;
    }

    /**
     * Implements {@link MapSet#getIsToExhaustive()}.
     */
    public boolean getIsToExhaustive() {
      return isToExhaustive();
    }

    /**
     * Implements {@link MapSet#getFromComplexity()}.
     */
    public String getFromComplexity() {
      Attribute[] atts = getAttributesByName("MTH_MAPFROMCOMPLEXITY");
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return atts[i].getValue();
        }
      }
      return null;
    }

    /**
     * Implements {@link MapSet#getToComplexity()}.
     */
    public String getToComplexity() {
      Attribute[] atts = getAttributesByName("MTH_MAPTOCOMPLEXITY");
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return atts[i].getValue();
        }
      }
      return null;
    }

    /**
     * Implements {@link MapSet#getMapSetComplexity()}.
     */
    public String getMapSetComplexity() {
      Attribute[] atts = getAttributesByName("MTH_MAPSETCOMPLEXITY");
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return atts[i].getValue();
        }
      }
      return null;
    }

    /**
     * Implements {@link MapSet#getDescription()}.
     */
    public String getDescription() {
      Attribute[] atts = getAttributesByName("SOS");
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return atts[i].getValue();
        }
      }
      return null;
    }

    /**
     * Implements {@link MapSet#getValue(String)}.
     */
    public String getValue(String key) {
      Attribute[] atts = getAttributesByName(key);
      for (int i = 0; i < atts.length; i++) {
        if (atts[i].isReleasable()) {
          return atts[i].getValue();
        }
      }
      return null;
    }

    /**
     * Implements {@link MapSet#getMappings()}.
     */
    public Mapping[] getMappings() {
      return (Mapping[]) mappings.toArray(new Mapping[0]);
    }

    /**
     * Implements {@link MapSet#getMapTo()}.
     */
    public MapObject[] getMapTo() {
      return (MapObject[]) map_to.toArray(new MapObject[0]);
    }

    /**
     * Implements {@link MapSet#getMapFrom()}.
     */
    public MapObject[] getMapFrom() {
      return (MapObject[]) map_from.toArray(new MapObject[0]);
    }

    /**
     * Implements {@link MapSet#containsMapTo(MapObject)}.
     */
    public boolean containsMapTo(MapObject mo) {
      return map_to.contains(mo);
    }

    /**
     * Implements {@link MapSet#containsMapFrom(MapObject)}.
     */
    public boolean containsMapFrom(MapObject mo) {
      return map_from.contains(mo);
    }

    //
    // Implementation of Concept interface
    //

    /**
     * Implements {@link Concept#addAttribute(Attribute)}.
     */
    public void addAttribute(Attribute attribute) {
      if (attribute instanceof Mapping) {
        mappings.add(attribute);
      }
      else if (attribute instanceof MapObject &&
               attribute.getName().equals("XMAPTO") &&
               attribute.isReleasable()) {
        map_to.add(attribute);
      }
      else if (attribute instanceof MapObject &&
               attribute.getName().equals("XMAPFROM") &&
               attribute.isReleasable()) {
        map_from.add(attribute);

        // END OF MAPPINGS is set by MappingMapper when it finishes when restricted
      }
      else if (!attribute.getName().equals("END OF MAPPINGS")) {
        super.addAttribute(attribute);
      }
    }

    /**
     * Implements {@link Concept#clearAttributes()}.
     */
    public void clearAttributes() {
      super.clearAttributes();
      mappings.clear();
      map_to.clear();
      map_from.clear();
    }

    /**
     * Implements {@link Concept#removeAttribute(Attribute)}.
     */
    public void removeAttribute(Attribute attribute) {
      if (attribute instanceof Mapping) {
        mappings.remove(attribute);
      }
      if (attribute instanceof MapObject &&
          attribute.getName().equals("XMAPTO") &&
          attribute.isReleasable()) {
        map_to.remove(attribute);
      }
      if (attribute instanceof MapObject &&
          attribute.getName().equals("XMAPFROM") &&
          attribute.isReleasable()) {
        map_from.remove(attribute);
      }
      else {
        super.removeAttribute(attribute);
      }
    }

  }
}
