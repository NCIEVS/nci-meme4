/*****************************************************************************
 *
 * Object:  PrintWorklistAction.java
 * Author:  Brian Carlsen
 *
 * Notes: we should use actions/have menus and support saving to rpt file.
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.util.HTMLDocumentRenderer;
import gov.nih.nlm.util.SystemToolkit;

import java.util.StringTokenizer;

import java.awt.Event;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;

/**
 * Used for the "Print" button on the {@link TestReportFrame}.  It
 * makes use of the {@link gov.nih.nlm.util.HTMLDocumentRenderer}
 * to print a concept report.  The code could be easily adapted to
 * print other HTML docs.
 *
 * @author  Brian Carlsen (bcarlsen@apelon.com)
 */
public class PrintConceptReportAction extends AbstractAction {


  private static final long serialVersionUID = 1L;

  //
  // private fields
  //
  private JEditorPane concept_report;
  private TestReportFrame tr_frame;
  private String title = null;

  //
  // Constructors
  //

  /**
   * Constructor.
   * @param tr_frame An object {@link TestReportFrame}.
   */
  public PrintConceptReportAction(TestReportFrame tr_frame) {
    super();

    putValue(Action.NAME, "Print");
    putValue(Action.MNEMONIC_KEY,
             new Integer( (int) 'p'));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke('P', Event.CTRL_MASK));
    putValue(Action.SHORT_DESCRIPTION,
             "Pretty print the concept report");

    this.tr_frame = tr_frame;
    concept_report = new JEditorPane();
    concept_report.setContentType("text/html");
    concept_report.setEditable(false);
  }

  /**
   * Sets the title.
   * @param title the title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Action performed.
   * @param event An object {@link ActionEvent}
   */
  public void actionPerformed(ActionEvent event) {
    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          tr_frame.enableGlassPane();
          HTMLDocumentRenderer hdr = new HTMLDocumentRenderer();
          hdr.setTwoPagesPerSheet();
          //hdr.setOnePagePerSheet();
          hdr.setTitle(title);
          JEditorPane editor = new JEditorPane();
          editor.setContentType("text/html");
          editor.setText(
              "<html><head><style> a {text-decoration: none; }</style></head><body>" +
              SystemToolkit.removeLinks(tr_frame.getText()) + "</body></html>");
          //HTMLDocument doc = (HTMLDocument)editor.getDocument();
          hdr.print(editor);
          tr_frame.disableGlassPane();
        } catch (Exception e) {
          tr_frame.disableGlassPane();
          MEMEToolkit.handleError(e);
        }
        ;
      }
    });
    t.start();
  }

  /**
   * Splits a string on word boundaries so that each element of the string
   * array returned represents a "line" of the original string that is no
   * longer than line_length in length.  This is a very useful method to break
   * up a long string into 80 character chunks for displaying in a terminal.
   * @param to_split An object {@link String} representation of strings to
   * split.
   * @param line_length An <code>int</code> representation of maximum line
   * length.
   * @return An array of object {@link String} representation of a line of
   * splitted strings.
   */
  public static String[] splitString(String to_split, int line_length) {

    int max_length = 80;
    if (line_length > 0) {
      max_length = line_length;

    }
    String word = null;
    StringTokenizer st = new StringTokenizer(to_split, " ");
    String[] result = new String[1000];

    int first_word = 1;
    int char_count = 0;

    int i = 0;
    while (st.hasMoreTokens()) {
      word = st.nextToken();

      if (char_count + ( (first_word > 0) ? 0 : 1) + word.length() < max_length) {
        if (result[i] == null) {
          result[i] = "";
        }
        result[i] = result[i] + ( (first_word > 0) ? "" : " ") + word;
        char_count += ( (first_word > 0) ? 0 : 1) + word.length();
      } else {
        if (first_word == 0) {
          i++;
        }
        result[i] = word;
        char_count = word.length();
      }
      if (first_word > 0) {
        first_word = 0;
      }
    }
    return result;
  }

}
