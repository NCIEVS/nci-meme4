/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  QAResult
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

import java.util.Comparator;

/**
 * Compares two QA results by name and by their reasons.  This ordering is
 * used to generate the QA report.
 *
 * Used by JSP applications for sorting QA reasons.
 *
 * @author TTN
 */
public class ByNameByReasonComparator implements Comparator {

  /**
   * Compare the two {@link QAResult}s.
   * @param o1 the first {@link QAResult}
   * @param o2 the second {@link QAResult}
   * @return an <code>int</code> representing the relative sort ordering
   */
  public int compare(Object o1, Object o2) {
    if (o1 == null || o2 == null) {
      return 0;
    }

    //
    // sort on name, value, count
    //
    QAResult q1 = (QAResult) o1;
    QAResult q2 = (QAResult) o2;
    if (q1.getName().compareTo(q2.getName()) != 0) {
      return q1.getName().compareTo(q2.getName());
    } else {
      return q1.getReasonsAsString().compareTo(q2.getReasonsAsString());
    }
  }

  /**
   * Indicates whether this object and the one specified are equal.
   * @param object the {@link Object} to compare to
   * @return <code>true</code> if an <code>==</code> comparison passes,
   * <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    return object == this;
  }

}
