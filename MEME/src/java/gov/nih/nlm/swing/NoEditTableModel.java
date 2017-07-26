/**************************************************************************
 *
 * Package:    gov.nih.nlm.swing;
 * Interface:  NoEditTableModel.java
 *
 *************************************************************************/
package gov.nih.nlm.swing;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 *
 * {@link DefaultTableModel} that is not editable.
 *
 * @author Brian Carlsen
 **/
public class NoEditTableModel extends DefaultTableModel {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link NoEditTableModel}.
   */
  public NoEditTableModel() {
    super();
  }

  /**
       * Instantiates a  {@link NoEditTableModel} with the specified data and headers.
   * @param data An {@link Object}<code>[][]</code> containing table data
   * @param headers An  {@link Object}<code>[]</code> containing column headers
   */
  public NoEditTableModel(Object[][] data, Object[] headers) {
    super(data, headers);
  }

  /**
       * Instantiates a  {@link NoEditTableModel} with the specified data and headers.
   * @param headers  An {@link Object}<code>[]</code> containing column headers
   * @param rows An <code>int</code> row count
   */
  public NoEditTableModel(Object[] headers, int rows) {
    super(headers, rows);
  }

  /**
   * Instantiates a  {@link NoEditTableModel} with the specified data and row count.
   * @param data An {@link Vector} containing table data
   * @param rows An <code>int</code> row count
   */
  public NoEditTableModel(Vector data, int rows) {
    super(data, rows);
  }

  /**
       * Instantiates a  {@link NoEditTableModel} with the specified data and headers.
   * @param data An {@link Vector} containing table data
   * @param headers An {@link Vector} containing column headers
   */
  public NoEditTableModel(Vector data, Vector headers) {
    super(data, headers);
  };

  //
  // Overridden Methods
  //

  /**
   * Indicates that cells are NOT editable.
   * @param row the row index
   * @param col the column index
   * @return <code>false</code>.
   */
  public boolean isCellEditable(int row, int col) {
    return false;
  }
}
