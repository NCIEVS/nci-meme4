/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRRANKReleaseHandler.java
 *
 ***********************************************************************/

package gov.nih.nlm.mrd.server.handlers;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.sql.FileColumnStatisticsHandler;
import gov.nih.nlm.util.OrderedHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Handler for "MRRANK" target.
 *
 * @author TTN, BAC
 */
public class FullMRRANKReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullMRRANKReleaseHandler}.
   */
  public FullMRRANKReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Writes <code>MRRANK.RRF</code> in "Build URI" META directory.
   * @throws MEMEException if failed to generate
   */

  public void generate() throws MEMEException {

    String dir_name = release.getBuildUri() + File.separator + "META";
    File file = new File(dir_name, "MRRANK.RRF");

    FileColumnStatisticsHandler stats =
        new FileColumnStatisticsHandler(file.getName().substring(0,
        file.getName().indexOf('.')), data_source);

    MEMEToolkit.logCommentToBuffer(
        "WRITING " + dir_name + File.separator + file.getName(),
        true, log);

    ArrayList list = data_source.getReleaseTermgroups();
    PrintWriter out;
    try {
      out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

      int length = list.size();
      Iterator iter = list.iterator();

      while (iter.hasNext()) {
        OrderedHashMap map = (OrderedHashMap) iter.next();
        StringBuffer buffer = new StringBuffer();
        String str = "0000" + length--;
        buffer.append(str.substring(str.length() - 4));
        buffer.append("|");
        buffer.append( (String) map.get("root_source"));
        buffer.append("|");
        buffer.append( (String) map.get("tty"));
        buffer.append("|");
        buffer.append( (String) map.get("supres"));
        buffer.append("|");
        stats.processLine(buffer.toString());
        out.println(buffer.toString());
      }
      out.close();
      data_source.setFileStatistics(stats.getFileStatistics());
    } catch (Exception e) {
      DeveloperException dev = new DeveloperException(
          "Failed to generate the release data", this);
      dev.setEnclosedException(e);
      throw dev;
    }
  }

}
