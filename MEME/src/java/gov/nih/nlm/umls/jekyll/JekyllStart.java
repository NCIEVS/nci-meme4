/**
 * JekyllStart.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.client.ClientConstants;
import gov.nih.nlm.meme.client.ClientToolkit;

import java.util.Properties;

/**
 * Start point of the application. 
 * System properties that can be specified to customize application:
 * <br />
 * <br />
 * <code>meme.prop.loc</code>location of the meme.prop file.
 * <br />
 * <code>meme.server.host</code>a mid-service or explicit hostname for machine running MEME server.
 * If not defined, defaults to <i>meme-server-host</i> mid-service.
 * <br />
 * <code>meme.server.port</code>a mid-service or explicit port number for port on which MEME server is listening.
 * If not defined, defaults to <i>meme-server-port</i> mid-service.
 * <br />
 * <code>db.name</code>database where editing will take place. Mid-service or explicit database TNS
 * name can be specified. If not defined, defaults to <i>editing-db</i> mid-service.
 * <br />
 * <code>default.rela.source</code>default source used in SAB and SL for rela editing.
 */
class JekyllStart {
    public static void main(String[] s) {
        try {
            ClassLoader cl = ClientToolkit.class.getClassLoader();
            Properties props = new Properties();
            props.load(cl.getResourceAsStream(System
                    .getProperty("jnlp.meme.prop.loc")));

            // ---------------------------------------------
            // MID Services
            // ---------------------------------------------
            MEMEToolkit.setProperty(MEMEToolkit.MIDSVCS_HOST, props
                    .getProperty(MEMEToolkit.MIDSVCS_HOST));
            MEMEToolkit.setProperty(MEMEToolkit.MIDSVCS_PORT, props
                    .getProperty(MEMEToolkit.MIDSVCS_PORT));
            MIDServices.refreshCache();

            props.setProperty("meme.log.file", "jekyll.log");

            // ---------------------------------------------
            // MEME Server host
            // ---------------------------------------------
            String server_host_service = System.getProperty("meme.server.host");
            if (server_host_service == null) {
                props.setProperty("meme.client.protocol", MIDServices
                        .getService("jekyll-server-host"));
                props.setProperty("meme.client.server.host", MIDServices
                        .getService("jekyll-server-host"));
            } else if (!MIDServices.getService(server_host_service).equals("")) {
                props.setProperty("meme.client.server.protocol", MIDServices
                        .getService(server_host_service));
                props.setProperty("meme.client.server.host", MIDServices
                        .getService(server_host_service));
            } else {
                props.setProperty("meme.client.protocol", server_host_service);
                props.setProperty("meme.client.server.host", server_host_service);
            }

            // ---------------------------------------------
            // MEME Server port
            // ---------------------------------------------
            String server_port_service = System.getProperty("meme.server.port");
            if (server_port_service == null) {
                props.setProperty("meme.client.server.port", MIDServices
                        .getService("jekyll-server-port"));
            } else if (!MIDServices.getService(server_port_service).equals("")) {
                props.setProperty("meme.client.server.port", MIDServices
                        .getService(server_port_service));
            } else {
                props.setProperty("meme.client.server.port", server_port_service);
            }

	    MEMEToolkit.logComment("server_host_service = " + server_host_service);
	    MEMEToolkit.logComment("server_port_service = " + server_port_service);
	    MEMEToolkit.logComment("meme.client.server.host = " + props.getProperty("meme.client.server.host"));
	    MEMEToolkit.logComment("meme.client.server.port = " + props.getProperty("meme.client.server.port"));

            props.setProperty("meme.view", "true");
            props.setProperty("meme.debug", "false");

            ClientToolkit.initialize(props);
            
            // if db or mid-service is not specified as a system property,
            // use default, which usually points to the production db.
            if (System.getProperty("db.name") == null) {
                System.setProperty("db.name", MIDServices
                        .getService("editing-db"));
            }

            // Checking if server is actually running on specified
            // host and port.
            try {
                JekyllKit.getAdminClient().ping();
            } catch (Exception e) {
                MEMEToolkit
                        .notifyUser("Server is not available at host/port:\n"
                                + ClientToolkit
                                        .getProperty(ClientConstants.SERVER_HOST)
                                + " / "
                                + ClientToolkit
                                        .getProperty(ClientConstants.SERVER_PORT));
                e.printStackTrace();
                System.exit(1);
            }

            JekyllKit.main(new String[0]);
        } catch (Exception e) {
            MEMEToolkit
                    .reportError("There was an error in initializing application's properties."
                            + "\nConsole may have more information."
            +"HOST="+MEMEToolkit.getProperty(MEMEToolkit.MIDSVCS_HOST)+" "
            +"PORT="+MEMEToolkit.getProperty(MEMEToolkit.MIDSVCS_PORT));
            e.printStackTrace();
            System.exit(1);
        }
    }
}