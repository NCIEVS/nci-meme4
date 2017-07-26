/*****************************************************************************
 *
 * Package: gov.nih.nlm.swing
 * Object:  SwingToolkit
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import java.awt.Color;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * Contains constants used by {@link SwingToolkit}.
 *
 * @author  MEME Group
 */
public interface SwingConstants {

  //
  // Constants
  //

  /**
   * Fields in GUI applications which have popup menus
   * should use this border.
   */
  public final static Border HAS_POPUP_BORDER =
      new BevelBorder(BevelBorder.LOWERED,
                      new Color(0, 135, 0), new Color(0, 95, 0));

  /**
   * Fields in GUI applications which are required
   * should use this border.
   */
  public final static Border IS_REQUIRED_BORDER =
      new BevelBorder(BevelBorder.LOWERED,
                      new Color(135, 0, 0), new Color(95, 0, 0));

  /**
   * Use this to create a 15 pixel padding around a GUI component.
   */
  public final static Border EMPTY_BORDER =
      BorderFactory.createEmptyBorder(15, 15, 15, 15);

  /**
   * Use this to create a 15 bixel padding around a GUI component
   * except for the top which will have no padding.
   */
  public final static Border EMPTY_BORDER_NO_TOP =
      BorderFactory.createEmptyBorder(0, 15, 15, 15);

  /**
   * When using GridBagLayout, this should be the default insets
   * setting around the components in the layout.
   */
  public final static Insets GRID_INSETS = new Insets(2, 2, 2, 2);

  /**
   * Use this if you want components to have empty insets.
   */
  public final static Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

  /**
   * Name of the property indicating whether or not user interaction will take
   * place in GUI windows or on the  command line.
   * @see SwingToolkit#usingView()
   */
  public final static String VIEW = "meme.view";

}