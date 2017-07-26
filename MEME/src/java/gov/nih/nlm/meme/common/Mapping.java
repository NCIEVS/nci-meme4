/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Mapping
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.util.FieldedStringTokenizer;

/**
 * Generically represents a mapping between two identifiers or expressions.
 *
 * @author MEME Group
 */

public interface Mapping extends Attribute {

  /**
   * Sets the subset {@link Identifier}.
   * @param map_subset_id the subset {@link Identifier}
   */
  public void setSubsetIdentifier(Identifier map_subset_id);

  /**
   * Returns the subset {@link Identifier}.
   * @return the subset {@link Identifier}
   */
  public Identifier getSubsetIdentifier();

  /**
   * Sets the map rank.  This value is used for ordering entries
   * within a subset.
   * @param map_rank the map rank
   */
  public void setMapRank(String map_rank);

  /**
   * Returns the map rank.
   * @return the map rank
   */
  public String getMapRank();

  /**
   * Sets the "map from" {@link MapObject}.
   * @param map_object the "map from" {@link MapObject}
   */
  public void setFrom(MapObject map_object);

  /**
   * Returns the "map from" {@link MapObject}.
   * @return the "map from" {@link MapObject}
   */
  public MapObject getFrom();

  /**
   * Sets the relationship name.
   * @param map_rel_name the relationship name
   */
  public void setRelationshipName(String map_rel_name);

  /**
   * Returns the relationship name.
   * @return the relationship name
   */
  public String getRelationshipName();

  /**
   * Sets the relationship attribute.
   * @param map_rel_attr the relationship attribute
   */
  public void setRelationshipAttribute(String map_rel_attr);

  /**
   * Returns the relationship attribute.
   * @return the relationship attribute
   */
  public String getRelationshipAttribute();

  /**
   * Sets the "map to" {@link MapObject}.
   * @param map_object the "map to" {@link MapObject}
   */
  public void setTo(MapObject map_object);

  /**
   * Returns the "map to" {@link MapObject}.
   * @return the "map to" {@link MapObject}
   */
  public MapObject getTo();

  /**
   * Sets the machine processable rule.  This is used to determine whether
   * or not this mapping should apply based on specified conditions.
   * @param map_rule the machine processable rule.
   */
  public void setRule(String map_rule);

  /**
   * Returns the machine processable rule.
   * @return the machine processable rule
   */
  public String getRule();

  /**
   * Sets the human readable restriction. This is used to determine whether
   * or not this mapping should apply based on specified conditions.
   * @param map_res the human readable restriction
   */
  public void setRestriction(String map_res);

  /**
   * Returns the human readable restriction.
   * @return the human readable restriction
   */
  public String getRestriction();

  /**
   * Sets the type.
   * @param map_type the type
   */
  public void setType(String map_type);

  /**
   * Returns the type.
   * @return the type
   */
  public String getType();

  /**
   * Sets the mapping attribute name.
   * @param map_attr_name the mapping attribute name
   */
  public void setMappingAttributeName(String map_attr_name);

  /**
   * Returns the mapping attribute name.
   * @return the mapping attribute name
   */
  public String getMappingAttributeName();

  /**
   * Sets the mapping attribute value.
   * @param map_attr_value the mapping attribute value
   */
  public void setMappingAttributeValue(String map_attr_value);

  /**
   * Returns the mapping attribute value.
   * @return the mapping attribute value
   */
  public String getMappingAttributeValue();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link Mapping} interface.
   */
  public class Default extends Attribute.Default implements Mapping {

    //
    // Fields
    //

    private Identifier map_subset_id = null;
    private String map_rank = null;
    private MapObject map_to_object = null;
    private MapObject map_from_object = null;
    private String map_rel_name = null;
    private String map_rel_attr = null;
    private String map_rule = null;
    private String map_res = null;
    private String map_type = null;
    private String map_attr_name = null;
    private String map_attr_value = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link Mapping}.
     */
    public Default() {
      super();
    }

    //
    // Implementation of Mapping interface
    //

    /**
     * Implements {@link Mapping#setSubsetIdentifier(Identifier)}.
     */
    public void setSubsetIdentifier(Identifier map_subset_id) {
      this.map_subset_id = map_subset_id;
    }

    /**
     * Implements {@link Mapping#getSubsetIdentifier()}.
     */
    public Identifier getSubsetIdentifier() {
      return map_subset_id;
    }

    /**
     * Implements {@link Mapping#setMapRank(String)}.
     */
    public void setMapRank(String map_rank) {
      this.map_rank = map_rank;
    }

    /**
     * Implements {@link Mapping#getMapRank()}.
     */
    public String getMapRank() {
      return map_rank;
    }

    /**
     * Implements {@link Mapping#setFrom(MapObject)}.
     */
    public void setFrom(MapObject map_object) {
      this.map_from_object = map_object;
    }

    /**
     * Implements {@link Mapping#getFrom()}.
     */
    public MapObject getFrom() {
      return map_from_object;
    }

    /**
     * Implements {@link Mapping#setRelationshipName(String)}.
     */
    public void setRelationshipName(String map_rel_name) {
      this.map_rel_name = map_rel_name;
    }

    /**
     * Implements {@link Mapping#getRelationshipName()}.
     */
    public String getRelationshipName() {
      return map_rel_name;
    }

    /**
     * Implements {@link Mapping#setRelationshipAttribute(String)}.
     */
    public void setRelationshipAttribute(String map_rel_attr) {
      this.map_rel_attr = map_rel_attr;
    }

    /**
     * Implements {@link Mapping#getRelationshipAttribute()}.
     */
    public String getRelationshipAttribute() {
      return map_rel_attr;
    }

    /**
     * Implements {@link Mapping#setTo(MapObject)}.
     */
    public void setTo(MapObject map_object) {
      this.map_to_object = map_object;
    }

    /**
     * Implements {@link Mapping#getTo()}.
     */
    public MapObject getTo() {
      return map_to_object;
    }

    /**
     * Implements {@link Mapping#setRule(String)}.
     */
    public void setRule(String map_rule) {
      this.map_rule = map_rule;
    }

    /**
     * Implements {@link Mapping#getRule()}.
     */
    public String getRule() {
      return map_rule;
    }

    /**
     * Implements {@link Mapping#setRestriction(String)}.
     */
    public void setRestriction(String map_res) {
      this.map_res = map_res;
    }

    /**
     * Implements {@link Mapping#getRestriction()}.
     */
    public String getRestriction() {
      return map_res;
    }

    /**
     * Implements {@link Mapping#setType(String)}.
     */
    public void setType(String map_type) {
      this.map_type = map_type;
    }

    /**
     * Implements {@link Mapping#getType()}.
     */
    public String getType() {
      return map_type;
    }

    /**
     * Implements {@link Mapping#setMappingAttributeName(String)}.
     */
    public void setMappingAttributeName(String map_attr_name) {
      this.map_attr_name = map_attr_name;
    }

    /**
     * Implements {@link Mapping#getMappingAttributeName()}.
     */
    public String getMappingAttributeName() {
      return map_attr_name;
    }

    /**
     * Implements {@link Mapping#setMappingAttributeValue(String)}.
     */
    public void setMappingAttributeValue(String map_attr_value) {
      this.map_attr_value = map_attr_value;
    }

    /**
     * Implements {@link Mapping#getMappingAttributeValue()}.
     */
    public String getMappingAttributeValue() {
      return map_attr_value;
    }

    //
    // Implementation of Attribute interface
    //

    /**
     * Overrides {@link Attribute#setValue(String)}.
     * Sets the value that will represent as micro-syntax rows
     * in the attributes table with attribute_name = 'XMAP' and attached
     * to the XM atom for the map set.  These attributes values will be
     * ~ separated fields, one for each of MRMAP fields:
     * MAPSUBSETID, MAPRANK, FROMID, REL, RELA, TOID
     * MAPRULE, MAPTYPE, MAPATN, MAPATV, MAPSID, MAPRES.
     * @param value the attribute value
     */
    public void setValue(String value) {
      if (value.startsWith("<>Long_Attribute<>:")) {
        super.setValue(value);
        return;
      }
      super.setValue(null);

      // (MAPSUBSETID, MAPRANK, FROMID, REL, RELA, TOID
      //  MAPRULE, MAPTYPE, MAPATN, MAPATV, MAPSID, MAPRES)
      String[] tokens = FieldedStringTokenizer.split(value, "~");
      setSubsetIdentifier(new Identifier.Default(tokens[0]));
      setMapRank(tokens[1]);
      setRelationshipName(tokens[3]);
      setRelationshipAttribute(tokens[4]);
      setRule(tokens[6]);
      setType(tokens[7]);
      setMappingAttributeName(tokens[8]);
      setMappingAttributeValue(tokens.length > 9 ? tokens[9] : "");
      setSourceIdentifier(tokens.length > 10 ? new Identifier.Default(tokens[10]) : null);
      setRestriction(tokens.length > 11 ? tokens[11] : "");
    }

    /**
     * Overrides {@link Attribute#getValue()}.
     * Returns the value that will represent as micro-syntax rows
     * in the attributes table with attribute_name = 'XMAP' and attached
     * to the XM atom for the map set.  These attributes values will be
     * ~ separated fields, one for each of MRMAP fields:
     * MAPSUBSETID, MAPRANK, FROMID, REL, RELA, TOID
     * MAPRULE, MAPTYPE, MAPATN, MAPATV, MAPSID, MAPRES.
     * @return the attribute value
     */
    public String getValue() {
      if (super.getValue() != null &&
          super.getValue().startsWith("<>Long_Attribute<>:")) {
        return super.getValue();
      }
      StringBuffer sb = new StringBuffer(10000);
      sb.append(getSubsetIdentifier() == null ? "" :
                getSubsetIdentifier().toString())
          .append("~")
          .append(getMapRank())
          .append("~")
          .append(getFrom() == null ? "" : getFrom().getIdentifier() == null ?
                  "" :
                  getFrom().getIdentifier().toString())
          .append("~")
          .append(getRelationshipName())
          .append("~")
          .append(getRelationshipAttribute())
          .append("~")
          .append(getTo() == null ? "" : getTo().getIdentifier() == null ? "" :
                  getTo().getIdentifier().toString())
          .append("~")
          .append(getRule())
          .append("~")
          .append(getType())
          .append("~")
          .append(getMappingAttributeName())
          .append("~")
          .append(getMappingAttributeValue())
          .append("~")
          .append(getSourceIdentifier() == null ? "" :
                  getSourceIdentifier().toString())
          .append("~")
          .append(getRestriction());
      return sb.toString();
    }

  }
}
