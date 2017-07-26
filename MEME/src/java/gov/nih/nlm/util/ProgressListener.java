/************************************************************************
 * Package:     gov.nih.nlm.meme
 * Object:      ProgressListener.java
 ***********************************************************************/
package gov.nih.nlm.util;

/**
 * Generically listens for progress updates.
 */
public interface ProgressListener {

  public void updateProgress(ProgressEvent pe);

}
