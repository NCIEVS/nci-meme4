/************************************************************************
 * Package:     gov.nih.nlm.meme
 * Object:      ProgressReporter.java
 ***********************************************************************/
package gov.nih.nlm.util;

/**
 * Generically listens for progress updates.
 */
public interface ProgressReporter {

  /**
   * Adds the progress listener.
   * @param pl the {@link ProgressListener}
   */
  public void addProgressListener(ProgressListener pl);

  /**
   * Removes the progress listener.
   * @param pl the {@link ProgressListener}
   */
  public void removeProgressListener(ProgressListener pl);

}
