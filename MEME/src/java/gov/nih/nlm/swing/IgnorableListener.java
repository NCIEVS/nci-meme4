/**************************************************************************
 *
 * Package:    gov.nih.nlm.swing;
 * Interface:  IgnorableListener.java
 *
 *************************************************************************/
package gov.nih.nlm.swing;

/**
 * Generically represents a mechanism for disabling listener functionality
 * while certain kinds of processing are taking place.
 * For example, if you want to programatically select rows of
 * a table, you likely want to ignore the <code>valueChanged</code>
 * events that will be generated as those rows are selected.
 *
 * @author Brian Carlsen
 */
public interface IgnorableListener {

  /**
   * Indicates whether or not events are to be ignored.
   * @return <code>true</code> if events should be ignored,
   * 			 	<code>false</code> otherwise
   */
  public boolean ignoreEvents();

  /**
   * Set the flag indicating whether or not events should be ignored.
   * @param b <code>true</code> if they should, <codE>false</code> otherwise
   */
  public void setIgnoreEvents(boolean b);
}
