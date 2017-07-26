/*****************************************************************************
 *
 * Package: gov.nih.nlm.util
 * Object:  OnePagePrintable
 *****************************************************************************/
package gov.nih.nlm.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RepaintManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.View;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Handles printing of the contents of a {@link JEditorPane},
 * {@link JTree}, and {@link JTable} using one page per sheet of paper.
 * This class implements the {@link Printable} interface, instances of this
 * class can also be used as the argument in the <code>setPrintable</code>
 * method of the PrinterJob class.
 *
 * @see HTMLDocumentRenderer
 *
 * @author  MEME Group
 */
public class OnePagePrintable implements Printable {

  //
  // Fields
  //

  // Used to keep track of when the page to print changes.
  protected int current_page = -1;

  // Item to print
  protected JEditorPane jeditor_pane;
  protected JTable jtable;
  protected JTree jtree;

  // Location of the current page end
  protected double page_end_y = 0;

  // Location of the current page start
  protected double page_start_y = 0;

  // number of pages printed
  protected int pages_printed = 0;

  // document title
  protected String title = null;

  // indicates whether or not to scale component down to fit
  private boolean scale_width_to_fit = true;

  // scaling factor (if not applying mechanism from above)
  private double scaling_factor = 1;

  //
  // Constructors
  //

  /**
   * Instantiates {@link OnePagePrintable} using the
   * specified {@link JEditorPane} and {@link String} title.
   * @param jp the {@link JEditorPane} to print
   * @param title the title
   */
  public OnePagePrintable(JEditorPane jp, String title) {
    jeditor_pane = jp;
    RepaintManager.currentManager(jp).setDoubleBufferingEnabled(false);
    if (title == null) {
      this.title = "untitled";
    } else {
      this.title = title;
    }
  }

  /**
   * Instantiates {@link OnePagePrintable} using the
   * specified {@link JTable} and {@link String} title.
   * @param jt the {@link JTable} to print
   * @param title the title
   */
  public OnePagePrintable(JTable jt, String title) {
    jtable = jt;
    RepaintManager.currentManager(jt).setDoubleBufferingEnabled(false);
    if (title == null) {
      this.title = "untitled";
    } else {
      this.title = title;
    }
  }

  /**
   * Instantiates {@link OnePagePrintable} using the
   * specified {@link JTree} and {@link String} title.
   * @param jt the {@link JTree} to print
   * @param title the title
   */
  public OnePagePrintable(JTree jt, String title) {
    jtree = jt;
    RepaintManager.currentManager(jt).setDoubleBufferingEnabled(false);
    if (title == null) {
      this.title = "untitled";
    } else {
      this.title = title;
    }
  }

  //
  // Methods
  //

  /**
   * Returns the number of pages printed.
   * @return the number of pages printed
   */
  public int getPagesPrinted() {
    return pages_printed;
  }

  /**
   * The print method implements the {@link Printable} interface. The code
   * prints one logical page per sheet of paper with a title
   * bar.
   * @param graphics the {@link Graphics} representing the printer
   * @param page_format the {@link PageFormat}
   * @param page_index the page index
   * @return an <code>int</code> representation of printable status
   */
  public int print(Graphics graphics, PageFormat page_format, int page_index) {

    JComponent comp = null;
    if (jtable != null) {
      comp = jtable;
    } else if (jtree != null) {
      comp = jtree;
    } else if (jeditor_pane != null) {
      comp = jeditor_pane;
      //
      // Size the JEditorPane
      //
      jeditor_pane.setSize(
          (int) (page_format.getImageableWidth() * 1.6),
          Integer.MAX_VALUE);
      jeditor_pane.setMinimumSize(
          new Dimension(
          (int) (page_format.getImageableWidth() * 1.6),
          Integer.MAX_VALUE));
      jeditor_pane.validate();
    }

    pages_printed = page_index;
    Graphics2D graphics_2D;

    //
    // Assign/compute margins
    //
    int margin = 27;
    int content_margin = 36;
    int inner_margin = content_margin - margin;
    int inner_title_margin = 18;

    //
    // Reset paper dimensions
    //
    Paper paper = page_format.getPaper();
    paper.setImageableArea(
        margin, margin,
        page_format.getWidth() - (margin * 2),
        page_format.getHeight() - (margin * 2));
    page_format.setPaper(paper);

    //
    // Cast so that we have full Graphics2D API accessible.
    //
    graphics_2D = (Graphics2D) graphics;

    //
    // Set the initial clipping area to imageable area.
    //
    graphics_2D.setClip(
        (int) page_format.getImageableX(),
        (int) page_format.getImageableY(),
        (int) page_format.getImageableWidth(),
        (int) page_format.getImageableHeight());

    //
    // Draw box around imagable area
    //
    BasicStroke bs = new BasicStroke(1);
    graphics_2D.setStroke(bs);
    graphics_2D.draw(graphics_2D.getClipBounds());

    //
    // Translate to the upper left hand corner
    //
    graphics_2D.translate(graphics_2D.getClipBounds().getX(),
                          graphics_2D.getClipBounds().getY());

    //
    // Draw a title box
    //
    Color c = graphics_2D.getColor();
    graphics_2D.setColor(Color.lightGray);
    graphics_2D.fill(
        new Rectangle(
        0, 0,
        (int) (page_format.getImageableWidth()),
        (int) (inner_title_margin)
        ));
    graphics_2D.setColor(c);
    graphics_2D.draw(
        new Rectangle(
        0, 0,
        (int) (page_format.getImageableWidth()),
        (int) (inner_title_margin)
        ));

    //
    // Draw the title and page number
    //
    Font f = graphics_2D.getFont();
    Font f2 = new Font("Serif", Font.BOLD, 10);
    int height = (int) f2.getLineMetrics("1",
                                         graphics_2D.getFontRenderContext()).
        getAscent();
    int baseline = (int) ( ( (inner_title_margin) - height) / 2) + height;
    graphics_2D.setFont(f2);
    graphics_2D.drawString(title,
                           (int) (margin), baseline);
    graphics_2D.drawString(String.valueOf(page_index + 1),
                           (int) ( (page_format.getImageableWidth() - margin)),
                           baseline);
    graphics_2D.setFont(f);

    //
    // Translate to upper left hand corner of content area
    //
    graphics_2D.translate(
        inner_margin, (inner_title_margin + inner_margin));
    graphics_2D.setClip(
        0, 0, (int) ( (page_format.getImageableWidth() - inner_margin)),
        (int) ( (page_format.getImageableHeight() -
                 ( (2 * inner_margin) + inner_title_margin))));

    //
    // Compute page offsets
    //
    if (page_index > current_page) {
      current_page = page_index;
      page_start_y += page_end_y;
      page_end_y = graphics_2D.getClipBounds().getHeight();
    }

    //
    // Scale the graphics object
    //
    double scale = scaling_factor;
    if (getScaleWidthToFit() &&
        comp.getMinimumSize().getWidth() >
        (page_format.getImageableWidth())) {
      scale = page_format.getImageableWidth() /
          (comp.getMinimumSize().getWidth() + 40);
      graphics_2D.scale(scale, scale);
    } else {
      graphics_2D.scale(1 / scale, 1 / scale);

    }
    if (jtable != null) {
      return printTable(graphics_2D, page_format, page_index);
    } else if (jtree != null) {
      return printTree(graphics_2D, page_format, page_index);
    } else if (jeditor_pane != null) {
      return printEditorPane(graphics_2D, page_format, page_index);
    }
    return NO_SUCH_PAGE;
  }

  /**
   * printView is a recursive method which iterates through the tree structure
   * of the view sent to it. If the view sent to printView is a branch view,
   * that is one with children, the method calls itself on each of these
       * children. If the view is a leaf view, that is a view without children which
   * represents an actual piece of text to be painted, printView attempts to
   * render the view to the {@link Graphics2D} object.
   *
   * I.    When any view starts after the beginning of the current printable
   *       page, this means that there are pages to print and the method sets
   *       page_exists to true.
   * II.   When a leaf view is taller than the printable area of a page, it
   *       cannot, of course, be broken down to fit a single page. Such a View
       *       will be printed whenever it intersects with the {@link Graphics2D} clip.
       * III.  If a leaf view intersects the printable area of the graphics clip and
   *       fits vertically within the printable area, it will be rendered.
   *
   *  @param graphics_2D An object {@link Graphics2D}
   *  @param allocation An object {@link Shape}
   *  @param view An object {@link View}
   *  @return A <code>boolean</code> representation of page status
   */
  protected boolean printView(Graphics2D graphics_2D, Shape allocation,
                              View view) {
    boolean page_exists = false;
    Rectangle clip_rectangle = graphics_2D.getClipBounds();
    Shape child_allocation;
    View child_view;

    if (view.getViewCount() > 0) {
      for (int i = 0; i < view.getViewCount(); i++) {
        child_allocation = view.getChildAllocation(i, allocation);
        if (child_allocation != null) {
          child_view = view.getView(i);
          if (printView(graphics_2D, child_allocation, child_view)) {
            page_exists = true;
          }
        }
      }
    } else {
      //  I
      if (allocation.getBounds().getMaxY() >= clip_rectangle.getY()) {
        page_exists = true;
        //  II
        if ( (allocation.getBounds().getHeight() > clip_rectangle.getHeight()) &&
            (allocation.intersects(clip_rectangle))) {
          view.paint(graphics_2D, allocation);
        } else {
          //  III
          if (allocation.getBounds().getY() >= clip_rectangle.getY()) {
            if (allocation.getBounds().getMaxY() <= clip_rectangle.getMaxY()) {
              view.paint(graphics_2D, allocation);
            } else {
              //  IV
              if (allocation.getBounds().getY() < page_end_y) {
                page_end_y = allocation.getBounds().getY();
              }
            }
          }
        }
      }
    }
    return page_exists;
  }

  /**
   * Used to print a {@link JEditorPane}.
   * @param graphics_2D the graphics object
   * @param page_format the page format
   * @param page_index the page index
   * @return indication of whether or not we have reached the last page
   */
  private int printEditorPane(Graphics2D graphics_2D, PageFormat page_format,
                              int page_index) {

    //
    // Obtain the root view
    //
    View root_view = jeditor_pane.getUI().getRootView(jeditor_pane);

    //
    // Allocate region of JEditorPane to print
    //
    Rectangle allocation = new Rectangle(0,
                                         (int) - page_start_y,
                                         (int) (jeditor_pane.getMinimumSize().
                                                getWidth()),
                                         (int) (jeditor_pane.getPreferredSize().
                                                getHeight()));

    //
    // Print this page and return if nothing printed
    //
    if (printView(graphics_2D, allocation, root_view)) {
      return Printable.PAGE_EXISTS;
    } else {
      page_start_y = 0;
      page_end_y = 0;
      current_page = -1;
      return Printable.NO_SUCH_PAGE;
    }
  }

  /**
   * Used to print a {@link JTree}
   * @param graphics_2D the graphics object
   * @param page_format the page format
   * @param page_index the page index
   * @return indication of whether or not we have reached the last page
   */
  private int printTree(Graphics2D graphics_2D, PageFormat page_format,
                        int page_index) {
    //
    // print as a container
    // Get all children, figure out how many are on each page.
    //
    int page_height = (int) graphics_2D.getClipBounds().getHeight();
    int row_height = (int) ( (DefaultTreeCellRenderer) jtree.getCellRenderer()).
        getPreferredSize().getHeight();
    int rows_per_page = (int) Math.floor(page_height / row_height);
    //int rem = page_height % row_height;
    int starting_y = (rows_per_page * row_height) * page_index;
    if (starting_y > jtree.getHeight()) {
      return NO_SUCH_PAGE;
    }
    graphics_2D.translate(0, -starting_y);

    graphics_2D.setClip(
        0, starting_y, (int) graphics_2D.getClipBounds().getWidth(),
        rows_per_page * row_height);

    jtree.paint(graphics_2D);

    return PAGE_EXISTS;
  }

  /**
   * Used to print a {@link JTable}
   * @param graphics_2D the graphics object
   * @param page_format the page format
   * @param page_index the page index
   * @return indication of whether or not we have reached the last page
   */
  private int printTable(Graphics2D graphics_2D, PageFormat page_format,
                         int page_index) {

    //
    // Print column headers and figure out how wide each column is
    //
    Font header_font = jtable.getFont().deriveFont(Font.BOLD);
    graphics_2D.setFont(header_font);
    FontMetrics fm = graphics_2D.getFontMetrics();
    graphics_2D.setColor(Color.black);

    TableColumnModel col_model = jtable.getColumnModel();
    int n_columns = col_model.getColumnCount();
    int x[] = new int[n_columns];
    x[0] = 0;

    int n_row = 0, n_col = 0, y = 10;
    for (n_col = 0; n_col < n_columns; n_col++) {
      TableColumn tk = col_model.getColumn(n_col);
      int width = tk.getWidth();
      if (x[n_col] + width > graphics_2D.getClipBounds().getWidth()) {
        n_columns = n_col;
        break;
      }
      if (n_col + 1 < n_columns) {
        x[n_col + 1] = x[n_col] + width;
      }
      String title = tk.getHeaderValue().toString();
      graphics_2D.drawString(title, x[n_col], y);
    }

    graphics_2D.setFont(jtable.getFont());
    fm = graphics_2D.getFontMetrics();
    int h = fm.getHeight();
    y += h; // from printing column header
    int header = y;
    int row_h = h; //Math.max( (int) (h * 1.5), 10);
    int row_per_page = (int) ( (graphics_2D.getClipBounds().getHeight() -
                                header) / row_h);

    int ini_row = page_index * row_per_page;
    int end_row = Math.min(jtable.getRowCount(),
                           ini_row + row_per_page);
    if (ini_row > end_row) {
      return NO_SUCH_PAGE;
    }
    for (n_row = ini_row; n_row < end_row; n_row++) {
      y += h;
      for (n_col = 0; n_col < n_columns; n_col++) {
        int col = jtable.getColumnModel().getColumn(n_col).getModelIndex();
        Object obj = jtable.getValueAt(n_row, col);
        String str = obj.toString();
        graphics_2D.setColor(Color.black);
        graphics_2D.drawString(str, x[n_col], y);
      }
    }

    System.gc();
    return PAGE_EXISTS;
  }

  /**
   * Indicates whether or not the content should be scaled to fit
   * the width of the page.
   * @return <code>true</code> if it should, <code>false</code> otherwise
   */
  public boolean getScaleWidthToFit() {
    return scale_width_to_fit;
  }

  /**
   * Sets the flag indicating whether or not to scale the content.
   * @param scale_width A <code>boolean</code> flag indiciating whether
   * or not to scale the content to fit the width of the page
   */
  public void setScaleWidthToFit(boolean scale_width) {
    scale_width_to_fit = scale_width;
  }

  /**
   * Set the scaling factor.
   * @param scaling_factor the scaling factor
   */
  public void setScalingFactor(double scaling_factor) {
    this.scaling_factor = scaling_factor;
  }

  /**
   * Returns the scaling factor.
   * @return the scaling factor
   */
  public double getScalingFactor() {
    return scaling_factor;
  }

}
