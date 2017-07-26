/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  FinderClientTest
 *
 * 01/30/2006 RBE (1-763IU): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.client.FinderClient;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Finder
 */
public class FinderClientTest extends TestSuite {

  public FinderClientTest() {
    setName("FinderClientTest");
    setDescription("Test Suite for Finder");
  }

  /**
   * Perform Test Suite Finder
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    FinderClient client = null;

    try {
      client = new FinderClient("apelon");

      String finder = "exact_string";
      String source = "AIR94";
      String semantic = "Antibiotic";
      String string = "monosodium";
      String[] words = new String[] {"Other", "Brain", "Heart"};

	    //
	    // 1.1. Test setMidService(String), getMidService()
	    //
      addToLog("    1.1. Test setMidService(String), "
        + "getMidService() ... "
        + date_format.format(timestamp));

      client.setMidService("apelon");
      if (client.getMidService().equals("apelon"))
        addToLog("    1.1. Test Passed");
      else {
        addToLog("    1.1. Test Failed");
        thisTestFailed();
      }

	    //
	    // 2.1. Test setSessionId(String), getSessionId()
	    //
      addToLog("    2.1. Test setSessionId(String), "
        + "getSessionId() ... "
        + date_format.format(timestamp));

      client.setSessionId(null);
      if (client.getSessionId() == null)
        addToLog("    2.1. Test Passed");
      else {
        addToLog("    2.1. Test Failed");
        thisTestFailed();
      }
      
      //
	    // 3.1. Test setMaxResultCount(int), getMaxResultCount()
	    //
      addToLog(
        "    3.1. Test setMaxResultsCount(int), getMaxResultCount() ... "
        + date_format.format(timestamp));
      client.setMaxResultCount(10);
      if (client.getMaxResultCount() == 10)
        addToLog("    3.1. Test Passed");
      else {
        addToLog("    3.1. Test Failed");
        thisTestFailed();
      }

      //
	    // 4.1. Test setRecursive(boolean), getRecursive()
	    //
      addToLog(
        "    4.1. Test setRecursive(boolean), getRecursive() ... "
        + date_format.format(timestamp));
      client.setRecursive(true);
      if (client.getRecursive())
        addToLog("    4.1. Test Passed");
      else {
        addToLog("    4.1. Test Failed");
        thisTestFailed();
      }
      
      //
	    // 5.1. Test setAuthority(Authority), getAuthority()
	    //
	    addToLog("    5.1. Test setAuthority(Authority), "
        + "getAuthority() ... "
        + date_format.format(timestamp));
	
	    Authority authority = new Authority.Default("MTH");
	    client.setAuthority(authority);
	    if (client.getAuthority().equals(authority))
	      addToLog("    5.1. Test Passed");
	    else {
	      addToLog("    5.1. Test Failed");
	      thisTestFailed();
	    }
      
      
      //
	    // 6.1. Test restrictByConcept(Concept)
	    //
      addToLog(
        "    6.1. Test restrictByConcept(Concept) ... "
        + date_format.format(timestamp));
      client.restrictByConcept(new Concept.Default(2859));

      //
	    // 7.1. Test restrictByWorklist(Worklist)
	    //
      //addToLog(
      //  "    7.1. Test restrictByWorklist(Worklist) ... "
      //  + date_format.format(timestamp));
      //Worklist wrklst = new Worklist();
      //wrklst.setName("meow.wrk03c_units_ch_02");
      //client.restrictByWorklist(wrklst);

      //
	    // 8.1. Test restrictByCoreDataType(Class)
	    //
      //addToLog(
      //  "    8.1. Test restrictByCoreDataType(Class) ... "
      //  + date_format.format(timestamp));
      //client.restrictByCoreDataType(Atom.class);
      //client.restrictByCoreDataType(Class.forName(
      //  "gov.nih.nlm.meme.common.Atom"));

      //
	    // 9.1. Test restrictByTransaction(MolecularTransaction)
	    //
      //addToLog(
      //  "    9.1. Test restrictByTransaction(MolecularTransaction) ... "
      //  + date_format.format(timestamp));
      //client.restrictByTransaction(new MolecularTransaction(12345));

      //
	    // 10.1. Test restrictByActionType(MolecularAction)
	    //
      //addToLog(
      //  "    10.1. Test restrictByActionType(MolecularAction) ... "
      //  + date_format.format(timestamp));
      //MolecularAction ma = new MolecularAction(12345);
      //ma.setActionName("MOLECULAR_INSERT");
      //client.restrictByActionType(ma);

      //
	    // 11.1. Test restrictByDateRange(Date, Date)
	    //
      //addToLog(
      //  "    11.1. Test restrictByDateRange(Date, Date) ... "
      //  + date_format.format(timestamp));
      //client.restrictByDateRange(new java.util.Date(), new java.util.Date());

      //
	    // 12.1. Test restrictBySource(Source)
	    //
      addToLog(
        "    12.1. Test restrictBySource(Source) ... "
        + date_format.format(timestamp));
      if (source != null) {
        Source src = new Source.Default();
        src.setSourceAbbreviation(source);
        client.restrictBySource(src);
      }

      //
	    // 12.2. Test restrictBySources(Source[])
	    //
      //addToLog(
      //  "    12.2. Test restrictBySources(Source[]) ... "
      //  + date_format.format(timestamp));

      //
	    // 13.1. Test restrictByReleasable()
	    //
      addToLog(
        "    13.1. Test restrictByReleasable() ... "
        + date_format.format(timestamp));
      client.restrictByReleasable();

      //
	    // 14.1. Test restrictByChemicalSemanticTpye()
	    //
      //addToLog(
      //  "    14.1. Test restrictByChemicalSemanticType() ... "
      //  + date_format.format(timestamp));
      //client.restrictByChemicalSemanticType();

      //
	    // 15.1. Test restrictByNonChemicalSemanticType()
	    //
      //addToLog(
      //  "    15.1. Test restrictByNonChemicalSemanticType() ... "
      //  + date_format.format(timestamp));
      //client.restrictByNonChemicalSemanticType();

      //
	    // 16.1. Test restrictBySemanticType(SemanticType)
	    //
      addToLog(
        "    16.1. Test restrictBySemanticType(SemanticType) ... "
        + date_format.format(timestamp));

      if (semantic != null) {
        SemanticType sty = new SemanticType.Default();
        sty.setValue(semantic);
        client.restrictBySemanticType(sty);
      }

      //
	    // 16.2. Test restrictBySemanticTypes(SemanticType[])
	    //
      //addToLog(
      //  "    16.2. Test restrictBySemanticTypes(SemanticType[]) ... "
      //  + date_format.format(timestamp));

      if (finder.equals("find_molecular_actions")) {
        //
  	    // 17.1. Test findMolecularActions()
  	    //
        addToLog(
          "    17.1. Test findMolecularActions() ... "
          + date_format.format(timestamp));
        MolecularAction[] actions = client.findMolecularActions();
        for (int i = 0; i < actions.length; i++) {
          addToLog("\t\t" + actions[i].getIdentifier().toString());
          if (i > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

      } else if (finder.equals("find_concepts")) {
        //
  	    // 17.2. Test findConceptsByCode(Code)
  	    //
        addToLog(
          "    17.2. Test findConceptsByCode(Code) ... "
          + date_format.format(timestamp));
        Code code = new Code(string);
        Concept[] concepts = client.findConceptsByCode(code);
        for (int i = 0; i < concepts.length; i++) {
          addToLog("\t\t" + concepts[i].getIdentifier().toString()
            + " - " + concepts[i].getPreferredAtom().toString());
          if (i > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

      } else if (finder.equals("exact_string")) {
        //
  	    // 17.3. Test findExactStringMatches(String)
  	    //
        addToLog(
          "    17.3. Test findExactStringMatches(String) ... "
          + date_format.format(timestamp));
        Concept[] concepts = client.findExactStringMatches(string);
        for (int i = 0; i < concepts.length; i++) {
          addToLog("\t\t" + concepts[i].getIdentifier().toString()
            + " - " + concepts[i].getPreferredAtom().toString());
          if (i > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

      } else if (finder.equals("norm_string")) {
        //
  	    // 17.4. Test findNormalizedStringMatches(String)
  	    //
        addToLog(
          "    17.4. Test findNormalizedStringMatches(String) ... "
          + date_format.format(timestamp));
        Concept[] concepts = client.findNormalizedStringMatches(string);
        addToLog("\tTesting findNormalizedStringMatches(String) ... " + new java.util.Date());
        for (int i = 0; i < concepts.length; i++) {
          addToLog("\t\t" + concepts[i].getIdentifier().toString()
            + " - " + concepts[i].getPreferredAtom().toString());
          if (i > 5) {
            addToLog(
                "          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

      } else if (finder.equals("all_word")) {
        //
  	    // 17.5. Test findAllWordMatches(String[])
  	    //
        addToLog(
          "    17.5. Test findAllWordMatches(String[]) ... "
          + date_format.format(timestamp));

        for (int i=0; i<words.length; i++) {
          addToLog("words["+i+"]==" + words[i]);
        }
        Concept[] concepts = client.findAllWordMatches(words);
        for (int i = 0; i < concepts.length; i++) {
          addToLog("\t\t" + concepts[i].getIdentifier().toString()
            + " - " + concepts[i].getPreferredAtom().toString());
          if (i > 5) {
            addToLog(
                "          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

      } else if (finder.equals("all_norm_word")) {
        //
  	    // 17.6. Test findAllNormalizedWordMatches(String[])
  	    //
        addToLog(
          "    17.6. Test findAllNormalizedWordMatches(String[]) ... "
          + date_format.format(timestamp));

        for (int i=0; i<words.length; i++) {
          addToLog("words["+i+"]==" + words[i]);
        }
        Concept[] concepts = client.findAllNormalizedWordMatches(words);
        for (int i = 0; i < concepts.length; i++) {
          addToLog("\t\t" + concepts[i].getIdentifier().toString()
            + " - " + concepts[i].getPreferredAtom().toString());
          if (i > 5) {
            addToLog(
                "          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

      } else if (finder.equals("any_word")) {
        //
  	    // 17.7. Test findAnyWordMatches(String[])
  	    //
        addToLog(
          "    17.7. Test findAnyWordMatches(String[]) ... "
          + date_format.format(timestamp));

        for (int i=0; i<words.length; i++) {
          addToLog("words["+i+"]==" + words[i]);
        }
        Concept[] concepts = client.findAnyWordMatches(words);
        for (int i = 0; i < concepts.length; i++) {
          addToLog("\t\t" + concepts[i].getIdentifier().toString()
            + " - " + concepts[i].getPreferredAtom().toString());
          if (i > 5) {
            addToLog(
                "          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

      } else if (finder.equals("any_norm_word")) {
        //
  	    // 17.8. Test findAnyNormalizedWordMatches(String[])
  	    //
        addToLog(
          "    17.8. Test findAnyNormalizedWordMatches(String[]) ... "
          + date_format.format(timestamp));

        for (int i=0; i<words.length; i++) {
          addToLog("words["+i+"]==" + words[i]);
        }
        Concept[] concepts = client.findAnyNormalizedWordMatches(words);
        for (int i = 0; i < concepts.length; i++) {
          addToLog("\t\t" + concepts[i].getIdentifier().toString()
            + " - " + concepts[i].getPreferredAtom().toString());
          if (i > 5) {
            addToLog(
                "          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
      }
      
      //
	    // 18.1. Test clearRestrictions()
	    //
      addToLog(
          "    18.1. Test clearRestrictions() ... "
          + date_format.format(timestamp));
      client.clearRestrictions();

    } catch (Exception e) {
      MEMEException me = new MEMEException("Test Failed.", e);
      thisTestFailed();
      addToLog(me);
      me.setPrintStackTrace(true);
      me.printStackTrace();
    }

    addToLog("");

    if (this.isPassed())
      addToLog("    All tests passed");
    else
      addToLog("    At least one test did not complete successfully");

    //
    // Main Footer
    //

    addToLog("");

    addToLog("-------------------------------------------------------");
    addToLog("Finished FinderClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}