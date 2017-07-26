/*****************************************************************************
 *
 * Object:  ChangeServerDialog
 * Author:  Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.IntegerDocument;
import gov.nih.nlm.swing.SwingToolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog for changing the MEME application server host and port.
 * The initial values are passed in.
 *
 * @author  Brian Carlsen (bcarlsen@apelon.com)
 */
public class ChangeServerDialog extends JDialog {

  private static final long serialVersionUID = 1L;
  //
  // Fields
  //
  private String host = null;
  private String port = null;
  private boolean cancelled = false;

  //
  // Constructors
  //

  /**
   * Construct the dialog.
   * @param parent An object {@link Component}.
   * @param default_host the default host.
   * @param default_port the default port.
   * @throws Exception if failed to change server dialog.
   */
  public ChangeServerDialog(Component parent,
                            String default_host, int default_port) throws
      Exception {
    super();
    setLocationRelativeTo(parent);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setModal(true);
    setTitle("Change Server");

    JPanel ldata_panel = new JPanel(new GridBagLayout());
    GridBagConstraints lc = new GridBagConstraints();
    lc.weightx = 1.0;
    lc.weighty = 1.0;
    lc.fill = GridBagConstraints.BOTH;
    lc.gridx = GridBagConstraints.RELATIVE;
    lc.insets = new Insets(5, 5, 5, 5);
    lc.gridy = 0;

    //
    // Host field
    //
    ldata_panel.add(new JLabel("Host:"), lc);
    String[] hosts = MIDServices.getHostServicesList();
    for (int i = 0; i < hosts.length; i++) {
      hosts[i] = MIDServices.getService(hosts[i]);
    }
    Arrays.sort(hosts);
    String prev_host = "";
    List list = new ArrayList();
    for (int i = 0; i < hosts.length; i++) {
      if (!hosts[i].equals(prev_host)) {
        prev_host = hosts[i];
        list.add(hosts[i]);
      }
    }
    final JComboBox jhosts = new JComboBox(list.toArray(new String[0]));
    jhosts.addItem(default_host);
    jhosts.setSelectedItem(default_host);
    jhosts.setEditable(true);
    ldata_panel.add(jhosts, lc);
    lc.gridy++;

    //
    // Port field
    //
    ldata_panel.add(new JLabel("Port:"), lc);
    final JTextField jport = new JTextField(new IntegerDocument(),
                                            String.valueOf(default_port), 4);
    ldata_panel.add(jport, lc);
    lc.gridy++;

    JPanel lbtn_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    JButton ok_btn = new JButton("OK");
    ok_btn.addActionListener(
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ChangeServerDialog.this.host = (String) jhosts.getSelectedItem();
        ChangeServerDialog.this.port = jport.getText();
        cancelled = false;
        setVisible(false);
      }
    });
    lbtn_panel.add(ok_btn);

    JButton cancel_btn = new JButton("Cancel");
    cancel_btn.addActionListener(
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelled = true;
        setVisible(false);
      }
    });
    lbtn_panel.add(cancel_btn);

    lc.gridwidth = 2;
    ldata_panel.add(lbtn_panel, lc);

    // Set border & build pane
    ldata_panel.setBorder(SwingToolkit.EMPTY_BORDER);
    getContentPane().add(ldata_panel);
    getRootPane().setDefaultButton(ok_btn);
    FontSizeManager.adjustFontSize(this);
    pack();

  }

  /**
   * Indicate whether or not the dialog was cancelled
   * @return whether or not it is cancelled.
   */
  public boolean cancelled() {
    return cancelled;
  }

  /**
   * Return the selected host.
   * @return the host.
   */
  public String getHost() {
    return host;
  }

  /**
   * Return the selected port.
   * @return the port.
   */
  public int getPort() {
    return Integer.valueOf(port).intValue();
  }

}