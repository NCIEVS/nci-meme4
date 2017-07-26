/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MEMEApplicationService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.exception.MEMEException;

/**
 * Generically represents an application service.
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant to take a SessionContext argument 
 * 
 * @author MEME Group
 */
public interface MEMEApplicationService {

  //
  // Methods
  //

  /**
   * Perform the actual work of processing an incoming request.
   *
   * @param context the {@link SessionContext} containing the request
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException;

  /**
   * Returns <code>true</code> if the implementation requires
   * requests to use sessions, and <code>false</code> otherwise.
   * @return <code>boolean</code>
   */
  public boolean requiresSession();

  /**
   * Returns <code>true</code> if the server is currently running,
   * and <code>false</code> otherwise.
   * @return <code>boolean</code>
   */
  public boolean isRunning();

  /**
   * Returns <code>true</code> if the server is currently running and
   * this is a re entrant, <code>false</code> otherwise.
   * @param context the {@link SessionContext} containing the request
   * @return <code>boolean</code>
   */
  public boolean isReEntrant(SessionContext context);



}
