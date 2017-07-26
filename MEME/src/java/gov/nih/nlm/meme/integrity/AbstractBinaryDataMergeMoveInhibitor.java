/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  AbstractBinaryDataMergeMoveInhibitor
 *
 * 04/07/2006 RBE (1-AV8WP): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Abstract implementation of a {@link MergeInhibitor} that is also
 * a {@link BinaryDataIntegrityCheck}. This should be the superclass for
 * such checks as {@link MGV_J}.
 *
 * @author MEME Group
 */

public abstract class AbstractBinaryDataMergeMoveInhibitor extends
    AbstractMergeMoveInhibitor implements BinaryDataIntegrityCheck {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AbstractBinaryDataMergeMoveInhibitor}.
   */
  public AbstractBinaryDataMergeMoveInhibitor() {
    super();
  }

  //
  // Fields
  //

  private BinaryCheckData[] check_data_list = null;

  //
  // Implementation of BinaryDataIntegrityCheck
  //

  /**
   * Returns all check data.
   * @return all check data
   */
  public BinaryCheckData[] getCheckData() {
    return check_data_list;
  }

  /**
   * Sets check data.
   * @param check_data_list the check data
   */
  public void setCheckData(BinaryCheckData[] check_data_list) {
    this.check_data_list = check_data_list;
  }

  /**
   * Returns the first half of the data values for the check.
   * @return the first half of the data values for the check
   */
  public String[] getCheckDataValues1() {
    return getCheckDataValues1(false);
  }

  /**
   * Returns the first half of the data values for the check.
   * @param negation a flag used to negate the sense of the check
   * @return the first half of the data values matching the negation flag
   */
  public String[] getCheckDataValues1(boolean negation) {
    int ct = 0;
    for (int i = 0; i < check_data_list.length; i++) {
      if (check_data_list[i].isNegated() == negation) {
        ct++;
      }
    }

    String[] values = new String[ct++];
    ct = 0;
    for (int i = 0; i < check_data_list.length; i++) {
      if (check_data_list[i].isNegated() == negation) {
        values[ct++] = check_data_list[i].getValue1();
      }
    }
    return values;
  }

  /**
   * Returns the second half of the data values for the check.
   * @return the second half of the data values matching the negation flag
   */
  public String[] getCheckDataValues2() {
    return getCheckDataValues2(false);
  }

  /**
   * Returns the second half of the data values for the check.
   * @param negation a flag used to negate the sense of the check
   * @return the second half of the data values matching the negation flag
   */
  public String[] getCheckDataValues2(boolean negation) {
    int ct = 0;
    for (int i = 0; i < check_data_list.length; i++) {
      if (check_data_list[i].isNegated() == negation) {
        ct++;
      }
    }

    String[] values = new String[ct++];
    ct = 0;
    for (int i = 0; i < check_data_list.length; i++) {
      if (check_data_list[i].isNegated() == negation) {
        values[ct++] = check_data_list[i].getValue2();
      }
    }
    return values;
  }

}
