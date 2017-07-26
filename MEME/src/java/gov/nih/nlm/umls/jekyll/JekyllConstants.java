/**
 * JekyllConstants.java
 */

package gov.nih.nlm.umls.jekyll;

import java.awt.Color;
import java.awt.Font;

/**
 * This interface holds constants, which are used throughout the application.
 * 
 * @see <a href="src/JekyllConstants.java.html">source </a>
 */
public interface JekyllConstants {

    public static final String SFO_LFO_NAME = "SFO/LFO";

    /**
     * Font for buttons.
     */
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.PLAIN, 14);

    /**
     * Font for table data.
     */
    public static final Font TABLE_FONT = new Font("Dialog", Font.PLAIN, 14);

    /**
     * Background color for text fields, labels.
     */
    //     public static final Color LABEL_BKG= new Color(245,245,220); // beige
    public static final Color LABEL_BKG = Color.yellow;

    /**
     * Foreground color for text fields, labels.
     */
    public static final Color LABEL_FG = Color.blue;

    /**
     * Light brown background color for some buttons.
     */
    public static final Color LIGHT_BROWN_BKG = new Color(204, 153, 051);

    /**
     * Light purple background for the relationships that have just been edited.
     */
    public static final Color PURPLE_BKG = new Color(252, 2, 251);

    /**
     * Exception message indicating that specified data is not found in the mid.
     */
    public static final String MISSING_DATA_MSG = "Missing Data.";

    /**
     * Value in the MID to indicate that data needs to be looked at by an editor
     * and approved.
     */
    public static final char UNAPPROVED = 'N';

    /**
     * Value in the MID indicating that data is approved.
     */
    public static final char APPROVED = 'R';

    /**
     * Value in the MID indicating that data has not been looked at by an editor
     * and will not be looked at by an editor.
     */
    public static final char UNREVIEWED = 'U';

    public static final char SUGGESTED = 'S';

    /**
     * Value in the MID to indicate that core data will not be released in the
     * next version of Meta.
     */
    public static final char UNRELEASABLE = 'n';

    /**
     * Value in the MID for broader relationship between two concepts.
     */
    public static final String BROADER = "BT";

    /**
     * Value in the MID for narrower relationship between two concepts.
     */
    public static final String NARROWER = "NT";

    /**
     * Value in the MID for other relationship between two concepts.
     */
    public static final String OTHER_RELATED = "RT";

    /**
     * Value in the MID for non-existent relationship between two concepts.
     */
    public static final String NOT_RELATED = "XR";

    public static final String BEQUEATHED_BROADER = "BBT";

    public static final String BEQUEATHED_NARROWER = "BNT";

    public static final String BEQUEATHED_RELATED = "BRT";

    /**
     * Value in the MID indicating concept level core data.
     */
    public static final char CONCEPT_LEVEL = 'C';

    /**
     * Value in the MID indicating source level core data.
     */
    public static final char SOURCE_LEVEL = 'S';
}