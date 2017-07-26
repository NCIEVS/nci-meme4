/**
 * RelaEditor.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import gov.nih.nlm.meme.client.ReportsClient;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.umls.jekyll.JekyllKit;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Main class for Rela Editor tool.
 */
public class RelaEditor
    extends JFrame {

  private String current_workfile_name = null;
  private WrkList current_workfile = null;
  private ReportsClient reports_client = null;

  //
  // Components
  //
  private GlassComponent glass_comp = null;
  private WrkListPane wrklist_panel = null;
  private ConceptPane concept_1_panel = null; // Left-positioned panel
  private ConceptPane concept_2_panel = null; // Right-positioned panel
  private RelationshipsPane nlm_rels_panel = null;
  private RelationshipsPane other_rels_panel = null;
  private RelPane rel_panel = null;
  private RelPane rela_panel = null;
  private ButtonPane buttons = null;
  private RelLabel rel_label = null;
  private Menu menu = null;
  private SessionToken st = null;

  // Constructor
  public RelaEditor() {
    initComponents();
    initValues();
    setTitleBar();
    
    // sizes the frame so that all its contents
    // are at or above their preferred sizes
    pack();
  }

  private void initComponents() {
    concept_1_panel = new ConceptPane(this, "Concept 1");
    concept_2_panel = new ConceptPane(this, "Concept 2");
    nlm_rels_panel = new RelationshipsPane(this, "NLM% Rels");
    other_rels_panel = new RelationshipsPane(this, "Other Rels");
    rel_panel = new RelPane(this, "REL");
    rela_panel = new RelPane(this, "RELA");
    buttons = new ButtonPane(this);
    rel_label = new RelLabel();
    menu = new Menu(this);
    st = new SessionToken();
    wrklist_panel = new WrkListPane(this);

    // this frame
    this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

    setJMenuBar(menu);

    glass_comp = new GlassComponent(this);
    setGlassPane(glass_comp);

    // lay out components
    this.setContentPane(doGBLayout());
  } // initComponents

  private void initValues() {
    RelSemantics.setCurrentSAB_SL(System.getProperty("default.rela.source"));
    getSessionToken().setAuthority(JekyllKit.getAuthority().toString());
    getSessionToken().setSABandSL(RelSemantics.getCurrentSAB_SL());
    rel_panel.setContent(RelSemantics.getRelationshipNames());
    rela_panel.setContent(RelSemantics.getRelationshipAttributes());
  } // initValues()

  private JPanel doGBLayout() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // How much to add to the minimum size of the components
    c.ipadx = 0;
    c.ipady = 0;

    // The minimum amount of space between the component
    // and the edges of its display area.
    // Insets(top, left, bottom, right)
    c.insets = new Insets(1, 1, 1, 1);

    // constraints for wrklist_panel
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 8;
    c.fill = GridBagConstraints.BOTH; // VERTICAL
    c.anchor = GridBagConstraints.CENTER; // PAGE_START
    c.weightx = 0;
    c.weighty = 100; // ok

    panel.add(wrklist_panel, c);

    // constraints for rel_label
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 8;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH; // FIRST_LINE_START
    c.weightx = 100; // ok
    c.weighty = 0;

    panel.add(rel_label, c);

    // TODO: Make panel names more intuitive

    // constraints for concept_1_panel
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 3;
    c.gridheight = 6;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER; // PAGE_START
    // not necessary because specified above ?
    c.weightx = 100;
    c.weighty = 100;

    panel.add(concept_1_panel, c);

    // constraints for concept_2_panel
    c.gridx = 6;
    c.gridy = 1;
    c.gridwidth = 3;
    c.gridheight = 6;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER; // PAGE_START
    // not necessary because specified above ?
    c.weightx = 100;
    c.weighty = 100;

    panel.add(concept_2_panel, c);

    // constraints for rel_panel
    c.gridx = 4;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.fill = GridBagConstraints.NONE; // NONE <was BOTH>
    c.anchor = GridBagConstraints.NORTH; // PAGE_START
    c.weightx = 0;
    // not necessary because specified above ?
    c.weighty = 100;

    panel.add(rel_panel, c);

    // constraints for rela_panel
    c.gridx = 5;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.fill = GridBagConstraints.NONE; // NONE <was BOTH>
    c.anchor = GridBagConstraints.NORTH; // PAGE_START
    c.weightx = 0;
    // not necessary because specified above ?
    c.weighty = 100;

    panel.add(rela_panel, c);

    // constraints for nlm_rels_panel
    c.gridx = 4;
    c.gridy = 3;
    c.gridwidth = 2;
    c.gridheight = 2;
    c.fill = GridBagConstraints.BOTH; // NONE
    c.anchor = GridBagConstraints.CENTER; // CENTER <was NORTH>
    c.weightx = 0;
    c.weighty = 0; // do we need this?

    panel.add(nlm_rels_panel, c);

    // Do we need this panel?
    // constraints for other_rels_panel
    c.gridx = 4;
    c.gridy = 5;
    c.gridwidth = 2;
    c.gridheight = 2;
    c.fill = GridBagConstraints.NONE; // <was BOTH>
    c.anchor = GridBagConstraints.NORTH;
    c.weightx = 0;
    c.weighty = 100;

    panel.add(other_rels_panel, c);

    // constraints for buttons
    c.gridx = 1;
    c.gridy = 7;
    c.gridwidth = 8;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.CENTER; // PAGE_END
    c.weightx = 100; // do we need this?
    c.weighty = 0;
    c.insets = new Insets(12, 1, 12, 1);

    panel.add(buttons, c);

    // Though it's opaque by default,
    // we set it anyway just to be safe.
    panel.setOpaque(true);

    return panel;
  } // doGBLayout()

  //
  // Property Accessor Methods
  //
  ConceptPane getConcept_1_Panel() {
    return concept_1_panel;
  }

  ConceptPane getConcept_2_Panel() {
    return concept_2_panel;
  }

  RelLabel getRel_Label() {
    return rel_label;
  }

  RelationshipsPane getNLM_Rels_Panel() {
    return nlm_rels_panel;
  }

  RelationshipsPane getOther_Rels_Panel() {
    return other_rels_panel;
  }

  RelPane getRel_Panel() {
    return rel_panel;
  }

  RelPane getRela_Panel() {
    return rela_panel;
  }

  SessionToken getSessionToken() {
    return st;
  }

  WrkListPane getWrkList_Panel() {
    return wrklist_panel;
  }

  public void setWorklist(String name) {
    current_workfile_name = name;

    this.getGlassPane().setVisible(true);

    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          current_workfile = new WrkList(current_workfile_name);
          if (current_workfile.isEmpty()) {
            return;
          }

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              getWrkList_Panel().setWrkList(current_workfile);
              setTitleBar();
            }
          });
        }
        catch (Exception ex) {
          ex.printStackTrace(JekyllKit.getLogWriter());
        }
        finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              RelaEditor.this.getGlassPane().setVisible(false);
            }
          });
        }
      }
    });

    t.start();
  } // setWorklist()

  public void setTitleBar() {
    StringBuffer sb = new StringBuffer(500);
    sb.append("REL/RELA Editor :: ");
    sb.append("Current db: ");
    sb.append(JekyllKit.getDataSource());
    sb.append(" | ");
    sb.append("Editor: ");
    sb.append(st.getAuthority());
    sb.append(" | ");
    sb.append("rela source: ");
    sb.append(RelSemantics.getCurrentSAB_SL());
    sb.append(" | ");
    sb.append("selected worklist: ");
    sb.append( (current_workfile_name == null) ? "" : current_workfile_name);
    setTitle(sb.toString());
  } // setTitleBar()

  public void updateSAB_SL(String source) {
      RelSemantics.setCurrentSAB_SL(source);
      getSessionToken().setSABandSL(RelSemantics.getCurrentSAB_SL());
      setTitleBar();
  }
  
  /**
   * Reports client for getting text-based concept reports.
   *
   * @throws Exception
   * @return ReportsClient
   */
  synchronized ReportsClient getReportsClient() throws Exception {
    if (reports_client == null) {
      reports_client = new ReportsClient(JekyllKit.getDataSource());
      reports_client.setTimeout(100000000);
      reports_client.setRelationshipViewMode(ReportsClient.ALL);
    }

    return reports_client;
  } // getReportsClient()

  public void updateReportsClient(String mid_service) {
    if (reports_client != null) {
      reports_client.setMidService(mid_service);
    }

    setTitleBar();
  }

  public void resetReportsClient() {
    reports_client = null;
  }

  public void clearContent() {
    concept_2_panel.clearContent();
    concept_1_panel.clearContent();
    nlm_rels_panel.clearContent();
    other_rels_panel.clearContent();
    rel_panel.clearContent();
    rela_panel.clearContent();
    rel_label.clearContent();
    wrklist_panel.clearContent();
  } // clearContent()

  // ----------------------------
  // Overriding Methods
  // ----------------------------

  public Dimension getPreferredSize() {
    Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
    return new Dimension(screen_size.width, screen_size.height / 2);
  }

  public void setVisible(boolean b) {
    super.setVisible(b);
    if (!b) {
      clearContent();
    }
  }

}
