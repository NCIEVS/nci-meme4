/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRMAPReleaseHandler.java
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
 * Handler for "MRMAP" target.
 *
 * @author TTN, BAC
 * 03/13/2009: SL:Modifying the MRSSAB where clause. 
 */
public class FullMRMAPReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullMRMAPReleaseHandler}.
   */
  public FullMRMAPReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Calls <code>MRD_RELEASE_OPERATIONS.mrmap_prepare</code>.
   * @throws DataSourceException if failed to prepare
   */
  public void prepare() throws DataSourceException {

    StringBuffer call = new StringBuffer(50);
    try {
      call.append("{call MRD_RELEASE_OPERATIONS.mrmap_prepare ()}");
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
   * Writes <code>MRMAP.RRF</code> and <code>MRSMAP.RRF</code>
   * to "Build URI" META directory.
   * @throws MEMEException if failed to generate
   */
  public void generate() throws MEMEException {

    OrderedHashMap order = new OrderedHashMap();
    order.put("1", null);

    DataWriterConstraints constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    constr.order = order;

    DataWriter writer = new DataWriter.Default(data_source);

    String dir_name = release.getBuildUri() + File.separator + "META";
    File file = new File(dir_name, "MRMAP.RRF");

    FileColumnStatisticsHandler statsHandler = new FileColumnStatisticsHandler(
        file.getName().substring(0, file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator +
                                   file.getName(), true, log);
    writer.write(file, "mrmap_pre", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    order = new OrderedHashMap();
    order.put("1", null);

    constr = new DataWriterConstraints();

    constr.addHandler(new StripDuplicateSpacesHandler());

    constr.order = order;

    OrderedHashMap fields = new OrderedHashMap();
    fields.put("mapsetcui", null);
    fields.put("mapsetsab", null);
    fields.put("mapid", null);
    fields.put("mapsid", null);
    fields.put("fromexpr", null);
    fields.put("fromtype", null);
    fields.put("rel", null);
    fields.put("rela", null);
    fields.put("toexpr", null);
    fields.put("totype", null);
    fields.put("cvf", null);
    constr.fields_to_write = fields;

    String[] conds = new String[] {"NVL(mapsubsetid,'0') = '0'",
        "NVL(maprank,0) = 0"};
    constr.conditions = conds;

    file = new File(dir_name, "MRSMAP.RRF");

    statsHandler = new FileColumnStatisticsHandler(file.getName().substring(0,
        file.getName().indexOf('.')), data_source);
    constr.addHandler(statsHandler);

    MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator +
                                   file.getName(), true, log);
    writer.write(file, "mrmap_pre", constr);

    data_source.setFileStatistics(statsHandler.getFileStatistics());

    dropTable("mrmap_pre");
  }

  /**
   * Returns the file list.
   * @return the file list
   * @throws MEMEException if failed to get file list
   */
  public String[] getFiles() throws MEMEException {
    return new String[] {
        "MRMAP", "MRSMAP"};
  }
}
