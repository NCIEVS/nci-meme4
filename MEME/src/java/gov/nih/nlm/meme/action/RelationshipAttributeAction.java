/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  RelationshipAttributeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
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

public class RelationshipAttributeAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private String rela = null;
  private String inverse = null;
  private int rank = 0;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link RelationshipAttributeAction} with
   * the specified relationship.
   * @param rela the relationship attribute
   * @param inverse the inverse relationship
   * @param rank the rank
   */
  private RelationshipAttributeAction(String rela, String inverse, int rank) {
    this.rela = rela;
    this.inverse = inverse;
    this.rank = rank;
  }

  /**
   * Instantiates an {@link RelationshipAttributeAction} with
   * the specified relationship.
   * @param rela the relationship attribute
   */
  private RelationshipAttributeAction(String rela) {
    this.rela = rela;
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
   * Performs a new add relationship attribute action
   * @param rela the relationship attribute
   * @param inverse the inverse relationship
   * @param rank the rank
   * @return an object {@link RelationshipAttributeAction}
   */
  public static RelationshipAttributeAction
      newAddRelationshipAttributeAction(String rela, String inverse, int rank) {
    RelationshipAttributeAction raa = new RelationshipAttributeAction(rela,
        inverse, rank);
    raa.setMode("ADD");
    return raa;
  }

  /**
   * Performs a new remove relationship attribute action
   * @param rela the relationship attribute
   * @return an object {@link RelationshipAttributeAction}
   */
  public static RelationshipAttributeAction
      newRemoveRelationshipAttributeAction(String rela) {
    RelationshipAttributeAction raa = new RelationshipAttributeAction(rela);
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
      mds.addRelationshipAttribute(rela, inverse, rank);
    else if (mode.equals("REMOVE"))
      mds.removeRelationshipAttribute(rela);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    RelationshipAttributeAction raa = null;
    if (mode.equals("ADD"))
      raa = newRemoveRelationshipAttributeAction(rela);
    else if (mode.equals("REMOVE"))
      raa = newAddRelationshipAttributeAction(rela, inverse, rank);

    raa.setUndoActionOf(this);
    return raa;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("REMOVE")) {
      final String query = "SELECT a.rank " +
          "FROM inverse_rel_attributes a, inverse_rel_attributes b " +
          "WHERE a.relationship_attribute = ? " +
          "AND b.inverse_rel_attribute = a.relationship_attribute";

      PreparedStatement pstmt = null;
      try {
        pstmt = mds.prepareStatement(query);
        pstmt.setString(1, rela);

        ResultSet rs = pstmt.executeQuery();

        // Read
        while (rs.next()) {
          rank = rs.getInt("RANK");
        }

        // Close statement
        pstmt.close();

        inverse = mds.getInverseRelationshipAttribute(rela);

      } catch (BadValueException bve) {
        throw new DataSourceException(bve.getMessage(), rela, bve);
      } catch (SQLException se) {
        try {
          pstmt.close();
        } catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to look up inverse relationship attribute.", query, se);
        dse.setDetail("query", query);
        dse.setDetail("relationship attribute", rela);
        throw dse;
      }
    }
  }

}