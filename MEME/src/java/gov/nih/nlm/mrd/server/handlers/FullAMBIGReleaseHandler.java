/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullAMBIGReleaseHandler.java
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
 * Handles creation of <code>AMBIG.LUI</code> and <code>AMBIG.SUI</code>.
 *
 * @author Tun Tun Naing
 */
public class FullAMBIGReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullAMBIGReleaseHandler}.
   */
  public FullAMBIGReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Calls <code>MRD_RELEASE_OPERATIONS.ambig_prepare</code>.
   * @throws DataSourceException if failed to prepare
   */
  public void prepare() throws DataSourceException {

    StringBuffer call = new StringBuffer(50);
    try {
      call.append("{call MRD_RELEASE_OPERATIONS.ambig_prepare ()}");
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
   * Generate files.
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
    File file = new File(dir_name, "AMBIGLUI.RRF");

    //
    // Look up stats
    //
    FileColumnStatisticsHandler statsHandler =
        new FileColumnStatisticsHandler(file.getName().substring(0,
        file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    //
    // Write AMBIGLUI.RRF
    //
    MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator +
                                   file.getName(), true, log);
    writer.write(file, "ambig_lui_pre", constr);

    //
    // Reconfigure stats
    //
    data_source.setFileStatistics(statsHandler.getFileStatistics());
    constr = new DataWriterConstraints();
    constr.addHandler(new StripDuplicateSpacesHandler());
    constr.order = order;
    file = new File(dir_name, "AMBIGSUI.RRF");
    statsHandler = new FileColumnStatisticsHandler(file.getName().substring(0,
        file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    //
    // Write file
    //
    MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator +
                                   file.getName(), true, log);
    writer.write(file, "ambig_sui_pre", constr);
    data_source.setFileStatistics(statsHandler.getFileStatistics());

    //
    // Cleanup
    //
    dropTable("ambig_lui_pre");
    dropTable("ambig_sui_pre");
  }

  /**
   * Returns the list of files.
   * @return the list of files
   * @throws MEMEException if failed to feedback
   */
  public String[] getFiles() throws MEMEException {
    return new String[] {"AMBIGLUI", "AMBIGSUI"};
  }

}