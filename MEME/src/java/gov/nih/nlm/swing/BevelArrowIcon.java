/**************************************************************************
 *
 * Package:	gov.nih.nlm.swing
 * Object:	BevelArrowIcon.java
 *
 **************************************************************************/
package gov.nih.nlm.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * {@link Icon} that depicts an arrow used on a column header to indicate
 * if sorting is ascending or descending.
 */
public class BevelArrowIcon implements Icon {

  //
  // Fields
  //
  public static final int UP = 0; // direction
  public static final int DOWN = 1;

  private static final int DEFAULT_SIZE = 11;
  private Color edge1;
  private Color edge2;
  private Color fill;
  private int size;
  private int direction;

  /**
   * Instantiates a {@link BevelArrowIcon} from the specified parameters.
   * @param direction up or down
   * @param is_raised_view does appear as raised
   * @param is_pressed_view does appear as pressed
   */
  public BevelArrowIcon(int direction, boolean is_raised_view,
                        boolean is_pressed_view) {
    if (is_raised_view) {
      if (is_pressed_view) {
        init(UIManager.getColor("controlLtHighlight"),
             UIManager.getColor("controlDkShadow"),
             UIManager.getColor("controlShadow"),
             DEFAULT_SIZE, direction);
      } else {
        init(UIManager.getColor("controlHighlight"),
             UIManager.getColor("controlShadow"),
             UIManager.getColor("control"),
             DEFAULT_SIZE, direction);
      }
    } else {
      if (is_pressed_view) {
        init(UIManager.getColor("controlDkShadow"),
             UIManager.getColor("controlLtHighlight"),
             UIManager.getColor("controlShadow"),
             DEFAULT_SIZE, direction);
      } else {
        init(UIManager.getColor("controlShadow"),
             UIManager.getColor("controlHighlight"),
             UIManager.getColor("control"),
             DEFAULT_SIZE, direction);
      }
    }
  }

  /**
   * Redraws the {@link Icon}.
   * @param c the {@link Component}
   * @param g the {@link Graphics}
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public void paintIcon(Component c, Graphics g, int x, int y) {
    switch (direction) {
      case DOWN:
        drawDownArrow(g, x, y);
        break;
      case UP:
        drawUpArrow(g, x, y);
        break;
    }
  }

  /**
   * Returns the width of the icon.
   * @return the width of the icon
   */
  public int getIconWidth() {
    return size;
  }

  /**
   * Returns the height of the icon.
   * @return the height of the icon
   */
  public int getIconHeight() {
    return size;
  }

  /**
   * Initializes the icon.
   * @param edge1 the {@link Color} of edge 1
   * @param edge2 the {@link Color} of edge 2
   * @param fill the fill {@link Color}
   * @param size the size
   * @param direction the direction it is pointing
   */
  private void init(Color edge1, Color edge2, Color fill,
                    int size, int direction) {
    this.edge1 = edge1;
    this.edge2 = edge2;
    this.fill = fill;
    this.size = size;
    this.direction = direction;
  }

  /**
   * Draws a downward pointing arrow.
   * @param g the {@link Graphics}
   * @param xo the x coordinate
   * @param yo the y coordinate
   */
  private void drawDownArrow(Graphics g, int xo, int yo) {
    g.setColor(edge1);
    g.drawLine(xo, yo, xo + size - 1, yo);
    g.drawLine(xo, yo + 1, xo + size - 3, yo + 1);
    g.setColor(edge2);
    g.drawLine(xo + size - 2, yo + 1, xo + size - 1, yo + 1);
    int x = xo + 1;
    int y = yo + 2;
    int dx = size - 6;
    while (y + 1 < yo + size) {
      g.setColor(edge1);
      g.drawLine(x, y, x + 1, y);
      g.drawLine(x, y + 1, x + 1, y + 1);
      if (0 < dx) {
        g.setColor(fill);
        g.drawLine(x + 2, y, x + 1 + dx, y);
        g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
      }
      g.setColor(edge2);
      g.drawLine(x + dx + 2, y, x + dx + 3, y);
      g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
      x += 1;
      y += 2;
      dx -= 2;
    }
    g.setColor(edge1);
    g.drawLine(xo + (size / 2), yo + size - 1, xo + (size / 2), yo + size - 1);
  }

  /**
   * Draws an upward pointing arrow.
   * @param g the {@link Graphics}
   * @param xo the x coordinate
   * @param yo the y coordinate
   */
  private void drawUpArrow(Graphics g, int xo, int yo) {
    g.setColor(edge1);
    int x = xo + (size / 2);
    g.drawLine(x, yo, x, yo);
    x--;
    int y = yo + 1;
    int dx = 0;
    while (y + 3 < yo + size) {
      g.setColor(edge1);
      g.drawLine(x, y, x + 1, y);
      g.drawLine(x, y + 1, x + 1, y + 1);
      if (0 < dx) {
        g.setColor(fill);
        g.drawLine(x + 2, y, x + 1 + dx, y);
        g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
      }
      g.setColor(edge2);
      g.drawLine(x + dx + 2, y, x + dx + 3, y);
      g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
      x -= 1;
      y += 2;
      dx += 2;
    }
    g.setColor(edge1);
    g.drawLine(xo, yo + size - 3, xo + 1, yo + size - 3);
    g.setColor(edge2);
    g.drawLine(xo + 2, yo + size - 2, xo + size - 1, yo + size - 2);
    g.drawLine(xo, yo + size - 1, xo + size, yo + size - 1);
  }

}
