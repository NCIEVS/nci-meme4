/********************************************************************
 *
 * Package:	gov.nih.nlm.swing
 * Object:	CutandPasteDNDTable.java
 *
 ********************************************************************/
package gov.nih.nlm.swing;

import java.util.HashSet;
import java.util.Iterator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * A type of {@link JTable} that supports cut-and-paste
 * and drag-and-drop operations for reordering rows.
 * <p>
 * The Cut-and-paste mechanism allows users to select rows,
 * Use the <control>-X key to remove the rows from the table,
 * and finally the <control>-V key to paste the rows back in
 * to the table above the current first selected row.
 * <p>
 * The Drag-and-drop mechanism allows users to select rows,
 * and then drag those rows and drop them to another place in
 * the table.
 * <p>
 * Using this class requires the use of a {@link CutandPasteDNDTableModel}.
 *
 * @author Deborah Shapiro
 */

public class CutandPasteDNDTable extends JTable implements DropTargetListener,
    DragSourceListener, DragGestureListener, Autoscroll {

  //
  // Private Fields
  //

  private DropTarget drop_target = null;
  private DragSource drag_source = null;
  private Point prev_point = null;
  private int[] drag_row_indexes = null;
  private CutandPasteDNDTableModel table_model = null;
  private BufferedImage img_ghost;
  private Rectangle2D rect = new Rectangle2D.Float();
  private HashSet mls = new HashSet();
  private HashSet mmls = new HashSet();
  private boolean finished_construction = false;
  private Color background;
  private boolean cut_done = false;
  private Insets autoscroll_insets = new Insets(20, 20, 20, 20);

  //
  // Constructors
  //

  /**
   * Instantiates a {@link CutandPasteDNDTable} from the specified model.
   * @param table_model the {@link CutandPasteDNDTableModel}
   */
  public CutandPasteDNDTable(CutandPasteDNDTableModel table_model) {
    super(table_model);
    this.table_model = table_model;
    drop_target = new DropTarget(this, this);
    drag_source = new DragSource();
    drag_source.createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_COPY_OR_MOVE, this);

    background = (new JTextField()).getSelectionColor();
    background = new Color(background.getRed(),
                           background.getGreen(),
                           background.getBlue(), 126);
    finished_construction = true;
  }

  /**
   * Instantiates a {@link CutandPasteDNDTable} without a model.
   */
  public CutandPasteDNDTable() {
    super();
    drop_target = new DropTarget(this, this);
    drag_source = new DragSource();
    drag_source.createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_MOVE, this);

    background = (new JTextField()).getSelectionColor();
    background = new Color(background.getRed(),
                           background.getGreen(),
                           background.getBlue(), 126);
    finished_construction = true;
  }

  //
  // Accessor Methods
  //

  /**
   * Sets the table model.
   * @param table_model the {@link CutandPasteDNDTableModel}
   */
  public void setModel(CutandPasteDNDTableModel table_model) {
    this.table_model = table_model;
    if (finished_construction) {
      super.setModel(table_model);
    }
  }

  /**
   * Sets the cut mode.  This programatically allows the user
   * to change the mode of the table.
   * @param mode the new cut mode
   */
  public void setCutMode(boolean mode) {
    cut_done = mode;
  }

  /**
   * Returns the cut mode.
   * @return <code>true</code> if a cut has been completed
   * 	    <code>false</code> if a paste has been completed
   */
  public boolean getCutMode() {
    return cut_done;
  }

  /**
   * Returns <code>true</code> if a cut has been completed,
   * 	    <code>false</code> otherwise
   * @return <code>true</code> if a cut has been completed,
   * 	    <code>false</code> otherwise
   */
  public boolean isCutDone() {
    return cut_done;
  }

  /**
   * Returns <code>true</code> if a cut has been completed,
   * 	    <code>false</code> otherwise
   * @return <code>true</code> if a cut has been completed,
   * 	    <code>false</code> otherwise
   */
  public boolean isPasteDone() {
    return!cut_done;
  }

  //
  // DropTargetListener Implementation
  //

  /**
   * Event handler for when a drag enters the drop component.
   * Accepts or rejects a drag event.
   * @param event the {@link DropTargetDragEvent}
   */
  public void dragEnter(DropTargetDragEvent event) {
    if (event.getSource() == drop_target) {
      event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    } else {
      event.rejectDrag();
    }
  }

  /**
   * Event handler for when a drag exits the drop component.
   * @param event the {@link DropTargetEvent}
   */
  public void dragExit(DropTargetEvent event) {
    if (!DragSource.isDragImageSupported()) {
      repaint(rect.getBounds());
    }
  }

  /**
   * Event handler for when a drag is over the drop component.
   * @param event a {@link DropTargetDragEvent}
   */
  public void dragOver(DropTargetDragEvent event) {

    //
    // Figure out where the point is
    //
    Point pt = event.getLocation();
    if (pt.equals(prev_point)) {
      return;
    }

    //
    // Save point location
    //
    prev_point = pt;

    //
    // Get graphics object and draw ghost image.
    //
    Graphics2D g2 = (Graphics2D) getGraphics();
    if (!DragSource.isDragImageSupported()) {
      paintImmediately(rect.getBounds());
      rect.setRect(pt.x, pt.y, img_ghost.getWidth(), img_ghost.getHeight());
      g2.drawImage(img_ghost,
                   AffineTransform.getTranslateInstance(
          rect.getX(), rect.getY()), null);
    }
  }

  /**
   * Event handler when a drop occurs
   * in the drop component. Accept the drop.
   * @param event the {@link DropTargetDragEvent}
   */
  public void drop(DropTargetDropEvent event) {

    event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

    //
    // Get drop location
    //
    Point drop_point = event.getLocation();

    //
    // Get index of the table
    //
    int drop_row_index = this.rowAtPoint(drop_point);

    //
    // Move table rows
    //
    table_model.moveRow(drag_row_indexes, drop_row_index);

    //
    // Complete the drop
    //
    event.getDropTargetContext().dropComplete(true);
  }

  /**
   * Event handler for when a drop action is changed.
   * Currently does nothing.
   * @param event the {@link DropTargetDragEvent}
   */
  public void dropActionChanged(DropTargetDragEvent event) {
    // Do nothing
  }

  //
  // DragGestureListener Implementation
  //

  /**
   * Event handler for recognizing that a drag has started.
   * In the future, we need to refine this to more easily
   * distinguish between a drag gesture and a "select multiple
   * table rows" gesture.
   * @param event the {@link DragGestureEvent}
   */
  public void dragGestureRecognized(DragGestureEvent event) {
    //
    // Save the indexes to drag
    //
    drag_row_indexes = this.getSelectedRows();
    if (drag_row_indexes.length == 0) {
      return;
    }

    //
    // Determine the number of dragged rows
    //
    int num_dragged_rows =
        drag_row_indexes[drag_row_indexes.length - 1] - drag_row_indexes[0] + 1;

    //
    // Obtain the ghosted image & prepare it
    //
    img_ghost = new BufferedImage(
        getWidth() + 1, (getRowHeight() * (num_dragged_rows + 1)) + 1,
        BufferedImage.TYPE_INT_ARGB_PRE);

    Graphics2D graphics = img_ghost.createGraphics();

    //
    // This does the ghosting, .5f means 50%
    //
    graphics.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
    graphics.setStroke(new java.awt.BasicStroke());
    int y = getRowHeight();
    int row = 0;
    for (int i = 0; i < num_dragged_rows; i++) {
      if (isRowSelected(drag_row_indexes[0] + i)) {
        paintGhostRow(graphics, y * row, row++);
      }
    }
    graphics.dispose();

    //
    // Begin the drag
    //
    drag_source.startDrag(event, new Cursor(Cursor.CROSSHAIR_CURSOR),
                          img_ghost, new Point( -10, 0),
                          new StringSelection("DRAG THIS"), this);
  }

  /**
   * Draws the ghosted box that follows the drag
   * @param g the {@linke Graphics2D} object with the ghosted image
   * @param height the row height
   * @param row the row index
   */
  private void paintGhostRow(Graphics2D g, int height, int row) {

    //
    // Set background color & Clear the rectangle
    //
    g.setBackground(background);
    g.clearRect(0, height, getWidth() + 1, getRowHeight() + 1);

    //
    // Loop through and draw all selected rows
    //
    int prev_w = 0;
    for (int i = 0; i < getColumnCount(); i++) {
      int width = getColumnModel().getColumn(i).getWidth();
      g.setPaint(Color.gray);
      g.draw(
          new Rectangle2D.Float(prev_w, height, width, getRowHeight()));

      //
      // Draw cell string
      //
      g.setPaint(Color.black);
      g.drawString(
          getValueAt(drag_row_indexes[row], i).toString(),
          prev_w + 2, height + (getRowHeight() - 5));
      prev_w += width;
    }
  }

  //
  // DragSourceListener Implementation
  //

  /**
   * Event handler for when dragging ends. Do nothing.
   * @param event the {@link DragSourceDropEvent}
   */
  public void dragDropEnd(DragSourceDropEvent event) {
    // Do nothing
  }

  /**
   * Event handler for when dragging
   * enters the drag source component. Do nothing.
   * @param event thye {@link DragSourceDragEvent}
   */
  public void dragEnter(DragSourceDragEvent event) {
    // Do nothing
  }

  /**
   * Event handler for when dragging
   * exits the drag source component. Do nothing.
   * @param event the {@link DragSourceEvent}
   */
  public void dragExit(DragSourceEvent event) {
    // Do nothing
  }

  /**
   * Event handler for when dragging
   * passes over the drag source component.
   * Set the cursor.
   * @param event the {@link DragSourceDragEvent}
   */
  public void dragOver(DragSourceDragEvent event) {
    DragSourceContext context = event.getDragSourceContext();
    context.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
  }

  /**
   * Event handler for when the drop action changes. Do nothing.
   * @param event the {@link DragSourceDragEvent}
   */
  public void dropActionChanged(DragSourceDragEvent event) {
    // do nothing
  }

  //
  // Overridden JComponent Methods
  //

  /**
   * Event handler for <control>-X and <control>-V key presses.
   * Calls the respective methods in the table model to
   * execute the cut and paste operations on the selected rows
   * It also manages the cut-mode to only allow cut when cut
   * is allowed, and only allow paste when paste is allowed.
   * @param e the {@link KeyEvent}
   */
  protected void processKeyEvent(KeyEvent e) {

    //
    // Handle <control>-X
    //
    if (e.getKeyCode() == KeyEvent.VK_X &&
        e.isControlDown() &&
        e.getID() == KeyEvent.KEY_RELEASED) {

      //
      // Check cut mode, handle, and fire events
      //
      if (!cut_done) {
        int[] rows_to_cut = this.getSelectedRows();
        table_model.cutRow(rows_to_cut);
        cut_done = true;
        fireCutDoneinCutMode();
      } else {
        fireCutDoneinPasteMode();
      }
    }

    //
    // Handle <control>-V
    //
    else if (e.getKeyCode() == KeyEvent.VK_V &&
             e.isControlDown() &&
             e.getID() == KeyEvent.KEY_RELEASED) {

      //
      // Check cut mode, handle, and fire events
      //
      if (cut_done) {
        int paste_location = this.getSelectedRow();
        table_model.pasteRow(paste_location);
        cut_done = false;
        firePasteDoneinPasteMode();
      } else {
        firePasteDoneinCutMode();
      }
    }

    //
    // Forward keypress to superclass
    //
    else {
      super.processKeyEvent(e);
    }
  }

  /**
   * Event handler for mouse events.  Remove mouse listeners
   * while dragging is taking place to avoid selection of
   * additional rows.
   * @param e the {@link MouseEvent}
   */
  protected void processMouseEvent(MouseEvent e) {

    //
    // If the mouse was released a drag is over
    //
    if (e.getID() == MouseEvent.MOUSE_RELEASED) {
      restoreMouseListeners();

      //
      // Drag is taking place
      // We may be able to better identify a drag, or possibly
      // just remove mouse listeners as soon as a drag
      // is initiated.
      //
    }
    if (e.getID() == MouseEvent.MOUSE_PRESSED &&
        this.isRowSelected(this.rowAtPoint(e.getPoint())) &&
        !e.isControlDown()) {
      removeMouseListeners(true);

      //
      // Forward the event to superclass
      //
    }
    super.processMouseEvent(e);
  }

  //
  // Listener Methods
  //

  /**
   * Adds a {@link CutandPasteModeListener}.
   * @param cpml the {@link CutandPasteModeListener}
   */
  public void addCutandPasteModeListener(CutandPasteModeListener cpml) {
    listenerList.add(CutandPasteModeListener.class, cpml);
  }

  /**
   * Removes a {@link CutandPasteModeListener}.
   * @param cpml the {@link CutandPasteModeListener}
   */
  public void removeCutandPasteModeListener(CutandPasteModeListener cpml) {
    listenerList.remove(CutandPasteModeListener.class, cpml);
  }

  /**
   * Adds a {@link CutandPasteModeListener}.
   * @param cpml the {@link CutandPasteModeListener}
   */
  public void addFocusListener(FocusListener cpml) {
    listenerList.add(FocusListener.class, cpml);
  }

  /**
   * Removes a {@link CutandPasteModeListener}.
   * @param cpml the {@link CutandPasteModeListener}
   */
  public void removeFocusListener(FocusListener cpml) {
    listenerList.remove(FocusListener.class, cpml);
  }

  /**
   * Remove mouse listeners from listener list.
   * @param drag_flag the flag indicating whether or not a drag is taking place
   */
  private synchronized void removeMouseListeners(boolean drag_flag) {

    //
    // Get mouse listeners
    //
    MouseListener[] mls_array =
        (MouseListener[]) (this.getListeners(MouseListener.class));
    MouseMotionListener[] mmls_array =
        (MouseMotionListener[]) (this.getListeners(MouseMotionListener.class));

    //
    // Remove non-DragGestureListener listeners
    //
    for (int i = 0; i < mls_array.length; i++) {
      if (!mls.contains(mls_array[i]) &&
          ! (mls_array[i] instanceof DragGestureRecognizer)) {
        mls.add(mls_array[i]);
        this.removeMouseListener(mls_array[i]);
      }
    }
    for (int i = 0; i < mmls_array.length; i++) {
      if (!mmls.contains(mmls_array[i]) &&
          ! (mmls_array[i] instanceof DragGestureRecognizer)) {
        mmls.add(mmls_array[i]);
        this.removeMouseMotionListener(mmls_array[i]);
      }
    }
  }

  /**
   * Restore mouse listeners to listener list.
   */
  private synchronized void restoreMouseListeners() {
    if (!mls.isEmpty()) {
      Iterator iter = mls.iterator();
      while (iter.hasNext()) {
        MouseListener me = (MouseListener) iter.next();
        this.addMouseListener( (MouseListener) me);
      }
    }
    mls.clear();

    if (!mmls.isEmpty()) {
      Iterator iter = mmls.iterator();
      while (iter.hasNext()) {
        MouseMotionListener me = (MouseMotionListener) iter.next();
        this.addMouseMotionListener( (MouseMotionListener) me);
      }
    }
    mmls.clear();
  }

  //
  // Protected Methods
  //

  /**
   * Informs listeners that a cut was appropriately done in cut mode.
   */
  protected void fireCutDoneinCutMode() {
    Object[] listeners = listenerList.getListenerList();
    CutandPasteModeEvent event = new CutandPasteModeEvent(this);
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == CutandPasteModeListener.class) {
        ( (CutandPasteModeListener) listeners[i + 1]).cutDoneinCutMode(event);
      }
    }
  }

  /**
   * Informs listeners that a cut was illegally attempted in paste mode.
   */
  protected void fireCutDoneinPasteMode() {
    Object[] listeners = listenerList.getListenerList();
    CutandPasteModeEvent event = new CutandPasteModeEvent(this);
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == CutandPasteModeListener.class) {
        ( (CutandPasteModeListener) listeners[i + 1]).cutDoneinPasteMode(event);
      }
    }
  }

  /**
   * Informs listeners that a paste was appropriately done in paste mode
   */
  protected void firePasteDoneinPasteMode() {
    Object[] listeners = listenerList.getListenerList();
    CutandPasteModeEvent event = new CutandPasteModeEvent(this);
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == CutandPasteModeListener.class) {
        ( (CutandPasteModeListener) listeners[i +
            1]).pasteDoneinPasteMode(event);
      }
    }
  }

  /**
   * Informs listeners that a paste was illegally attempted in cut mode
   */
  protected void firePasteDoneinCutMode() {
    Object[] listeners = listenerList.getListenerList();
    CutandPasteModeEvent event = new CutandPasteModeEvent(this);
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == CutandPasteModeListener.class) {
        ( (CutandPasteModeListener) listeners[i + 1]).pasteDoneinCutMode(event);
      }
    }
  }

  //
  // Autoscroll Implementation
  //

  /**
   * Provides insets that delineate the area of the table
   * in which autoscrolling should take place.
   * @return the {@link Insets}
   */
  public Insets getAutoscrollInsets() {

    //
    // Get insets of 12 pixels from the edge
    //
    Rectangle b = getBounds();
    Rectangle v = this.getVisibleRect();
    int top = v.y + 12;
    int lr = 12;
    int bottom = (b.height - (v.y + v.height)) + 12;
    autoscroll_insets.top = top;
    autoscroll_insets.left = lr;
    autoscroll_insets.bottom = bottom;
    autoscroll_insets.right = lr;
    return autoscroll_insets;

  }

  /**
   * Provides the auto scrolling functionality during a drag.
   * This method should be repeatedly called during the drag
   * operation if the mouse is within the area defined by
   * {@link #getAutoscrollInsets()}.
   * @param p the starting {@link Point}
   */
  public void autoscroll(Point p) {

    //
    // Scroll down
    //
    int size = this.getRowHeight() + this.getRowMargin();
    Rectangle rect = new Rectangle(p.x - size, p.y - size, size * 3, size * 3);
    this.scrollRectToVisible(rect);
  }

}