/**
 * RelPane.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import gov.nih.nlm.umls.jekyll.swing.*;

/**
 * The class is a container for the list of either
 * relationship names or relatationship attributes.
 *
 */
public class RelPane
    extends JPanel
    implements ListSelectionListener {

  //
  // Private Fields
  //
  private DefaultListModel rels_model = null;
  private JList rels = null;
  private RelaEditor frame = null;
  private String current_value = null;

  // Constructor
  public RelPane(RelaEditor frame, String name) {

    this.frame = frame;

    rels_model = new DefaultListModel();

    rels = new JList();
    rels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    rels.setSelectionModel(new ToggleSelectionModel());
    rels.addListSelectionListener(this);
    rels.setCellRenderer(new RelCellRenderer());

    JScrollPane rels_sp = new JScrollPane(rels);

    Dimension d = new Dimension(100, 200);
    rels_sp.setPreferredSize(d);
    // we set minimum size to prevent horizontal resizing
    rels_sp.setMinimumSize(d);

    setName(name);
    setBorder(gov.nih.nlm.umls.jekyll.swing.SwingUtilities.buildTitledBorder(name));
    setLayout(new BorderLayout());
    add(rels_sp, BorderLayout.CENTER);
  }

  // -------------------------
  // Methods
  // -------------------------

  // 1. clear selection
  // 2. clear current value
  public void clearContent() {
    rels.clearSelection();
    setCurrentValue(null);
  }

  public void clearSelection() {
    rels.clearSelection();
  }

  public void setContent(String[] data) {
    clearSelection();

    if (data == null) {
      return;
    }

    for (int i = 0; i < data.length; i++) {
      rels_model.addElement(data[i]);
    }
    rels.setModel(rels_model);
  } // setContent()

  public void setCurrentValue(String value) {
    current_value = value;

    if (getName().equals("RELA") && (!rels_model.contains(value)) &&
        (value != null)) {
      rels_model.addElement(value);
    }

    rels.ensureIndexIsVisible(rels_model.indexOf(value));
    rels.clearSelection();
  } // setCurrentValue()

  public Object getSelectedValue() {
    return rels.getSelectedValue();
  }

  public void setSelectedValue(String name) {
    rels.setSelectedValue(name, false);
  }

  public void invertRelAttribute() {
    if (getName().equals("RELA")) {
      int selected_index = rels.getSelectedIndex();
      String selected_value = getSelectedValue().toString();
      rels_model.set(selected_index,
                     RelSemantics.getInvertedAttribute(selected_value));
      rels.clearSelection();
      rels.setSelectedIndex(selected_index);
    }
  } // invertRelAttribute()

  //
  // Interface Implementation
  //
  public void valueChanged(ListSelectionEvent e) {

    if (e.getValueIsAdjusting()) {
      return;
    }

    RelLabel rel_label = frame.getRel_Label();
    SessionToken st = frame.getSessionToken();

    String selected_value = (String) getSelectedValue();

    if (getName().equals("REL")) {
      st.setNew_Rel(selected_value);
      rel_label.setRelationship_Name(selected_value);
    }
    else if (getName().equals("RELA")) {
      st.setNew_Attr(selected_value);
      rel_label.setRelationship_Attr(selected_value);
      if (selected_value != null) {
        String default_rel_name = RelSemantics.getCorrespondingRel(
            selected_value);
        frame.getRel_Panel().setSelectedValue(default_rel_name);
        if (default_rel_name == null) {
          frame.getRel_Panel().clearSelection();
        }
      }
    }
  } // valueChanged()

  // ---------------------------
  // Inner Classes
  // ---------------------------
  class RelCellRenderer
      extends JLabel
      implements ListCellRenderer {
    public RelCellRenderer() {
      setOpaque(true);
    }

    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {

      setText(value.toString());

      if (value.toString().equals(current_value)) {
        setBackground(isSelected ? Color.red : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : Color.red);
      }
      else {
        setBackground(isSelected ? list.getSelectionBackground() :
                      list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() :
                      list.getForeground());
      }

      return this;
    }

  } // RelCellRenderer

}
