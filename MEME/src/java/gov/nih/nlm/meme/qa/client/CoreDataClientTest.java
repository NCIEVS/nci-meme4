/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  CoreDataClientTest
 *
 * 03/13/2006 RBE (1-A07F5): Changes to not use harcoded values
 * 01/30/2006 RBE (1-763IU): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.client.CoreDataClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Core Data
 */
public class CoreDataClientTest extends TestSuite {

  public CoreDataClientTest() {
    setName("CoreDataClientTest");
    setDescription("Test Suite for Core Data");
  }

  /**
   * Perform Test Suite Core Data
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    CoreDataClient client = null;

    try {
      client = new CoreDataClient("apelon");

	    //
	    // 1.1.1. Test setMidService(String), getMidService()
	    //
	    addToLog("    1.1.1. Test setMidService(String), "
        + "getMidService() ... "
        + date_format.format(timestamp));
	
	    client.setMidService("apelon");
	    if (client.getMidService().equals("apelon"))
	      addToLog("    1.1.1. Test Passed");
	    else {
	      addToLog("    1.1.1. Test Failed");
	      thisTestFailed();
	    }

	    //
	    // 2.1.1. Test setSessionId(String), getSessionId()
	    //
      addToLog("    2.1.1. Test setSessionId(String), "
        + "getSessionId() ... "
        + date_format.format(timestamp));

      client.setSessionId(null);
      if (client.getSessionId() == null)
        addToLog("    2.1.1. Test Passed");
      else {
        addToLog("    2.1.1. Test Failed");
        thisTestFailed();
      }
      
      addToLog(
          "    3.1.1. Test getConcept(Concept) ... "
          + date_format.format(timestamp));
      Concept concept = new Concept.Default(101);
      if (client.getConcept(concept).equals(concept))
        addToLog("    3.1.1. Test Passed");
      else {
        addToLog("    3.1.1. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.1.2. Test getConcept(Identifier) ... "
          + date_format.format(timestamp));
      if (client.getConcept(concept).getIdentifier().equals(concept.getIdentifier()))
        addToLog("    3.1.2. Test Passed");
      else {
        addToLog("    3.1.2. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.1.3. Test getConcept(int) ... "
          + date_format.format(timestamp));
      if (client.getConcept(concept).getIdentifier().intValue() == concept.getIdentifier().intValue())
        addToLog("    3.1.3. Test Passed");
      else {
        addToLog("    3.1.3. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.1.4. Test getConcept(CUI) ... "
          + date_format.format(timestamp));
      concept.setCUI(new CUI("C0000005"));
      if (client.getConcept(concept.getCUI()).getCUI().equals(concept.getCUI()))
        addToLog("    3.1.4. Test Passed");
      else {
        addToLog("    3.1.4. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.2.1. Test getAtoms(Concept) ... "
          + date_format.format(timestamp));
      Atom[] atoms = client.getAtoms(concept);
      for (int i=0; i<atoms.length; i++) {
        addToLog("           Atom["+i+"] = " + atoms[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }     
      addToLog(
          "    3.2.2. Test getAtom(Atom) ... "
          + date_format.format(timestamp));
      if (client.getAtom(atoms[0]).equals(atoms[0]))
        addToLog("    3.2.2. Test Passed");
      else {
        addToLog("    3.2.2. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.2.3. Test getAtom(Identifier) ... "
          + date_format.format(timestamp));
      if (client.getAtom(atoms[0]).getIdentifier().equals(atoms[0].getIdentifier()))
        addToLog("    3.2.3. Test Passed");
      else {
        addToLog("    3.2.3. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.2.4. Test getAtom(int) ... "
          + date_format.format(timestamp));
      if (client.getAtom(atoms[0]).getIdentifier().intValue() == atoms[0].getIdentifier().intValue())
        addToLog("    3.2.4. Test Passed");
      else {
        addToLog("    3.2.4. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.3.1. Test getAttributes(Concept) ... "
          + date_format.format(timestamp));
      Attribute[] attrs = client.getAttributes(client.getConcept(1515));
      for (int i=0; i<attrs.length; i++) {
        addToLog("           Attribute["+i+"] = " + attrs[i].getName());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }
      addToLog(
          "    3.3.2. Test getAttribute(Attribute) ... "
          + date_format.format(timestamp));
      if (client.getAttribute(attrs[0]).equals(attrs[0]))
        addToLog("    3.3.2. Test Passed");
      else {
        addToLog("    3.3.2. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.3.3. Test getAttribute(int) ... "
          + date_format.format(timestamp));
      if (client.getAttribute(attrs[0]).getIdentifier().intValue() == attrs[0].getIdentifier().intValue())
        addToLog("    3.3.3. Test Passed");
      else {
        addToLog("    3.3.3. Test Failed");
        thisTestFailed();
      }
      /*
      addToLog(
          "    3.4.1. Test getDeadAtom(int) ... "
          + date_format.format(timestamp));
      Atom dead_atom = new Atom.Default(26187421);
      if (client.getDeadAtom(26187421).equals(dead_atom))
        addToLog("    3.4.1. Test Passed");
      else {
        addToLog("    3.4.1. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.5.1. Test getDeadAttribute(int) ... "
          + date_format.format(timestamp));
      Attribute dead_attr = new Attribute.Default(52404821);
      if (client.getDeadAttribute(52404821).equals(dead_attr))
        addToLog("    3.5.1. Test Passed");
      else {
        addToLog("    3.5.1. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.6.1. Test getDeadConcept(int) ... "
          + date_format.format(timestamp));
      Concept dead_concept = new Concept.Default(1682989);
      if (client.getDeadConcept(1682989).equals(dead_concept))
        addToLog("    3.6.1. Test Passed");
      else {
        addToLog("    3.6.1. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.7.1. Test getDeadRelationship(int) ... "
          + date_format.format(timestamp));
      Relationship dead_rel = new Relationship.Default(51012127);
      if (client.getDeadRelationship(51012127).equals(dead_rel))
        addToLog("    3.7.1. Test Passed");
      else {
        addToLog("    3.7.1. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.8.1. Test getDeadContextRelationship(int) ... "
          + date_format.format(timestamp));
      ContextRelationship dead_ctx_rel = new ContextRelationship.Default(60672723);
      if (client.getDeadRelationship(60672723).equals(dead_ctx_rel))
        addToLog("    3.8.1. Test Passed");
      else {
        addToLog("    3.8.1. Test Failed");
        thisTestFailed();
      }
      addToLog(
          "    3.9.1. Test getInverseRelationship(int) ... "
          + date_format.format(timestamp));
      Relationship inverse_rel = new Relationship.Default(58366535);
      if (client.getInverseRelationship(58366535).equals(inverse_rel))
        addToLog("    3.9.1. Test Passed");
      else {
        addToLog("    3.9.1. Test Failed");
       thisTestFailed();
      }
      addToLog(
         "    3.10.1. Test getRelationship(int) ... "
         + date_format.format(timestamp));
      Relationship rel = new Relationship.Default(1034);
      if (client.getRelationship(1034).equals(rel))
        addToLog("    3.10.1. Test Passed");
      else {
        addToLog("    3.10.1. Test Failed");
        thisTestFailed();
      }
      addToLog(
         "    3.11.1. Test getRelationship(Relationship) ... "
         + date_format.format(timestamp));
      rel = new Relationship.Default(1034);
      if (client.getRelationship(rel).equals(rel))
        addToLog("    3.11.1. Test Passed");
      else {
        addToLog("    3.11.1. Test Failed");
       thisTestFailed();
      }
      */
      addToLog(
         "    3.12.1. Test getRelationshipCount(Concept) ... "
         + date_format.format(timestamp));
      int count = client.getRelationshipCount(concept);
      if (count > 0)
         addToLog("    3.12.1. Count="+count);
      else
         addToLog("    3.12.1. No relationship found on concept ("+concept.getIdentifier().intValue()+")");

      addToLog(
         "    3.13.1. Test getRelationships(Concept) ... "
         + date_format.format(timestamp));
      Relationship[] rels = client.getRelationships(concept);
      if (rels.length > 0) {
         for (int i=0; i < rels.length; i++) {
           addToLog("            rels["+i+"]= " + rels[i].getName());
           if (i > 5) {
             addToLog("          >>> Loop terminated. Only few records displayed.");
             break;
           }
         }
      } else
        addToLog("    3.13.1. No relationship found on concept ("+concept.getIdentifier().intValue()+")");

      client.populateRelationships(concept);
      
      addToLog(
         "    3.14.1. Test getRelationships(Concept, int, int) ... "
         + date_format.format(timestamp));
      rels = client.getRelationships(concept, 1, 3);
      if (rels.length > 0) {
         for (int i=0; i < rels.length; i++) {
           addToLog("            rels["+i+"]= " + rels[i].getName());
           if (i > 5) {
             addToLog("          >>> Loop terminated. Only few records displayed.");
             break;
           }
         }
      } else
         addToLog("    3.14.1. No relationship found on concept ("+concept.getIdentifier().intValue()+")");
           
      client.populateRelationships(concept, 1, 3);
      
      
      addToLog(
          "    3.15.1. Test getContextRelationships(Concept) ... "
          + date_format.format(timestamp));
       ContextRelationship[] cxt_rels = client.getContextRelationships(concept);
       if (cxt_rels.length > 0) {
          for (int i=0; i < cxt_rels.length; i++) {
            addToLog("            cxt_rels["+i+"]= " + cxt_rels[i].getName());
            if (i > 5) {
              addToLog("          >>> Loop terminated. Only few records displayed.");
              break;
            }
          }
       } else
          addToLog("    3.15.1. No context relationship found on concept ("+concept.getIdentifier().intValue()+")");

      client.populateContextRelationships(concept);
      
      if (cxt_rels.length > 0) {
        addToLog(
            "    3.15.2. Test getContextRelationship(int) ... "
            + date_format.format(timestamp));

      	if (client.getContextRelationship(cxt_rels[0].getIdentifier().intValue()).equals(cxt_rels[0]))
          addToLog("    3.15.2. Test Passed");
        else {
          addToLog("    3.15.2. Test Failed");
          thisTestFailed();
        }
      }
      
      addToLog(
          "    3.16.1. Test getContextRelationships(Concept, int, int) ... "
          + date_format.format(timestamp));

    	cxt_rels = client.getContextRelationships(concept, 1, 3);
      if (cxt_rels.length > 0) {
        for (int i=0; i < cxt_rels.length; i++) {
          addToLog("            cxt_rels["+i+"]= " + cxt_rels[i].getName());
          if (i > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
     } else
        addToLog("    3.16.1. No context relationship found on concept ("+concept.getIdentifier().intValue()+")");
     
      client.populateContextRelationships(concept, 1, 3);
      
      addToLog(
         "    3.17.1. Test getContextRelationshipCount(Concept) ... "
         + date_format.format(timestamp));

      count = client.getContextRelationshipCount(concept);
      if (count > 0)
         addToLog("    3.17.1. Count="+count);
      else
         addToLog("    3.17.1. No context relationship found on concept ("+concept.getIdentifier().intValue()+")");

           
      addToLog(
         "    3.18.1. Test setReadLanguagesToExclude(String[]), getLanguages() ... "
         + date_format.format(timestamp));
      client.setReadLanguagesToExclude(new String[] {"ENG"});
      String[] languages = client.getReadLanguages();
      if (languages.length > 0) {
         for (int i=0; i < languages.length; i++) {
           addToLog("            languages["+i+"]= " + languages[i]);
           if (i > 5) {
             addToLog("          >>> Loop terminated. Only few records displayed.");
             break;
           }
         }
         addToLog("    3.18.1. Test Passed");
      } else {
         addToLog("    3.18.1. Test Failed");
         thisTestFailed();
      }
      addToLog(
         "    3.19.1. Test includeOrExcludeLanguages() ... "
         + date_format.format(timestamp));
      if (!client.includeOrExcludeLanguages()) {
         addToLog("    3.19.1. Test Passed");
      } else {
         addToLog("    3.19.1. Test Failed");
         thisTestFailed();
      }
      addToLog(
         "    3.20.1. Test setReadLanguagesToInclude(String[]), getLanguages() ... "
         + date_format.format(timestamp));
      client.setReadLanguagesToInclude(new String[] {"SPA"});
      languages = client.getReadLanguages();
      if (languages.length > 0) {
         for (int i=0; i < languages.length; i++) {
           addToLog("            languages["+i+"]= " + languages[i]);
           if (i > 5) {
             addToLog("          >>> Loop terminated. Only few records displayed.");
             break;
           }
         }
         addToLog("    3.20.1. Test Passed");
      } else {
         addToLog("    3.20.1. Test Failed");
         thisTestFailed();
      }
      addToLog(
         "    3.21.1. Test includeOrExcludeLanguages() ... "
         + date_format.format(timestamp));
      if (client.includeOrExcludeLanguages()) {
         addToLog("    3.21.1. Test Passed");
      } else {
         addToLog("    3.21.1. Test Failed");
         thisTestFailed();
      }
    } catch (MEMEException me) {
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
    addToLog("Finished CoreDataClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}