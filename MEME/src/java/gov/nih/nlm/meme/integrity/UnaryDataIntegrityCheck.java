/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  UnaryDataIntegrityCheck
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Generically represents a data driven integrity check that requires one
 * additional piece of information (in this case taken from <code>ic_single</code>).
 *
 * @author MEME Group
 */
public interface UnaryDataIntegrityCheck extends IntegrityCheck {

  //
  // Methods
  //

  /**
   * Returns the {@link UnaryCheckData}.
   * @return the {@link UnaryCheckData}
   */
  public UnaryCheckData[] getCheckData();

  /**
   * Sets the {@link UnaryCheckData}.
   * @param check_data_list the {@link UnaryCheckData}
   */
  public void setCheckData(UnaryCheckData[] check_data_list);

  /**
   * Returns the {@link UnaryCheckData} values.
   * @return the {@link UnaryCheckData} values
   */
  public String[] getCheckDataValues();

  /**
   * Returns the the {@link UnaryCheckData} values where the negation
   * flag is set to the specified value.
       * @param negated flag indicating whether or not to negate the sense of the data
   * @return the values
   */
  public String[] getCheckDataValues(boolean negated);

}
