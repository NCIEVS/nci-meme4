/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe
 * Object:     ConnectionDialog.java
 * 
 * Author:     Brian Carlsen
 *
 * Remarks:    This frame gathers information needed to open an SQL connection
 * CHANGES
 *  12/13/2007 BAC (1-FZIZD): Support JDBC string in DB_SERVICE param. No need for midsvcs lookup
 *****************************************************************************/
package gov.nih.nlm.swing;


import gov.nih.nlm.recipe.MIDServices;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxToolkit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;




public class ConnectionDialog extends JDialog {

  private JTextField juser;
  private JPasswordField jpassword;
  private JTextField jdb;
  private static java.sql.Connection connection;

  /**
   * Initialize the class Frame
   */
  public ConnectionDialog() {
    RxToolkit.trace("ConnectionDialog::ConnectionDialog()");
    setModal(true);
     setLocation(200, 100);
   
    JPanel content_pane = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 0;
    constraints.insets = SwingToolkit.GRID_INSETS;

    content_pane.add(new JLabel("User:"), constraints);
    content_pane.add(juser = new JTextField(RxToolkit.getProperty(RxConstants.DB_USER),20), constraints);
    
    constraints.gridy = 1;
    content_pane.add(new JLabel("Password:"), constraints);
    content_pane.add(jpassword = new JPasswordField(20), constraints);
    String default_password = RxToolkit.getProperty(RxConstants.DB_PASSWORD, "");
    jpassword.setText(default_password);

    constraints.gridy = 2;
    content_pane.add(new JLabel("Database:"), constraints);
    content_pane.add(jdb = new JTextField(RxToolkit.getProperty(RxConstants.DB_SERVICE),40), constraints);

    // Create a panel for buttons (15 pixel hgap)
    JPanel button_panel = new JPanel(new FlowLayout(FlowLayout.CENTER,15,0));
    JButton ok_button = new JButton("OK");

    JButton cancel_button = new JButton("Cancel");
    ok_button.setPreferredSize(cancel_button.getPreferredSize());
    button_panel.add(ok_button);
    button_panel.add(cancel_button);
    ok_button.addActionListener(
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	  RxToolkit.setProperty(RxConstants.DB_USER,juser.getText());
	  RxToolkit.setProperty(RxConstants.DB_PASSWORD, new String(jpassword.getPassword()));
	  String db_service = jdb.getText();
	  RxToolkit.setProperty(RxConstants.DB_SERVICE, db_service);
	  makeConnection();
          setVisible(false);	     
	}
    } );
    cancel_button.addActionListener(
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	  dispose();
	}
    } );

    // Put content pane & button panel into the frame
    JPanel enclosing_pane = new JPanel(new BorderLayout());
    content_pane.setBorder(RxConstants.EMPTY_BORDER);
    enclosing_pane.add(content_pane,BorderLayout.CENTER);
    button_panel.setBorder(RxConstants.EMPTY_BORDER_NO_TOP);
    enclosing_pane.add(button_panel,BorderLayout.SOUTH);

    getRootPane().setDefaultButton(ok_button);

    getContentPane().add(enclosing_pane);
    setTitle("Set Database Connection");
    pack();
  };

  public static Connection getDBConnection() {
    RxToolkit.trace("ConnectionDialog::getDBConnection()");
    if((RxToolkit.getProperty(RxConstants.DB_USER) != null) &&
       (RxToolkit.getProperty(RxConstants.DB_PASSWORD) != null) &&
       (RxToolkit.getProperty(RxConstants.DB_SERVICE) != null)) { 
        RxToolkit.trace("ConnectionDialog::getDBConnection() != null");
        RxToolkit.trace(RxToolkit.getProperty(RxConstants.DB_USER));
        RxToolkit.trace(RxToolkit.getProperty(RxConstants.DB_PASSWORD));
        RxToolkit.trace(RxToolkit.getProperty(RxConstants.DB_SERVICE));
        RxToolkit.trace(RxToolkit.getProperty("MIDSVCS_HOST"));
       
	  Connection connection = makeConnection();
          return connection;
    } else {
    RxToolkit.trace("ConnectionDialog::getDBConnection() after else");
      ConnectionDialog con = new ConnectionDialog();
      if(con == null)
    RxToolkit.trace("ConnectionDialog::getDBConnection() after if");
      con.setVisible(true);
      con.dispose();
      return ConnectionDialog.connection;
    }
  };

  public static Connection makeConnection() {
    String driver = RxToolkit.getProperty(RxConstants.DB_DRIVER_CLASS);
    //register the driver
    try {
      DriverManager.registerDriver((Driver)Class.forName(driver).newInstance());
    } catch (Exception e1) {
      RxToolkit.reportError("Error loading database driver: " + driver, true);
    };
    // this uses "thin" jdbc
    String driverurl = "";
    String user = RxToolkit.getProperty(RxConstants.DB_USER);
    String password = RxToolkit.getProperty(RxConstants.DB_PASSWORD, "");
    String password_mask = "***************************************";
    try {
    	String db_service = RxToolkit.getProperty(RxConstants.DB_SERVICE);
    	if (db_service.startsWith("jdbc"))
    		driverurl = db_service;
    	else 
    		driverurl = MIDServices.getService(RxToolkit.getProperty(RxConstants.DB_SERVICE) + "-jdbc");
    } catch (Exception e2) {
      RxToolkit.reportError("Error loading database driver url", true);
    };

    // For security password is removed from properties
    RxToolkit.setProperty(RxConstants.DB_PASSWORD,"");
    
    // naveen: UMLS:136: setting TNS_ADMIN to get db details from tnsnames.ora
    System.setProperty("oracle.net.tns_admin", System.getenv("TNS_ADMIN")); 

    RxToolkit.logComment("Initializing SQL session = open connection" +		"\n\tURL=" + driverurl +
	"\n\tUSER=" + user +
	"\n\tPASSWORD=" + password_mask.substring(0,password.length()),true);

    try {
      connection = DriverManager.getConnection(driverurl, user, password);
    } catch (SQLException e1) {
      RxToolkit.reportError("An SQL exception occurred while opening the connection\n" + e1.getMessage(), false);
    };
    return connection;
  };
}

    
