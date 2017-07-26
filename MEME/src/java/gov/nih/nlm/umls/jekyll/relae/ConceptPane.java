/**
 * ConceptPane.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.umls.jekyll.JekyllKit;
import gov.nih.nlm.umls.jekyll.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 * The class is a container for the <code>Concept</code>
 * information.
 */
public class ConceptPane
    extends JPanel
    implements ActionListener {

  //
  // Private Fields
  //
  JList header_list = null;
  JEditorPane report_area = null;
  JPopupMenu popup = null;
  private String STY_DEF = "sty.definition";
  private String PRINT = "print";

  ConceptSemanticType[] stys = null;
  private RelaEditor frame = null;

  // Constructor
  public ConceptPane(RelaEditor frame, String name) {
    this.frame = frame;
    initComponents(name);
  }

  //
  // Methods
  //
  private void initComponents(String border_title) {

    // header JList
    header_list = new JList();
    JScrollPane header_list_sp = new JScrollPane(header_list);

    // report area
    report_area = new JEditorPane();
    report_area.setEditable(false);
    JScrollPane report_area_sp = new JScrollPane(report_area);

    // split panel
    JSplitPane split_panel = new JSplitPane();
    split_panel.setOrientation(JSplitPane.VERTICAL_SPLIT);
    split_panel.setTopComponent(header_list_sp);
    split_panel.setBottomComponent(report_area_sp);
    split_panel.setOneTouchExpandable(true);
    split_panel.setDividerLocation(75);

    // popup menu
    // different L&Fs may choose whether to display label or not
    popup = new JPopupMenu("Concept Menu");

    // menu item for STY definition
    JMenuItem menuItem = new JMenuItem("STY definition");
    menuItem.setActionCommand(STY_DEF);
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("Print");
    menuItem.setActionCommand(PRINT);
    menuItem.addActionListener(this);
    popup.add(menuItem);

    // listener for popup menu
    MouseListener popupListener = new PopupListener();
    header_list.addMouseListener(popupListener);
    report_area.addMouseListener(popupListener);

    setBorder(SwingUtilities.buildTitledBorder(border_title));
    setLayout(new BorderLayout());
    add(split_panel, BorderLayout.CENTER);
  } // initComponents

  public void clearContent() {
    header_list.setListData(new Vector());
    report_area.setText(null);
  }

  public void setContent(Concept concept) {
    // clear old data
    clearContent();

    if (concept == null) {
      return;
    }

    try {
      Vector v = new Vector();
      StringBuffer sb = new StringBuffer(500);

      // constructing concept header
      // 1st line, concept_id + PN
      sb.append("CN#: ");
      sb.append(concept.getIdentifier().toString());
      sb.append("   ");
      String pn = concept.getPreferredAtom().getString();
      sb.append( (pn == null) ? "" : pn);
      v.add(sb.toString());

      // clear StringBuffer
      sb.delete(0, sb.length());

      // 2+ line(s), CUIs + status
      CUI[] cuis = concept.getCUIs();
      for (int i = 0; i < cuis.length; i++) {
        if (cuis[i] == null) {
          continue;
        }

        sb.append("CUI: ");
        sb.append(cuis[i].toString());
        sb.append("   ");
        sb.append(concept.getStatus());
        v.add(sb.toString());

        // clear StringBuffer
        sb.delete(0, sb.length());
      }

      // 3+ line(s), STYs
      stys = concept.getSemanticTypes();
      for (int i = 0; i < stys.length; i++) {
        sb.append("STY: ");
        sb.append(stys[i].getValue());
        v.add(sb.toString());

        // clear StringBuffer
        sb.delete(0, sb.length());
      }

      header_list.setListData(v);

      report_area.setText(frame.getReportsClient().getReport(concept.getIdentifier().intValue()));
      report_area.setCaretPosition(0); // scroll it to the top
    }
    catch (Exception ex) {
      if (ex instanceof MissingDataException) {
        MEMEToolkit.notifyUser("Concept was not found: " +
                               concept.getIdentifier().toString());
      }
      else {
        ex.printStackTrace(JekyllKit.getLogWriter());
        MEMEToolkit.notifyUser(frame, "Failed to get a report for concept: "
                               + concept.getIdentifier().toString()
                               + "\nLog file may contain more information.");
      }
    }
  } // setContent()

  // ----------------------------
  // Interface Implementation
  // ----------------------------
  public void actionPerformed(ActionEvent evt) {
    String cmd = evt.getActionCommand();

    if (cmd.equals(STY_DEF)) {

      if (header_list.isSelectionEmpty()) {
        MEMEToolkit.notifyUser("Please select one of the semantic types");
      }
      else {

        frame.getGlassPane().setVisible(true);
        try {
          int index = header_list.getSelectedIndex();

          InfoDialog dialog = new InfoDialog(frame,
                                             "STY Definition & Example",
                                             stys[index - 2]);
          dialog.pack();
          dialog.setVisible(true);
        }
        catch (Exception ex) {
          ex.printStackTrace(JekyllKit.getLogWriter());
          MEMEToolkit.notifyUser(frame,
                                 "Failed to retrieve definition for sty."
                                 + "\nLog file may contain more information.");
        }
        finally {
          frame.getGlassPane().setVisible(false);
        }
      }
    }
    else if (cmd.equals(PRINT)) {
      try {
        // Get the PrinterJob object that coordinates everything
        PrinterJob job = PrinterJob.getPrinterJob();

        // Get the default page format, then ask the user to customize it.
        PageFormat format = job.pageDialog(job.defaultPage());

        // Create our PageableText object, and tell the PrinterJob about it
        job.setPageable(new PageableText(report_area.getText(), format));

        // Ask the user to select a printer, etc., and if not canceled, print!
        if (job.printDialog()) {
          job.print();
        }
      }
      catch (java.io.IOException ex) {
        ex.printStackTrace(JekyllKit.getLogWriter());
        MEMEToolkit.notifyUser(frame, "There was an error printing a report."
                               +
                               "\nFailed to access a stream of text for printing");
      }
      catch (java.lang.NullPointerException ex) {
        ex.printStackTrace(JekyllKit.getLogWriter());
        MEMEToolkit.notifyUser(frame, "No text to print.");
      }
      catch (java.awt.print.PrinterException ex) {
        ex.printStackTrace(JekyllKit.getLogWriter());
        MEMEToolkit.notifyUser(frame, "Failed to print a concept report."
                               + "\nLog file may contain more information.");
      }
    }
  } // actionPerformed()

  // -----------------------------
  // Inner Classes
  // -----------------------------
  class PopupListener
      extends MouseAdapter {

    public void mousePressed(MouseEvent evt) {
      maybeShowPopup(evt);
    }

    public void mouseReleased(MouseEvent evt) {
      maybeShowPopup(evt);
    }

    private void maybeShowPopup(MouseEvent evt) {
      if (evt.isPopupTrigger() && (evt.getSource() instanceof JList)) {
        if (header_list.isSelectionEmpty()) {
          return;
        }
        else if (header_list.getSelectedValue().toString().startsWith("STY")) {
          if (! ( (JMenuItem) popup.getComponent(0)).isVisible()) {
            ( (JMenuItem) popup.getComponent(0)).setVisible(true);
          }
          popup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
      }
      else if (evt.isPopupTrigger()) {
        // we hide "STY definition" here
        ( (JMenuItem) popup.getComponent(0)).setVisible(false);
        popup.show(evt.getComponent(), evt.getX(), evt.getY());
      }
    }
  }
}
