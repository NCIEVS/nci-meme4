/**
 * InfoDialog.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;

import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.Relationship;

public class InfoDialog
    extends JDialog {

  //
  // Private Fields
  //
  private JEditorPane editor = null;

  //
  // Constructors
  //

  /**
   * Creates dialog, which displays additional information
   * about a relationship such as authority, source, etc.
   *
   * @param owner Frame
   * @param title String
   * @param rel Relationship
   */
  public InfoDialog(Frame owner, String title, Relationship rel) {
    // non-modal dialog, i.e., other
    // windows can be active at the same time
    super(owner, false);
    setTitle(title);
    initComponents();
    editor.setText(getRelationshipInfo(rel));
  }

  /**
   * Creates dialog, which displays an STY's
   * definition and example.
   *
   * @param owner Frame
   * @param title String
   * @param sty ConceptSemanticType
   */
  public InfoDialog(Frame owner, String title, ConceptSemanticType sty) {
    // non-modal dialog, i.e., other
    // windows can be active at the same time
    super(owner, false);
    setTitle(title);
    initComponents();

    editor.setText(getSTYInfo(sty));
  }

  /**
   * Creates dialog, which displays relationship attribute's
   * definition and example.
   *
   * @param owner Frame
   * @param title String
   * @param rel_attr String
   */
  public InfoDialog(Frame owner, String title, String rel_attr) {
    // non-modal dialog, i.e., other
    // windows can be active at the same time
    super(owner, false);
    setTitle(title);
    initComponents();

    editor.setText(getRelAttrInfo(rel_attr));
  }

  //
  // Methods
  //
  private void initComponents() {
    Container contents = getContentPane();
    contents.setLayout(new BorderLayout());

    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    editor = new JEditorPane();
    editor.setEditable(false);
    editor.setContentType("text/html");
    editor.setBackground(new Color(MetalLookAndFeel.getControl().getRGB()));

    // a buttons panel
    JPanel button_panel = new JPanel();

    JButton ok_btn = new JButton();
    ok_btn.setText("Ok");
    ok_btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        setVisible(false);
        dispose();
      }
    });

    button_panel.add(ok_btn);

    // lay out components
    contents.add(editor, BorderLayout.CENTER);
    contents.add(button_panel, BorderLayout.SOUTH);
  } // initComponents()

  private String getRelationshipInfo(Relationship rel) {
    StringBuffer sb = new StringBuffer(500);
    sb.append("<HTML>");
    sb.append("<HEAD></HEAD>");
    sb.append("<BODY>");

    // table starts here
    sb.append("<TABLE border=1>");

    sb.append("<TR>");
    sb.append("<TD>Level: <TD>");
    sb.append(rel.getLevel());

    sb.append("<TR>");
    sb.append("<TD>Authority: <TD>");
    sb.append(rel.getAuthority().toString());

    sb.append("<TR>");
    sb.append("<TD>Source: <TD>");
    sb.append(rel.getSource().toString());

    sb.append("<TR>");
    sb.append("<TD>Source of Label: <TD>");
    sb.append(rel.getSourceOfLabel().toString());

    sb.append("<TR>");
    sb.append("<TD>Timestamp: <TD>");
    sb.append(rel.getTimestamp().toString());

    sb.append("<TR>");
    sb.append("<TD>Status: <TD>");
    sb.append(rel.getStatus());

    sb.append("<TR>");
    sb.append("<TD>Tobereleased: <TD>");
    sb.append(rel.getTobereleased());

    // table ends here
    sb.append("</TABLE>");

    // footer
    sb.append("</BODY>");
    sb.append("</HTML>");

    return sb.toString();
  } // getRelationshipInfo()

  // Returns sty's definition and example
  // MTH.SRDEF table is a good table to query
  // for this kind of information.
  private String getSTYInfo(ConceptSemanticType sty) {
    StringBuffer sb = new StringBuffer(500);

    sb.append("<HTML>");
    sb.append("<HEAD></HEAD>");
    sb.append("<BODY>");

    // table starts here
    sb.append("<TABLE border=1>");

    // STY name
    sb.append("<TR>");
    sb.append("<TH colspan=2>");
    sb.append(sty.getValue());

    // Definition
    sb.append("<TR>");
    sb.append("<TD>Definition<TD>");
    sb.append( ( (ConceptSemanticType) sty).getDefinition());

    // Example
    // TODO: API is needed
    sb.append("<TR>");
    sb.append("<TD>Example<TD>");
    sb.append("");

    // table ends here
    sb.append("</TABLE>");

    // footer
    sb.append("</BODY>");
    sb.append("</HTML>");

    return sb.toString();
  } // getSTYInfo()

  // Needs MEME4 functionality
  // MTH.SRDEF looks like a good table
  // for this kind of information.
  private String getRelAttrInfo(String rel_attr) {
    String definition = "";
    String example = "";

    StringBuffer sb = new StringBuffer(500);

    sb.append("<HTML>");
    sb.append("<HEAD></HEAD>");
    sb.append("<BODY>");

    // table starts here
    sb.append("<TABLE border=1>");

    // Relationship attribute's name
    sb.append("<TR>");
    sb.append("<TH colspan=2>");
    sb.append(rel_attr);

    // Definition
    sb.append("<TR>");
    sb.append("<TD>Definition<TD>");
    sb.append(definition);

    // Example
    sb.append("<TR>");
    sb.append("<TD>Example<TD>");
    sb.append(example);

    // table ends here
    sb.append("</TABLE>");

    // footer
    sb.append("</BODY>");
    sb.append("</HTML>");

    return sb.toString();
  } // getRelAttrInfo()

  public Dimension getPreferredSize() {
    return new Dimension(500, 350);
  }
}
