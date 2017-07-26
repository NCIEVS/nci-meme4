/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.xml
 * Object:  CGIStyleMEMEServiceRequest
 *
 *****************************************************************************/
package gov.nih.nlm.meme.xml;

import gov.nih.nlm.meme.common.Parameter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a request made to the MEME server via the CGI interface.
 * In other words, a client connected and sent a request something like this
 * <pre>
 * POST / HTTP/1.1
 *
 * service=SomeService&param1=value1
 * </pre>
 *
 * The {@link gov.nih.nlm.meme.server.HTTPRequestListener} is designed to
 * handle requests that contain either <code>MASRequest</code> documents or
 * CGI parameters in the <code>POST</code> data.  The only method here is a
 * constructor which adds a return value to the outgoing request containing a
 * message that should be overwritten by any services handling this request.
 *
 * @author MEME Group
 */
public class CGIStyleMEMEServiceRequest extends MEMEServiceRequest {

  //
  // Fields
  //
  private OutputStream out;
  private Set multiple_values = new HashSet();

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link CGIStyleMEMEServiceRequest}.
   */
  public CGIStyleMEMEServiceRequest() {
    super();
    addReturnValue("HTML",
        "<html><body>The service handler should overwrite this document</body></html>", false);
    // Session parameters should be set by the incoming document
    // See the CGI section of gov.nih.nlm.meme.server.HTTPRequestListener
    //setInitiateSession(true);
    //setSessionId("12345");
  }

  /**
   * Returns the {@link OutputStream} for this request.
   * @return the {@link OutputStream} for this request
   */
  public OutputStream getOutputStream() {
    return out;
  }

  /**
   * Sets the {@link OutputStream} for this request.
   * @param out the {@link OutputStream} for this request
   */
  public void setOutputStream(OutputStream out) {
    this.out = out;
  }

  /**
   * This overrides the superclass method so that if a multiple select
   * list is used, we can accommodate all values in the list.  We
   * package them here into a {@link List}.
   * @param name the parameter name
   * @param value the object value
   * @param flag the flag
   */
  public void addParameter(String name, Object value, boolean flag) {

    // If this parameter has not yet been seen
    // just add it as a simple value.
    if (getParameter(name) == null) {
      super.addParameter(name, value, flag);
    } else {
      // If the parameter has been seen
      // check to see if it has been converted into
      // a list yet or not.  If so, just get the list
      // and add another value
      if (multiple_values.contains(name)) {
        List params = (List) getParameter(name).getValue();
        params.add(value);
      } else {
        // If it has not yet been converted into a list
        // Make a list, add the initial value and the
        // current value, indicate that there are now multiple
        // values for this parameter and add it back to
        // the parameters map.
        multiple_values.add(name);
        List params = new ArrayList();
        params.add(getParameter(name).getValue());
        params.add(value);
        super.addParameter(new Parameter.Default(name, params));

      }

    }
  }

  /**
   * Indicates whether or not a particular parameter has multiple values.
   * @param param_name a parameter name
   * @return <code>true</code> if the parameter name has multiple values, f
   *   <code>false</code> otherwise
   */
  public boolean hasMultipleValues(String param_name) {
    return multiple_values.contains(param_name);
  }

}
