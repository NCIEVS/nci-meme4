/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRLOReleaseHandler.java
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
 * <b>DO NOT USE</b>.
 */
public class FullMRLOReleaseHandler extends ReleaseHandler.Default {

  // Constructor
  public FullMRLOReleaseHandler() {
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
      call.append("{call MRD_RELEASE_OPERATIONS.mrlo_prepare (")
          .append("meta_mbd => '")
          .append(ServerToolkit.getDateFormat().format(release.getMBDStartDate()))
          .append("', meta_med => '")
          .append(ServerToolkit.getDateFormat().format(release.getMEDStartDate()))
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
   * Implements {@link ReleaseHandler#prepare()}
   * @throws MEMEException if failed to generate.
   */
  public void generate() throws MEMEException {
    DataWriterConstraints constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    OrderedHashMap order = new OrderedHashMap();
    order.put("1", null);

    constr.order = order;

    DataWriter writer = new DataWriter.Default(data_source);

    String dir_name = release.getBuildUri() + File.separator + "META";
    File file = new File(dir_name, "MRLO.RRF");

    //constr.addHandler(new VersionlessSourceHandler(file.getName(),data_source));

    FileColumnStatisticsHandler statsHandler = new FileColumnStatisticsHandler(
        file.getName().substring(0, file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator +
                                   file.getName(), true, log);
    writer.write(file, "mrlo_pre", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    dropTable("mrlo_pre");
  }
}
