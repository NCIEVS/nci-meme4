/*****************************************************************************
 *
 * Package:    com.lexical.meme.swing
 * Object:     FactFilterPopupMenu.java
 * 
 * Author:     Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import gov.nih.nlm.recipe.AdHocFactFilter;
import gov.nih.nlm.recipe.AdHocQueryFactFilter;
import gov.nih.nlm.recipe.FactFilter;
import gov.nih.nlm.recipe.RxToolkit;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;


/**
 * This subclass of JPopupMenu is used for editing FactFilters
 *
 * @author Owen J. Carlsen
 * @version 1.0
 * 
 */
public class FactFilterPopupMenu extends JPopupMenu {

  //
  // Fields
  //
  private SuperJList jfilters = null;
  private JMenuItem add_menu = null; 
  private JMenuItem add_query_menu = null; 
  private JMenuItem edit_menu = null; 
  private JMenuItem delete_menu = null; 
  private JMenuItem delete_all_menu = null; 

  /**
   * Default Constructor.
   */
  public FactFilterPopupMenu () { super(); };

  /**
   * Constructor that hooks a fact filter vector up to
   * a jlist representing it with add/delete functions
   * @param filter ArrayList
   * @param jfilter SuperJList
   */
  public FactFilterPopupMenu (SuperJList jf) {
    super();
    this.jfilters = jf;

    add_menu = new JMenuItem("Add Filter");
    add(add_menu);
    add_menu.addActionListener (
       new ActionListener () {
	 public void actionPerformed (ActionEvent e) {
	   FactFilter ff = FactFilterDialog.addFilter(jfilters);
	   if (ff != null) {
	     RxToolkit.trace("The filters JList must use DefaultListModel:"+
			       jfilters.getModel().getClass());
	     ((DefaultListModel)jfilters.getModel()).addElement(ff); 
	     int size = jfilters.getModel().getSize();
	     jfilters.setVisibleRowCount( (size > 6) ? 6 : 
					  (size == 0) ? 1 : 
					  size );
	     delete_menu.setEnabled(true);
	     delete_all_menu.setEnabled(true);
	     ((Window)jfilters.getTopLevelAncestor()).pack();
	   }
         } 
       } ); 

    add_menu = new JMenuItem("Add Ad Hoc Filter");
    add(add_menu);
    add_menu.addActionListener (
       new ActionListener () {
	 public void actionPerformed (ActionEvent e) {
	   String script = RxToolkit.getUserInput("Enter script name:");
	   if (script != null) { // no cancel
	     AdHocFactFilter ff = new AdHocFactFilter(script);
	     RxToolkit.trace("The filters JList must use DefaultListModel:"+
			       jfilters.getModel().getClass());
	     ((DefaultListModel)jfilters.getModel()).addElement(ff); 
	     delete_menu.setEnabled(true);
	     delete_all_menu.setEnabled(true);
	     jfilters.resizeList(1,6);
	   }
         } 
       } ); 

    add_query_menu = new JMenuItem("Add Ad Hoc Query Filter");
    add(add_query_menu);
    add_query_menu.addActionListener (
       new ActionListener () {
	 public void actionPerformed (ActionEvent e) {
	   String query = RxToolkit.getMultiLineUserInput("Enter query:");
	   if (query != null) { // no cancel
	     AdHocQueryFactFilter ff = new AdHocQueryFactFilter(query);
	     ((DefaultListModel)jfilters.getModel()).addElement(ff); 
	     delete_menu.setEnabled(true);
	     delete_all_menu.setEnabled(true);
	     jfilters.resizeList(1,6);
	   }
         } 
       } ); 

    edit_menu = new JMenuItem("Edit Selected Filter");
    add(edit_menu);
    edit_menu.addActionListener (
       new ActionListener () {
	 public void actionPerformed (ActionEvent e) {
	   FactFilter ff = (FactFilter)jfilters.getSelectedValue();
	   if (ff instanceof AdHocFactFilter) {
	     String script = RxToolkit.getUserInput(
			      "Enter new script name:");
	     if (script != null) { // no cancel
	       AdHocFactFilter aff = (AdHocFactFilter)ff;
	       aff.script = script;
	     }
	   } else {
	     FactFilterDialog.editFilter(
		 jfilters,ff);
	   };
	 }
       } ); 

    delete_menu = new JMenuItem("Delete Filter");
    add(delete_menu);
    delete_menu.addActionListener(
       new ActionListener () {
	 public void actionPerformed (ActionEvent e) {
	   FactFilter ff = (FactFilter) jfilters.getSelectedValue();
	   if (ff != null) {
	     ((DefaultListModel)jfilters.getModel()).removeElement(ff);
	     if (jfilters.getModel().getSize() == 0)
	       delete_menu.setEnabled(false);
	   }
	   jfilters.resizeList(1,6);

         } 
       } ); 
    addSeparator();
    delete_all_menu = new JMenuItem("Delete All Filters");
    add(delete_all_menu);
    delete_all_menu.addActionListener(
       new ActionListener () {
	 public void actionPerformed (ActionEvent e) {
	   boolean response = 
	     RxToolkit.confirmRequest( 
	        "Are you sure you want to delete all of the filters?" );
	   if (response) {
	     jfilters.setListData(new Object[] {});
	     jfilters.setVisibleRowCount(1);
	     delete_menu.setEnabled(false);
	     delete_all_menu.setEnabled(false);
	   }
	   ((Window)jfilters.getTopLevelAncestor()).pack();
         } 
    } ); 
    setLabel("Edit Fact Filters");
    setBorder(new BevelBorder(BevelBorder.RAISED));

    // enable menus
    if (jfilters.getModel().getSize() == 0) {
      delete_menu.setEnabled(false);
      delete_all_menu.setEnabled(false);
      add_menu.setEnabled(true);

    };

    // Add a double-click listener to edit
    jfilters.addMouseListener (
       new MouseAdapter () {
	 public void mouseClicked (MouseEvent e) {
	   if (e.getClickCount() == 2 && jfilters.getModel().getSize()>0) {
	     edit_menu.doClick();
	   }
         } 
    } ); 
  }

  /**
   * This method overrides show() to disable/enable
   * the edit menu depending on whether the SuperJList has
   * a row selected
   * @param invoker Component
   * @param x int
   * @param y int
   */
  public void show (Component invoker, int x, int y) {
    if (jfilters.getSelectedValue() == null)
      edit_menu.setEnabled(false);
    else
      edit_menu.setEnabled(true);

    super.show (invoker,x,y);
  }

}
