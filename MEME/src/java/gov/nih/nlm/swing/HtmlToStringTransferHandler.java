/*****************************************************************************
 *
 * Package: gov.nih.nlm.swing
 * Object:  HtmlToStringTransferHandler
 *
 * Author:  BAC, RBE
 *
 * History:
 *   10/28/2003: 1st Version.
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

/**
 * {@link TransferHandler} for managing HTML content.  To be used
 * for cut-and paste from something like an HTML {@link JEditorPane}
 * to an outside text-based application.
 *
 * @author MEME Group
 */
public class HtmlToStringTransferHandler extends TransferHandler {

  //
  // Methods
  //

  /**
   * Indicates whether or not data can be imported.  This class only
   * supports exporting of data.
   * @param c the {@link JComponent}
   * @param flavors the {@link DataFlavor}
   * @return <code>false</code>
   */
  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    return false;
  }

  /**
   * Returns the actions allowed by this handler.  This class only
   * supports <code>COPY</code>
   * @param c the {@link JComponent}
   * @return {@link TransferHandler}.<code>COPY</code>
   */
  public int getSourceActions(JComponent c) {
    return COPY;
  }

  /**
   * Creates data for clip board.
   * @param c the {@link JComponent}
   * @return the {@link StringSelection} containing the text from
   * the component stripped of HTML tags
   */
  protected Transferable createTransferable(JComponent c) {

    //
    // If it is a text component, and content-type is text/html,
    // strip out all of the tags
    //
    if (c instanceof  JTextComponent) {
      JTextComponent tc = (JTextComponent)c;
      int start = tc.getSelectionStart();
      int end = tc.getSelectionEnd();
      JEditorPane jep = new JEditorPane();
      jep.setContentType("text/html");
      jep.setText(tc.getText().replaceAll("<br>","</p><p>").replaceAll("\u00A0"," "));
      jep.setSelectionStart(start);
      jep.setSelectionEnd(end);
      return new StringSelection(jep.getSelectedText());
    }

    //
    // Otherwise return empty string. (i.e. only use this with JTextComponents)
    //
    return new StringSelection("");
  }

}
