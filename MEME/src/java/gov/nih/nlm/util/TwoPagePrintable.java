/*****************************************************************************
 * Package: gov.nih.nlm.util
 * Object:  TwoPagePrintable
 *****************************************************************************/
package gov.nih.nlm.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import javax.swing.JEditorPane;
import javax.swing.RepaintManager;
import javax.swing.text.View;

/**
 * Handles printing of the contents of a {@link JEditorPane} using two pages
 * per sheet of paper.
 * This class implements the {@link Printable} interface, instances of this
 * class can also be used as the argument in the <code>setPrintable</code>
 * method of the PrinterJob class.
 *
 * @see HTMLDocumentRenderer
 *
 * @author  MEME Group
 */
public class TwoPagePrintable implements Printable {

  //
  // Fields
  //

  protected JEditorPane jeditor_pane; //Container to hold the
  //Document. This object will
  //be used to lay out the
  //Document for printing.
  protected double page_end_y = 0; //Location of the current page
  //end.
  protected double current_page = -1; //The current page

  protected double page_start_y = 0; //Location of the current page
  //start.
  protected int[] page_starts = new int[1000];
  protected int pages_printed = 0;
  protected String title = null;

  private boolean scale_width_to_fit = true; //boolean to allow control over
  //whether pages too wide to fit
  //on a page will be scaled.
  private double scaling_factor = 1.5;

  //
  // Constructors
  //

  /**
   * Instantiates the {@link TwoPagePrintable} using the
   * specified {@link JEditorPane} and {@link String} title.
   * @param jp the {@link JEditorPane} to print
   * @param title the title
   */
  public TwoPagePrintable(JEditorPane jp, String title) {
    jeditor_pane = jp;
    RepaintManager.currentManager(jp).setDoubleBufferingEnabled(false);
    page_starts[0] = 0;
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
   * prints two logical pages to the same sheet of paper with a title
   * bar and a vertical line separating the two pages.
   * @param graphics the {@link Graphics} representing the printer
   * @param page_format the {@link PageFormat}
   * @param page_index the page index
   * @return an <code>int</code> representation of printable status
   */
  public int print(Graphics graphics, PageFormat page_format, int page_index) {
    pages_printed = page_index;
    double scale = 1.0;
    Graphics2D graphics_2D;
    View root_view;

    // Assign/compute various margin variables
    int margin = 27;
    int content_margin = 36;
    int inner_margin = content_margin - margin;
    int top_margin = 45;
    int inner_title_margin = top_margin - margin;

    // Re-set the paper dimensions.
    Paper paper = page_format.getPaper();
    paper.setImageableArea(
        margin, margin,
        page_format.getWidth() - (margin * 2),
        page_format.getHeight() - (margin * 2));
    page_format.setPaper(paper);

    // Cast so that we have full Graphics2D API accessable.
    graphics_2D = (Graphics2D) graphics;

    //
    // Size the JEditorPane
    //
    jeditor_pane.setSize(
        (int) (page_format.getImageableWidth() * scaling_factor),
        Integer.MAX_VALUE);
    jeditor_pane.setMinimumSize(
        new Dimension(
        (int) (page_format.getImageableWidth() * scaling_factor),
        Integer.MAX_VALUE));
    jeditor_pane.validate();

    //
    // Obtain the root view for printing.
    //
    root_view = jeditor_pane.getUI().getRootView(jeditor_pane);

    //

    // Set the initial clipping area to imageable area.
    scale = 1;
    graphics_2D.setClip(
        (int) (page_format.getImageableX() / scale),
        (int) (page_format.getImageableY() / scale),
        (int) (page_format.getImageableWidth() / scale),
        (int) (page_format.getImageableHeight() / scale));

    //
    // Draw box around imagable area
    //
    BasicStroke bs = new BasicStroke(1);
    graphics_2D.setStroke(bs);
    graphics_2D.draw(graphics_2D.getClipBounds());

    //
    // Translate and rotate so that we are in upper left
    // corner of the page with a landscape view of the page
    //
    graphics_2D.translate(
        graphics_2D.getClipBounds().getX() +
        page_format.getImageableWidth() / scale,
        graphics_2D.getClipBounds().getY());
    graphics_2D.rotate(Math.toRadians(90));

    //
    // Draw a gray title box
    //
    Color c = graphics_2D.getColor();
    graphics_2D.setColor(Color.lightGray);
    graphics_2D.fill(
        new Rectangle(
        0, 0,
        (int) (page_format.getImageableHeight() / scale),
        (int) (inner_title_margin / scale)
        ));
    graphics_2D.setColor(c);
    graphics_2D.draw(
        new Rectangle(
        0, 0,
        (int) (page_format.getImageableHeight() / scale),
        (int) (inner_title_margin / scale)
        ));

    //
    // Draw the title and page number
    //
    Font f = graphics_2D.getFont();
    Font f2 = new Font("Serif", Font.BOLD, 10);
    int height = (int) f2.getLineMetrics("1",
                                         graphics_2D.getFontRenderContext()).
        getAscent();
    int baseline = (int) ( ( (inner_title_margin / scale) - height) / 2) +
        height;
    graphics_2D.setFont(f2);
    graphics_2D.drawString(title,
                           (int) (margin / scale), baseline);
    graphics_2D.drawString(String.valueOf(page_index + 1),
                           (int) ( (page_format.getImageableHeight() - margin) /
                                  scale),
                           baseline);
    graphics_2D.setFont(f);

    //
    // Translate to the upper left corner of the first content page on this sheet
    //
    graphics_2D.translate(
        inner_margin / scale, (inner_title_margin + inner_margin) / scale);
    graphics_2D.setClip(
        0, 0,
        (int) ( ( (page_format.getImageableHeight() / 2) - inner_margin) / scale),
        (int) ( (page_format.getImageableWidth() -
                 ( (2 * inner_margin) + inner_title_margin)) / scale));

    //
    //  Scale the graphics object
    //
    if (getScaleWidthToFit() &&
        jeditor_pane.getMinimumSize().getWidth() >
        ( (page_format.getImageableHeight() - (2 * inner_margin)) / 2)) {
      scale = ( (page_format.getImageableHeight() - (2 * inner_margin)) / 2) /
          jeditor_pane.getMinimumSize().getWidth();
      graphics_2D.scale(scale, scale);
    }

    //
    // Prepare document offsets (used to offset the editor pane
    //  to the page that we are printing).
    //
    page_end_y = graphics_2D.getClipBounds().getHeight();
    page_start_y = page_starts[page_index];

    //
    //  Allocate a region of JEditorPane to print
    //
    Rectangle allocation = new Rectangle(0,
                                         (int) - page_start_y,
                                         (int) (jeditor_pane.getMinimumSize().
                                                getWidth()),
                                         (int) (jeditor_pane.getPreferredSize().
                                                getHeight()));

    //
    // Print the view & return if no page was printed
    //
    if (!printView(graphics_2D, allocation, root_view)) {
      page_start_y = 0;
      page_end_y = 0;
      return Printable.NO_SUCH_PAGE;
    }

    //
    // Translate to center of page to draw vertical line
    //
    graphics_2D.translate(
        (int) ( ( (page_format.getImageableHeight() - (2 * inner_margin)) /
                 scale) / 2),
        - (inner_margin) / scale);
    graphics_2D.setClip(
        0, 0, 5, (int) ( (page_format.getImageableWidth()) / scale));

    //
    // Draw vertical line
    //
    bs = new BasicStroke(1);
    graphics_2D.setStroke(bs);
    graphics_2D.drawLine(
        0, 0,
        0,
        (int) ( (page_format.getImageableWidth() - inner_title_margin) / scale));

    //
    // Set page offsets again for second page of this sheet
    //
    page_start_y += page_end_y;
    page_end_y = graphics_2D.getClipBounds().getHeight();

    //
    // Translate to upper left hand corner of second content page
    //
    graphics_2D.translate(
        inner_margin / scale,
        (inner_margin) / scale);

    //
    // Clip to size of page
    //
    graphics_2D.setClip(
        0, 0,
        (int) ( ( (page_format.getImageableHeight() / 2) - inner_margin) / scale),
        (int) ( (page_format.getImageableWidth() -
                 ( (2 * inner_margin) + inner_title_margin)) / scale));

    //
    // Allocate region of JEditorPane to print
    //
    allocation = new Rectangle(0,
                               (int) - page_start_y,
                               (int) (jeditor_pane.getMinimumSize().getWidth()),
                               (int) (jeditor_pane.getPreferredSize().getHeight()));

    //
    // Print second page
    //
    printView(graphics_2D, allocation, root_view);

    //
    // Update page offsets for the next sheet. The offsets must
    // be saved in this way because the print method may be called
    // more than once, so we have to remember where we are for the
    // given page index.
    //
    page_starts[page_index + 1] = (int) (page_start_y + page_end_y);

    // If we got this far, a page exists.
    return Printable.PAGE_EXISTS;

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
    Rectangle clipRectangle = graphics_2D.getClipBounds();
    Shape childAllocation;
    View childView;

    if (view.getViewCount() > 0) {
      for (int i = 0; i < view.getViewCount(); i++) {
        childAllocation = view.getChildAllocation(i, allocation);
        if (childAllocation != null) {
          childView = view.getView(i);
          if (printView(graphics_2D, childAllocation, childView)) {
            page_exists = true;
          }
        }
      }
    } else {
      //  I
      if (allocation.getBounds().getMaxY() >= clipRectangle.getY()) {
        page_exists = true;
        //  II
        if ( (allocation.getBounds().getHeight() > clipRectangle.getHeight()) &&
            (allocation.intersects(clipRectangle))) {
          view.paint(graphics_2D, allocation);
        } else {
          //  III
          if (allocation.getBounds().getY() >= clipRectangle.getY()) {
            if (allocation.getBounds().getMaxY() <= clipRectangle.getMaxY()) {
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
   * Indicates whether or not to scale content to fit width of the page.
   * @return <code>true</code> if the content should be scaled,
   * <code>false</code> otherwise
   */
  public boolean getScaleWidthToFit() {
    return scale_width_to_fit;
  }

  /**
   * Sets the flag indiciating whether or not to scale content to fit
   * the width of the page
   * @param scale_width a <code>boolean</code> flag indicating whether or not to scale the content
   */
  public void setScaleWidthToFit(boolean scale_width) {
    scale_width_to_fit = scale_width;
  }

  /**
   * Set the scaling factor.
   * @param scaling_factor <code>double</code>
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
