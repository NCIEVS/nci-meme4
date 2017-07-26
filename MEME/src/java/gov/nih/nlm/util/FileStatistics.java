/************************************************************************
 * Package:     gov.nih.nlm.meme
 * Object:      FileStatistics.java
 ***********************************************************************/

package gov.nih.nlm.util;

import java.io.UnsupportedEncodingException;

/**
 * Container for info about file statistics.
 */
public class FileStatistics {

  //
  // File name
  //
  private String file_name;

  //
  // Description
  //
  private String description;

  //
  // Byte count
  //
  private long byte_count;

  //
  // Line count
  //
  private int line_count;

  //
  // Statistics for each column
  //
  private ColumnStatistics[] column_stats;

  //
  // Tokens for splitting line
  //
  private String[] tokens = null;

  //
  // Line separator length for this platform
  //
  private final int line_separator_length =
      System.getProperty("line.separator").length();

  //
  // Indicates whether or not for this file
  // UTF-8 non-ASCII chars are allowed
  //
  private boolean chars_equals_bytes = true;

  /**
   * Instantiates an empty {@link FileStatistics}
   */
  public FileStatistics() {
    clear();
  }

  /**
   * Returns the byte count.
   * @return the byte count
   */
  public long getByteCount() {
    return byte_count;
  }

  /**
   * Returns the {@link ColumnStatistics} for the file.
   * @return the {@link ColumnStatistics} for the file
   */
  public ColumnStatistics[] getAllColumnStatistics() {
    //
    // if no rows were read in, this prevents the minimum
    // length from staying at 100
    //
    for (int i = 0; i < column_stats.length; i++) {
      if (column_stats[i].getMinLength() == 100) {
        column_stats[i].setMinLength(0);
      }
    }
    return column_stats;
  }

  /**
   * Returns the statistics for a particular column of the file.
   * @param column the specified column name
   * @return the {@link ColumnStatistics} for the column name
   */
  public ColumnStatistics getColumnStatistics(String column) {
    for (int i = 0; i < column_stats.length; i++) {
      if (column_stats[i].getMinLength() == 100) {
        column_stats[i].setMinLength(0);
      }
      if (column_stats[i].getColumnName().equals(column)) {
        return column_stats[i];
      }
    }
    return null;
  }

  /**
   * Returns the file name.
   * @return the file name
   */
  public String getFileName() {
    return file_name;
  }

  /**
   * Returns the line count.
   * @return the line count
   */
  public int getLineCount() {
    return line_count;
  }

  /**
   * Sets the byte count.
   * @param newbyte_count the byte count
   */
  public void setByteCount(long newbyte_count) {
    byte_count = newbyte_count;
  }

  /**
   * Sets all of the {@link ColumnStatistics}.
   * @param newColumns the specified {@link ColumnStatistics}.
   */
  public void setAllColumnStatistics(ColumnStatistics[] newColumns) {
    column_stats = newColumns;
    tokens = new String[column_stats.length];
  }

  /**
   * Sets the file name.
   * @param newfile_name the file name
   */
  public void setFileName(String newfile_name) {
    file_name = newfile_name;
  }

  /**
   * Sets the line count.
   * @param newline_count the line ocunt
   */
  public void setLineCount(int newline_count) {
    line_count = newline_count;
  }

  /**
   * Increments the line count by 1.
   */
  public void incrementLineCount() {
    line_count++;
  }

  /**
   * Increments the byte count by a certain amount.
   * @param increment the amount to increment by
   */
  public void incrementByteCount(int increment) {
    byte_count += increment;
  }

  /**
   * Clear all statistics.
   */
  public void clear() {
    byte_count = 0;
    line_count = 0;
    if (column_stats != null) {
      for (int i = 0; i < column_stats.length; i++) {
        column_stats[i].clear();
      }
    }
  }

  /**
   * Returns a description of the file.
   * @return a description of the files
   */
  public String getDescription() {
    return (description == null ? "" : description);
  }

  /**
   * Sets a flag which indicates if the character encoding is
   * one byte per character or not.
   * @param chars_equals_bytes <code>boolean</code>
   */
  public void setCharsEqualsBytes(boolean chars_equals_bytes) {
    this.chars_equals_bytes = chars_equals_bytes;
  }

  /**
   * Indicates if the character encoding is
   * one byte per character or not.
   * @return chars_equals_bytes <code>boolean</code>
   */
  public boolean getCharsEqualsBytes() {
    return chars_equals_bytes;
  }

  /**
   * Sets the file description
   * @param newDescription the file description
   */
  public void setDescription(String newDescription) {
    description = newDescription;
  }

  /**
   * Compute statistics for the line of the file.
   * This includes computing the underlying column stats.
   * @param line a line of the file
   * @return the input line (unchanged)
   */
  public String processLine(String line) {
    // Update line count
    incrementLineCount();

    // UPdate byte count
    // If the character encoding is ASCII, meaning one byte per character,
    // increment by the length of the line.  Otherwise, get the byte count
    // based on more than one byte per character.
    if (chars_equals_bytes) {
      incrementByteCount(line.length());
    } else {
      try {
        incrementByteCount(line.getBytes("UTF-8").length);
      } catch (UnsupportedEncodingException e) {
        // do nothing
      }
    }
    incrementByteCount(line_separator_length);

    // Break line into fields and forward to column_stats
    FieldedStringTokenizer.split(line, "|", column_stats.length, tokens);

    for (int i = 0; i < column_stats.length; i++) {
      column_stats[i].processField(tokens[i]);

    }
    return line;
  }

}
