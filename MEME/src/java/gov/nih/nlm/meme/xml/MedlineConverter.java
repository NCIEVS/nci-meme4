/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.xml
 * Object:  MedlineConverter
 *		    Converts the heading string to a id.
 *****************************************************************************/
package gov.nih.nlm.meme.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MedlineConverter {
	 private int heading_id;
	 private String subheading_qa;
	 private static String user, password, host, sid, xml;
	  private Set error_seen = new HashSet();
	 private static PrintWriter coc_heading, coc_subheading, todel;
//	 contains the names of the active tags
	  private HashSet active = new HashSet();

	  // Map of heading names to atom_ids and codes
	  private Map hid = new Hashtable();

	  // Map of QA attributes for current version MSH/TQ atoms
	  private Map qa = new Hashtable();
	  private String mode;
	  
	 public MedlineConverter(String mode) {
		    this.mode = mode;

		    try {
		      Class.forName("oracle.jdbc.driver.OracleDriver");

		      Connection readConn = DriverManager.getConnection("jdbc:oracle:thin:@" +
		          host + ":1521:" + sid,
		          user, password);
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
		          executeQuery("select code, c.atom_id, attribute_value " +
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

		      readConn.close();
		    } catch (Exception e) {
		      e.printStackTrace();
		      System.out.println("Received Exception:  " + e.getMessage());
                      
		    }
	 }
	public static void main(String[] args) {
	    try {
	      if (args.length < 6) {
	        System.err.println("Usage: MedlineHandler <host> <sid> <username> <password> <mode> <file list>");
	        return;
	      }

	      host = args[0];
	      sid = args[1];
	      user = args[2];
	      password = args[3];

	      MedlineConverter converter = new MedlineConverter( args[4]);
	      int gcCount =0;
	      for (int i = 5; i < args.length; i++) {
	          System.out.println("    processing the file" + args[i] + "    " + new Date());
	          String [] temp = null;
	          temp = args[i].split("\\.");
	          String filename = null;
	          if (temp.length > 3) {
	        	  if (args[4].equals("heading")) {
	        	   filename = "coc_headings_"+ temp[2]+ ".dat";
	        	  } else {
	        		  filename = "coc_subheadings_"+ temp[2]+ ".dat";
	        	  }
	          } else {
	        	  if (args[4].equals("heading")) {
		        	   filename = "coc_headings.dat";
	        	  }
	        	  else {
	        		  filename= "coc_subheadings.dat";
	        	  }
	          }
	          File file = null;
	          if (args[4].equals("heading")) {
	          file = new File(filename);
		      coc_heading = new PrintWriter(new BufferedWriter(
		          new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")));
	          } else {
		      file = new File(filename);
		      coc_subheading = new PrintWriter(new BufferedWriter(
		          new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")));
	          }
	          converter.process(args[i]);
		      if (coc_heading != null) {
		    	    coc_heading.flush();
			        coc_heading.close();
			  }
			  if (coc_subheading != null) {
				  coc_subheading.flush();  
				  coc_subheading.close();
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
	    	  coc_heading.flush();
	        coc_heading.close();
	      }
	      if (coc_subheading != null) {
	    	  coc_subheading.flush();
	    	  coc_subheading.close();
	      }     
	    }
	  }
	  private void process(String fileName) {
		  try {
			  BufferedReader in = new BufferedReader(new FileReader(fileName));
			  String str;
		      while ((str = in.readLine()) != null) {
		              writeStr(str,mode,fileName);
		      }
		      in.close();
		  } catch (IOException e) {
		  }  
	  }
	  private void writeStr(String str, String mode,String fileName) {
		  String[] temp = null;
		  temp = str.split("\\|");
		  heading_id = -1;
		  if (mode.equals("heading")) {
			  if (hid.containsKey(temp[2])) {
		          heading_id = ( (Integer) ( (Map) hid.get(temp[2])).get("id")).
		              intValue();
		          if (heading_id == -1) {
		        	  System.out.println("Error couldn't find: " + temp[2] + ":" + fileName );
		          }
		        } else {
		          if (error_seen.add(temp[2])) {
		            System.out.println("Error couldn't find: " + temp[2] + ":" + fileName );
		          }
		          System.out.println("Error couldn't find: " + temp[2] + ":" + fileName );
		        }
			  if (heading_id !=-1) {
			  StringBuffer heading_line = new StringBuffer();
		        heading_line.append(temp[0])
		            .append("|")
		            .append(temp[1])
		            .append("|")
		            .append(heading_id)
		            .append("|")
		            .append(temp[3])
		            .append("|")
		            .append(temp[4])
		            .append("|")
		            .append(temp[5])
		            .append("|")
		            .append("L")
		            .append("|");        
		          coc_heading.println(heading_line.toString());
		          if (coc_heading.checkError()) {
		        	  System.out.println("has errors in writing");
		          }
			  }
		  } else if (mode.equalsIgnoreCase("subheading")) {
			  if (qa.containsKey(temp[2])) {
		          subheading_qa = (String) ( (Map) qa.get(temp[2])).get("qa");
		        } else {
		          if (error_seen.add(temp[2])) {
		            System.out.println("Error couldn't find subheading_qa for " +
		                               temp[2] + ":" + fileName);
		          }
		          subheading_qa = "";
		        }
			  StringBuffer subheading_line = new StringBuffer();
	          subheading_line.append(temp[0])
		            .append("|")
		            .append(temp[1])
		            .append("|")
		            .append(subheading_qa)
		            .append("|")
		            .append(temp[3])
		            .append("|");
		        coc_subheading.println(subheading_line.toString());
		        if (coc_subheading.checkError()) {
		        	System.out.println("has errors in writing"); 
		        }
		  }
	  }
/*********************
 *  public void doit() {
      String s3 = "Real.How.To";
      // String s3 = "Real-How-To";
      String [] temp = null;
      temp = s3.split("\\.");
      // temp = s3.split("-");
      dump(temp);
      }

public void dump(String []s) {
    System.out.println("------------");
    for (int i = 0 ; i < s.length ; i++) {
        System.out.println(s[i]);
    }
    System.out.println("------------");
}
BufferedReader in = new BufferedReader(new FileReader("infilename"));
String str;
        while ((str = in.readLine()) != null) {
            process(str);
        }
        in.close();
    } catch (IOException e) {
    }

 */
	

}
