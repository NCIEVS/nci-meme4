/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  QAComparison
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

/**
 * Represents the comparison of two {@link QAResult}s.
 *
 * @author TTN
 */
public class QAComparison extends QAResult {

  /**
   * The comparison count.
   */
  private long comparison_count;

  /**
   * Returns the comparison count.
   * @return the comparison count
   */
  public long getComparisonCount() {
    return comparison_count;
  }

  /**
   * Sets the comparison count.
   * @param comparison_count the comparison count
   */
  public void setComparisonCount(long comparison_count) {
    this.comparison_count = comparison_count;
  }
}