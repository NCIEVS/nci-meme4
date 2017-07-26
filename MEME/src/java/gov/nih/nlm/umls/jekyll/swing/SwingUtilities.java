/************************************************************************
 *
 * Package:     gov.nih.nlm.umls.jekyll.swing
 * Object:      SwingUtilities
 *
 * Author:      Vladimir Olenichev
 *
 * Remarks:     
 *
 * Change History: 
 *  09/23/2002: First version
 *
 ***********************************************************************/

package gov.nih.nlm.umls.jekyll.swing;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.JOptionPane;

/**
 * A collection of utility methods for Swing.
 *
 * <p>
 * {@link <a href="/vlad-doc/jekyll/src_files/Swing/SwingUtilites.java.html">Browse Source</a>}
 */
public class SwingUtilities {

    /**
     * Returns a titled border with specified title.
     *
     * @param title desired title
     * @return titled <code>Border</code>
     */
    public static TitledBorder buildTitledBorder(String title) {
	Border black_line_border = BorderFactory.createLineBorder(Color.black);
	return BorderFactory.createTitledBorder(black_line_border, title);
    }

    /**
     * Brings up an information-message type dialog with a specified
     * message and a dialog title.
     *
     * @param message the <code>Object</code> to display
     * @param title the title string for the dialog
     */
    public static void showInfoMessageDialog(Object message, String title) {
	JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Brings up an error-message type dialog with a specified
     * message and a dialog title.
     *
     * @param message the <code>Object</code> to display
     * @param title the title string for the dialog
     */
    public static void showErrorMessageDialog(Object message, String title) {
	JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Sets the JComponents in the list to be the same size.
     * This is done dynamically by setting each button's 
     * preferred and maximum sizes after the components have
     * been created. This way, the layout automatically adjusts 
     * to the locale-specific strings and customized fonts.
     * 
     * The sizes of the JComponents are NOT modified here. That is
     * done later by the layout manager.
     * 
     * @param components must contain only instances of JComponent
     */
    public static void equalizeComponentSizes(java.util.List components) {

        // Get the largest width and height
        int i = 0;
        Dimension maxPreferred = new Dimension(0,0);
        JComponent oneComponent = null;
        Dimension thisPreferred = null;
        for (i = 0; i < components.size(); ++i) {
            oneComponent = (JComponent)components.get(i);
            thisPreferred = oneComponent.getPreferredSize();
            maxPreferred.width = 
                Math.max(maxPreferred.width, (int)thisPreferred.getWidth());
            maxPreferred.height = 
                Math.max(maxPreferred.height, (int)thisPreferred.getHeight());
        }
      
        // reset preferred and maximum size since BoxLayout takes both 
        // into account 
        for (i = 0; i < components.size(); ++i) {
            oneComponent = (JComponent)components.get(i);
            oneComponent.setPreferredSize((Dimension)maxPreferred.clone());
            oneComponent.setMaximumSize((Dimension)maxPreferred.clone());
        }
    } // equalizeComponentSizes()
}
