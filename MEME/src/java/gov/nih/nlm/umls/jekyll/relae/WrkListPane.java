/**
 * WrkListPane.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;

import gov.nih.nlm.umls.jekyll.JekyllKit;
import gov.nih.nlm.umls.jekyll.swing.SwingUtilities;

/**
 * A panel for displaying pairs of concepts from
 * selected worklist/checklist.
 *
 * <p>
 * History:
 * <br>
 *  01/24/2002: First version<br>
 *  05/15/2002: SNF editing related changes to the concept report loading
 *              mechanism.<br>
 *  09/20/2002: Modifications to allow a single concept load.
 */
public class WrkListPane
    extends JPanel
    implements ListSelectionListener {

  //
  // Private Fields
  //
  private JList list = null;
  private WrkList w = null;
  private Concept concept_1 = null;
  private Concept concept_2 = null;
  private boolean single_concept = false;

  private RelaEditor frame = null;
  private ConceptPane concept_2_panel = null;
  private ConceptPane concept_1_panel = null;
  private RelationshipsPane nlm_rels_panel = null;
  private RelationshipsPane other_rels_panel = null;
  private RelLabel rel_label = null;
  private SessionToken st = null;

  //
  // Constructors
  //
  public WrkListPane(RelaEditor frame) {
    this.frame = frame;
    initComponents();
  }

  private void initComponents() {
    concept_1_panel = frame.getConcept_1_Panel();
    concept_2_panel = frame.getConcept_2_Panel();
    nlm_rels_panel = frame.getNLM_Rels_Panel();
    other_rels_panel = frame.getOther_Rels_Panel();
    rel_label = frame.getRel_Label();
    st = frame.getSessionToken();

    // list
    list = new JList();
    list.setSelectionModel(new WrkListSelectionModel());
    list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    list.addListSelectionListener(this);
    list.setCellRenderer(new WrkListCellRenderer());

    JScrollPane list_sp = new JScrollPane(list);

    // we set minimum size here to prevent horizontal
    // resizing
    // TODO: try with the new constraints' values
    Dimension d = new Dimension(100, list_sp.getPreferredSize().height);
    list_sp.setPreferredSize(d);
    list_sp.setMinimumSize(d);

    setBorder(SwingUtilities.buildTitledBorder("Worklist Browser"));
    setLayout(new BorderLayout());
    add(list_sp, BorderLayout.CENTER);
  } // initComponents()

  //
  // Methods
  //
  public void clearContent() {
    list.clearSelection();
    list.setListData(new Object[0]);
    concept_1 = null;
    concept_2 = null;
    w = null;
  }

  public void setWrkList(WrkList w) {
    this.w = w;
    list.setListData(w.getData());
    list.ensureIndexIsVisible(0); // scroll list to the top
    single_concept = false;
  }

  public void setConcept(Concept concept) {
    list.setListData(new Object[] {new Integer(concept.getIdentifier().intValue()),
                     new Integer(0)});
    single_concept = true;
    concept_1 = concept;
    concept_2 = null;
    list.setSelectedIndex(0);
  } // setConcept()

  // --------------------------
  // Interface implementation
  // --------------------------
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting() || list.isSelectionEmpty()) {
      return;
    }

    frame.getGlassPane().setVisible(true);

    try {
      // clear old content
      concept_2_panel.clearContent();
      concept_1_panel.clearContent();
      nlm_rels_panel.clearContent();
      other_rels_panel.clearContent();
      frame.getRel_Panel().clearContent();
      frame.getRela_Panel().clearContent();
      rel_label.clearContent();

//      int concept_id_1 = ( (Integer) list.getModel().getElementAt(list.
//          getMinSelectionIndex())).intValue();
//      int concept_id_2 = ( (Integer) list.getModel().getElementAt(list.
//          getMaxSelectionIndex())).intValue();

      if (!single_concept) {
        Concept[] concepts = w.getConcepts(list.getSelectedIndex());
        concept_1 = concepts[0];
        concept_2 = concepts[1];
      }

      st.setConceptId_1(concept_1.getIdentifier().intValue());
      if (concept_2 == null) {
        st.setConceptId_2(0);
      }
      else {
        st.setConceptId_2(concept_2.getIdentifier().intValue());
      }

//      ThreadGroup tg = new ThreadGroup("Reports");
//      new ReportSubTask(concept_1.getIdentifier().intValue(), tg, "1").start();
      concept_1_panel.setContent(concept_1);

      if (!single_concept) {
//        new ReportSubTask(concept_2.getIdentifier().intValue(), tg, "2").start();
        concept_2_panel.setContent(concept_2);
      }

      Relationship[] nlm_rels = null;
      boolean load_rels = true;

      if (JekyllKit.getCoreDataClient().getRelationshipCount(concept_1) >
          1000) {
        if (JOptionPane.showConfirmDialog(this,
                                          "There are more than 1000 relationships" +
                                          "\nfor this concept. Continue?",
                                          "Load relationship(s)?",
                                          JOptionPane.YES_NO_OPTION) ==
            JOptionPane.NO_OPTION) {
          load_rels = false;
        }
      }

      if (load_rels) {
        JekyllKit.getCoreDataClient().populateRelationships(concept_1);
        nlm_rels = WrkList.getNLM_Rels(concept_1, null);
        nlm_rels_panel.setContent(nlm_rels);
//        other_rels_panel.setContent(other_rels);
      }

      // -- not applicable for "NLM03" project
//      frame.getRela_Panel().setContent(RelSemantics.getAttributesRestrBySTYs(concept_1,
//          concept_2));

      rel_label.setConcept1_PrefName(concept_1.getPreferredAtom().getString());
      if (!single_concept && (concept_2 != null)) {
        rel_label.setConcept2_PrefName(concept_2.getPreferredAtom().getString());
      }
    }
    catch (Exception ex) {
      ex.printStackTrace(JekyllKit.getLogWriter());
      MEMEToolkit.notifyUser("There was an error loading concept(s)."
                             + "\nLog file may contain more information.");
    }
    finally {
      frame.getGlassPane().setVisible(false);
    }
  } // valueChanged()

  public void refresh(boolean isAfterMolAction) {
    int index0 = list.getMinSelectionIndex();

    if (isAfterMolAction && !single_concept) {
      w.setReviewedStatus(index0);
    }

    list.clearSelection();
    list.setSelectedIndex(index0);
  } // refresh()

  public void setSelectedCluster(int cluster_id) {
    int index = w.find(cluster_id);

    if (index == -1) {
      MEMEToolkit.notifyUser(frame,
                             "Cluster id " + cluster_id +
                             " was not found.");
    }
    else {
      list.clearSelection();
      list.setSelectedIndex(index);
      list.ensureIndexIsVisible(index);
    }
  } // setSelectedCluster()

  public void nextPair() {
    int index0 = list.getMinSelectionIndex();

    if (index0 == (list.getModel().getSize() - 2)) {
      MEMEToolkit.notifyUser(frame, "The end of list is reached.");
    }
    else {
      list.clearSelection();
      list.setSelectedIndex(index0 + 2);
    }
  } // nextPair()

  // --------------------------------
  // Inner Classes
  // --------------------------------
  /**
   * Customized selection model, selection is made in pairs.
   */
  class WrkListSelectionModel
      extends DefaultListSelectionModel {

    public void setSelectionInterval(int index0, int index1) {
      // If even index, select the following index as well
      if (index0 % 2 == 0) {
        index1 = index0 + 1;
        super.setSelectionInterval(index0, index1);
      }
      else {
        index1 = index0 - 1;
        super.setSelectionInterval(index0, index1);
      }
    }

  } // WrkListSelectionModel

  class WrkListCellRenderer
      extends JLabel
      implements ListCellRenderer {
    public WrkListCellRenderer() {
      setOpaque(true);
    }

    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {

      setText(value.toString());

      if ( (!single_concept) && w.isBkgGray(index)) {
        setBackground(isSelected ? list.getSelectionBackground() :
                      Color.lightGray);
      }
      else {
        setBackground(isSelected ? list.getSelectionBackground() :
                      list.getBackground());
      }

      if ( (!single_concept) && w.isReviewed(index)) {
        setForeground(list.getForeground());
      }
      else {
        setForeground(Color.red);
      }

      setFont(list.getFont());
      return this;
    }

  } // WrkListCellRenderer

//  class ReportSubTask
//      extends Thread {
//    private int concept_id = 0;
//
//    public ReportSubTask(int concept_id, ThreadGroup tg, String name) {
//      super(tg, name);
//      this.concept_id = concept_id;
//    }
//
//    public void run() {
//      if (!frame.getGlassPane().isVisible()) {
//        frame.getGlassPane().setVisible(true);
//      }
//
//      if (Integer.parseInt(getName()) == 1) {
//        concept_2_panel.setContent(concept_id);
//      }
//      else {
//        concept_1_panel.setContent(concept_id);
//      }
//
//      // if it's the last thread in the group, set the cursor back to
//      // default
//      if (activeCount() == 1) {
//        frame.getGlassPane().setVisible(false);
//      }
//    }
//  }
}
