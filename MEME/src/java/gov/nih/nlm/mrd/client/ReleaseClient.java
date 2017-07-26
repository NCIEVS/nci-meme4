/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.client
 * Object:  ReleaseClient
 *   03/08/2007 TTN (1-DKB57): Add Finish Release method
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.client;

import gov.nih.nlm.meme.client.ClientAPI;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.mrd.common.QAReason;
import gov.nih.nlm.mrd.common.QAReport;
import gov.nih.nlm.mrd.common.QAResult;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.common.ReleaseTarget;
import gov.nih.nlm.meme.common.StageStatus;

/**
 * Generically represents the API used to build a Metathesaurus release.
 *
 * @author MEME Group
 */
public abstract class ReleaseClient extends ClientAPI {

  //
  // Release name
  //
  protected String name;

  //
  // MID Service
  //
  protected String mid_service = null;

  /**
   * Instantiates a {@link ReleaseClient} connected to "mrd-db".
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public ReleaseClient() throws MEMEException {
    this("mrd-db");
  }

  /**
   * Instantiates a {@link ReleaseClient} connected to the specified mid service.
   * @param service the mid service
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated
   */
  public ReleaseClient(String service) throws MEMEException {
    super();
    this.mid_service = service;
  }

  /**
   * Sets the release generator name.
   * @param name the release generator name
   */
  protected void setReleaseGenerator(String name) {
    this.name = name;
  }

  /**
   * Sets the mid service.
   * @param mid_service the mid service
   */
  public void setMidService(String mid_service) {
    if (!mid_service.equals(this.mid_service)) {
      this.mid_service = mid_service;
    }
  }

  /**
   * Returns the mid service.
   * @return the mid service
   */
  public String getMidService() {
    return mid_service;
  }

  /**
   * Returns release generator name.
   * @return release generator name
   */
  protected String getReleaseGenerator() {
    return name;
  }

  /**
   * Request that the server download the MEDLINE baseline files to the
   * $MRD_HOME/Medline server directory.
   * @throws MEMEException if failed to download the files
   */
  public void downloadMedlineBaseline() throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "downloadMedlineBaseline"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Requests that the server parses the MEDLINE Baseline XML files
   * in the $MRD_HOME/Medline server directory.
   * @param release_name the release name
   * @throws MEMEException if failed to parse the files
   */
  public void parseMedlineBaseline(String release_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "parseMedlineBaseline"));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Requests that the server process the MEDLINE baseline data in the
   * $MRD_HOME/Medline server directory.
   * @throws MEMEException if failed to process the data
   */
  public void processMedlineBaseline() throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "processMedlineBaseline"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Requests that the server downloads, parse, and process
   * the MEDLINE update XML files (in the $MRD_HOME/Medline/update server
   * directory).
   * @param release_name the release name
   * @throws MEMEException if failed to process the data
   */
  public void updateMedline(String release_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "updateMedline"));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Returns the status of all Medline processing stages.
   * @return the status of all Medline processing stages
   * @throws MEMEException if failed to get status
   */
  public StageStatus[] getMedlineStatus() throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getMedlineStatus"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (StageStatus[]) request.getReturnValue("status").getValue();
  }

  /**
   * Requests that the server delete the specified MEDLINE Update XML file
   * from the $MRD_HOME/Medline/update server directory.  This is used when
   * a parsing error is encountered in an update file.
   * @param file_name the file name to delete
   * @throws MEMEException if failed to get status
   */
  public void deleteUpdateMedlineXML(String file_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "deleteUpdateMedlineXML"));
    request.addParameter(new Parameter.Default("file_name", file_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Returns the status of specified Medline processing stage.
   * @param stage_name the stage name
   * @return the status of specified Medline processing stage
   * @throws MEMEException if failed to get status
   */
  public StageStatus getMedlineStageStatus(String stage_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "getMedlineStageStatus"));
    request.addParameter(new Parameter.Default("stage_name", stage_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (StageStatus) request.getReturnValue("status").getValue();
  }

  /**
   * Returns all {@link ReleaseInfo}.
   * @return all {@link ReleaseInfo}
   * @throws MEMEException if failed to get release history
   */
  public ReleaseInfo[] getReleaseHistory() throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getReleaseHistory"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (ReleaseInfo[]) request.getReturnValue("release_history").getValue();
  }

  /**
   * Returns {@link ReleaseInfo} for the specified release name.
   * @param release_name the release name
   * @return {@link ReleaseInfo} for the specified release name
   * @throws MEMEException if failed to get release
   */
  public ReleaseInfo getReleaseInfo(String release_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getReleaseInfo"));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process and return response
    return (ReleaseInfo) request.getReturnValue("release_info").getValue();
  }

  /**
   * Requests that the server prepare the specified release.  In the
   * background, this builds the directory structure (e.g., 2002AC 2002AC/META 2002AC/QA 2002AC/log)
   * and adds an entry to the release_history table.
   * @param release the {@link ReleaseInfo}
   * @throws MEMEException if failed to prepare new release
   */
  public void prepareRelease(ReleaseInfo release) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "prepareRelease"));
    request.addParameter(new Parameter.Default("releaseinfo", release));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Requests that the server finish the specified release.  In the
   * background, this creates the log file in log/Finished.log
   * @param release the {@link ReleaseInfo}
   * @throws MEMEException if failed to finish release
   */
  public void finishRelease(ReleaseInfo release) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "finishRelease"));
    request.addParameter(new Parameter.Default("releaseinfo", release));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Indicates whether or not the specified release is ready to be built.
   * @param release_name the release name
   * @return true if all prerequisites for build are finished
   * @throws MEMEException if failed to process the request
   */
  public boolean isReadyForBuild(String release_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "isReadyForBuild"));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return ( (Boolean) request.getReturnValue("isReadyForBuild").getValue()).
        booleanValue();
  }

  /**
   * Indicates whether or not the specified release is already finished.
   * @param release_name the release name
   * @return true if the release is finished
   * @throws MEMEException if failed to process the request
   */
  public StageStatus getReleaseStatus(String release_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getReleaseStatus"));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (StageStatus) request.getReturnValue("releaseStatus").getValue();
  }

  /**
   * Indicates whether or not the specified release is ready to be published.
   * @param release_name the release name
   * @return true if all prerequisites for publish are finished
   * @throws MEMEException if failed to process the request
   */
  public boolean isReadyForPublish(String release_name) throws MEMEException {
    return isBuilt(release_name);
  }

  /**
   * Indicates whether or not the specified release is finished.
   * @param release_name the release name
   * @return true if all release processes are finished
   * @throws MEMEException if failed to process the request
   */
  public boolean isFinished(String release_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "isFinished"));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return ( (Boolean) request.getReturnValue("isFinished").getValue()).
        booleanValue();
  }

  /**
   * Indicates whether or not the specified release is built.
   * @param release_name the release name
   * @return true if all release targets have been built
   * @throws MEMEException if failed to process the request
   */
  public boolean isBuilt(String release_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "isBuilt"));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return ( (Boolean) request.getReturnValue("isBuilt").getValue()).
        booleanValue();
  }

  /**
   * Request the server to perform the specified process.  Used to build,
   * validate, and publish files.  The process applies to all currently
   * activated handlers.
   * @param process the process (e.g. "build")
   * @param release_name the release name
   * @throws MEMEException if failed to process the request
   */
  public void doProcess(String process, String release_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "doProcess"));
    request.addParameter(new Parameter.Default("process", process));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Requests that the server clear the log file for the specified stage.
   * @param release_name the release name
   * @param target_name the target name
   * @param stage the stage name
   * @throws MEMEException if failed to process the request
   */
  public void clearStatus(String release_name, String target_name, String stage) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "clearStatus"));
    request.addParameter(new Parameter.Default("release_name", release_name));
    request.addParameter(new Parameter.Default("target_name", target_name));
    request.addParameter(new Parameter.Default("stage", stage));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Requests that the server remove the log file for the
   * specified MEDLINE processing stage.
   * @param stage the stage name
   * @throws MEMEException if failed to process the request
   */
  public void clearMedlineStatus(String stage) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "clearMedlineStatus"));
    request.addParameter(new Parameter.Default("stage", stage));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Returns the first "n" lines of the specified target.  This simulates
   * something like a "head -100 MRCONSO.RRF".
   * @param release_name the release name
   * @param target_name the target name
   * @param lines the number of lines
   * @return the first "n" lines of the specified target
   * @throws MEMEException if failed to preview target
   */
  public String previewTarget(String release_name, String target_name,
                              int lines) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "previewTarget"));
    request.addParameter(new Parameter.Default("release_name", release_name));
    request.addParameter(new Parameter.Default("target_name", target_name));
    request.addParameter(new Parameter.Default("lines", new Integer(lines)));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (String) request.getReturnValue("previewTarget").getValue();
  }

  /**
   * Returns the {@link QAResult}s for the specified release and target.
   * @param release_name the release name
   * @param target_name the target name
   * @return the {@link QAResult}s for the specified release and target
   * @throws MEMEException if failed to get results
   */
  public QAResult[] getQAResults(String release_name, String target_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getQAResults"));
    request.addParameter(new Parameter.Default("release_name", release_name));
    request.addParameter(new Parameter.Default("target_name", target_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (QAResult[]) request.getReturnValue("QAResults").getValue();
  }

  /**
   * Returns the gold standard {@link QAResult}s for the specified release name
   * and target.
   * @param release_name the release name
   * @param target_name the target name
   * @return the gold standard {@link QAResult}s for the specified release name
   * and target
   * @throws MEMEException if failed to get QA results
   */
  public QAResult[] getGoldStandardQAResults(String release_name,
                                             String target_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "getGoldStandardQAResults"));
    request.addParameter(new Parameter.Default("release_name", release_name));
    request.addParameter(new Parameter.Default("target_name", target_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (QAResult[]) request.getReturnValue("goldStandardQAResults").
        getValue();
  }

  /**
   * Adds the specified {@link QAReason}.
   * @param qareason the {@link QAReason}
   * @param target_name the target name
   * @throws MEMEException if failed to add reason to QA comparison
   */
  public void addQAReason(QAReason qareason, String target_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "addQAReason"));
    request.addParameter(new Parameter.Default("qareason", qareason));
    request.addParameter(new Parameter.Default("target_name", target_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Removes the specified {@link QAReason}.
   * @param qareason the {@link QAReason}
   * @param target_name the target name
   * @throws MEMEException if failed to remove reason
   */
  public void removeQAReason(QAReason qareason, String target_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "removeQAReason"));
    request.addParameter(new Parameter.Default("qareason", qareason));
    request.addParameter(new Parameter.Default("target_name", target_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Returns the "CUI Comparison" {@link QAReport} for the specified cui.
   * @param cui the CUI
   * @param release_name the release name
   * @return the "CUI Comparison" {@link QAReport}
   * @throws MEMEException if failed to remove reason
   */
  public QAReport getCuiComparisonReport(String cui, String release_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "getCuiComparisonReport"));
    request.addParameter(new Parameter.Default("cui", cui));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (QAReport) request.getReturnValue("report").getValue();
  }

  /**
   * Requests that the server generate a "CUI Comparison" {@link QAReport} from
   * the specified CUI.
   * @param cui the CUI
   * @param current the current version of the release files
   * @param compareTo the version to compare to
   * @throws MEMEException if failed to remove reason
   */
  public void generateCuiComparisonReport(String cui, String current,
                                          String compareTo) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "generateCuiComparisonReport"));
    request.addParameter(new Parameter.Default("cui", cui));
    request.addParameter(new Parameter.Default("current", current));
    request.addParameter(new Parameter.Default("compareTo", compareTo));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Returns the specified {@link ReleaseTarget}.
   * @param release_name the release name
   * @param target_name the target name
   * @return the specified {@link ReleaseTarget}
   * @throws MEMEException if failed to get target
   */
  public ReleaseTarget getTarget(String release_name, String target_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getTarget"));
    request.addParameter(new Parameter.Default("release_name", release_name));
    request.addParameter(new Parameter.Default("target_name", target_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (ReleaseTarget) request.getReturnValue("target").getValue();
  }

  /**
   * Returns the {@link ReleaseTarget} with the stage status populated.
   * @param release_name the release name
   * @param target_name the target name
   * @return the {@link ReleaseTarget} with the stage status populated
   * @throws MEMEException if failed to get target
   */
  public ReleaseTarget getTargetStatus(String release_name, String target_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getTargetStatus"));
    request.addParameter(new Parameter.Default("release_name", release_name));
    request.addParameter(new Parameter.Default("target_name", target_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (ReleaseTarget) request.getReturnValue("target").getValue();
  }

  /**
   * Returns the {@link ReleaseTarget} with the needs review {@link QAReport} populated.
   * @param release_name the release name
   * @param target_name the target name
   * @return the {@link ReleaseTarget} with the needs review {@link QAReport} populated
   * @throws MEMEException if failed to get target
   */
  public ReleaseTarget getTargetQAReport(String release_name,
                                         String target_name) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getTargetQAReport"));
    request.addParameter(new Parameter.Default("release_name", release_name));
    request.addParameter(new Parameter.Default("target_name", target_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (ReleaseTarget) request.getReturnValue("target").getValue();
  }

  /**
   * Returns all {@link ReleaseTarget}s.
   * @param release_name the release name
   * @return all {@link ReleaseTarget}s
   * @throws MEMEException if failed to get targets
   */
  public ReleaseTarget[] getTargets(String release_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getTargets"));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (ReleaseTarget[]) request.getReturnValue("targets").getValue();
  }

  /**
   * Returns all release target names.
   * @param release_name the release name
   * @return all release target names
   * @throws MEMEException if failed to get target names
   */
  public String[] getTargetNames(String release_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "getTargetNames"));
    request.addParameter(new Parameter.Default("release_name", release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    return (String[]) request.getReturnValue("targets").getValue();
  }

  /**
   * Activates the handler for the specified target.
   * @param target_name the target name
   * @throws MEMEException if failed to activate target handler
   */
  public void activateTargetHandler(String target_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "activateTargetHandler"));
    request.addParameter(new Parameter.Default("target_name", target_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Activates the handler for the specified targets.
   * @param target_names the target names
   * @throws MEMEException if failed to activate target handler
   */
  public void activateTargetHandlers(String[] target_names) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "activateTargetHandlers"));
    request.addParameter(new Parameter.Default("target_names", target_names));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Deactivates the handler for the specified targets.
   * @param target_name the target names
   * @throws MEMEException if failed to deactivate target handler
  */
  public void deactivateTargetHandler(String target_name) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function",
                                               "deactivateTargetHandler"));
    request.addParameter(new Parameter.Default("target_name", target_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Deactivates the handler for the specified targets.
   * @param target_names the target names
   * @throws MEMEException if failed to deactivate target handlers
  */
  public void deactivateTargetHandlers(String[] target_names) throws
      MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "deactivateTargetHandlers"));
    request.addParameter(new Parameter.Default("target_names", target_names));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Applies changes to the specified {@link ReleaseInfo}.
   * @param release_info the {@link ReleaseInfo} to change
   * @throws MEMEException if failed to set release info
   */
  public void setReleaseInfo(ReleaseInfo release_info) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "setReleaseInfo"));
    request.addParameter(new Parameter.Default("release_info", release_info));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Remove the specified {@link ReleaseInfo}.
   * @param release_info the {@link ReleaseInfo} to remove
   * @throws MEMEException if failed to remove release info
   */
  public void removeReleaseInfo(ReleaseInfo release_info) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();

    request.addParameter(new Parameter.Default("function", "removeReleaseInfo"));
    request.addParameter(new Parameter.Default("release_info", release_info));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  protected MEMEServiceRequest getServiceRequest() {
    MEMEServiceRequest request = new MEMEServiceRequest();
    request.setService(getReleaseGenerator());
    request.setMidService(mid_service);
    request.setNoSession(true);
    return request;
  }

  /**
   * This inner class serves as a default implementation of the
   * {@link ReleaseClient} class.
   */
  public static class Default extends ReleaseClient {

    /**
     * Instantiates a {@link ReleaseClient.Default} connected to "mrd-db".
     * @throws MEMEException if the required properties are not set,
     * or if the protocol handler cannot be instantiated
     */
    public Default() throws MEMEException {
      this("mrd-db");
    }

    /**
     * Instantiates a {@link ReleaseClient.Default} connected to the
     * specified mid service.
     * @param service the mid service
     * @throws MEMEException if the required properties are not set,
     * or if the protocol handler cannot be instantiated
     */
    public Default(String service) throws MEMEException {
      super();
      this.mid_service = service;
      setReleaseGenerator("ReleaseGenerator");
    }
  }
}
