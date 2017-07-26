/**
 * RelLabel.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * A label for a fully spelled-out relationship.
 */
public class RelLabel
    extends JLabel {

  //
  // Private fields
  //
  private String concept_1_pn = null;
  private String concept_2_pn = null;
  private String rel_name = null;
  private String rel_attr = null;

  //
  // Constructors
  //
  public RelLabel() {
    setHorizontalAlignment(SwingConstants.CENTER);
    setForeground(Color.blue);
    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.black,
                                               Color.gray));
    int width = this.getPreferredSize().width;
    this.setPreferredSize(new Dimension(width, 20));
    this.setMinimumSize(this.getPreferredSize());

    //Font font = new Font("Serif", Font.PLAIN, 12);

    setLabel();
  }

  //
  // Accessor Methods
  //
  void setConcept1_PrefName(String name) {
    concept_1_pn = name;
  }

  void setConcept2_PrefName(String name) {
    concept_2_pn = name;
  }

  public void setRelationship_Name(String name) {
    rel_name = (name == null) ? "" : RelSemantics.getLongForm(name);
    setLabel();
  } // setRelationship_Name

  public void setRelationship_Attr(String attr) {
    rel_attr = (attr == null) ? "" : attr;
    setLabel();
  } // setRelationship_Attr()

  String getLabel() {
    return getText();
  }

  void clearContent() {
    concept_1_pn = null;
    rel_name = null;
    rel_attr = null;
    concept_2_pn = null;
    setText(null);
  }

  //
  // Private Methods
  //
  private void setLabel() {
    StringBuffer sb = new StringBuffer(500);
    sb.append( (concept_1_pn == null) ? "" : concept_1_pn);
    sb.append("   ");
    sb.append( (rel_name == null) ? "" : rel_name);
    sb.append(" | ");
    sb.append( (rel_attr == null) ? "" : rel_attr);
    sb.append("   ");
    sb.append( (concept_2_pn == null) ? "" : concept_2_pn);

    setText(sb.toString());
  } // setLabel()
}
