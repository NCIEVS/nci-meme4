/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  AbstractUnaryDataMergeMoveInhibitor
 *
 * 04/07/2006 RBE (1-AV8WP): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Abstract implementation of a {@link MergeInhibitor} that is also
 * a {@link UnaryDataIntegrityCheck}.
 * This should be the superclass for such checks as {@link MGV_B}.
 *
 * @author MEME Group
 */

public abstract class AbstractUnaryDataMergeMoveInhibitor extends
    AbstractMergeMoveInhibitor implements UnaryDataIntegrityCheck {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AbstractUnaryDataMergeMoveInhibitor}.
   */
  public AbstractUnaryDataMergeMoveInhibitor() {
    super();
  }

  //
  // Fields
  //

  private UnaryCheckData[] check_data_list = null;

  //
  // Implementation of UnaryDataIntegrityCheck
  //

  /**
   * Return all check data.
   * @return all check data
   */
  public UnaryCheckData[] getCheckData() {
    return check_data_list;
  }

  /**
   * Sets all check data.
   * @param check_data_list the check data
   */
  public void setCheckData(UnaryCheckData[] check_data_list) {
    this.check_data_list = check_data_list;
  }

  /**
   * Returns the set of check data values.
   * @return the set of check data values
   */
  public String[] getCheckDataValues() {
    return getCheckDataValues(false);
  }

  /**
   * Returns the set of check data values matching the negation flag.
   * @param negation the negation flag
   * @return the data values matching the negation flag
   */
  public String[] getCheckDataValues(boolean negation) {
    int ct = 0;
    for (int i = 0; i < check_data_list.length; i++) {
      // If negation matches, keep this row
      if (check_data_list[i].isNegated() == negation) {
        ct++;
      }
    }
    String[] values = new String[ct];
    ct = 0;
    for (int i = 0; i < check_data_list.length; i++) {
      if (check_data_list[i].isNegated() == negation) {
        values[ct++] = check_data_list[i].getValue();
      }
    }
    return values;
  }

}
