/************************************************************************
 *
 * Object:  LVGServer
 *
 * Author: suresh@nlm.nih.gov
 *
 * History: 
 *
 * 11/2004
 * with lvg2005, no need for Oracle; it uses HSqlDb
 *
 * 8/2002
 *
 * The port for the server is obtained from MIDservices:
 * lvgport, unless overridden with properties.
 * 
 * Command line properties:
 * lvg-dir= required! path to the LVG directory
 * lvg-server-port= (default is the value of this MID service)
 */

package gov.nih.nlm.umls.ems.lvg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Vector;

public class LVGServer extends Thread {
    public static final String VERSION="2004/11/17 12:45pm";
    public static final String LOGDIR="/tmp";
    public static final String FIELD_SEPARATOR="|";

    public static final String LUINORMFIELD = "L" + FIELD_SEPARATOR;
    public static final String NORMFIELD = "N" + FIELD_SEPARATOR;
    public static final String WORDINDFIELD = "W" + FIELD_SEPARATOR;

    private int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private String logFileName;
    private String lvgDir;

    private LvgApiIF apiIF;
    private boolean readyToQuit = false;

    public LVGServer() {
	init();
    }

    public LVGServer(String l, String dir) {
	this.logFileName = l;
	this.lvgDir = dir;
	init();
    }

    public LVGServer(int p, String l, String dir) {
	this.port = p;
	this.logFileName = l;
	this.lvgDir = dir;
	init();
    }

    private void init() {
	append_to_log("Starting LVG server on port: " + port + ", version: \"" + VERSION + "\"");
	apiIF = new LvgApiIF(this, lvgDir);
    }

    public void run() {

	try {
	    serverSocket = new ServerSocket(port);
	    while (!readyToQuit) {
		clientSocket = serverSocket.accept();
		LVGHandler handler = new LVGHandler(this, clientSocket);
		handler.start();
	    }
	} catch (BindException e) {
	    // port already in use
	    append_to_log("ERROR: port already in use: " + e.getMessage());
	    System.exit(0);
	} catch (IOException e) {
	    System.out.println("IOException: " + " Port: " + port + " already in use: " + e);
	    append_to_log("IO Error: " + e.getMessage());
	} finally {
	    try {
		if (serverSocket != null) {
		    serverSocket.close();
		}
	    } catch (IOException e1) {
		append_to_log("IO Error closing socket: " + e1.getMessage());
	    }
	}
	quit();
    }

    protected void finalize() throws Throwable {
	try {
	    quit();
	} catch (Exception e) {
	}
    }

    public void quit() {
	readyToQuit = true;

	apiIF.cleanup();
	apiIF = null;
	try {
	    serverSocket.close();
	} catch (Exception e) {
	    append_to_log("IO Error closing socket: " + e.getMessage());
	}
	append_to_log("exiting");
	System.exit(0);
    }

    // logs a string to the log file
    public synchronized void append_to_log(String msg) {
	StringBuffer b = new StringBuffer("LVGServer: " + msg + " on: " + (new Date()).toString());
	try {
	    PrintWriter p = new PrintWriter(new FileWriter(logFileName, true));
	    p.println(b.toString());
	    p.flush();
	    p.close();
	} catch (Exception e) {
	}
    }

    // returns the API interface object
    public LvgApiIF getApiIF() {
	return apiIF;
    }

    public Vector request2response(String q) {
	Vector v;
	if (q.startsWith(LUINORMFIELD)) {
	    v = apiIF.luinorm(q.substring(2));
	} else if (q.startsWith(NORMFIELD)) {
	    v = apiIF.norm(q.substring(2));
	} else if (q.startsWith(WORDINDFIELD)) {
	    v = apiIF.wordind(q.substring(2));
	} else {
	    v = apiIF.luinorm(q);
	}
	return v;
    }

    public static void main(String[] args) {
	String logfile;
	int port;
	String s;
	String lvgdir = null;

        if ((s = System.getProperty("lvg-dir")) != null) {
	    lvgdir = s;
	} else {
	    System.err.println("The lvg-dir property must be specified.");
	    System.exit(2);
	}

	// get port
	port = -1;
	try {
	    if ((s = System.getProperty("lvg-server-port")) != null) {
		port = Integer.parseInt(s);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (port == -1) {
	    System.err.println("LVGServer: ERROR: Cannot find a port to run on");
	}

	// log to /tmp/LVGServer.log
	logfile = "/tmp/LVGServer.log";
	try {
	    File f = new File(logfile);
	    f.createNewFile();
	} catch (Exception e) {
	    logfile = "/dev/null";
	}
	
	LVGServer server = new LVGServer(port, logfile, lvgdir);
	server.start();
    }
}
