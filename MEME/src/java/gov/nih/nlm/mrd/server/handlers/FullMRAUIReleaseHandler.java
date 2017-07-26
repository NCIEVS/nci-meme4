/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRAUIReleaseHandler.java
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
import gov.nih.nlm.mrd.sql.FileColumnStatisticsHandler;
import gov.nih.nlm.util.OrderedHashMap;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Handler for "MRAUI" target.
 *
 * @author Tun Tun Naing
 */
public class FullMRAUIReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullMRAUIReleaseHandler}.
   */
  public FullMRAUIReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Calls <code>MRD_RELEASE_OPERATIONS.mraui_prepare</code>.
   * @throws DataSourceException if failed to prepare
   * */
  public void prepare() throws DataSourceException {
    StringBuffer call = new StringBuffer(50);
    try {
      call.append("{call MRD_RELEASE_OPERATIONS.mraui_prepare() }");

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
   * Write <code>$Build_Uri/META/MRAUI.RRF</code>.
   * @throws MEMEException if failed to generate
   */
  public void generate() throws MEMEException {

    //
    // Configure data writer
    //
    DataWriterConstraints constr = new DataWriterConstraints();
    constr.addHandler(new StripDuplicateSpacesHandler());
    OrderedHashMap order = new OrderedHashMap();
    order.put("1", null);
    constr.order = order;
    DataWriter writer = new DataWriter.Default(data_source);
    String dir_name = release.getBuildUri() + File.separator + "META";
    File file = new File(dir_name, "MRAUI.RRF");

    //
    // Look up file stats
    //
    FileColumnStatisticsHandler statsHandler =
        new FileColumnStatisticsHandler(file.getName().substring(0,
        file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    //
    // Write file
    //
    MEMEToolkit.logCommentToBuffer(
        "WRITING " + dir_name + File.separator + file.getName(),
        true, log);
    writer.write(file, "mraui_pre", constr);

    //
    // Set file stats
    //
    data_source.setFileStatistics(statsHandler.getFileStatistics());
  }

  /**
   * Returns the list of files belonging to this target.
   * @return the list of files belonging to this target
   * @throws MEMEException if failed to get file list
   */
  public String[] getFiles() throws MEMEException {
    return new String[] {"MRAUI", };
  }

}