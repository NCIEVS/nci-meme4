/*****************************************************************************
 *
 * Package:    gov.nih.nlm.swing
 * Object:     HtmlViewer.java
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import java.net.URL;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Used to display an HTML documents.
 *
 * @author Brian Carlsen, Owen J. Carlsen, Yun-Jung Kim
 */
public class HtmlViewer extends JPanel {

  //
  // Fields
  //
  private JEditorPane editor_pane = null;

  /**
   * Instantiates an {@link HtmlViewer}.
   */
  public HtmlViewer() {
    super();
    initialize();
  }

  /**
   * Initialize the viewer.
   */
  private void initialize() {
    setName("HtmlViewer");
    editor_pane = new JEditorPane();
    editor_pane.setEditable(false);
    this.setLayout(new BorderLayout());
    JScrollPane agreement_scroll_pane = new JScrollPane(editor_pane,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    agreement_scroll_pane.validate();

    this.add(agreement_scroll_pane, BorderLayout.CENTER);
    editor_pane.setPreferredSize(new Dimension(700, 500));
  }

  /**
   * Sets the {@link URL} to display.
   * @param url the {@link URL} to display
   */
  public void setPage(URL url) {
    try {
      editor_pane.setEditorKit(new HTMLEditorKit());
      //editor_pane.getEditorKitForContentType("text/html"));
      editor_pane.setPage(url);
      editor_pane.setEditable(false);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Sets the text to display.
   * @param text the text to display
   */
  public void setText(String text) {
    editor_pane.setEditorKit(
        editor_pane.getEditorKitForContentType("text/html"));
    editor_pane.setEditable(false);
    editor_pane.setText(text);
  }

  /**
   * Returns the {@link JEditorPane} which holds HTML.
   * @return {@link JEditorPane}
   */
  public JEditorPane getEditorPane() {
    return editor_pane;
  }

}
