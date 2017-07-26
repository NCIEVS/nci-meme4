/*****************************************************************************
 * Package: gov.nih.nlm.util
 * Object:  HTMLDocumentRenderer
 *****************************************************************************/
package gov.nih.nlm.util;

import javax.print.PrintService;

import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.html.HTMLDocument;

/**
 * Handles printing of HTML documents. Text attributes, including
 * fonts, color, and small icons, will be rendered to a printed page.
 * {@link HTMLDocumentRenderer} computes line breaks, paginates, and performs
 * other formatting.
 *
 * An {@link HTMLDocument} is printed by sending it as an argument to the
 * print({@link HTMLDocument}) method. A {@link PlainDocument} is printed the
 * same way. Other types of {@link Document}s must be sent in a {@link JEditorPane} as
 * an argument to the print({@link JEditorPane}) method. Printing documents in
 * this way will automatically display a print dialog unless the printer name
 * has been specified.
 *
 * @author  MEME Group
 */
public class HTMLDocumentRenderer {

  //
  // Fields
  //

  protected JEditorPane jeditor_pane;

  // The {@link HTMLDocumentRenderer} class uses page format and printer job
  // in its methods. Note that page format is not the variable name used by the
  // print method of the {@link HTMLDocumentRenderer}. Although it would always
  // be expected to reference the page format object, the print method gets its
  // {@link PageFormat} as an argument.

  protected PageFormat p_format;
  protected PrinterJob p_job;
  protected PrintService p_service;
  protected String printer_name = null;
  protected int pages_per_sheet = 1;
  protected int pages_printed = 0;
  protected String title = null;
  protected boolean scale_width_to_fit = true;
  protected double scaling_factor = 1.0;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link HTMLDocumentRenderer}.
   */
  public HTMLDocumentRenderer() {
    //
    // Ready the page format and printer job
    //
    p_format = new PageFormat();
    p_job = PrinterJob.getPrinterJob();
  }

  //
  // Methods
  //

  /**
   * Set the scale width to fit flag.
   * @param scale_width_to_fit a flag indiciating whether or not to
   * scale the content to fit into the allowable width
   */
  public void setScaleWidthToFit(boolean scale_width_to_fit) {
    this.scale_width_to_fit = scale_width_to_fit;
  }

  /**
   * Indicates whether or not the document should be scaled to fit the
   * width of the page.
   * @return <code>true<code> if it should, <code>false</code> otherwise
   */
  public boolean getScaleWidthToFit() {
    return scale_width_to_fit;
  }

  /**
   * Sets the scaling factor.
   * @param scaling_factor the scaling factor (higher is smaller)
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

  /**
   * Sets the printer name.
   * @param printer_name the printer name
   */
  public void setPrinterName(String printer_name) {
    this.printer_name = printer_name;
  }

  /**
   * Returns the printer name.
   * @return the printer name
   */
  public String getPrinterName() {
    return printer_name;
  }

  /**
   * Returns the list of available printer names.
   * @return the list of available printer names
   */
  public String[] getPrinterNames() {
    PrintService[] p_services = PrinterJob.lookupPrintServices();
    String[] printer_names = new String[p_services.length];
    for (int i = 0; i < p_services.length; i++) {
      printer_names[i] = p_services[i].getName();
    }
    return printer_names;
  }

  /**
   * Sets the title.
   * @param title the title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the title.
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the number of pages per sheet.
   * @return the nubmer of pages per sheet
   */
  public int getPagesPerSheet() {
    return pages_per_sheet;
  }

  /**
   * Sets the number of pages per sheet to one (1).
   */
  public void setOnePagePerSheet() {
    pages_per_sheet = 1;
  }

  /**
   * Sets the number of pages per sheet to two (2).
   */
  public void setTwoPagesPerSheet() {
    pages_per_sheet = 2;
  }

  /**
   * Returns the current {@link Document}.
   * @return the current {@link Document}
   */
  public Document getDocument() {
    if (jeditor_pane != null) {
      return jeditor_pane.getDocument();
    } else {
      return null;
    }
  }

  /**
   * Displays a page setup dialog.
   */
  public void pageDialog() {
    p_format = p_job.pageDialog(p_format);
  }

  /**
   * Prints the specified {@link HTMLDocument}.
   * @param html_document the {@link HTMLDocument} to print
   * @return the number of pages printed
   * @throws PrinterException if failed to print document
   */
  public int print(HTMLDocument html_document) throws PrinterException {
    setDocument(html_document);
    return printDialog();
  }

  /**
   * Prints the specified {@link JEditorPane}.
   * @param jed_pane the {@link JEditorPane} to print
   * @return the number of pages printed
   * @throws PrinterException if failed to print document
   */
  public int print(JEditorPane jed_pane) throws PrinterException {
    setDocument(jed_pane);
    return printDialog();
  }

  /**
   * Prints the specified {@link PlainDocument}
   * @param plain_document the {@link PlainDocument} to print
   * @return the number of pages printed
   * @throws PrinterException if failed to print document
   */
  public int print(PlainDocument plain_document) throws PrinterException {
    setDocument(plain_document);
    return printDialog();
  }

  /**
   * Displays the print dialog and initiates
   * printing in response to user input.
   * @return the number of pages printed
   * @throws PrinterException if failed to print document
   */
  protected int printDialog() throws PrinterException {

    //
    // Create handlers for 1 or 2 pages per sheet
    //
    OnePagePrintable opp = null;
    TwoPagePrintable tpp = null;

    //
    // Configure the handler
    //
    if (getPagesPerSheet() == 1) {
      opp = new OnePagePrintable(jeditor_pane, title);
      opp.setScaleWidthToFit(scale_width_to_fit);
      opp.setScalingFactor(scaling_factor);
      p_job.setPrintable(opp, p_format);
    } else if (getPagesPerSheet() == 2) {
      tpp = new TwoPagePrintable(jeditor_pane, title);
      tpp.setScaleWidthToFit(scale_width_to_fit);
      tpp.setScalingFactor(scaling_factor);
      p_job.setPrintable(tpp, p_format);
    }

    //
    // If printer name was specified, look it up and use it
    //
    if (printer_name != null) {
      PrintService[] p_services = PrinterJob.lookupPrintServices();
      boolean found = false;
      for (int i = 0; i < p_services.length; i++) {
        if (p_services[i].getName().equals(printer_name)) {
          found = true;
          p_job.setPrintService(p_services[i]);
          p_job.print();
        }
      }
      if (!found) {
        throw new IllegalArgumentException("Printer does not exist: "
                                           + printer_name);
      }

      //
      // Printer name was not specified, show dialog
      //
    } else if (p_job.printDialog()) {
      p_job.print();
    }

    //
    // Obtain the number of pages printed
    //
    if (getPagesPerSheet() == 1) {
      pages_printed = opp.getPagesPrinted();
    } else if (getPagesPerSheet() == 2) {
      pages_printed = tpp.getPagesPrinted();
    }

    //
    // Done, return number pages printed
    //
    return pages_printed;
  }

  /**
   * Sets the content type the {@link JEditorPane}.
   * @param type the content type the {@link JEditorPane}
   */
  protected void setContentType(String type) {
    jeditor_pane.setContentType(type);
  }

  /**
   * Sets an {@link HTMLDocument} as the document to print.
   * @param html_doc the specified {@link HTMLDocument}
   */
  public void setDocument(HTMLDocument html_doc) {
    jeditor_pane = new JEditorPane();
    setDocument("text/html", html_doc);
  }

  /**
   * Sets the document to print as the one contained in a {@link JEditorPane}.
   * This method is useful when Java does not provide direct access to a
       * particular {@link Document} type, such as a Rich Text Format document. With
       * this method such a document can be sent to the {@link HTMLDocumentRenderer}
   * class enclosed in a {@link JEditorPane}.
   * @param jed_pane An object {@link JEditorPane}
   */
  public void setDocument(JEditorPane jed_pane) {
    jeditor_pane = new JEditorPane();
    setDocument(jed_pane.getContentType(), jed_pane.getDocument());
  }

  /**
   * Sets a {@link PlainDocument} as the {@link Document} to print.
   * @param plain_document the {@link PlainDocument} to print
   */
  public void setDocument(PlainDocument plain_document) {
    jeditor_pane = new JEditorPane();
    setDocument("text/plain", plain_document);
  }

  /**
   * Sets the content type and document of the {@link JEditorPane}.
   * @param type document type
   * @param document the {@link Document}
   */
  protected void setDocument(String type, Document document) {
    setContentType(type);
    jeditor_pane.setDocument(document);
  }

}
