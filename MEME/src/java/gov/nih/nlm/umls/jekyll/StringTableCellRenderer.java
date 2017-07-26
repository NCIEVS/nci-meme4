/**
 * StringTableCellRenderer.java
 */
package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.umls.jekyll.swing.SortableJTable;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * A cell renderer for <code>JTable</code>-based components that renders
 * data according to values of particular fields. 
 */
public class StringTableCellRenderer extends DefaultTableCellRenderer {
    // ------------------------------------
    // Constants
    //  ------------------------------------
    static final String STATUS_IDENTIFIER = "Status";
    static final String TBR_IDENTIFIER = "Tbr";
    static final String UNDO_IDENTIFIER = "Undo";
    static final String SORT_RANK_IDENTIFIER = "Sort Rank";
    static final String SUPP_IDENTIFIER = "Suppressible";
    static final String SOURCE_IDENTIFIER = "Source";
    static final String BASE_AMBIGUITY_FLAG = "Base_ambig";
    // Fields
    private SortableJTable s_table = null;
    private TableModel model = null;
    protected int status_column = -1;
    protected int tbr_column = -1;
    protected int undo_column = -1;
    protected int sort_rank_column = -1;
    protected int supp_column = -1;
    protected int rsab_column = -1;
    protected int base_ambig_column = -1;

    // Constructor
    public StringTableCellRenderer(SortableJTable table, TableModel model) {
        s_table = table;
        this.model = model;
        TableColumnModel columnModel = s_table.getColumnModel();
        try {
            status_column = columnModel.getColumnIndex(STATUS_IDENTIFIER);
            tbr_column = columnModel.getColumnIndex(TBR_IDENTIFIER);
        } catch (IllegalArgumentException ex) {
            // print nothing
        }
    }

    public void setColumnIndex(String identifier, int index) {
        if ((identifier == null) || (identifier.equals(""))) {
            return;
        }
        if (identifier.equals(STATUS_IDENTIFIER)) {
            status_column = index;
        } else if (identifier.equals(TBR_IDENTIFIER)) {
            tbr_column = index;
        } else if (identifier.equals(UNDO_IDENTIFIER)) {
            undo_column = index;
        } else if (identifier.equals(SORT_RANK_IDENTIFIER)) {
            sort_rank_column = index;
        } else if (identifier.equals(SUPP_IDENTIFIER)) {
        	supp_column = index;
        } else if (identifier.equals(SOURCE_IDENTIFIER)) {
        	rsab_column = index;
        }else if (identifier.equals(BASE_AMBIGUITY_FLAG)) {
        	base_ambig_column = index;
        }
    }

    /**
     * Returns the default table cell renderer.
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        String status = null;
        String tbr = null;
        boolean undo = false;
        String sort_rank = null;
        String suppressible = null;
        String rsab = null;
        String baseAmbiguity = null;
        if (status_column != -1) {
            status = (String) model.getValueAt(s_table.mapIndex(row),
                    status_column);
        }
        if (tbr_column != -1) {
            tbr = (String) model.getValueAt(s_table.mapIndex(row), tbr_column);
        }
        if (undo_column != -1) {
            undo = ((Boolean) model.getValueAt(s_table.mapIndex(row),
                    undo_column)).booleanValue();
        }
        if (sort_rank_column != -1) {
            sort_rank = ((String) model.getValueAt(s_table.mapIndex(row),
                    sort_rank_column));
        }
        if (supp_column != -1) {
        	suppressible = ((String) model.getValueAt(s_table.mapIndex(row),
                    supp_column));
        }
        if (rsab_column != -1) {
        	rsab = ((String) model.getValueAt(s_table.mapIndex(row),
                    rsab_column));
        }
        if (base_ambig_column != -1) {
        	baseAmbiguity = ((String) model.getValueAt(s_table.mapIndex(row),
                    base_ambig_column));
        }

        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
        }
        if ((sort_rank != null)
                && (sort_rank.startsWith(RelationshipsFrame.JUST_EDITED))) {
            setForeground(new Color(252, 2, 251)); // Purple
            // color
        } else if (baseAmbiguity != null && tbr != null
        			&& (tbr.equals(String.valueOf(CoreData.FV_RELEASABLE)) || 
        					tbr.equals(String.valueOf(CoreData.FV_WEAKLY_RELEASABLE))) ){
      		setForeground(new Color(255, 128, 0)); // Orange
      	  /**
             * Soma: Adding a logic if the source is RxNorm, and the suppressiblity is 'O' set the row as purple.
             */
        } else if ((suppressible != null) &&
                suppressible.equals("O") &&
                rsab != null &&
                (rsab.startsWith("RXNORM") ||
                 rsab.startsWith("GO"))&& !((tbr != null)
                && tbr.equals(String.valueOf(CoreData.FV_WEAKLY_UNRELEASABLE))
                && ((status == null) || !status.equals(String
                        .valueOf(CoreData.FV_STATUS_DEMOTED))))){
      		setForeground(new Color(252, 2, 251)); // Purple
        // logic added on 12/22/2004:
        // if tbr == n and datum is a demotion, it should appear in red

        } else if ((tbr != null)
                && tbr.equals(String.valueOf(CoreData.FV_WEAKLY_UNRELEASABLE))
                && ((status == null) || !status.equals(String
                        .valueOf(CoreData.FV_STATUS_DEMOTED)))) {
            setForeground(Color.green);
        } else if ((sort_rank != null)
                && (sort_rank.startsWith(RelationshipsFrame.NEEDS_REVIEW))) {
            setForeground(Color.red);
          
        } else if ((status != null)
                && (status.equals(String
                        .valueOf(CoreData.FV_STATUS_NEEDS_REVIEW)) || status
                        .equals(String.valueOf(CoreData.FV_STATUS_DEMOTED)))) {
            setForeground(Color.red);

        
        } else {
            setForeground(table.getForeground());
        }
        if (undo && !isSelected) {
            setBackground(GUIToolkit.LIGHT_BROWN);
            setForeground(Color.white);
        }
        setFont(table.getFont());
        setBorder(GUIToolkit.EMPTY_BORDER);
        setValue(value);
        return this;
    }
}