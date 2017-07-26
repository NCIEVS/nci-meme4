/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MapObject
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.util.FieldedStringTokenizer;

/**
 * Generically represents the identifier or expression of the "to" or "from" of
 * a {@link Mapping}.
 *
 * @author MEME Group
 */

public interface MapObject extends Attribute {

  /**
   * Sets the {@link Identifier}.
   * @param mo_id the {@link Identifier}
   */
  public void setMapObjectIdentifier(Identifier mo_id);

  /**
   * Returns the {@link Identifier}.
   * @return the {@link Identifier}
   */
  public Identifier getMapObjectIdentifier();

  /**
   * Sets the source asserted {@link Identifier}.
   * @param mo_id the source asserted {@link Identifier}
   */
  public void setMapObjectSourceIdentifier(Identifier mo_id);

  /**
   * Returns the source asserted {@link Identifier}.
   * @return the source asserted {@link Identifier}
   */
  public Identifier getMapObjectSourceIdentifier();

  /**
   * Sets the expression.
   * @param mo_expression an expression
   */
  public void setExpression(String mo_expression);

  /**
   * Returns the expression.
   * @return the expression
   */
  public String getExpression();

  /**
   * Sets the type.
   * @param mo_type a type
   */
  public void setType(String mo_type);

  /**
   * Returns the type.
   * @return the type
   */
  public String getType();

  /**
   * Sets the rule.
   * @param mo_rule a rule
   */
  public void setRule(String mo_rule);

  /**
   * Returns the rule.
   * @return the rule
   */
  public String getRule();

  /**
   * Sets the restriction.
   * @param mo_restriction a restriction
   */
  public void setRestriction(String mo_restriction);

  /**
   * Returns the restriction.
   * @return the restriction
   */
  public String getRestriction();

  /**
   * Sets the {@link Source}.
   * @param mo_source the {@link Source}
   */
  public void setMapObjectSource(Source mo_source);

  /**
   * Returns the {@link Source}.
   * @return the {@link Source}
   */
  public Source getMapObjectSource();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link MapObject} interface.
   */
  public class Default extends Attribute.Default implements MapObject {

    //
    // Fields
    //

    private Identifier mo_id = null;
    private Identifier mo_source_id = null;
    private String mo_expression = null;
    private String mo_type = null;
    private String mo_rule = null;
    private String mo_restriction = null;
    private Source mo_source = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link MapObject}.
     */
    public Default() {
      super();
    }

    //
    // Implementation of MapObject interface
    //

    /**
     * Implements {@link MapObject#setMapObjectIdentifier(Identifier)}.
     */
    public void setMapObjectIdentifier(Identifier mo_id) {
      this.mo_id = mo_id;
    }

    /**
     * Implements {@link MapObject#getMapObjectIdentifier()}.
     */
    public Identifier getMapObjectIdentifier() {
      return mo_id;
    }

    /**
     * Implements {@link MapObject#setMapObjectSourceIdentifier(Identifier)}.
     */
    public void setMapObjectSourceIdentifier(Identifier mo_source_id) {
      this.mo_source_id = mo_source_id;
    }

    /**
     * Implements {@link MapObject#getMapObjectSourceIdentifier()}.
     */
    public Identifier getMapObjectSourceIdentifier() {
      return mo_source_id;
    }

    /**
     * Implements {@link MapObject#setExpression(String)}.
     */
    public void setExpression(String mo_expression) {
      this.mo_expression = mo_expression;
    }

    /**
     * Implements {@link MapObject#getExpression()}.
     */
    public String getExpression() {
      return mo_expression;
    }

    /**
     * Implements {@link MapObject#setType(String)}.
     */
    public void setType(String mo_type) {
      this.mo_type = mo_type;
    }

    /**
     * Implements {@link MapObject#getType()}.
     */
    public String getType() {
      return mo_type;
    }

    /**
     * Implements {@link MapObject#setRule(String)}.
     */
    public void setRule(String mo_rule) {
      this.mo_rule = mo_rule;
    }

    /**
     * Implements {@link MapObject#getRule()}.
     */
    public String getRule() {
      return mo_rule;
    }

    /**
     * Implements {@link MapObject#setRestriction(String)}.
     */
    public void setRestriction(String mo_restriction) {
      this.mo_restriction = mo_restriction;
    }

    /**
     * Implements {@link MapObject#getRestriction()}.
     */
    public String getRestriction() {
      return mo_restriction;
    }

    /**
     * Implements {@link MapObject#setMapObjectSource(Source)}.
     */
    public void setMapObjectSource(Source mo_source) {
      this.mo_source = mo_source;
    }

    /**
     * Implements {@link MapObject#getMapObjectSource()}.
     */
    public Source getMapObjectSource() {
      return mo_source;
    }

    //
    // Implementation of Attribute interface
    //

    /**
     * Overrides {@link Attribute#getValue()}.
     * Sets the value that will be represented as micro-syntax rows in the
     * attributes table with the attribute_name  of 'XMAPTO' or 'XMAPFROM and
     * also attached to the XM atom.  For each mapping the FROMUI will be
     * represented by an XMAPTO entry, and the TOUI by an XMAPTO entry.
     * These attributes will be ~ separated fields, one for each fields.
     * @param value the value
     */
    public void setValue(String value) {
      if (value.startsWith("<>Long_Attribute<>:")) {
        super.setValue(value);
        return;
      }
      super.setValue(null);
      String[] tokens = FieldedStringTokenizer.split(value, "~");
      setMapObjectIdentifier(new Identifier.Default(tokens[0]));
      setMapObjectSourceIdentifier(new Identifier.Default(tokens[1]));
      setExpression(tokens[2]);
      setType(tokens[3]);
      setRule(tokens[4]);
      if (tokens.length > 5) {
        setRestriction(tokens[5]);
      } else {
        setRestriction("");
      }
    }

    /**
     * Overrides {@link Attribute#getValue()}.
     * Returns the value that will be represented as micro-syntax rows in the
     * attributes table with the attribute_name  of 'XMAPTO' or 'XMAPFROM and
     * also attached to the XM atom.  For each mapping the FROMUI will be
     * represented by an XMAPTO entry, and the TOUI by an XMAPTO entry.
     * These attributes will be ~ separated fields, one for each fields.
     * @return the value
     */
    public String getValue() {
      if (super.getValue() != null &&
          super.getValue().startsWith("<>Long_Attribute<>:")) {
        return super.getValue();
      }
      StringBuffer sb = new StringBuffer(10000);
      sb.append(getMapObjectIdentifier() == null ? "" :
                getMapObjectIdentifier().toString())
          .append("~")
          .append(getMapObjectSourceIdentifier() == null ? "" :
                  getMapObjectSourceIdentifier().toString())
          .append("~")
          .append(getExpression())
          .append("~")
          .append(getType())
          .append("~")
          .append(getRule())
          .append("~")
          .append(getRestriction())
          .append("~");
      return sb.toString();
    }

  }
}
