/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ClientToolkit
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Properties;

/**
 * This toolkit sets up the environment used by client
 * applications.  It is important that the
 * {@link #initialize()} method is called before any attempt
 * is made to connect to the server.  Calling it multiple
 * times has no ill effect.  Generally, initializing the
 * client toolkit will not be required by client applications
 * because the {@link ClientAPI} automatically does it.
 *
 * @author MEME Group
 */
public class ClientToolkit extends MEMEToolkit {

  //
  // Fields
  //
  private static boolean initialized = false;

  //
  // Constructors
  //

  /**
   * Private constructor prevents instantiation.
   */
  private ClientToolkit() {};

  /**
   * Initializes client application environment.  In practice
   * all it does is initialize the {@link MEMEToolkit}.
   * Once this method has been called, calling it again has no
   * effect.
   * @throws InitializationException if initialization failed.
   */
  public static void initialize() throws InitializationException {
    if (!initialized) {
      MEMEToolkit.initialize(
          ClientConstants.ALLOWABLE_PROPERTIES,
          ClientConstants.REQUIRED_PROPERTIES);

    }
    initialized = true;
  }

  /**
   * Initializes client application environment.  In practice
   * all it does is initialize the {@link MEMEToolkit}.
   * Once this method has been called, calling it again has no
   * effect.
   * @param props An object {@link Properties}.
   * @throws InitializationException if initialization failed.
   */
  public static void initialize(Properties props) throws
      InitializationException {
    if (!initialized) {
      MEMEToolkit.initialize(
          props,
          ClientConstants.ALLOWABLE_PROPERTIES,
          ClientConstants.REQUIRED_PROPERTIES);

    }
    initialized = true;
  }

}