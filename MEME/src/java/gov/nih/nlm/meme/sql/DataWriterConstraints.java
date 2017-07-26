/******************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  DataWriterConstraints.java
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.util.OrderedHashMap;

import java.util.ArrayList;

/**
 * Parameter container for the {@link DataWriter}.  Allows you to specify
 * things like the columns to read, whether or not to select default,
 * how to join multiple tables, and what additional handling should be
 * performed before writing to the file.
 */
public class DataWriterConstraints {

  //
  // PUBLIC FIELDS
  //

  /**
       * The fields in the file will be separated by the field_separator. The default
   * value is '|'. If it is different, one has to set this parameter.
   */
  public char field_separator = '|';

  /**
   * Indicates if the file gets overwritten or if the new lines get appended to
   * the end of the file. The default value is true.
   */
  public boolean truncate = true;

  /**
   * Indicates if all or only all distinct lines get written out. The default
   * is that only distinct lines get written out.
   */
  public boolean distinct = true;

  /**
       * Fields of the table which should be written out (if one chooses to not write
       * out every field ot to write them out in another order). The fields get written
   * out in the order of the ArrayList.
   */
  public OrderedHashMap fields_to_write = null;

  /**
   * If there are default values that should get written out the same in every
   * line (without looking it up in the data source), these values can
   */
  public OrderedHashMap defaults = null;

  /**
   * For example order.put("rank", "DESC") or
   * order.put("cui_1");
   * order.put("cui_2")
   */
  public OrderedHashMap order = null;

  /**
   * This is used to give conditions which have to be met by the data.
   * For example,
   * <pre>
   *   table_names = new String[] {"mrd_classes", "mrd_concepts"};
   *   conditions = new String[] {"mrd_classes.cui = mrd_concepts.cui",
   *                              "mrd_concepts.status = 'R'", "mrd_classes.expiration_date IS NULL"};
   * </pre>
   */
  public String[] conditions = null;

  /**
   * List of {@link DataWriterHandler}
   */
  public ArrayList handler_list = new ArrayList();

  /**
   * Add the {@link DataWriterHandler} to the list
   * @param dwh  a {@link DataWriterHandler}
   */
  public void addHandler(DataWriterHandler dwh) {
    handler_list.add(dwh);
  }

  /**
   * Clear all {@link DataWriterHandler}s in the list
   */
  public void clearHandlers() {
    handler_list.clear();
  }

  /**
   * Remove the {@link DataWriterHandler} from the list
   * @param dwh the {@link DataWriterHandler} to remove
   */
  public void removeHandler(DataWriterHandler dwh) {
    handler_list.remove(dwh);
  }

}