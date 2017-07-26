/*****************************************************************************
 * Package: gov.nih.nlm.util
 * Object:  Enscript
 *****************************************************************************/

package gov.nih.nlm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JEditorPane;

/**
 * Application entry point for printing HTML files.
 *
 * @author  MEME Group
 */

public class Enscript {

  /**
   * Print a file based on the parameters
   * @param s {@link String}<code>[]</code> which is
   * expected to have five parameters
   * <ol><li>s[0] = the file name</li>
   *     <li>s[1] = printer name (or "no printer")</li>
   *     <li>s[2] = title</li>
   *     <li>s[3] = pages per sheet (1 or 2)</li>
       *     <li>s[4] = indicates whether or not to wrap content with <html> tags.</li>
   *     <li>s[5] = indicates whether or not to scale the content.</li>
   *     <li>s[6] = scaling factor (higher number means smaller.</li>
   * </ol>
   */
  public static void main(String[] s) {

    //
    // Check parameters length
    //
    if (s.length != 7) {
      System.err.println("Bad number of arguments.");
      System.exit(1);
    }

    //
    // Handle parameters
    //
    String file = s[0];
    String printer_name = s[1];
    if (printer_name.equals("no printer")) {
      printer_name = null;
    }
    String title = s[2];
    int pages_per_sheet = Integer.valueOf(s[3]).intValue();
    boolean wrap_with_html = Boolean.valueOf(s[4]).booleanValue();
    boolean scale_width_to_fit = Boolean.valueOf(s[5]).booleanValue();
    double scale_factor = Double.valueOf(s[6]).doubleValue();

    //
    // Open specified file
    //
    File f = new File(file);
    if (f.exists()) {

      //
      // Read the file, if it exists
      //
      try {
        BufferedReader in =
            new BufferedReader(new InputStreamReader(new FileInputStream(f),
            "UTF-8"));
        StringBuffer sb = new StringBuffer(1000);
        String line = null;

        //
        // Wrap with HTML tags if parameter indicates it
        //
        if (wrap_with_html) {
          sb.append("<html><head><style type=\"text/css\">A {text-decoration: none} BODY {background-color: white}</style></head><body>");

        } while ( (line = in.readLine()) != null) {
          sb.append(line);
        }
        in.close();

        //
        // Wrap with HTML tags if parameter indicates it
        //
        if (wrap_with_html) {
          sb.append("</body></html>");

          //
          // Convert to string
          //
        }
        file = sb.toString();

      } catch (IOException ioe) {
        System.err.println("Error reading file: " + file);
        System.exit(1);
      } catch (Exception e) {
        System.err.println("Unexpected Exception");
        e.printStackTrace();
        System.exit(1);
      }

      //
      // Create the editor pane for printing
      //
      JEditorPane jep = new JEditorPane();
      jep.setContentType("text/html");
      jep.setText(SystemToolkit.removeLinks(file));

      //
      // Create the renderer, set preferences
      //
      HTMLDocumentRenderer hdr = new HTMLDocumentRenderer();
      hdr.setPrinterName(printer_name);
      hdr.setTitle(title);
      hdr.setScaleWidthToFit(scale_width_to_fit);
      hdr.setScalingFactor(scale_factor);
      if (pages_per_sheet == 1) {
        hdr.setOnePagePerSheet();
      } else if (pages_per_sheet == 2) {
        hdr.setTwoPagesPerSheet();

        //
        // Attempt to print the document
        //
      }
      try {
        int pages = hdr.print(jep);
        System.out.println(pages + " page(s) spooled to the printer");
      } catch (IllegalArgumentException iae) {
        System.err.println(iae.getMessage());
        String[] printers = hdr.getPrinterNames();
        System.err.println("Please use one of the following printers:");
        for (int i = 0; i < printers.length; i++) {
          System.err.println("  " + printers[i]);
        }
        System.exit(1);
      } catch (Exception e) {
        System.err.println("Unexpected Exception");
        e.printStackTrace();
        System.exit(1);
      }
    } else { // file not exist
      System.err.println("File does not exist: " + f.getName());
      System.exit(1);
    }

    //
    // Finished
    //
    System.exit(0);
  }
}