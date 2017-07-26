/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe
 * Object:     PersistenceManager.java
 * 
 * Author:     Brian Carlsen, Deborah Shapiro
 *
 * Remarks:    
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import java.io.File;

/**
 * @author Brian Carlsen, Owen J. Carlsen, Yun-Jung Kim
 * @version 1.5
 */
public interface PersistenceManager {
  public Object read (File file) throws Exception;
  public void write (Object o, File file) throws Exception;
}
