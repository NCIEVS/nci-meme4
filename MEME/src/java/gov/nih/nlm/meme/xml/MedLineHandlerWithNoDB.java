/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.xml
 * Object:  MedlineHandler
 *
 *****************************************************************************/
package gov.nih.nlm.meme.xml;

import gov.nih.nlm.util.OrderedHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
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
public class MedLineHandlerWithNoDB
    extends DefaultHandler {

  //
  // Private fields
  //
  // contains the names of the active tags
  private HashSet active = new HashSet();

  // Map of heading names to atom_ids and codes
  //private Map hid = new Hashtable();

  // Map of QA attributes for current version MSH/TQ atoms
  //private Map qa = new Hashtable();

  private Pattern p_year_month_month, p_year_month_day, p_year_month_year_month,
      p_year, p_year_year, p_year_day, p_year_month, p_year_month_day_day,
      p_year_month_day_year_month_day,
      p_year_month_day_month_day, p_year_month_day_day_day, p_year_season,
      p_year_year_season, p_year_season_season,
  	  general_pattern;

  private OrderedHashMap patterns = new OrderedHashMap();
  private Map macros = new HashMap();
  private Map days_in_month = new Hashtable();
  private String[] months;
  private Map seasons_to_month = new Hashtable();
  private Map month_to_abbr = new Hashtable();
  //private Set error_seen = new HashSet();
  private Map dbPatterns = new Hashtable();
  private boolean ignore_record;
  private boolean has_subheadings;
  private boolean is_medline_date;
  private boolean subheading_major_topic;
  private boolean any_subheading_major_topic;
  private boolean pf;
  private String subheading_string;
  private boolean heading_major_topic;
  private String heading_string;
  private String citation_set_id;
  //private String subheading_qa;
  private String medline_date;
  private String season;
  private String day;
  private String month;
  private String year;
  private int sct;
  //private String heading_id;
  private String subheading_set_id;
  private String pub_date;
  private String med;
  private String mode;
  private int start_year;
  private Date now;

  private static String  xml;
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
  public MedLineHandlerWithNoDB(String year, String release_date, String mode) {
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
    /************************************************************************
     * MODIFIED BY SOMA LANKA
     ***********************************************************************/
     dbPatterns.put("([^,]*),([^,]*)","$1 $2");
    /* dbPatterns.put("2001 Aug1 5","2001 Aug 15");
     dbPatterns.put("^(<year>) +(<day>) +(<month>)","$1 $3 $2");
     dbPatterns.put("^(<year>) +(<month>)(<day>)","$1 $2 $3");
     dbPatterns.put("^(<year>)-(<day>) +(<season>)","$1 $3");
     dbPatterns.put("^(<year>) (<season>)-(<year>) +(<season>)","$1 $2");
      //1950 Jul-Sept 
      
      //1950 Sep-Oct 1
    dbPatterns.put("^(<year>) +(<month>)- +(<month>)","$1 $2-$3");
    dbPatterns.put("^(<year>) +(<month>)-+(<month>) +(<day>)","$1 $2-$3");
    dbPatterns.put("^(<year>)-(<year>) +(<month>)","$1 $3");
    dbPatterns.put("^(<year>) +(<month>) +(<day>)-(<month>)(<day>)","$1 $2 $3");
    dbPatterns.put("^(<year>) +(<month>) +(<day>)-(<day>)-(<month>) +(<day>)","$1 $2 $3");
    dbPatterns.put("^(<year>) +(<month>)-(Sept)","$1 $2-Sep");
    dbPatterns.put("^(<year>) (1st Quarter)","$1 spring");
    dbPatterns.put("^(<year>) (4th Quarter)","$1 winter");
    dbPatterns.put("^(<year>) +(<month>) +(Suppl)","$1 $2");
    dbPatterns.put("^(<year>) (Session)","$1");
    dbPatterns.put("^(<year>) +(<month>) +-(<month>) +(<year>)","$1 $2");
    dbPatterns.put("^(<year>) (3d Trimest)","$1");
    dbPatterns.put("^(<year>) (2d Trimest)","$1");
    dbPatterns.put("^(<year>) +(<month>) +(<day>)-(<month>) +(<day>)-(<day>)","$1 $2 $3");
    dbPatterns.put("^(<year>) +(<month>) +-(<month>)","$1 $2");
      
      // New Date Format 1949 Sept 15-30
    dbPatterns.put("^(<year>) +(Sept) +(<day>)-(<day>)","$1 Sep $3-$4");
    //  {citation_set_id=13413374, file_name=medline06n0023.xml, medline_date=1956 Jul 1957}
    dbPatterns.put("^(<year>) +(<month>) +(<year>)", "$1 $2");
      
    // (FATAL) Invalid Medline Date Pattern
    // {citation_set_id=14439129, file_name=medline06n0034.xml, medline_date=1959 Passim Dec
    dbPatterns.put("^(<year) (Passim) +(<month>)","$1 $2");
    */
     dbPatterns.put("^(<year>) +(<month>)- +(<month>)","$1 $2-$3");
     dbPatterns.put("^(<year>) +(<month>) +-(<month>)","$1 $2");
    dbPatterns.put("^(<year>) +(<day>) +(<month>)","$1 $3 $2");
    dbPatterns.put("^(<year>)-(<day>) +(<season>)","$1 $3");
    dbPatterns.put("^(<year>) (<season>)[- ]+(<year>) (<season>)*","$1 $2");
    dbPatterns.put("^(<year>) +(<month>)(<day>)","$1 $2 $3");
    dbPatterns.put("^(<year>) +(<month>)[- ]+(<month>) +(<year>)","$1 $2");
    dbPatterns.put("^(<year>) +(<month>)-(<month>) +(<day>)","$1 $2");
    dbPatterns.put("^(<year>)-(<year>) +(<month>)","$1 $3");
    dbPatterns.put("^(<year>) +(<month>)[- ]+(<day>)[- ]+(<month>) *(<day>)*","$1 $2 $3");
    dbPatterns.put("^(<year>) +(<month>) +(<day>)-(<day>)[- ]+((<year>) +)*(<month>) +(<day>)","$1 $2 $3");
    dbPatterns.put("^(<year>) +(<month>) +Suppl","$1 $2");
    dbPatterns.put("^(<year>) +(<month>) +(<day>)-(<month>) +(<day>)-(<day>)","$1 $2 $3");
    dbPatterns.put("^(<year>) +(<month>) +(<year>) +(<day>) +p +(<day>)","$1 $2");
    dbPatterns.put("^(<year>) (1st|First)(-2nd)* Quart(er)*","$1 Jan");
    dbPatterns.put("^(<year>) (2|2d|2nd|)(-3rd)* Quart(er)*","$1 Apr");
    dbPatterns.put("^(<year>) (3d|3rd|Third)(-4th)* Quart(er)*","$1 Jul");
    dbPatterns.put("^(<year>) (4th|4d) Quart(er)*","$1 Oct");
    dbPatterns.put("^(<year>) +(<month>)[- ]+((<year>) +)*(<month>)","$1 $2");
    dbPatterns.put("^(<year>)-(<day>) +(<month>)(-(<month>))*","$1 $3");
    dbPatterns.put("^(<year>)-(<year>) +(<season>)-(<season>)","$1 $3");
    dbPatterns.put("^(<year>) +(<month>)-(<day>)","$1 $2 $3");
    dbPatterns.put("^(<year>)(<month>) +(<day>)(-(<day>))*","$1 $2 $3");
    dbPatterns.put("^(<year>) +CHRISTMAS","$1 December 1");
    dbPatterns.put("^(<year>) +EASTER","$1 April 1");
    dbPatterns.put("^(<year>) +SUM$","$1 summer");
    dbPatterns.put("^(<year>) +SPR$","$1 spring");
    dbPatterns.put("^(<year>) +(<month>) +(<year>)-(<month>)","$1 $2");
    dbPatterns.put("^(<year>) +(<season>) +(<year>)([- ]+(<day>))*","$1 $2");
    
      
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
    general_pattern=Pattern.compile("^(" + year_ptn + ")(.*)", Pattern.CASE_INSENSITIVE);
    try {
      now = new SimpleDateFormat(
          "MM/dd/yyyy").parse(release_date);
      med = "NLM-MED";
      Iterator patternIterator = dbPatterns.keySet().iterator();
      while(patternIterator.hasNext()) {
   	   String key = (String)patternIterator.next();
   	   String value = (String)dbPatterns.get(key);
   	   //Iterator macrosIterator = macros.keySet().iterator();
	   	Iterator iterator = macros.keySet().iterator();
	    while (iterator.hasNext()) {
	      String macro = (String) iterator.next();
	      key = key.replaceAll(macro,
	                           (String) macros.get(macro));
	    }
	    Pattern p = Pattern.compile(key, Pattern.CASE_INSENSITIVE); ;
        patterns.put(p, value);
      }
    } catch (Exception e) {
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
    //
    // MeshHeadingList is encountered, we only want to parse the date if the
    // record has MeshHeading
    // Put the pieces together and construct $pub_date
    //
    //
    else if (element.equals("MeshHeadingList")) {
      try {
    	  if (ignore_record == false ) {
	        //
	        // Parse various formats for medline dates
	        //
	        if (is_medline_date) {
	        medline_date = medline_date.trim();
	        //System.out.println("Medline Date is [" + medline_date + "]");
	          Iterator iterator = patterns.orderedKeySet().iterator();
	          while (iterator.hasNext()) {
	            Pattern p = (Pattern) iterator.next();
	            Matcher m = p.matcher(medline_date);
		       // System.out.println("2Medline Date is [" + medline_date + "] Pattern is [" + p.pattern() + "]");
	            medline_date = m.replaceAll( (String) patterns.get(p));
		     // System.out.println("2Medline Date is [" + medline_date + "]");
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
		        //System.out.println("1Medline Date is [" + medline_date + "]");
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
	                      m = p_year_year.matcher(medline_date);
	                      if (m.matches()) {
	                        year = m.group(1);
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
	                                    	m = general_pattern.matcher(medline_date);
	                                    	if (m.matches()) {
	                                    		year = m.group(1);
	                                    		day = "1";
	                                    		month = "Jan";
	                                    		System.err.println("Adding Invalid Date Pattern. Medline Date:[" + medline_date +
			                                    		"] citation set id: [" + citation_set_id + "] file name [" + xml +"]");
	                                    	} else {
		                                    pf = false;
		                                    System.err.println("Invalid Date Pattern. Medline Date:[" + medline_date +
		                                    		"] citation set id: [" + citation_set_id + "] file name [" + xml +"]");
		                                    return;
	                                    	}
		                                    
	                                    	/*BadValueException bve = new
		                                          BadValueException(
		                                              "Invalid Medline Date Pattern");
		                                      bve.setDetail("medline_date",
		                                          medline_date);
		                                      bve.setDetail("citation_set_id",
		                                          citation_set_id);
		                                      bve.setDetail("file_name", xml);
		                                      throw bve;
		                                      */
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
	        catch (Exception e) {
	        }
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
	        try {
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
	        pf = (i_year >= start_year ? true : false);
	        if (now.before(dateformatter.parse(pub_date))) {
	          System.out.println("publication date ["+ pub_date +"] is greater than release date [" + now +"] for "+ citation_set_id);
	          pf = false;
	        }
	        } catch (Exception e) {
	        	System.out.println("Error parsing citation_set_id: " +
	                                    citation_set_id);
	        	 e.printStackTrace();
	             throw new SAXException("Error parsing citation_set_id: " +
	                                    citation_set_id
	                                    , e);
	        }    
        } else {
      	  pf = false;
        }
    	  
      }
      catch (Exception e) {
    	  e.printStackTrace();
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
    }

    //
    // MeshHeading start element
    // If publication date is not set, error
    //
    else if (element.equals("MeshHeading")) {
      if (!ignore_record) {
        if (citation_set_id.equals("")) {
          System.out.println("Record without PMID!");
        }

        if (pub_date == "") {
          System.out.println("Missing PubDate in record " + citation_set_id);
        }
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
            .append(heading_string)
            .append("|")
            .append(mt)
            .append("|")
            .append(subheading_set_id)
            .append("|")
            .append(med)
            .append("|")
            .append("L")
            .append("|");

        if (pf) {
          coc_heading.println(heading_line.toString());
          if (coc_heading.checkError()) {
        	  System.out.println("has error in writing heading");
          }
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
        
        StringBuffer subheading_line = new StringBuffer();
        subheading_line.append(citation_set_id)
            .append("|")
            .append(subheading_set_id)
            .append("|")
            .append(subheading_string)
            .append("|")
            .append( (subheading_major_topic ? "Y" : "N"))
            .append("|");
        if (pf) {
          coc_subheading.println(subheading_line.toString());
          if (coc_subheading.checkError()) {
        	  System.out.println("has error in writing subheading");
          }
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
        if (todel.checkError()) {
      	  System.out.println("has error in writing delete files");
        }
      }

      //
      // MedlineCitation end element
      //
      else if (mode.equals("update") && element.equals("MedlineCitation")) {
        todel.println(citation_set_id + "|");
        if (todel.checkError()) {
        	  System.out.println("has error in writing delete files");
          }
      }

      //
      // Unmark element as active
      //
      active.remove(element);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new SAXException("Error parsing citation_set_id: " +
                             citation_set_id
                             , e);
    }
  }

  public static void main(String[] args) {
    try {
      if (args.length < 4) {
        System.err.println("Usage: MedlineHandler <year> <release_date> <mode> <file list>");
        return;
      }
      DefaultHandler handler = new MedLineHandlerWithNoDB(args[0], args[1],args[2]);
      if(!args[2].equals("update")) {
 	     File file = new File("coc_headings_baseline.txt");
 	     coc_heading = new PrintWriter(new BufferedWriter(
 	         new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")));
 	     file = new File("coc_subheadings_baseline.txt");
 	     coc_subheading = new PrintWriter(new BufferedWriter(
 	         new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")));
 	     if (args[2].equals("update")) {
 	       file = new File("coc_headings_todelete.dat");
 	       todel = new PrintWriter(new BufferedWriter(
 	           new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")));
 	     }
      }
      // Use the default (non-validating) parser
      SAXParserFactory factory = SAXParserFactory.newInstance();

      // Parse the input
      int gcCount = 0;
      SAXParser saxParser = factory.newSAXParser();
      for (int i = 4; i < args.length; i++) {
        System.out.println("    processing " + args[i] + "    " + new Date());
        if (args[2].equals("update")) {
     	   File file = new File("coc_headings.dat." + args[i]);
   	     coc_heading = new PrintWriter(new BufferedWriter(
   	         new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")));
   	     file = new File("coc_subheadings.dat."+ args[i]);
   	     coc_subheading = new PrintWriter(new BufferedWriter(
   	         new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")));
   	     if (args[2].equals("update")) {
   	    	String [] temp = null;
 	        temp = args[i].split("\\.");
   	       file = new File("coc_headings_todelete." + temp[0] + ".dat");
   	       todel = new PrintWriter(new BufferedWriter(
   	           new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")));
   	     } 
        }
        xml = args[i];
        saxParser.parse(new File(args[i]), handler);
        if(args[2].equals("update")) {
     	   if (coc_heading != null) {
     	       coc_heading.close();
     	     }
     	     if (coc_subheading != null) {
     	       coc_subheading.close();
     	     }
     	     if (todel != null) {
     	       todel.close();
     	     }	   
        }
        gcCount++;
        if (gcCount > 10) {
        	System.out.println("Free Memory - " + Runtime.getRuntime().freeMemory() + " Total memory - " + Runtime.getRuntime().totalMemory());
        	System.gc();
        	gcCount =0;
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    } finally {
      if (coc_heading != null) {
        coc_heading.close();
      }
      if (coc_subheading != null) {
        coc_subheading.close();
      }
      if (todel != null && args[2].equals("update")) {
        todel.close();
      }
    }  
      
  }
}
