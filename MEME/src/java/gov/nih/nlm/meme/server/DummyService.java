/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  DummyService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.Date;

/**
 * Used for debugging (<B>DO NOT USE</b>).  Deprecated by
 * {@link AdminService}
 *
 * @author MEME Group
 */

public class DummyService implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives requests from the {@link MEMEApplicationServer}
   * Handles the request based on the "function" parameter.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    context.getServiceRequest().addReturnValue("time", new Date(), false);
    MEMEServiceRequest request = context.getServiceRequest();
    String function = (String) request.getParameter("function").getValue();

    if (function.equals("echo")) {
      request.addReturnValue(new Parameter.Default("function", function));
    }
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean requiresSession() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isRunning() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isReEntrant() {
    return false;
  }
}
