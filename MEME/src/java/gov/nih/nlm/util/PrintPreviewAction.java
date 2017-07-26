/**
 *  Copyright 1999-2002 Matthew Robinson and Pavel Vorobiev.
 *  All Rights Reserved.
 *
 *  ===================================================
 *  This program contains code from the book "Swing"
 *  2nd Edition by Matthew Robinson and Pavel Vorobiev
 *  http://www.spindoczine.com/sbe
 *  ===================================================
 *
 *  The above paragraph must be included in full, unmodified
 *  and completely intact in the beginning of any source code
 *  file that references, copies or uses (in any way, shape
 *  or form) code contained in this file.
 */
package gov.nih.nlm.util;

import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.SwingToolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.MatteBorder;

/**
 * Frame for previewing stuff to print
 */
public class PrintPreviewAction extends AbstractAction implements
    WindowListener {

  //
  // Page width and height
  //
  protected int page_width;
  protected int page_height;

  //
  // scaling factor and GUI element
  //
  protected int scale;
  protected JComboBox scale_box;

  //
  // Container for preview pages
  //
  protected PreviewContainer preview_container;

  //
  // Frame (reference needed to repaint)
  //
  protected JFrame frame;

  //
  // Preview Frame Title
  //
  protected String title;

  //
  // Page Count Label
  //
  protected JLabel page_ct_label;

  //
  // Page index
  //
  protected int page_index = 0;
  protected int selected_page_index = -1;

  //
  // Printable to preview
  //
  protected Printable printable;

  //
  // Action for "Print" button
  //
  protected Action print_action;

  //
  // Scroll pane
  //
  protected JScrollPane ps;

  //
  // Zoom cursor
  //
  protected Cursor zoom_cursor = null;

  private GlassComponent gc = null;

  /**
   * Instantiates a {@link PrintPreviewAction} from the specified info.
   * @param icon_path path to the icon
   * @param print_action the print action
   */
  public PrintPreviewAction(String icon_path, Action print_action) {
    this.print_action = print_action;

    // configure action
    putValue(Action.NAME, "Print Preview");
    putValue(Action.SHORT_DESCRIPTION, "Print Preview");
    putValue(Action.LONG_DESCRIPTION, "Print Preview");
    putValue(Action.MNEMONIC_KEY, new Integer( (int) 'p'));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke('P', Event.CTRL_MASK));

    if (icon_path != null) {
      ImageIcon icon = null;
      try {
        URL gif_url = this.getClass().getClassLoader().getResource(
            icon_path);
        BufferedImage image = SwingToolkit.readScaledImage(gif_url, 17, false);
        icon = new ImageIcon(image);
      } catch (Exception ex) {
        throw new RuntimeException(
            "Scaled version of " + icon_path + " could not be loaded.");
      }
      putValue(Action.SMALL_ICON, icon);
    }

  }

  /**
   * Sets the title.
   * @param title the title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Sets the {@link Printable} to preview.
   * @param printable the {@link Printable} to preview
   */
  public void setPrintable(Printable printable) {
    this.printable = printable;
  }

  /**
   * Handles action.
   * @param ae the {@link ActionEvent}
   */
  public void actionPerformed(ActionEvent ae) {
    frame = new JFrame();
    frame.setTitle(title);
    frame.addWindowListener(this);

    //
    // Handle glass pane
    //
    gc = new GlassComponent(frame);
    frame.setGlassPane(gc);

    JToolBar tb = new JToolBar();
    JButton print_btn = new JButton(print_action);
    print_btn.setMargin(new Insets(2, 6, 2, 6));
    tb.add(print_btn);
    print_btn.setPreferredSize(print_btn.getPreferredSize());

    // Zoom - in button
    JButton zoom_in = new JButton();
    zoom_in.setMnemonic('+');
    ImageIcon clm_icon = null;
    BufferedImage clm_image = null;
    try {
      URL gif_url = this.getClass().getClassLoader().getResource(
          "config/ZoomIn24.gif");
      clm_image = SwingToolkit.readScaledImage(gif_url, 17, false);
      clm_icon = new ImageIcon(clm_image);
      zoom_cursor = Toolkit.getDefaultToolkit().createCustomCursor(
          SwingToolkit.readScaledImage(gif_url, 24, false), new Point(4, 4),
          "Zoom");
    } catch (Exception ex) {
      throw new RuntimeException(
          "Scaled version of config/ZoomIn24.gif could not be loaded.");
    }
    zoom_in.setIcon(clm_icon);
    zoom_in.setToolTipText("Zoom In");
    if (zoom_in.getIcon() != null) {
      zoom_in.setText(""); //an icon-only button
    }
    zoom_in.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gc.setVisible(true);
        scale = scale * 2;
        scale_box.setSelectedItem(scale + " %");
        gc.setVisible(false);
      }
    });
    tb.add(zoom_in);

    // Zoom -out button
    JButton zoom_out = new JButton();
    zoom_out.setMnemonic('-');
    try {
      URL gif_url = this.getClass().getClassLoader().getResource(
          "config/ZoomOut24.gif");
      clm_image = SwingToolkit.readScaledImage(gif_url, 17, false);
      clm_icon = new ImageIcon(clm_image);
    } catch (Exception ex) {
      throw new RuntimeException(
          "Scaled version of config/ZoomOut24.gif could not be loaded.");
    }
    zoom_out.setIcon(clm_icon);
    zoom_out.setToolTipText("Zoom Out");
    if (zoom_out.getIcon() != null) {
      zoom_out.setText(""); //an icon-only button
    }
    zoom_out.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gc.setVisible(true);
        scale = scale / 2;
        scale_box.setSelectedItem(scale + " %");
        gc.setVisible(false);
      }
    });
    tb.add(zoom_out);

    // Zoom pick-list
    String[] scales = {
        "10 %", "25 %", "50 %", "100 %", "150 %", "200 %"};
    scale_box = new JComboBox(scales);
    scale_box.setSelectedItem("100 %");
    scale = 100;
    scale_box.setToolTipText("Set Zoom");
    scale_box.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Thread runner = new Thread() {
          public void run() {
            String str = scale_box.getSelectedItem().toString();
            if (str.endsWith("%")) {
              str = str.substring(0, str.length() - 1);
            }
            str = str.trim();
            try {
              scale = Integer.parseInt(str);
            } catch (NumberFormatException ex) {
              return;
            }
            if (scale < 1) {
              scale_box.setSelectedItem("1 %");
              return;
            }
            if (scale > 400) {
              scale_box.setSelectedItem("400 %");
              return;
            }

            preview_container.removeAll();
            createComponents();
            preview_container.doLayout();
            preview_container.getParent().getParent().validate();
            preview_container.requestFocus();
            preview_container.scrollToSelectedPage();
          }
        };
        runner.start();
      }
    });
    scale_box.setMaximumSize(new Dimension(80, 20));
    scale_box.setEditable(true);
    tb.add(scale_box);
    tb.addSeparator();

    // Page count indicator
    page_ct_label = new JLabel();
    page_ct_label.setText("Page count: " + page_index);
    tb.add(page_ct_label);
    tb.addSeparator();

    JButton close_btn = new JButton(" Close ");
    close_btn.setMnemonic('c');
    close_btn.setToolTipText("Close");
    close_btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        frame.dispose();
      }
    });
    close_btn.setMargin(new Insets(2, 6, 2, 6));
    tb.add(close_btn);

    frame.getContentPane().add(tb, BorderLayout.NORTH);
    preview_container = new PreviewContainer();
    createComponents();

    ps = new JScrollPane(preview_container);
    ps.setPreferredSize(preview_container.getPreferredPageSize());

    frame.getContentPane().add(ps, BorderLayout.CENTER);

    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    frame.pack();
    frame.setVisible(true);
  }

  //
  // Create components for individual page views
  //
  protected void createComponents() {
    try {
      //
      // Get page format, scale page width and height
      //
      PrinterJob print_job = PrinterJob.getPrinterJob();
      PageFormat page_format = print_job.defaultPage();
      if (page_format.getHeight() == 0 || page_format.getWidth() == 0) {
        throw new RuntimeException("Unable to determine default page size");
      }
      page_width = (int) (page_format.getWidth());
      page_height = (int) (page_format.getHeight());
      final int w = (int) (page_width * scale / 100);
      final int h = (int) (page_height * scale / 100);

      //
      // Draw components into a buffered image
      //
      page_index = 0;
      while (true) {
        BufferedImage img = new BufferedImage(w, h,
                                              BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        g.scale( (scale / 100.0), (scale / 100.0));
        if (printable.print(g, page_format, page_index) !=
            Printable.PAGE_EXISTS) {
          break;
        }
        final PagePreview pp = new PagePreview(w, h, img);
        final int l_page_index = page_index;
        pp.setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
        pp.addMouseListener(new MouseAdapter() {
          public void mouseEntered(MouseEvent me) {
            if (scale < 100) {
              pp.setBorder(new MatteBorder(1, 1, 2, 2, Color.blue));
              pp.setToolTipText("Zoom to page " + (l_page_index + 1));
              pp.setCursor(zoom_cursor);
            }
          }

          public void mouseExited(MouseEvent me) {
            if (scale < 100) {
              pp.setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
            }
          }

          public void mouseClicked(MouseEvent me) {
            if (scale < 100) {
              selected_page_index = l_page_index;
              scale_box.setSelectedItem("100 %");
              pp.setCursor(Cursor.getDefaultCursor());
            }
          }
        });
        if (page_index == selected_page_index) {
          preview_container.setSelectedPage(pp);
          selected_page_index = -1;
        }
        preview_container.add(pp);
        page_index++;
      }

      page_ct_label.setText("Page count: " + page_index);
      frame.repaint();

    } catch (PrinterException e) {
      e.printStackTrace();
    }
  }

  public void windowActivated(WindowEvent we) {

  }

  public void windowDeactivated(WindowEvent we) {

  }

  public void windowClosed(WindowEvent we) {

  }

  public void windowClosing(WindowEvent we) {

  }

  public void windowOpened(WindowEvent we) {

  }

  public void windowDeiconified(WindowEvent we) {

  }

  public void windowIconified(WindowEvent we) {

  }

  /**
   * Inner class for showing preview.
   */
  class PreviewContainer extends JPanel {

    protected int H_GAP = 16;
    protected int V_GAP = 10;
    protected PagePreview selected_page = null;

    public Dimension getPreferredPageSize() {
      int n = getComponentCount();
      if (n == 0) {
        return new Dimension(H_GAP, V_GAP);
      }
      Component comp = getComponent(0);
      Dimension d = comp.getPreferredSize();
      return new Dimension( ( (int) (d.getWidth() + (H_GAP * 3))),
                           ( (int) (d.getHeight() + (V_GAP * 3))));
    }

    public Dimension getPreferredSize() {
      int n = getComponentCount();
      if (n == 0) {
        return new Dimension(H_GAP, V_GAP);
      }
      Component comp = getComponent(0);
      Dimension dc = comp.getPreferredSize();
      int w = dc.width;
      int h = dc.height;

      Dimension dp = getParent().getSize();
      int nCol = Math.max( (dp.width - H_GAP) / (w + H_GAP), 1);
      int nRow = n / nCol;
      if (nRow * nCol < n) {
        nRow++;

      }
      int ww = nCol * (w + H_GAP) + H_GAP;
      int hh = nRow * (h + V_GAP) + V_GAP;
      Insets ins = getInsets();
      return new Dimension(ww + ins.left + ins.right,
                           hh + ins.top + ins.bottom);
    }

    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public void doLayout() {
      Insets ins = getInsets();
      int x = ins.left + H_GAP;
      int y = ins.top + V_GAP;

      int n = getComponentCount();
      if (n == 0) {
        return;
      }
      Component comp = getComponent(0);
      Dimension dc = comp.getPreferredSize();
      int w = dc.width;
      int h = dc.height;

      Dimension dp = getParent().getSize();
      int nCol = Math.max( (dp.width - H_GAP) / (w + H_GAP), 1);
      int nRow = n / nCol;
      if (nRow * nCol < n) {
        nRow++;

      }
      int index = 0;
      for (int k = 0; k < nRow; k++) {
        for (int m = 0; m < nCol; m++) {
          if (index >= n) {
            return;
          }
          comp = getComponent(index++);
          comp.setBounds(x, y, w, h);
          x += w + H_GAP;
        }
        y += h + V_GAP;
        x = ins.left + H_GAP;
      }
    }

    public void setSelectedPage(PagePreview pp) {
      selected_page = pp;
    }

    public void scrollToSelectedPage() {
      if (selected_page != null) {
        this.scrollRectToVisible(selected_page.getBounds());
      }
    }

  }

  /**
   * Preview of a single page.
   */
  class PagePreview extends JPanel {
    protected int m_w;
    protected int m_h;
    protected Image m_source;
    protected Image m_img;

    public PagePreview(int w, int h, Image source) {
      m_w = w;
      m_h = h;
      m_source = source;
      m_img = source;
      m_img.flush();
      setBackground(Color.white);
      setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
    }

    public void setScaledSize(int w, int h) {
      m_w = w;
      m_h = h;
      m_img = m_source;
      //repaint();
    }

    public Dimension getPreferredSize() {
      Insets ins = getInsets();
      return new Dimension(m_w + ins.left + ins.right,
                           m_h + ins.top + ins.bottom);
    }

    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public void paint(Graphics g) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
      g.drawImage(m_img, 0, 0, this);
      paintBorder(g);
    }
  }

}