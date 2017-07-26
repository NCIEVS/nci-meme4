/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ClientConstants
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEConstants;

/**
 * This interface holds constants commonly used by MEME client.  Many of the
 * constants define property names that appear in the property file.
 *
 * @author MEME Group
 */
public interface ClientConstants extends MEMEConstants {

  //
  // MEME Application Client properties
  //

  /**
   * Name of the property which indicates the authority
   * used by a client application.
   */
  public final static String AUTHORITY = "meme.client.authority";

  /**
   * Name of the property which indicates the host name
   * of the server used by the client.
   */
  public final static String SERVER_HOST = "meme.client.server.host";

  /**
   * Name of the property which indicates the port
   * of the server used by the client.
   */
  public final static String SERVER_PORT = "meme.client.server.port";

  /**
   * Soma: adding a property to include the languages from the client.
   * Format is meme.client.languages.include=languageA,languageB
   * Currenly in Jekyll this property will be set.
   */
  public final static String INCLUDE_LANGUAGES = "meme.client.languages.include";
  /**
   * Name of the property which indicates the protocol handler
   * class used by clients.  The value of the property with this
   * name should be a fully qualified name for a class implementing
   * {@link MEMERequestClient}.
   */
  public final static String PROTOCOL_HANDLER = "meme.client.protocol.class";

  /**
   * List of properties which must be set.
   */
  public final static String[] REQUIRED_PROPERTIES = {
      SERVER_HOST, SERVER_PORT, PROTOCOL_HANDLER};

  /**
   * Additional allowed properties.  These are read from the
   * properties file when the toolkit is initialized.
   */
  public final static String[] ALLOWABLE_PROPERTIES = {
      AUTHORITY, SERVER_HOST, SERVER_PORT, PROTOCOL_HANDLER,INCLUDE_LANGUAGES};

}
