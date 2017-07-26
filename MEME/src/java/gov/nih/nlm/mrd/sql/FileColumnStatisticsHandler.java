/*******************************************************************************
 *
 *  Package:    gov.nih.nlm.mrd.sql
 *  Object:     FileColumnStatisticsHandler.java
 *
 ******************************************************************************/

package gov.nih.nlm.mrd.sql;

import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.DataWriterHandler;
import gov.nih.nlm.util.FileStatistics;

/**
 * {@link DataWriterHandler} for computing file and column statistics.
 */
public class FileColumnStatisticsHandler implements DataWriterHandler {

  //
  // Fields
  //
  private FileStatistics file_stats = null;

  /**
   * Instantiates a {@link FileColumnStatisticsHandler} for the specified file and MRD data source.
   * @param file_name the file
   * @param ds the data source
   * @throws DataSourceException if failed to initialize
   */
  public FileColumnStatisticsHandler(String file_name, MRDDataSource ds)
    throws DataSourceException {
    file_stats = ds.getFileStatistics(file_name);
    file_stats.clear();
  }

  /**
   * Computes column and file statistics.
   * @param line the line to process
   * @return the processed line
   * @throws MEMEException if failed to process line
   */
  public String processLine(String line) throws MEMEException {
    return file_stats.processLine(line);
  }
  /**
   * Returns the {@link FileStatistics}.
   * @return the {@link FileStatistics}
   */
  public FileStatistics getFileStatistics() {
    return file_stats;
  }
}