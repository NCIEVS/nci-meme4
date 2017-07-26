/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe
 * Object:     RxWriterView.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 * Remarks:    This is the frame for the recipe writer.
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import gov.nih.nlm.swing.ConnectionDialog;
import gov.nih.nlm.swing.SwingToolkit;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;


/**
 * This object is the main GUI for editing the recipe.
 * @author Brian Carlsen, Owen J. Carlsen, Yun-Jung Kim
 * @version 1.5
 */

public class RxWriterView extends JFrame {
  
  //
  // Fields
  //

  private JButton prev_sec_btn;
  private JButton prev_step_btn;
  private JButton next_sec_btn;
  private JButton next_step_btn;
  private JButton set_btn;
  private JButton skip_btn;
  private JPanel rxw_content_pane;
  private JButton del_step_btn;
  private JButton ins_step_btn;

  // This maps function names to buttons
  private HashMap function_component_map = new HashMap();

  //  This is a function enable/disable map
  private static HashMap default_function_map = new HashMap ();
  static {
    default_function_map.put(RxConstants.FN_NEXT_SECTION,"false");
    default_function_map.put(RxConstants.FN_PREVIOUS_SECTION,"false");
    default_function_map.put(RxConstants.FN_NEXT_STEP,"false");
    default_function_map.put(RxConstants.FN_PREVIOUS_STEP,"false");
    default_function_map.put(RxConstants.FN_INSERT_SECTION,"false");
    default_function_map.put(RxConstants.FN_INSERT_STEP,"false");
    default_function_map.put(RxConstants.FN_DELETE_SECTION,"false");
    default_function_map.put(RxConstants.FN_DELETE_STEP,"false");
    default_function_map.put(RxConstants.FN_SET_STEP,"false");
    default_function_map.put(RxConstants.FN_SKIP_STEP,"false");
    default_function_map.put(RxConstants.FN_OPEN_RECIPE,"true");
    default_function_map.put(RxConstants.FN_SAVE_RECIPE,"false");
    default_function_map.put(RxConstants.FN_SAVE_RECIPE_AS,"false");
    default_function_map.put(RxConstants.FN_SAVE_PROPERTIES,"true");
    default_function_map.put(RxConstants.FN_VIEW_RECIPE_HTML,"false");
    default_function_map.put(RxConstants.FN_WRITE_SHELL_SCRIPT,"false");
    default_function_map.put(RxConstants.FN_NEW_RECIPE,"true");
    default_function_map.put(RxConstants.FN_EXIT,"true");
    default_function_map.put(RxConstants.FN_HELP_WRITER,"true");
    default_function_map.put(RxConstants.FN_HELP_STEP,"false");
    default_function_map.put(RxConstants.FN_HELP_MENU,"true");
    default_function_map.put(RxConstants.FN_FILE_MENU,"true");
    default_function_map.put(RxConstants.FN_OPTIONS_MENU,"true");
  };
  
  // This is a function to action listener map
  private HashMap function_listener_map = new HashMap();

  public static final java.awt.Cursor wait_cursor =  
    new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR);
  public static final java.awt.Cursor default_cursor = 
    new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR);
  
  public JPanel curr_view;
  public RxWriter my_rxw;
  public String browser;
  
  /**
   * Constructor
   */
  public RxWriterView(RxWriter parent_app) {
    super();
    RxToolkit.trace("RxWriterView::RxWriterView()");
    my_rxw = parent_app;
    parent_app.setView(this);
    initialize();
    parent_app.refreshView();
    setVisible(true);
  }
  
  /**
   * Initialize the class RxWriterView.
   */
  private void initialize() {
    RxToolkit.trace("RxWriterView::initialize()");
    
    setLocation(200, 200);
    
    // Currently the frame opens blank (maybe put a start screen)
    // setView expects curr_view to be set to something
    curr_view = new JPanel();
    setView(null,null);
    //getRxWriterViewContentPane().add(curr_view, BorderLayout.CENTER);

    setTitle("Recipe Writer");
    
    // menu bar
    JMenuBar mb = new JMenuBar();
    mb.setLayout(new BorderLayout(0,0));

    // Initialize action listeners for menu options
    function_listener_map.put(
      RxConstants.FN_EXIT,
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	  saveAndExit();
	}
      });

    function_listener_map.put(
      RxConstants.FN_VIEW_RECIPE_HTML,
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	setCursor(wait_cursor);
	my_rxw.requestViewAsHTML();
	setCursor(default_cursor);
	}
      });
    function_listener_map.put(
      RxConstants.FN_WRITE_SHELL_SCRIPT,
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	setCursor(wait_cursor);
	my_rxw.requestWriteShellScript();
	setCursor(default_cursor);
	}
      });
    function_listener_map.put(
      RxConstants.FN_OPEN_RECIPE,
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	setCursor(wait_cursor);
	my_rxw.requestLoad();
	setCursor(default_cursor);
	}
      });
    function_listener_map.put(
      RxConstants.FN_SAVE_RECIPE,
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	setCursor(wait_cursor);
	my_rxw.requestSave();
	setCursor(default_cursor);
	}
      });
    function_listener_map.put(
      RxConstants.FN_SAVE_RECIPE_AS,
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	setCursor(wait_cursor);
	my_rxw.requestSaveAs();
	setCursor(default_cursor);
	}
      });
    function_listener_map.put(
      RxConstants.FN_NEW_RECIPE,
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	setCursor(wait_cursor);
	my_rxw.requestNewRecipe();
	setCursor(default_cursor);
	}
      });

    function_listener_map.put(
      RxConstants.FN_SAVE_PROPERTIES,
      new ActionListener () {
        public void actionPerformed (ActionEvent e) {
	setCursor(wait_cursor);
	RxToolkit.storeProperties();
	setCursor(default_cursor);
	}
      });

    mb.add(SwingToolkit.makeMenu( 
		 RxConstants.FN_FILE_MENU,
		 new String [] {RxConstants.FN_NEW_RECIPE,
				  RxConstants.FN_OPEN_RECIPE,
				  RxConstants.FN_VIEW_RECIPE_HTML,
				  RxConstants.FN_WRITE_SHELL_SCRIPT,
				  RxConstants.FN_SAVE_RECIPE,
				  RxConstants.FN_SAVE_RECIPE_AS,
				  null,
				  RxConstants.FN_SAVE_PROPERTIES,
				  null,
				  RxConstants.FN_EXIT },
		 RxConstants.MENU_FN_MNEMONIC_MAP,
		 RxConstants.MENU_FN_ACCELERATOR_MAP,
		 function_listener_map,
		 function_component_map),
	   BorderLayout.WEST);
  
    TreeSet options = new TreeSet(RxToolkit.getProperties().keySet());
    mb.add(makeOptionsMenu(options.toArray()),BorderLayout.CENTER);


    //    mb.setHelpMenu(makeHelpMenu());
    mb.add(makeHelpMenu(),BorderLayout.EAST);

    setJMenuBar(mb);    
    // Insert Step button
    getInsStepBtn().addActionListener( 
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	  setCursor(wait_cursor);
	  my_rxw.requestInsertStep();
	  setCursor(default_cursor);
	}
      } 
     );
    function_component_map.put(RxConstants.FN_INSERT_STEP,getInsStepBtn());

    // Delete Step button
    getDelStepBtn().addActionListener( 
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	  setCursor(wait_cursor);
	  my_rxw.requestDeleteStep();
	  setCursor(default_cursor);
	}
       } 
     );
    function_component_map.put(RxConstants.FN_DELETE_STEP,getDelStepBtn());

    // Skip Step button
    getSkipBtn().addActionListener( 
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	  setCursor(wait_cursor);
	  my_rxw.requestSkip();
	  setCursor(default_cursor);
	}
       } 
     );
    function_component_map.put(RxConstants.FN_SKIP_STEP,getSkipBtn());

    // Set Step button
    getSetBtn().addActionListener( 
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	  setCursor(wait_cursor);
	  my_rxw.requestSet();
	  setCursor(default_cursor);
	}
       } 
     );
    function_component_map.put(RxConstants.FN_SET_STEP,getSetBtn());

    // Next Step button
    getNextStepBtn().addActionListener( 
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	  setCursor(wait_cursor);
	  my_rxw.requestNextStep();
	  setCursor(default_cursor);
	}
       } 
     );
    function_component_map.put(RxConstants.FN_NEXT_STEP,getNextStepBtn());

    // Next Section button
     getNextSecBtn().addActionListener( 
      new ActionListener () {
       public void actionPerformed(ActionEvent e) {
    	  setCursor(wait_cursor);
      my_rxw.requestNextSection();
      setCursor(default_cursor);
    }
     } 
    );
    function_component_map.put(RxConstants.FN_NEXT_SECTION,
                               getNextSecBtn());

    // Prev Step button
    getPrevStepBtn().addActionListener( 
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	  setCursor(wait_cursor);
	  my_rxw.requestPreviousStep();
	  setCursor(default_cursor);
	}
       } 
     );
    function_component_map.put(RxConstants.FN_PREVIOUS_STEP,getPrevStepBtn());

    // Prev Section button
    getPrevSecBtn().addActionListener( 
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	  setCursor(wait_cursor);
	  my_rxw.requestPreviousSection();
	  setCursor(default_cursor);
	}
    } 
      );
    function_component_map.put(RxConstants.FN_PREVIOUS_SECTION,getPrevSecBtn());

    // Set up closing behavior
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent e) { saveAndExit();}
    } );
    
    setFunctionEnableMap();
    
    setContentPane(getRxWriterViewContentPane());
    setName("RxWriterView");
    pack();
  };
    
  /**
   * This method sets the enabling of buttons and menu items
   * The default_function_map contains all available functions
   */
  public void setFunctionEnableMap() {
    RxToolkit.trace("RxWriterView::setFunctionEnableMap()");
    
    // If there is no recipe, use default_function_map,
    // otherwise everything should be enabled
    if (my_rxw.getRecipe() == null) {
      setFunctionEnableMask(default_function_map);
    } else {
      // set all buttons enabled on
      Iterator i = function_component_map.values().iterator();
      while (i.hasNext()) {
	JComponent j = (JComponent)i.next();
	if (! j.isEnabled())
	  j.setEnabled(true);
      }
    }
  };
    
  /**
   * This method overrides the enabled for each function
   * specified in the function mask.
   */
  public void setFunctionEnableMask( HashMap function_mask ) {
    RxToolkit.trace("RxWriterView::setFunctionEnableMask("+function_mask+")");    

    // if null, no overrides
    if (function_mask == null)
      return;

    // Go through all all functions in function_mask
    Iterator i = function_mask.entrySet().iterator();
    while (i.hasNext()) {
      Map.Entry me = (Map.Entry)i.next();
      String function = (String)me.getKey();
      //      MEMEToolkit.trace("RxWriterView::setFunctionEnableMask - "+
      //		      " entry ("+me+"), function ("+function+")");
      boolean enable = Boolean.valueOf((String)me.getValue()).booleanValue();
      
      JComponent j =(JComponent)function_component_map.get(function);
      // If there is no component for this function, ignore
      if (j != null && j.isEnabled() != enable)
	j.setEnabled(enable);
    }
    
  }
    
  /**
   * Return the JButton1 property value.
   * @return JButton
   */
  private JButton getPrevSecBtn() {
    if (prev_sec_btn == null) {
      try {
	prev_sec_btn = new JButton();
	prev_sec_btn.setName("JButton1");
	prev_sec_btn.setText("Prev Section");
	prev_sec_btn.setBounds(28, 390, 130, 25);
      } catch (Throwable ivjExc) {
	handleException(ivjExc);
      }
    };
    return prev_sec_btn;
  }
  
  /**
   * Return the JButton2 property value.
   * @return JButton
   */
  private JButton getPrevStepBtn() {
    if (prev_step_btn == null) {
      try {
	prev_step_btn = new JButton();
	prev_step_btn.setName("JButton2");
	prev_step_btn.setText("Prev Step");
	prev_step_btn.setBounds(28, 428, 130, 25);
      } catch (Throwable ivjExc) {
	handleException(ivjExc);
      }
    };
    return prev_step_btn;
  }
  
  /**
   * Return the JButton3 property value.
   * @return JButton
   */
  private JButton getNextSecBtn() {
    if (next_sec_btn == null) {
      try {
	next_sec_btn = new JButton();
	next_sec_btn.setName("JButton3");
	next_sec_btn.setText("Next Section");
	next_sec_btn.setBounds(165, 390, 130, 25);
      } catch (Throwable ivjExc) {
	handleException(ivjExc);
      }
    };
    return next_sec_btn;
  }
  
  /**
   * Return the JButton4 property value.
   * @return JButton
   */
  private JButton getNextStepBtn() {
    if (next_step_btn == null) {
      try {
	next_step_btn = new JButton();
	next_step_btn.setName("JButton4");
	next_step_btn.setText("Next Step");
	next_step_btn.setBounds(165, 428, 130, 25);
      } catch (Throwable ivjExc) {
	
	handleException(ivjExc);
      }
    };
    return next_step_btn;
  }
  
  /**
   * Return the JButton5 property value.
   * @return JButton
   */
  private JButton getSetBtn() {
    if (set_btn == null) {
      try {
	set_btn = new JButton();
	set_btn.setName("JButton5");
	set_btn.setText("Set");
	set_btn.setBounds(461, 390, 130, 25);
      } catch (Throwable ivjExc) {
	handleException(ivjExc);
      }
    };
    return set_btn;
  }
  
  /**
   * Return the JButton6 property value.
   * @return JButton
   */
  private JButton getSkipBtn() {
    if (skip_btn == null) {
      try {
	skip_btn = new JButton();
	skip_btn.setName("JButton6");
	skip_btn.setText("Skip");
	skip_btn.setBounds(461, 428, 130, 25);
      } catch (Throwable ivjExc) {
	handleException(ivjExc);
      }
    };
    return skip_btn;
  }
  
  /**
   * Return the JButton7 property value.
   * @return JButton
   */
  private JButton getDelStepBtn() {
    if (del_step_btn == null) {
      try {
	del_step_btn = new JButton();
	del_step_btn.setName("JButton7");
	del_step_btn.setText("Delete Step");
	del_step_btn.setBounds(301, 390, 130, 25);
	del_step_btn.setEnabled(true);
      } catch (Throwable ivjExc) {
	handleException(ivjExc);
      }
    };
    return del_step_btn;
  }
  
  /**
   * Return the JButton8 property value.
   * @return JButton
   */
  private JButton getInsStepBtn() {
    if (ins_step_btn == null) {
      try {
	ins_step_btn = new JButton();
	ins_step_btn.setName("JButton8");
	ins_step_btn.setText("Insert Step");
	ins_step_btn.setBounds(301, 428, 130, 25);
      } catch (Throwable ivjExc) {
	handleException(ivjExc);
      }
    };
    return ins_step_btn;
  }
  
  /**
   * Return the JFrameContentPane property value.
   * @return JPanel
   */
  private JPanel getRxWriterViewContentPane() {
    RxToolkit.trace("RxWriterView::getRxWriterViewContentPane()");
    if (rxw_content_pane == null) {
      try {
	rxw_content_pane = new JPanel(new BorderLayout());
	
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.weightx = 1.0;
	constraints.weighty = 1.0;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridy = 0;    // Top row
	constraints.insets = SwingToolkit.GRID_INSETS;
	
	JPanel button_panel = new JPanel(new GridBagLayout());
	button_panel.add(getPrevSecBtn(), constraints);
	button_panel.add(getPrevStepBtn(), constraints);
	button_panel.add(getInsStepBtn(), constraints);
	button_panel.add(getSetBtn(), constraints);
	
	constraints.gridy++;   // Bottom row
	
	button_panel.add(getNextSecBtn(), constraints);
	button_panel.add(getNextStepBtn(), constraints);
	button_panel.add(getDelStepBtn(), constraints);
	button_panel.add(getSkipBtn(), constraints);
	
	JPanel instrument_panel = new JPanel(new GridBagLayout());
	constraints.insets = new Insets (5,15,15,15);    // Because 5+15=20, the common border
	instrument_panel.add(button_panel, constraints);
	
	getRxWriterViewContentPane().add(instrument_panel, BorderLayout.SOUTH);
      } catch (Throwable ivjExc) {
	handleException(ivjExc);
      }
    };
    return rxw_content_pane;
  }
    
  /**
   * Called whenever the part throws an exception.
   * @param exception Throwable
   */
  private void handleException(Throwable e) {
    RxToolkit.reportError("Unexpected exception in RxWriterView: "+e);
  }
    
  /**
   * This method creates the Options menu
   * It also connects action listeners to the menu functions, and puts
   * @param items Object[]
   * @return JMenu
   */
  private JMenu makeOptionsMenu( Object [] items) {

    RxToolkit.trace("RxWriterView::makeOptionsMenu("+items+")");
    JMenu menu = new JMenu(RxConstants.FN_OPTIONS_MENU);
    Object mnemonic = RxConstants.MENU_FN_MNEMONIC_MAP.get(RxConstants.FN_OPTIONS_MENU);
    if (mnemonic != null)
      menu.setMnemonic(((Character)mnemonic).charValue());
    
    // Add set DB connection 
    JMenuItem mi = new JMenuItem(RxConstants.FN_CHANGE_DB);
    mi.addActionListener(
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	  ConnectionDialog.getDBConnection();
	};
      } );
    mnemonic = RxConstants.MENU_FN_MNEMONIC_MAP.get(RxConstants.FN_CHANGE_DB);
    if (mnemonic != null)
      mi.setMnemonic(((Character)mnemonic).charValue());
    Object shortcut = RxConstants.MENU_FN_ACCELERATOR_MAP.get(
					   RxConstants.FN_CHANGE_DB);
    if (shortcut != null)
      mi.setAccelerator((KeyStroke)shortcut);
    menu.add(mi);

    // Add separator
    menu.addSeparator();

    for (int i = 0; i <items.length; i++) {

      if (items[i] == null) {
	menu.addSeparator();
      } else {
	mi = new JMenuItem((String)items[i]);
	mi.addActionListener( 
	  new ActionListener () {
	    public void actionPerformed (ActionEvent e) {
	    // Allow user to change options
	      String key = e.getActionCommand();
	      String response = RxToolkit.getUserInput(
			       "Enter a new value for: " + key +
			       "(" + RxToolkit.getProperty(key) + ")");
	      if (response != null) {
		RxToolkit.setProperty(e.getActionCommand(),response);
	      }
	    }
	} );
	menu.add(mi);
      };
    }
    function_component_map.put(RxConstants.FN_OPTIONS_MENU,menu);
    return menu;
  }
  

  /**
   * This method creates the Help menu
   * It also connects action listeners to the help functions
   * @return JMenu
   */
  private JMenu makeHelpMenu() {

    RxToolkit.trace("RxWriterView::makeHelpMenu()");
    JMenu menu = new JMenu(RxConstants.FN_HELP_MENU);
    Object mnemonic = RxConstants.MENU_FN_MNEMONIC_MAP.get(RxConstants.FN_HELP_MENU);
    if (mnemonic != null)
      menu.setMnemonic(((Character)mnemonic).charValue());
    
    // Add help for the recipe writer
    JMenuItem mi = new JMenuItem(RxConstants.FN_HELP_WRITER);
    mi.addActionListener(
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	  getHelp();
	};
      } );
    mnemonic = RxConstants.MENU_FN_MNEMONIC_MAP.get(RxConstants.FN_HELP_WRITER);
    if (mnemonic != null)
      mi.setMnemonic(((Character)mnemonic).charValue());

    function_component_map.put(RxConstants.FN_HELP_WRITER,mi);
    menu.add(mi);

    // Add help for the current step
    mi = new JMenuItem(RxConstants.FN_HELP_STEP);
    mi.addActionListener(
      new ActionListener () {
        public void actionPerformed(ActionEvent e) {
	    my_rxw.getWorkingStepHelp();
	};
      } );
    mnemonic = RxConstants.MENU_FN_MNEMONIC_MAP.get(RxConstants.FN_HELP_STEP);
    if (mnemonic != null)
      mi.setMnemonic(((Character)mnemonic).charValue());
    Object shortcut = RxConstants.MENU_FN_ACCELERATOR_MAP.get(
					   RxConstants.FN_HELP_STEP);
    if (shortcut != null)
      mi.setAccelerator((KeyStroke)shortcut);

    function_component_map.put(RxConstants.FN_HELP_STEP,mi);
    menu.add(mi);

    function_component_map.put(RxConstants.FN_HELP_MENU,menu);
    return menu;
  }
  
  /**
   * This method removes the existing view 
   * and puts the current view to the frame.
   * @param new_view JPanel
   * @param function_enable_mask HashMap
   */
  public void setView(JPanel new_view, HashMap function_enable_mask) {
    RxToolkit.trace("RxWriterView::setView("+new_view+","+
		    function_enable_mask );

    // Make frame invisible
    // remove old pane, get new one
    // enable/disable buttons
    // revalidate the view
    // pack & display it
    if (new_view == null) {
      new_view = new JPanel();
    };
    getRxWriterViewContentPane().remove(curr_view);
    new_view.setBorder(RxConstants.EMPTY_BORDER);
    getRxWriterViewContentPane().add(new_view,BorderLayout.CENTER);
    curr_view=new_view;

    setFunctionEnableMap();
    // mask overrides default map
    setFunctionEnableMask(function_enable_mask);

    pack();
    
  }

  /**
   * This method displays some kind of help window
   */
  public void getHelp () {
    RxToolkit.notifyUser("Help is not currently available for the writer.\n"+
			 "The RxWriterView::getHelp() method must be fleshed out.");
  }

  /**
   * This method checks if the recipe should be saved before exiting
   * It also calls MEMEToolkit.Exit to cleanup resources before exiting.
   */
  public void saveAndExit () {
    
    if (my_rxw.hasDataChanged()) {
      boolean result = RxToolkit.confirmRequest(
	"Do you want to save the recipe before exiting?");
      if (result) {
	setCursor(wait_cursor);
	my_rxw.requestSave();
	setCursor(default_cursor);
      }
    }
    RxToolkit.Exit(0);
  }

  public void startWaitMode () {
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    getGlassPane().addMouseListener(RxConstants.NULL_MOUSE_LISTENER);
  }
  
  public void stopWaitMode () {
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    getGlassPane().removeMouseListener(RxConstants.NULL_MOUSE_LISTENER);
  }
  
}
