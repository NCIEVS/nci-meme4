/**************************************************************************
 *
 * Package:     gov.nih.nlm.util
 * Class:      *
 **************************************************************************/
package gov.nih.nlm.util;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 *
 * @author Brian Carlsen, Deborah Shapiro
 */
public class MultiplePathReader {

  //
  // Fields
  //
  private int reader_index = 0;
  private BufferedReader[] readers;

  /**
   * Instantiates a {@link MultiplePathReader} from the component {@link BufferedReader}s.
   * @param readers array of {@link BufferedReader}s
   */
  public MultiplePathReader(BufferedReader[] readers) {
    this.readers = readers;
    reader_index = 0;
  }

  /**
   * Read a line of text from the current {@link BufferedReader}.  Increment to return
       * lines from the next {@link BufferedReader} when the current one returns null.
   * @return line of text {@link String}
   * @throws IOException
   */
  public String readLine() throws IOException {
    String line;
    if ( (line = (readers[reader_index]).readLine()) != null) {
      //System.out.println("line " + line);
      return line;
    } else if (reader_index < readers.length - 1) {
      //System.out.println("incrementing reader_index " + reader_index);
      reader_index++;
      return readLine();
    } else {
      return null;
    }
  }

  /**
   * Close the {@link MultiplePathReader}.
   * @throws IOException
   */
  public void close() throws IOException {
    for (int i = 0; i < readers.length; i++) {
      readers[i].close();
    }
  }

  /**
   * Mark the present position in the stream.
   * @param read_ahead_limit limit on the number of characters that may be read
   * while still preserving the mark
   * @throws IOException
   */
  public void mark(int read_ahead_limit) throws IOException {
    for (int i = reader_index; i < readers.length; i++) {
      readers[i].mark(read_ahead_limit);
    }
  }

  /**
   * Reset the stream to the most recent mark.
   * @throws IOException
   */
  public void reset() throws IOException {
    for (int i = reader_index; i < readers.length; i++) {
      readers[i].reset();
    }
  }
}
