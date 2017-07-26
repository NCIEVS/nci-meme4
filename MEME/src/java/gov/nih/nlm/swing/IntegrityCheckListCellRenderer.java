/*****************************************************************************
 *
 * Package:    com.lexical.meme.swing
 * Object:     IntegrityCheckListCellRenderer
 * 
 * Author:     Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import gov.nih.nlm.recipe.IntegrityCheck;
import gov.nih.nlm.recipe.RxToolkit;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * Used to render descriptions as tooltext for lists of ic checks
 *
 * @author Brian Carlsen
 * @version 1.0
 *
 */
public class IntegrityCheckListCellRenderer extends DefaultListCellRenderer 
  implements ListCellRenderer <Object>{

  /** 
   * Constructor
   */
  public IntegrityCheckListCellRenderer () {
    setOpaque(true);
    // make sure that this is registered so list elements can
    // have tool tips
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
      setToolTipText ( RxToolkit.toMultiLineHTML (
			    ((IntegrityCheck)value).long_description, 30));
      return this;
    };
}
