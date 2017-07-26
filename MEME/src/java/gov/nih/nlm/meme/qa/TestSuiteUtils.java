/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa
 * Object:  TestSuiteUtils.java
 *
 * Changes
 *   01/13/2006 BAC (1-73T75): printHeader does a better job of reporting
 *     correct host, port, and mid service
 *   
 *****************************************************************************/
package gov.nih.nlm.meme.qa;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.client.EditingClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * Provides utility functions to test suites
 */
public class TestSuiteUtils {

  /**
   * State variables
   */
  private static final String NAME = "mth";
  private static final String PASSWORD = "umls_tuttle";

  /**
   * Instantiates and populates a new {@link Atom} object.
   * @param name the atom name
   * @param client the {@link EditingClient} being used
   * @return the nwe {@link Atom}
   * @throws MEMEException if anything goes wrong
   */
  public static Atom createAtom(String name, EditingClient client) throws
      MEMEException {
    //create an atom
    Atom newAtom = new Atom.Default();

    newAtom.setString(name);
    newAtom.setSource(client.getSource("MTH"));
    newAtom.setTermgroup(client.getTermgroup("MTH/PT"));
    newAtom.setCode(Code.newCode("NOCODE"));
    newAtom.setStatus('R');
    newAtom.setGenerated(true);
    newAtom.setReleased('N');
    newAtom.setTobereleased('Y');
    newAtom.setSuppressible("N");

    return newAtom;
  }

  /**
   * Instantiates and populates a new {@link Attribute} object.
   * @param name the attribute name
   * @param test_atom the {@link Atom} to connect the attribute to
   * @param client the {@link EditingClient} being used
   * @return the new atom-level {@link Attribute}
   * @throws MEMEException if anything goes wrong
   */
  public static Attribute createAttribute(String name, Atom test_atom,
                                          EditingClient client) throws  MEMEException {
    //
    // create an attribute
    //
    Attribute test_attr = new Attribute.Default();

    test_attr.setName(name);
    test_attr.setAtom(test_atom);
    test_attr.setSource(client.getSource("MTH"));
    test_attr.setValue("This is a test attribute.");
    //S for Atom level, C for concept level
    test_attr.setLevel('S');
    test_attr.setStatus('R');
    test_attr.setGenerated(false);
    test_attr.setReleased('A');
    test_attr.setTobereleased('Y');

    return test_attr;
  }

  /**
   * Instantiates and populates a new concept level {@link Relationship} object.
   * @param client the {@link EditingClient} being used
   * @param concept1 the first {@link Concept}
   * @param concept2 the second {@link Concept}
   * @return a concept level {@link Relationship}
   * @throws MEMEException if anything goes wrong
   */
  public static Relationship createConceptLevelRelationship(
      EditingClient client,
      Concept concept1,
      Concept concept2) throws MEMEException {
    Relationship new_rel = new Relationship.Default();
    new_rel.setConcept(concept1);
    new_rel.setRelatedConcept(concept2);
    new_rel.setLevel('C');
    new_rel.setName("RT");
    new_rel.setAttribute("mapped_to");
    new_rel.setTobereleased('Y');
    new_rel.setReleased('A');
    new_rel.setStatus('N');
    new_rel.setSource(client.getSource("MTH"));
    new_rel.setSourceOfLabel(client.getSource("MTH"));

    return new_rel;
  }

  /**
   * Returns an {@link EditingClient} connected to the default database.
   * @return the {@link EditingClient}
   * @throws MEMEException if anything fails
   */
  public static EditingClient getClient() throws MEMEException {
    return getClient("");
  }


  /**
   * Returns an {@link EditingClient} connected to the specified database.
   * @param mid_service the mid service
   * @return the {@link EditingClient}
   * @throws MEMEException
   */
  public static EditingClient getClient(String mid_service) throws MEMEException {
    EditingClient client = new EditingClient(mid_service, NAME, PASSWORD);
    //configure the editing client
    client.setChangeStatus(true);
    client.setTransactionIdentifier(client.getNextIdentifierForType(
        MolecularTransaction.class));
    client.setAuthority(new Authority.Default("L-QA"));
    return client;
  }


  /**
   * Returns the {@link DateFormat} to use.
   * @return date format used for all test suites
   */
  public static SimpleDateFormat getDateFormat() {
    return MEMEToolkit.getDateFormat();
  }

  /**
   * Prints a log header from the system properties.
   * @param current_test_suite the current {@link ITestSuite}}
   */
  public static void printHeader(ITestSuite current_test_suite) {

    // Read properties file.
    DateFormat date_format = getDateFormat();
    Date start_time = new Date(System.currentTimeMillis());
    current_test_suite.addToLog(
        "--------------------------------------------------------");
    current_test_suite.addToLog("Starting " + current_test_suite.getName() + " at " +
                                date_format.format(start_time));
    current_test_suite.addToLog(
        "--------------------------------------------------------");
    //current_test_suite.addToLog("MEME_HOME:   " + properties.getProperty("env.MEME_HOME"));
    //current_test_suite.addToLog("ORACLE_HOME: " + properties.getProperty("env.ORACLE_HOME"));
    current_test_suite.addToLog("MID SERVICE: " + current_test_suite.getClient().getMidService());
    current_test_suite.addToLog("HOST:        " + MEMEToolkit.getProperty("meme.client.server.host"));
    current_test_suite.addToLog("PORT:        " + MEMEToolkit.getProperty("meme.client.server.port"));
    current_test_suite.addToLog("");
  }
}