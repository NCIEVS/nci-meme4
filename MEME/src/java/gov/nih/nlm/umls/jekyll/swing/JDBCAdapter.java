/************************************************************************
 *
 * Package:     gov.nih.nlm.umls.meta.vlad.swing
 * Object:      JDBCAdaptor
 *
 * Author:      Vladimir Olenichev
 *
 * Remarks:     
 *
 * Change History: 
 *
 ***********************************************************************/

package gov.nih.nlm.umls.jekyll.swing;

import java.util.Vector;
import java.sql.*;
import javax.sql.DataSource;
import javax.swing.table.AbstractTableModel;

/**
 * An adaptor, transforming the JDBC interface to the TableModel interface.
 * Most of the code harvested from SDK distribution. Read-only version,
 * i.e., rows are non-editable.
 *
 * <p>
 * {@link <a href="/vlad-doc/jekyll/src_files/Swing/JDBCAdaptor.java.html">Browse Source</a>}
 *
 * @author Philip Milne, Vladimir Olenichev
 */
public class JDBCAdapter extends AbstractTableModel {

    /**
     * A connection (session) with a specific database.
     */
    Connection conn = null;

    /**
     * An object used for executing a static SQL
     * statement.
     */
    Statement stmt = null;

    /**
     * A table of data representing a database
     * result set.
     */
    ResultSet rs = null;

    /**
     * An array of column names.
     */
    // TODO: proper initialization value?
    String[] columnNames = {};

    /**
     * An array of table rows.
     */
    Vector rows = new Vector();

    /**
     * Contains information about the types and
     * properties of the columns in a <code>ResultSet</code>
     * object.
     */
    ResultSetMetaData rsmd = null;

    //
    // Constructors
    //
    /**
     * Default consturctor, which uses <code>DriverManager</code>
     * facility to initiate database session.
     *
     * @param url URL of the database connection string
     * @param driverName a class name for JDBC driver
     * @param user user name
     * @param passwd login password for the user name
     */
    public JDBCAdapter(String url, String driverName,
                       String user, String passwd) {
        try {
            Class.forName(driverName);
            System.out.println("Opening db connection");

            conn = DriverManager.getConnection(url, user, passwd);
            stmt = conn.createStatement();
        } catch (ClassNotFoundException ex) {
            System.err.println("Cannot find the database driver classes.");
            System.err.println(ex);
        } catch (SQLException ex) {
            System.err.println("Cannot connect to this database.");
            System.err.println(ex);
        }
    }

    /**
     * Creates model using the specific database
     * data source.
     */
    public JDBCAdapter(DataSource ds) {
	try {
	    conn = ds.getConnection();
	    stmt = conn.createStatement();
	} catch (SQLException ex) {
	    System.err.println("Unable connect to the database.");
	    System.err.println(ex);
	}
    }

    /**
     * Executes a given query and notifies all registered
     * listeners of this model.
     *
     * @param query a query to execute
     */
    public void executeQuery(String query) {

        if (conn == null || stmt == null) {
            System.err.println("There is no database to execute the query.");
            return;
        }

        try {
            rs = stmt.executeQuery(query);
            rsmd = rs.getMetaData();

            int numberOfColumns =  rsmd.getColumnCount();
            columnNames = new String[numberOfColumns];
            // Get the column names and cache them.
            // Then we can close the connection.
            for(int column = 0; column < numberOfColumns; column++) {
                columnNames[column] = rsmd.getColumnLabel(column+1);
            }

            // Get all rows.
	    rows.removeAllElements();
            while (rs.next()) {
                Vector newRow = new Vector();
                for (int i = 1; i <= getColumnCount(); i++) {
	            newRow.addElement(rs.getObject(i));
                }
                rows.addElement(newRow);
            }

            //  close(); Need to copy the metaData, bug in jdbc:odbc driver.
	    // Tell the listeners a new table has arrived.
            fireTableChanged(null);
        } catch (SQLException ex) {
            System.err.println(ex);
        } catch (Exception ex) {
	    System.err.println(ex);
	}
    } // executeQuery();

    /**
     * Releses JDBC resources immediately instead of waiting for
     * them to be garbage collected.
     *
     * @throws SQLException if a database access error occurs
     */
    public void close() throws SQLException {
//         System.out.println("Closing db connection");
        rs.close();
        stmt.close();
        conn.close();
    }

    /**
     * Releases JDBC resources and calls on <code>finalize</code>
     * method of the superclass.
     *
     * @throws Throwable an <code>Exception</code> raised by
     * this method.
     */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    //
    // Interface Implementation
    //
    /**
     * Returns a name for the column.
     *
     * @param column the column being queried
     */
    public String getColumnName(int column) {
        if (columnNames[column] != null) {
            return columnNames[column];
        } else {
            return "";
        }
    }

    /**
     * Returns an appropriate data type for the
     * specified column. This helps <code>JTable</code>
     * to display data in the best format.
     *
     * @param c the column being queried
     */
    public Class getColumnClass(int c) {
	return getValueAt(0, c).getClass();
    }

    /**
     * Returns the number of columns in the model.
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Returns the number of rows in the model.
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code>
     * and <code>rowIndex</code>.
     *
     * @param rowIndex the row whose value is being queried
     * @param columnIndex the column whose value is being queried
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Vector row = (Vector)rows.elementAt(rowIndex);
        return row.elementAt(columnIndex);
    }
}
