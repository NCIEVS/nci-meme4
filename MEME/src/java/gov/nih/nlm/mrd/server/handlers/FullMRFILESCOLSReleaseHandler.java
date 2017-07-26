/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRFILESCOLSReleaseHandler.java
 *
 ***********************************************************************/

package gov.nih.nlm.mrd.server.handlers;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.sql.FileColumnStatisticsHandler;
import gov.nih.nlm.util.ColumnStatistics;
import gov.nih.nlm.util.FileStatistics;
import gov.nih.nlm.util.SystemToolkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Vector;

/**
 * Handler for "MRFILESCOLS" target.
 *
 * @author TTN, BAC
 */
public class FullMRFILESCOLSReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullMRFILESCOLSReleaseHandler}.
   */
    public FullMRFILESCOLSReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Compute and write <code>MRCOLS.RRF</code> and <code>MRFILES.RRF</code>
   * to "Build URI" META direcgtory.
   * @throws MEMEException if failed to generate
   */
  public void generate() throws MEMEException {
    try {
      DecimalFormat formatter = new DecimalFormat("0.00");
      FileColumnStatisticsHandler f_handler =
          new FileColumnStatisticsHandler("MRFILES", data_source);

      FileColumnStatisticsHandler c_handler =
          new FileColumnStatisticsHandler("MRCOLS", data_source);
      String[] files;
      StringBuffer query = new StringBuffer();
      try {
        query
            .append("SELECT key, value, description ")
            .append("FROM mrd_properties ")
            .append("WHERE key_qualifier = 'MRFILES'")
            .append("  AND expiration_date IS NULL");
        MEMEToolkit.trace("Query: " + query.toString());
        Statement stmt = data_source.createStatement();
        ResultSet rset = stmt.executeQuery(query.toString());
        Vector v = new Vector();
        while (rset.next()) {
          v.addElement(rset.getString("key"));
        }
        files = new String[v.size()];
        v.copyInto(files);
      } catch (SQLException e) {
        DataSourceException dse = new DataSourceException(
            "Failded to get MRFILES values. ");
        dse.setDetail("statement", query.toString());
        dse.setEnclosedException(e);
        throw dse;
      }

      StringBuffer mrcols = new StringBuffer();
      StringBuffer mrfiles = new StringBuffer();

      for (int i = 0; i < files.length; i++) {

        if (files[i].equals("MRCOLS") || files[i].equals("MRFILES")) {
          continue;
        }

        FileStatistics stats = data_source.getFileStatistics(files[i]);
        // construct a mrfiles line from stats
        StringBuffer mrfiles_line = new StringBuffer();
        mrfiles_line
            .append(stats.getFileName())
            .append(".RRF")
            .append("|")
            .append(stats.getDescription())
            .append("|");
        ColumnStatistics[] col_stats = stats.getAllColumnStatistics();
        for (int j = 0; j < col_stats.length - 1; j++) {
          mrfiles_line.append(col_stats[j].getColumnName()).append(",");
        }
        mrfiles_line
            .append(col_stats[col_stats.length - 1].getColumnName())
            .append("|")
            .append(col_stats.length)
            .append("|")
            .append(stats.getLineCount())
            .append("|")
            .append(stats.getByteCount())
            .append("|");
        mrfiles.append(f_handler.processLine(mrfiles_line.toString()));
        mrfiles.append("\n");
        for (int j = 0; j < col_stats.length; j++) {
          //construct mrcols line from col_stats
          StringBuffer mrcols_line = new StringBuffer();
          mrcols_line
              .append(col_stats[j].getColumnName())
              .append("|")
              .append(col_stats[j].getDescription())
              .append("|")
              .append("|")
              .append(col_stats[j].getMinLength())
              .append("|")
              .append(formatter.format(col_stats[j].getAverageLength()))
              .append("|")
              .append(col_stats[j].getMaxLength())
              .append("|")
              .append(col_stats[j].getFileName())
              .append(".RRF")
              .append("|")
              .append(col_stats[j].getDataType())
              .append("|");
          mrcols.append(c_handler.processLine(mrcols_line.toString()));
          mrcols.append("\n");
        }
      }

      // now build a mrcols line for MRCOLS and pass it to f_handler
      // and c_handler, then append the line to mrcols (stringbuffer)
      // with \n
      FileStatistics stats = c_handler.getFileStatistics();
      ColumnStatistics[] col_stats = stats.getAllColumnStatistics();
      for (int j = 0; j < col_stats.length; j++) {
        //construct mrcols line from col_stats
        StringBuffer mrcols_line = new StringBuffer();
        mrcols_line
            .append(col_stats[j].getColumnName())
            .append("|")
            .append(col_stats[j].getDescription())
            .append("|")
            .append("|")
            .append(col_stats[j].getMinLength())
            .append("|")
            .append(formatter.format(col_stats[j].getAverageLength()))
            .append("|")
            .append(col_stats[j].getMaxLength())
            .append("|")
            .append(col_stats[j].getFileName())
            .append(".RRF")
            .append("|")
            .append(col_stats[j].getDataType())
            .append("|");
        c_handler.processLine(mrcols_line.toString());
      }
      StringBuffer mrfiles_line = new StringBuffer();
      mrfiles_line
          .append(stats.getFileName())
          .append(".RRF")
          .append("|")
          .append(stats.getDescription())
          .append("|");
      for (int j = 0; j < col_stats.length - 1; j++) {
        mrfiles_line.append(col_stats[j].getColumnName()).append(",");
      }
      mrfiles_line
          .append(col_stats[col_stats.length - 1].getColumnName())
          .append("|")
          .append(col_stats.length)
          .append("|")
          .append(stats.getLineCount())
          .append("|")
          .append(stats.getByteCount())
          .append("|");
      f_handler.processLine(mrfiles_line.toString());

      // then build a mrfiles line for MRFILES and pass it
      // to f_handler and c_handler, then append the line to
      // mrfiles (string buffer) with \n.
      stats = f_handler.getFileStatistics();

      col_stats = stats.getAllColumnStatistics();
      for (int j = 0; j < col_stats.length; j++) {
        //construct mrcols line from col_stats
        StringBuffer mrcols_line = new StringBuffer();
        mrcols_line
            .append(col_stats[j].getColumnName())
            .append("|")
            .append(col_stats[j].getDescription())
            .append("|")
            .append("|")
            .append(col_stats[j].getMinLength())
            .append("|")
            .append(formatter.format(col_stats[j].getAverageLength()))
            .append("|")
            .append(col_stats[j].getMaxLength())
            .append("|")
            .append(col_stats[j].getFileName())
            .append(".RRF")
            .append("|")
            .append(col_stats[j].getDataType())
            .append("|");
        c_handler.processLine(mrcols_line.toString());
      }
      mrfiles_line = new StringBuffer();
      mrfiles_line
          .append(stats.getFileName())
          .append("|")
          .append(stats.getDescription())
          .append("|");
      for (int j = 0; j < col_stats.length - 1; j++) {
        mrfiles_line.append(col_stats[j].getColumnName()).append(",");
      }
      mrfiles_line
          .append(col_stats[col_stats.length - 1].getColumnName())
          .append("|")
          .append(col_stats.length)
          .append("|")
          .append(stats.getLineCount())
          .append("|")
          .append(stats.getByteCount())
          .append("|");
      f_handler.processLine(mrfiles_line.toString());

      stats = c_handler.getFileStatistics();
      col_stats = stats.getAllColumnStatistics();
      for (int j = 0; j < col_stats.length; j++) {
        //construct mrcols line from col_stats
        StringBuffer mrcols_line = new StringBuffer();
        mrcols_line
            .append(col_stats[j].getColumnName())
            .append("|")
            .append(col_stats[j].getDescription())
            .append("|")
            .append("|")
            .append(col_stats[j].getMinLength())
            .append("|")
            .append(formatter.format(col_stats[j].getAverageLength()))
            .append("|")
            .append(col_stats[j].getMaxLength())
            .append("|")
            .append(col_stats[j].getFileName())
            .append(".RRF")
            .append("|")
            .append(col_stats[j].getDataType())
            .append("|");
        mrcols.append(mrcols_line.toString());
        mrcols.append("\n");
      }
      mrfiles_line = new StringBuffer();
      mrfiles_line
          .append(stats.getFileName())
          .append(".RRF")
          .append("|")
          .append(stats.getDescription())
          .append("|");
      for (int j = 0; j < col_stats.length - 1; j++) {
        mrfiles_line.append(col_stats[j].getColumnName()).append(",");
      }
      mrfiles_line
          .append(col_stats[col_stats.length - 1].getColumnName())
          .append("|")
          .append(col_stats.length)
          .append("|")
          .append(stats.getLineCount())
          .append("|")
          .append(stats.getByteCount())
          .append("|");
      mrfiles.append(mrfiles_line.toString());
      mrfiles.append("\n");

      // then build a mrfiles line for MRFILES and pass it
      // to f_handler and c_handler, then append the line to
      // mrfiles (string buffer) with \n.
      stats = f_handler.getFileStatistics();

      col_stats = stats.getAllColumnStatistics();
      for (int j = 0; j < col_stats.length; j++) {
        //construct mrcols line from col_stats
        StringBuffer mrcols_line = new StringBuffer();
        mrcols_line
            .append(col_stats[j].getColumnName())
            .append("|")
            .append(col_stats[j].getDescription())
            .append("|")
            .append("|")
            .append(col_stats[j].getMinLength())
            .append("|")
            .append(formatter.format(col_stats[j].getAverageLength()))
            .append("|")
            .append(col_stats[j].getMaxLength())
            .append("|")
            .append(col_stats[j].getFileName())
            .append(".RRF")
            .append("|")
            .append(col_stats[j].getDataType())
            .append("|");
        mrcols.append(mrcols_line.toString());
        mrcols.append("\n");
      }
      mrfiles_line = new StringBuffer();
      mrfiles_line
          .append(stats.getFileName())
          .append(".RRF")
          .append("|")
          .append(stats.getDescription())
          .append("|");
      for (int j = 0; j < col_stats.length - 1; j++) {
        mrfiles_line.append(col_stats[j].getColumnName()).append(",");
      }
      mrfiles_line
          .append(col_stats[col_stats.length - 1].getColumnName())
          .append("|")
          .append(col_stats.length)
          .append("|")
          .append(stats.getLineCount())
          .append("|")
          .append(stats.getByteCount())
          .append("|");
      mrfiles.append(mrfiles_line.toString());
      mrfiles.append("\n");

      MEMEToolkit.trace("mrcols = " + mrcols);
      MEMEToolkit.trace("mrfiles = " + mrfiles);

      String dir_name = release.getBuildUri() + File.separator + "META";
      File file = new File(dir_name, "MRCOLS.RRF");
      MEMEToolkit.logCommentToBuffer(
          "WRITING " + dir_name + File.separator + file.getName(),
          true, log);
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
      out.print(mrcols);
      out.close();
      try {
        SystemToolkit.sort(dir_name + File.separator + file.getName());
      } catch (IOException ioe) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to sort the target file", ioe);
        ere.setDetail("file", file.getName());
        throw ere;
      }
      file = new File(dir_name, "MRFILES.RRF");
      MEMEToolkit.logCommentToBuffer(
          "WRITING " + dir_name + File.separator + file.getName(),
          true, log);
      out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
      out.print(mrfiles);
      out.close();
      try {
        SystemToolkit.sort(dir_name + File.separator + file.getName());
      } catch (IOException ioe) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to sort the target file", ioe);
        ere.setDetail("file", file.getName());
        throw ere;
      }
    } catch (Exception e) {
      DeveloperException dev = new DeveloperException(
          "Failed to generate the release data", this);
      dev.setEnclosedException(e);
      throw dev;
    }

  }

  /**
   * Returns the file list.
   * @return the file list
   * @throws MEMEException if failed to get file list
   */
  public String[] getFiles() throws MEMEException {
    return new String[] {
        "MRFILES", "MRCOLS"};
  }
}