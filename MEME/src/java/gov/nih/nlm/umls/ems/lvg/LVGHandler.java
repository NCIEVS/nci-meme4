/************************************************************************
 *
 * Object:  LVGHandler
 *
 * Author: suresh@nlm.nih.gov
 *
 * History: 
 *
 * 5/21/2002: First release - Suresh
 *
 */

package gov.nih.nlm.umls.ems.lvg;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Provides a thread to transform an LVG request to a response
 */
public class LVGHandler extends Thread {
    LVGServer server;
    Socket socket;

    public LVGHandler(LVGServer s, Socket clientSocket) {
	this.server = s;
	this.socket = clientSocket;
    }

    public void run() {
	String q=null;
	Vector v;
	String enc = "UTF-8";

	try {
	    //	    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ASCII"));
	    //	    PrintWriter out = new PrintWriter(socket.getOutputStream());
	    BufferedReader utf8in = new BufferedReader(new InputStreamReader(socket.getInputStream(), enc));
	    PrintWriter utf8out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), enc)));

	    while ((q = utf8in.readLine()) != null) {
		v = server.request2response(q);
		utf8out.print(q);
		//		server.append_to_log("Query: " + q);
		for (Enumeration e=v.elements(); e.hasMoreElements(); ) {
		    String r = (String)e.nextElement();
		    utf8out.print(LVGServer.FIELD_SEPARATOR + r);
		    //		    server.append_to_log("Response: " + r);
		}
		utf8out.println();
		utf8out.flush();
	    }
	    utf8in.close();
	    utf8out.close();

	} catch (Exception e) {
	    server.append_to_log("ERROR in query: " + q + " " + e.getMessage());
	}
    }
}
