/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.xml
 * Object:  MedlineHandler
 *
 * Changes:
 *   01/20/2006 TTN (1-74OL9): bug fix for update
 *       checking the wrong number of args for update
 *   01/06/2006 TTN (1-72FM6):
 *       1. Formalize ignore_record
 *       2. Formalize ignore_heading
 *       3. Don't write subheading entries that don't match map
 *       4. don't use "pf" variable for program flow, use #1, #2
 *       5. Handle bad pub dates by reporting error and continuing instead of throwing exception
 *       6. Add (<year>).* pattern to catch many bad date cases. Interpret as 01-jan and report to user so a substitution pattern can be added if desired.
 *   12/22/2005 BAC (1-719S2): QA value lookup uses hints to speed query.
 *
 *****************************************************************************/
package gov.nih.nlm.meme.xml;

import gov.nih.nlm.util.OrderedHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX parser handler used to create intermediate data files from the
 * Medline XML files.
 *
 * @author TTN
 */
public class MedlineHandler
    extends DefaultHandler {

  //
  // Private fields
  //
  // contains the names of the active tags
  private HashSet active = new HashSet();

  // Map of heading names to atom_ids and codes
  private Map hid = new Hashtable();

  // Map of QA attributes for current version MSH/TQ atoms
  private Map qa = new Hashtable();

  private Pattern p_year_month_month, p_year_month_day, p_year_month_year_month,
      p_year, p_year_year, p_year_day, p_year_month, p_year_month_day_day,
      p_year_month_day_year_month_day,
      p_year_month_day_month_day, p_year_month_day_day_day, p_year_season,
      p_year_year_season, p_year_season_season, p_year_characters;

  private OrderedHashMap patterns = new OrderedHashMap();
  private Map macros = new HashMap();
  private Map days_in_month = new Hashtable();
  private String[] months;
  private Map seasons_to_month = new Hashtable();
  private Map month_to_abbr = new Hashtable();
  private Set error_seen = new HashSet();

  private boolean ignore_record;
  private boolean has_subheadings;
  private boolean is_medline_date;
  private boolean subheading_major_topic;
  private boolean any_subheading_major_topic;
  private boolean ignore_heading;
  private String subheading_string;
  private boolean heading_major_topic;
  private String heading_string;
  private String citation_set_id;
  private String subheading_qa;
  private String medline_date;
  private String season;
  private String day;
  private String month;
  private String year;
  private int sct;
  private int heading_id;
  private String subheading_set_id;
  private String pub_date;
  private String med;
  private String mode;
  private int start_year;
  private Date releaseDate;

  private static String user, password, host, sid, xml;
  private static PrintWriter coc_heading, coc_subheading, todel;

  // Patterns for matching days, months, years, and seasones
  private static String day_ptn = "\\d{1,2}";
  private static String month_ptn =
      "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|" +
      "january|february|march|april|june|july|august|september|october|november|december";
  private static String year_ptn = "\\d{4}";
  private static String season_ptn = "spring|summer|fall|autumn|winter";
  private static SimpleDateFormat dateformatter = new SimpleDateFormat(
      "dd-MMM-yyyy");

  //
  // Constructor
  //

  /**
   *  Instantiates a {@link MedlineHandler} using the specified
   *  start year and mode.
   *  @param year only track Medline citations after this year
   *  @param release_date only track Medline citations before this date
   *  @param mode update or baseline?
   */
  public MedlineHandler(String year, String release_date, String mode) {
    start_year = Integer.parseInt(year);
    this.mode = mode;

    // Map seasons to months
    seasons_to_month.put("spring", "mar");
    seasons_to_month.put("summer", "jun");
    seasons_to_month.put("fall", "sep");
    seasons_to_month.put("autumn", "sep");
    seasons_to_month.put("winter", "dec");

    // Map numbers to month names
    months = new String[13];
    months[1] = "jan";
    months[2] = "feb";
    months[3] = "mar";
    months[4] = "apr";
    months[5] = "may";
    months[6] = "jun";
    months[7] = "jul";
    months[8] = "aug";
    months[9] = "sep";
    months[10] = "oct";
    months[11] = "nov";
    months[12] = "dec";

    // Map month names to maximum number of days
    days_in_month.put("jan", new Integer(31));
    days_in_month.put("feb", new Integer(28));
    days_in_month.put("mar", new Integer(31));
    days_in_month.put("apr", new Integer(30));
    days_in_month.put("may", new Integer(31));
    days_in_month.put("jun", new Integer(30));
    days_in_month.put("jul", new Integer(31));
    days_in_month.put("aug", new Integer(31));
    days_in_month.put("sep", new Integer(30));
    days_in_month.put("oct", new Integer(31));
    days_in_month.put("nov", new Integer(30));
    days_in_month.put("dec", new Integer(31));

    month_to_abbr.put("january", "jan");
    month_to_abbr.put("february", "feb");
    month_to_abbr.put("march", "mar");
    month_to_abbr.put("april", "apr");
    month_to_abbr.put("june", "jun");
    month_to_abbr.put("july", "jul");
    month_to_abbr.put("august", "aug");
    month_to_abbr.put("september", "sep");
    month_to_abbr.put("october", "oct");
    month_to_abbr.put("november", "nov");
    month_to_abbr.put("december", "dec");

    macros.put("<year>", "\\" + year_ptn);
    macros.put("<month>", month_ptn);
    macros.put("<day>", "\\" + day_ptn);
    macros.put("<season>", season_ptn);

    p_year_month_month = Pattern.compile("^(" + year_ptn + ") +(" +
                                         month_ptn +
                                         ")-(" + month_ptn + ")",
                                         Pattern.CASE_INSENSITIVE);

    p_year_month_day = Pattern.compile("^(" + year_ptn + ") +(" +
                                       month_ptn + ") +(" +
                                       day_ptn + ")",
                                       Pattern.CASE_INSENSITIVE);
    p_year_month_year_month = Pattern.compile("^(" + year_ptn + ") +(" +
                                              month_ptn + ")-" +
                                              year_ptn + " +(" + month_ptn +
                                              ")",
                                              Pattern.CASE_INSENSITIVE);

    p_year = Pattern.compile("^(" + year_ptn + ")",
                             Pattern.CASE_INSENSITIVE);
    p_year_year = Pattern.compile("^(" + year_ptn + ")-" + year_ptn,
                                  Pattern.CASE_INSENSITIVE);
    p_year_day = Pattern.compile("^(" + year_ptn + ")-" + day_ptn,
                                 Pattern.CASE_INSENSITIVE);
    p_year_month = Pattern.compile("^(" + year_ptn + ") +(" + month_ptn +
                                   ")",
                                   Pattern.CASE_INSENSITIVE);
    p_year_month_day_day = Pattern.compile("^(" + year_ptn + ") +(" +
                                           month_ptn + ") +(" +
                                           day_ptn + ")-(" + day_ptn + ")",
                                           Pattern.CASE_INSENSITIVE);
    p_year_month_day_year_month_day = Pattern.compile("^(" + year_ptn +
        ") +(" + month_ptn + ") +(" +
        day_ptn + ")-(" + year_ptn + ") +(" + month_ptn +
        ") +(" + day_ptn + ")", Pattern.CASE_INSENSITIVE);
    p_year_month_day_month_day = Pattern.compile("^(" + year_ptn +
                                                 ") +(" + month_ptn + ") +(" +
                                                 day_ptn + ")-(" + month_ptn +
                                                 ") " + day_ptn,
                                                 Pattern.CASE_INSENSITIVE);
    p_year_month_day_day_day = Pattern.compile("^(" + year_ptn + ") +(" +
                                               month_ptn + ") +(" +
                                               day_ptn + ")-(" + day_ptn + ") " +
                                               day_ptn,
                                               Pattern.CASE_INSENSITIVE);
    p_year_season = Pattern.compile("^(" + year_ptn + ") +(" +
                                    season_ptn + ")",
                                    Pattern.CASE_INSENSITIVE);
    p_year_year_season = Pattern.compile("^(" + year_ptn + ")-(" +
                                         year_ptn + ") +(" +
                                         season_ptn + ")",
                                         Pattern.CASE_INSENSITIVE);
    p_year_season_season = Pattern.compile("^(" + year_ptn + ") +(" +
                                           season_ptn + ")-(" +
                                           season_ptn + ")",
                                           Pattern.CASE_INSENSITIVE);
    p_year_characters = Pattern.compile("^(" + year_ptn + ").*",
                                        Pattern.CASE_INSENSITIVE);

    try {
      releaseDate = new SimpleDateFormat(
          "MM/dd/yyyy").parse(release_date);

      Class.forName("oracle.jdbc.driver.OracleDriver");

      Connection readConn = DriverManager.getConnection("jdbc:oracle:thin:@" +
          host + ":1521:" + sid,
          user, password);

      med = "NLM-MED";
      ResultSet r1 = readConn.createStatement().
      executeQuery("select /*+ RULE */ a.atom_id, a.code, c.atom_name " +
          "from classes a, classes b, atoms c, source_version d " +
          "where b.atom_id = c.atom_id " +
          "and d.source = 'MSH' " +
          "and a.source = d.current_name " +
          "and b.source like 'MSH%' " +
          "and b.source != d.current_name " +
          "and a.tty in ('MH','TQ') " +
          "and b.tty in ('MH','TQ') " +
          "and a.sui != b.sui " +
          "and a.code = b.code");
      while (r1.next()) {
        Map ht = new Hashtable();
        ht.put("id", new Integer(r1.getInt("atom_id")));
        ht.put("code", r1.getString("code"));
        hid.put(r1.getString("atom_name"), ht);
      }
      r1 = readConn.createStatement().
          executeQuery("select a.atom_id,code, atom_name " +
                       "from atoms a, classes b, source_version c " +
                       "where a.atom_id=b.atom_id " +
                       "and c.source='MSH' " +
                       "and c.current_name=b.source " +
                       "and (termgroup like 'MSH%/MH' OR " +
                       "termgroup like 'MSH%/TQ')");
      while (r1.next()) {
        Map ht = new Hashtable();
        ht.put("id", new Integer(r1.getInt("atom_id")));
        ht.put("code", r1.getString("code"));
        hid.put(r1.getString("atom_name"), ht);
      }
      r1 = readConn.createStatement().
          executeQuery("select atom_name, a.atom_id, attribute_value " +
                       "from atoms a, classes b, attributes c, source_version d " +
                       "where a.atom_id=b.atom_id and b.atom_id=c.atom_id " +
                       "and d.source='MSH' " +
                       "and current_name = b.source " +
                       "and b.termgroup like 'MSH%/TQ' " +
                       "and attribute_name='QA' ");
      while (r1.next()) {
        Map ht = new Hashtable();
        ht.put("id", new Integer(r1.getInt("atom_id")));
        ht.put("qa", r1.getString("attribute_value"));
        qa.put(r1.getString("atom_name"), ht);
      }
      r1 = readConn.createStatement().
          executeQuery("select /*+ INDEX(c,x_attr_an) INDEX(b,classes_pk) */ code, c.atom_id, attribute_value " +
                       "from classes c, attributes a, source_version b " +
                       "where a.atom_id=c.atom_id " +
                       "and b.source='MSH' " +
                       "and current_name = a.source " +
                       "and attribute_name='YE' ");
      while (r1.next()) {
        Map ht = new Hashtable();
        ht.put("id", new Integer(r1.getInt("atom_id")));
        ht.put("code", r1.getString("code"));
        qa.put(r1.getString("attribute_value"), ht);
      }
      r1 = readConn.createStatement().
          executeQuery("select key, value " +
                       "from meme_properties " +
                       "where key_qualifier = 'MEDLINE' " +
                       "order by TO_NUMBER(example) ");
      while (r1.next()) {
        String key = r1.getString("key");
        Iterator iterator = macros.keySet().iterator();
        while (iterator.hasNext()) {
          String macro = (String) iterator.next();
          key = key.replaceAll(macro,
                               (String) macros.get(macro));
        }
        Pattern p = Pattern.compile(key, Pattern.CASE_INSENSITIVE); ;
        patterns.put(p, r1.getString("value"));
      }
      readConn.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  //
  // SAX Api
  //

  /**
   * Handle the start of the document.  In practice do nothing.
   */
  public void startDocument() {}

  /**
   * @param namespaceURI the namespace URI
   * @param sName the simple name
   * @param qName the qualified name
   * @param attrs an object {@link Attributes}
   * @throws SAXException if failed to parse object
   */
  public void startElement(String namespaceURI,
                           String sName, // simple name (localName)
                           String qName, // qualified name
                           Attributes attrs) throws SAXException {

    String element = sName; // element name
    if ("".equals(element)) {
      element = qName; // namespaceAware = false

      // Mark element as active in a hash.
    }
    active.add(element);

    // A new citation is encountered.  Reset any flags
    // that start anew for each citation.
    if (element.equals("MedlineCitation")) {
      //determine if record is complete
      if ("MEDLINE".equals(attrs.getValue("Status"))
          || "OLDMEDLINE".equals(attrs.getValue("Status"))
          || "Completed".equals(attrs.getValue("Status"))) {
        ignore_record = false;
      }
      else {
        ignore_record = true;
      }
      // Reset subheading_set_id counter
      sct = 0;

      // Reset citation set id
      citation_set_id = "";

      // Reset publication date
      pub_date = "";
    }

    //
    // A DeletCitation is encountered.  Reset ignore_record
    // and citation_set_id flags
    //
    else if (element.equals("DeleteCitation")) {
      ignore_record = false;
      citation_set_id = "";
    }

    else if (ignore_record) {
      return;
    }

    //
    // MeshHeadingList is encountered, we only want to parse the date if the
    // record has MeshHeading
    // Put the pieces together and construct $pub_date
    //
    //
    else if (element.equals("MeshHeadingList")) {
      try {
        //
        // Parse various formats for medline dates
        //
        if (is_medline_date) {
          Iterator iterator = patterns.orderedKeySet().iterator();
          while (iterator.hasNext()) {
            Pattern p = (Pattern) iterator.next();
            Matcher m = p.matcher(medline_date);
            medline_date = m.replaceAll( (String) patterns.get(p));
          }
          /*
                     // dealing with invalid dates
                     if(medline_date.indexOf(',') != -1) {
            //System.err.println("Bad date 1 in record "+citation_set_id + ": "+medline_date);
            medline_date = medline_date.replace(',',' ');
                     }
                     Pattern p = Pattern.compile("^("+year_ptn+") +("+day_ptn+") +("+month_ptn+")",Pattern.CASE_INSENSITIVE);
                     Matcher m = p.matcher(medline_date);
                     if(m.matches()) {
            //System.err.println("Bad date 2 in record "+citation_set_id + ": "+medline_date);
            year = m.group(1);
            month = m.group(3);
            day = m.group(2);
                     }
               p = Pattern.compile("2001 Aug1 5", Pattern.CASE_INSENSITIVE);
                     m = p.matcher(medline_date);
                     if (m.matches()) {
            //System.err.println("Bad date 3 in record "+citation_set_id + ": "+medline_date);
            year = "2001";
            month = "Aug";
            day = "15";
                     }
                     p = Pattern.compile("^("+year_ptn+") +("+month_ptn+")("+day_ptn+")",Pattern.CASE_INSENSITIVE);
                     m = p.matcher(medline_date);
                     if(m.matches()) {
            //System.err.println("Bad date 4 in record "+citation_set_id + ": "+medline_date);
            year = m.group(1);
            month = m.group(2);
            day = m.group(3);
                     }
                     p = Pattern.compile("^("+year_ptn+") +.+("+month_ptn+") .*",Pattern.CASE_INSENSITIVE);
                     m = p.matcher(medline_date);
                     if(m.matches()) {
            //System.err.println("Bad date 5 in record "+citation_set_id + ": "+medline_date);
            year = m.group(1);
            month = m.group(2);
                     }
                     p = Pattern.compile("^("+year_ptn+") +.+("+season_ptn+") .*",Pattern.CASE_INSENSITIVE);
                     m = p.matcher(medline_date);
                     if(m.matches()) {
            //System.err.println("Bad date 6 in record "+citation_set_id + ": "+medline_date);
            year = m.group(1);
            month = (String)seasons_to_month.get(m.group(2).toLowerCase());
                     }
                     p = Pattern.compile("^("+year_ptn+")-("+day_ptn+") +("+season_ptn+")",Pattern.CASE_INSENSITIVE);
                     m = p.matcher(medline_date);
                     if(m.matches()) {
            //System.err.println("Bad date 7 in record "+citation_set_id + ": "+medline_date);
            year = m.group(1);
            month = (String)seasons_to_month.get(m.group(3).toLowerCase());
            day = "21";
                     }
           */

          Matcher m = p_year_month_month.matcher(medline_date);
          if (m.matches()) {
            year = m.group(1);
            month = m.group(2);
            day = "1";
          }
          else {
            m = p_year_month_day.matcher(medline_date);
            if (m.matches()) {
              year = m.group(1);
              month = m.group(2);
              day = m.group(3);
            }
            else {
              m = p_year_month_year_month.matcher(medline_date);
              if (m.matches()) {
                year = m.group(1);
                month = m.group(2);
                day = "1";
              }
              else {
                m = p_year.matcher(medline_date);
                if (m.matches()) {
                  year = m.group(1);
                }
                else {
                  m = p_year_year.matcher(medline_date);
                  if (m.matches()) {
                    year = m.group(1);
                  }
                  else {
                    m = p_year_day.matcher(medline_date);
                    if (m.matches()) {
                      year = m.group(1);
                    }
                    else {
                      m = p_year_month.matcher(medline_date);
                      if (m.matches()) {
                        year = m.group(1);
                        month = m.group(2);
                      }
                      else {
                        m = p_year_month_day_day.matcher(medline_date);
                        if (m.matches()) {
                          year = m.group(1);
                          month = m.group(2);
                          day = m.group(3);
                        }
                        else {
                          m = p_year_month_day_year_month_day.matcher(
                              medline_date);
                          if (m.matches()) {
                            year = m.group(1);
                            month = m.group(2);
                            day = m.group(3);
                          }
                          else {
                            m = p_year_month_day_month_day.matcher(medline_date);
                            if (m.matches()) {
                              year = m.group(1);
                              month = m.group(2);
                              day = m.group(3);
                            }
                            else {
                              m = p_year_month_day_day_day.matcher(medline_date);
                              if (m.matches()) {
                                year = m.group(1);
                                month = m.group(2);
                                day = m.group(3);
                              }
                              else {
                                m = p_year_season.matcher(medline_date);
                                if (m.matches()) {
                                  year = m.group(1);
                                  month = (String) seasons_to_month.get(m.group(
                                      2).
                                      toLowerCase());
                                  day = "21";
                                }
                                else {
                                  m = p_year_year_season.matcher(medline_date);
                                  if (m.matches()) {
                                    year = m.group(1);
                                    month = (String) seasons_to_month.get(m.
                                        group(
                                            3).
                                        toLowerCase());
                                    day = "21";
                                  }
                                  else {
                                    m = p_year_season_season.matcher(
                                        medline_date);
                                    if (m.matches()) {
                                      year = m.group(1);
                                      month = (String) seasons_to_month.get(m.
                                          group(
                                              2).
                                          toLowerCase());
                                      day = "21";
                                    }
                                    else {
                                      m = p_year_characters.matcher(
                                          medline_date);
                                      if (m.matches()) {
                                        year = m.group(1);
                                        System.out.println(
                                            "Bad Medline Date Pattern, Parse year and use 01-Jan : " +
                                            citation_set_id + " = " +
                                            medline_date);
                                      }
                                      else {
                                        System.out.println(
                                            "Invalid Medline Date Pattern : " +
                                            citation_set_id + " = " +
                                            medline_date);
                                        ignore_record = true;
                                        return;
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
          if (month_to_abbr.containsKey(month.toLowerCase())) {
            month = (String) month_to_abbr.get(month.toLowerCase());
          }
        }

        try {
          int mm = Integer.parseInt(month);
          month = months[mm];
        }
        catch (Exception e) {}
        //
        // Convert numbered months to names
        //
        //String mm = months[month];

        //
        // There is some bad data. The file  medline01n0005.xml
        // contains record 70235294 which has a publication date
        // of 31-nov-1966.  This is clearly illegal, so here
        // we correct it back to 30-nov-1966
        //
        int i_day = Integer.parseInt(day);
        int i_year = Integer.parseInt(year);
        if (i_day >
            ( (Integer) days_in_month.get(month.toLowerCase())).intValue()) {
          // feb 29 on leap years is OK
          // leap year is divisible by 4
          // and either divisible by 400 or not divisible by 100
          // e.g. 1900 is NOT a leap year, but 2000 is
          if (! (i_day == 29 &&
                 month.equalsIgnoreCase("feb") && (i_year % 4 == 0) &&
                 ( (i_year % 100 != 0) || (i_year % 400 == 0)))) {
            //System.err.println("Bad date in record "+citation_set_id + ": "+day+"-"+month+"-"+year);
            i_day = ( (Integer) days_in_month.get(month.toLowerCase())).
                intValue();
          }
        }

        //
        // There is other bad data.  The record 11634676 contains
        // a publication date with the year 2968.  This is clearly
        // illegal, so we correct it back by 1000 years.
        //
        if (i_year > 2900) {
          i_year -= 1000;
        }

        //
        // Construct pub_date
        //
        day = "00" + i_day;
        day = day.substring(day.length() - 2);

        pub_date = day + "-" + month + "-" + i_year;

        //
        // Indicate whether or not this record should be
        // processed based on the publication date
        //
        if (i_year < start_year) {
          System.out.println(
              "Publication Date is before specified year : " +
              citation_set_id + " = " +
              pub_date);
          ignore_record = true;
        }
        if (releaseDate.before(dateformatter.parse(pub_date))) {
          System.out.println(
              "Publication Date is later than specified release date : " +
              citation_set_id + " = " +
              pub_date);
          ignore_record = true;
        }
      }
      catch (Exception e) {
        throw new SAXException("Error parsing citation_set_id: " +
                               citation_set_id
                               , e);
      }
    }

    //
    // Subheading start element
    // Set the major topic and subheading flags
    //
    else if (element.equals("QualifierName")) {
      String major_topic = attrs.getValue("MajorTopicYN");
      if (major_topic == null || major_topic.equals("N")) {
        subheading_major_topic = false;
      }
      else if (major_topic.equals("Y")) {
        subheading_major_topic = true;
      }
      if (subheading_major_topic) {
        any_subheading_major_topic = true;
      }
      subheading_string = "";
      has_subheadings = true;
    }

    //
    // Descriptor start element:
    // Set the major topic flag, reset heading/subheading strings
    //
    else if (element.equals("DescriptorName")) {
      String h_major_topic = attrs.getValue("MajorTopicYN");
      if (h_major_topic == null || h_major_topic.equals("N")) {
        heading_major_topic = false;
      }
      else if (h_major_topic.equals("Y")) {
        heading_major_topic = true;
      }
      heading_string = "";
      subheading_string = "";

      // Reset ignore heading
      ignore_heading = false;
    }

    //
    // MeshHeading start element
    // If publication date is not set, error
    //
    else if (element.equals("MeshHeading")) {
      if (citation_set_id.equals("")) {
        System.out.println("Record without PMID!");
        ignore_record = true;
      }

      if (pub_date == "") {
        System.out.println("Missing PubDate in record " + citation_set_id);
        ignore_record = true;
      }
    }

    //
    // MedlineDate start element:
    // reset medline_date
    //
    else if (element.equals("MedlineDate")) {
      medline_date = "";
    }

    //
    // PubDate start element
    // Initialize $day,$month and $year
    //
    else if (element.equals("PubDate")) {
      day = "1";
      month = "Jan";
      year = "";
      is_medline_date = false;
    }

    //
    // Month start element within a PubDate element
    // Reset month
    //
    else if (active.contains("PubDate") && element.equals("Month")) {
      month = "";
    }

    //
    // Day start element within a PubDate element
    // Reset day
    //
    else if (active.contains("PubDate") && element.equals("Day")) {
      day = "";
    }

    //
    // Year start element within a PubDate element
    // Reset year
    //
    else if (active.contains("PubDate") && element.equals("Year")) {
      year = "";
    }

    //
    // Season start element within a PubDate element
    // Reset $season
    //
    else if (active.contains("PubDate") && element.equals("Season")) {
      season = "";
    }

  }

  // Gathers the character data in a StringBuffer corresponding to the enclosing
  // tag. The enclosing tag is identified with the help of the HashSet active.
  public void characters(char[] chars, int start, int length) {
    //
    // ignore the event if the record is in progress
    //
    if (ignore_record) {
      return;
    }

    String s = new String(chars, start, length);
    //
    // PMID data being sent, append to $citation_set_id
    //
    if (active.contains("PMID") && !active.contains("CommentsCorrections")) {
      citation_set_id = citation_set_id.concat(s);
    }

    //
    // Descriptor data (the string) is being sent, append to $heading_string
    //
    else if (active.contains("DescriptorName")) {
      heading_string = heading_string.concat(s);
    }

    //
    // SubHeading data (the string) is being sent, append to $subheading_string
    //
    else if (active.contains("QualifierName")) {
      subheading_string = subheading_string.concat(s);
    }

    //
    // We are within a PubDate tag, receiving date info
    //
    else if (active.contains("PubDate")) {
      //
      // Year info is begin sent, append to $year
      //
      if (active.contains("Year")) {
        year = year.concat(s);
      }

      //
      // Month info is begin sent, append to $month
      //
      else if (active.contains("Month")) {
        month = month.concat(s);
      }

      //
      // Day info is being sent, append to $day
      //
      else if (active.contains("Day")) {
        day = day.concat(s);
      }

      //
      // Season info is being sent, append to $season
      // When the end season tag is reached, convert
      // the data into a month
      //
      else if (active.contains("Season")) {
        season = season.concat(s);
      }

      //
      // MedlineDate info being sent, set $medline_date
      //
      else if (active.contains("MedlineDate")) {
        medline_date = medline_date.concat(s);
      }
    }
  }

  // Here the elements stored in the table are added to the corresponding enclosing
  // element.
  public void endElement(String namespaceURI,
                         String sName, // simple name
                         String qName // qualified name
      ) throws SAXException {

    String element = sName; // element name
    if ("".equals(element)) {
      element = qName; // namespaceAware = false
      //
      // ignore the event
      // And unmark element as active if the record is in progress
      //
    }
    if (ignore_record) {
      active.remove(element);
      return;
    }
    try {
      //
      // MeshHeading end element
      // Here is where we write to the coc_headings.dat file.
      // All elements for the citation have been seen so all
      // necessary information about subheadings exists.  This
      // will write a single line for each MeshHeading element
      //
      if (element.equals("MeshHeading")) {

        //
        // Print the Descriptor line at the end of the Mesh Heading
        // because then we know whether or not there were subheadings
        //
        if (!has_subheadings) {
          subheading_set_id = "";
          // decrement subheading set counter
          sct--;
        }

        // Keep D or Q depending on code.
        //$heading_code =~ s/^(.).*/$1/;
       String mt = "N";
        if (heading_major_topic || any_subheading_major_topic) {
          mt = "Y";
        }
        StringBuffer heading_line = new StringBuffer();
        heading_line.append(citation_set_id)
            .append("|")
            .append(pub_date)
            .append("|")
            .append(heading_id)
            .append("|")
            .append(mt)
            .append("|")
            .append(subheading_set_id)
            .append("|")
            .append(med)
            .append("|")
            .append("L")
            .append("|");


        if (!ignore_heading) {
          coc_heading.println(heading_line.toString());

          //
          // At end of MeshHeading clear the major topic
          // and has_subheadings flags.
          //
        }
        any_subheading_major_topic = false;
        has_subheadings = false;
      }

      //
      // Descriptor end element
      // Here we map the descriptor string to an id and a code
      // and we increment the subheading set id counter.
      //
      else if (element.equals("DescriptorName")) {
        if (hid.containsKey(heading_string)) {
          heading_id = ( (Integer) ( (Map) hid.get(heading_string)).get("id")).
              intValue();
        }
        else {
          if (error_seen.add(heading_string)) {
            System.out.println("Error couldn't find: " + heading_string);
          }
          ignore_heading = true;
        }

        // use counter for subheading_set_ids
        subheading_set_id = String.valueOf(sct++);
      }

      //
      // SubHeading end element
      // Here we map the subheading string to a QA attribute
      // and actually write out a coc_subheadings.dat entry.
      // We have already encountered enough tags to have set
      // up the $citation_set_id and $subheading_set_id
      //
      else if (element.equals("QualifierName")) {
        if (qa.containsKey(subheading_string)) {
          subheading_qa = (String) ( (Map) qa.get(subheading_string)).get("qa");
          StringBuffer subheading_line = new StringBuffer();
          subheading_line.append(citation_set_id)
              .append("|")
              .append(subheading_set_id)
              .append("|")
              .append(subheading_qa)
              .append("|")
              .append( (subheading_major_topic ? "Y" : "N"))
              .append("|");
          if(!ignore_heading)
            coc_subheading.println(subheading_line.toString());
        }
        else {
          if (error_seen.add(subheading_string)) {
            System.out.println("Error couldn't find subheading_qa for " +
                               subheading_string);
          }
          subheading_qa = "";
        }
      }

      //
      // MedlineDate end element
      // Indicate that we have processed a MedlineDate
      //
      else if (element.equals("MedlineDate")) {
        is_medline_date = true;
      }

      //
      // Season end element within a PubDate element
      // Convert $season to a month/day
      //
      else if (active.contains("PubDate") && element.equals("Season")) {
        month = (String) seasons_to_month.get(season.toLowerCase());
        day = "21";
      }

      //
      // PMID end element within DeleteCitation element
      // Reset the citation_set_id for each citation
      //
      else if (mode.equals("update") &&
               active.contains("DeleteCitation") && element.equals("PMID")) {
        todel.println(citation_set_id + "|");
        citation_set_id = "";
      }

      //
      // MedlineCitation end element
      //
      else if (mode.equals("update") && element.equals("MedlineCitation")) {
        todel.println(citation_set_id + "|");
      }

      //
      // Unmark element as active
      //
      active.remove(element);
    }
    catch (Exception e) {
      throw new SAXException("Error parsing citation_set_id: " +
                             citation_set_id
                             , e);
    }
  }

  public static void main(String[] args) {
    try {
      if (args.length < 8) {
        System.err.println("Usage: MedlineHandler <host> <sid> <username> <password> <year> <release_date> <mode> <file list>");
        return;
      }

      host = args[0];
      sid = args[1];
      user = args[2];
      password = args[3];
      DefaultHandler handler = new MedlineHandler(args[4], args[5], args[6]);

      File file = new File("coc_headings.dat");
      coc_heading = new PrintWriter(new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")));
      file = new File("coc_subheadings.dat");
      coc_subheading = new PrintWriter(new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")));
      if (args[6].equals("update")) {
        file = new File("coc_headings_todelete.dat");
        todel = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")));
      }

      // Use the default (non-validating) parser
      SAXParserFactory factory = SAXParserFactory.newInstance();

      // Parse the input
      SAXParser saxParser = factory.newSAXParser();
      for (int i = 7; i < args.length; i++) {
        System.out.println("    processing " + args[i] + "    " + new Date());
        xml = args[i];
        saxParser.parse(new File(args[i]), handler);
      }
    }
    catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
    finally {
      if (coc_heading != null) {
        coc_heading.close();
      }
      if (coc_subheading != null) {
        coc_subheading.close();
      }
      if (todel != null && args[6].equals("update")) {
        todel.close();
      }
    }
  }
}
