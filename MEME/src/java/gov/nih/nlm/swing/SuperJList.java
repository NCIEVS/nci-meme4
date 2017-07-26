/*****************************************************************************
 *
 * Package:    gov.nih.nlm.swing
 * Object:     SuperJList.java
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.ToolTipManager;

/**
 * {@link JList} with a key listener and other extras.
 *
 * @author Owen J. Carlsen, Brian Carlsen
 */
public class SuperJList extends JList {

  //
  // Private Fields
  //
  private JPopupMenu popup = null;
  private DefaultListModel model = null;

  /**
   * Instantiates an empty {@link SuperJList}.
   */
  public SuperJList() {
    super();
    model = new DefaultListModel();
    setModel(model);
    addKeyListener(
        new KeyAdapter() {

      //
      // have the key listener track selected items by keypresses
      //
      public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        Object o = getSelectedValue();
        String element;
        char f;

        //
        // if nothing selected, select first row starting with c
        // do this by forcing f != c to trigger next if clause
        //
        if (o != null) {
          element = o.toString();
          f = element.toLowerCase().charAt(0);
        } else {
          f = (char) ( ( (int) c) + 1);
        }
        ListModel model = getModel();
        int z = model.getSize();
        int i;
        //
        // if c != f, set selected value to first element starting with c
        // if no such element exists, do not change selections
        //
        if (c != f) {
          for (i = 0; i < z; i++) {
            element = model.getElementAt(i).toString();
            if (c == element.toLowerCase().charAt(0)) {
              setSelectedIndex(i);
              ensureIndexIsVisible(i);
              break;
            }
          }
        }
        //
        // If c = f, set selected value to either the next element
        // if it also starts with c or to the first element starting
        // with c
        //
        else {
          i = getSelectedIndex();
          //
          // if i is not last & next row starts with c, select it
          //
          if (i < (z - 1) &&
              model.getElementAt(i + 1).toString().toLowerCase().charAt(0) ==
              c) {
            setSelectedIndex(i + 1);
            ensureIndexIsVisible(i + 1);
          }
          //
          // otherwise count down until the first row starting with c is
          // found and select that one.
          //
          else {
            for (; i > -1; i--) {
              element = model.getElementAt(i).toString();
              if (element.toLowerCase().charAt(0) != c) {
                setSelectedIndex(i + 1);
                ensureIndexIsVisible(i + 1);
                break;
              }
            }
          }
        }
      }
    });

    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /**
   * Instantiates a {@link SuperJList} from the specified data.
   * @param items the list items
   */
  public SuperJList(Object[] items) {
    this();
    setListData(items);
  }

  /**
   * Selects all elements of the list.
   */
  public void selectAll() {
    int index = getModel().getSize() - 1;
    getSelectionModel().setSelectionInterval(0, index);
  }

  /**
   * Sets the list data.
   * @param items the list data
   */
  public void setListData(Object[] items) {
    model.clear();
    for (int i = 0; i < items.length; i++) {
      model.addElement(items[i]);
    }
  }

  /**
   * Resize the list to be within specifications.
   * @param min minimum size
   * @param max maximum size
   */
  public void resizeList(int min, int max) {
    int size = getModel().getSize();
    setVisibleRowCount( (size > max) ? max :
                       (size == 0) ? min :
                       size);
  }

  /**
   * Programatically selects multiple items.  If the flag is <code>true</code>
   * scroll to the first selected item.
   * @param items list of items to select
   * @param should_scroll indicates whether or not to scroll to first
   * selected item
   */
  public void setSelectedValues(Object[] items, boolean should_scroll) {

    //
    //  setSelectedValue() clears current selection, so can only use it once.
    //  After that, must use addSelectionInterval method.
    //
    if (items.length > 0) {
      setSelectedValue(items[0], should_scroll);

    }
    if (items.length > 1) {
      ListModel lm = (ListModel) getModel();
      for (int item_idx = 1; item_idx < items.length; ++item_idx) {
        for (int list_idx = 0; list_idx < lm.getSize(); ++list_idx) {
          if (items[item_idx].equals(lm.getElementAt(list_idx))) {
            addSelectionInterval(list_idx, list_idx);
            break; //  Quit inner loop
          }
        }
      }
    }
  }

  /**
   * Returns all selected values as {@link String}s.
   * @return all selected values as {@link String}s
   */
  public String[] getSelectedStringValues() {
    Object[] oa = getSelectedValues();
    String[] sa = new String[oa.length];
    for (int i = 0; i < oa.length; i++) {
      sa[i] = oa[i].toString();
    }
    ;
    return sa;
  }

  /**
   * Returns the tool tip text.
   * @param event a {@link MouseEvent}
   * @return the tool tip text
   */
  public String getToolTipText(MouseEvent event) {

    //
    // get index, get the object, get the renderer, call its getToolTipText...
    //
    int index = locationToIndex(event.getPoint());
    if (index > -1) {
      Object item = getModel().getElementAt(index);
      JComponent cell = (JComponent) getCellRenderer().
          getListCellRendererComponent(this, item, index, false, false);

      return cell.getToolTipText(event);
    }
    return null;
  }

  /**
   * Sets the {@link JPopupMenu}.
   * @param popup the {@link JPopupMenu}
   */
  public void setPopupMenu(JPopupMenu popup) {
    this.popup = popup;
  };

  /**
   * Opens the popup menu if the is one,
   * otherwise forwards the request to the superclass.
   * @param e the {@link MouseEvent}
   */
  public void processMouseEvent(MouseEvent e) {
    if (e.isPopupTrigger() && popup != null) {
      popup.show(this, e.getX(), e.getY());
    } else {
      super.processMouseEvent(e);
    }
  }

  /**
   * Scroll the list to the first selected row.
   */
  public void scrollToFirstSelectedRow() {
    int[] indices = getSelectedIndices();
    if (indices.length>0)
      scrollRectToVisible(getCellBounds(indices[0],indices[indices.length-1]));
  }
}