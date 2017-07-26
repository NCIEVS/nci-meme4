/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  LongAttributeMapper
 *
 * Changes
 *   12/22/2005 BAC: (1-719SB) don't fail on text_total error, just report to
 *                   administrator and keep going.
 *                   
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.ContextFormatter;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MissingDataException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This mapping function assigns long attributes
 * from the <>Long_Attribute<>: pointers in an {@link Attribute}.
 *
 * @author MEME Group
 */

public class LongAttributeMapper extends AttributeMapper.Default implements
    MEMEConnectionQueries {

  //
  // Fields
  //

  private PreparedStatement long_attribute_pstmt = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link LongAttributeMapper} connected
   * to the specified data source.
   * @param mds a {@link MEMEDataSource}
   * @throws SQLException if there are any errors reading the
   * long attribute value
   */
  public LongAttributeMapper(MEMEDataSource mds) throws SQLException {
    long_attribute_pstmt = mds.prepareStatement(READ_LONG_ATTRIBUTE);
  }

  /**
   * Closes the prepared statement.
   * @throws SQLException if failed to closes the statement.
   */
  public void close() throws SQLException {
    long_attribute_pstmt.close();
  }

  /**
   * Populates the long attribute.
   * @param rs the {@link ResultSet} to map from
   * @param mds the {@link MEMEDataSource}
   * @param attr the attribute to look up the long value for
   * @return a status value
   * @throws SQLException if sql process failed
   * @throws DataSourceException if populate failed
   * @throws BadValueException if failed due to invalid values
   */
  public boolean populate(ResultSet rs, MEMEDataSource mds, Attribute attr) throws
      SQLException, DataSourceException, BadValueException {

    int string_id = 0;
    try {

      string_id = Integer.valueOf(attr.getValue().substring(19)).intValue();
      long_attribute_pstmt.setInt(1, string_id);
      ResultSet long_attribute_rs = long_attribute_pstmt.executeQuery();

      int row_count = 0;
      StringBuffer sb = new StringBuffer(1784);
      int text_total = 0;

      // Read
      while (long_attribute_rs.next()) {
        text_total = long_attribute_rs.getInt("TEXT_TOTAL");
        sb.append(long_attribute_rs.getString("TEXT_VALUE"));
        row_count++;
      }

      // The length of the string buffer should match the text total
      if (sb.length() != text_total) {
        //long_attribute_pstmt.close();
        DataSourceException dse = new DataSourceException(
            "Long attribute text value length does not match text_total.");
        //throw dse;
        dse.setFatal(false);
        dse.setInformAdministrator(true);
        dse.setInformUser(false);
        dse.setDetail("concept",attr.getConcept().getIdentifier().toString());
        MEMEToolkit.handleError(dse);
      }

      // At least one row should've been read
      if (row_count < 1) {
        long_attribute_pstmt.close();
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("attribute_id", attr.getIdentifier());
        throw dse;
      }

      // Set new attribute value
      attr.setValue(sb.toString());

      if (attr.getName().equals(Attribute.CONTEXT) &&
          attr.getValue().indexOf('\t') != -1 &&
          attr.getAtom().getTermgroup() != null && attr.getAtom().getCode() != null) {
        attr.setValue
            (ContextFormatter.formatContext(
            attr.getValue(),
            attr.getSource().getSourceAbbreviation() + "/" +
            attr.getAtom().getTermgroup().toString(),
            attr.getAtom().getCode().toString()));
      }

    } catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to load long attribute.", this, se);
      dse.setDetail("query", READ_LONG_ATTRIBUTE);
      dse.setDetail("string_id", Integer.toString(string_id));
      throw dse;
    }

    return true;
  }

}