/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  BinaryDataIntegrityCheck
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
     * Generically represents a data driven integrity check that requires two pieces
 * of information (in this case taken from <code>ic_pair</code>).
 *
 * @author MEME Group
 */
public interface BinaryDataIntegrityCheck extends IntegrityCheck {

  //
  // Methods
  //

  /**
   * Returns the {@link BinaryCheckData}.
   * @return the {@link BinaryCheckData}
   */
  public BinaryCheckData[] getCheckData();

  /**
   * Sets the {@link BinaryCheckData}
   * @param check_data_list the {@link BinaryCheckData}
   */
  public void setCheckData(BinaryCheckData[] check_data_list);

  /**
   * Returns the the first half of the data values for the check.
   * @return the the first half of the data values for the check
   */
  public String[] getCheckDataValues1();

  /**
   * Returns those values from the first half that match
   * the negation flag.
   * @param negation indicates the sense in which to compare the data
   * @return those values from the first half that match
   * the negation flag
   */
  public String[] getCheckDataValues1(boolean negation);

  /**
   * Returns the the second half of the data values for the check.
   * @return the the second half of the data values for the check
   */
  public String[] getCheckDataValues2();

  /**
   * Returns those values from the second half that match
   * the negation flag.
   * @param negation indicates the sense in which to compare the data
   * @return those values from the second half that match
   * the negation flag
   */
  public String[] getCheckDataValues2(boolean negation);

}
