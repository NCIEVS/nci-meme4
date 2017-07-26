/************************************************************************
 *
 * Package:     gov.nih.nlm.meme.sql
 * Object:      DataWriterHandler.java
 *
 ***********************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.exception.MEMEException;

/**
 * Generically represents a handler that pre-processes a line of data
 * before it is written to a file
 */

public interface DataWriterHandler {

  /**
   * Perform some operation on the input line and return the transformed line.
   * @param line {@link String} the line to process.
   * @return the processed line.
   * @throws MEMEException if failed to process line.
   */
  public String processLine(String line) throws MEMEException;

}