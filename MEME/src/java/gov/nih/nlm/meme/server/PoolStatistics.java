/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  PoolStatistics
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

/**
 * Tracks of the current detail value of thread pool.
 *
 * @author MEME Group
 */

public class PoolStatistics {

  //
  // Fields
  //

  private int active_count = 0;
  private int inactive_count = 0;
  private long last_sample = 0;
  private double average_usage = 0.0;
  private long sample_size = 0;

  //
  // Constructors
  //

  /**
   * Instantiates {@link PoolStatistics} from parameters representing all the fields in this class.
   * @param active_count the active count
   * @param inactive_count the inactive count
   * @param last_sample the last sample size
   * @param average_usage the average length of use
   * @param sample_size the sample size
   */
  public PoolStatistics(int active_count, int inactive_count, long last_sample,
                        double average_usage, long sample_size) {
    this.active_count = active_count;
    this.inactive_count = inactive_count;
    this.last_sample = last_sample;
    this.average_usage = average_usage;
    this.sample_size = sample_size;
  };

  //
  // Methods
  //

  /**
   * Returns the active count.
   * @return the active count
   */
  public int getActiveCount() {
    return active_count;
  }

  /**
   * Returns the inactive count.
   * @return the inactive count
   */
  public int getInactiveCount() {
    return inactive_count;
  }

  /**
   * Returns the last sample.
   * @return the last sample
   */
  public long getLastSample() {
    return last_sample;
  }

  /**
   * Returns the average usage.
   * @return the average usage
   */
  public double getAverageUsage() {
    return average_usage;
  }

  /**
   * Returns the sample size.
   * @return the sample size
   */
  public long getSampleSize() {
    return sample_size;
  }

}
