/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server.handlers
 * Object:  QAReportParser
 *
 *****************************************************************************/
package gov.nih.nlm.mrd.server.handlers;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.XMLParseException;
import gov.nih.nlm.mrd.common.QAComparison;
import gov.nih.nlm.mrd.common.QAComparisonReason;
import gov.nih.nlm.mrd.common.QAReason;
import gov.nih.nlm.mrd.common.QAReport;
import gov.nih.nlm.mrd.common.QAResult;
import gov.nih.nlm.mrd.common.QAResultReason;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.common.SourceType;
import gov.nih.nlm.mrd.server.ServerToolkit;
import gov.nih.nlm.mrd.sql.MRDDataSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser for QA report document (for example, "qa_MRCONSO.xml").
 *
 * @author TTN
 */
public class QAReportParser {

  private static final String dtd_version = "1.5";
  private static final int MAX_COUNT = 100;
  private QAReport qaReport;
  // used to load the dtd and reference the document to it.

  /**
   * SAX Parser Handler.
   */
  private class QAReportHandler extends DefaultHandler {

    // contains the names of the active tags
    private HashSet active = new HashSet();
    private String check, warning, compareTo;
    private String target, release_name, previous, previousMajor;
    private ArrayList differences, notInOther, missing;
    // for characters
    private StringBuffer buffer = new StringBuffer();
    private StringBuffer error_buffer = new StringBuffer();
    private MRDDataSource data_source;
    private HashMap macro_sources_map = new HashMap();
    private HashMap sources_by_type = new HashMap();
    private HashMap qareasons_map = new HashMap();
    private int error_count = 0;
    private QAResultReason[] qaresultreasons;
    private QAComparisonReason[] qacomparereasons;

    /**
     * Handle start elements.
     * @param namespaceURI the namespace URI
     * @param sName the simple tag name
     * @param qName the qualified tag name
     * @param attributes the attributes
     * @throws SAXException if anything goes wrong
     */
    public void startElement(String namespaceURI, String sName,
                             String qName,  Attributes attributes) throws SAXException {
      String element = sName;

      if ("".equals(element)) {
        element = qName; // namespaceAware = false
        // mark element as active, used by characters to determines where the character
        // data belongs, will be deactivated by the endElement method of this element
      }
      active.add(element);
      try {
        if (element.equals("QAReport")) {
          target = attributes.getValue("target");
          release_name = attributes.getValue("release");
          previous = attributes.getValue("previous");
          previousMajor = attributes.getValue("previousMajor");
          data_source = ServerToolkit.getMRDDataSource(attributes.getValue(
              "database"), null, null);
          ReleaseInfo release_info = data_source.getReleaseInfo(release_name);
          Source[] sources = data_source.getSourceByType(SourceType.UPDATE,
              release_info);
          sources_by_type.put(new Integer(SourceType.UPDATE), sources);
          if (sources.length > 0) {
            macro_sources_map.put("<update>", sources);
          }
          sources = data_source.getSourceByType(SourceType.NEW, release_info);
          sources_by_type.put(new Integer(SourceType.NEW), sources);
          if (sources.length > 0) {
            macro_sources_map.put("<new>", sources);
          }
          sources = data_source.getSourceByType(SourceType.OBSOLETE,
                                                release_info);
          sources_by_type.put(new Integer(SourceType.OBSOLETE), sources);
          if (sources.length > 0) {
            macro_sources_map.put("<obsolete>", sources);
          }
          qaReport = new QAReport();
          qaReport.setName(target);
          qaReport.setReleaseName(release_name);
          qaReport.setPreviousMajorReleaseName(previousMajor);
          qaReport.setPreviousReleaseName(previous);
        } else if (element.equals("Check")) {
          check = attributes.getValue("name");
          warning = null;
          error_buffer = new StringBuffer();
          error_count = 0;
        } else if (element.equals("CompareTo")) {
          compareTo = attributes.getValue("name");
          differences = new ArrayList();
          notInOther = new ArrayList();
          missing = new ArrayList();
        } else if (element.equals("Error")) {
          error_buffer.append(attributes.getValue("name")).append("\n");
        } else if (element.equals("Warning")) {
          warning = attributes.getValue("value");
        } else if (element.equals("QAComparison")) {
          QAComparison qa = new QAComparison();
          qa.setName(attributes.getValue("name"));
          qa.setValue(attributes.getValue("value"));
          qa.setCount(Long.parseLong(attributes.getValue("count")));
          qa.setComparisonCount(Long.parseLong(attributes.getValue(
              "comparisoncount")));
          ArrayList reasons = new ArrayList();
          for (int i = 0; i < qacomparereasons.length; i++) {
            if (qacomparereasons[i].appliesTo(qa, macro_sources_map)) {
              MEMEToolkit.trace("reason applies to " +
                                qacomparereasons[i].getReason() + ":" +
                                qa.getName() + "|" + qa.getValue());
              reasons.add(qacomparereasons[i]);
              qareasons_map.remove(qacomparereasons[i]);
            }
          }
          qa.setReasons( (QAComparisonReason[]) reasons.toArray(new
              QAComparisonReason[] {}));
           differences.add(qa);
        } else if (element.equals("QAResult")) {
          QAResult qa = new QAResult();
          qa.setName(attributes.getValue("name"));
          qa.setValue(attributes.getValue("value"));
          qa.setCount(Long.parseLong(attributes.getValue("count")));
          ArrayList reasons = new ArrayList();
          for (int i = 0; i < qaresultreasons.length; i++) {
            if (qaresultreasons[i].appliesTo(qa, macro_sources_map)) {
              reasons.add(qaresultreasons[i]);
              qareasons_map.remove(qaresultreasons[i]);
            }
          }
          qa.setReasons( (QAResultReason[]) reasons.toArray(new
              QAResultReason[] {}));
          if (active.contains("NotInOther")) {
            notInOther.add(qa);
          } else if (active.contains("Missing")) {
            missing.add(qa);
          }
        } else if (element.equals("Value")) {
          buffer = new StringBuffer();
        } else if (element.equals("Differences") || element.equals("NotInOther") ||
                   element.equals("Missing")) {
          String[] comparison_name = new String[] {
              compareTo};
          if (compareTo.equals("Gold")) {
            comparison_name = new String[] {
                compareTo, release_name + "_gold"};
          } else if (compareTo.equals("Previous")) {
            comparison_name = new String[] {
                compareTo, previous};
          } else if (compareTo.equals("PreviousMajor")) {
            comparison_name = new String[] {
                compareTo, "Previous", previousMajor};
          }
          if (element.equals("Differences")) {
            qacomparereasons = data_source.getQAComparisonReasons(new String[] {
                release_name, "Current"}
                , comparison_name, target);
            for (int i = 0; i < qacomparereasons.length; i++) {
              qareasons_map.put(qacomparereasons[i],
                                qacomparereasons[i].getReason());
            }
          } else if (element.equals("NotInOther")) {
            qaresultreasons = data_source.getQAResultReasons(new String[] {
                release_name, "Current"}
                , comparison_name, target);
            for (int i = 0; i < qaresultreasons.length; i++) {
              qareasons_map.put(qaresultreasons[i],
                                qaresultreasons[i].getReason());
            }
          } else if (element.equals("Missing")) {
            qaresultreasons = data_source.getQAResultReasons(comparison_name,
                new String[] {release_name, "Current"}
                , target);
            for (int i = 0; i < qaresultreasons.length; i++) {
              qareasons_map.put(qaresultreasons[i],
                                qaresultreasons[i].getReason());
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        throw new SAXException(
            "Problem parsing QAReport. error message: " + e.getMessage());
      }

    }

    /**
     * Gathers the character data in a {@link StringBuffer} corresponding to the enclosing
     * tag. The enclosing tag is identified with the help of the {@link HashSet} active.
     * @param chars the characters
     * @param start the starting index
     * @param length the length
     */
    public void characters(char[] chars, int start, int length) {
      if (active.contains("Value")) {
        buffer.append(chars, start, length);
      }
    }

    /**
     * Handles the end element.
     * @param namespaceURI the namespace URI
     * @param sName the simple tag name
     * @param qName the qualified tag name
     * @throws SAXException if anything goes wrong
     */
    public void endElement(String namespaceURI, String sName,
                           String qName ) throws SAXException {
      String element = sName; // element name
      if ("".equals(element)) {
        element = qName; // namespaceAware = false
      }
      try {
        if (element.equals("Check")) {
          qaReport.setCheck(check, error_buffer.toString(), warning);
        } else if (element.equals("Value")) {
          if (error_count++ < MAX_COUNT) {
            error_buffer.append(buffer.toString()).append("\n");
          }
        } else if (element.equals("CompareTo")) {
          if (compareTo.equals("Gold")) {
            qaReport.setGoldQAComparisons( (QAComparison[]) differences.toArray(new
                QAComparison[] {}));
            if (!notInOther.isEmpty()) {
              qaReport.setMetaNotGold( (QAResult[]) notInOther.toArray(new
                  QAResult[] {}));
            }
            if (!missing.isEmpty()) {
              qaReport.setGoldNotMeta( (QAResult[]) missing.toArray(new
                  QAResult[] {}));
            }
          } else if (compareTo.equals("Previous")) {
        	for(Iterator iterator = differences.iterator(); iterator.hasNext();) {
        		QAComparison qa = (QAComparison)iterator.next();
                qa.setShouldHaveReason(data_source.shouldBeExplainedBySRCQA(target, qa));
              if(data_source.isExplainedBySRCQA(target, qa)) {
            	  qa.addReason(data_source.getSRCQAReason(target, qa));
              }
        	}
            qaReport.setMinorPreviousQAComparisons( (QAComparison[])
                differences.
                toArray(new
                        QAComparison[] {}));
            if (!notInOther.isEmpty()) {
            	for(Iterator iterator = notInOther.iterator(); iterator.hasNext();) {
            		QAResult qa = (QAResult)iterator.next();
                    qa.setShouldHaveReason(data_source.shouldBeExplainedBySRCQA(target, qa));
                    if(data_source.isExplainedBySRCQA(target, release_name, previous, qa)) {
                  	  qa.addReason(data_source.getSRCQAReason(target, release_name, previous, qa));
                    }
            	}
              qaReport.setMetaNotMinorPrevious( (QAResult[]) notInOther.toArray(new
                  QAResult[] {}));
            }
            if (!missing.isEmpty()) {
            	for(Iterator iterator = missing.iterator(); iterator.hasNext();) {
            		QAResult qa = (QAResult)iterator.next();
                    qa.setShouldHaveReason(data_source.shouldBeExplainedBySRCQA(target, qa));
                    if(data_source.isExplainedBySRCQA(target, previous, release_name,  qa)) {
                  	  qa.addReason(data_source.getSRCQAReason(target, previous, release_name, qa));
                    }
            	}
              qaReport.setMinorPreviousNotMeta( (QAResult[]) missing.toArray(new
                  QAResult[] {}));
            }
          } else if (compareTo.equals("PreviousMajor")) {
          	for(Iterator iterator = differences.iterator(); iterator.hasNext();) {
        		QAComparison qa = (QAComparison)iterator.next();
                qa.setShouldHaveReason(data_source.shouldBeExplainedBySRCQA(target, qa));
              if(data_source.isExplainedBySRCQA(target, qa)) {
            	  qa.addReason(data_source.getSRCQAReason(target, qa));
              }
        	}
            qaReport.setMajorPreviousQAComparisons( (QAComparison[])
                differences.
                toArray(new
                        QAComparison[] {}));
            if (!notInOther.isEmpty()) {
            	for(Iterator iterator = notInOther.iterator(); iterator.hasNext();) {
            		QAResult qa = (QAResult)iterator.next();
                    qa.setShouldHaveReason(data_source.shouldBeExplainedBySRCQA(target, qa));
                    if(data_source.isExplainedBySRCQA(target, release_name, previous, qa)) {
                  	  qa.addReason(data_source.getSRCQAReason(target, release_name, previous, qa));
                    }
            	}
              qaReport.setMetaNotMajorPrevious( (QAResult[]) notInOther.toArray(new
                  QAResult[] {}));
            }
            if (!missing.isEmpty()) {
            	for(Iterator iterator = missing.iterator(); iterator.hasNext();) {
            		QAResult qa = (QAResult)iterator.next();
                    qa.setShouldHaveReason(data_source.shouldBeExplainedBySRCQA(target, qa));
                    if(data_source.isExplainedBySRCQA(target, previous, release_name,  qa)) {
                  	  qa.addReason(data_source.getSRCQAReason(target, previous, release_name, qa));
                    }
            	}
              qaReport.setMajorPreviousNotMeta( (QAResult[]) missing.toArray(new
                  QAResult[] {}));
            }
          }
        } else if (element.equals("QAReport")) {
          QAReason[] reasons = new QAReason[qareasons_map.size()];
          int i = 0;
          for (Iterator iter = qareasons_map.keySet().iterator(); iter.hasNext(); ) {
            reasons[i++] = (QAReason) iter.next();
          }
          qaReport.setUnusedReasons(reasons);
          if (data_source != null) {
            try {
              ServerToolkit.returnDataSource(data_source);
            } catch (BadValueException bve) {
              ServerToolkit.handleError(bve);
            }
          }
        }
        active.remove(element);
      } catch (Exception e) {
    	  e.printStackTrace();
        throw new SAXException(
            "Problem parsing QAReport. error message: " + e.getMessage());
      }
    }
  }

  /**
   *  Parses the specified file into a {@link QAReport}.
   *  If the document is valid with respect to QAReport.dtd,
   *  the method constructs an {@link QAReport} object which reflects the information
   *  of the document.
   *  @param dtd_version the DTD Version
   *  @param file the file to parse
   *  @return the {@link QAReport} contained in the file
   * @throws ExternalResourceException if failed to handle the event
   * @throws XMLParseException if failed to handle the event
   */
  public QAReport parse(String dtd_version, String file) throws
      XMLParseException, ExternalResourceException {
    if (! (dtd_version.equals(QAReportParser.dtd_version))) {
      return null;
    }

    try {
      //data_source.prepareDTD(dtd_name, dtd_version);
      //InputSource in = new InputSource(new StringReader(document));
      //in.setSystemId(systemId);

      QAReportHandler event_handler = new QAReportHandler();

      // Get a "parser factory", an an object that creates parsers
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

      // Set up the factory to create the appropriate type of parser
      saxParserFactory.setValidating(true);
      saxParserFactory.setNamespaceAware(false);

      SAXParser parser = saxParserFactory.newSAXParser();
      parser.parse(new File(file), event_handler);
      return qaReport;
    } catch (IOException e) {
      ExternalResourceException ere = new ExternalResourceException(e.
          getMessage(), e);
      ere.setDetail("file", file);
      throw ere;
    } catch (SAXException ex) {
      XMLParseException xmle = new XMLParseException(ex.getMessage(), null, ex);
      xmle.setDetail("file", file);
      throw xmle;
    } catch (ParserConfigurationException px) {
      XMLParseException xmle = new XMLParseException(px.getMessage(), null, px);
      xmle.setDetail("file", file);
      throw xmle;
    }
  }

  /**
   * Testing method.
   * @param args command line args
   */
  public static void main(String[] args) {
    try {
      QAReportParser parser = new QAReportParser();
      QAReport report = parser.parse("1.5", "qa_MRHIST.xml");
      if (report != null) {
        String[] checks = report.getChecks();
        for (int i = 0; i < checks.length; i++) {
          System.out.println(checks[i]);
          if (report.getErrorForCheck(checks[i]) != null) {
            System.out.println(report.getErrorForCheck(checks[i]));
          }
          if (report.getWarningForCheck(checks[i]) != null) {
            System.out.println(report.getWarningForCheck(checks[i]));
          }
        }
        QAComparison[] qa = report.getGoldQAComparisons();
        System.out.println("getGoldQAComparisons");
        for (int i = 0; i < qa.length; i++) {
          System.out.println(qa[i].getName() + "|" + qa[i].getValue() + "|" +
                             qa[i].getCount() + "|" +
                             +qa[i].getComparisonCount());
        }
        QAResult[] qa_result = report.getGoldNotMeta();
        System.out.println("GoldNotMeta");
        for (int i = 0; i < qa_result.length; i++) {
          System.out.println(qa_result[i].getName() + "|" +
                             qa_result[i].getValue() +
                             "|" + qa_result[i].getCount());
        }
        qa_result = report.getMetaNotGold();
        System.out.println("MetaNotGold");
        for (int i = 0; i < qa_result.length; i++) {
          System.out.println(qa_result[i].getName() + "|" +
                             qa_result[i].getValue() +
                             "|" + qa_result[i].getCount());
        }
        qa = report.getMinorPreviousQAComparisons();
        System.out.println("getMinorPreviousQAComparisons");
        for (int i = 0; i < qa.length; i++) {
          System.out.println(qa[i].getName() + "|" + qa[i].getValue() + "|" +
                             qa[i].getCount() + "|" +
                             +qa[i].getComparisonCount());
        }
        qa_result = report.getMinorPreviousNotMeta();
        System.out.println("getMinorPreviousNotMeta");
        for (int i = 0; i < qa_result.length; i++) {
          System.out.println(qa_result[i].getName() + "|" +
                             qa_result[i].getValue() +
                             "|" + qa_result[i].getCount());
        }
        qa_result = report.getMetaNotMinorPrevious();
        System.out.println("getMetaNotMinorPrevious");
        for (int i = 0; i < qa_result.length; i++) {
          System.out.println(qa_result[i].getName() + "|" +
                             qa_result[i].getValue() +
                             "|" + qa_result[i].getCount());
        }
        qa = report.getMajorPreviousQAComparisons();
        System.out.println("getMajorPreviousQAComparisons");
        for (int i = 0; i < qa.length; i++) {
          System.out.println(qa[i].getName() + "|" + qa[i].getValue() + "|" +
                             qa[i].getCount() + "|" +
                             +qa[i].getComparisonCount());
        }
        qa_result = report.getMajorPreviousNotMeta();
        System.out.println("getMajorPreviousNotMeta");
        for (int i = 0; i < qa_result.length; i++) {
          System.out.println(qa_result[i].getName() + "|" +
                             qa_result[i].getValue() +
                             "|" + qa_result[i].getCount());
        }
        qa_result = report.getMetaNotMajorPrevious();
        System.out.println("getMetaNotMajorPrevious");
        for (int i = 0; i < qa_result.length; i++) {
          System.out.println(qa_result[i].getName() + "|" +
                             qa_result[i].getValue() +
                             "|" + qa_result[i].getCount());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
