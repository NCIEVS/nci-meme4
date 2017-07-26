/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  QueryAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Represents a query action.
 *
 * @author MEME Group
 */

public class QueryAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private String query = null;
  private String inverse_query = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link QueryAction} with no parameter.
   */
  private QueryAction() {}

  /**
   * Instantiates a {@link QueryAction} with
   * the specified query string.
   * @param query the query string
   */
  private QueryAction(String query) {
    this.query = query;
  }

  /**
   * Instantiates a {@link QueryAction} with the specified query
   * and inverse query string.
   * @param query the query string
   * @param inverse_query the query string
   */
  private QueryAction(String query, String inverse_query) {
    this.query = query;
    this.inverse_query = inverse_query;
  }

  //
  // Methods
  //

  /**
   * Performs an execute query action with no inverse
   * @param query the query string
   * @return an object {@link QueryAction}
   */
  public static QueryAction executeQueryAction(String query) {
    QueryAction qa = new QueryAction(query);
    return qa;
  }

  /**
   * Performs an execute query action with inverse
   * @param query the query string
   * @param inverse_query the inverse query string
   * @return an object {@link QueryAction}
   */
  public static QueryAction executeQueryAction(String query,
                                               String inverse_query) {
    QueryAction qa = new QueryAction(query, inverse_query);
    return qa;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    try {
      Statement stmt = mds.createStatement();
      stmt.executeUpdate(query);
      stmt.close();

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to execute query.", this, se);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    QueryAction qa = null;
    if (inverse_query != null)
      qa = executeQueryAction(inverse_query, query);
    else if (inverse_query == null) {
      return new NonOperationAction();
    }

    qa.setUndoActionOf(this);
    return qa;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {}

}