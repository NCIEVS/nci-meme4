/************************************************************************
 *
 * Package:     gov.nih.nlm.umls.jekyll.swing
 * Object:      GlassPane
 *
 * Author:      Vladimir Olenichev
 *
 * Remarks:
 *
 * Change History:
 *  07/05/2002: First version
 *
 ***********************************************************************/

package gov.nih.nlm.umls.jekyll.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This component can be used as an intelligent glass pane to disable all
 * mouse and key events during time-consuming operations.
 * Here is a simple example of how to use it:
 * <pre>
 *
 * import gov.nih.nlm.umls.jekyll.swing.GlassPane;
 *
 * public class MyApp extends JFrame {
 *
 *  public MyApp() {
 *   setGlassPane(new GlassPane());
 *  }
 *  .
 *  .
 *  .
 *
 *  getGlassPane().setVisible(true);
 *  while (i < 1000000) {
 *    ...
 *  }
 *  getGlassPane.setVisible(false);
 *
 * }
 *
 * </pre>
 *
 * <p>
 * {@link <a href="/vlad-doc/jekyll/src_files/Swing/GlassPane.java.html">Browse Source</a>}
 */
public class GlassPane
    extends JComponent
    implements AWTEventListener {

  //
  // Private Fields
  //
  private Window parentWindow = null;

  //
  // Constructors
  //
  public GlassPane() {
    setOpaque(false);
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        evt.consume();
// 		    System.out.println("Mouse click is consumed");
      }
    });
  }

  /**
   * Makes the glass pane visible or invisible, plus installs/removes a key events hook
   * that allows to intercept all key events.
   *
   * @param b if <code>true</code>, the glass pane becomes visible, and
   * all the mouse and the key events are disabled; <code>false</code> otherwise
   */
  public void setVisible(boolean b) {
    if (b) {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      if (parentWindow == null) {
        parentWindow = javax.swing.SwingUtilities.windowForComponent(this);
      }

      Toolkit.getDefaultToolkit().addAWTEventListener(this,
          AWTEvent.KEY_EVENT_MASK);
    }
    else {
      Toolkit.getDefaultToolkit().removeAWTEventListener(this);
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    super.setVisible(b);
  }

  /**
   * Invoked whenever there is an event in AWT queue. The current implementation
   * consumes all key events, not just events for this window. Logic can be enhanced
   * to examine the source of the event and the source's parent window and skip only
   * those events that originated from disabled window.
   */
  public void eventDispatched(AWTEvent event) {
    if (event instanceof KeyEvent && event.getSource() instanceof Component) {
      if (javax.swing.SwingUtilities.windowForComponent( (Component) event.getSource()) ==
          this.parentWindow) {
// 		System.out.println("Key typed: " + ((KeyEvent) event).getKeyChar());
        ( (KeyEvent) event).consume();
      }
    }
  }

  /**
   * This main() is provided for debugging purposes.
   */
  public static void main(String[] args) {
    final JFrame frame = new JFrame();
    frame.setGlassPane(new GlassPane());
    JButton button = new JButton("make GlassPane visible");

    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        frame.getGlassPane().setVisible(true);
      }
    });

    frame.getContentPane().add(button);
    frame.setSize(300, 100);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);

  } // main()
}
