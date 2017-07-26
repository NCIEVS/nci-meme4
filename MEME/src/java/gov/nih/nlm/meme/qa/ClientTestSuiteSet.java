/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  ClientTestSuiteSet
 *
 * 02/14/2006 RBE (1-79GGX): Changed sets of client objects to test
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.qa.client.ActionClientTest;
import gov.nih.nlm.meme.qa.client.AdminClientTest;
import gov.nih.nlm.meme.qa.client.AuxiliaryDataClientTest;
import gov.nih.nlm.meme.qa.client.ConceptMappingClientTest;
import gov.nih.nlm.meme.qa.client.ContentViewClientTest;
import gov.nih.nlm.meme.qa.client.CoreDataClientTest;
import gov.nih.nlm.meme.qa.client.DummyClientTest;
import gov.nih.nlm.meme.qa.client.FinderClientTest;
import gov.nih.nlm.meme.qa.client.MEMERelaEditorKitTest;
import gov.nih.nlm.meme.qa.client.MaintenanceClientTest;
import gov.nih.nlm.meme.qa.client.MappingClientTest;
import gov.nih.nlm.meme.qa.client.MergeEngineClientTest;
import gov.nih.nlm.meme.qa.client.ReportsClientTest;
import gov.nih.nlm.meme.qa.client.WorklistClientTest;

/**
 * Represents a set of test suites designed to test the complete functionality
 * of the molecular actions.
 */
public class ClientTestSuiteSet extends TestSuiteSet {

  /**
   * Instantiates an empty {@link ClientTestSuiteSet}.
   */
  public ClientTestSuiteSet() { }

  /**
   * Returns the test set name.
   * @return the test set name
   */
  public String getName() {
    return "ClientTestSuiteSet";
  }

  /**
   * Returns the test set description.
   * @return the test set description
   */
  public String getDescription() {
    return "QA Tests for Clients";
  }

  /**
   * Returns the {@link ITestSuite}s that are part of this set.
   * @return the {@link ITestSuite}s that are part of this set
   */
  public ITestSuite[] getTestSuites() {
    return new ITestSuite[] {
    		new ActionClientTest(),							 // 0
    		new AdminClientTest(),							 // 1
    		new AuxiliaryDataClientTest(),			 // 2
    		new ConceptMappingClientTest(),			 // 3
    		new ContentViewClientTest(),				 // 4
    		new CoreDataClientTest(),						 // 5
    		new DummyClientTest(),							 // 6
    		new FinderClientTest(),							 // 7
    		new MaintenanceClientTest(),				 // 8
    		new MappingClientTest(),						 // 9
    		new MEMERelaEditorKitTest(),				 // 10
    		new MergeEngineClientTest(),				 // 11
    		new WorklistClientTest(),						 // 12
    		new ReportsClientTest()
    };
  }

}