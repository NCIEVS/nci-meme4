/************************************************************************
 *
 * Package:     gov.nih.nlm.meme.sql
 * Object:      StripDuplicateSpacesHandler.java
 *
 ***********************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.util.StringTokenizer;

/**
 * Removes duplicate spaces before sending a line of output to a file.
 */
public class StripDuplicateTokensHandler implements DataWriterHandler {

	private String delimiter;
  /**
   * Instantiates an empty {@link StripDuplicateSpacesHandler}.
   */
  public StripDuplicateTokensHandler(String delimiter) {this.delimiter = delimiter;}

  /**
   * Removes duplicate spaces from the "|" delimited input line.
   * @param line the input line
   * @return the input line stripped of duplicate spaces
   * @throws MEMEException if anything goes wrong
   */
  public String processLine(String line) throws MEMEException {
    final StringBuffer sb = new StringBuffer();
    String[] fields = FieldedStringTokenizer.split(line, String.valueOf('|'));
    for(int i=0; i<fields.length; i++) {
        StringTokenizer st = new StringTokenizer(fields[i], delimiter);
	    if (st.hasMoreElements()) {
	      sb.append(st.nextToken());
	    }
	    while (st.hasMoreElements()) {
	    	String next = st.nextToken();
	    	sb.append(delimiter).append(next);
	    }
	    sb.append("|");
    }
    return sb.toString();
  }

}