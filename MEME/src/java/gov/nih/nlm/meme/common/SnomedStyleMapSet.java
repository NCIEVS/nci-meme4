/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  SnomedStyleMapSet
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This class is an object that contains mappings of concept
 * attributes for snomed.
 *
 * @author MEME Group
 */

public class SnomedStyleMapSet extends MapSet.Default {

  /**
   * Returns the scheme identifier.
   * @return An object {@link Identifier}
   */
  public Identifier getSchemeIdentifier() {
    Attribute[] atts = getAttributesByName("MAPSETSCHEMEID");
    if (atts.length > 0) {
      return new Identifier.Default(atts[0].getValue());
    }
    return null;
  }

  /**
   * Returns the scheme name.
   * @return the scheme name
   */
  public String getSchemeName() {
    Attribute[] atts = getAttributesByName("MAPSETSCHEMENAME");
    if (atts.length > 0) {
      return atts[0].getValue();
    }
    return null;
  }

  /**
   * Returns the scheme version.
   * @return the scheme version
   */
  public String getSchemeVersion() {
    Attribute[] atts = getAttributesByName("MAPSETSCHEMEVERSION");
    if (atts.length > 0) {
      return atts[0].getValue();
    }
    return null;
  }

  /**
   * Returns the realm id.
   * @return An object {@link Identifier}
   */
  public Identifier getRealmId() {
    Attribute[] atts = getAttributesByName("MAPSETREALMID");
    if (atts.length > 0) {
      return new Identifier.Default(atts[0].getValue());
    }
    return null;
  }

  /**
   * Returns the list separator.
   * @return the list separator
   */
  public String getListSeparator() {
    Attribute[] atts = getAttributesByName("MTH_UMLSMAPSETSEPARATOR");
    if (atts.length > 0) {
      return atts[0].getValue();
    }
    return null;
  }

  /**
   * Returns the rule type.
   * @return the rule type
   */
  public String getRuleType() {
    Attribute[] atts = getAttributesByName("MAPSETRULETYPE");
    if (atts.length > 0) {
      return atts[0].getValue();
    }
    return null;
  }

  /**
   * Returns the target scheme identifier.
   * @return An object {@link Identifier}
   */
  public Identifier getTargetSchemeIdentifier() {
    Attribute[] atts = getAttributesByName("TARGETSCHEMEID");
    if (atts.length > 0) {
      return new Identifier.Default(atts[0].getValue());
    }
    return null;
  }

}