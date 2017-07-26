/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRCOCReleaseHandler.java
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
import gov.nih.nlm.mrd.sql.COASortHandler;
import gov.nih.nlm.mrd.sql.FileColumnStatisticsHandler;
import gov.nih.nlm.util.OrderedHashMap;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Handler for "MRCOC" target.
 * @author Tun Tun Naing, Brian Carlsen
 */
public class FullMRCOCReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullMRCOCReleaseHandler}.
   */
  public FullMRCOCReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Calls <code>MRD_RELEASE_OPERATIONS.mrcoc_prepare</codE>.
   * @throws DataSourceException if failed to prepare.
   */
  public void prepare() throws DataSourceException {
    StringBuffer call = new StringBuffer(50);
    try {
      call.append("{call MRD_RELEASE_OPERATIONS.mrcoc_prepare (")
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
   * Writes <code>$Build_Uri/META/MRAUI.RRF</code>.
   * @throws MEMEException if failed to generate
   */
  public void generate() throws MEMEException {

    DataWriterConstraints constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    constr.addHandler(new COASortHandler());

    OrderedHashMap order = new OrderedHashMap();
    order.put("1", null);

    constr.order = order;

    DataWriter writer = new DataWriter.Default(data_source);

    String dir_name = release.getBuildUri() + File.separator + "META";
    File file = new File(dir_name, "MRCOC.RRF");

    FileColumnStatisticsHandler statsHandler = new FileColumnStatisticsHandler(
        file.getName().substring(0, file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator +
                                   file.getName(), true, log);
    writer.write(file, "mrcoc_pre", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    dropTable("mrcoc_pre");
  }
}