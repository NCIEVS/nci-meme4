/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe.steps
 * Object:     FactFilterDialog.java
 * 
 * Author:     Brian A. Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import gov.nih.nlm.recipe.FactFilter;
import gov.nih.nlm.recipe.RxConstants;
import gov.nih.nlm.recipe.RxToolkit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;



/**
 * This is JDialog for editing Fact Filters
 *
 * @author Brian Carlsen
 * @version 1.0
 */

public class FactFilterDialog extends JDialog {

  //
  // Fields
  //
  private FactFilter filter = null;

  private JComboBox jtype_1 = null;
  private JCheckBox jnegation_1 = new JCheckBox("Negate?");
  private SuperJList jvalue_1 = new SuperJList();
  private JComboBox jtype_2 = null;
  private JCheckBox jnegation_2 = new JCheckBox("Negate?");
  private SuperJList jvalue_2 = new SuperJList();

  /**
   * Constructor
   * @param opener JComponent
   */
  public FactFilterDialog(JComponent opener) {
    super((Frame)null,"Edit Fact Filters", true);
    setLocationRelativeTo(opener);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    initialize();
  }


  /**
   * Constructor
   * @param opener JComponent
   * @param ff FactFilter
   */
  public FactFilterDialog(JComponent opener, FactFilter ff) {
    super((Frame)null,"Edit Fact Filters", true);
    setLocationRelativeTo(opener);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    filter = ff;
    initialize();
  }
    
 /**
   * Initialize the view, but do NOT setVisible(true):
   */
  private void initialize() {

    JPanel data_panel = new JPanel(new GridBagLayout());      
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.insets = SwingToolkit.GRID_INSETS;

    // Add first type
    constraints.gridy = 0;
    data_panel.add(new JLabel("First Type"),constraints);
    jtype_1 = new JComboBox(
	    RxToolkit.DBToolkit.getFilterData(
		RxToolkit.DBToolkit.FILTER_TYPES));
    data_panel.add(jtype_1,constraints);
    
    // if type_1 changes, get new data
    // if type chosen has no data, disable value field
    jtype_1.addActionListener(
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	   String type = jtype_1.getSelectedItem().toString();
	   Object [] data = RxToolkit.DBToolkit.getFilterData(type);
	   int size;
	   if (data == null) {
	     jvalue_1.setEnabled(false);
	     jvalue_1.setListData(new Object[] {});
	     jvalue_1.setVisibleRowCount( 1);
	     pack();
	   } else {	   
	     jvalue_1.setEnabled(true);
	     jvalue_1.setListData(data);
	     size = jvalue_1.getModel().getSize();
	     jvalue_1.setVisibleRowCount( size > 6 ? 6 : size);
	     pack();
	   }
	 }
       } );
    
    // add value_1
    constraints.gridy = 1;
    data_panel.add(new JLabel("First Value:"), constraints);
    jvalue_1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    // trigger actionPerformed

    JScrollPane jvalue_1_scroll = new JScrollPane();
    jvalue_1_scroll.setViewportView(jvalue_1);
    data_panel.add(jvalue_1_scroll, constraints);
    
    // add negation_1
    constraints.gridy = 2;
    constraints.gridx = 1;
    data_panel.add(jnegation_1,constraints);
    constraints.gridx = GridBagConstraints.RELATIVE;

    // Add second type
    constraints.gridy = 3;
    data_panel.add(new JLabel("Second Type"),constraints);
    jtype_2 = new JComboBox(RxToolkit.DBToolkit.getFilterTypes());
    data_panel.add(jtype_2,constraints);
    
    // if type_2 changes, get new data
    // if type chosen has no data, disable value field
    jtype_2.addActionListener(
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	   String type = jtype_2.getSelectedItem().toString();
	   Object data = RxToolkit.DBToolkit.getFilterData(type);
	   int size;
	   if (data == null) {
	     jvalue_2.setEnabled(false);
	     jvalue_2.setListData(new Object[] {});
	     jvalue_2.setVisibleRowCount( 1);
	     pack();
	   } else {	   
	     jvalue_2.setEnabled(true);
	     jvalue_2.setListData((Object[]) data);
	     size = jvalue_2.getModel().getSize();
	     jvalue_2.setVisibleRowCount( size > 6 ? 6 : size);
	     pack();
	   }
	 }
       } );
    
    // add value_2
    constraints.gridy = 4;
    data_panel.add(new JLabel("Second Value:"), constraints);
    jvalue_2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    // trigger actionPerformed
    if (filter != null)
      jtype_2.setSelectedItem(filter.type_2);
    else
      jtype_2.setSelectedIndex(0);
    
    JScrollPane jvalue_2_scroll = new JScrollPane();
    jvalue_2_scroll.setViewportView(jvalue_2);
    data_panel.add(jvalue_2_scroll, constraints);
 
    // add negation_2
    constraints.gridy = 5;
    constraints.gridx = 1;
    data_panel.add(jnegation_2,constraints);
    constraints.gridx = GridBagConstraints.RELATIVE;

    // set values
    if (filter != null) {
      jtype_1.setSelectedItem(filter.type_1);
      jvalue_1.setSelectedValues(filter.values_1,true);
      jnegation_1.setSelected(filter.negation_1); 
      jtype_2.setSelectedItem(filter.type_2);
      jvalue_2.setSelectedValues(filter.values_2,true);
      jnegation_2.setSelected(filter.negation_2);
    } else {
      jtype_1.setSelectedIndex(0);
      jtype_2.setSelectedIndex(0);
    }


    // Add buttons..
    JPanel control_panel = new JPanel(new FlowLayout(FlowLayout.CENTER,15,0));
    JButton ok_btn = new JButton("OK");
    ok_btn.setMnemonic('O');
    control_panel.add(ok_btn);
    getRootPane().setDefaultButton(ok_btn);		     

    // The OK button has an action listener.  If the user clicks
    // OK before filling out all the fields,  she gets a warning. 
    //  Otherwise a FactFilter is created and the dialog closed
    ok_btn.addActionListener (
      new ActionListener () {
        public void actionPerformed (ActionEvent e) { 
	    if (jvalue_1.getSelectedValue() == null &&
	      jvalue_1.getModel().getSize() > 0) {
	      RxToolkit.reportError(
	        "You must select a first value.");
	      return;
	    }
	    if (jvalue_2.getSelectedValue() == null &&
	      jvalue_2.getModel().getSize() > 0) {
	      RxToolkit.reportError(
	        "You must select a second value."); 
	      return;
	    }
	     
	    if (filter == null)
	      filter = new FactFilter();
	    
	    filter.type_1 = (String)jtype_1.getSelectedItem();
	    filter.values_1= jvalue_1.getSelectedStringValues();
	    filter.negation_1 =  jnegation_1.isSelected();
	    filter.type_2 = (String)jtype_2.getSelectedItem();
	    filter.values_2= jvalue_2.getSelectedStringValues();
	    filter.negation_2 =  jnegation_2.isSelected();
	    
	    setVisible(false);
	 }
       } );
    
    JButton cancel_btn = new JButton("Cancel");
    cancel_btn.setMnemonic('C');
    control_panel.add(cancel_btn);
    cancel_btn.addActionListener(
      new ActionListener () {
        public void actionPerformed (ActionEvent e) { 
	  filter = null;
	  setVisible(false);
	 }
       } );
    
    ok_btn.setPreferredSize(cancel_btn.getPreferredSize());

    data_panel.setBorder(RxConstants.EMPTY_BORDER);
    getContentPane().add(data_panel,BorderLayout.CENTER);
    control_panel.setBorder(RxConstants.EMPTY_BORDER_NO_TOP);
    getContentPane().add(control_panel,BorderLayout.SOUTH);
    pack();
  }

  /**
   * This method returns the filter
   * @return FactFilter
   */
  public FactFilter getValue() {
    return filter;
  }



  //
  // Static methods
  //

  /**
   * This method opens a modal dialog and allows 
   * editing of filters.  when finished, it determines
   * if the filters were changed and returns a true or false
   * @param opener JComponent
   * @return FactFilter
   */
  public static FactFilter addFilter (JComponent opener) {

    FactFilterDialog ffd = new FactFilterDialog(opener);
    
    // hopefully it hangs here until closed
    ffd.setVisible(true);

    FactFilter ff = ffd.getValue();
    ffd.dispose();
    opener.revalidate();
    return ff;
  }

  /** this method opens a modal dialog
   * and passes in a filter for editing.  when finished, it returns the 
   * new, edited filter
   * @param opener JComponent
   */
  public static void editFilter(JComponent opener, FactFilter ff) {
    
    FactFilterDialog ffd = new FactFilterDialog(opener,ff);
    ffd.setVisible(true);
    ffd.dispose();
    opener.revalidate();
    return;
  };    
}
