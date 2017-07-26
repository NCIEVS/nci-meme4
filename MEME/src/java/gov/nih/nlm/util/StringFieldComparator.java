/*****************************************************************************
 * Package: gov.nih.nlm.util
 * Object:  StringFieldComparator
 *****************************************************************************/
package gov.nih.nlm.util;

import java.util.Comparator;

/**
 * Used to compare string field objects.
 *
 * @author  MEME Group
 */

public class StringFieldComparator implements Comparator {

  //
  // Fields
  //

  private String delim = null;
  private int field_num = 0;
  private boolean ascending = true;
  private boolean numeric = false;

  //
  // Constructors
  //

  /**
   * Instantiates a comparator for the specified parameters.
   * @param delim a {@link String} containing delimiter characters
   * @param field_num an <code>int</code> representation of field numbers
   */
  public StringFieldComparator(String delim, int field_num) {
    this.delim = delim;
    this.field_num = field_num;
  }

  /**
   * Instantiates a comparator for the specified parameters.
   * @param delim a {@link String} containing delimiter characters
   * @param field_num an <code>int</code> representation of field numbers
   * @param ascending <code>true</code> if compare in ascending order;
   * <code>false</code> otherwise
   */
  public StringFieldComparator(String delim, int field_num, boolean ascending) {
    this.delim = delim;
    this.field_num = field_num;
    this.ascending = ascending;
  }

  /**
   * Causes sort to be numeric
   * @param b <code>boolean</code>
   */
  public void setNumeric(boolean b) {
    numeric = b;
  }

  //
  // Implementation of Comparator interface
  //

  /**
   * Compares the specified field of each {@link String} object.
   * @param o1 the first {@link Object} to be compared.
   * @param o2 the second {@link Object} to be compared.
   * @return An <code>int</code> which might be negative integer, zero, or a positive integer
       *    as the first argument is less than, equal to, or greater than the second.
   */
  public int compare(Object o1, Object o2) {
    if (o1 == null || o2 == null ||
        ! (o1 instanceof String) || ! (o2 instanceof String)) {
      return 0;
    }
    String s1 = (String) o1;
    String s2 = (String) o2;
    String[] f1 = FieldedStringTokenizer.split(s1, delim);
    String[] f2 = FieldedStringTokenizer.split(s2, delim);

    if (!numeric) {
      if (ascending) {
        return f1[field_num].compareTo(f2[field_num]);
      } else {
        return -1 * f1[field_num].compareTo(f2[field_num]);
      }
    } else {
      if (ascending) {
        return Integer.parseInt(f1[field_num]) - Integer.parseInt(f2[field_num]);
      } else {
        return Integer.parseInt(f2[field_num]) - Integer.parseInt(f1[field_num]);
      }
    }
  }

}