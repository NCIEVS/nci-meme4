/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRXReleaseHandler.java
 *
 ***********************************************************************/

package gov.nih.nlm.mrd.server.handlers;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.server.ServerConstants;
import gov.nih.nlm.mrd.server.ServerToolkit;
import gov.nih.nlm.mrd.sql.FileColumnStatisticsHandler;
import gov.nih.nlm.nls.lvg.Api.NormApi;
import gov.nih.nlm.nls.lvg.Api.WordIndApi;
import gov.nih.nlm.util.FieldedStringTokenizer;
import gov.nih.nlm.util.SystemToolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
 * Handler for "MRX" target.
 * 
 * @author TTN, BAC
 */
public class FullMRXReleaseHandler extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullMRXReleaseHandler}.
   */
  public FullMRXReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Scans through <code>MRCONSO.RRF</code> and generates word, norm string,
   * and norm word indexes by using the LVG API. At the end it sorts each file.
   * 
   * @throws MEMEException
   *           if failed to generate
   */
  public void generate() throws MEMEException {
    data_source = 
      ServerToolkit.getMRDDataSource(
        data_source.getDataSourceName(), null, null);

    NormApi norm = null;
    WordIndApi wordind = null;
    try {
      String config_file = ServerToolkit
          .getProperty(ServerConstants.LVG_CONFIG_FILE);
      norm = new NormApi(config_file);
      wordind = new WordIndApi();
    } catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to initialize LVG Api.  The most likely cause " +
          "is a bad meme.lvg.config.file property setting.", e);
      throw ere;
    }

    try {

      String dir_name = release.getBuildUri() + File.separator + "META";
      HashMap lat_file_map = new HashMap();
      HashMap lat_writer_map = new HashMap();
      HashMap statsHandler_map = new HashMap();

      File file = new File(dir_name, "MRCONSO.RRF");
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));

      file = new File(dir_name, "MRXNS_ENG.RRF");
      PrintWriter xns_eng = new PrintWriter(new OutputStreamWriter(
              new FileOutputStream(file,false),
              "UTF-8"));
      //PrintWriter xns_eng = new PrintWriter(new BufferedWriter(new FileWriter(file)));
      FileColumnStatisticsHandler xns_eng_stats = new FileColumnStatisticsHandler(
          file.getName().substring(0, file.getName().indexOf('.')), data_source);
      MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator
          + file.getName(), true, log);

      file = new File(dir_name, "MRXNW_ENG.RRF");
      PrintWriter xnw_eng = new PrintWriter(new OutputStreamWriter(
              new FileOutputStream(file,false),
              "UTF-8"));
      //PrintWriter xnw_eng = new PrintWriter(new BufferedWriter(new FileWriter(file)));
      FileColumnStatisticsHandler xnw_eng_stats = new FileColumnStatisticsHandler(
          file.getName().substring(0, file.getName().indexOf('.')), data_source);

      MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator
          + file.getName(), true, log);

      String line;
      while ((line = in.readLine()) != null) {
        String[] fields = FieldedStringTokenizer.split(line, String
            .valueOf('|'));
        if (fields[1].equals("ENG")) {
          Vector norm_strings = norm.Mutate(fields[14]);
          HashSet hs = new HashSet();
          for (Enumeration e = norm_strings.elements(); e.hasMoreElements();) {
            String norm_str = (String) e.nextElement();
            Vector words = wordind.Mutate(norm_str);
            for (Enumeration ew = words.elements(); ew.hasMoreElements();) {
              hs.add((String) ew.nextElement());
            }
            StringBuffer buf = new StringBuffer();
            buf.append(fields[1]).append("|").append(norm_str).append("|")
                .append(fields[0]).append("|").append(fields[3]).append("|")
                .append(fields[5]).append("|");
            xns_eng_stats.processLine(buf.toString());
            xns_eng.println(buf.toString());
            for (Iterator iw = hs.iterator(); iw.hasNext();) {
              buf = new StringBuffer();
              buf.append(fields[1]).append("|").append((String) iw.next())
                  .append("|").append(fields[0]).append("|").append(fields[3])
                  .append("|").append(fields[5]).append("|");
              xnw_eng_stats.processLine(buf.toString());
              xnw_eng.println(buf.toString());
            }
          }
        }
        Vector words = wordind.Mutate(fields[14]);
        if (!lat_file_map.containsKey(fields[1])) {
          file = new File(dir_name, "MRXW_" + fields[1] + ".RRF");
          lat_file_map.put(fields[1], file);
          lat_writer_map.put(fields[1], new PrintWriter(new OutputStreamWriter(
                  new FileOutputStream(file,false),
                  "UTF-8")));
          //lat_writer_map.put(fields[1], new PrintWriter(new BufferedWriter(
           //   new FileWriter(file))));
          MEMEToolkit.logCommentToBuffer("WRITING " + dir_name + File.separator
              + file.getName(), true, log);
          statsHandler_map.put(fields[1],
              new FileColumnStatisticsHandler(file.getName().substring(0,
                  file.getName().indexOf('.')), data_source));
        }
        PrintWriter out = (PrintWriter) lat_writer_map.get(fields[1]);
        FileColumnStatisticsHandler stats_handler = (FileColumnStatisticsHandler) statsHandler_map
            .get(fields[1]);
        HashSet hs = new HashSet();
        for (Enumeration ew = words.elements(); ew.hasMoreElements();) {
          hs.add((String) ew.nextElement());
        }
        for (Iterator iw = hs.iterator(); iw.hasNext();) {
          StringBuffer buf = new StringBuffer();
          buf.append(fields[1]).append("|").append((String) iw.next()).append(
              "|").append(fields[0]).append("|").append(fields[3]).append("|")
              .append(fields[5]).append("|");
          stats_handler.processLine(buf.toString());
          out.println(buf.toString());
        }
      }
      xns_eng.close();
      xnw_eng.close();
      Iterator ilat = lat_writer_map.values().iterator();
      while (ilat.hasNext()) {
        ((Writer) ilat.next()).close();
      }
      ilat = lat_file_map.values().iterator();
      MEMEToolkit.logCommentToBuffer("SORTING " + dir_name + File.separator
          + "MRXNS_ENG.RRF", true, log);
      SystemToolkit.sort(dir_name + File.separator + "MRXNS_ENG.RRF", true);
      MEMEToolkit.logCommentToBuffer("SORTING " + dir_name + File.separator
          + "MRXNW_ENG.RRF", true, log);
      SystemToolkit.sort(dir_name + File.separator + "MRXNW_ENG.RRF", true);
      MEMEToolkit.logCommentToBuffer("SORTING " + dir_name + File.separator
          + "MRXW_*", true, log);
      while (ilat.hasNext()) {
        SystemToolkit.sort(dir_name + File.separator
            + ((File) ilat.next()).getName(), true);
      }
      ilat = statsHandler_map.values().iterator();
      data_source.setFileStatistics(xns_eng_stats.getFileStatistics());
      data_source.setFileStatistics(xnw_eng_stats.getFileStatistics());
      while (ilat.hasNext()) {
        data_source.setFileStatistics(((FileColumnStatisticsHandler) ilat
            .next()).getFileStatistics());
      }
    } catch (IOException ioe) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to write the target file", ioe);
      throw ere;
    } catch (Exception e) {
      DeveloperException dev = new DeveloperException(
          "Unexpected exception while generating release data.", this);
      dev.setEnclosedException(e);
      throw dev;
    }

  }

  /**
   * Returns target file list.
   * 
   * @return target file list
   * @throws MEMEException
   *           if failed to get file list
   */
  public String[] getFiles() throws MEMEException {
    ArrayList files = new ArrayList();
    files.add("MRXNS_ENG");
    files.add("MRXNW_ENG");
    Language[] langs = data_source.getLanguages();
    for (int i = 0; i < langs.length; i++) {
      files.add("MRXW_" + langs[i].getAbbreviation());
    }
    return (String[]) files.toArray(new String[0]);
  }
}