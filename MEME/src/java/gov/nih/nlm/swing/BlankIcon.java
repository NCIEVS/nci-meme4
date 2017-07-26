/**************************************************************************
 *
 * Package:	gov.nih.nlm.swing
 * Object:	BlankIcon.java
 *
 **************************************************************************/
package gov.nih.nlm.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 *  Used as a filler on the column header when the user has not yet chosen
 *  to sort a column and a directional {@link BevelArrowIcon} is inappropriate.
 */
public class BlankIcon implements Icon {
  private Color fillColor;
  private int size;

  /**
   * Instantiate an empty {@link BlankIcon}.
   */
  public BlankIcon() {
    this(null, 11);
  }

  /**
   * Instantiates a {@link BlankIcon} using the color and size specified
   * @param color the {@link Color}
   * @param size the size
   */
  public BlankIcon(Color color, int size) {
    fillColor = color;
    this.size = size;
  }

  /**
   * Repaints the {@link BlankIcon}.
   * @param c the {@link Component}
   * @param g the {@link Graphics}
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public void paintIcon(Component c, Graphics g, int x, int y) {
    if (fillColor != null) {
      g.setColor(fillColor);
      g.drawRect(x, y, size - 1, size - 1);
    }
  }

  /**
   * Returns the icon width.
   * @return the icon width
   */
  public int getIconWidth() {
    return size;
  }

  /**
   * Returns the icon height.
   * @return the icon height
   */
  public int getIconHeight() {
    return size;
  }
}
