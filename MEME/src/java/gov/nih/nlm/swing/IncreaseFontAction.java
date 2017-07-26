/*****************************************************************************
 *
 * Object:  IncreaseFontAction
 * Author:  Brian Carlsen
 *
 * Changes
 *   12/22/2005 BAC (1-718MI): Use ^+ accelerator
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import java.awt.Event;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * GUI {@link Action} used to decrease the font in a window.  It is
 * intended to be used with an "Decrease Font" menu item.
 *
 * @author Brian Carlsen
 */
public class IncreaseFontAction extends AbstractAction {

  /**
   * Instantiates an {@link IncreaseFontAction}.
   */
  public IncreaseFontAction() {
    super();

    //
    // configure action
    //
    putValue(Action.NAME, "Increase Font");
    putValue(Action.SHORT_DESCRIPTION, "Increase Font Size");
    putValue(Action.MNEMONIC_KEY,
             new Integer( (int) 'i'));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(']', Event.CTRL_MASK));

  }

  /**
   * Increases the font.
   * @param e the <code>ActionEvent</code>
   */
  public void actionPerformed(ActionEvent e) {
    FontSizeManager.increaseFontSize();
  }

}
