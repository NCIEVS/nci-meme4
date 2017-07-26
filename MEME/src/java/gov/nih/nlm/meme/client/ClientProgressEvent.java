/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ClientProgressEvent
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

/**
 * Used to inform the client of server-side progress on a certain task.
 *
 * @author BAC, RBE
 *
 */

public class ClientProgressEvent {

  //
  // Fields
  //

  private String message = null;
  private int progress = 0;

  //
  // Constructors
  //
  /**
   * Instantiates a {@link ClientProgressEvent} from the specified
   * message and progress amount.
   * @param message the progress message
   * @param progress the progress amount
   */
  public ClientProgressEvent(String message, int progress) {
    this.message = message;
    this.progress = progress;
  }

  //
  // Methods
  //

  /**
   * Returns the progress message.
   * @return the progress message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the progress amount.
   * @return the progress amount
   */
  public int getProgress() {
    return progress;
  }

}