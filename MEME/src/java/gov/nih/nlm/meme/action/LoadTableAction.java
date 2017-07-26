/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  LoadTableAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.ActionEngine;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a load table action.
 *
 * @author MEME Group
 */

public class LoadTableAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private String table = null;
  private String[] fields = null;
  private String data = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link LoadTableAction} with no parameter.
   */
  private LoadTableAction() {}

  /**
   * Instantiates a {@link LoadTableAction} with the specified table.
   * @param table the table to be use to insert data
   */
  private LoadTableAction(String table) {
    this.table = table;
  }

  /**
   * Instantiates a {@link LoadTableAction} with
   * the specified table and fields.
   * @param table the table to be use to insert data
   * @param fields the fields to be use to insert data
   */
  private LoadTableAction(String table, String[] fields) {
    this.table = table;
    this.fields = fields;
  }

  //
  // Methods
  //

  /**
   * Set the action mode.
   * @param mode the action mode
   */
  private void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * Performs a create table action.
   * @param table the table to create
   * @return an action to create a table
   */
  public static LoadTableAction newCreateTableAction(String table) {
    LoadTableAction lta = new LoadTableAction(table);
    lta.setMode("CREATE");
    lta.setIsImplied(true);
    return lta;
  }

  /**
   * Performs a new insert data action
   * @param table the table to be use to insert data
   * @return an object {@link LoadTableAction}
   */
  public static LoadTableAction newInsertDataAction(String table) {
    LoadTableAction lta = new LoadTableAction(table);
    lta.setMode("SELECT");
    lta.setIsImplied(true);
    return lta;
  }

  /**
   * Performs a new insert data action
   * @param table the table to be use to insert data
   * @param fields the fields to be use to insert data
   * @return an object {@link LoadTableAction}
   */
  public static LoadTableAction newInsertDataAction(String table,
      String[] fields) {
    LoadTableAction lta = new LoadTableAction(table, fields);
    lta.setMode("INSERT");
    return lta;
  }

  /**
   * Adds a row
   * @param s the rows to add
   */
  public void setContents(String s) {
    data = s;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {

    if (mode.equals("INSERT")) {

      // Map insert string
      StringBuffer sb = new StringBuffer(500);
      sb.append("INSERT INTO ")
          .append(table)
          .append(" (");
      for (int i = 0; i < fields.length; i++) {
        if (i != fields.length)
          sb.append(",");
        sb.append(fields);
      }
      sb.append(") VALUES (");
      for (int i = 0; i < fields.length; i++) {
        if (i != fields.length)
          sb.append(",");
        sb.append("?");
      }
      sb.append(")");

      PreparedStatement pstmt = null;
      try {
        pstmt = mds.prepareStatement(sb.toString());
        String[] rows = FieldedStringTokenizer.split(data, "\n");
        for (int i = 0; i < rows.length; i++) {
          String[] values = FieldedStringTokenizer.split(rows[i], "|");
          pstmt.clearParameters();
          for (int j = 0; j < fields.length; j++) {
            pstmt.setString(j, values[j]);
          }
          pstmt.executeUpdate();
        }
        pstmt.close();
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to insert row to " + table + ".", sb.toString(), se);
        dse.setDetail("insert", sb.toString());
        throw dse;
      }

    } else if (mode.equals("SELECT")) {

      //
      // Get column names
      //
      final List column_names = new ArrayList();
      PreparedStatement pstmt = null;
      try {
        final String query =
            "SELECT column_name FROM user_tab_columns" +
            " WHERE table_name = upper(?)";
        pstmt = mds.prepareStatement(query);
        pstmt.setString(1, table);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next())
          column_names.add(rs.getString("COLUMN_NAME"));
        pstmt.close();
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to look up column names", table, se);
        dse.setDetail("table", table);
        throw dse;
      }

      //
      // Create/log actions for loading table data
      //
      fields = (String[])column_names.toArray(new String[0]);
      final String query = "SELECT * FROM " + table;
      pstmt = null;
      try {
        pstmt = mds.prepareStatement(query);
        final ResultSet rs = pstmt.executeQuery();
        LoadTableAction lta = LoadTableAction.newInsertDataAction(table, fields);
        lta.setAuthority(getAuthority());
        lta.setParent(getParent());
        Date start = new Date();
        lta.setTimestamp(start);
        StringBuffer sb = new StringBuffer(110000);
        final ActionEngine ae = mds.getActionEngine();
        while (rs.next()) {
          // Every 10K rows, log another action to keep total size down
          if (sb.length() > 100000) {
            lta.setContents(sb.toString());
            lta.setElapsedTime(new Date().getTime() - start.getTime());
            ae.logAction(lta);
            lta = LoadTableAction.newInsertDataAction(table, fields);
            lta.setAuthority(getAuthority());
            lta.setParent(getParent());
            start = new Date();
            lta.setTimestamp(start);
            sb = new StringBuffer(110000);
          }
          for (int i = 0; i < column_names.size(); i++) {
            sb.append(rs.getString( (String)column_names.get(i))).append("|");
          }
          sb.append("\n");
        }
        // Log final action
        lta.setContents(sb.toString());
        lta.setElapsedTime(new Date().getTime() - start.getTime());
        ae.logAction(lta);

        // Close statement
        pstmt.close();
      } catch (ActionException ae) {
        DataSourceException dse = new DataSourceException(
            "Failed to log action.", this, ae);
        throw dse;
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to look up " + table + ".", table, se);
        dse.setDetail("query", query);
        throw dse;
      }

    } else if (mode.equals("CREATE")) {

      //
      // Get column names, data types, and lengths
      //
      StringBuffer sb = new StringBuffer(1000);
      sb.append("CREATE TABLE ").append(table).append(" ( ");
      PreparedStatement pstmt = null;
      Date start = new Date();
      try {
        final String query =
            "SELECT column_name, data_type, data_length " +
            "FROM user_tab_columns " +
            "WHERE table_name = upper(?)";
        pstmt = mds.prepareStatement(query);
        pstmt.setString(1, table);
        ResultSet rs = pstmt.executeQuery();
        int ct = 0;
        while (rs.next()) {
          if (ct != 0)
            sb.append(", ");
          ct++;
          final String name = rs.getString("COLUMN_NAME");
          final String type = rs.getString("DATA_TYPE");
          final String len = rs.getString("DATA_LENGTH");
          sb.append(name).append(" ")
              .append(type).append(" ")
              .append(type.equals("DATE") ? "" : "(" + len + ")");
        }
        sb.append(")");
        pstmt.close();
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to look up column names", table, se);
        dse.setDetail("table", table);
        throw dse;
      }

      QueryAction qa = QueryAction.executeQueryAction(sb.toString());
      qa.setAuthority(getAuthority());
      qa.setParent(getParent());
      qa.setTimestamp(start);
      qa.setElapsedTime(new Date().getTime() - start.getTime());
      try {
        mds.getActionEngine().logAction(qa);
      } catch (ActionException ae) {
        DataSourceException dse = new DataSourceException(
            "Failed to log CREATE TABLE action.", this, ae);
        throw dse;
      }

    } // end if (mode.equals(...))
  } // end performAction

  public void getInitialState(MEMEDataSource mds) {}

}