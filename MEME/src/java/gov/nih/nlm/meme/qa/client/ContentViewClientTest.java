/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  ContentViewClientTest
 *
 * 03/22/2007 BAC (1-D0BIJ): set algorithm for test of generateContentViewMembers.
 * 06/19/2006 RBE (1-BIC23): Bug fixes
 * 01/30/2006 RBE (1-763IU): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.client.ContentViewClient;
import gov.nih.nlm.meme.common.ATUI;
import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ContentView;
import gov.nih.nlm.meme.common.ContentViewMember;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.RUI;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Content View
 */
public class ContentViewClientTest extends TestSuite {

  public ContentViewClientTest() {
    setName("ContentViewClientTest");
    setDescription("Test Suite for Content View");
  }

  /**
   * Perform Test Suite Content View
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    ContentViewClient client = null;

    try {
      client = new ContentViewClient("");

	    //
	    // 1.1. Test setMidService(String), getMidService()
	    //
	    addToLog("    1.1. Test setMidService(String), "
        + "getMidService() ... "
        + date_format.format(timestamp));
	
	    client.setMidService("");
	    if (client.getMidService().equals(""))
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
      // 3.0. ContentView
      //
      ContentView cv = new ContentView.Default();

      //
      // 3.1. Test ContentView: setIdentifier(Identifier), getIdentifier()
      //
      addToLog(
          "    3.1. Test ContentView: setIdentifier(Identifier), getIdentifier() ... "
          + date_format.format(timestamp));
      Identifier id = new Identifier.Default(123456);
      cv.setIdentifier(id);
      if (cv.getIdentifier().equals(id))
        addToLog("    3.1. Test Passed");
      else {
        addToLog("    3.1. Test Failed");
        thisTestFailed();
      }

      //
      // 3.2. Test ContentView: setContributor(String), getContributor()
      //
      addToLog(
          "    3.2. Test ContentView: setContributor(String), getContributor() ... "
          + date_format.format(timestamp));
      cv.setContributor("MTH");
      if (cv.getContributor().equals("MTH"))
        addToLog("    3.2. Test Passed");
      else {
        addToLog("    3.2. Test Failed");
        thisTestFailed();
      }

      //
      // 3.3. Test ContentView: setContributorVersion(String), getContributorVersion()
      //
      addToLog(
          "    3.3. Test ContentView: setContributorVersion(String), getContributorVersion() ... "
          + date_format.format(timestamp));
      String cont_ver = "1.3.0";
      cv.setContributorVersion(cont_ver);
      if (cv.getContributorVersion().equals(cont_ver))
        addToLog("    3.3. Test Passed");
      else {
        addToLog("    3.3. Test Failed");
        thisTestFailed();
      }

      //
      // 3.4. Test ContentView: setContributorDate(Date), getContributorDate()
      //
      addToLog(
          "    3.4. Test ContentView: setContributorDate(Date), getContributorDate() ... "
          + date_format.format(timestamp));
      java.util.Date cont_date = new java.util.Date();
      cv.setContributorDate(cont_date);
      if (cv.getContributorDate().equals(cont_date))
        addToLog("    3.4. Test Passed");
      else {
        addToLog("    3.4. Test Failed");
        thisTestFailed();
      }

      //
      // 3.5. Test ContentView: setMaintainer(String), getMaintainer()
      //
      addToLog(
          "    3.5. Test ContentView: setMaintainer(String), getMaintainer() ... "
          + date_format.format(timestamp));
      cv.setMaintainer("MTH");
      if (cv.getMaintainer().equals("MTH"))
        addToLog("    3.5. Test Passed");
      else {
        addToLog("    3.5. Test Failed");
        thisTestFailed();
      }

      //
      // 3.6. Test ContentView: setMaintainerVersion(String), getMaintainerVersion()
      //
      addToLog(
          "    3.6. Test ContentView: setMaintainerVersion(String), getMaintainerVersion() ... "
          + date_format.format(timestamp));
      String maint_ver = "1.6.0";
      cv.setMaintainerVersion(maint_ver);
      if (cv.getMaintainerVersion().equals(maint_ver))
        addToLog("    3.6. Test Passed");
      else {
        addToLog("    3.6. Test Failed");
        thisTestFailed();
      }

      //
      // 3.7. Test ContentView: setMaintainerDate(Date), getMaintainerDate()
      //
      addToLog(
          "    3.7. Test ContentView: setMaintainerDate(Date), getMaintainerDate() ... "
          + date_format.format(timestamp));
      java.util.Date maint_date = new java.util.Date();
      cv.setMaintainerDate(maint_date);
      if (cv.getMaintainerDate().equals(maint_date))
        addToLog("    3.7. Test Passed");
      else {
        addToLog("    3.7. Test Failed");
        thisTestFailed();
      }

      //
      // 3.8. Test ContentView: setName(String), getName()
      //
      addToLog(
          "    3.8. Test ContentView: setName(String), getName() ... "
          + date_format.format(timestamp));
      cv.setName("CONTENT VIEW 1");
      if (cv.getName().equals("CONTENT VIEW 1"))
        addToLog("    3.8. Test Passed");
      else {
        addToLog("    3.8. Test Failed");
        thisTestFailed();
      }

      //
      // 3.9. Test ContentView: setDescription(String), getDescription()
      //
      addToLog(
          "    3.9. Test ContentView: setDescription(String), getDescription() ... "
          + date_format.format(timestamp));
      String desc = "CONTENT VIEW FINAL DESCRIPTION";
      cv.setDescription(desc);
      if (cv.getDescription().equals(desc))
        addToLog("    3.9. Test Passed");
      else {
        addToLog("    3.9. Test Failed");
        thisTestFailed();
      }

      //
      // 3.10. Test ContentView: setAlgorithm(String), getAlgorithm()
      //
      addToLog(
          "    3.10. Test ContentView: setAlgorithm(String), getAlgorithm() ... "
          + date_format.format(timestamp));
      String algo = "SELECT aui as meta_ui FROM classes WHERE rownum < 10";
      cv.setAlgorithm(algo);
      if (cv.getAlgorithm().equals(algo))
        addToLog("    3.10. Test Passed");
      else {
        addToLog("    3.10. Test Failed");
        thisTestFailed();
      }

      //
      // 3.11. Test ContentView: setCategory(String), getCategory()
      //
      addToLog(
          "    3.11. Test ContentView: setCategory(String), getCategory() ... "
          + date_format.format(timestamp));
      String category = "CATEGORY SYSTEM";
      cv.setCategory(category);
      if (cv.getCategory().equals(category))
        addToLog("    3.11. Test Passed");
      else {
        addToLog("    3.11. Test Failed");
        thisTestFailed();
      }

      //
      // 3.12. Test ContentView: setSubCategory(String), getSubCategory()
      //
      addToLog(
          "    3.12. Test ContentView: setSubCategory(String), getSubCategory() ... "
          + date_format.format(timestamp));
      String subcategory = "SUB CATEGORY SYSTEM";
      cv.setSubCategory(subcategory);
      if (cv.getSubCategory().equals(subcategory))
        addToLog("    3.12. Test Passed");
      else {
        addToLog("    3.12. Test Failed");
        thisTestFailed();
      }

      //
      // 3.13. Test ContentView: setContentViewClass(String), getContentViewClass()
      //
      addToLog(
          "    3.13. Test ContentView: setContentViewClass(String), getContentViewClass() ... "
          + date_format.format(timestamp));
      String cv_class = "THIS.CLASS";
      cv.setContentViewClass(cv_class);
      if (cv.getContentViewClass().equals(cv_class))
        addToLog("    3.13. Test Passed");
      else {
        addToLog("    3.13. Test Failed");
        thisTestFailed();
      }

      //
      // 3.14. Test ContentView: setCode(long), getCode()
      //
      addToLog(
          "    3.14. Test ContentView: setCode(long), getCode() ... "
          + date_format.format(timestamp));
      cv.setCode(123);
      if (cv.getCode() == 123)
        addToLog("    3.14. Test Passed");
      else {
        addToLog("    3.14. Test Failed");
        thisTestFailed();
      }

      //
      // 3.15. Test ContentView: setCascade(boolean), getCascade()
      //
      addToLog(
          "    3.15. Test ContentView: setCascade(boolean), getCascade() ... "
          + date_format.format(timestamp));
      cv.setCascade(false);
      if (!cv.getCascade())
        addToLog("    3.15. Test Passed");
      else {
        addToLog("    3.15. Test Failed");
        thisTestFailed();
      }

      //
      // 3.16. Test ContentView: setIsGeneratedByQuery(boolean), isGeneratedByQuery()
      //
      addToLog(
          "    3.16. Test ContentView: setIsGeneratedByQuery(boolean), isGeneratedByQuery() ... "
          + date_format.format(timestamp));
      cv.setIsGeneratedByQuery(true);
      if (cv.isGeneratedByQuery())
        addToLog("    3.16. Test Passed");
      else {
        addToLog("    3.16. Test Failed");
        thisTestFailed();
      }

      //
      // 3.17. Test ContentView: setPreviousMeta(String), getPreviousMeta()
      //
      addToLog(
          "    3.17. Test ContentView: setPreviousMeta(String), getPreviousMeta() ... "
          + date_format.format(timestamp));
      cv.setPreviousMeta("MetaMap");
      if (cv.getPreviousMeta().equals("MetaMap"))
        addToLog("    3.17. Test Passed");
      else {
        addToLog("    3.17. Test Failed");
        thisTestFailed();
      }

      //
      // 3.18. Test ContentView: setContributorURL(String), getContributorURL()
      //
      addToLog(
          "    3.18. Test ContentView: setContributorURL(String), getContributorURL() ... "
          + date_format.format(timestamp));
      cv.setContributorURL("mth");
      if (cv.getContributorURL().equals("mth"))
        addToLog("    3.18. Test Passed");
      else {
        addToLog("    3.18. Test Failed");
        thisTestFailed();
      }

      //
      // 3.19. Test ContentView: setMaintainerURL(String), getMaintainerURL()
      //
      addToLog(
          "    3.19. Test ContentView: setMaintainerURL(String), getMaintainerURL() ... "
          + date_format.format(timestamp));
      cv.setMaintainerURL("mth");
      if (cv.getMaintainerURL().equals("mth"))
        addToLog("    3.19. Test Passed");
      else {
        addToLog("    3.19. Test Failed");
        thisTestFailed();
      }

      //
      // ContentViewMember
      //
      ContentViewMember cvm = new ContentViewMember.Default();

      //
      // 4.10. Test ContentViewMember: setContentView(ContentView), getContentView()
      //
      addToLog(
          "    4.10. Test ContentViewMember: setContentView(ContentView), getContentView() ... "
          + date_format.format(timestamp));
      cvm.setContentView(cv);
      if (cvm.getContentView().equals(cv))
        addToLog("    4.10. Test Passed");
      else {
        addToLog("    4.10. Test Failed");
        thisTestFailed();
      }

      //
      // 4.20. Test ContentViewMember: setIdentifier(Identifier), getIdentifier()
      //
      addToLog(
          "    4.20. Test ContentViewMember: setIdentifier(Identifier), getIdentifier() ... "
          + date_format.format(timestamp));
      Identifier cvm_id = new Identifier.Default("123456");
      cvm.setIdentifier(cvm_id);
      if (cvm.getIdentifier().equals(cvm_id))
        addToLog("    4.20. Test Passed");
      else {
        addToLog("    4.20. Test Failed");
        thisTestFailed();
      }

      //
      // 3.20. Test ContentView: addMember(ContentViewMember), getMembers()
      //
      addToLog(
          "    3.20. Test ContentView: addMember(ContentViewMember), getMembers() ... "
          + date_format.format(timestamp));
      cv.addMember(cvm);
      ContentViewMember[] members = cv.getMembers();
      ContentViewMember[] save_members = members;
      boolean found = false;
      for (int i = 0; i < members.length; i++) {
        if (members[i].getIdentifier().equals(cvm.getIdentifier())) {
          addToLog("    3.20. Test Passed");
          found = true;
          break;
        }
        else {
          addToLog("    3.20. Test Failed");
        }
      }
      if (!found)
        thisTestFailed();

      //
      // 3.21. Test ContentView: removeMember(ContentViewMember), getMembers()
      //
      addToLog(
          "    3.21. Test ContentView: removeMember(ContentViewMember), getMembers() ... "
          + date_format.format(timestamp));
      cv.removeMember(cvm);
      members = cv.getMembers();
      int ctr = 0;
      for (int i = 0; i < members.length; i++) {
        ctr++;
        if (members[i].getIdentifier() == null)
          addToLog("    3.21. Test Passed");
        else {
          addToLog("    3.21. Test Failed");
          thisTestFailed();
        }
      }
      if (ctr == 0)
        addToLog("    3.21. Test Passed");

      //
      // 3.22. Test ContentView: setMembers(ContentViewMember[]), getMembers()
      //
      addToLog(
          "    3.22. Test ContentView: setMembers(ContentViewMember[]), getMembers() ... "
          + date_format.format(timestamp));
      cv.setMembers(save_members);
      members = cv.getMembers();
      found = false;
      for (int i = 0; i < members.length; i++) {
        if (members[i].getIdentifier().equals(cvm.getIdentifier())) {
          addToLog("    3.22. Test Passed");
          found = true;
          break;
        }
        else {
          addToLog("    3.22. Test Failed");
        }
      }
      if (!found)
        thisTestFailed();

      //
      // 3.23. Test ContentView: isMember(AUI)
      //
      addToLog(
          "    3.23. Test ContentView: isMember(AUI) ... "
          + date_format.format(timestamp));
      cv.removeMember(cvm);
      AUI aui = new AUI("A0123456");
      Atom atom = new Atom.Default();
      atom.setAUI(aui);
      cvm.setContentView(cv);
      cvm.setIdentifier(atom.getAUI());
      cv.addMember(cvm);
      if (cv.isMember(atom))
        addToLog("    3.23. Test Passed");
      else {
        addToLog("    3.23. Test Failed");
        thisTestFailed();
      }

      //
      // 3.24. Test ContentView: isMember(ATUI)
      //
      addToLog(
          "    3.24. Test ContentView: isMember(ATUI) ... "
          + date_format.format(timestamp));
      cv.removeMember(cvm);
      ATUI atui = new ATUI("AT123456");
      Attribute attr = new Attribute.Default();
      attr.setATUI(atui);
      cvm.setContentView(cv);
      cvm.setIdentifier(attr.getATUI());
      cv.addMember(cvm);
      if (cv.isMember(attr))
        addToLog("    3.24. Test Passed");
      else {
        addToLog("    3.24. Test Failed");
        thisTestFailed();
      }

      //
      // 3.25. Test ContentView: isMember(CUI)
      //
      addToLog(
          "    3.25. Test ContentView: isMember(CUI) ... "
          + date_format.format(timestamp));
      cv.removeMember(cvm);
      CUI cui = new CUI("C0123456");
      Concept concept = new Concept.Default();
      concept.setCUI(cui);
      cvm.setContentView(cv);
      cvm.setIdentifier(concept.getCUI());
      cv.addMember(cvm);
      if (cv.isMember(concept))
        addToLog("    3.25. Test Passed");
      else {
        addToLog("    3.25. Test Failed");
        thisTestFailed();
      }

      //
      // 3.26. Test ContentView: isMember(RUI)
      //
      addToLog(
          "    3.26. Test ContentView: isMember(RUI) ... "
          + date_format.format(timestamp));
      cv.removeMember(cvm);
      RUI rui = new RUI("R0123456");
      Relationship rel = new Relationship.Default();
      rel.setRUI(rui);
      cvm.setContentView(cv);
      cvm.setIdentifier(rel.getRUI());
      cv.addMember(cvm);
      if (cv.isMember(rel))
        addToLog("    3.26. Test Passed");
      else {
        addToLog("    3.26. Test Failed");
        thisTestFailed();
      }

      //
      // 5.10. Test addContentView(ContentView), getContentViews(), getContentView(Identifier)
      //
      addToLog(
          "    5.10. Test addContentView(ContentView), getContentViews(), getContentView(Identifier) ... "
          + date_format.format(timestamp));

      client.addContentView(cv);
      ContentView[] cvs = client.getContentViews();
      Identifier cv_id = cvs[0].getIdentifier();
      if (client.getContentView(cv_id).getIdentifier().equals(cv_id))
        addToLog("    5.10. Test Passed");
      else {
        addToLog("    5.10. Test Failed");
        thisTestFailed();
      }

      //
      // 5.20. Test addContentViewMember(ContentViewMember), getContentViewMembers(ContentView)
      //
      addToLog(
          "    5.20. Test addContentViewMember(ContentViewMember), getContentViewMembers(ContentView) ... "
          + date_format.format(timestamp));

      client.addContentViewMember(cvm);
      ContentViewMember[] cvms = client.getContentViewMembers(cv,0,-1);
      if (cvms.length > 0) {
        if (cvms[0].getIdentifier().equals(cvm.getIdentifier()))
          addToLog("    5.20. Test Passed");
        else {
          addToLog("    5.20. Test Failed (id does not match)");
          thisTestFailed();
        }
      } else {
        addToLog("    5.20. Test Failed (no CV member)");
        thisTestFailed();
      }

      //
      // 5.30. Test generateContentViewMembers(), getContentViewMembers()
      //
      addToLog(
          "    5.30. Test generateContentViewMembers(), getContentViewMembers() ... "
          + date_format.format(timestamp));
      cv.setAlgorithm("SELECT aui as meta_ui FROM classes WHERE aui is not null AND rownum<110");
      client.setContentView(cv);
      client.generateContentViewMembers(cv);
      ContentViewMember[] cvms2 = client.getContentViewMembers(cv,0,-1);
      if (cvms2.length > 1)
        addToLog("    5.30. Test Passed");
      else {
        addToLog("    5.30. Test Failed");
        thisTestFailed();
      }

      //
      // 5.35. Test removeContentViewMember(ContentViewMember), getContentViewMembers()
      //
      addToLog(
          "    5.35. Test removeContentViewMember(ContentViewMember), getContentViewMembers() ... "
          + date_format.format(timestamp));

      client.removeContentViewMember(cvm);
      cvms = client.getContentViewMembers(cv,0,-1);
      found = false;
      for (int i=0; i<cvms.length; i++) {
      	if (cvms[i].equals(cvm)) {
      		found = true;
      	}
      }
      if (!found)
        addToLog("    5.35. Test Passed");
      else {
        thisTestFailed();
        addToLog("    5.35. Test Failed");
      }

      //
      // 5.40. Test removeContentViewMembers(ContentView), getContentViewMembers()
      //
      addToLog(
          "    5.40. Test removeContentViewMembers(ContentView), getContentViewMembers() ... "
          + date_format.format(timestamp));

      client.removeContentViewMembers(cv);
      cvms2 = client.getContentViewMembers(cv,0,-1);
      found = false;
      for (int i=0; i<cvms.length; i++) {
      	if (cvms[i].getIdentifier().equals(cv.getIdentifier())) {
      		found = true;
      	}
      }
      if (!found)
        addToLog("    5.40. Test Passed");
      else {
        thisTestFailed();
        addToLog("    5.40. Test Failed");
      }

      //
      // 5.45. Test removeContentViewMembers(ContentViewMember[]), getContentViewMembers()
      //
      addToLog(
          "    5.45. Test removeContentViewMembers(ContentViewMember[]), getContentViewMembers() ... "
          + date_format.format(timestamp));

      client.removeContentViewMembers(cvms2);
      cvms2 = client.getContentViewMembers(cv,0,-1);
      if (cvms2.length == 0)
        addToLog("    5.45. Test Passed");
      else {
        thisTestFailed();
        addToLog("    5.45. Test Failed");
      }

      //
      // 5.50. Test removeContentView(ContentView), getContentView(Identifier)
      //
      addToLog(
          "    5.50. Test removeContentView(ContentView), getContentView(Identifier) ... "
          + date_format.format(timestamp));

      cv.setIdentifier(cv_id);
      client.removeContentView(cv);
      if (client.getContentView(cv_id).getIdentifier() == null)
        addToLog("    5.50. Test Passed");
      else {
        addToLog("    5.50. Test Failed");
        thisTestFailed();
      }

    } catch (MEMEException e) {
      thisTestFailed();
      addToLog(e);
      e.setPrintStackTrace(true);
      e.printStackTrace();
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
    addToLog("Finished ContentViewClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}