/*****************************************************************************
 *
 * Object:  ChangeDataSourceAction
 * Author:  Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.swing.ListDialog;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.awt.Component;
import java.awt.Event;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

/**
 * Used for GUI buttons or menu items allowing the user
 * to switch databases.
 *
 * Use of this class is not trivial.  You will want to create an
 * instance of the action which is relative to a particular {@link Component}
 * (for when the dialog opens up).  The initial data source selection is
 * specified in the constructor.
 *
 * Once the action exists, you should connect any interested listeners
 * to it, as they will be informed of any change to the server information
 * via a {@link DataSourceChangeEvent}.  Additionally, any GUIs that should
 * be disabled while the new data source connection is being validated should respond
 * to the other {@link DataSourceChangeListener} methods which indicate when the frame
     * should be disabled and when it should be re-enabled.  For a full test-example
 * please see {@link TestFinderFrame}.
 *
 * @author  Brian Carlsen (bcarlsen@msdinc.com)
 */

public class ChangeDataSourceAction extends AbstractAction {

  //
  // Fields
  //

  private static final long serialVersionUID = 1L;
  private Component parent = null;
  private String current_service = null;
  private EventListenerList listener_list = null;
  private DataSourceChangeEvent dsc_event = null;

  //
  // Constructors
  //

  /**
   * Instantiate a {@link ChangeDataSourceAction}.
   * @param parent the parent {@link Component}
   * @param current_service the current mid service
   */
  public ChangeDataSourceAction(Component parent, String current_service) {
    super();
    this.parent = parent;
    this.current_service = current_service;
    listener_list = new EventListenerList();
    putValue(Action.NAME, "Change Data Source");
    putValue(Action.SHORT_DESCRIPTION, "Change the data source being used");
    putValue(Action.MNEMONIC_KEY,
             new Integer( (int) 'd'));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke('D', Event.CTRL_MASK));
  }

  //
  // Methods
  //

  /**
   * Opens a dialog that allows the user to select a data source to use.
   * The list of choices is obtained through the {@link MIDServices}.
   * @param e a dummy event
   */
  public void actionPerformed(ActionEvent e) {

    try {
      fireEnableGlassPane();
      String[] services = FieldedStringTokenizer.split(
          MIDServices.getService("databases"), ",");

      final String mid_service = (String) ListDialog.showListSingleMode(
          parent, "Select a database",
          "Select a database", services,
          current_service);

      if (mid_service != null) {
        Thread t = new Thread(
            new Runnable() {
          public void run() {
            current_service = mid_service;
            fireDataSourceChanged(current_service);
            fireDisableGlassPane();
          }
        }
        );
        t.start();
      } else {
        fireDisableGlassPane();
      }
    } catch (Exception ex) {}
    ;
  }

  /**
   * Add a listener.
   * @param l the {@link DataSourceChangeListener} to add
   */
  public void addDataSourceChangeListener(DataSourceChangeListener l) {
    listener_list.add(DataSourceChangeListener.class, l);
  }

  /**
   * Remove a listener.
   * @param l the {@link DataSourceChangeListener} to remove
   */
  public void removeDataSourceChangeListener(DataSourceChangeListener l) {
    listener_list.remove(DataSourceChangeListener.class, l);
  }

  /**
   * Inform listeners that the data source has
   * changed via a {@link DataSourceChangeEvent}.
   * @param new_service the new mid service value
   */
  protected void fireDataSourceChanged(String new_service) {
    Object[] listeners = listener_list.getListenerList();
    dsc_event = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == DataSourceChangeListener.class) {
        // Lazily create the event:
        if (dsc_event == null) {
          dsc_event = new DataSourceChangeEvent(new_service);
        }
        ( (DataSourceChangeListener) listeners[i +
         1]).dataSourceChanged(dsc_event);
      }
    }
  }

  /**
   * Inform any GUI component listeners that they should be re-enabled.
   * This method is called once the new data source selection is validated.
   */
  protected void fireEnableGlassPane() {
    Object[] listeners = listener_list.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == DataSourceChangeListener.class) {
        ( (DataSourceChangeListener) listeners[i + 1]).enableGlassPane();
      }
    }
  }

  /**
   * Inform any GUI component listeners that they should disable themselves.
   * This method is called before the new data source is validated in order
   * to allow applications to gracefully disable their frames (i.e. put up
   * an hourglass cursor and prevent user interaction).
   */
  protected void fireDisableGlassPane() {
    Object[] listeners = listener_list.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == DataSourceChangeListener.class) {
        ( (DataSourceChangeListener) listeners[i + 1]).disableGlassPane();
      }
    }
  }

}
