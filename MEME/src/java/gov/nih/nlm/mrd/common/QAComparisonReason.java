/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  QAComparisonReason
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.util.Iterator;
import java.util.Map;

/**
 * Represents a reason for a {@link QAComparison} discrepancy between
 * two QA reports.  This reason is used in a {@link QAReport} where
 * there are differences between the counts of a matching name/value in
 * two sets of {@link QAResult}s.
 *
 * @author TTN
 */
public class QAComparisonReason extends QAReason.Default {

  /**
   * The comparison count.
   */
  private long comparisonCount;

  /**
   * The difference between the count and the comparison count.
   */
  private long diffCount;

  /**
   * The operator used to compare the counts.
   */
  private String comparisonCountOperator;

  /**
   * The operator used to compare the diff counts.
   */
  private String diffCountOperator;

  /**
   * Returns the comparison count.
   * @return the comparison count
   */
  public long getComparisonCount() {
    return comparisonCount;
  }

  /**
   * Returns the diff count operator.
   * @return the diff count operator
   */
  public String getDiffCountOpertor() {
    return diffCountOperator;
  }

  /**
   * Returns the comparison count operator.
   * @return the comparison count operator
   */
  public String getComparisonCountOperator() {
    return comparisonCountOperator;
  }

  /**
   * Sets the comparison count.
   * @param comparisonCount the comparison count
   */
  public void setComparisonCount(long comparisonCount) {
    this.comparisonCount = comparisonCount;
  }

  /**
   * Sets the comparison count operator.
   * @param comparisonCountOperator the comparison count operator
   */
  public void setComparisonCountOperator(String comparisonCountOperator) {
    this.comparisonCountOperator = comparisonCountOperator;
  }

  /**
   * Sets the diff count operator.
   * @param diffCountOperator the diff count operator
   */
  public void setDiffCountOperator(String diffCountOperator) {
    this.diffCountOperator = diffCountOperator;
  }

  /**
   * Returns the operator used to compare diff counts.
   * @return the operator used to compare diff counts
   */
  public String getDiffCountOperator() {
    return diffCountOperator;
  }

  /**
   * Returns the diff count.
   * @return the diff count
   */
  public long getDiffCount() {
    return diffCount;
  }

  /**
   * Sets the diff count.
   * @param diffCount the diff count
   */
  public void setDiffCount(long diffCount) {
    this.diffCount = diffCount;
  }

  /**
   * Indicates whether or not this reason applies to the specified
   * {@link QAComparison}.
   * @param qacompare the compariosn
   * @param macro_map a map of source type macro to list of sources
   * @return <code>true</code> if it does; <code>false</code> otherwise
   */
  public boolean appliesTo(QAComparison qacompare, Map macro_map) {

    //
    // Compare names
    //
    if ("=".equals(getNameOperator()) && !getName().equals(qacompare.getName())) {
      return false;
    }

    if ("=~".equals(getNameOperator()) &&
        qacompare.getName().indexOf(getName()) == -1) {
      if (!qacompare.getName().matches(getName())) {
        return false;
      }
    }
    if ("in".equals(getNameOperator())) {
      String[] names = FieldedStringTokenizer.split(getName(), ",");
      boolean found = false;
      for (int i = 0; i < names.length; i++) {
        if (names[i].equals(qacompare.getName())) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }
    if ("in =~".equals(getNameOperator())) {
      String[] names = FieldedStringTokenizer.split(getName(), ",");
      boolean found = false;
      for (int i = 0; i < names.length; i++) {
        if (qacompare.getName().matches(names[i])) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }

    //
    // Compare values
    //
    if ("=".equals(getValueOperator()) && !getValue().equals(qacompare.getValue())) {
      return false;
    }
    if ("in".equals(getValueOperator())) {
      String[] values = FieldedStringTokenizer.split(getValue(), ",");
      boolean found = false;
      for (int i = 0; i < values.length; i++) {
        if (macro_map.containsKey(values[i])) {
          Source[] srcs = (Source[]) macro_map.get(values[i]);
          for (int j = 0; j < srcs.length; j++) {
            if (srcs[j].getStrippedSourceAbbreviation().equals(qacompare.
                getValue())) {
              found = true;
            }
          }
        } else if (values[i].equals(qacompare.getValue())) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }

    if ("=~".equals(getValueOperator()) &&
        qacompare.getValue().indexOf(getValue()) == -1) {
      String value = getValue();
      for (Iterator iter = macro_map.keySet().iterator(); iter.hasNext(); ) {
        String macro = (String) iter.next();
        if (value.indexOf(macro) != -1) {
          StringBuffer list = new StringBuffer("(");
          Source[] srcs = (Source[]) macro_map.get(macro);
          for (int j = 0; j < srcs.length - 1; j++) {
            list.append(srcs[j].getStrippedSourceAbbreviation()).append("|");
          }
          list.append(srcs[srcs.length - 1].getStrippedSourceAbbreviation()).
              append(")");
          value = value.replaceAll(macro, list.toString());
        }
      }
      if (!qacompare.getValue().matches(value)) {
        return false;
      }
    }

    if ("in =~".equals(getValueOperator())) {
      String[] values = FieldedStringTokenizer.split(getValue(), ",");
      boolean found = false;
      for (int i = 0; i < values.length; i++) {
        boolean inmacro = false;
        for (Iterator iter = macro_map.keySet().iterator(); iter.hasNext(); ) {
          String macro = (String) iter.next();
          if (values[i].indexOf(macro) != -1) {
            inmacro = true;
            Source[] srcs = (Source[]) macro_map.get(macro);
            for (int j = 0; j < srcs.length; j++) {
              if (qacompare.getValue().matches(values[i].replaceAll(macro,
                  srcs[j].getStrippedSourceAbbreviation()))) {
                found = true;
              }
            }
          }
        }
        if (!inmacro && qacompare.getValue().matches(values[i])) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }
    //
    // Compare counts
    //
    if ("=".equals(getCountOperator()) && getCount() != qacompare.getCount()) {
      return false;
    }
    if ("<".equals(getCountOperator()) && getCount() < qacompare.getCount()) {
      return false;
    }
    if (">".equals(getCountOperator()) && getCount() > qacompare.getCount()) {
      return false;
    }

    //
    // Compare comparison counts
    //
    if ("=".equals(getComparisonCountOperator()) &&
        getComparisonCount() != qacompare.getComparisonCount()) {
      return false;
    }
    if ("<".equals(getComparisonCountOperator()) &&
        getComparisonCount() < qacompare.getComparisonCount()) {
      return false;
    }
    if (">".equals(getComparisonCountOperator()) &&
        getComparisonCount() > qacompare.getComparisonCount()) {
      return false;
    }

    //
    // Compare diff counts
    //
    if ("=".equals(getDiffCountOperator()) &&
        getDiffCount() != qacompare.getDiffCount()) {
      return false;
    }
    if (">".equals(getDiffCountOperator()) &&
        getDiffCount() > qacompare.getDiffCount()) {
      return false;
    }
    if ("<".equals(getDiffCountOperator()) &&
        getDiffCount() < qacompare.getDiffCount()) {
      return false;
    }
    if ("abs >".equals(getDiffCountOperator()) &&
        Math.abs(getDiffCount()) >
        Math.abs(qacompare.getDiffCount())) {
      return false;
    }
    if ("abs <".equals(getDiffCountOperator()) &&
        Math.abs(getDiffCount()) <
        Math.abs(qacompare.getDiffCount())) {
      return false;
    }
    if("% <".equals(getDiffCountOperator()) && (qacompare.getCount() == 0 ||
    (((qacompare.getDiffCount()) * 100) / qacompare.getCount()) < 0 ||
     (qacompare.getDiffCount()) < 0 ||
    getDiffCount() < (((qacompare.getDiffCount()) * 100) / qacompare.getCount()))) {
      return false;
    }
    if("% >".equals(getDiffCountOperator()) && (qacompare.getCount() == 0 ||
    (((qacompare.getDiffCount()) * 100) / qacompare.getCount()) > 0 ||
    (qacompare.getDiffCount()) > 0 ||
    getDiffCount() > (((qacompare.getDiffCount()) * 100) / qacompare.getCount()) )) {
      return false;
    }
    return true;
  }

  /**
   * Returns a {@link String} representation of the QAComparisonReason.
   * @return a {@link String} representation of the QAComparisonReason.
   */
  public String toString() {
    return super.toString() + comparisonCount + comparisonCountOperator +
        diffCount + diffCountOperator;
  }
}
