/*****************************************************************************
 *
 * Object:  DecreaseFontAction
 * Author:  Brian Carlsen
 *
 * Changes
 *   12/22/2005 BAC (1-718MI): Use ^- accelerator
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
public class DecreaseFontAction extends AbstractAction {

  /**
   * Instantiates a {@link DecreaseFontAction}.
   */
  public DecreaseFontAction() {
    super();
    // configure action
    putValue(Action.NAME, "Decrease Font");
    putValue(Action.SHORT_DESCRIPTION, "Decrease Font Size");
    putValue(Action.MNEMONIC_KEY,
             new Integer( (int) 'd'));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke('[', Event.CTRL_MASK));
  }

  /**
   * Decrease the font.
   * @param e the {@link ActionEvent}
   */
  public void actionPerformed(ActionEvent e) {
    FontSizeManager.decreaseFontSize();
  }

}
