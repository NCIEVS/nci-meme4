/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  QAReport
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Represents the report resulting from the comparison of two sets
 * of {@link QAResult}s.
 *
 * @author TTN
 */
public class QAReport {

  //
  // Fields
  //
  private ArrayList checks;
  private HashMap errors, warnings;
  private QAComparison[] goldQAComparisons;
  private QAResult[] goldNotMeta;
  private QAResult[] metaNotGold;
  private QAComparison[] minorPreviousQAComparisons;
  private QAResult[] minorPreviousNotMeta;
  private QAResult[] metaNotMinorPrevious;
  private QAComparison[] majorPreviousQAComparisons;
  private QAResult[] majorPreviousNotMeta;
  private QAResult[] metaNotMajorPrevious;
  private QAReason[] unusedReasons;
  private String name, release, previous, previousMajor;

  /**
   * Instantiates a {@link QAReport}.
   */
  public QAReport() {
    checks = new ArrayList();
    errors = new HashMap();
    warnings = new HashMap();
  }

  /**
   * Returns the target name.
   * @return the target name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the target name.
   * @param name the target name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the release name.
   * @return the release name
   */
  public String getReleaseName() {
    return release;
  }

  /**
   * Set the release name.
   * @param release release name
   */
  public void setReleaseName(String release) {
    this.release = release;
  }

  /**
   * Returns the previous release name.
   * @return the previous release name
   */
  public String getPreviousReleaseName() {
    return previous;
  }

  /**
   * Sets the previous release name
   * @param previous the previous release name
   */
  public void setPreviousReleaseName(String previous) {
    this.previous = previous;
  }

  /**
   * Returns the previos major release name.
   * @return the previos major release name
   */
  public String getPreviousMajorReleaseName() {
    return previous;
  }

  /**
   * Sets the previos major release name.
   * @param previousMajor the name of the previous Release
   */
  public void setPreviousMajorReleaseName(String previousMajor) {
    this.previousMajor = previousMajor;
  }

  /**
   * Returns names of the semantic QA checks.
   * @return names of the semantic QA checks
   */
  public String[] getChecks() {
    return (String[]) checks.toArray(new String[] {});
  }

  /**
   * Adds a semantic QA check entry to the report.
   * @param name the semantic QA check name
   * @param error the error (or <code>null</code> if no error)
   * @param warning the warning (or <code>null</code> if no warning)
   */
  public void setCheck(String name, String error, String warning) {
    checks.add(name);
    if (error != null && !error.equals("")) {
      errors.put(name, error);
    }
    if (warning != null) {
      warnings.put(name, warning);
    }
  }

  /**
   * Returns the error message for a particular semantic QA check.
   * @param check the semantic QA check name
   * @return the error message for a particular semantic QA check
   */
  public String getErrorForCheck(String check) {
    if (errors.containsKey(check)) {
      return (String) errors.get(check);
    }
    return null;
  }

  /**
   * Returns the warning message for a particular semantic QA check.
   * @param check the semantic QA check name
   * @return the warning message for a particular semantic QA check
   */
  public String getWarningForCheck(String check) {
    if (warnings.containsKey(check)) {
      return (String) warnings.get(check);
    }
    return null;
  }

  /**
   * Returns all of the semantic QA check warnings.
   * @return all of the semantic QA check warnings
   */
  public String[] getWarnings() {
    String[] retWarnings = new String[warnings.size()];
    int i = 0;
    for (Iterator iterator = warnings.values().iterator(); iterator.hasNext(); ) {
      retWarnings[i++] = (String) iterator.next();
    }
    return retWarnings;
  }

  /**
   * Returns all of the semantic QA check errors.
   * @return all of the semantic QA check errors
   */
  public String[] getErrors() {
    String[] retErrors = new String[errors.size()];
    int i = 0;
    for (Iterator iterator = errors.values().iterator(); iterator.hasNext(); ) {
      retErrors[i++] = (String) iterator.next();
    }
    return retErrors;
  }

  /**
   * Indicates whether or not the QA report passes.
   * @return <Code>true</code> if so; <code>false</code> otherwise
   */
  public boolean isPassed() {
    return (false);
  }

  /**
   * Returns all of the {@link QAComparison}s from comparing the gold script
   * counts to the current release file counts.
   * @return all of the {@link QAComparison}s from comparing the gold script
   * counts to the current release file counts
   */
  public QAComparison[] getGoldQAComparisons() {
    if (goldQAComparisons != null) {
      return goldQAComparisons;
    }
    return new QAComparison[] {};
  }

  /**
   * Sets the {@link QAComparison}s from comparing the gold script
   * counts to the current release file counts.
   * @param goldQAComparisons the gold script {@link QAComparison}s
   */
  public void setGoldQAComparisons(QAComparison[] goldQAComparisons) {
    this.goldQAComparisons = goldQAComparisons;
  }

  /**
   * Returns the {@link QAResult}s found in the gold script counts but
   * not in the current release file counts.
   * @return the {@link QAResult}s missing from the release file counts
   */
  public QAResult[] getGoldNotMeta() {
    if (goldNotMeta != null) {
      return goldNotMeta;
    }
    return new QAResult[] {};
  }

  /**
   * Sets the {@link QAResult}s for the gold script counts missing
   * from the current release file counts.
   * @param goldNotMeta the {@link QAResult}s missing from the current
   * release file counts
   */
  public void setGoldNotMeta(QAResult[] goldNotMeta) {
    this.goldNotMeta = goldNotMeta;
  }

  /**
   * Returns the {@link QAResult}s found in the current release file counts
   * not in the gold script counts
   * @return the {@link QAResult}s missing from the gold script counts
   */
  public QAResult[] getMetaNotGold() {
    if (metaNotGold != null) {
      return metaNotGold;
    }
    return new QAResult[] {};
  }

  /**
   * Sets the {@link QAResult}s for the current release file counts missing
   * from the gold script counts.
   * @param metaNotGold the {@link QAResult}s missing from the
   * gold script counts
   */
  public void setMetaNotGold(QAResult[] metaNotGold) {
    this.metaNotGold = metaNotGold;
  }

  /**
   * Returns the {@link QAComparison}s from comparing the current release file
   * counts to the previos minor release file counts.
   * @return the previous minor file count {link QAComparison}s
   */
  public QAComparison[] getMinorPreviousQAComparisons() {
    if (minorPreviousQAComparisons != null) {
      return minorPreviousQAComparisons;
    }
    return new QAComparison[] {};
  }

  /**
   * Sets the {@link QAComparison}s from comparing the current release file
   * counts to the previous minor release file counts.
       * @param minorPreviousQAComparisons the current release {@link QAComparison}s
   */
  public void setMinorPreviousQAComparisons(QAComparison[]
                                            minorPreviousQAComparisons) {
    this.minorPreviousQAComparisons = minorPreviousQAComparisons;
  }

  /**
   * Returns the {@link QAResult}s found in the previous minor release file counts but
   * not in the current release file counts.
   * @return the {@link QAResult}s missing from the current release file counts
   */
  public QAResult[] getMinorPreviousNotMeta() {
    if (minorPreviousNotMeta != null) {
      return minorPreviousNotMeta;
    }
    return new QAResult[] {};
  }

  /**
   * Sets the {@link QAResult}s found in the previous minor release file counts but
   * not in the current release file counts.
   * @param minorPreviousNotMeta the {@link QAResult}s missing from the current release file counts
   */
  public void setMinorPreviousNotMeta(QAResult[] minorPreviousNotMeta) {
    this.minorPreviousNotMeta = minorPreviousNotMeta;
  }

  /**
   * Returns the {@link QAResult}s found in the current release file counts
   * not in the previous minor release file counts
   * @return the {@link QAResult}s missing from the previous minor release
   * file counts
   */
  public QAResult[] getMetaNotMinorPrevious() {
    if (metaNotMinorPrevious != null) {
      return metaNotMinorPrevious;
    }
    return new QAResult[] {};
  }

  /**
   * Sets the {@link QAResult}s found in the current release file counts
   * not in the previous minor release file counts
   * @param metaNotMinorPrevious the {@link QAResult}s missing from
   * the previous minor release file counts
   */
  public void setMetaNotMinorPrevious(QAResult[] metaNotMinorPrevious) {
    this.metaNotMinorPrevious = metaNotMinorPrevious;
  }

  /**
   * Returns the {@link QAComparison}s from comparing the current release file
   * counts to the previous major release file counts.
   * @return the previous major release file count {link QAComparison}s
   */
  public QAComparison[] getMajorPreviousQAComparisons() {
    if (majorPreviousQAComparisons != null) {
      return majorPreviousQAComparisons;
    }
    return new QAComparison[] {};
  }

  /**
   * Sets the {@link QAComparison}s from comparing the current release file
   * counts to the previous major release file counts.
       * @param majorPreviousQAComparisons the current release {@link QAComparison}s
   */
  public void setMajorPreviousQAComparisons(QAComparison[]
                                            majorPreviousQAComparisons) {
    this.majorPreviousQAComparisons = majorPreviousQAComparisons;
  }

  /**
   * Returns the {@link QAResult}s found in the previous major release file counts but
   * not in the current release file counts.
   * @return the {@link QAResult}s missing from the current release file counts
   */
  public QAResult[] getMajorPreviousNotMeta() {
    if (majorPreviousNotMeta != null) {
      return majorPreviousNotMeta;
    }
    return new QAResult[] {};
  }

  /**
   * Sets the {@link QAResult}s found in the previous major release file counts but
   * not in the current release file counts.
   * @param majorPreviousNotMeta the {@link QAResult}s missing from the current release file counts
   */
  public void setMajorPreviousNotMeta(QAResult[] majorPreviousNotMeta) {
    this.majorPreviousNotMeta = majorPreviousNotMeta;
  }

  /**
   * Returns the {@link QAResult}s found in the current release file counts
   * not in the previous major release file counts
   * @return the {@link QAResult}s missing from the previous major release
   * file counts
   */
  public QAResult[] getMetaNotMajorPrevious() {
    if (metaNotMajorPrevious != null) {
      return metaNotMajorPrevious;
    }
    return new QAResult[] {};
  }

  /**
   * Sets the {@link QAResult}s found in the current release file counts
   * not in the previous major release file counts
   * @param metaNotMajorPrevious the {@link QAResult}s missing from the previous major release
   * file counts
   */
  public void setMetaNotMajorPrevious(QAResult[] metaNotMajorPrevious) {
    this.metaNotMajorPrevious = metaNotMajorPrevious;
  }

  /**
   * Sets the unused reasons.
   * @param unusedReasons the unused reasons
   */
  public void setUnusedReasons(QAReason[] unusedReasons) {
    this.unusedReasons = unusedReasons;
  }

  /**
   * Returns the unused reasons.
   * @return the unused reasons
   */
  public QAReason[] getUnusedReasons() {
    return unusedReasons;
  }

  /**
   * Returns a copy of the {@link QAReport} containing only those results
   * and comparisons that have not yet been assigned reasons.
   * @return the portion of the {@link QAReport} that requires editing
   */
  public QAReport getNeedsReviewReport() {
    QAReport report = new QAReport();
    report.setName(name);
    report.setReleaseName(release);
    report.setPreviousMajorReleaseName(previousMajor);
    report.setPreviousReleaseName(previous);
    ArrayList qa = new ArrayList();
    String[] checks = getChecks();
    for (int i = 0; i < checks.length; i++) {
      report.setCheck(checks[i], getErrorForCheck(checks[i]),
                      getWarningForCheck(checks[i]));
    }
    QAComparison[] qacompare = getGoldQAComparisons();
    for (int i = 0; i < qacompare.length; i++) {
      if (qacompare[i].getReasons().length == 0) {
        qa.add(qacompare[i]);
      }
    }
    if (qa.size() > 0) {
      report.setGoldQAComparisons( (QAComparison[]) qa.toArray(new
          QAComparison[] {}));
    }
    qa.clear();
    QAResult[] qa_result = getGoldNotMeta();
    for (int i = 0; i < qa_result.length; i++) {
      if (qa_result[i].getReasons().length == 0) {
        qa.add(qa_result[i]);
      }
    }
    if (qa.size() > 0) {
      report.setGoldNotMeta( (QAResult[]) qa.toArray(new
          QAResult[] {}));
    }
    qa.clear();
    qa_result = getMetaNotGold();
    for (int i = 0; i < qa_result.length; i++) {
      if (qa_result[i].getReasons().length == 0) {
        qa.add(qa_result[i]);
      }
    }
    if (qa.size() > 0) {
      report.setMetaNotGold( (QAResult[]) qa.toArray(new
          QAResult[] {}));
    }
    qa.clear();
    qacompare = getMinorPreviousQAComparisons();
    for (int i = 0; i < qacompare.length; i++) {
      if (qacompare[i].getReasons().length == 0) {
        qa.add(qacompare[i]);
      }
    }
    if (qa.size() > 0) {
      report.setMinorPreviousQAComparisons( (QAComparison[]) qa.toArray(new
          QAComparison[] {}));
    }
    qa.clear();
    qa_result = getMinorPreviousNotMeta();
    for (int i = 0; i < qa_result.length; i++) {
      if (qa_result[i].getReasons().length == 0) {
        qa.add(qa_result[i]);
      }
    }
    if (qa.size() > 0) {
      report.setMinorPreviousNotMeta( (QAResult[]) qa.toArray(new
          QAResult[] {}));
    }
    qa.clear();
    qa_result = getMetaNotMinorPrevious();
    for (int i = 0; i < qa_result.length; i++) {
      if (qa_result[i].getReasons().length == 0) {
        qa.add(qa_result[i]);
      }
    }
    if (qa.size() > 0) {
      report.setMetaNotMinorPrevious( (QAResult[]) qa.toArray(new
          QAResult[] {}));
    }
    qa.clear();
    qacompare = getMajorPreviousQAComparisons();
    for (int i = 0; i < qacompare.length; i++) {
      if (qacompare[i].getReasons().length == 0) {
        qa.add(qacompare[i]);
      }
    }
    if (qa.size() > 0) {
      report.setMajorPreviousQAComparisons( (QAComparison[]) qa.toArray(new
          QAComparison[] {}));
    }
    qa.clear();
    qa_result = getMajorPreviousNotMeta();
    for (int i = 0; i < qa_result.length; i++) {
      if (qa_result[i].getReasons().length == 0) {
        qa.add(qa_result[i]);
      }
    }
    if (qa.size() > 0) {
      report.setMajorPreviousNotMeta( (QAResult[]) qa.toArray(new
          QAResult[] {}));
    }
    qa.clear();
    qa_result = getMetaNotMajorPrevious();
    for (int i = 0; i < qa_result.length; i++) {
      if (qa_result[i].getReasons().length == 0) {
        qa.add(qa_result[i]);
      }
    }
    if (qa.size() > 0) {
      report.setMetaNotMajorPrevious( (QAResult[]) qa.toArray(new
          QAResult[] {}));
    }
    report.setUnusedReasons(getUnusedReasons());
    return report;
  }
}
