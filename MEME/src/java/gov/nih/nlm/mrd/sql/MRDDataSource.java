/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.sql
 * Object:  MRDDataSource
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.sql;

import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.mrd.common.QAComparisonReason;
import gov.nih.nlm.mrd.common.QAReason;
import gov.nih.nlm.mrd.common.QAResult;
import gov.nih.nlm.mrd.common.QAResultReason;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.server.MRDProcessHandler;
import gov.nih.nlm.mrd.server.RegisteredHandler;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.util.ColumnStatistics;
import gov.nih.nlm.util.FileStatistics;

import java.util.ArrayList;

/**
 * Generically represents a connection to a source of MRD data.
 *
 * @author  MEME Group
 */
public interface MRDDataSource extends MEMEDataSource {

  /**
   * Adds a concept_id to the clean_concepts table if it is not already there.
   * @param concept_id int
   * @throws DataSourceException if failed to add the event.
   */
  public void addCleanConcept(int concept_id) throws DataSourceException;

  /**
   * Removes a concept_id from the clean_concepts table.
   * @param concept_id int
   * @throws DataSourceException if failed to remove the concept id.
   */
  public void removeCleanConcept(int concept_id) throws DataSourceException;

  /**
   * Given a concept_id this method returns true, if the concept is clean, i.e.
   * if all entries in the CRACS tables which are part of this concept have
   * status in "R" or "U".
   * @param concept_id int
   * @return true if the concept is clean, otherwise false.
   * @throws DataSourceException if failed to check the concept id.
   */
  public boolean isConceptClean(int concept_id) throws DataSourceException;

  /**
   * Returns the last extraction id.
   * @return An <code>int</code> representation of last extraction id.
   * @throws DataSourceException if failed to load id.
   */
  public int getLastExtractionId() throws DataSourceException;

  /**
   * Sets the last extraction id.
   * @param id An <code>int</code> representation of last extraction id.
   * @throws DataSourceException if failed to set id.
   */
  public void setLastExtractionId(int id) throws DataSourceException;

  /**
   * Removes the data change event from the action log.
   * @param event LoggedAction
   * @throws DataSourceException if failed to remove the event.
   */
  public void removeDataChangeEvent(LoggedAction event) throws DataSourceException;

  /**
   * Returns a list of the names of the registered state handler
   * @return Array of object {@link MRDProcessHandler} representation of process handler.
       * @throws DataSourceException if failed to get the registered state handlers.
   */
  public MRDProcessHandler[] getRegisteredStateHandlers() throws
      DataSourceException;

  /**
   * Returns a list of the names of the registered feedkback handler
   * @return Array of object {@link MRDProcessHandler} representation of process handler.
       * @throws DataSourceException if failed to get the registered feedback handlers.
   */
  public MRDProcessHandler[] getRegisteredFeedbackHandlers() throws
      DataSourceException;

  /**
   * Get the file column statistics
   * @param file An object {@link String} representation of file name.
   * @param col An object {@link String} representation of column name.
       * @return An object {@link ColumnStatistics} representation of column statistics
       * @throws DataSourceException if failed to get columnn statistics of the file.
   */
  public ColumnStatistics getColumnStatistics(String file, String col) throws
      DataSourceException;

  /**
   * Set the file column statistics
   * @param stats An object {@link ColumnStatistics} representation of column statistics
       * @throws DataSourceException if failed to set columnn statistics of the file.
   */
  public void setColumnStatistics(ColumnStatistics stats) throws
      DataSourceException;

  /**
   * Get the file statistics
   * @param file An object {@link String} representation of file name.
       * @return An object {@link FileStatistics} representation of column statistics
   * @throws DataSourceException if failed to get file statistics.
   */
  public FileStatistics getFileStatistics(String file) throws
      DataSourceException;

  /**
   * Set the file statistics
   * @param stats An object {@link FileStatistics} representation of column statistics
   * @throws DataSourceException if failed to set file statistics.
   */
  public void setFileStatistics(FileStatistics stats) throws
      DataSourceException;

  /**
   * Get release info
       * @param release_name An object {@link String} representation of release name.
   * @return An object {@link ReleaseInfo} representation of release.
   * @throws DataSourceException if failed to get release info.
   */
  public ReleaseInfo getReleaseInfo(String release_name) throws
      DataSourceException;

  /**
   * Set the release info
   * @param release_info ReleaseInfo
   * @throws DataSourceException if failed to update release info.
   */
  public void setReleaseInfo(ReleaseInfo release_info) throws
      DataSourceException;

  /**
   * Add release info into release history
   * @param release_info ReleaseInfo
   * @throws DataSourceException if failed to add release info.
   */
  public void addReleaseInfo(ReleaseInfo release_info) throws
      DataSourceException;

  /**
   * Remove the release info from release history
   * @param release_info ReleaseInfo
   * @throws DataSourceException if failed to delete release info.
   */
  public void removeReleaseInfo(ReleaseInfo release_info) throws
      DataSourceException;

  /**
   * Get all release history
   * @return An array of object {@link ReleaseInfo} representation of release.
   * @throws DataSourceException if failed to get release history.
   */
  public ReleaseInfo[] getReleaseHistory() throws DataSourceException;

  /**
   *  Returns a list of the names of the registered release handler
   *  @param type An object {@link String} representation of type of the handler.
   *  @return An array of object {@link ReleaseHandler} representation of relese handler.
   *  @throws DataSourceException if failed to get release handlers.
   */
  public ReleaseHandler[] getReleaseHandlers(String type) throws
      DataSourceException;

  /**
   *  Returns a registered release handler
   *  @param type An object {@link String} representation of type of the handler.
       *  @param handler_name An object {@link String} representation of handler name.
       *  @return An object {@link ReleaseHandler} representation of relese handler.
   *  @throws MEMEException if failed to get release handler.
   */
  public ReleaseHandler getReleaseHandler(String type, String handler_name) throws
      MEMEException;

  /**
   * Gets QA results from the qa_xxxx_<release> table
       * @param release_name An object {@link String} representation of release name.
   * @param target_name An object {@link String} representation of target name.
   * @return Array of object {@link QAResult} representation of qa result.
   * @throws DataSourceException if failed to get QAResults.
   */
  public QAResult[] getQAResults(String release_name, String target_name) throws
      DataSourceException;

  /**
   * Gets QA results from the qa_xxxx_gold_<release> table
       * @param release_name An object {@link String} representation of release name.
   * @param target_name An object {@link String} representation of target name.
   * @return An array of object {@link QAResult} representation of QAResults.
   * @throws DataSourceException if failed to get QAResults.
   */

  public QAResult[] getGoldStandardQAResults(String release_name,
                                             String target_name) throws
      DataSourceException;

  /**
   * activate target handler
   * @param handler An object {@link RegisteredHandler} representation of target handler.
   * @throws DataSourceException if failed to activate target handler.
   */
  public void activateRegisteredHandler(RegisteredHandler handler) throws
      DataSourceException;

  /**
   * deactivate target handler
   * @param handler An object {@link RegisteredHandler} representation of target handler.
   * @throws DataSourceException if failed to deactivate target handler.
   */
  public void deactivateRegisteredHandler(RegisteredHandler handler) throws
      DataSourceException;

  /**
   * The ArrayList contains OrderedHashMap's with keySet {"rank", "sab",
   * "tty", "supres"} and valueSet containing the corresponding strings.
   * @return An object {@link ArrayList} representation of release termgroups.
   * @throws DataSourceException if failed to get release termgroups.
   */
  public ArrayList getReleaseTermgroups() throws DataSourceException;

  /**
   * Gets QA result reasons from the table
   * @param release_name An array of {@link String} representation of release name.
   * @param comparison_name An array of {@link String} representation of comparison name.
   * @param target_name An object {@link String} representation of target name.
   * @return An array of object {@link QAReason} representation of QAResult reasons.
   * @throws DataSourceException if failed to get QAResults.
   */
  public QAResultReason[] getQAResultReasons(String[] release_name,
      String[] comparison_name,
      String target_name) throws
      DataSourceException;

  /**
   * Gets QA result reasons from the table
   * @param release_name An array of {@link String} representation of release name.
   * @param comparison_name An array of {@link String} representation of comparison name.
   * @param target_name An object {@link String} representation of target name.
   * @return An array of object {@link QAReason} representation of QAResult reasons.
   * @throws DataSourceException if failed to get QAResults.
   */
  public QAComparisonReason[] getQAComparisonReasons(String[] release_name,
      String[] comparison_name,
      String target_name) throws
      DataSourceException;

  /**
   * Add the QAreason
   * @param qareason An object {@link QAReason} representation of QAReason.
   * @param target_name An object {@link String} representation of target name.
   * @throws DataSourceException if failed to add QAReason.
   */
  public void addQAReason(QAReason qareason, String target_name) throws
      DataSourceException;

  /**
   * Remove the QAReason
   * @param qareason An object {@link QAReason} representation of QAReason.
   * @param target_name An object {@link String} representation of target name.
   * @throws DataSourceException if failed to remove reason.
   */
  public void removeQAReason(QAReason qareason, String target_name) throws
      DataSourceException;

  /**
   * Gets the list sources in given type
   * @param type the source type
   * @param release_info the {@link ReleaseInfo}
   * @return all sources for given type
   * @throws DataSourceException if failed to get sources.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Source[] getSourceByType(int type, ReleaseInfo release_info) throws
      BadValueException, DataSourceException;
}