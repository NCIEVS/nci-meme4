/************************************************************************
 *
 * Package:     gov.nih.nlm.meme.sql
 * Object:      StripDuplicateSpacesHandler.java
 *
 ***********************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.exception.MEMEException;

import java.util.StringTokenizer;

/**
 * Removes duplicate spaces before sending a line of output to a file.
 */
public class StripDuplicateSpacesHandler implements DataWriterHandler {

  /**
   * Instantiates an empty {@link StripDuplicateSpacesHandler}.
   */
  public StripDuplicateSpacesHandler() {}

  /**
   * Removes duplicate spaces from the input line.
   * @param line the input line
   * @return the input line stripped of duplicate spaces
   * @throws MEMEException if anything goes wrong
   */
  public String processLine(String line) throws MEMEException {
    final StringTokenizer st = new StringTokenizer(line, " ");
    final StringBuffer sb = new StringBuffer();
    if (st.hasMoreElements()) {
      sb.append(st.nextToken());
    } while (st.hasMoreElements()) {
      sb.append(" ").append(st.nextToken());
    }
    return sb.toString();
  }

}