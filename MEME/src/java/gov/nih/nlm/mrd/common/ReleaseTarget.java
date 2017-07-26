/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  ReleaseTarget
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

import gov.nih.nlm.meme.exception.MEMEException;

import java.util.ArrayList;

/**
 * Represents a release target.  Each target is associated with a release
 * handler on the server side that performs the build/validate/publish
 * tasks.
 *
 * @author  MRD Group
 */
public class ReleaseTarget {

  //
  // Fields
  //
  private ReleaseInfo release_info;
  private String name, dependencies;
  private QAReport qaReport;
  private QAResult[] qaresults;
  private ArrayList list = new ArrayList();
  private boolean isActive, reviewQAReport;
  private StageStatus[] status;

  /**
   * Instantiates an empty {@link ReleaseTarget}.
   */
  public ReleaseTarget() { }

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
   * Sets the {@link StageStatus} values.
   * @param status the {@link StageStatus} values.
   */
  public void setStageStatus(StageStatus[] status) {
    this.status = status;
  }

  /**
   * Returns all {@link StageStatus} values.
   * @return all {@link StageStatus} values
   */
  public StageStatus[] getStageStatus() {
    return status;
  }

  /**
   * Sets the {@link QAReport}.
   * @param qaReport the {@link QAReport}
   */
  public void setQAReport(QAReport qaReport) {
    this.qaReport = qaReport;
    reviewQAReport = true;
    String[] errors = qaReport.getErrors();
    for (int i = 0; i < errors.length; i++) {
      list.add(errors[i]);
    }
    QAReport report = qaReport.getNeedsReviewReport();
    if (report.getGoldQAComparisons().length == 0 &&
        report.getGoldNotMeta().length == 0 &&
        report.getMetaNotGold().length == 0 &&
        report.getMinorPreviousQAComparisons().length == 0 &&
        report.getMinorPreviousNotMeta().length == 0 &&
        report.getMetaNotMinorPrevious().length == 0 &&
        report.getMajorPreviousQAComparisons().length == 0 &&
        report.getMajorPreviousNotMeta().length == 0 &&
        report.getMetaNotMajorPrevious().length == 0) {
      reviewQAReport = false;
    }
  }

  /**
   * Returns the {@link QAReport}.
   * @return the {@link QAReport}
   */
  public QAReport getQAReport() {
    return qaReport;
  }

  /**
   * Sets the {@link QAResult}s.
   * @param qaresults the {@link QAResult}s
   */
  public void setQAResults(QAResult[] qaresults) {
    this.qaresults = qaresults;
  }

  /**
   * Returns the {@link QAResult}s.
   * @return the {@link QAResult}s
   */
  public QAResult[] getQAResults() {
    return qaresults;
  }

  /**
   * Sets the {@link ReleaseInfo}.
   * @param release_info the {@link ReleaseInfo}
   */
  public void setReleaseInfo(ReleaseInfo release_info) {
    this.release_info = release_info;
  }

  /**
   * Returns the {@link ReleaseInfo}.
   * @return the {@link ReleaseInfo}
   */
  public ReleaseInfo getReleaseInfo() {
    return release_info;
  }

  /**
   * Indicates whether or not the target is active.
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws MEMEException if failed to get the status of the target
   */
  public boolean isActive() throws MEMEException {
    return isActive;
  }

  /**
   * Sets the flag indicating whether or not the target is active.
   * @param isActive <code>true</code> if so, <code>false</code> otherwise
   * @throws MEMEException if failed to set the status of the target
   */
  public void setIsActive(boolean isActive) throws MEMEException {
    this.isActive = isActive;
  }

  /**
   * Sets the target dependencies. This is a comma (,) separated list
   * of target names.
   * @param dependencies the target dependencies
   */
  public void setDependencies(String dependencies) {
    this.dependencies = dependencies;
  }

  /**
   * Returns the target dependencies.  This is a comma (,) separated list of
   * target names
   * @return the target dependencies
   */
  public String getDependencies() {
    return dependencies;
  }

  /**
   * Indicates whether or not the QA report is the "needs review" report.
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws MEMEException if failed to find out
   */
  public boolean needsReviewQAReport() throws MEMEException {
    return reviewQAReport;
  }

  /**
   * Indicates whether or not the QA report has errors.
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws MEMEException if failed to find out
   */
  public boolean hasErrors() {
    return
        list.size() > 0;
  }

  /**
   * Adds the error message.
   * @param err the error message
   */
  public void addError(String err) {
    list.add(err);
  }

  /**
   * Returns all error messages.
   * @return all error messages
   */
  public String[] getErrors() {
    return (String[])list.toArray(new String[0]);
  }

  /**
   * Returns a copy of the {@link ReleaseTarget} with the QA report populated.
   * This functionality used exclusively on the server side.
   * @return a copy of the {@link ReleaseTarget} with the QA report populated
   * @throws MEMEException if failed to get target
   */
  public ReleaseTarget newInstanceWithQAReport() throws MEMEException {
    ReleaseTarget ret_target = newInstanceWithStatus();
    ret_target.setQAReport(getQAReport().getNeedsReviewReport());
    return ret_target;
  }

  /**
   * Returns a copy of the {@link ReleaseTarget} with the stage status set.
   * This functionality used exclusively on the server side.
   * @return a copy of the {@link ReleaseTarget} with the stage status set
   * @throws MEMEException if failed to get target
   */
  public ReleaseTarget newInstanceWithStatus() throws
      MEMEException {
    ReleaseTarget ret_target = newInstance();
    ret_target.setStageStatus(getStageStatus());
    ret_target.reviewQAReport = reviewQAReport;
    return ret_target;

  }

  /**
   * Returns a copy of the {@link ReleaseTarget}.
   * This functionality used exclusively on the server side.
   * @return a copy of the {@link ReleaseTarget}
   * @throws MEMEException if failed to get target
   */
  public ReleaseTarget newInstance() throws
      MEMEException {
    ReleaseTarget ret_target = new ReleaseTarget();
    ret_target.setName(getName());
    ret_target.setReleaseInfo(getReleaseInfo());
    ret_target.setIsActive(isActive());
    ret_target.setDependencies(getDependencies());
    return ret_target;

  }
}