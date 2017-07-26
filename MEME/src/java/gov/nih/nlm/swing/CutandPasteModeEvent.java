/**************************************************************************
 *
 * Package:    gov.nih.nlm.swing;
 * Interface:  CutandPasteModeEvent.java
 *
 *************************************************************************/
package gov.nih.nlm.swing;

import java.util.EventObject;

/**
 * Represents a cut or paste event fired from the {@link CutandPasteDNDTable}.
 * It is simply a container for the table that generated the event.
 *
 * @author Brian Carsen
 */
public class CutandPasteModeEvent extends EventObject {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link CutandPasteModeEvent} from the specified source.
   * @param source the {@link CutandPasteDNDTable} that fired the event
   */
  public CutandPasteModeEvent(Object source) {
    super(source);
  };

}