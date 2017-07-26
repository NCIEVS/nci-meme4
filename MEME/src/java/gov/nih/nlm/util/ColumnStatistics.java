/************************************************************************
 * Package:     gov.nih.nlm.util
 * Object:      ColumnStatistics.java
 ***********************************************************************/
package gov.nih.nlm.util;

/**
 * Container for statistics about file columns.
 */
public class ColumnStatistics {

  //
  // Fields
  //
  private String column_name;
  private String file_name;
  private String description;

  //
  // Statistics
  //
  private int max_length;
  private int min_length;
  private long total_length;
  private int sample_size;

  private String data_type;
  private String data_type_length;

  /**
   * Instantiates an empty {@link ColumnStatistics}.
   */
  public ColumnStatistics() {
    clear();
  }

  /**
   * Clears the statistics for the column.
   */
  public void clear() {
    min_length = 100;
    max_length = 0;
    total_length = 0;
    sample_size = 0;
  }

  /**
   * Returns the column name.
   * @return the column name
   */
  public String getColumnName() {
    return column_name;
  }

  /**
   * Returns the file name.
   * @return the file name
   */
  public String getFileName() {
    return file_name;
  }

  /**
   * Returns the maximum length of the column.
   * @return the maximum length of the column
   */
  public int getMaxLength() {
    return max_length;
  }

  /**
   * Returns the minimum length of the column.
   * @return the minimum length of the column
   */
  public int getMinLength() {
    return min_length;
  }

  /**
   * Sets the column name.
   * @param newcolumn_name the column name
   */
  public void setColumnName(String newcolumn_name) {
    column_name = newcolumn_name;
  }

  /**
   * Sets the file name.
   * @param newfile_name the file name
   */
  public void setFileName(String newfile_name) {
    file_name = newfile_name;
  }

  /**
   * Sets the maximum length of column
   * @param newMax_length the maximum length
   */
  public void setMaxLength(int newMax_length) {
    max_length = newMax_length;
  }

  /**
   * Set the minimum length of column
   * @param newMin_length the minimum length
   */
  public void setMinLength(int newMin_length) {
    min_length = newMin_length;
  }

  /**
   * Sets the average length of column
   * @param new_average_length the average length
   */
  public void setAverageLength(double new_average_length) {
    if (sample_size == 0) {
      sample_size++;
    }
    total_length = (long) (new_average_length * sample_size);
  }

  /**
   * Returns the column description.
   * @return the column description
   */
  public String getDescription() {
    return (description == null ? "" : description);
  }

  /**
   * Sets the the description.
   * @param new_description the column description
   */
  public void setDescription(String new_description) {
    description = new_description;
  }

  /**
   * Returns the average length.
   * @return the average length
   */
  public double getAverageLength() {
    if (sample_size != 0) {
      return (total_length / (double) sample_size);
    } else {
      return 0;
    }
  }

  /**
   * Returns the data type of the column.
   * @return the data type of the column
   */
  public String getDataType() {
    return data_type;
  }

  /**
   * Sets the column data type.
   * @param new_data_type the column data type
   */
  public void setDataType(String new_data_type) {
    data_type = new_data_type;
  }

  /**
   * Returns the data type length of the column.
   * @return the data type length of the column
   */
  public int getDataTypeLength() {
    return new Integer(data_type_length).intValue();
  }

  /**
   * Sets the column data type length.
   * @param new_data_type_length the column data type length
   */
  public void setDataTypeLength(String new_data_type_length) {
    data_type_length = new_data_type_length;
  }

  /**
   * Computes statistics for the field.
   * @param field the field value to compute stats for
   * @return the field passed in (unchanged)
   */
  public String processField(String field) {
    // compute length of field
    int len = field.length();

    // Determine if we have a new min or max
    max_length = (max_length >= len) ? max_length : len;
    min_length = (min_length <= len) ? min_length : len;

    // Update the total length seen and increment the
    // sample size, these are used to compute average length
    total_length += len;
    sample_size++;

    return field;
  }

}
