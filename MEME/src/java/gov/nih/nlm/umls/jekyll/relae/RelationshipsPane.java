/**
 * RelationshipsPane.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.Vector;

import gov.nih.nlm.umls.jekyll.JekyllKit;
import gov.nih.nlm.umls.jekyll.swing.SwingUtilities;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;

/**
 * Class for displaying NLM type relationships
 * and all other, non-NLM, relationships.
 */
public class RelationshipsPane
    extends JPanel
    implements ListSelectionListener {

  //
  // Private Fields
  //
  private JList rels_list = null;
  private Relationship[] rels = null;
  int concept_id_1 = 0;
  private RelaEditor frame = null;

  //
  // Constructors
  //
  public RelationshipsPane(RelaEditor frame, String name) {

    this.frame = frame;

    rels_list = new JList();
    rels_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    rels_list.addListSelectionListener(this);

    if (name.equals("Other Rels")) {
      rels_list.setEnabled(false);
    }

    JScrollPane rels_sp = new JScrollPane(rels_list);
    // TODO: try with the new constraints' values
    rels_sp.setPreferredSize(new Dimension(200, 100));
    // we set minimum size to prevent horizontal resizing
    rels_sp.setMinimumSize(new Dimension(200, 100));

    setName(name);
    setBorder(SwingUtilities.buildTitledBorder(name));
    setLayout(new BorderLayout());
    add(rels_sp, BorderLayout.CENTER);
  }

  //
  // Methods
  //
  public void clearContent() {
    rels_list.setListData(new Vector());
    rels = null;
  }

  public void clearSelection() {
    rels_list.clearSelection();
  }

  void setContent(Relationship[] rels) {

    this.rels = rels;

    if (rels == null) {
      clearContent();
      return;
    }

    Vector v = new Vector();
    StringBuffer sb = new StringBuffer(500);

    for (int i = 0; i < rels.length; i++) {

      if (rels[i].getTobereleased() == Relationship.FV_UNRELEASABLE) {
        continue;
      }

      String rel_name = rels[i].getName();
      String rel_attr = (rels[i].getAttribute() == null) ? "" :
          rels[i].getAttribute();

// ==================================================
// The commented block below used to invert values of
// relationship names and attributes, so that
// a relationship could be read from left-to-right.
// Abandoned it in favor of the way concept reports
// are read, i.e., from right-to-left, to avoid
// confusion.
// ==================================================
//      if (rel_name.equals("BT")) {
//        rel_name = "NT";
//      }
//      else if (rel_name.equals("NT")) {
//        rel_name = "BT";
//      }
//
//      if ( (rel_name.equals("BT") || rel_name.equals("NT")) &&
//          (rel_attr != null)) {
//        // TODO: MEME4 method?
//        rel_attr = RelSemantics.getInvertedAttribute(rel_attr);
//      }
// ==================================================

      sb.append(rel_name);
      sb.append(" | ");
      sb.append(rel_attr);
      sb.append(" | ");
      Concept concept = rels[i].getRelatedConcept();
      sb.append(concept.getPreferredAtom().getString());
      sb.append("( ");
      sb.append(concept.getIdentifier().toString());
      sb.append(" )");
      v.add(sb.toString());

      // clear StringBuffer
      sb.delete(0, sb.length());
    }

    rels_list.setListData(v);
  } // setContent()

  public Relationship getSelectedRel() {
    if (rels_list.isSelectionEmpty()) {
      return null;
    }
    else {
      return rels[rels_list.getSelectedIndex()];
    }
  }

  //
  // Interface Implementation
  //
  public void valueChanged(ListSelectionEvent evt) {

    if (evt.getValueIsAdjusting() || rels_list.isSelectionEmpty()) {
      return;
    }

    frame.getGlassPane().setVisible(true);

    try {
      RelPane rel_panel = frame.getRel_Panel();
      RelPane rela_panel = frame.getRela_Panel();
      RelLabel rel_label = frame.getRel_Label();
      ConceptPane concept_2_panel = frame.getConcept_2_Panel();
      SessionToken st = frame.getSessionToken();

      int index = rels_list.getSelectedIndex();
      String current_rel = rels[index].getName();
      String current_attr = rels[index].getAttribute();
      Concept concept = JekyllKit.getCoreDataClient().getConcept(rels[index].
          getRelatedConcept());

      rel_panel.setCurrentValue(current_rel);
      rela_panel.setCurrentValue(current_attr);

      // load concept report for concept 2
      concept_2_panel.setContent(concept);

      // set values for SessionToken
      st.setCurrent_Rel(current_rel);
      st.setCurrent_Attr(current_attr);
      st.setNew_Rel(current_rel);
      st.setNew_Attr(current_attr);
      st.setRelationship_Id(rels[index].getIdentifier().intValue());
      st.setConceptId_2(concept.getIdentifier().intValue());

      // set directionality label
      rel_label.setConcept2_PrefName(concept.getPreferredAtom().getString());
      rel_label.setRelationship_Name(current_rel);
      rel_label.setRelationship_Attr(current_attr);

    }
    catch (Exception ex) {
      ex.printStackTrace(JekyllKit.getLogWriter());
      MEMEToolkit.notifyUser(frame, "Failed to load related concept."
                             + "\nLog file may contain more information.");
    }
    finally {
      frame.getGlassPane().setVisible(false);
    }
  } // valueChanged()

}
