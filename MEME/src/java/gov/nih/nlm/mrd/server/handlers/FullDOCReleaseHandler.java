/************************************************************************
 *
 * Package:     gov.nih.nlm.mrd.server.handlers
 * Object:      FullMRDOCReleaseHandler.java
 * Changes:
 *   02/13/2006 TTN (1-798X1): change a report file name from section 2.7.1.2.5 to 2.7.1.3.10
 *   02/06/2006 TK (1-778JZ): Uncommented out section
 ***********************************************************************/

package gov.nih.nlm.mrd.server.handlers;

// for comparison stuff
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.XMLParseException;
import gov.nih.nlm.mrd.common.SourceType;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.mrd.server.ServerToolkit;
import gov.nih.nlm.util.XMLEntityEncoder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

/**
 * Handler for the "DOC" target.
 *
 * @author Tun Tun Naing, Brian Carlsen
 */
public class FullDOCReleaseHandler
    extends ReleaseHandler.Default {

  /**
   * Instantiates a {@link FullDOCReleaseHandler}.
   */
  public FullDOCReleaseHandler() {
    setProcess("RELEASE");
    setType("Full");
  }

  /**
   * Makes an HTML directory under the "build URI".
   * @throws MEMEException if anything goes wrong
   */
  public void prepare() throws MEMEException {
    try {
      ServerToolkit.logCommentToBuffer("Prepare HTML directory", true, log);
      File file = new File(release.getBuildUri() + File.separator + "HTML");
      if (!file.exists()) {
        file.mkdir();
      }
    }
    catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to make the output folder", e);
      throw ere;
    }
  }

  /**
   * Generates HTML documents for UMLS documentation.
   * @throws MEMEException if failed to generate
   */

  public void generate() throws MEMEException {
    try {
      String dir_name = release.getBuildUri() + File.separator + "META";
      String file = "MRCOLS.RRF";
      String target = "mrcols";
      String xml = getXML(dir_name, file, target);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/METAB.html", true, log);
      toHtml(xml, target + ".xsl", release.getBuildUri() + "/HTML/METAB.html", false);

      dir_name = release.getBuildUri() + File.separator + "METAO";
      file = "MRCOLS";
      target = "mrcols_orf";
      xml = getXML(dir_name, file, target);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/METAB.html", true, log);
      toHtml(xml,
             target + ".xsl",
             release.getBuildUri() + "/HTML/METAB.html", true);

      dir_name = release.getBuildUri() + File.separator + "META";
      file = "MRDOC.RRF";
      target = "mrdoc";
      xml = getXML(dir_name, file, target);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/METAB2.html", true, log);
      toHtml(xml,
             target + "-b2.xsl",
             release.getBuildUri() + "/HTML/METAB2.html", false);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/METAB3.html", true, log);
      toHtml(xml,
             target + "-b3.xsl",
             release.getBuildUri() + "/HTML/METAB3.html", false);

      file = "MRSAB.RRF";
      target = "mrsab";
      xml = getXML(dir_name, file, target);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/METAB4.html", true, log);
      toHtml(xml,
             target + ".xsl",
             release.getBuildUri() + "/HTML/METAB4.html", false);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/METAA1.html", true, log);
      toHtml(xml,
             target + "2.xsl",
             release.getBuildUri() + "/HTML/METAA1.html", false);

      file = "MRRANK.RRF";
      target = "mrrank";
      xml = getXML(dir_name, file, target);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/METAB5.html", true, log);
      toHtml(xml,
             target + ".xsl",
             release.getBuildUri() + "/HTML/METAB5.html", false);

      target = "metadata";
      xml = getXML(dir_name, file, target);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/METAB6.html", true, log);
      toHtml(xml,
             target + ".xsl",
             release.getBuildUri() + "/HTML/METAB6.html", false);
      target = "mrcoc";
      file = "MRCOC.RRF";
      xml = getXML(dir_name, file, target);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/2.7.1.3.10.txt", true, log);
      toHtml(xml,
             target + ".xsl",
             release.getBuildUri() + "/HTML/2.7.1.3.10.txt", false);

      target = "mrfiles";
      file = "MRFILES.RRF";
      xml = getXML(dir_name, file, target);
      MEMEToolkit.logCommentToBuffer("WRITING " + release.getBuildUri() +
                                     "/HTML/B.1.txt", true, log);
      toHtml(xml,
             target + ".xsl",
             release.getBuildUri() + "/HTML/B.1.txt", false);
    }
    catch (Exception e) {
      throw new XMLParseException("Failed to generate the html document", this,
                                  e);
    }
  }

  /**
   * Publishes files to "release Host/URI".
   * @return flag indicating whether or not publish succeeded
   * @throws MEMEException if failed to publish
   */

  public boolean publish() throws MEMEException {
    try {
      ArrayList target_files = new ArrayList();
      target_files.add("METAA1.html");
      target_files.add("METAB.html");
      target_files.add("METAB2.html");
      target_files.add("METAB3.html");
      target_files.add("METAB4.html");
      target_files.add("METAB5.html");
      for (Iterator iterator = target_files.iterator(); iterator.hasNext(); ) {
        String target_file = (String) iterator.next();

        ServerToolkit.exec(
            new String[] {
            "/bin/rcp",
            release.getBuildUri() + "/HTML/" + target_file,
            release.getReleaseHost() + ":" +
            release.getReleaseUri() +
            "/AppendixDocs/"});
        String local_digest = localDigest(
            release.getBuildUri() + "/HTML/" + target_file);
        ServerToolkit.logCommentToBuffer("MD5 digest " +
                                         release.getBuildUri() +
                                         "/HTML/" + target_file +
                                         " - " + local_digest, true, log);

        String remote_digest = remoteDigest(
            release.getReleaseUri() +
            "/AppendixDocs/" + target_file,
            release.getReleaseHost());
        ServerToolkit.logCommentToBuffer("MD5 digest " +
                                         release.getReleaseUri() +
                                         "/AppendixDocs/" + target_file +
                                         " - " + remote_digest, true, log);

        if (!local_digest.equals(remote_digest)) {
          throw new Exception(
              "Remote digest does not match local digest.");
        }
      }
    }
    catch (Exception e) {
      DeveloperException dev = new DeveloperException(
          "Failed to publish the target", this);
      dev.setEnclosedException(e);
      throw dev;
    }
    return true;
  }

  /**
   * Helper method to obtain XML-ized version of a release file.
   * @param dir the directory
   * @param file the file
   * @param target the target
   * @return the XML version of the specified file
   * @throws Exception if anything goes wrong
   */
  private String getXML(String dir, String file, String target) throws
      Exception {
    StringBuffer xml = new StringBuffer();
    String line;
    Hashtable sourceTypes = new Hashtable();
    try {
      if (target.equals("metadata")) {
        Statement stmt = data_source.createStatement();

        xml.append("<?xml version=\"1.0\"?>\n");
        xml.append("<");
        xml.append(target);
        xml.append("Collection>\n");

        xml.append("<release>");
        xml.append(release.getName());
        xml.append("</release>\n");
        String query =
            "SELECT count(DISTINCT cui) FROM mrd_classes WHERE expiration_date IS NULL";
        ResultSet rset = stmt.executeQuery(query);
        if (rset.next()) {
          xml.append("<concepts>");
          xml.append(rset.getString(1));
          xml.append("</concepts>\n");
        }
        query =
            "SELECT count(DISTINCT cui||aui) FROM mrd_classes WHERE expiration_date IS NULL";
        rset = stmt.executeQuery(query);
        if (rset.next()) {
          xml.append("<auis>");
          xml.append(rset.getString(1));
          xml.append("</auis>\n");
        }
        query =
            "SELECT count(DISTINCT sui) FROM mrd_classes WHERE expiration_date IS NULL";
        rset = stmt.executeQuery(query);
        if (rset.next()) {
          xml.append("<suis>");
          xml.append(rset.getString(1));
          xml.append("</suis>\n");
        }
        query =
            "SELECT count(DISTINCT lui) FROM mrd_classes WHERE expiration_date IS NULL";
        rset = stmt.executeQuery(query);
        if (rset.next()) {
          xml.append("<luis>");
          xml.append(rset.getString(1));
          xml.append("</luis>\n");
        }
        query =
            "SELECT count(distinct source_family||language) " +
            "FROM mrd_source_rank " +
            "WHERE expiration_date IS NULL AND is_current = 'Y' " +
            "  AND source = normalized_source ";
        rset = stmt.executeQuery(query);
        if (rset.next()) {
          xml.append("<sources_by_language>");
          xml.append(rset.getString(1));
          xml.append("</sources_by_language>\n");
        }
        query = " SELECT count(distinct root_source)  " +
        		"FROM mrd_classes a " +
        		"WHERE expiration_date IS NULL " +
        		"and exists (select 1 from mrd_source_rank b where a.root_source=b.root_source " +
        		" AND is_current = 'Y'  " +
        		"  AND b.source = normalized_source) ";
        rset = stmt.executeQuery(query);
        if (rset.next()) {
          xml.append("<sources>");
          xml.append(rset.getString(1));
          xml.append("</sources>\n");
        }
        query =
            "SELECT count(distinct language) " +
            "FROM mrd_source_rank " +
            "WHERE expiration_date IS NULL AND is_current = 'Y' " +
            "  AND source = normalized_source ";
        rset = stmt.executeQuery(query);
        if (rset.next()) {
          xml.append("<languages>");
          xml.append(rset.getString(1));
          xml.append("</languages>\n");
        }

        query =
            "SELECT language,count(distinct aui) FROM mrd_classes WHERE expiration_date IS NULL "
            + "GROUP BY language";
        rset = stmt.executeQuery(query);
        while (rset.next()) {
          xml.append("<name_by_language>\n");
          xml.append("<language>");
          xml.append(rset.getString(1));
          xml.append("</language>\n");
          xml.append("<count>");
          xml.append(rset.getString(2));
          xml.append("</count>\n");
          xml.append("</name_by_language>\n");
        }

        query =
            "SELECT restriction_level,count(distinct aui) " +
            "FROM mrd_classes a, mrd_source_rank b " +
            "WHERE a.expiration_date IS NULL " +
            "  AND b.expiration_date IS NULL " +
            "  AND a.root_source=b.root_source " +
            "  AND is_current = 'Y' " +
            "GROUP BY restriction_level";

        rset = stmt.executeQuery(query);
        while (rset.next()) {
          xml.append("<name_by_srl>\n");
          xml.append("<srl>");
          xml.append(rset.getString(1));
          xml.append("</srl>\n");
          xml.append("<count>");
          xml.append(rset.getString(2));
          xml.append("</count>\n");
          xml.append("</name_by_srl>\n");
        }

        query =
            "SELECT suppressible,count(distinct aui) FROM mrd_classes WHERE expiration_date IS NULL "
            + "GROUP BY suppressible";
        rset = stmt.executeQuery(query);
        while (rset.next()) {
          xml.append("<atom_by_suppress>\n");
          xml.append("<suppress>");
          xml.append(rset.getString(1));
          xml.append("</suppress>\n");
          xml.append("<count>");
          xml.append(rset.getString(2));
          xml.append("</count>\n");
          xml.append("</atom_by_suppress>\n");
        }

        query =
            "SELECT language,count(distinct source) FROM mrd_source_rank " +
            "WHERE expiration_date IS NULL AND is_current = 'Y' " +
            "  AND source = normalized_source " +
            "GROUP BY language";

        rset = stmt.executeQuery(query);
        while (rset.next()) {
          xml.append("<source_by_language>\n");
          xml.append("<source>");
          xml.append(rset.getString(1));
          xml.append("</source>\n");
          xml.append("<count>");
          xml.append(rset.getString(2));
          xml.append("</count>\n");
          xml.append("</source_by_language>\n");
        }
        xml.append("</");
        xml.append(target);
        xml.append("Collection>\n");
      }
      else if (target.equals("mrcoc")) {
        xml.append("<?xml version=\"1.0\"?>\n");
        xml.append("<");
        xml.append(target);
        xml.append("Collection>\n");

        xml.append("<release>");
        xml.append(release.getName());
        xml.append("</release>\n");
        Set newSources = new HashSet();
        BufferedReader in = new BufferedReader(new FileReader(
            new File(dir, file)));
        while ( (line = in.readLine()) != null) {
          newSources.add(line.split("\\|")[4]);
        }
        Set oldSources = new HashSet();
        dir = release.getPreviousReleaseInfo().getBuildUri() + "/META";
        in = new BufferedReader(new FileReader(
            new File(dir, file)));
        while ( (line = in.readLine()) != null) {
          oldSources.add(line.split("\\|")[4]);
        }
        in.close();
        for(Iterator iterator = newSources.iterator(); iterator.hasNext();) {
          String source = (String)iterator.next();
          if(!oldSources.contains(source)) {
            writeXML(xml,"newsource",source);
          }
        }
        for(Iterator iterator = oldSources.iterator(); iterator.hasNext();) {
          String source = (String)iterator.next();
          if(!newSources.contains(source)) {
            writeXML(xml,"oldsource",source);
          }
        }
        xml.append("</");
        xml.append(target);
        xml.append("Collection>\n");
      }
      else if (target.equals("mrfiles")) {
        xml.append("<?xml version=\"1.0\"?>\n");
        xml.append("<");
        xml.append(target);
        xml.append("Collection>\n");

        xml.append("<release>");
        xml.append(release.getName());
        xml.append("</release>\n");
        Map newFiles = new HashMap();
        BufferedReader in = new BufferedReader(new FileReader(
            new File(dir, file)));
        while ( (line = in.readLine()) != null) {
          String[] fields = line.split("\\|");
          newFiles.put(fields[0],fields[2]);
        }
        Map oldFiles = new HashMap();
        dir = release.getPreviousReleaseInfo().getBuildUri() + "/META";
        in = new BufferedReader(new FileReader(
            new File(dir, file)));
        while ( (line = in.readLine()) != null) {
          String[] fields = line.split("\\|");
          oldFiles.put(fields[0],fields[2]);
        }
        in.close();
        for(Iterator iterator = newFiles.keySet().iterator(); iterator.hasNext();) {
          String filename = (String)iterator.next();
          if(!oldFiles.containsKey(filename)) {
            writeXML(xml,"newfile",filename + "|" + (String)newFiles.get(filename));
          } else {
            String columns = (String)newFiles.get(filename);
            if(!columns.equals(oldFiles.get(filename))) {
              String oldCols = (String)oldFiles.get(filename);
              xml.append("<changefile>");
              writeXML(xml,"filename",filename);
              String[] fields = columns.split(",");
              String[] oldFields = oldCols.split(",");
              Arrays.sort(fields);
              Arrays.sort(oldFields);
              for(int i = 0; i < fields.length; i++ ) {
                if(Arrays.binarySearch(oldFields,fields[i]) < 0 ) {
                  xml.append("<newfield>");
                  writeXML(xml,"name",fields[i]);
                  file = "MRCOLS.RRF";
                  dir = release.getBuildUri() + "/META";
                  in = new BufferedReader(new FileReader(
                      new File(dir, file)));
                  while ( (line = in.readLine()) != null) {
                    String[] colfields = line.split("\\|");
                    if(fields[i].equals(colfields[0]) && filename.equals(colfields[6])) {
                      writeXML(xml,"detail",line);
                    }
                  }
                  xml.append("</newfield>");
                }
              }
              for(int i = 0; i < oldFields.length; i++ ) {
                if (Arrays.binarySearch(fields, oldFields[i]) < 0) {
                  xml.append("<oldfield>");
                  writeXML(xml, "name", fields[i]);
                  file = "MRCOLS.RRF";
                  dir = release.getPreviousReleaseInfo().getBuildUri() + "/META";
                  in = new BufferedReader(new FileReader(
                      new File(dir, file)));
                  while ( (line = in.readLine()) != null) {
                    String[] colfields = line.split("\\|");
                    if (fields[i].equals(colfields[0])  && filename.equals(colfields[6])) {
                      writeXML(xml, "detail", line);
                    }
                  }
                  xml.append("</oldfield>");
                }
              }
              xml.append("</changefile>");
            }
          }
        }
        for(Iterator iterator = oldFiles.keySet().iterator(); iterator.hasNext();) {
          String filename = (String) iterator.next();
          if (!newFiles.containsKey(filename)) {
            writeXML(xml, "oldfile", filename + "|" + (String)oldFiles.get(filename));
          }
        }
        xml.append("</");
        xml.append(target);
        xml.append("Collection>\n");
      }
      else {
        if (target.endsWith("mrsab")) {
          Source[] sources = data_source.getSourceByType(SourceType.NEW,
              release);
          for (int i = 0; i < sources.length; i++) {
            sourceTypes.put(sources[i].getSourceAbbreviation(), "new");
          }
          sources = data_source.getSourceByType(SourceType.UPDATE, release);
          for (int i = 0; i < sources.length; i++) {
            sourceTypes.put(sources[i].getSourceAbbreviation(), "updated");
          }
        }
        String mrfiles = "MRFILES";
        if (file.endsWith(".RRF")) {
          mrfiles = mrfiles + ".RRF";
        }
        BufferedReader in = new BufferedReader(new FileReader(
            new File(dir, mrfiles)));
        String list = null;
        while ( (line = in.readLine()) != null) {
          if (line.startsWith(file)) {
            list = line.split("\\|")[2];
          }
        }
        in = new BufferedReader(new FileReader(
            new File(dir, file)));
        //	Add in the XML heading
        xml.append("<?xml version=\"1.0\"?>\n");
        xml.append("<");
        xml.append(target);
        xml.append("Collection>\n");

        xml.append("<release>");
        xml.append(release.getName());
        xml.append("</release>\n");
        String[] names = list.split(",");
        while ( (line = in.readLine()) != null) {
          xml.append("<entry>\n");
          String[] cols = line.split("\\|");
          for (int i = 0; i < cols.length; i++) {
            writeXML(xml, names[i].toLowerCase(), cols[i]);
          }
          if (target.equals("mrsab")) {
            String sourceType = "";
            if (sourceTypes.containsKey(cols[2])) {
              sourceType = (String) sourceTypes.get(cols[2]);
            }
            writeXML(xml, "vsabtype", sourceType);

          }
          xml.append("</entry>\n");
        }
        xml.append("</");
        xml.append(target);
        xml.append("Collection>\n");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    return xml.toString();
  }

  /**
   * Helper method to write XML for a column name/value.
   * @param xml the document so far
   * @param colName the column name
   * @param value the value
   */
  private static void writeXML(StringBuffer xml,
                               String colName, String value) {
    StringBuffer sb = new StringBuffer();
    sb.append("    <");
    sb.append(colName);
    sb.append(">");

    sb.append(XMLEntityEncoder.encode(value));

    sb.append("</");
    sb.append(colName);
    sb.append(">\n");

    StringBuffer newbuf = new StringBuffer(600000);

    //	Look through the content and locate the references to
    //	email addresses and make link for them.

    processEmail(sb.toString(), newbuf);

    sb = newbuf;
    newbuf = new StringBuffer(600000);

    //	Look through the content and locate the references to
    //	website addresses and make link for them.
    processWebsites(sb.toString(), newbuf, "http://");

    xml.append(newbuf.toString());

  }

  /**
   * Helper method to apply styloe sheet to input XML.
   * @param xml the XML document
   * @param xslFile the stylesheet
   * @param htmlFile the HTML file
   * @param append indicates whether or not to append to file
   */
  private void toHtml(String xml, String xslFile, String htmlFile,
                      boolean append) throws Exception {
    OutputStream ostr = new ByteArrayOutputStream();
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer(
        new StreamSource(this.getClass().getResource(xslFile).openStream()));

    transformer.setOutputProperty("method", "html");

    transformer.transform(new StreamSource(new StringReader(xml)),
                          new StreamResult(ostr));
    String html = null;
    if (ostr != null) {
      html = ostr.toString();
      ostr.close();
    }
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
        htmlFile, append)));

    html = html.replaceAll("(<[hb]r)([^>]*)>", "$1$2 />");

    out.println(html);

    //	Close the input and output streams.
    out.close();

  }

  /**
   * Helper method to
   * locate the email addresses of the form 'user@place' and
   * surround that with an href tag, resulting in:
   * <a href="user@place">user@place</a>.
   * @param str the doc
   * @param newbuf the new doc
   */
  private static void processEmail(String str, StringBuffer newbuf) {
    int last = 0;
    while (true) {
      //	Find the email addresses and encase them in an "a href"
      //	The @ is what distinguishes an email address.
      int atIndex = str.indexOf('@', last);
      if (atIndex == -1) {
        //	We're done so add the rest of the string
        newbuf.append(str.substring(last, str.length()));
        break;
      }
      else {
        //	Go back from the @ symbol until you have a stop
        //	character (':', ' ', ';', '"', '<')
        int beginEmail = findStopChar(str, atIndex, -1);
        int endEmail = findStopChar(str, atIndex, 1);
        String email = str.substring(beginEmail + 1, endEmail);
        if (email.endsWith(".")) {
          email = email.substring(0, email.length() - 1);
          endEmail--;

          //	Add the section of text before the email
        }
        newbuf.append(str.substring(last, beginEmail + 1));

        //	Add the href start, email, and end href
        newbuf.append("<LINK target=\"mailto:");
        newbuf.append(email);
        newbuf.append("\">");
        newbuf.append(email);
        newbuf.append("</LINK>");

        last = endEmail;
      }
    }
  }

  /**
   * Locates the website addresses of the form 'http://' and
   *	surround that element with an href tag, resulting in:
   * 	<a href="http://www.nlm.nih.gov">http://www.nlm.nih.gov</a>.
   * @param str the document
   * @param newbuf the new document
   * @param websiteKey not sure.
   */
  private static void processWebsites(String str, StringBuffer newbuf,
                                      String websiteKey) {
    int last = 0;
    while (true) {
      //	Find the website addresses and encase them in an "a href"
      //	The @ is what distinguishes an email address.
      int atIndex = str.indexOf(websiteKey, last);
      if (atIndex == -1) {
        //	We're done so add the rest of the string
        newbuf.append(str.substring(last, str.length()));
        break;
      }
      else {
        //	Go back from the http:// until you have a stop
        //	character (':', ' ', ';')
        //	If you already have a double quote ", then
        //	this is already a properly defined href no need
        //	to look at this one
        int beginWS = findWebsiteStopChar(str, atIndex, -1);
        int endWS = findWebsiteStopChar(str, atIndex, 1);
        String ws = str.substring(beginWS + 1, endWS);
        if (ws.endsWith(".")) {
          ws = ws.substring(0, ws.length() - 1);
          endWS--;
          //	Add the section of text before the website
        }
        newbuf.append(str.substring(last, beginWS + 1));
        /*
                 if (str.substring(beginWS - 1, beginWS).equals("=")) {
          //	Just add the website reference as it's already
          //	correct.
          newbuf.append(" ");
          newbuf.append(ws);
          newbuf.append(str.substring(beginWS, beginWS + 1));
                 }
                 else {
         */
        //	Add the href start, website, and end href
        newbuf.append("<LINK>");
        newbuf.append(ws);
        newbuf.append("</LINK>");
        //}

        last = endWS;
      }
    }
  }

  /**
   * Returns the index of the character that is the stop character.
   * @param str the document
   * @param atIndex the index
   * @param offset the offset
   * @return the index of the character that is the stop character
   */
  private static int findStopChar(String str, int atIndex, int offset) {
    char[] v = str.toCharArray();
    if (offset < 1) {
      for (int i = atIndex - 1; i > 0; i--) {
        char ch = v[i];
        if (ch == ';' ||
            ch == ':' ||
            ch == '"' ||
            ch == ' ' ||
            ch == '>' ||
            ch == ',' ||
            ch == '[' ||
            ch == ']' ||
            ch == '(' ||
            ch == ')' ||
            ch == '&') {
          return i;
        }
      }

      return 0;
    }
    else {
      for (int i = atIndex; i < str.length(); i++) {
        char ch = v[i];
        if (ch == ';' ||
            ch == ':' ||
            ch == '"' ||
            ch == ' ' ||
            ch == '<' ||
            ch == '&') {
          return i;
        }
      }
      return str.length();
    }
  }

  /**
   * Returns the character just before or after the website stop
   * character, depending on the negative/positive offset value.
   * @param str the doc
   * @param atIndex the index
   * @param offset the offset
   * @return the character just before or after the website stop char
   */
  private static int findWebsiteStopChar(String str, int atIndex, int offset) {
    char[] v = str.toCharArray();
    if (offset < 1) {
      for (int i = atIndex - 1; i > 0; i--) {
        char ch = v[i];
        if (ch == ';' ||
            ch == '"' ||
            ch == '>' ||
            ch == ' ') {
          return i;
        }
      }

      return 0;
    }
    else {
      for (int i = atIndex; i < str.length(); i++) {
        char ch = v[i];
        if (ch == ';' ||
            ch == '"' ||
            ch == '<' ||
            ch == ' ') {
          return i;
        }
      }

      return str.length();
    }
  }

  /**
   * Returns the list of files.
   * @return the list of files
   * @throws MEMEException if failed to get file list
   */
  public String[] getFiles() throws MEMEException {
    return new String[] {};
  }
}
