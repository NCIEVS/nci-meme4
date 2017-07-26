/*****************************************************************************
 *
 * Package:    gov.nih.nlm.swing
 * Object:     FontSizeManager.java
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import java.util.ArrayList;
import java.util.Map;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.awt.font.TextAttribute;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

/**
 * Resizes {@link Font}s in a {@link Container}.
 *
 * @author Brian Carlsen
 */

public class FontSizeManager {

  //
  // Fields
  //
  private static ArrayList containers = new ArrayList();
  private static int size = 0;

  /**
   * Privately instantiates a {@link FontSizeManager}.
   * Prevents subclassing and direct instantiation.
   */
  private FontSizeManager() {
  }

  /**
   * Adds a {@link Container} to the list of Containers whose
   * font size will be managed.
   * @param c the {@link Container} to manage
   */
  public synchronized static void addContainer(Container c) {
    if (!containers.contains(c)) {
      containers.add(c);
    }
    adjustFontSize(c);
  }

  /**
   * Removes a {@link Container} from the list of Containers whose
   * font size will be managed.
   * @param c the {@link Container} to remove
   */
  public synchronized static void removeContainer(Container c) {
    containers.remove(c);
  }

  /**
   * Increases font size of all managed containers.  This method also tracks
   * the number of font size increments/decrements that have been performed
   * so that new frames/dialogs can be adjusted before they are made visible.
   * @see #adjustFontSize(Container)
   */
  public synchronized static void increaseFontSize() {
    size++;
    for (int i = 0; i < containers.size(); i++) {
      Container c = (Container) containers.get(i);
      increaseFonts(c);
      if (containers.get(i)instanceof Window) {
        ( (Window) containers.get(i)).pack();
      }
      if (containers.get(i)instanceof JPopupMenu) {
        ( (JPopupMenu) containers.get(i)).pack();
      }
      if (containers.get(i)instanceof JInternalFrame) {
        ( (JInternalFrame) containers.get(i)).pack();
      }
    }
  }

  /**
   * Decreases font size of all managed containers.  This method also tracks
   * the number of font size increments/decrements that have been performed
   * so that new frames/dialogs can be adjusted before they are made visible.
   * @see #adjustFontSize(Container)
   */
  public synchronized static void decreaseFontSize() {
    size--;
    for (int i = 0; i < containers.size(); i++) {
      decreaseFonts( (Container) containers.get(i));
      if (containers.get(i)instanceof Window) {
        ( (Window) containers.get(i)).pack();
      }
      if (containers.get(i)instanceof JPopupMenu) {
        ( (JPopupMenu) containers.get(i)).pack();
      }
      if (containers.get(i)instanceof JInternalFrame) {
        ( (JInternalFrame) containers.get(i)).pack();
      }
    }
  }

  /**
   * Adjusts the size of the fonts in the specified {@link Container}.  This
   * makes calls to helper methods that increase or decrease the font
   * as many times as it takes to scale the font size of the container to
   * match the number of increase/decrease font calls that have been made
   * thus far.  This method should be used for dialogs/frames that are
   * just about to become visible.
   * @param c the {@link Container} to adjust
   */
  public synchronized static void adjustFontSize(Container c) {
    int this_size = 0;
    if (size == 0) {
      return;
    }
    if (size < 0) {
      do {
        decreaseFonts(c);
        this_size--;
      } while (this_size > size);
    }
    if (size > 0) {
      do {
        increaseFonts(c);
        this_size++;
      } while (this_size < size);
    }
  }

  /**
   * This helper method implements the font increase for a container.
   * @param c the {@link Container} to resize
   */
  private static void increaseFonts(Container c) {
    if (c == null) {
      return;
    }
    Component[] chd = c.getComponents();
    for (int i = 0; i < chd.length; i++) {
      Font f = chd[i].getFont();
      if (f == null) {
        continue;
      }
      Map m = f.getAttributes();
      Float size = (Float) m.get(TextAttribute.SIZE);
      size = new Float(size.floatValue() + (float) 2.0);
      m.put(TextAttribute.SIZE, size);
      chd[i].setFont(new Font(m));

      //
      // maintain size of scroll pane
      //
      if (chd[i] instanceof JScrollPane && chd[i].getSize().getWidth() != 0) {
        ( (JComponent) chd[i]).setPreferredSize(chd[i].getSize());
      }
      if (chd[i] instanceof Container && !containers.contains(chd[i])) {
        increaseFonts( (Container) chd[i]);
      }
      if (chd[i] instanceof JMenu) {
        increaseFonts( ( (JMenu) chd[i]).getPopupMenu());
      }
      if (chd[i] instanceof JComponent) {
        Border border = ( (JComponent) chd[i]).getBorder();
        if (border instanceof CompoundBorder) {
          Border outside_border = ( (CompoundBorder) border).getOutsideBorder();
          if (outside_border instanceof TitledBorder) {
            TitledBorder titled_border = (TitledBorder) outside_border;
            Font font = titled_border.getTitleFont();
            titled_border.setTitleFont(
                new Font(font.getName(), font.getStyle(), font.getSize() + 2));
          }
        }
      }
    }

    if (c instanceof JTable) {
      JTable table = (JTable) c;
      table.setRowHeight(table.getFontMetrics(table.getFont()).getHeight() - 1);
    }

  }

  /**
   * This private method implements the font decrease for a container.
   * @param c the {@link Container} to resize
   */
  private static void decreaseFonts(Container c) {
    if (c == null) {
      return;
    }
    Component[] chd = c.getComponents();
    for (int i = 0; i < chd.length; i++) {

      //
      // maintain size of scroll pane
      //
      if (chd[i] instanceof JScrollPane && chd[i].getSize().getWidth() != 0) {
        ( (JComponent) chd[i]).setPreferredSize(chd[i].getSize());
      }
      if (chd[i] instanceof Container && !containers.contains(chd[i])) {
        decreaseFonts( (Container) chd[i]);
      }
      if (chd[i] instanceof JMenu) {
        decreaseFonts( ( (JMenu) chd[i]).getPopupMenu());
      }
      if (chd[i] instanceof JComponent) {
        Border border = ( (JComponent) chd[i]).getBorder();
        if (border instanceof CompoundBorder) {
          Border outside_border = ( (CompoundBorder) border).getOutsideBorder();
          if (outside_border instanceof TitledBorder) {
            TitledBorder titled_border = (TitledBorder) outside_border;
            Font font = titled_border.getTitleFont();
            titled_border.setTitleFont(
                new Font(font.getName(), font.getStyle(), font.getSize() - 2));
          }
        }
      }

      Font f = chd[i].getFont();
      if (f == null) {
        continue;
      }
      Map m = f.getAttributes();
      Float size = (Float) m.get(TextAttribute.SIZE);
      size = new Float(size.floatValue() - (float) 2.0);
      m.put(TextAttribute.SIZE, size);
      chd[i].setFont(new Font(m));
    }

    if (c instanceof JTable) {
      JTable table = (JTable) c;
      table.setRowHeight(table.getFontMetrics(table.getFont()).getHeight() - 1);
    }
  }

}
