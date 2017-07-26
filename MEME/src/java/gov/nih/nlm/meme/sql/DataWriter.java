/******************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  DataWriter.java
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.util.OrderedHashMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Iterator;

/**
 * Generically represents a way of writing tables or queries from a database
 * to a data file.  Implementation leverages the {@link DataWriterConstraints}.
 */
public interface DataWriter {

  /**
   * Writes out all fields and rows of a table using the default field
   * separator '|' to a file.
   * @param file the {@link File} to write to
   * @param table_name the table to read from
   * @throws MEMEException if failed to write
   */
  public void write(File file, String table_name) throws MEMEException;

  /**
   * Writes out all fields and rows of a table using the specified constraints.
   * @param file the {@link File} to write to
   * @param table_name the table to read from
   * @param constraints the {@link DataWriterConstraints}
   * @throws MEMEException if failed to write
   */
  public void write(File file, String table_name,
                    DataWriterConstraints constraints) throws MEMEException;

  /**
   * Writes out all fields and rows of multiple tables using the specified constraints.
   * @param file the {@link File} to write to
   * @param tables_names the tables to read from
   * @param constraints the {@link DataWriterConstraints}
   * @throws MEMEException if failed to write
   */
  public void write(File file, String[] tables_names,
                    DataWriterConstraints constraints) throws MEMEException;

  /**
   * Default implementation of {@link DataWriter}.
   */
  public class Default implements DataWriter {

    //
    // The data source to read from
    //
    private MEMEDataSource data_source;

    /**
     * Instantiates a default {@link DataWriter} from the specified
     * {@link MEMEDataSource}
     * @param data_source a {@link MEMEDataSource}
     */
    public Default(MEMEDataSource data_source) {
      this.data_source = data_source;
    }

    /**
     * Writes out all fields and rows of a table using the default field
     * separator '|' to a file.
     * @param file the {@link File} to write to
     * @param table_name the table to read from
     * @throws MEMEException if failed to write
     */
    public void write(File file, String table_name) throws MEMEException {
      write(file, (Object) table_name, new DataWriterConstraints());
    }

    /**
         * Writes out all fields and rows of a table using the specified constraints.
     * @param file the {@link File} to write to
     * @param table_name the table to read from
     * @param constraints the {@link DataWriterConstraints}
     * @throws MEMEException if failed to write
     */
    public void write(File file, String table_name,
                      DataWriterConstraints constraints) throws MEMEException {
      write(file, (Object) table_name, constraints);
    }

    /**
     * Writes out all fields and rows of multiple tables using the specified constraints.
     * @param file the {@link File} to write to
     * @param table_names the tables to read from
     * @param constraints the {@link DataWriterConstraints}
     * @throws MEMEException if failed to write
     */
    public void write(File file, String[] table_names,
                      DataWriterConstraints constraints) throws MEMEException {
      write(file, (Object) table_names, constraints);
    }

    /**
     * Performs the actual work of writing the file.
     * @param file the {@link File} to write to
     * @param obj the table or tables
     * @param constraints the {@link DataWriterConstraints}
     * @throws MEMEException if failed to write
     */
    private void write(File file, Object obj, DataWriterConstraints constraints) throws
        MEMEException {
      try {

        //
        // Get handlers
        //
        DataWriterHandler[] handlers = (DataWriterHandler[])
            constraints.handler_list.toArray(new DataWriterHandler[0]);

        //
        // Open file for writing
        //
        PrintWriter out =
            new PrintWriter(
            new OutputStreamWriter(
            new FileOutputStream(file.getPath(), !constraints.truncate),
            "UTF-8"));

        //
        // Build up query
        //
        StringBuffer query = new StringBuffer();
        query.append("SELECT ");
        if (constraints.distinct) {
          query.append("DISTINCT ");
        }
        if (constraints.fields_to_write == null && obj instanceof String) {

          //
          // Get Table Column Names
          //
          String col_query = "select * from " + (String) obj +
              " where rownum < 2";
          Statement stmt = data_source.createStatement();
          stmt.setFetchSize(10000);
          ResultSet rset = stmt.executeQuery(col_query);
          ResultSetMetaData metadata = rset.getMetaData();
          int columncount = metadata.getColumnCount();
          OrderedHashMap map = new OrderedHashMap();
          for (int i = 0; i < columncount; i++) {
            map.put( (metadata.getColumnName(i + 1)).toLowerCase(), null);
          }
          rset.close();
          stmt.close();
          constraints.fields_to_write = map;
        }

        //
        // Iterate through fields, get default values
        //
        Iterator iter = constraints.fields_to_write.orderedKeySet().iterator();
        while (iter.hasNext()) {
          String field = (String) iter.next();
          if (constraints.defaults != null &&
              constraints.defaults.containsKey(field)) {
            query.append("'");
            query.append( (String) constraints.defaults.get(field));
            query.append("'");
          } else {
            query.append(field);
          }
          query.append("||'");
          query.append(constraints.field_separator);
          query.append("'");
          if (iter.hasNext()) {
            query.append("||");
          }
        }

        //
        // Get table list
        //
        query.append(" FROM ");
        if (obj instanceof String) {
          query.append( (String) obj);
        } else if (obj.getClass().isArray()) {
          String[] table_names = (String[]) obj;
          for (int i = 0; i < table_names.length; i++) {
            query.append(table_names[i]);
            if (i < table_names.length - 1) {
              query.append(", ");
            }
          }
        }

        //
        // Build constraint clauses
        //
        if (constraints.conditions != null) {
          query.append(" WHERE ");
          for (int i = 0; i < constraints.conditions.length; i++) {
            query.append(constraints.conditions[i]);
            if (i < constraints.conditions.length - 1) {
              query.append(" AND ");
            }
          }
        }

        //
        // Build order
        //
        if (constraints.order != null) {
          query.append(" ORDER BY ");
          for (int i = 0; i < constraints.order.size(); i++) {
            query.append( (String) constraints.order.getKey(i));
            if (constraints.order.getValue(i) != null) {
              query.append(" ");
              query.append( (String) constraints.order.getValue(i));
            }
            if (i < (constraints.order.size() - 1)) {
              query.append(", ");
            }
          }
        }

        //
        // Execute the statement
        //
        MEMEToolkit.trace("Query: " + query.toString());
        Statement stmt = data_source.createStatement();
        ResultSet rset = stmt.executeQuery(query.toString());
        rset.setFetchSize(100000);
        while (rset.next()) {
          //
          // Get the line to write
          //
          String line = rset.getString(1);

          //
          // Apply filters
          //
          for (int i = 0; i < handlers.length; i++) {
            line = handlers[i].processLine(line);
          }

          //
          // Write line to file
          //
          out.println(line);
        }
        out.close();
        stmt.close();

      } catch (MEMEException me) {
        throw me;
      } catch (Exception e) {
        throw new MEMEException("Exception in DataWriter.write() " +
                                e.getMessage());
      }
    }
  }
}
