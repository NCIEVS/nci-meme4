/************************************************************************
 *
 * Package:     gov.nih.nlm.meme.sql
 * Object:      LongAttributeHandler.java
 *
 ***********************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Processes fields with "<>Long_Attribute<>:%' and looks up the
 * long attribute values in stringtab.
 */
public class LongAttributeHandler implements DataWriterHandler {

  //
  // The data source
  //
  public MEMEDataSource data_source;

  /**
   * Instantiates a {@link LongAttributeHandler} from the specified
   * {@link MEMEDataSource}
   * @param conn the {@link MEMEDataSource} to connect to
   */
  public LongAttributeHandler(MEMEDataSource conn) {
    data_source = conn;
  }

  /**
   * Looks up long attribute values in <tt>stringtab</tt>.
   * @param line the line to process.
   * @return the processed line.
   * @throws MEMEException if failed to process line.
   */
  public String processLine(String line) throws MEMEException {

    StringBuffer revised = new StringBuffer();
    FieldedStringTokenizer tokenizer =
        new FieldedStringTokenizer(line, String.valueOf('|'));

    //
    // For any field, if it contains a long attribute pointer
    // replace it with the full attribute value from stringtab.
    //
    while (tokenizer.hasMoreTokens()) {
      String attribute_value = tokenizer.nextToken();
      if (attribute_value.length() > 19 &&
          attribute_value.substring(0, 19).equals("<>Long_Attribute<>:")) {
        String hashcode = attribute_value.substring(19);
        StringBuffer query = new StringBuffer();
        query.append("SELECT row_sequence, text_total, text_value ");
        query.append("FROM mrd_stringtab WHERE hashcode = '");
        query.append(hashcode);
        query.append("' AND expiration_date IS NULL ");
        query.append("ORDER BY row_sequence ");

        try {
          Statement stmt = data_source.createStatement();
          ResultSet rset = stmt.executeQuery(query.toString());
          StringBuffer buffer = new StringBuffer();
          int count = 1;
          int test = 1;
          while (rset.next()) {
            if (rset.getInt("row_sequence") == count) {
              buffer.append(rset.getString("text_value"));
            } else {
              DataSourceException dse = new DataSourceException(
                  "mrd_stringtab table not in correct form ");
              dse.setDetail("hashcode", hashcode);
              throw dse;
            }
            count++;
            test = rset.getInt("text_total") - buffer.length();
          }
          if (test != 0) {
            DataSourceException dse = new DataSourceException(
                "mrd_stringtab table not in correct form ");
            dse.setDetail("hashcode", hashcode);
            throw dse;
          }
          attribute_value = buffer.toString();
          stmt.close();
        } catch (SQLException se) {
          DataSourceException dse = new DataSourceException(
              "Failed to get Long Attribute Value", this, se);
          dse.setDetail("hashcode", hashcode);
          throw dse;
        }
      }
      revised.append(attribute_value);
      revised.append("|");
    }
    return revised.toString();
  }
}