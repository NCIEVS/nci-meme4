/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MetaCode
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a key/value with a type.
 * Data comes from the <code>code_map</code> table.
 *
 * @author MEME Group
 */
public class MetaCode implements Comparable {

  //
  // The identifier
  //
  private Identifier id;

  //
  // the code
  //
  private String code;

  //
  // tye type
  //
  private String type;

  //
  // the value
  //
  private String value;

  /**
   * Instantiates a {@link MetaCode}.
   */
  public MetaCode() {}

  /**
       * Instantiates a {@link MetaCode} with the specified id, code, type, and value
   * @param id the id
   * @param code the code
   * @param type the type
   * @param value the value
   */
  public MetaCode(Identifier id, String code, String type, String value) {
    this.id = id;
    this.code = code;
    this.type = type;
    this.value = value;
  }

  /**
   * Returns the identifier;
   * @return the identifier
   */
  public Identifier getIdentifier() {
    return id;
  }

  /**
   * Returns the code.
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * Returns the type.
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the value.
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the identifier.
   * @param id the identifier
   */
  public void setIdentifier(Identifier id) {
    this.id = id;
  }

  /**
   * Sets the code.
   * @param code the code
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Sets the type.
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Indicates whether or not the specified object equals this one.
   * @param o the object to compare to
   * @return <code>true</code> if equal; <code>false</codE> otherwise
   */
  public boolean equals(Object o) {
    if (o instanceof MetaCode) {
      MetaCode c = (MetaCode) o;
      if (c == null) {
        return false;
      }
      if (getIdentifier() == null) {
        return (getCode() + getType() + getValue()).equals(
            c.getCode() + c.getType() + c.getValue());
      } else {
        return getIdentifier().equals(c.getIdentifier());
      }
    } else {
      return false;
    }
  }

  /**
   * Returns the <code>int</code> hashcode.
   * @return the <code>int</code> hashcode
   */
  public int hashCode() {
    return (getCode() + getType() + getValue()).hashCode();
  }

  /**
   * Implements a natural sort ordering.
   * @param o the object to compare to
   * @return an integer indicating the relative sort ordering
   */
  public int compareTo(Object o) {
    if (o instanceof MetaCode) {
      MetaCode c = (MetaCode) o;
      if (c == null) {
        return 0;
      }
      if (getCode().compareTo(c.getCode()) != 0) {
        return getCode().compareTo(c.getCode());
      } else if (getType().compareTo(c.getType()) != 0) {
        return getType().compareTo(c.getType());
      } else {
        return getValue().compareTo(c.getValue());
      }
    } else {
      return 0;
    }

  }
}
