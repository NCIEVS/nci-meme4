/*****************************************************************************
 *
 * Package:    com.lexical.meme.core
 * Object:     IntegrityVector.java
 * 
 * Author:     Brian Carlsen
 *
 * Remarks:    This object represents an integrity Vector in the database
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import gov.nih.nlm.swing.IntegrityCheckListCellRenderer;
import gov.nih.nlm.swing.SuperJList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;


/**
 * This class represents an integrity vector.
 *
 * @author Brian A. Carlsen
 * @version 1.0
 *
 */
public class IntegrityVector 
  extends ArrayList implements Serializable {

  /**
   * Constructor
   */
  public IntegrityVector () { 
    super();
  };

  /**
   * This method puts a GUI display of the integrity vector into a panel
   * @return Jlist
   */
  public SuperJList toJList () {
    SuperJList display = new SuperJList(toArray());
    display.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    display.setCellRenderer(new IntegrityCheckListCellRenderer());
    display.setVisibleRowCount( ((size() > 6) ? 6 : size()));
    // To see integrityCheck tooltip text, register list & viewport
    ToolTipManager.sharedInstance().registerComponent(display);

    return display;
  }

  /**
   * Overrides hashCode
   */
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * This overrides toString to provide a string representation
   * @return
   */
  public String toString () {
    StringBuffer sb = new StringBuffer();
    Iterator iter = iterator();
    while (iter.hasNext()) {
      IntegrityCheck ic = (IntegrityCheck)iter.next();
      sb.append("<");
      sb.append(ic.ic_name);
      sb.append(":");
      sb.append(ic.ic_code);
      sb.append(">");
    }
    return sb.toString();
  }

  /**
   * This produces HTML representing the vector uses &lt; &gt;
   * @return
   */
  public String toHTML () {
    StringBuffer sb = new StringBuffer();
    Iterator iter = iterator();
    while (iter.hasNext()) {
      IntegrityCheck ic = (IntegrityCheck)iter.next();
      sb.append("&lt;");
      sb.append(ic.ic_name);
      sb.append(":");
      sb.append(ic.ic_code);
      sb.append("&gt;");
    }
    return sb.toString();
  }

  /**
   * This method returns an IntegrityCheck [] instead of Object []
   * @return IntegrityCheck []
   */
  public IntegrityCheck [] toICArray () {
    IntegrityCheck [] ic_array = new IntegrityCheck[size()];
    Iterator iter = iterator();
    int i = 0;
    while (iter.hasNext()) 
      ic_array[i++] = (IntegrityCheck)iter.next();
    
    return ic_array;
  }
}
