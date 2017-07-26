/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  NativeIdentifier
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import gov.nih.nlm.util.FieldedStringTokenizer;

/**
 * This class represents an identifier expressed in terms the
 * original source intended.
 * This class represents an <code>sg_id</code>
 * that uses a type of <code>CODE_SOURCE</code> or
 * <code>CODE_STRIPPED_SOURCE</code>.
 *
 * @see CoreData
 *
 * @author MEME Group
 */

public class NativeIdentifier extends Identifier.Default {

  //
  // Fields
  //
  private String qualifier = null;
  private String type = null;
  private Identifier core_data_identifier = null;
  private String core_data_type_code = null;
  private CoreData core_data = null;

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link NativeIdentifier}.
   */
  public NativeIdentifier() {
    super();
  }

  /**
   * Instantiates a {@link NativeIdentifier} from the specified id, type and qualifier.
   * @param id A code value
   * @param type A type
   * @param qualifier qualifier
   * @param core_data_identifier a core data identifier
   * @param core_data_type_code A core data type code
   */
  public NativeIdentifier(String id, String type, String qualifier,
                          String core_data_identifier,
                          String core_data_type_code) {
    super(id);
    if (qualifier != null && !qualifier.equals("")) {
      this.qualifier = qualifier;
    }
    this.type = type;
    if (core_data_identifier != null) {
      this.core_data_identifier = new Identifier.Default(core_data_identifier);
    }
    this.core_data_type_code = core_data_type_code;
  }

  /**
   * Returns the qualifying identifier.
   * @return the qualifying identifier
   */
  public String getQualifier() {
    return qualifier;
  }

  /**
   * Sets the qualifying identifier.
   * @param qualifier the qualifying identifier
   */
  public void setQualifier(String qualifier) {
    this.qualifier = qualifier;
  }

  /**
   * Returns the qualifier type.
   * @return the qualifier type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the qualifier type.
   * @param type the qualifier type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Returns the core data {@link Identifier}.
   * @return the core data {@link Identifier}
   */
  public Identifier getCoreDataIdentifier() {
    return core_data_identifier;
  }

  /**
   * Sets the core data {@link Identifier}.
   * @param core_data_identifier the core data {@link Identifier}
   */
  public void setCoreDataIdentifier(Identifier core_data_identifier) {
    this.core_data_identifier = core_data_identifier;
  }

  /**
   * Returns the core data type code.
   * @return the core data type code
   */
  public String getCoreDataTypeCode() {
    return core_data_type_code;
  }

  /**
   * Sets the core data type code.
   * @param core_data_type_code the core data type code
   */
  public void setCoreDataTypeCode(String core_data_type_code) {
    this.core_data_type_code = core_data_type_code;
  }

  /**
   * Returns the {@link CoreData} element
   * @return the {@link CoreData} element
   */
  public CoreData getCoreDataElement() {
    return core_data;
  }

  /**
   * Sets the {@link CoreData} element.
   * @param core_data the {@link CoreData} element
   */
  public void setCoreDataElement(CoreData core_data) {
    this.core_data = core_data;
  }

  /**
   * Returns the string representation.
   * <B>DO NOT CHANGE</B>, used by serialization to treat as a primitive value.
   * @return the string representation
   */
  public String getString() {
    return toString() + "~" +
        getType() + "~" +
        getQualifier() + "~" +
        (getCoreDataIdentifier() != null ? getCoreDataIdentifier().toString() :
         "") + "~" +
        (getCoreDataTypeCode() != null ? getCoreDataTypeCode() : "");
  }

  /**
   * Returns new instance of {@link NativeIdentifier}.
   * <B>DO NOT CHANGE</B>, used by serialization to treat as a primitive value.
   * @param s string containing native identifier
   * @return new instance of {@link NativeIdentifier}
   */
  public static NativeIdentifier newNativeIdentifier(String s) {
    String[] ss = FieldedStringTokenizer.split(s, "~");
    if (ss.length == 4) {
      return new NativeIdentifier(ss[0], ss[1], ss[2], ss[3], "");
    } else {
      return new NativeIdentifier(ss[0], ss[1], ss[2], ss[3], ss[4]);
    }
  }
}
