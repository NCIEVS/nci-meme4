/*****************************************************************************
 *
 * Object:  GlassComponent.java
 * Author:  Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Used to block access to a frame by placing it
 * on the glass pane.  Making it visible blocks the
 * glass pane (with a wait cursor), and making it
 * invisible returns control to the frame.
 *
 * @author  Brian Carlsen (bcarlsen@apelon.com)
 */
public class GlassComponent extends JComponent implements AWTEventListener {

  //
  // Private constants
  //
  // Events will be consumed for this window.
  private Window parent_window;
  // Focus will be returned to this component.
  private Component last_focus_owner = null;

  /**
   * Instantiates a {@link GlassComponent} with the specified root component.
   * @param root the root {@link Component}
   */
  public GlassComponent(Component root) {
    //
    // Add listeners to capture events
    //
    addMouseListener(new MouseAdapter() {});
    addKeyListener(new KeyAdapter() {});

    //
    // Set cursor
    //
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  /**
   * Causes the component to become visible/invisible.
       * @param visible <code>boolean</code> indicating if component should be visible
   */
  public void setVisible(boolean visible) {
    if (visible) {
      //
      // Find parent window
      //
      if (parent_window == null) {
        parent_window = SwingUtilities.windowForComponent(this);
      }

      //
      // Get component with focus and save it
      //
      Component focus_owner = parent_window.getFocusOwner();
      if (focus_owner != this) {
        last_focus_owner = focus_owner;
      }

      //
      // Add this as an event listener for AWT Key events and request focus.
      //
      Toolkit.getDefaultToolkit().addAWTEventListener(this,
          AWTEvent.KEY_EVENT_MASK);
      requestFocus();
      super.setVisible(visible);
    } else {

      //
      // Remove this as an event listener for AWT Key events
      //
      Toolkit.getDefaultToolkit().removeAWTEventListener(this);
      super.setVisible(visible);
      if (last_focus_owner != null) {
        last_focus_owner.requestFocus();
      }
    }
  }

  /**
   * This absorbs {@link KeyEvent}s so that they don't have any effect on the
   * window that has the {@link GlassComponent} enabled.
   * @param event any {@link AWTEvent}
   */
  public void eventDispatched(AWTEvent event) {
    //
    // Consume KeyEvents
    //
    if (event instanceof KeyEvent && event.getSource()instanceof Component) {
      if (SwingUtilities.windowForComponent( (Component) event.getSource()) ==
          parent_window) {
        ( (KeyEvent) event).consume();
      }
    }
  }
}
