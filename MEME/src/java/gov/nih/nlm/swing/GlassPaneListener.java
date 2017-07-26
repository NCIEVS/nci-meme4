/*****************************************************************************
 *
 * Package: gov.nih.nlm.swing
 * Object:  GlassPaneListener
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

/**
 * Generically represents a listener for enabling or disabling glass pane.
 *
 * @author BAC, RBE
 */
public interface GlassPaneListener {

  /**
   * Enable glass pane.
   */
  public void enableGlassPane();

  /**
   * Disable glass pane.
   */
  public void disableGlassPane();

}
