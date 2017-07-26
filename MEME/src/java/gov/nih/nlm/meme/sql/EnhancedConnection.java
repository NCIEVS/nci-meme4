/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  EnhancedConnection
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.exception.DataSourceException;
import oracle.sql.CLOB;

import java.io.BufferedReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import java.util.Stack;

/**
 * Enhanced version of the {@link Connection} implementation
 * in the java.sql package. It has the same functionality except that the that
 * the {@link #setAutoCommit(boolean)} method has "memory".
 *
 * In addition, there are {@link #restoreAutoCommit()} and
 * {@link #resetAutoCommit()} methods which bring back or reset the auto
 * commit memory.
 *
 * It contains some additional methods to handle CLOBs, a data type used for
 * character strings which could be longer than 4KB.
 * These methods use the oracle.sql package.
 *
 * @see Connection
 * @author MEME Group
 */
public class EnhancedConnection implements Connection {

  //
  // Fields
  //

  private Connection conn;
  private Stack stack = new Stack();
  protected Stack sort_area_stack = new Stack();
  protected Stack hash_area_stack = new Stack();

  /**
   * clob_insert_length: 4K is the limit of data to insert in a CLOB at once.
   */
  protected static int clob_insert_length = 3999;

  /**
   * max_int is a little smaller than the biggest int, so if a CLOB has length
   * bigger than max_int, we get it in pieces of length max_int.
   */
  protected static int max_int = Integer.MAX_VALUE / 100;

  //
  // Constructors
  //

  /**
   * EnhancedConnection wraps arround a {@link Connection}.
   * @param conn the {@link Connection} representation of connection.
   * @throws DataSourceException if failed to enhance connection.
   */
  public EnhancedConnection(Connection conn) throws DataSourceException {
    this.conn = conn;
    // Alter the session to use the MEME date format
    setDateFormat();
  }

  //
  // Public methods used for handling Clob's
  //

  /**
   * Given a {@link ResultSet} and a column_name this methods returns
   * a String representation of the data stored in this Clob if the column
   * contains CLOB values in the database.
   * @param clob the {@link CLOB} representation of clob.
   * @return the {@link String} representation of converted clob.
   * @throws DataSourceException if failed to convert clob as string.
   */
  public String getClobAsString(CLOB clob) throws DataSourceException {
    try {
      StringBuffer buffer = new StringBuffer();
      long length = clob.length();

      if (length <= max_int) {
        int le = (int) length;
        buffer.append(clob.getSubString(1, le));
      } else {
        long position = 1;
        do {
          buffer.append(clob.getSubString(position, max_int));
          position += max_int;
          length = length - max_int;
        } while (length > 0);
      }
      return buffer.toString();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to read CLOB.", this, se);
      throw dse;
    }
  }

  /**
   * Given a {@link ResultSet} and a column_name this methods returns
   * a BufferedReader containing the data stored in this Clob if the column
   * contains CLOB values in the database.
   * @param clob the {@link CLOB} representation of clob.
   * @return the {@link BufferedReader} representation of buffered reader.
   * @throws DataSourceException if failed to convert clob to buffer reader.
   */
  public BufferedReader getClobAsReader(CLOB clob) throws DataSourceException {
    try {
      return new BufferedReader(clob.getCharacterStream());
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to read CLOB.", this, se);
      throw dse;
    }
  }

  /**
       * Given a ResultSet rset this method insert the data in this "row" of rset and
   * in the column with number
   * columnnumber, if this column stores Clob values.
   * @param clob the {@link CLOB} representation of clob.
   * @param data the {@link String} representation of data string.
   * @throws DataSourceException if failed to write data into clob.
   */
  public void setClob(CLOB clob, String data) throws DataSourceException {
    try {
      //CLOB clob = (CLOB)rset.getObject(columnnumber);
      int temp_length = 0;
      while (data.length() - temp_length > clob_insert_length) {
        clob.putString(temp_length + 1, data.substring(
            temp_length, temp_length + clob_insert_length));
        temp_length += clob_insert_length;
      }
      clob.putString(temp_length + 1, data.substring(temp_length));
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to write CLOB.", this, se);
      dse.setDetail("data", data);
      throw dse;
    }
  }

  //
  // Public overridden method
  //

  /**
   * Works similar to the Connection.setAutoCommit() method, except
   * that the previous state is stored and can be restored using the
   * restoreAutoCommit() method.
   * @param auto_commit A <code>boolean</code> representation of auto commit.
   * @throws SQLException if failed to set auto commit.
   */
  public void setAutoCommit(boolean auto_commit) throws SQLException {
    stack.push(new Boolean(conn.getAutoCommit()));
    conn.setAutoCommit(auto_commit);
  }

  //
  // New public methods
  //

  /**
   * Restores the auto commit mode set before the last setAutoCommit() call.
   * Should not be used more times than the setAutoCommit method.
   * @throws DataSourceException if failed to restore auto commit.
   */
  public void restoreAutoCommit() throws DataSourceException {
    if (!stack.empty()) {
      boolean old_state = ( (Boolean) stack.pop()).booleanValue();
      try {
        conn.setAutoCommit(old_state);
      } catch (SQLException se) {
        DataSourceException dse = new DataSourceException(
            "Failed to set transaction mode.", this, se);
        Boolean b = new Boolean(old_state);
        dse.setDetail("auto_commit", b.toString());
        throw dse;
      }
    }
  }

  /**
       * Deletes the memory of the setAutoCommit() method and resets the auto_commit
   * mode to the state it had at the beginning.
   * @throws DataSourceException if failed to reset auto commit.
   */
  public void resetAutoCommit() throws DataSourceException {
    if (!stack.empty()) {
      boolean first_state = ( (Boolean) stack.firstElement()).booleanValue();
      try {
        conn.setAutoCommit(first_state);
      } catch (SQLException se) {
        DataSourceException dse = new DataSourceException(
            "Failed to set transaction mode.", this, se);
        Boolean b = new Boolean(first_state);
        dse.setDetail("auto_commit", b.toString());
        throw dse;
      }
      stack = new Stack();
    }
  }

  /**
   * Creates a database buffer of size 10000.
   * @throws DataSourceException if failed to enable buffer.
   */
  public void enableBuffer() throws DataSourceException {
    enableBuffer(100000);
  }

  /**
   * Creates a database output buffer of size "size" .
   * @param size buffer size
   * @throws DataSourceException if failed to enable buffer.
   */
  public void enableBuffer(int size) throws DataSourceException {
    StringBuffer call = new StringBuffer("{call DBMS_OUTPUT.enable(");
    call.append(size);
    call.append(")}");
    try {
      CallableStatement cstmt = prepareCall(call.toString());
      cstmt.execute();
      cstmt.close();
    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed while performing SQL operations.", this, se);
      dse.setDetail("call", call.toString());
      throw dse;
    }
  }

  /**
   * Flushes the database buffer to MEMEToolkit.trace(). So the information
   * stored in the database buffer can be seen from a Java program.
   * @return the {@link String} representation of buffered string.
   * @throws DataSourceException if failed to flush buffer.
   */
  public String flushBuffer() throws DataSourceException {
    try {
      CallableStatement cstmt = prepareCall("{call DBMS_OUTPUT.get_line(?,?)}");
      String line;
      int status;
      StringBuffer buffer = new StringBuffer();
      do {
        cstmt.registerOutParameter(1, Types.VARCHAR);
        cstmt.registerOutParameter(2, Types.INTEGER);
        cstmt.execute();
        line = cstmt.getString(1);
        status = cstmt.getInt(2);
        if (line != null) {
          buffer.append(line);
          buffer.append('\n');
        }
      } while (status != 1);
      cstmt.close();
      return buffer.toString();
    } catch (SQLException e) {
      String message = "Failed to set flush the database buffer.";
      DataSourceException dse = new DataSourceException(message, this, e);
      throw dse;
    }
  }

  /**
   * Determines if session is still active.
   * @return <code>true</code> if session is still active; <code>false</code>
   * otherwise.
   */
  public boolean isConnected() {
    Statement stmt = null;
    try {
      stmt = createStatement();
      stmt.execute("SELECT 1 FROM dual");
      stmt.close();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  //
  // Public methods got from implementation of Connection
  //

  /**
   * @see Connection#clearWarnings()
   * @throws SQLException if failed to clear warnings.
   */
  public void clearWarnings() throws SQLException {
    conn.clearWarnings();
  }

  /**
   * @see Connection#close()
   * @throws SQLException if failed to close.
   */
  public void close() throws SQLException {
    conn.close();
  }

  /**
   * @see Connection#commit()
   * @throws SQLException if failed to commit.
   */
  public void commit() throws SQLException {
    conn.commit();
  }

  /**
   * @see Connection#createStatement()
   * @return the {@link Statement}.
   * @throws SQLException if failed to create statement.
   */
  public Statement createStatement() throws SQLException {
    return conn.createStatement();
  }

  /**
   * @see Connection#createStatement(int,int)
   * @param resultSetType
   * @param resultSetConcurrency
   * @return the {@link Statement}
   * @throws SQLException if failed to create statement.
   */
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws
      SQLException {
    return conn.createStatement(resultSetType, resultSetConcurrency);
  }

  /**
   * see Connection#createStatement(int,int,int)
   * @param resultSetType
   * @param resultSetConcurrency
   * @param resultSetHoldability
   * @return the {@link Statement}
   * @throws SQLException if failed to create statement.
   */
  public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                   int resultSetHoldability) throws
      SQLException {
    return conn.createStatement(resultSetType, resultSetConcurrency,
                                resultSetHoldability);
  }

  /**
   * @see Connection#getAutoCommit()
   * @return auto commit status.
   * @throws SQLException if failed to perform auto commit.
   */
  public boolean getAutoCommit() throws SQLException {
    return conn.getAutoCommit();
  }

  /**
   * @see Connection#getCatalog()
   * @return the catalog.
   * @throws SQLException if failed to get catalog.
   */
  public String getCatalog() throws SQLException {
    return conn.getCatalog();
  }

  /**
   * see Connection#getHoldability()
   * @return holdability value.
   * @throws SQLException if failed to get holdability.
   */
  public int getHoldability() throws SQLException {
    return conn.getHoldability();
  }

  /**
   * @see Connection#getMetaData()
   * @return the {@link DatabaseMetaData}.
   * @throws SQLException if failed to get meta data.
   */
  public DatabaseMetaData getMetaData() throws SQLException {
    return conn.getMetaData();
  }

  /**
   * @see Connection#getTransactionIsolation()
   * @return the value of transaction isolation.
   * @throws SQLException if failed to get transaction isolation.
   */
  public int getTransactionIsolation() throws SQLException {
    return conn.getTransactionIsolation();
  }

  /**
   * @see Connection#getTypeMap()
   * @return the {@link Map}.
   * @throws SQLException if failed to get type map
   */
  public Map getTypeMap() throws SQLException {
    return conn.getTypeMap();
  }

  /**
   * @see Connection#getWarnings()
   * @return the {@link SQLWarning}.
   * @throws SQLException if failed to get warnings
   */
  public SQLWarning getWarnings() throws SQLException {
    return conn.getWarnings();
  }

  /**
   * @see Connection#isClosed()
   * @return the status
   * @throws SQLException if failed to determine if closed or not.
   */
  public boolean isClosed() throws SQLException {
    return conn.isClosed();
  }

  /**
   * @see Connection#isReadOnly()
   * @return the status of read only
   * @throws SQLException if failed to determine read only
   */
  public boolean isReadOnly() throws SQLException {
    return conn.isReadOnly();
  }

  /**
   * @see Connection#nativeSQL(String)
   * @param sql the native SQL to perform.
   * @return the native SQL.
   * @throws SQLException if failed to perform native SQL.
   */
  public String nativeSQL(String sql) throws SQLException {
    return conn.nativeSQL(sql);
  }

  /**
   * @see Connection#prepareCall(String)
   * @param sql the SQL to be prepare.
   * @return the {@link CallableStatement}.
   * @throws SQLException if failed to perform prepare call.
   */
  public CallableStatement prepareCall(String sql) throws SQLException {
    return conn.prepareCall(sql);
  }

  /**
   * @see Connection#prepareCall(String,int,int)
   * @param sql the sql to be prepare
   * @param resultSetType
   * @param resultSetConcurrency
   * @return the {@link CallableStatement}.
   * @throws SQLException if failed to perform prepare call.
   */
  public CallableStatement prepareCall(String sql, int resultSetType,
                                       int resultSetConcurrency) throws
      SQLException {
    return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  /**
   * @see Connection#prepareCall(String,int,int)
   * @param sql
   * @param resultSetType
   * @param resultSetConcurrency
   * @param resultSetHoldability
   * @return the {@link CallableStatement}.
   * @throws SQLException if failed to perform prepare call.
   */
  public CallableStatement prepareCall(String sql, int resultSetType,
                                       int resultSetConcurrency,
                                       int resultSetHoldability) throws
      SQLException {
    return conn.prepareCall(sql, resultSetType, resultSetConcurrency,
                            resultSetHoldability);
  }

  /**
   * @see Connection#prepareStatement(String)
   * @param sql
   * @return the {@link PreparedStatement}.
   * @throws SQLException if failed to prepare statement.
   */
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return conn.prepareStatement(sql);
  }

  /**
   * see Connection#prepareStatement(String, int)
   * @param sql
   * @param autoGeneratedKeys
   * @return PreparedStatement
   * @throws SQLException if failed to prepare statement
   */
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws
      SQLException {
    return conn.prepareStatement(sql, autoGeneratedKeys);
  }

  /**
   * see Connection#prepareStatement(String, int[])
   * @param sql
   * @param columnIndexes
   * @return PreparedStatement
   * @throws SQLException if failed to prepare statement
   */
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws
      SQLException {
    return conn.prepareStatement(sql, columnIndexes);
  }

  /**
   * see Connection#prepareStatement(String, String[])
   * @param sql
   * @param columnNames
   * @return PreparedStatement
   * @throws SQLException if failed to prepare statement
   */
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws
      SQLException {
    return conn.prepareStatement(sql, columnNames);
  }

  /**
   * @see Connection#prepareStatement(String,int,int)
   * @param sql
   * @param resultSetType
   * @param resultSetConcurrency
   * @return PreparedStatement
   * @throws SQLException if failed to prepare statement
   */
  public PreparedStatement prepareStatement(String sql, int resultSetType,
                                            int resultSetConcurrency) throws
      SQLException {
    return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  /**
   * @see Connection#prepareStatement(String,int,int)
   * @param sql
   * @param resultSetType
   * @param resultSetConcurrency
   * @param resultSetHoldability
   * @return PreparedStatement
   * @throws SQLException if failed to prepare statement
   */
  public PreparedStatement prepareStatement(String sql, int resultSetType,
                                            int resultSetConcurrency,
                                            int resultSetHoldability) throws
      SQLException {
    return conn.prepareStatement(sql, resultSetType, resultSetConcurrency,
                                 resultSetHoldability);
  }

  /**
   * see Connection#releaseSavepoint(Savepoint)
   * @param savepoint
   * @throws SQLException if failed to release save point
   */
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    conn.releaseSavepoint(savepoint);
  }

  /**
   * @see Connection#rollback()
   * @throws SQLException if failed to rollback changes.
   */
  public void rollback() throws SQLException {
    conn.rollback();
  }

  /**
   * see Connection#rollback(Savepoint)
   * @param savepoint
   * @throws SQLException if failed to rollback changes.
   */
  public void rollback(Savepoint savepoint) throws SQLException {
    conn.rollback(savepoint);
  }

  /**
   * @see Connection#setCatalog(String)
   * @param catalog
   * @throws SQLException if failed to set catalog.
   */
  public void setCatalog(String catalog) throws SQLException {
    conn.setCatalog(catalog);
  }

  /**
   * see Connection#setHoldability(int)
   * @param h
   * @throws SQLException if failed to set holdability.
   */
  public void setHoldability(int h) throws SQLException {
    conn.setHoldability(h);
  }

  /**
   * @see Connection#setReadOnly(boolean)
   * @param readOnly
   * @throws SQLException if failed to set read only.
   */
  public void setReadOnly(boolean readOnly) throws SQLException {
    conn.setReadOnly(readOnly);
  }

  /**
   * see Connection#setSavepoint()
   * @return the {@link Savepoint}
   * @throws SQLException if failed to set save point.
   */
  public Savepoint setSavepoint() throws SQLException {
    return conn.setSavepoint();
  }

  /**
   * see Connection#setSavepoint(String)
   * @param name
   * @return the {@link Savepoint}
   * @throws SQLException if failed to set save point.
   */
  public Savepoint setSavepoint(String name) throws SQLException {
    return conn.setSavepoint(name);
  }

  /**
   * @see Connection#setTransactionIsolation(int)
   * @param level
   * @throws SQLException if failed to set transaction isolation.
   */
  public void setTransactionIsolation(int level) throws SQLException {
    conn.setTransactionIsolation(level);
  }

  /**
   * @see Connection#setTypeMap(Map)
   * @param map the {@link Map}.
   * @throws SQLException if failed to set type map.
   */
  public void setTypeMap(Map map) throws SQLException {
    conn.setTypeMap(map);
  }

  /**
   * Restores the sort data area size for this connection.
   * @throws DataSourceException if failed to restore sort area size.
   */
  public void restoreSortAreaSize() throws DataSourceException {

    if (!sort_area_stack.empty()) {
      // Pop value of sort area size
      int sort_area_size = ( (Integer) sort_area_stack.pop()).intValue();

      // Alter session
      String query = "ALTER SESSION SET sort_area_size = " + sort_area_size;
      try {
        Statement stmt = createStatement();
        stmt.executeUpdate(query);
        stmt.close();
      } catch (SQLException e) {
        DataSourceException dse = new DataSourceException(
            "Failed to alter the session for sort area size.", this, e);
        dse.setDetail("query", query);
        throw dse;
      }

    }
  }

  /**
   * Sets the sort data area size for this connection.
   * @param sort_area_size the area size to be sorted.
   * @throws DataSourceException if failed to set sort area size.
   */
  public void setSortAreaSize(int sort_area_size) throws DataSourceException {

    // Get the current value of sort area size
    String query = "SELECT value FROM v$parameter " +
        "WHERE LOWER(name) = 'sort_area_size'";
    try {
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query);

      // Push the current value of sort area size into the stack
      if (rs.next()) {
        sort_area_stack.push(new Integer(rs.getInt("VALUE")));

      }
      stmt.close();
    } catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up the current value of sort area size.", this, e);
      dse.setDetail("query", query);
      throw dse;
    }

    // Alter session
    query = "ALTER SESSION SET sort_area_size = " + sort_area_size;
    try {
      Statement stmt = createStatement();
      stmt.executeUpdate(query);
      stmt.close();
    } catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Failed to alter the session for sort area size.", this, e);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /**
   * Restores the hash data area size for this connection.
   * @throws DataSourceException if failed to restore hash area size.
   */
  public void restoreHashAreaSize() throws DataSourceException {

    if (!hash_area_stack.empty()) {
      // Pop value of hash area size
      int hash_area_size = ( (Integer) hash_area_stack.pop()).intValue();

      // Alter session
      String query = "ALTER SESSION SET hash_area_size = " + hash_area_size;
      try {
        Statement stmt = createStatement();
        stmt.executeUpdate(query);
        stmt.close();
      } catch (SQLException e) {
        DataSourceException dse = new DataSourceException(
            "Failed to alter the session for hash area size.", this, e);
        dse.setDetail("query", query);
        throw dse;
      }

    }
  }

  /**
   * Sets the hash data area size for this connection.
   * @param hash_area_size the area size to be hashed.
   * @throws DataSourceException if failed to set hash area size.
   */
  public void setHashAreaSize(int hash_area_size) throws DataSourceException {

    // Get the current value of hash area size
    String query = "SELECT value FROM v$parameter " +
        "WHERE LOWER(name) = 'hash_area_size'";
    try {
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query);

      // Push the current value of hash area size into the stack
      if (rs.next()) {
        hash_area_stack.push(new Integer(rs.getInt("VALUE")));

      }
      stmt.close();
    } catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up the current value of hash area size.", this, e);
      dse.setDetail("query", query);
      throw dse;
    }

    // Alter session
    query = "ALTER SESSION SET hash_area_size = " + hash_area_size;
    try {
      Statement stmt = createStatement();
      stmt.executeUpdate(query);
      stmt.close();
    } catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Failed to alter the session for hash area size.", this, e);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  //
  // Methods
  //

  /**
       * Sets the database date format to 'DD-mon-YYYY HH24:MI:SS' for this connection.
   * (Also shorter version of it like 'DD-mon-YYYY' are then ok.) Is called in
   * the constructor of this class.
   * @throws DataSourceException if failed to set date format.
   */
  private void setDateFormat() throws DataSourceException {
    try {
      Statement stmt = createStatement();
      stmt.executeUpdate(
          "ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS'");
      stmt.close();
    } catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Failed to set date format.", this, e);
      dse.setDetail("query",
          "ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS'");
      throw dse;
    }
  }

}
