/*****************************************************************************
 *
 * Package:    com.lexical.meme.swing
 * Object:     FactFilterListCellRenderer
 * 
 * Author:     Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import gov.nih.nlm.recipe.FactFilter;
import gov.nih.nlm.recipe.RxToolkit;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * Used to render descriptions as tooltext for lists of fact filters
 *
 * @author Brian Carlsen
 * @version 1.0
 *
 */
public class FactFilterListCellRenderer extends DefaultListCellRenderer 
  implements ListCellRenderer <Object>{

  /** 
   * Constructor
   */
  public FactFilterListCellRenderer () {
    setOpaque(true);
  }

  /**
   * Implement ListCellRenderer interface
   * @return Component
   */
  public Component getListCellRendererComponent(
	JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)  
    {
      super.getListCellRendererComponent(
	 list, value, index, isSelected, cellHasFocus);
      
      // build multi-line tooltip
      setToolTipText( RxToolkit.toMultiLineHTML(
			  ((FactFilter)value).longDescription(), 30));  
      return this;
    };
}

