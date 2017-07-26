/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server
 * Object:  MRDApplicationServer
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.server;

import gov.nih.nlm.meme.Initializable;
import gov.nih.nlm.meme.InitializationContext;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.server.MEMEApplicationServer;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.MEMESchedule;
import gov.nih.nlm.mrd.sql.DataSourceConstants;

/**
 * Application entry point for "MRD Server".
 *
 * @author  MEME Group
 */
public class MRDApplicationServer extends MEMEApplicationServer implements
    InitializationContext {

  //
  // Fields
  //

  /**
   * Instantiates a {@link MRDApplicationServer}.
   */
  public MRDApplicationServer() {
    super();
  }

  /**
   * Starts the server.
   */
  public void start() {
    // Initialize the MEMEToolkit first
    try {
      MEMEToolkit.initialize(ServerConstants.ALLOWABLE_PROPERTIES,
                             ServerConstants.REQUIRED_PROPERTIES);
    } catch (InitializationException ie) {
      MEMEToolkit.handleError(ie);
    }
    super.start();
  }

  /**
   * Stops the server.
   */
  public void stop() {
    super.stop();
  }

  /**
   * Adds an {@link Initializable} hook.
   * @param init the {@link Initializable} hook
   * @throws MEMEException if failed to add hook
   */
  public void addHook(Initializable init) {

    // Handle local stuff, otherwise forward
    // the request to the superclass
    if (init instanceof MRDDataSourcePool) {
      ServerToolkit.setDataSourcePool( (MRDDataSourcePool) init);
    } else if (init instanceof MEMESchedule) {
      MEMESchedule ms = (MEMESchedule) init;
      ServerToolkit.setMEMESchedule(ms);
      String service =
          MEMEToolkit.getProperty(DataSourceConstants.MEME_SCHEDULE);
      // If the setting of the data source fails
      // then we will get an initialization exception
      // in the MEMESchedule so don't worry about it here
      try {
        if (service.equals("MID")) {
          ms.setDataSource(ServerToolkit.newMIDDataSource());
        } else if (service.equals("MRD")) {
          ms.setDataSource(ServerToolkit.newMRDDataSource());
        }
      } catch (Exception e) {}
    } else {
      super.addHook(init);
    }
  }

  /**
   * Returns the correct kind of {@link MEMEDataSource} for this server.
   * The purpose of this method is to allow the {@link MRDApplicationServer}
   * to return MRD database connections instead of MID database connections.
   *
   * @param mid_service the mid services name
   * @param user the username
   * @param password the password
   * @return the correct {@link MEMEDataSource}
   * @throws MEMEException if failed to get datasource
   */
  public MEMEDataSource getDataSource(String mid_service, String user,
                                      String password) throws MEMEException {
    return ServerToolkit.getMRDDataSource(mid_service, user, password);
  }

  /**
   * Returns the data source to the server toolkit.
   * @param ds the {@link MEMEDataSource} to return
   * @throws MEMEException if failed to return data source
   */
  public void returnDataSource(MEMEDataSource ds) throws MEMEException {
    ServerToolkit.returnDataSource(ds);
  }

  /**
   * The main method starts the server.
   * @param s command line args.
   */
  public static void main(String[] s) {
    // Create and start server
    MRDApplicationServer server = new MRDApplicationServer();
    server.start();
  }

}
