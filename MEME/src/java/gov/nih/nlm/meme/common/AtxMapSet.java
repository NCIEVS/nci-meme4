/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtxMapSet
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This class is an object that contains mappings of concept
 * attributes.
 *
 * @author MEME Group
 */

public class AtxMapSet extends MapSet.Default {

  /**
   * Returns the grammar.
   * @return the grammar
   */
  public String getGrammar() {
    Attribute[] atts = getAttributesByName("MAPSETGRAMMAR");
    if (atts.length > 0) {
      return atts[0].getValue();
    }
    return null;
  }

}