/**************************************************************************
 *
 * Package:    gov.nih.nlm.swing;
 * Interface:  DataChangeEvent.java
 *
 *************************************************************************/
package gov.nih.nlm.swing;

import java.util.EventObject;

/**
 * Generically represents an event that indicates a change
 * in the underlying data structure of some component.
 *
 * @author Brian Carsen
 */
public class DataChangeEvent extends EventObject {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DataChangeEvent} specified source object.
   * @param source the source of the event
   */
  public DataChangeEvent(Object source) {
    super(source);
  };

}