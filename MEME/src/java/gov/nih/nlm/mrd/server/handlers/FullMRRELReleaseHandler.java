/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRRELReleaseHandler.java
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
 * Handler for "MRREL" target.
 *
 * @author TTN, BAC
 */
public class FullMRRELReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullMRRELReleaseHandler}.
   */
  public FullMRRELReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Calls <code>MRD_RELEASE_OPERATIONS.mrrel_prepare</code>.
   * @throws DataSourceException if failed to prepare
   */
  public void prepare() throws DataSourceException {
    StringBuffer call = new StringBuffer(50);
    try {
      call.append("{call MRD_RELEASE_OPERATIONS.mrrel_prepare ()}");
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
   * Writes <code>MRREL.RRF</code> to "Build URI" META directory.
   * @throws MEMEException if failed to generate
   */
  public void generate() throws MEMEException {
    DataWriterConstraints constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    OrderedHashMap order = new OrderedHashMap();
    order.put("1", null);

    constr.order = order;
    //constr.conditions = new String[] {"rownum <= 40"};

    DataWriter writer = new DataWriter.Default(data_source);

    String dir_name = release.getBuildUri() + File.separator + "META";
    File file = new File(dir_name, "MRREL.RRF");

    FileColumnStatisticsHandler statsHandler = new FileColumnStatisticsHandler(
        file.getName().substring(0, file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator +
                                   file.getName(), true, log);
    writer.write(file, "mrrel_pre", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    dropTable("mrrel_pre");
  }
}
