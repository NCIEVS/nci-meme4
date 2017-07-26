/**************************************************************************
 *
 * Package:    gov.nih.nlm.swing;
 * Interface:  CutandPasteModeListener.java
 *
 *************************************************************************/
package gov.nih.nlm.swing;

import java.util.EventListener;

/**
 * Generically represents a listener for cut and paste events fired
 * from a {@link CutandPasteDNDTable}.  There are four possibilities
 * as the methods communicate both the current mode of the table
 * and the current action.
 *
 * @author Brian Carsen
 */
public interface CutandPasteModeListener extends EventListener {

  /**
   * Informs the listener that a cut was made while in the mode
   * where a cut was expected.
   * @param e the {@link CutandPasteModeEvent}
   */
  public void cutDoneinCutMode(CutandPasteModeEvent e);

  /**
   * Informs the listener that a cut was made while in the mode
   * where a paste was expected.  Typically, this should result
   * in an exception.
   * @param e the {@link CutandPasteModeEvent}
   */
  public void cutDoneinPasteMode(CutandPasteModeEvent e);

  /**
   * Informs the listener that a paste was made while in the mode
   * where a paste was expected.
   * @param e the {@link CutandPasteModeEvent}
   */
  public void pasteDoneinPasteMode(CutandPasteModeEvent e);

  /**
   * Informs the listener that a paste was made while in the mode
   * where a cut was expected. Typically, this should result in
   * an exception.
   * @param e the {@link CutandPasteModeEvent}
   */
  public void pasteDoneinCutMode(CutandPasteModeEvent e);

}