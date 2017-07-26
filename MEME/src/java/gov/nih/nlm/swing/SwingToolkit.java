/*****************************************************************************
 *
 * Package: gov.nih.nlm.swing
 * Object:  SwingToolkit
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import gov.nih.nlm.util.BrowserLauncher;
import gov.nih.nlm.util.SystemToolkit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * GUI utility class. It provides several constants for useful
 * GUI elements, as well as methods for reporting errors, informing uses
 * of information, or requesting feedback from a user.
 *
 * @author  MEME Group
 */
public abstract class SwingToolkit implements SwingConstants {

  //
  // Fields
  //
  private static Properties properties = new Properties();
  protected static Map key_map = new HashMap();

  //
  // Methods
  //

  /**
   * Sets a property.
   * @param name the property name
   * @param value the property value
   */
  public static void setProperty(String name, String value) {
    properties.setProperty(name, value);
  }

  /**
   * Sets the toolkit properties.  This can be used to override
   * the properties set in the call to <code>initialize</code>.
   * @param props a set of new {@link Properties}
   */
  public static void setProperties(Properties props) {
    properties = props;
  }

  /**
   * Returns the property value for the specified name.
   * @param name the property name
   * @return the property value, or null if it is not found
   */
  public static String getProperty(String name) {
    return getProperty(name, null);
  }

  /**
       * Returns the property value for the specified name.  If the property does not
   * exist, the specified default value is returned instead.
   * @param name the property name
   * @param _default the default value if the property value is null
       * @return the property value, or the default value if the property value is null
   */
  public static String getProperty(String name, String _default) {
    return (properties.getProperty(name, _default));
  }

  /**
   * Remove a property.
   * @param name the property name.
   */
  public static void removeProperty(String name) {
    properties.remove(name);
  }

  /**
   * Returns the toolkit {@link Properties}.
   * @return the toolkit {@link Properties}
   */
  public static Properties getProperties() {
    return properties;
  }

  /**
   * Indicates whether or not the application is using a GUI. This
   * is based on the value of the {@link SwingConstants#VIEW} property.
   * @return <code>true</code> if the current application is using a view,
   *  <code>false</code> otherwise
   */
  public static boolean usingView() {
    return Boolean.valueOf(getProperty(VIEW)).booleanValue();
  }

  /**
   * Reports an error message to the user.  The error is either
   * written to STDERR or shown in a dialog box, depending upon
   * the return value of {@link #usingView()}.
   * @param error an error message
   */
  public static void reportError(String error) {
    reportError(null, error, false);
  }

  /**
   * Reports an error message to the user.  The error is either
   * written to STDERR or shown in a dialog box, depending upon
   * the return value of {@link #usingView()}.
   * @param error an error message
   * @param fatal a flag indicating whether or not the error is fatal
   */
  public static void reportError(String error, boolean fatal) {
    reportError(null, error, fatal);
  }

  /**
   * Reports an error message to the user.  The error is either
   * written to STDERR or shown in a dialog box, depending upon
   * the return value of {@link #usingView()}.
   * @param parent the parent component
   * @param error an error message
   * @param fatal a flag indicating whether or not the error is fatal
   */
  public static void reportError(Component parent, String error, boolean fatal) {
    if (fatal) {
      error = error + " Exiting . . .";
    }
    ;
    if (usingView()) {
      JOptionPane pane = new JOptionPane(error, JOptionPane.ERROR_MESSAGE);
      JDialog dialog = pane.createDialog(parent, "Error Report");
      FontSizeManager.adjustFontSize(dialog);
      dialog.pack();
      dialog.setVisible(true);
    } else {
      System.err.println("Error: " + error);
      System.err.flush();
    }
    if (fatal) {
      Exit(1);
    }
  }

  /**
   * Asks the user a (yes/no) question. Calls
   * {@link #confirmRequest(Component, String)} with a null <code>parent</code>
   * @param request the (yes/no) question
   * @return <code>true</code> if yes, <code>false</code> if no
   */
  public static boolean confirmRequest(String request) {
    return confirmRequest(null, request);
  }

  /**
   * Asks the user a (yes/no) question.  If {@link #usingView()}
   * returns true, this method opens a dialog box with a message and a
   * (yes/no) prompt for the user.  If {@link #usingView()} returns false,
   * it prompts the user on the command line.
   * @param parent the parent {@link Component} of the dialog box
   * @param request the (yes/no) question
   * @return <code>true</code> if yes; <code>false</code> if no
   */
  public static boolean confirmRequest(Component parent, String request) {
    if (usingView()) {
      // Open a dialog box, return results
      JOptionPane pane =
          new JOptionPane(request, JOptionPane.QUESTION_MESSAGE,
                          JOptionPane.YES_NO_OPTION);
      JDialog dialog = pane.createDialog(parent, "Question");
      FontSizeManager.adjustFontSize(dialog);
      dialog.pack();
      dialog.setVisible(true);
      int response = pane.getValue() != null ?
          ( (Integer) pane.getValue()).intValue() : JOptionPane.NO_OPTION;
      if (response == JOptionPane.YES_OPTION) {
        return true;
      } else if (response == JOptionPane.NO_OPTION) {
        return false;
      }
    } else {
      System.out.println(request);
      System.out.print("[Answer yes or no.] ");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String s;
      while (true) {
        try {
          s = in.readLine();
          if (s.equals("yes")) {
            return true;
          } else if (s.equals("no")) {
            return false;
          } else {
            System.out.print("[Please answer yes or no.] ");
          }
        } catch (Exception e) {
          reportError("Problem getting an answer to a request, try again");
          return confirmRequest(request);
        }
      }
    }
    return false;
  }

  /**
   * Asks the user a (yes/no) question.  If {@link #usingView()}
   * returns true, this method opens a dialog box with a message and a
   * (yes/no) prompt for the user.  If {@link #usingView()} returns false,
   * it prompts the user on the command line.
   * @param parent the parent {@link Component} of the dialog box
   * @param request the (yes/no) question
   * @param key used to differentiate this request from others
   * @return <code>true</code> if yes; <code>false</code> if no
   */
  public static boolean confirmRequestWithOption(Component parent,
                                                 String request,
                                                 String key) {
    if (usingView()) {
      final JCheckBox checkbox = new JCheckBox("Don't show this message again.");
      Object[] request_array = new Object[] {
          request, checkbox};
      if (key_map.containsKey(key)) {
        if ( ( ( (Integer) key_map.get(key)).intValue()) ==
            JOptionPane.YES_OPTION) {
          return true;
        } else {
          return false;
        }
      }

      // Open a dialog box, return results
      final JOptionPane pane =
          new JOptionPane(request_array, JOptionPane.QUESTION_MESSAGE,
                          JOptionPane.YES_NO_OPTION);
      final JDialog dialog = pane.createDialog(parent, "Question");
      final String final_key = key;
      FontSizeManager.adjustFontSize(dialog);
      pane.addPropertyChangeListener(
          new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();

          if ( (e.getSource() == pane)
              && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
            if (checkbox.isSelected()) {
              key_map.put(final_key, ( (Integer) pane.getValue()));
            }
            dialog.setVisible(false);
          }
        }
      });
      dialog.pack();
      dialog.setVisible(true);
      int response = pane.getValue() != null ?
          ( (Integer) pane.getValue()).intValue() : JOptionPane.NO_OPTION;
      if (response == JOptionPane.YES_OPTION) {
        return true;
      } else if (response == JOptionPane.NO_OPTION) {
        return false;
      }
    } else {
      System.out.println(request);
      System.out.print("[Answer yes or no.] ");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String s;
      while (true) {
        try {
          s = in.readLine();
          if (s.equals("yes")) {
            return true;
          } else if (s.equals("no")) {
            return false;
          } else {
            System.out.print("[Please answer yes or no.] ");
          }
        } catch (Exception e) {
          reportError("Problem getting an answer to a request, try again");
          return confirmRequest(request);
        }
      }
    }
    return false;
  }

  /**
       * Notifies the user of something. Calls {@link #notifyUser(Component, String)}
   * with a null <code>parent</code>.
   * @param request the message
   */
  public static void notifyUser(String request) {
    notifyUser(null, request, null);
  }

  /**
   * Notifies the user of something.  If {@link #usingView()} returns
   * <code>true</code>, this method opens a dialog box informing the
   * user of the state of something .  It returns upon an OK click or close.
   * If {@link #usingView()} returns <code>false</code>, it notifies the
   * user on the command line.
   * @param parent the parent {@link Component}
   * @param message the message
   */
  public static void notifyUser(Component parent, String message) {
    notifyUser(parent, message, null);
  }

  /**
   * Notifies the user of something.  If {@link #usingView()} returns
   * <code>true</code>, this method opens a dialog box informing the
   * user of the state of something .  It returns upon an OK click or close.
   * If {@link #usingView()} returns <code>false</code>, it notifies the
   * user on the command line.
   * @param parent the parent {@link Component}
   * @param message the message
   * @param title for the dialog box
   */
  public static void notifyUser(Component parent, String message, String title) {
    if (usingView()) {
      JOptionPane pane = new JOptionPane(message,
                                         JOptionPane.INFORMATION_MESSAGE);
      JDialog dialog;
      if (title != null && !title.equals("")) {
        dialog = pane.createDialog(parent, title);
      } else {
        dialog = pane.createDialog(parent, "Notify User");
      }
      FontSizeManager.adjustFontSize(dialog);
      dialog.pack();
      dialog.setVisible(true);
    } else {
      System.out.println(message);
      System.out.println("[Press <return> to continue.]");
      InputStreamReader in = new InputStreamReader(System.in);
      try {
        // just read a <return>
        in.read();
      } catch (IOException e) {
        // Do nothing
      }
    }
  }

  /**
   * Notifies the user of something.  If {@link #usingView()} returns
   * <code>true</code>, this method opens a dialog box informing the
   * user of the state of something .  It returns upon an OK click or close.
   * If {@link #usingView()} returns <code>false</code>, it notifies the
   * user on the command line.  Will not display if user has previously
   * checked the {@link JCheckBox} "Don't show this message again." from the
   * calling instance indicated by the key parameter.
   * @param parent the parent {@link Component}
   * @param message the message
   * @param title for the dialog box
   * @param key to differentiate calling instances
   */
  public static void notifyUserWithOption(Component parent, String message,
                                          String title, String key) {
    if (usingView()) {
      final JCheckBox checkbox = new JCheckBox("Don't show this message again.");
      Object[] message_array = new Object[] {
          message, checkbox};
      if (key_map.containsKey(key)) {
        return;
      }
      final JOptionPane pane = new JOptionPane(message_array,
                                               JOptionPane.INFORMATION_MESSAGE);
      final JDialog dialog;
      final String final_key = key;
      if (title != null && !title.equals("")) {
        dialog = pane.createDialog(parent, title);
      } else {
        dialog = pane.createDialog(parent, "Notify User");
      }
      FontSizeManager.adjustFontSize(dialog);

      pane.addPropertyChangeListener(
          new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();

          if ( (e.getSource() == pane)
              && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
            if (checkbox.isSelected()) {
              key_map.put(final_key, null);
            }
            dialog.setVisible(false);
          }
        }
      });

      dialog.pack();
      dialog.setVisible(true);

    } else {
      System.out.println(message);
      System.out.println("[Press <return> to continue.]");
      InputStreamReader in = new InputStreamReader(System.in);
      try {
        // just read a <return>
        in.read();
      } catch (IOException e) {
        // Do nothing
      }
    }
  }

  /**
   * Displays a text file.  If {@link #usingView()} returns
   * <code>true</code>, this method opens a dialog box displaying the
   * given {@link File}.  It returns upon an OK click or close.
       * If {@link #usingView()} returns <code>false</code>, it displays the text on
   * the command line.
   * @param parent the parent {@link Component}
   * @param title title for the display
   * @param file the message
   */
  public static void displayTextFile(Component parent, String title, File file) {
    displayTextFile(parent, title, file, 20, 90);
  }

  /**
   * Displays a text file.  If {@link #usingView()} returns
   * <code>true</code>, this method opens a dialog box displaying the
   * given {@link File}.  It returns upon an OK click or close.
       * If {@link #usingView()} returns <code>false</code>, it displays the text on
   * the command line.
   * @param parent the parent {@link Component}
   * @param title title for the display
   * @param file the message
       * @param rows the number of rows to be displayed at one time without scrolling
   * @param cols the number of columns to be displayed at one time without scrolling
   */
  public static void displayTextFile(Component parent, String title, File file,
                                     int rows, int cols) {
    if (usingView()) {
      BufferedReader reader = null;
      JTextArea text_area = new JTextArea(rows, cols);
      text_area.setEditable(false);
      text_area.setFont(new Font("Courier", Font.PLAIN, 12));
      try {
        reader = new BufferedReader(new FileReader(file));
        String line = "";
        while (true) {
          line = reader.readLine();
          if (line == null) {
            break;
          }
          text_area.append(line);
          text_area.append("\n");
        }
        text_area.setCaretPosition(0);
        reader.close();
      } catch (Exception ex) {
        notifyUser(ex.getMessage());
      }

      JOptionPane pane = new JOptionPane(
          new JScrollPane(text_area), JOptionPane.INFORMATION_MESSAGE);
      JDialog dialog = pane.createDialog(parent, title);
      FontSizeManager.adjustFontSize(dialog);
      dialog.pack();
      dialog.setVisible(true);
      dialog.toFront();
    } else {
      System.out.println("Please see the file: " + file.getAbsolutePath());
      /*System.out.println("[Press <return> to continue.]");
             InputStreamReader in = new InputStreamReader(System.in);
             try {
        // just read a <return>
        in.read();
             } catch (IOException e) {
        // Do nothing
             }*/
    }
  }

  /**
   * Displays the file at a given URL.  If {@link #usingView()} returns
   * <code>true</code>, this method opens a dialog box displaying the
   * given {@link URL}.  It returns upon an OK click or close.
   * If {@link #usingView()} returns <code>false</code>, nothing happens.
   * @param parent the parent {@link Component}
   * @param title title for the display
   * @param url {@link URL} to be displayed
   */
  public static void displayURL(Component parent, String title, URL url) {
    if (usingView()) {
      HtmlViewer html_viewer = new HtmlViewer();
      html_viewer.setPage(url);
      final JPanel base_panel = new JPanel();
      base_panel.setLayout(new BorderLayout());
      base_panel.add(html_viewer, BorderLayout.CENTER);

      final JFrame url_frame = new JFrame(title);

      JButton done_button = new JButton("Done");
      done_button.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          url_frame.dispose();
        }
      });
      base_panel.add(done_button, BorderLayout.SOUTH);

      FontSizeManager.addContainer(base_panel);
      url_frame.setContentPane(base_panel);
      url_frame.pack();
      url_frame.setLocationRelativeTo(null);
      url_frame.setVisible(true);
    }
  }

  /**
   * Asks the user a question. Calls {@link #getUserInput(Component, String)}
   * with a null <code>parent</code>.
   * @param request the message
   * @return the user response
   */
  public static String getUserInput(String request) {
    return getUserInput(null, request);
  }

  /**
   * Asks the user a question.  If {@link #usingView()} returns
   * <code>true</code>, this method opens a dialog box with the specified
   * question and "OK"/"Cancel" buttons. If {@link #usingView()} returns
   * <code>false</code>, a response from the user is solicited on the
   * command line.
   * @param parent the parent {@link Component}
   * @param message the message
   * @return the user response
   */
  public static String getUserInput(Component parent, String message) {
    if (usingView()) {
      JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE);
      pane.setWantsInput(true);
      JDialog dialog = pane.createDialog(parent, "Input");
      FontSizeManager.adjustFontSize(dialog);
      dialog.pack();
      dialog.setVisible(true);
      Object selectedValue = pane.getInputValue();
      return selectedValue.toString();
    } else {
      System.out.println(message);
      System.out.println("[Enter your response and press <return>]");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      try {
        return (in.readLine());
      } catch (Exception e) {
        reportError(
            "Problem getting an answer to a request, click OK to try again.");
        return getUserInput(message);
      }
    }
  }

  /**
   * Asks the user a question that involves a potentially multi-line
   * answer.  Calls {@link #getMultiLineUserInput(Component, String)} with
   * a null <code>parent</code>
   * @param request the request
   * @return the user response
   */
  public static String getMultiLineUserInput(String request) {
    return getMultiLineUserInput(null, request);
  }

  /**
   * Asks the user a question that involves a potentially multi-line
   * answer.  If {@link #usingView()} returns <code>true</code>, this
   * method opens a dialog box asking the user a question and
   * soliciting a multi-line response. If {@link #usingView()} returns
   * <code>false</code>,  a response from the user is solicited on the
   * command line.
   * @param parent the parent {@link Component}
   * @param message the message
   * @return the user response
   */
  public static String getMultiLineUserInput(Component parent, String message) {
    if (usingView()) {
      return MultiLineInputDialog.showDialog(parent, message, "Get User Input",
                                             "");
    } else {
      System.out.println(message);
      System.out.println(
          "[Type 'done' and press <return> to complete your response]");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      StringBuffer sb = new StringBuffer();
      String line;
      try {
        while ( (line = in.readLine()) != null) {
          if (line.equals("done")) {
            break;
          } else {
            sb.append(line);
          }
        }
        return sb.toString();
      } catch (Exception e) {
        reportError("Problem getting an answer to a request, try again.");
        return getMultiLineUserInput(message);
      }
    }
  }

  /**
   * Copies the input file to the output file.  This method makes use
   * of a progress monitor (if usingView() returnes true).
   * @param in the input {@link File}
   * @param out the output {@link File}
   * @return <code>true</code> if the copying was successful, false otherwise.
   * @throws IOException
   */
  public static boolean copy(File in, File out) throws IOException {

    //
    // Declare variables
    //
    ProgressMonitor pm = null;
    boolean using_view = usingView();
    File canonical_in = in.getAbsoluteFile();
    File canonical_out = out.getAbsoluteFile();

    //
    // Set up progress monitor if using view
    //
    if (using_view) {
      String title = "Copy " + canonical_in + " to " + canonical_out;
      String initial_msg = "Copying ... ";
      pm = new ProgressMonitor(null, title, initial_msg, 0, 100);
      pm.setMillisToDecideToPopup(10);
      pm.setMillisToPopup(2);
      pm.setProgress(0);
      pm.setMaximum(100);
    } else {
      System.out.println("Copy " + canonical_in + " to " + canonical_out);
    }

    //
    // Perform copy
    //
    SystemToolkit.copy(canonical_in, canonical_out, pm);

    boolean ret_val = true;
    if (using_view) {
      ret_val = !pm.isCanceled();
      pm.close();
    }
    return ret_val;
  }

  /**
   * Computes the MD5 of the specified file.
   * @param in the {@link File}
   * @return the MD5 value, or null if cancelled.
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static String md5(File in) throws IOException,
      NoSuchAlgorithmException {

    //
    // Declare variables
    //
    ProgressMonitor pm = null;
    boolean using_view = usingView();
    File canonical_in = in.getAbsoluteFile();

    //
    // Set up progress monitor if using view
    //
    if (using_view) {
      String title = "Validating " + canonical_in + " ... ";
      String initial_msg = "Validating ... ";
      pm = new ProgressMonitor(null, title, initial_msg, 0, 100);
      pm.setMillisToDecideToPopup(10);
      pm.setMillisToPopup(2);
      pm.setProgress(0);
      pm.setMaximum(100);
    } else {
      System.out.println("Validating " + canonical_in + " ... ");
    }

    //
    // Perform copy
    //
    String md5 = SystemToolkit.md5(canonical_in, pm);

    if (using_view) {
      if (pm.isCanceled()) {
        md5 = null;
      }
      pm.close();
    }
    return md5;
  }

  /**
   * Unzips the specified file into the specified directory using
   * the specified operating system command.  This method makes use
   * of a progress monitor (if usingView() returns true).
   * @param zip_file file to be unzipped
   * @param output_dir location for unzipped files
   * @param archive_subdir the portion of the archive to extract
       * @return <code>true</code> if the unzipping was successful, false otherwise.
   * @throws IOException
   */
  public static boolean unzip(final String zip_file,
                              final String output_dir,
                              final String archive_subdir) throws IOException {
    //
    // Declare variables
    //
    ProgressMonitor pm = null;
    boolean using_view = usingView();

    //
    // Set up progress monitor if using view
    //
    if (using_view) {
      String title = "Process " + zip_file;
      String initial_msg = "Starting ... ";
      pm = new ProgressMonitor(null, title, initial_msg, 0, 100);
      pm.setMillisToDecideToPopup(2);
      pm.setProgress(0);
    } else {
      System.out.println("Process " + zip_file);
    }

    //
    // Perform unzip
    //
    SystemToolkit.unzip(zip_file, output_dir, archive_subdir, pm);

    boolean ret_val = true;
    if (using_view) {
      ret_val = !pm.isCanceled();
      pm.close();
    }
    return ret_val;
  }

  /**
   * Unzips the specified file into the specified directory using
   * the specified operating system command.  This method makes use
   * of a progress monitor (if usingView() returns true).
   * @param unzip_cmd the OS command to use to unzip
   * @param zip_file file to be unzipped
   * @param output_dir location for unzipped files
   * @param archive_subdir the portion of the archive to extract
       * @return <code>true</code> if the unzipping was successful, false otherwise.
   * @throws IOException
   */
  public static boolean unzip(final String unzip_cmd, final String zip_file,
                              final String output_dir,
                              final String archive_subdir) throws IOException {

    //
    // Declare variables
    //
    ProgressMonitor pm = null;
    boolean using_view = usingView();

    //
    // Set up progress monitor if using view
    //
    if (using_view) {
      String title = "Process " + zip_file;
      String initial_msg = "Starting ... ";
      pm = new ProgressMonitor(null, title, initial_msg, 0, 100);
      pm.setMillisToDecideToPopup(2);
      pm.setProgress(0);
    } else {
      System.out.println("Process " + zip_file);
    }

    //
    // Perform unzip
    //
    SystemToolkit.unzip(unzip_cmd, zip_file, output_dir, archive_subdir, pm);

    boolean ret_val = true;
    if (using_view) {
      ret_val = !pm.isCanceled();
      pm.close();
    }
    return ret_val;
  }

  /**
   * Cleans up loose ends before calling <code>System.exit(int)</code>.
   * All applications should call <code>SwingToolkit.Exit(0)</code> instead of
   * calling <code>System.exit(0)</code>.
   * @param return_value the exit code. 0 typically means no error.
   */
  public static void Exit(int return_value) {
    System.exit(return_value);
  }

  /**
   * Returns a {@link JPanel} displaying the contents
   * of the specified url {@link URL}.
   * @param url to display
   * @return a {@link JPanel}
   */
  public static JPanel getPanelForHTML(URL url) {
    HtmlViewer html_viewer = new HtmlViewer();
    html_viewer.setPage(url);
    html_viewer.getEditorPane().addHyperlinkListener(
        new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent he) {
        if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          final String dsc = he.getDescription();
          URL local_url = null;
          try {
            local_url = new URL(dsc);
          } catch (Exception e) {
            notifyUser("Error with URL " + dsc);
          }
          final URL url = local_url;
          Thread t = new Thread(new Runnable() {
            public void run() {
              try {
                BrowserLauncher.openURL(dsc);
              } catch (IOException ioe) {
                SwingToolkit.displayURL(null, "Notice",
                                        url);
              }
            }
          }); // End of new Thread
          t.start();
        }
      }
    }
    );
    JPanel html_panel = new JPanel();
    html_panel.setLayout(new BorderLayout());
    html_panel.add(html_viewer, BorderLayout.CENTER);
    return html_panel;
  }

  /**
   * Make a certain color transparent and return a new image.
   * @param im The original {@link Image}
   * @param color the {@link Color} to be transparent
   * @param repl_color {@link Color} replace color
   * @return the new {@link Image}
   */
  public static Image makeColorTransparent(Image im, final Color color,
                                           final Color repl_color) {
    ImageFilter filter =
        new RGBImageFilter() {
      // Alpha bits are set to opaque, regardless of what they
      // might have been already.
      public int markerRGB = color.getRGB() | 0xFF000000;

      public final int filterRGB(int x, int y, int rgb) {
        if ( (rgb | 0xFF000000) == markerRGB) {
          // Mark the alpha bits as zero - transparent, but
          // preserve the other information about the color
          // of the pixel.
          return 0x00FFFFFF & rgb;
        } else {
          // replace color, or leave pixel untouched
          if (repl_color != null) {
            return repl_color.getRGB() | 0xFF000000;
          } else {
            return rgb;
          }
        }
      }
    }; // end of inner class

    // Setup to use transparency filter
    ImageProducer ip = new FilteredImageSource(im.getSource(),
                                               filter);

    // Pull the old image thru this filter and create a new one
    return Toolkit.getDefaultToolkit().createImage(ip);
  }

  /**
   * Read a scaled image from the specified URL.
   * @param url the {@link URL}
   * @param w scale to this width
   * @param composite <code>true</code> if images should be faded
   * @return {@link BufferedImage} matching the params
   * @throws IOException if the {@link URL} cannot be accessed
   */
  public static BufferedImage readScaledImage(URL url, int w, boolean composite) throws
      IOException {
    BufferedImage orig = ImageIO.read(url);
    Image i = orig.getScaledInstance(w, -1, Image.SCALE_SMOOTH);
    BufferedImage bi = new BufferedImage(w, i.getHeight(null),
                                         BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = bi.createGraphics();
    if (composite) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    }
    g.drawImage(i, 0, 0, null);
    return bi;
  }


  /**
	 * This method builds a JMenu and does the following 1. Attaches mnemonics to
	 * the menu items 2. Attaches accelerators to the menu items 3. Attaches
	 * action listeners to the menu items 4. Adds menu_items to a
	 * function_component_map
	 * 
	 * @param header String
	 * @param items String []
	 * @param mnemonic_map HashMap
	 * @param accelerator_map HashMap
	 * @param function_listener_map HashMap
	 * @param function_component_map HashMap
	 */
	public static JMenu makeMenu(String header, String[] items,
			HashMap mnemonic_map, HashMap accelerator_map,
			HashMap function_listener_map, HashMap function_component_map) {

		JMenu menu = new JMenu(header);

		// Assign mnemonic to header
		Object mnemonic = mnemonic_map.get(header);
		if (mnemonic != null)
			menu.setMnemonic(((Character) mnemonic).charValue());

		// Loop through items connecting them
		for (int i = 0; i < items.length; i++) {

			// a null item is a separator
			if (items[i] == null) {
				menu.addSeparator();

			} else {

				// create menuitem
				JMenuItem mi = new JMenuItem(items[i]);

				// add action listener
				ActionListener al = (ActionListener) function_listener_map
						.get(items[i]);
				if (al != null)
					mi.addActionListener(al);

				// add accelerator
				Object shortcut = accelerator_map.get(items[i]);
				if (shortcut != null) {
					mi.setAccelerator((KeyStroke) shortcut);
				}

				// add mnemonic
				mnemonic = mnemonic_map.get(items[i]);
				if (mnemonic != null) {
					mi.setMnemonic(((Character) mnemonic).charValue());
				}

				// Add to function_component_map
				function_component_map.put(items[i], mi);
				menu.add(mi);
			}
		}
		function_component_map.put(header, menu);
		return menu;
	}

}
