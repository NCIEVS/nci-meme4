/*****************************************************************************
 *
 * Object:  ChangeServerAction
 * Author:  Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;

import java.awt.Component;
import java.awt.Event;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

/**
 * Used for GUI buttons and menu items allowing the
 * user to point the application to a different MEME Server.
 *
 * Use of this class is not trivial.  You will want to create an
 * instance of the action which is relative to a particular {@link Component}
 * (for when the dialog opens up).  The initial host/port settings
 * are specified in the constructor.
 *
 * Once the action exists, you should connect any interested listeners
 * to it, as they will be informed of any change to the server information
 * via a {@link ServerChangeEvent}.  Additionally, any GUIs that should
     * be disabled while the new server connection is being validated should respond
 * to the other {@link ServerChangeListener} methods which indicate when the frame
     * should be disabled and when it should be re-enabled.  For a full test-example
 * please see {@link TestFinderFrame}.
 *
 * @author  Brian Carlsen (bcarlsen@msdinc.com)
 */

public class ChangeServerAction extends AbstractAction {

  //
  // Fields
  //

	
  private static final long serialVersionUID = 1L;
  private Component parent = null;
  private String current_host = null;
  private int current_port = 0;
  private EventListenerList listener_list = null;
  private ServerChangeEvent dsc_event = null;

  //
  // Constructors
  //

  /**
   * Instantiate a {@link ChangeServerAction}.
   * @param parent the parent {@link Component}
   * @param host the initial host setting
   * @param port the initial port setting
   */
  public ChangeServerAction(Component parent, String host, int port) {
    super();
    this.parent = parent;
    this.current_host = host;
    this.current_port = port;
    listener_list = new EventListenerList();
    putValue(Action.NAME, "Change Server");
    putValue(Action.SHORT_DESCRIPTION, "Change the server being used");
    putValue(Action.MNEMONIC_KEY,
             new Integer( (int) 'g'));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke('G', Event.CTRL_MASK));
  }

  //
  // Methods
  //

  /**
   * Opens a dialog that allows the user to select a server to use.
   * @param e a dummy event
   */
  public void actionPerformed(ActionEvent e) {

    try {

      fireEnableGlassPane();
      final ChangeServerDialog csd =
          new ChangeServerDialog(parent, current_host, current_port);

      // block until response
      csd.setVisible(true);

      if (csd.cancelled()) {
        csd.dispose();
        fireDisableGlassPane();
        return;
      }

      Thread t = new Thread(
          new Runnable() {
        public void run() {
          String server_version = null;
          csd.setVisible(false);
          try {
            AdminClient ac = new AdminClient();
            ac.getRequestHandler().setHost(csd.getHost());
            ac.getRequestHandler().setPort(csd.getPort());
            // do this in another thread...?
            server_version = ac.getServerVersion();
          } catch (Exception ex) {
            MEMEToolkit.notifyUser(
                "Cannot connect to specified host/port, try again.");
            ChangeServerAction.this.actionPerformed(null);
            return;
          }
          current_host = csd.getHost();
          current_port = csd.getPort();
          fireServerChanged(current_host, current_port);
          MEMEToolkit.notifyUser("Successfully connected to server\n" +
                                 "host: " + csd.getHost() + "\n" +
                                 "port: " + csd.getPort() + "\n" +
                                 "version: " + server_version);
          csd.dispose();
          fireDisableGlassPane();
        }
      }
      );
      t.start();

    } catch (Exception ex) {}
    ;
  }

  /**
   * Add a listener.
   * @param l the {@link ServerChangeListener} to add
   */
  public void addServerChangeListener(ServerChangeListener l) {
    listener_list.add(ServerChangeListener.class, l);
  }

  /**
   * Remove a listener.
   * @param l the {@link ServerChangeListener} to remove
   */
  public void removeServerChangeListener(ServerChangeListener l) {
    listener_list.remove(ServerChangeListener.class, l);
  }

  /**
   * Inform all listeners that the server connection has changed
   * via {@link ServerChangeEvent}s.
   * @param new_host the new server host
   * @param new_port the new server port
   */
  protected void fireServerChanged(String new_host, int new_port) {
    Object[] listeners = listener_list.getListenerList();
    dsc_event = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ServerChangeListener.class) {
        // Lazily create the event:
        if (dsc_event == null) {
          dsc_event = new ServerChangeEvent(new_host, new_port);
        }
        ( (ServerChangeListener) listeners[i + 1]).serverChanged(dsc_event);
      }
    }
  }

  /**
   * Inform any GUI component listeners that they should be re-enabled.
   * This method is called once the new server connection is validated.
   */
  protected void fireEnableGlassPane() {
    Object[] listeners = listener_list.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ServerChangeListener.class) {
        ( (ServerChangeListener) listeners[i + 1]).enableGlassPane();
      }
    }
  }

  /**
   * Inform any GUI component listeners that they should disable themselves.
       * This method is called before the new server connection is validated in order
   * to allow applications to gracefully disable their frames (i.e. put up
   * an hourglass cursor and prevent user interaction).
   */
  protected void fireDisableGlassPane() {
    Object[] listeners = listener_list.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ServerChangeListener.class) {
        ( (ServerChangeListener) listeners[i + 1]).disableGlassPane();
      }
    }
  }

}
