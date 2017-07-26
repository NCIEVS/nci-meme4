/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MetaProperty
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a MEME system property.
 * Data comes from the <code>meme_properties</code> table.
 *
 * @author MEME Group
 */
public class MetaProperty implements Comparable {

  //
  // The identifier
  //
  private Identifier id;

  //
  // the key
  //
  private String key;

  //
  // the key qualifier
  //
  private String key_qualifier;

  //
  // the value
  //
  private String value;

  //
  // the description
  //
  private String description;

  //
  // the definition
  //
  private String definition;

  //
  // the example
  //
  private String example;

  //
  // the documentation section reference
  //
  private String reference;

  /**
   * Instantiates a {@link MetaProperty}.
   */
  public MetaProperty() {}

  /**
   * Instantiates a {@link MetaProperty} with the specified key, key_qualifier,
   * value, description, definition, example, and section reference.
   * @param id the identifier
   * @param key the key
   * @param key_qualifier the key qualifier
   * @param value the value
   * @param description the description
   * @param definition the definition
   * @param example the example
   * @param reference the section reference
   */
  public MetaProperty(Identifier id,
                      String key, String key_qualifier, String value,
                      String description, String definition, String example,
                      String reference) {
    this.id = id;
    this.key = key;
    this.key_qualifier = key_qualifier;
    this.value = value;
    this.description = description;
    this.definition = definition;
    this.example = example;
    this.reference = reference;
  }

  /**
   * Returns the identifier.
   * @return the identifier
   */
  public Identifier getIdentifier() {
    return id;
  }

  /**
   * Returns the key.
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Returns the key qualifier.
   * @return the key qualifier
   */
  public String getKeyQualifier() {
    return key_qualifier;
  }

  /**
   * Returns the value.
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the descrpition.
   * @return the descrpition
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the definition.
   * @return the definition
   */
  public String getDefinition() {
    return definition;
  }

  /**
   * Returns the example.
   * @return the example
   */
  public String getExample() {
    return example;
  }

  /**
   * Returns the documentation section reference.
   * @return the reference
   */
  public String getReference() {
    return reference;
  }

  /**
   * Sets the identifier.
   * @param id the identifier
   */
  public void setIdentifier(Identifier id) {
    this.id = id;
  }

  /**
   * Sets the key.
   * @param key the key
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Sets the key qualifier.
   * @param key_qualifier the key qualifier
   */
  public void setKeyQualifier(String key_qualifier) {
    this.key_qualifier = key_qualifier;
  }

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Sets the descrpition.
   * @param description the descrpition
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the definition.
   * @param definition the definition
   */
  public void setDefinition(String definition) {
    this.definition = definition;
  }

  /**
   * Sets the example.
   * @param example the example
   */
  public void setExample(String example) {
    this.example = example;
  }

  /**
   * Sets the documentation section reference.
   * @param reference the reference
   */
  public void setReference(String reference) {
    this.reference = reference;
  }

  /**
   * Indicates whether or not the specified object equals this one.
   * @param o the object to compare to
   * @return <code>true</code> if equal; <code>false</codE> otherwise
   */
  public boolean equals(Object o) {
    if (o instanceof MetaProperty) {
      MetaProperty p = (MetaProperty) o;
      if (p == null) {
        return false;
      }
      if (getIdentifier() == null || p.getIdentifier() == null) {
        return (getKey() + getKeyQualifier() + getValue()).equals(
            p.getKey() + p.getKeyQualifier() + p.getValue());
      } else {
        return getIdentifier().equals(p.getIdentifier());
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
    return (getKey() + getKeyQualifier() + getValue()).hashCode();
  }

  /**
   * Implements a natural sort ordering.
   * @param o the object to compare to
   * @return an integer indicating the relative sort ordering
   */
  public int compareTo(Object o) {
    if (o instanceof MetaProperty) {
      MetaProperty p = (MetaProperty) o;
      if (p == null) {
        return 0;
      }
      if (getKey().compareTo(p.getKey()) != 0) {
        return getKey().compareTo(p.getKey());
      } else if (getKeyQualifier() != null && p.getKeyQualifier() != null &&
                 getKeyQualifier().compareTo(p.getKeyQualifier()) != 0) {
        return getKeyQualifier().compareTo(p.getKeyQualifier());
      } else if (getValue() != null && p.getValue() != null &&
                 getValue().compareTo(p.getValue()) != 0) {
        return getValue().compareTo(p.getValue());
      } else if (getDescription() != null && p.getDescription() != null) {
        return getDescription().compareTo(p.getDescription());
      } else {
        return 0;
      }
    } else {
      return 0;
    }

  }
}
