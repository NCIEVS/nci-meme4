/************************************************************************
 *
 * Package:     gov.nih.nlm.umls.jekyll.swing
 * Object:      ToggleSelectionModel
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

import javax.swing.DefaultListSelectionModel;

/**
 * 
 * <p>
 * {@link <a href="/vlad-doc/jekyll/src_files/Swing/ToggleSelectionModel.java.html">Browse Source</a>}
 */
public class ToggleSelectionModel extends DefaultListSelectionModel {

    boolean gestureStarted = false;

    public void setSelectionInterval(int index0, int index1) {
	if (isSelectedIndex(index0) && !gestureStarted) {
	    super.removeSelectionInterval(index0, index1);
	} else {
	    super.setSelectionInterval(index0, index1);
	}
	gestureStarted = true;
    }

    public void setValueIsAdjusting(boolean isAdjusting) {
	if (isAdjusting == false) {
	    gestureStarted = false;		
	}
    }
}
