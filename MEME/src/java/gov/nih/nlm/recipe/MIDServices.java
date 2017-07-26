/*****************************************************************************
 *
 * Package:    com.lexical.meme.core
 * Object:     MIDServices.java
 * 
 * Author:     Brian Carlsen
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;


/**
 * This class contains useful static used to access
 * Suresh's midsvcs socket server
 *
 * @author Brian A. Carlsen
 * @version 1.0
 *
 */
public class MIDServices {

  //
  // Public Methods
  //  

 /**
   * Returns a String [] containing all supported MID services
   */
  public static String [] getServicesList() throws Exception {
    ArrayList results = new ArrayList();
    String line;
    Socket sock = new Socket(
    		RxToolkit.getProperty("MIDSVCS_HOST"),
    		Integer.valueOf(RxToolkit.getProperty("MIDSVCS_PORT")).intValue());
    BufferedReader bin = new BufferedReader (
	     new InputStreamReader( sock.getInputStream() ));
    while ((line = bin.readLine()) != null) {
      results.add(line.substring(0,line.indexOf("|")));
    }
    return (String [])results.toArray(new String [] {""});
  
  }; // end getServicesList

 /**
   * Returns a String [] containing all supported DB services
   */
  public static String [] getDBServicesList() throws Exception {
    String all_services[] = new String[100];
    ArrayList db_services = new ArrayList();
    all_services = getServicesList();
    for(int i=0; i < all_services.length; i++) {
      if( all_services[i].endsWith("jdbc")) {
	db_services.add(all_services[i].substring(0, all_services[i].length() -5));
      }
    }
    db_services.add("lti8_meme3");
    db_services.add("meme3");

    // this should be removed
    db_services.add("testsw");
    return (String [])db_services.toArray(new String [] {""});
  }; // end getDBServicesList

  /**
   * Returns the value matching the service name
   */
  public static String getService( String service) throws Exception {
    String result = "";
    String line;
    if(service.equals("meme3-jdbc")) {
      return "jdbc:oracle:thin:@lti11:1521:meme3";
    } else if(service.equals("lti8_meme3-jdbc")) {
      return "jdbc:oracle:thin:@lti8:1521:cxtdb";
    } else if(service.equals("lti8_meme3-tns")) {
      return "lti8_meme3";
    } else if(service.equals("meme3-tns")) {
      return "meme3";
    // needs to be removed
    } else if(service.equals("testsw-jdbc")) {
      return "jdbc:oracle:thin:@oc.nlm.nih.gov:1521:testsw";
    } else if(service.equals("testsw-tns")) {
      return "oc_testsw";
    }
    Socket sock = new Socket(
    		RxToolkit.getProperty("MIDSVCS_HOST"),
    		Integer.valueOf(RxToolkit.getProperty("MIDSVCS_PORT")).intValue());
    BufferedReader bin = new BufferedReader (
	     new InputStreamReader( sock.getInputStream() ));
    while ((line = bin.readLine()) != null) {
      if (line.indexOf(service) == 0) {
	result =line.substring(line.indexOf("|")+1);
	break;
      }
    }
    //MEMEToolkit.trace("MIDServices.getService returns " + result);
    return result;

  }; // end getService  

  /**
   * Test the application
   */
  public static void main (String [] s) {
    System.out.println ("-------------------------------------------------------");
    System.out.println ("Starting test of MIDServices ..."+new java.util.Date());
    System.out.println ("-------------------------------------------------------");
    System.out.println ("Look up all services (getServicesList)");
    try {
      String [] svcs = getServicesList();
      for (int i = 0; i < svcs.length; i++) {
	System.out.println(svcs[i] + " = " +
			   getService(svcs[i]));
      }
    } catch (Exception e) {
      System.err.println("Exception caught (" +
			 e.getClass() + "): " +
			 e.getMessage());
    }
    System.out.println("");
    System.out.println("Look up DB Services (getDBServicesList)");
    try {
      String [] dbsvcs = getDBServicesList();
      for (int i = 0; i < dbsvcs.length; i++) {
	System.out.println(dbsvcs[i] + " = " +
			   getService(dbsvcs[i]));
      }
    } catch (Exception e) {
      System.err.println("Exception caught (" +
			 e.getClass() + "): " +
			 e.getMessage());
    }
    System.out.println ("-------------------------------------------------------");
    System.out.println ("Finished test of MIDServices ..."+new java.util.Date());
    System.out.println ("-------------------------------------------------------");

  }; // end main

}
