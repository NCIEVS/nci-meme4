/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  QAReason
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

import gov.nih.nlm.util.FieldedStringTokenizer;

/**
 * Generically represents an explanation for a difference in two QA results.
 * @author TTN,BAC
 */
public interface QAReason {

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the name.
   * @return the name
   */
  public String getName();

  /**
   * Returns the operator used for comparing names.
   * @return the operator used for comparing names
   */
  public String getNameOperator();

  /**
   * Sets the operator used for comparing names.
   * @param name the operator used for comparing names
   */
  public void setNameOperator(String name);

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(String value);

  /**
   * Returns the value.
   * @return the value
   */
  public String getValue();

  /**
   * Returns the operator used for comparing values.
   * @return the operator used for comparing values
   */
  public String getValueOperator();

  /**
   * Sets the operator used for comparing values.
   * @param op the operator used for comparing values
   */
  public void setValueOperator(String op);

  /**
   * Sets the count.
   * @param count the count
   */
  public void setCount(long count);

  /**
   * Returns the count.
   * @return the count
   */
  public long getCount();

  /**
   * Returns the coperator used for comparing counts.
   * @return the coperator used for comparing counts
   */
  public String getCountOperator();

  /**
   * Sets the coperator used for comparing counts.
   * @param count the coperator used for comparing counts
   */
  public void setCountOperator(String count);

  /**
   * Returns the release name.
   * @return the release name
   */
  public String getReleaseName();

  /**
   * Sets the release name.
   * @param release the release name
   */
  public void setReleaseName(String release);

  /**
   * Returns the actual reason for the discrepancy.
   * @return the actual reason for the discrepancy
   */
  public String getReason();

  /**
   * Sets the actual reason for the discrepancy.
   * @param reason the actual reason for the discrepancy
   */
  public void setReason(String reason);

  /**
   * Returns the release name of the comparison release.
   * @return the release name of the comparison release
   */
  public String getComparisonName();

  /**
   * Sets the release name of the comparison release.
   * @param comparison_name the release name of the comparison release
   */
  public void setComparisonName(String comparison_name);

  /**
   * This inner class serves as a default implementation of the
   * {@link QAReason} interface.
   */
  public class Default implements QAReason, Comparable {
    // Fields
    private String name;
    private String nameOperator;
    private String value;
    private String valueOperator;
    private long count;
    private String countOperator;
    private String release_name;
    private String comparison_name;
    private String reason;

    /**
     * Sets the name.
     * @param name the name
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
      return name;
    }

    /**
     * Returns the operator used for comparing names.
     * @return the operator used for comparing names
     */
    public String getNameOperator(){
      return nameOperator;
    }

    /**
     * Sets the operator used for comparing names.
     * @param name the operator used for comparing names
     */
    public void setNameOperator(String name) {
      this.nameOperator = name;
    }

    /**
     * Sets the value.
     * @param value the value
     */
    public void setValue(String value) {
      this.value = value;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public String getValue() {
      return value;
    }

    /**
     * Returns the operator used for comparing values.
     * @return the operator used for comparing values
     */
    public String getValueOperator() {
      return valueOperator;
    }

    /**
     * Sets the operator used for comparing values.
     * @param op the operator used for comparing values
     */
    public void setValueOperator(String op) {
      this.valueOperator = op;
    }

    /**
     * Sets the count.
     * @param count the count
     */
    public void setCount(long count) {
      this.count = count;
    }

    /**
     * Returns the count.
     * @return the count
     */
    public long getCount() {
      return count;
    }

    /**
     * Returns the coperator used for comparing counts.
     * @return the coperator used for comparing counts
     */
    public String getCountOperator() {
      return countOperator;
    }

    /**
     * Sets the coperator used for comparing counts.
     * @param count the coperator used for comparing counts
     */
    public void setCountOperator(String count) {
      this.countOperator = count;
    }

    /**
     * Returns the release name.
     * @return the release name
     */
    public String getReleaseName() {
      return release_name;
    }

    /**
     * Sets the release name.
     * @param release the release name
     */
    public void setReleaseName(String release) {
      this.release_name = release;
    }

    /**
     * Returns the actual reason for the discrepancy.
     * @return the actual reason for the discrepancy
     */
    public String getReason() {
      return reason;
    }

    /**
     * Sets the actual reason for the discrepancy.
     * @param reason the actual reason for the discrepancy
     */
    public void setReason(String reason) {
      this.reason = reason;
    }

    /**
     * Returns the release name of the comparison release.
     * @return the release name of the comparison release
     */
    public String getComparisonName() {
      return comparison_name;
    }

    /**
     * Sets the release name of the comparison release.
     * @param comparison_name the release name of the comparison release
     */
    public void setComparisonName(String comparison_name) {
      this.comparison_name = comparison_name;
    }

    /**
     * Returns the values.
     * @return the values
     */
    public String[] getValues() {
      return FieldedStringTokenizer.split(getValue(), "~");
    }


    /**
     * Returns an <code>int</code> hashcode.
     * @return an <code>int</code> hashcode
     */
    public int hashCode() {
      return toString().hashCode();
    }

    /**
     * Returns a {@link String} representation.
     * @return a {@link String} representation
     */
    public String toString() {
      return reason + release_name + comparison_name + name + nameOperator +
          value + valueOperator
          + count + countOperator;
    }

    /**
     * Equality function based on {@link String} representations.
     * @param object the object to compare to
     * @return <code>true</code> if the objects are equal,
     *         <code>false</code> oherwise
     */
    public boolean equals(Object object) {
      if (! (object instanceof QAReason)) {
        return false;
      }
      return toString().equals(object.toString());
    }

    //
    // Comparable implementation
    //
    /**
     * Comparison function.
     * @param o the object to compare to
     * @return relative sort order
     */
    public int compareTo(Object o) {
      return toString().compareTo(o.toString());
    }
  }

}