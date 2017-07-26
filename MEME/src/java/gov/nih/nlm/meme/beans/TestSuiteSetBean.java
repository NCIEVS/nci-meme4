/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  TestSuiteSetBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.ITestSuite;
import gov.nih.nlm.meme.qa.TestSuiteSet;

import java.util.HashMap;

/**
 * {@link AdminClient} wrapper used for performing test suites.
 *
 * @author MEME Group
 */

 public class TestSuiteSetBean extends ClientBean {

  //
  // Fields
  //

  private AdminClient ac = null;
  private String action_status = null;
  private TestSuiteSet[] test_sets = TestSuiteSet.getTestSuiteSets();
  private HashMap current_test_suites = new HashMap();

  //
  // Constructors
  //

  /**
   * Instantiates a {@link TestSuiteSetBean}.
   * @throws MEMEException if failed to construct this class
   */
  public TestSuiteSetBean() throws MEMEException {
    super();
    ac = new AdminClient();
  }

  //
  // Methods
  //

  /**
   * Returns a fully populated {@link AdminClient}.
   * @return a fully populated {@link AdminClient}
   */
  public AdminClient getAdminClient() {
    configureClient(ac);
    ac.setMidService(getMidService());
    return ac;
  }

  /**
   * Returns the status message regarding the state of the action.
   * @return the status message regarding the state of the action
   */
  public String getActionStatus() {
    return action_status != null ? action_status : "Connected.";
  }

  /**
   * Returns the an array of TestSuiteSets all {@link TestSuiteSet}s.
   * @return the an array of TestSuiteSets all {@link TestSuiteSet}s
   */
  public TestSuiteSet[] getTestSuiteSets() {
    return test_sets;
  }

  /**
   * Adds specified {@link ITestSuite} to the list of current test suites.
   * @param test the {@link ITestSuite}
   */
  public void putTestSuite(ITestSuite test) {
    current_test_suites.put(test.getName(), test);
  }

  /**
   * Returns the {@link ITestSuite} for the specified test suite name.
   * @param name the test suite name
   * @return the {@link ITestSuite} for the specified test suite name.
   */
  public ITestSuite getTestSuite(String name) {
    return (ITestSuite) current_test_suites.get(name);
  }

  /**
   * Indicates whether or not the current test suite set contains one
   * with the specified name.
   * @param name the test suite set name
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean contains(String name) {
    return current_test_suites.containsKey(name);
  }
}
