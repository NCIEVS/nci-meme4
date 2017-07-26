/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  RelationshipNameAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents an integrity check action.
 *
 * @author MEME Group
 */

public class RelationshipNameAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private String name = null;
  private String inverse_name = null;
  private boolean weak = false;
  private String long_name = null;
  private String inverse_long_name = null;
  private String release_name = null;
  private String inverse_release_name = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link RelationshipNameAction} with
   * the specified relationship.
   * @param name the relationship name
   * @param inverse_name The relationship inverse name
   * @param weak The <code>boolean</code> represents weak flag
   * @param long_name The relationship long name
   * @param inverse_long_name The relationship inverse long name
   * @param release_name The relationship release name
   * @param inverse_release_name The relationship inverse release name
   */
  private RelationshipNameAction(String name, String inverse_name, boolean weak,
                                 String long_name, String inverse_long_name,
                                 String release_name,
                                 String inverse_release_name) {
    this.name = name;
    this.inverse_name = inverse_name;
    this.weak = weak;
    this.long_name = long_name;
    this.inverse_long_name = inverse_long_name;
    this.release_name = release_name;
    this.inverse_release_name = inverse_release_name;
  }

  /**
   * Instantiates an {@link RelationshipNameAction} with
   * the specified relationship.
   * @param name the relationship name
   */
  private RelationshipNameAction(String name) {
    this.name = name;
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
   * Performs a new add relationship name action
   * @param name the relationship name
   * @param inverse_name The relationship inverse name
   * @param weak The <code>boolean</code> represents weak flag
   * @param long_name The relationship long name
   * @param inverse_long_name The relationship inverse long name
   * @param release_name The relationship release name
   * @param inverse_release_name The relationship inverse release name
   * @return an object {@link RelationshipNameAction}
   */
  public static RelationshipNameAction newAddRelationshipNameAction(
      String name, String inverse_name, boolean weak, String long_name,
      String inverse_long_name, String release_name,
      String inverse_release_name) {

    RelationshipNameAction raa = new RelationshipNameAction(name, inverse_name,
        weak, long_name, inverse_long_name, release_name, inverse_release_name);
    raa.setMode("ADD");
    return raa;
  }

  /**
   * Performs a new remove relationship name action
   * @param name the relationship name
   * @return an object {@link RelationshipNameAction}
   */
  public static RelationshipNameAction newRemoveRelationshipNameAction(String
      name) {
    RelationshipNameAction raa = new RelationshipNameAction(name);
    raa.setMode("REMOVE");
    return raa;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      mds.addRelationshipName(name, inverse_name, weak, long_name,
                              inverse_long_name, release_name,
                              inverse_release_name);
    else if (mode.equals("REMOVE"))
      mds.removeRelationshipName(name);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    RelationshipNameAction rna = null;
    if (mode.equals("ADD"))
      rna = newRemoveRelationshipNameAction(name);

    else if (mode.equals("REMOVE"))
      rna = newAddRelationshipNameAction(
          name, inverse_name, weak, long_name, inverse_long_name, release_name,
          inverse_release_name);

    rna.setUndoActionOf(this);
    return rna;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {

    if (mode.equals("REMOVE")) {

      final String query =
          "SELECT a.inverse_name, a.release_name, a.weak_flag, " +
          "b.long_name AS inverse_long_name, b.release_name AS inverse_release_name " +
          "FROM inverse_relationships a, inverse_relationships b " +
          "WHERE a.relationship_name = ? " +
          "AND b.inverse_name = a.relationship_name";

      PreparedStatement pstmt = null;
      try {
        pstmt = mds.prepareStatement(query);
        pstmt.setString(1, name);

        ResultSet rs = pstmt.executeQuery();

        int row_count = 0;
        // Read
        while (rs.next()) {
          row_count++;
          inverse_name = rs.getString("INVERSE_NAME");
          release_name = rs.getString("RELEASE_NAME");
          weak = Boolean.valueOf(rs.getString("WEAK_FLAG")).booleanValue();
          inverse_long_name = rs.getString("INVERSE_LONG_NAME");
          inverse_release_name = rs.getString("INVERSE_RELEASE_NAME");
        }

        // Close statement
        pstmt.close();

      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to look up inverse relationship name.", query, se);
        dse.setDetail("query", query);
        dse.setDetail("name", name);
        throw dse;
      }
    }

  }

}