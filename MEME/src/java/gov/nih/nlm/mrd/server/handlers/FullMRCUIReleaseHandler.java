/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRCUIReleaseHandler.java
 *
 ***********************************************************************/

package gov.nih.nlm.mrd.server.handlers;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.DataWriter;
import gov.nih.nlm.meme.sql.DataWriterConstraints;
import gov.nih.nlm.meme.sql.StripDuplicateSpacesHandler;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.server.ServerToolkit;
import gov.nih.nlm.mrd.sql.FileColumnStatisticsHandler;
import gov.nih.nlm.util.OrderedHashMap;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Handler for "MRCUI" target.
 *
 * @author TTN, BAC
 */
public class FullMRCUIReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullMRCUIReleaseHandler}.
   */
  public FullMRCUIReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Implements {@link ReleaseHandler#prepare()}
   * @throws DataSourceException if failed to prepare.
   */
  public void prepare() throws DataSourceException {
    StringBuffer call = new StringBuffer(50);
    try {
      call.append("{call MRD_RELEASE_OPERATIONS.mrcui_prepare (")
          .append("meta_previous => '")
          .append(ServerToolkit.getDateFormat().format(release.
          getPreviousReleaseInfo().getReleaseDate()))
          .append("', meta_previous_major => '")
          .append(ServerToolkit.getDateFormat().format(release.
          getPreviousMajorReleaseInfo().getReleaseDate()))
          .append("', prev_version => '")
          .append(release.getPreviousReleaseInfo().getName())
          .append("')}");
      CallableStatement cstmt = data_source.prepareCall(call.toString());
      cstmt.execute();
      cstmt.close();
    } catch (SQLException e) {
      DataSourceException me =
          new DataSourceException("Failed to prepare Release data.", this, e);
      me.setDetail("statement", call.toString());
      throw me;
    }
  }

  /**
   * Writes <code>MRCUI.RRF</code> into the "Build URI" META directory and the
   * "change" files into the CHANGE directory.
   * @throws MEMEException if failed to generate
   */
  public void generate() throws MEMEException {

    DataWriterConstraints constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    OrderedHashMap order = new OrderedHashMap();
    order.put("1", null);

    constr.order = order;

    DataWriter writer = new DataWriter.Default(data_source);

    String dir_name = release.getBuildUri() + File.separator + "META";
    File file = new File(dir_name, "CHANGE" + File.separator + "MERGEDCUI.RRF");

    FileColumnStatisticsHandler statsHandler =
        new FileColumnStatisticsHandler("CHANGE" + File.separator +
                                        file.
                                        getName().substring(0,
        file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer(
        "WRITING " + dir_name + File.separator + "CHANGE" + File.separator +
        file.getName(),
        true, log);
    writer.write(file, "t_merged_cuis", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    order = new OrderedHashMap();
    order.put("1", null);

    constr.order = order;
    file = new File(dir_name, "CHANGE" + File.separator + "DELETEDCUI.RRF");

    statsHandler = new FileColumnStatisticsHandler("CHANGE" + File.separator +
        file.getName().substring(0, file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer(
        "WRITING " + dir_name + File.separator + "CHANGE" + File.separator +
        file.getName(),
        true, log);
    writer.write(file, "t_deleted_cui", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    dropTable("t_deleted_cui");

    constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    order = new OrderedHashMap();
    order.put("1", null);

    constr.order = order;

    file = new File(dir_name, "CHANGE" + File.separator + "MERGEDLUI.RRF");

    statsHandler = new FileColumnStatisticsHandler("CHANGE" + File.separator +
        file.getName().substring(0, file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer(
        "WRITING " + dir_name + File.separator + "CHANGE" + File.separator +
        file.getName(),
        true, log);
    writer.write(file, "t_merged_luis", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    dropTable("t_merged_luis");

    constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    order = new OrderedHashMap();
    order.put("1", null);

    constr.order = order;
    file = new File(dir_name, "CHANGE" + File.separator + "DELETEDLUI.RRF");

    statsHandler = new FileColumnStatisticsHandler("CHANGE" + File.separator +
        file.getName().substring(0, file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer(
        "WRITING " + dir_name + File.separator + "CHANGE" + File.separator +
        file.getName(),
        true, log);
    writer.write(file, "t_deleted_lui", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    dropTable("t_deleted_lui");

    constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    order = new OrderedHashMap();
    order.put("1", null);

    constr.order = order;

    file = new File(dir_name, "CHANGE" + File.separator + "DELETEDSUI.RRF");

    statsHandler = new FileColumnStatisticsHandler("CHANGE" + File.separator +
        file.getName().substring(0, file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer(
        "WRITING " + dir_name + File.separator + "CHANGE" + File.separator +
        file.getName(),
        true, log);
    writer.write(file, "t_deleted_sui", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    dropTable("t_deleted_sui");

    constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    order = new OrderedHashMap();
    order.put("1", null);
    constr.order = order;

    file = new File(dir_name, "MRCUI.RRF");

    statsHandler =
        new FileColumnStatisticsHandler(file.getName().substring(0,
        file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer(
        "WRITING " + dir_name + File.separator + file.getName(),
        true, log);
    writer.write(file, "mrcui_pre");

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    dropTable("mrcui_pre");

  }

  /**
   * Generates feedback data
   * @throws DataSourceException if failed to feedback
  public void feedback() throws DataSourceException {
    StringBuffer call = new StringBuffer(50);
    try {
//      call.append("{call MRD_RELEASE_OPERATIONS.mrcui_feedback ()}");
      CallableStatement cstmt = data_source.prepareCall(call.toString());
      cstmt.execute();
      cstmt.close();
    } catch (SQLException e) {
      DataSourceException me =
          new DataSourceException("Failed to feedback Release data.", this, e);
      me.setDetail("statement", call.toString());
      throw me;
    }
  }
*/

  /**
   * Returns file list.
   * @return file list
   * @throws MEMEException if failed get file list
   */
  public String[] getFiles() throws MEMEException {
    return new String[] {
        "MRCUI",
        "CHANGE" + File.separator + "MERGEDCUI",
        "CHANGE" + File.separator + "DELETEDCUI",
        "CHANGE" + File.separator + "MERGEDLUI",
        "CHANGE" + File.separator + "DELETEDLUI",
        "CHANGE" + File.separator + "DELETEDSUI"
    };
  }
}