/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server
 * Object:  ReleaseGenerator
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.server;

import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.server.MEMEApplicationService;
import gov.nih.nlm.mrd.common.QAReason;
import gov.nih.nlm.mrd.common.QAReport;
import gov.nih.nlm.mrd.common.QAResult;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.common.ReleaseTarget;
import gov.nih.nlm.mrd.common.StageStatus;
import gov.nih.nlm.mrd.sql.MRDDataSource;

import java.io.File;

/**
 * Generically represents a service for producing release files.
 *
 * @author  MEME Group
 */
public abstract class ReleaseGenerator implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Indicates whether or not the release is ready to be published.
   * @param release the {@link ReleaseInfo} to publish
   * @return <code>true</code> if the release is built and ready for publishing,
   * <code>false</code> otherwise
   * @throws MEMEException if failed to process the request
   */
  public boolean isReadyForPublish(ReleaseInfo release) {
    return release.isBuilt();
  }

  /**
   * Indicates whether or not the release is ready to be built.
   * @param release the {@link ReleaseInfo} to build
   * @return <code>true</code> if the release is redy to be built,
   * <code>false</code> otherwise
   * @throws MEMEException if failed to process the request
   */
  public boolean isReadyForBuild(ReleaseInfo release) throws MEMEException {
    return new File(new File(release.getBuildUri()),"QA").exists();
  }

  /**
   * Indicates whether or not the release is built and published.
   * @param release the {@link ReleaseInfo}
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws MEMEException if failed to process the request
   */
  public boolean isFinished(ReleaseInfo release) {
    return release.isBuilt() && release.isPublished();
  }

  /**
   * Returns all {@link QAResult}s for the specified release target.
   * @param release_name the release name
   * @param target_name the target name
   * @param data_source the {@link MRDDataSource}
   * @return all {@link QAResult}s for the specified release target
   * @throws DataSourceException if failed to get QAResults
   */
  public QAResult[] getQAResults(String release_name, String target_name,
                                 MRDDataSource data_source) throws
      DataSourceException {
    return data_source.getQAResults(release_name, target_name);
  }

  /**
   * Returns all {@link ReleaseInfo}.
   * @param data_source the {@link MRDDataSource}
   * @return all {@link ReleaseInfo}
   * @throws MEMEException if failed to get release history
   */
  public ReleaseInfo[] getReleaseHistory(MRDDataSource data_source) throws
      MEMEException {
    return data_source.getReleaseHistory();
  }

  /**
   * Returns the {@link ReleaseInfo} for the specified release.
   * @param release_name the release name
   * @param data_source the {@link MRDDataSource}
   * @return the {@link ReleaseInfo} for the specified release
   * @throws MEMEException if failed to get release
   */
  public ReleaseInfo getReleaseInfo(String release_name,
                                    MRDDataSource data_source) throws
      MEMEException {
    return data_source.getReleaseInfo(release_name);
  }

  /**
   * Apply changes to the specified {@link ReleaseInfo}.
   * @param release_info the {@link ReleaseInfo} to change
   * @param data_source the {@link MRDDataSource}
   * @throws MEMEException if failed to set release info
   */
  public void setReleaseInfo(ReleaseInfo release_info,
                             MRDDataSource data_source) throws MEMEException {
    data_source.setReleaseInfo(release_info);
  }

  /**
   * Removes the specified {@link ReleaseInfo}.
   * @param release_info the {@link ReleaseInfo} to add
   * @param data_source the {@link MRDDataSource}
   * @throws MEMEException if failed to remove release info
   */
  public void removeReleaseInfo(ReleaseInfo release_info,
                                MRDDataSource data_source) throws MEMEException {
    data_source.removeReleaseInfo(release_info);
  }

  /**
   * Returns the "gold" {@link QAResult}s for the specified release target.
   * @param release_name the release name
   * @param target_name the target name
   * @param data_source the {@link MRDDataSource}
   * @return the "gold" {@link QAResult}s for the specified release target
   * @throws DataSourceException if failed to get QAResults
   */
  public QAResult[] getGoldStandardQAResults(String release_name,
                                             String target_name,
                                             MRDDataSource data_source) throws
      DataSourceException {
    return data_source.getGoldStandardQAResults(release_name, target_name);
  }

  /**
   * Adds the specified {@link QAReason}.
   * @param qareason the {@link QAReason} to add
   * @param target_name the target name
   * @param data_source the {@link MRDDataSource}
   * @throws DataSourceException if failed to add reason to {@link QAReason}
   */
  public void addQAReason(QAReason qareason, String target_name,
                          MRDDataSource data_source) throws DataSourceException {
    data_source.addQAReason(qareason, target_name);
  }

  /**
   * Removes the specified {@link QAReason}.
   * @param qareason the {@link QAReason} to remove
   * @param target_name the target name
   * @param data_source the {@link MRDDataSource}
   * @throws DataSourceException if failed to add reason to {@link QAReason}
   */
  public void removeQAReason(QAReason qareason, String target_name,
                             MRDDataSource data_source) throws
      DataSourceException {
    data_source.removeQAReason(qareason, target_name);
  }

  /**
   * Clears the stage status for the specified release target.
   * @param release_info the {@link ReleaseInfo}
   * @param target_name the target name
   * @param stage the sstage name
   * @throws MEMEException if failed to process the request
   */
  public abstract void clearStatus(ReleaseInfo release_info, String target_name,
                                   String stage) throws MEMEException;

  /**
   * Clears the MEDLINE files for the specified stage.
   * @param stage the stage name
   * @throws MEMEException if failed to process the request
   */
  public abstract void clearMedlineStatus(String stage) throws MEMEException;

  /**
   * Perform the specified process for all active handlers.
   * @param process the process, e.g. "build", "validate", etc.
   * @param release_name the release name
   * @param service the mid service
   * @throws MEMEException if failed to process
   */
  public abstract void doProcess(String process, String release_name,
                                 String service) throws MEMEException;

  /**
   * Returns the {@link ReleaseTarget} for the specified release and target name.
   * @param release_name the release name
   * @param target_name the target name
   * @param data_source the {@link MRDDataSource}
   * @return the {@link ReleaseTarget} for the specified release and target name
   * @throws MEMEException if failed to get target
   */
  public abstract ReleaseTarget getTarget(String release_name,
                                          String target_name,
                                          MRDDataSource data_source) throws
      MEMEException;

  /**
   * Returns the {@link ReleaseTarget} with its stage status information.
   * @param release_name the release name
   * @param target_name the target name
   * @param data_source the {@link MRDDataSource}
   * @return the {@link ReleaseTarget} for the specified release and target name
   * @throws MEMEException if failed to get target
   */
  public abstract ReleaseTarget getTargetStatus(String release_name,
                                                String target_name,
                                                MRDDataSource data_source) throws
      MEMEException;

  /**
   * Returns the {@link ReleaseTarget} with its {@link QAReport}.
   * @param release_name the release name
   * @param target_name the target name
   * @param data_source the {@link MRDDataSource}
   * @return the {@link ReleaseTarget} for the specified release and target name
   * @throws MEMEException if failed to get target
   */
  public abstract ReleaseTarget getTargetQAReport(String release_name,
                                                  String target_name,
                                                  MRDDataSource data_source) throws
      MEMEException;

  /**
   * Generates a CUI comparison report.
   * @param cui the cui
   * @param current the release name
   * @param compareTo the comparison release name
   * @throws MEMEException if failed to remove reason
   */
  public abstract void generateCuiComparisonReport(String cui,
      ReleaseInfo current,
      ReleaseInfo compareTo) throws MEMEException;

  /**
   * Returns the CUI comparison {@link QAReport} for the specified CUI.
   * @param cui the cui
   * @param release the {@link ReleaseInfo}
   * @return the CUI comparison {@link QAReport} for the specified CUI
   * @throws MEMEException if failed to remove reason
   */
  public abstract QAReport getCuiComparisonReport(String cui,
                                                  ReleaseInfo release) throws
      MEMEException;

  /**
   * Prepare the "Build Host/URI" structure for the release run and update
   * the release history.
   * @param release the release name
   * @param data_source the {@link MRDDataSource}
   * @throws MEMEException if failed to prepare new release
   */
  public abstract void prepareRelease(ReleaseInfo release,
                                      MRDDataSource data_source) throws
      MEMEException;

  /**
   * Returns the first n lines of the specified release target.
   * @param release the release name
   * @param target_name the target name
   * @param lines the line count
   * @param data_source the {@link MRDDataSource}
   * @return the first n lines of the specified release target
   * @throws MEMEException if failed to preview target
   */
  public abstract String previewTarget(ReleaseInfo release,
                                       MRDDataSource data_source,
                                       String target_name, int lines) throws
      MEMEException;

  /**
   * Publishes files from the "Build Host" to the "Release Host" and verifies
   * the upload using MD5.  Publishes all files for "active" targets
   * @param release_name the release name
   * @param service the mid service name
   * @throws MEMEException if failed to process the request
   */
  public abstract void publish(String release_name, String service) throws
      MEMEException;

  /**
   * Returns the complete list of target names.
   * @param release_name the release name
   * @param data_source the {@link MRDDataSource}
   * @return the complete list of target names
   * @throws MEMEException if failed to get target names
   */
  public abstract String[] getTargetNames(String release_name,
                                          MRDDataSource data_source) throws
      MEMEException;

  /**
   * Returns {@link ReleaseTarget}s for the specified release.
   * @param release_name the release name
   * @param data_source the {@link MRDDataSource}
   * @return {@link ReleaseTarget}s for the specified release
   * @throws MEMEException if failed to get target
   */
  public abstract ReleaseTarget[] getTargets(String release_name,
                                             MRDDataSource data_source) throws
      MEMEException;

  /**
   * Activates the specified target.
   * @param target_name the target name
   * @param data_source the {@link MRDDataSource}
   * @throws MEMEException if failed to activate target handler
   */
  public abstract void activateTargetHandler(String target_name,
                                             MRDDataSource data_source) throws
      MEMEException;

  /**
   * Activates the specified target.
   * @param target_names the target names
   * @param data_source the {@link MRDDataSource}
   * @throws MEMEException if failed to activate target handler
   */
  public abstract void activateTargetHandlers(String[] target_names,
                                              MRDDataSource data_source) throws
      MEMEException;

  /**
   * Deactivates the specified target.
   * @param target_name the target name
   * @param data_source the {@link MRDDataSource}
   * @throws MEMEException if failed to deactivate target handler
   */
  public abstract void deactivateTargetHandler(String target_name,
                                               MRDDataSource data_source) throws
      MEMEException;

  /**
   * Deactivate the specified targets.
   * @param target_names the target names
   * @param data_source the {@link MRDDataSource}
   * @throws MEMEException if failed to deactivate target handlers
   */
  public abstract void deactivateTargetHandlers(String[] target_names,
                                                MRDDataSource data_source) throws
      MEMEException;

  /**
   * Downloads the MEDLINE Baseline files from NLM machine to $MRD_HOME/Medline.
   * @throws MEMEException if failed to download the files
   */
  public abstract void downloadMedlineBaseline() throws MEMEException;

  /**
   * Parses the Medline Baseline XML files in $MRD_HOME/Medline.
   * @param service the mid service
   * @param release the release name
   * @throws MEMEException if failed to parse the files
   */
  public abstract void parseMedlineBaseline(String service, ReleaseInfo release) throws
      MEMEException;

  /**
   * Process the Medline Baseline data files in $MRD_HOME/Medline.
   * @param service the mid service
   * @throws MEMEException if failed to process the data
   */
  public abstract void processMedlineBaseline(String service) throws
      MEMEException;

  /**
   * Downloads, parses and process the Medline Update XML files in
   * $MRD_HOME/Medline/update.
   * @param service the service name
   * @param release the release name
   * @throws MEMEException if failed to process the data
   */
  public abstract void updateMedline(String service, ReleaseInfo release) throws MEMEException;

  /**
   * Returns the {@link StageStatus} for MEDLINE processing stages.
   * @return the {@link StageStatus} for MEDLINE processing stages
   * @throws MEMEException if failed to get status
   */
  public abstract StageStatus[] getMedlineStatus() throws MEMEException;

  /**
   * Returns the specified MEDLINE processing {@link StageStatus}.
   * @param stage_name the stage name
   * @return the specified MEDLINE processing {@link StageStatus}
   * @throws MEMEException if failed to get status
   */
  public abstract StageStatus getMedlineStageStatus(String stage_name) throws
      MEMEException;

  /**
   * Removes the specified XML file from the $MRD_HOME/Medline/update directory.
   * @param file_name the file name
   * @throws MEMEException if failed to get status
   */
  public abstract void deleteUpdateMedlineXML(String file_name) throws
      MEMEException;

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean requiresSession() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isRunning() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isReEntrant() {
    return false;
  }

}
