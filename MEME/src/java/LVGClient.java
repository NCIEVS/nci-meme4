/*

LVGClient.java - for LVG related functions (Norm, LuiNorm, Wordind, etc) via TCP/IP
suresh@nlm.nih.gov - Sept 2001

Command line: java LVGServer -Dlvg.host=
			     -Dlvg.port=
			     -Ddebug

Example:

-Dlvg.services=norm:16001,luinorm:16002,wordind:16003

*/

import java.io.*;
import java.net.*;

public class LVGClient {
    public static final String DEFAULT_LVG_HOST="smis.nlm.nih.gov";
    public static final int DEFAULT_LVG_PORT=18000;
    public static final String FIELD_SEPARATOR="|";
    private Socket clientSocket;
    PrintWriter toServer;
    BufferedReader fromServer;

    public LVGClient() throws Exception {
	this(DEFAULT_LVG_HOST, DEFAULT_LVG_PORT);
    }

    public LVGClient(int port) throws Exception {
	this(DEFAULT_LVG_HOST, port);
    }

    public LVGClient(String host, int port) throws Exception {
	
	// Open a TCP connection
	clientSocket = new Socket(host, port);
	this.toServer = new PrintWriter(clientSocket.getOutputStream(), true);
	this.fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String doit(String s) {
	String x;
	try {
	    toServer.println(s);
	    x = fromServer.readLine();
	    return x.substring(x.indexOf(FIELD_SEPARATOR)+1);
	} catch (Exception e) {
	    return "";
	}
    }

    public void cleanup() {
	try {
	    toServer.close();
	    fromServer.close();
	    clientSocket.close();
	} catch (Exception e) {
	}
    }

    public static void main(String[] args) {
	LVGClient c;
	String x;

	try {
	    c = new LVGClient(18001);

	    BufferedReader f = new BufferedReader(new InputStreamReader(System.in));
	    while ((x = f.readLine()) != null)
		System.out.println(c.doit(x));

	    f.close();
	    c.cleanup();
	} catch (Exception e) {
	    System.out.println("Cannot open socket: " + e);
	    System.exit(2);
	}
    }
}
