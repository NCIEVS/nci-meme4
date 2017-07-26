/**
 * ButtonPane.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.client.MEMERelaEditorKit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.exception.MissingDataException;

import gov.nih.nlm.umls.jekyll.JekyllKit;

/**
 * Panel for buttons.
 */
public class ButtonPane
    extends JPanel
    implements ActionListener {

  //
  // Private Fields
  //
  private String CMD_INSERT = "insert";
  private String CMD_UPDATE = "update";
  private String CMD_APPROVE_NEXT = "approve.next";
  private String CMD_APPROVE = "approve";
  private String CMD_NOT_APPROVE_NEXT = "not.approve.next";
  private String CMD_DELETE = "delete";
  private String CMD_LOAD = "load";

  private JTextField field = null;
  private RelaEditor frame = null;

  //
  // Constructors
  //
  public ButtonPane(RelaEditor frame) {
    this.frame = frame;
    initComponents();
  }

  //
  // Methods
  //
  private void initComponents() {

    Font f = new Font("SansSerif", Font.PLAIN, 12);

    // "Concept Id 2" text field for NLM03 project
    field = new JTextField(10);
    field.setMinimumSize(field.getPreferredSize());
    field.setMaximumSize(field.getPreferredSize());
    field.setActionCommand(CMD_LOAD);
    field.addActionListener(this);

    // label for the "Concept Id 2" text field
    JLabel fieldLabel = new JLabel();
    fieldLabel.setText("Concept 2 Id:");
    fieldLabel.setLabelFor(field);
    fieldLabel.setFont(f);

    // the LOAD button
    JButton loadButton = new JButton("load");
    loadButton.setMnemonic('l');
    loadButton.setFont(f);
    loadButton.setActionCommand(CMD_LOAD);
    loadButton.addActionListener(this);

    // an INSERT button
    JButton ins_Btn = new JButton();
    ins_Btn.setText("Insert");
    ins_Btn.setMnemonic('I');
    ins_Btn.setFont(f);
    ins_Btn.setActionCommand(CMD_INSERT);
    ins_Btn.addActionListener(this);

    // an UPDATE button
    JButton upd_Btn = new JButton();
    upd_Btn.setText("Update");
    upd_Btn.setMnemonic('U');
    upd_Btn.setFont(f);
    upd_Btn.setActionCommand(CMD_UPDATE);
    upd_Btn.addActionListener(this);
    upd_Btn.setEnabled(false);

    // a APPROVE/NEXT button
    JButton app_next_Btn = new JButton();
    app_next_Btn.setText("Approve -> Next");
    app_next_Btn.setMnemonic('p');
    app_next_Btn.setFont(f);
    app_next_Btn.setActionCommand(CMD_APPROVE_NEXT);
    app_next_Btn.addActionListener(this);
    app_next_Btn.setEnabled(false);

    // APPROVE button
    JButton app_Btn = new JButton();
    app_Btn.setText("Approve");
    app_Btn.setMnemonic('A');
    app_Btn.setFont(f);
    app_Btn.setActionCommand(CMD_APPROVE);
    app_Btn.addActionListener(this);
    app_Btn.setEnabled(false);

    // a NOT APPROVE/NEXT button
    JButton not_app_next_Btn = new JButton();
    not_app_next_Btn.setText("Not Approve -> Next");
    not_app_next_Btn.setMnemonic('N');
    not_app_next_Btn.setFont(f);
    not_app_next_Btn.setActionCommand(CMD_NOT_APPROVE_NEXT);
    not_app_next_Btn.addActionListener(this);
    not_app_next_Btn.setEnabled(false);

    // a DELETE button
    JButton del_Btn = new JButton();
    del_Btn.setText("Delete");
    del_Btn.setMnemonic('D');
    del_Btn.setFont(f);
    del_Btn.setForeground(Color.red);
    del_Btn.setActionCommand(CMD_DELETE);
    del_Btn.addActionListener(this);

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    add(ins_Btn);
    add(Box.createHorizontalStrut(5));
    add(upd_Btn);
    add(Box.createHorizontalStrut(5));
    add(app_next_Btn);
    add(Box.createHorizontalStrut(5));
    add(app_Btn);
    add(Box.createHorizontalStrut(5));
    add(not_app_next_Btn);
    add(Box.createHorizontalStrut(5));
    add(del_Btn);
    add(Box.createHorizontalGlue());
    add(fieldLabel);
    add(Box.createHorizontalStrut(5));
    add(field);
    add(Box.createHorizontalStrut(5));
    add(loadButton);

  } // initComponents()

  // --------------------------------
  // Interface implementation
  // --------------------------------
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    RelLabel rel_label = frame.getRel_Label();
    WrkListPane wrklist_panel = frame.getWrkList_Panel();

    frame.getGlassPane().setVisible(true);

    try {
      // =======================================================
      // INSERT and UPDATE
      // =======================================================
      if (cmd.equals(CMD_INSERT) || cmd.equals(CMD_UPDATE)) {
        String rel_name = (String) frame.getRel_Panel().getSelectedValue();
        String rel_attr = (String) frame.getRela_Panel().getSelectedValue();

        if (rel_name == null) {
          MEMEToolkit.notifyUser(frame, "Name for relationship is not set." +
                                 "\nPlease select appropriate value and try again.");
          return;
        }

        if (rel_attr != null && rel_name != null) {
          String default_name = RelSemantics.getCorrespondingRel(rel_attr);
          if (default_name != null) {
            if (!rel_name.equals(default_name)) {
              MEMEToolkit.notifyUser(frame, "Invalid rel/rela pair." +
                                     "\nPlease check the values and try again.");
              return;
            }
          }
        }

        if (frame.getSessionToken().getConceptId_2() == 0) {
          MEMEToolkit.notifyUser(frame, "There's no concept to link to.");
          return;
        }

        if (showDialog(rel_label.getLabel(), cmd) == JOptionPane.YES_OPTION) {
          useEditorKit(cmd);
          wrklist_panel.refresh(true);
          MEMEToolkit.notifyUser(frame,
                                 "Relationship was successfully " + cmd + "ed.");
        }
      }
      // =======================================================
      // APPROVE and APPROVE_NEXT
      // =======================================================
      else if (cmd.equals(CMD_APPROVE) || cmd.equals(CMD_APPROVE_NEXT)) {
        if (showDialog(rel_label.getLabel(), "approve") ==
            JOptionPane.YES_OPTION) {
          useEditorKit(cmd);
          if (cmd.equals(CMD_APPROVE)) {
            wrklist_panel.refresh(true);
          }
          else {
            wrklist_panel.nextPair();
          }
        }
      }
      // =======================================================
      // NOT_APPROVE_NEXT
      // =======================================================
      else if (cmd.equals(CMD_NOT_APPROVE_NEXT)) {
        wrklist_panel.nextPair();
      }
      // =======================================================
      // DELETE
      // =======================================================
      else if (cmd.equals(CMD_DELETE)) {
        if (showDialog(rel_label.getLabel(), cmd) == JOptionPane.YES_OPTION) {
          useEditorKit(cmd);
          wrklist_panel.refresh(true);
          MEMEToolkit.notifyUser(frame,
                                 "Relationship was successfully " + cmd + "d.");
        }
      }
      // =======================================================
      // LOAD
      // =======================================================
      else if (cmd.equals(CMD_LOAD)) {
        String concept_id = field.getText().trim();

        if (concept_id.equals("")) {
          return;
        }

        Concept concept_2 = JekyllKit.getCoreDataClient().getConcept(Integer.
            parseInt(concept_id));
        frame.getSessionToken().setConceptId_2(concept_2.getIdentifier().
                                               intValue());
        frame.getConcept_2_Panel().setContent(concept_2);
        frame.getRel_Label().setConcept2_PrefName(concept_2.getPreferredAtom().
                                                  getString());
        frame.getRel_Panel().clearContent();
        frame.getRela_Panel().clearContent();
        frame.getNLM_Rels_Panel().clearSelection();
      }
    }
    catch (MissingDataException ex) {
      MEMEToolkit.notifyUser(frame, "Concept was not found.");
    }
    catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(frame, "Invalid Concept Id",
                                    "Invalid integer",
                                    JOptionPane.ERROR_MESSAGE);
    }
    catch (Exception ex) {
      ex.printStackTrace(JekyllKit.getLogWriter());
      MEMEToolkit.notifyUser(frame,
                             "There was an error in performing specified action."
                             + "\nLog file may contain more information.");
    }
    finally {
      frame.getGlassPane().setVisible(false);
    }
  } // actionPerformed()

  private int showDialog(Object message, String title) {
    Object[] options = {
        "Yes", "No"};
    return JOptionPane.showOptionDialog(null,
                                        message,
                                        title,
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        options,
                                        options[1]);
  } // showDialog()

  private void useEditorKit(String action) throws Exception {
    MEMERelaEditorKit editor_kit = null;
    int rel_id = 0;

    SessionToken st = frame.getSessionToken();

    try {
      // Instantiating the editor kit
      editor_kit = new MEMERelaEditorKit(JekyllKit.getDataSource());

      if (action.equals(CMD_INSERT)) {

        rel_id = editor_kit.insertRelationshipBetweenConcepts(st.
            getConceptId_2(),
            st.getConceptId_1(),
            st.getNew_Rel(),
            st.getNew_Attr(),
            st.getSource(),
            st.getSource_Of_Label(),
            st.getAuthority());

        // We need to run this on as well.
        // The method above makes a relationship's tbr = 'n'
        editor_kit.changeRelationshipTBR(rel_id,
                                         st.getAuthority(),
                                         String.valueOf(CoreData.FV_RELEASABLE));
      }
      else if (action.equals(CMD_UPDATE)) {

        rel_id = editor_kit.processRelationshipBetweenConcepts(st.
            getConceptId_1(),
            st.getConceptId_2(),
            st.getNew_Rel(),
            st.getNew_Attr(),
            st.getSource(),
            st.getSource_Of_Label(),
            st.getAuthority(),
            st.getRelationship_Id());

        editor_kit.changeRelationshipTBR(rel_id,
                                         st.getAuthority(),
                                         String.valueOf(CoreData.
            FV_WEAKLY_UNRELEASABLE));
      }
      else if (action.equals(CMD_APPROVE) || action.equals(CMD_APPROVE_NEXT)) {

        rel_id = editor_kit.processRelationship(st.getCUI_1(),
                                                st.getCUI_2(),
                                                st.getCurrent_Rel(),
                                                st.getCurrent_Attr(),
                                                st.getSource(),
                                                st.getSource_Of_Label(),
                                                st.getAuthority(),
                                                st.getRelationship_Id());

        editor_kit.changeRelationshipTBR(rel_id,
                                         st.getAuthority(),
                                         String.valueOf(CoreData.
            FV_WEAKLY_UNRELEASABLE));
      }
      else if (action.equals(CMD_DELETE)) {
        editor_kit.changeRelationshipTBR(st.getRelationship_Id(),
                                         st.getAuthority(),
                                         String.valueOf(CoreData.
            FV_UNRELEASABLE));
      }
    }
    catch (Exception ex) {
      throw ex;
    }
    finally {
      // dereferencing editor_kit object
      editor_kit = null;
    }
  } // useEditorKit()

}
