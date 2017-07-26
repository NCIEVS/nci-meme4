/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  AuxiliaryDataClientTest
 * 
 * 03/13/2006 RBE (1-A07F5): Changes to not use harcoded values
 * 01/30/2006 RBE (1-763IU): File created
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.ISUI;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.LUI;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.MetaCode;
import gov.nih.nlm.meme.common.MetaProperty;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SUI;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Auxiliary Data
 */
public class AuxiliaryDataClientTest extends TestSuite {

  public AuxiliaryDataClientTest() {
    setName("AuxiliaryDataClientTest");
    setDescription("Test Suite for Auxiliary Data");
  }

  /**
   * Perform Test Suite Auxiliary Data
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    AdminClient admin_client = null;
    AuxiliaryDataClient client = null;

    try {
      admin_client = new AdminClient("apelon");
      client = new AuxiliaryDataClient("");

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
	    // 3.1. Test clearCache(), refreshCache()
	    //
      addToLog("    3.1. Test clearCache(), "
        + "refreshCache() ... "
        + date_format.format(timestamp));

      client.clearCache();
      client.refreshCache();

      //
      // 4.1. Test getSource(String)
      //      
      addToLog(
        "    4.1. Test getSource(String) ... "
        + date_format.format(timestamp));

      Source source = client.getSource("MTH");
      if (source != null) {
      	displaySource(source);
      } else {
        addToLog("    4.1. Test Failed");
        thisTestFailed();
      }
      
      //
      // 4.2. Test addSource(Source), getSource(String)
      //      
      addToLog(
        "    4.2. Test addSource(Source), getSource(String) ... "
        + date_format.format(timestamp));

      source.setSourceToOutrank(source);
      source.setInverter("MEME4 INVERTER");
      source.setSourceAbbreviation("MTH2");
      source.setNormalizedSourceAbbreviation("MTH2");
      source.setRootSourceAbbreviation("MTH2");
      source.setVersionedCui(new CUI("C000000"));
      source.setRootCui(new CUI("C000000"));

      client.addSource(source);

      source = client.getSource("MTH2");
      if (source != null) {
      	displaySource(source);
      } else {
        addToLog("    4.2. Test Failed");
        thisTestFailed();
      }

      //
      // 4.3. Test setSource(Source), getSource(String)
      //      
      addToLog(
        "    4.3. Test setSource(Source), getSource(String) ... "
        + date_format.format(timestamp));

      String official_name = source.getOfficialName();
      String restriction_level = source.getRestrictionLevel();

      source.setRestrictionLevel("1");
      source.setOfficialName("New Official Name");
      source.setVersionedCui(new CUI("C000001"));
      source.setRootCui(new CUI("C000001"));

      client.setSource(source);

      source = client.getSource("MTH2");
      if (source != null) {
      	displaySource(source);
      } else {
        addToLog("    4.3. Test Failed");
        thisTestFailed();
      }

      source.setRestrictionLevel(restriction_level);
      source.setOfficialName(official_name);
      source.setVersionedCui(new CUI("C000000"));
      source.setRootCui(new CUI("C000000"));

      client.setSource(source);

      //
      // 4.4. Test removeSource(Source)
      //      
      addToLog(
        "    4.4. Test removeSource(Source) ... "
        + date_format.format(timestamp));

      client.removeSource(source);

      //
      // 4.5. Test getSources()
      //      
      addToLog(
        "    4.5. Test getSources() ... "
        + date_format.format(timestamp));

      Source[] sources = client.getSources();
      for (int i=0; i<sources.length; i++) {
        addToLog("            Source["+i+"] = " + sources[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 4.6. Test getCurrentSource(String)
      //      
      addToLog(
        "    4.6. Test getCurrentSource(String) ... "
        + date_format.format(timestamp));

      Source current_source = client.getCurrentSource("MTH");
      if (current_source != null) {
      	displaySource(current_source);
      }

      //
      // 4.7. Test getPreviousSource(String)
      //      
      addToLog(
        "    4.7. Test getPreviousSource(String) ... "
        + date_format.format(timestamp));

      Source previous_source = client.getPreviousSource("MTH");
      if (previous_source != null) {
      	displaySource(previous_source);
      }

      //
      // 5.1. Test getTermgroup(String)
      //      
      addToLog(
        "    5.1. Test getTermgroup(String) ... "
        + date_format.format(timestamp));
        
      Termgroup old_tg = client.getTermgroup("MTH/PT");
      if (old_tg != null) {
      	displayTermgroup(old_tg);
      }
      
      Termgroup new_tg = new Termgroup.Default();
      new_tg.setNormExclude(old_tg.normExclude());
      new_tg.setNotes(old_tg.getNotes());
      new_tg.setRank(old_tg.getRank());
      new_tg.setReleaseRank(old_tg.getReleaseRank());
      new_tg.setSource(old_tg.getSource());
      new_tg.setSuppressible(old_tg.getSuppressible());
      new_tg.setTermgroupToOutrank(client.getTermgroup("MTH/PN"));
      new_tg.setTermType(old_tg.getTermType());

      //
      // 5.2. Test removeTermgroup(Termgroup)
      //      
      addToLog(
        "    5.2. Test removeTermgroup(Termgroup) ... "
        + date_format.format(timestamp));
      
      client.removeTermgroup(old_tg);

      //
      // 5.3. Test addTermgroup(Termgroup)
      //      
      addToLog(
        "    5.3. Test addTermgroup(Termgroup) ... "
        + date_format.format(timestamp));

      client.addTermgroup(new_tg);
      Termgroup tg = client.getTermgroup("MTH/PT");
      if (tg != null) {
      	displayTermgroup(tg);
      } else {
        addToLog("    5.3. Test Failed");
        thisTestFailed();
      }

      //
      // 5.4. Test setTermgroup(Termgroup)
      //      
      addToLog(
        "    5.4. Test setTermgroup(Termgroup) ... "
        + date_format.format(timestamp));
      
      client.setTermgroup(old_tg);
      tg = client.getTermgroup("MTH/PT");
      if (tg != null) {
      	displayTermgroup(tg);
      } else {
        addToLog("    5.4. Test Failed");
        thisTestFailed();
      }

      //
      // 5.5. Test addTermgroups(Termgroup[])
      //      
      addToLog(
        "    5.5. Test addTermgroups(Termgroup[]) ... "
        + date_format.format(timestamp));

      Termgroup[] tgs = new Termgroup[1];
      tgs[0] = tg;
      tgs[0].setTermgroupToOutrank(client.getTermgroup("MTH/PN"));
      
      client.addTermgroups(tgs);
      
      //
      // 5.6. Test getTermgroups()
      //      
      addToLog(
        "    5.6. Test getTermgroups() ... "
        + date_format.format(timestamp));
      
      tgs = client.getTermgroups();
      for (int i=0; i<tgs.length; i++) {
      	if (tgs[i] != null) {
          addToLog("            Termgroup: " + tgs[i].toString());
          if (i > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
      	}
      }

      //
      // 5.7. Test getTermgroupsAsStrings()
      //      
      addToLog(
        "    5.7. Test getTermgroupsAsStrings() ... "
        + date_format.format(timestamp));
      
      String[] str_tgs = client.getTermgroupsAsStrings();
      for (int i=0; i<str_tgs.length; i++) {
      	if (tgs[i] != null) {
          addToLog("            Termgroup: " + str_tgs[i]);
          if (i > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
      	}
      }

      //
      // 5.8. Test getTermgroup(String, String)
      //      
      addToLog(
        "    5.8. Test getTermgroup(String, String) ... "
        + date_format.format(timestamp));

      tg = client.getTermgroup("MTH", "PT");
      if (tg != null) {
      	displayTermgroup(tg);
      }

      //
      // 6.1. Test addLanguage(Language), getLanguage(String)
      //      
      addToLog(
        "    6.1. Test addLanguage(Language), getLanguage(String) ... "
        + date_format.format(timestamp));

      Language lang = new Language.Default("Mandarin", "MAN");
      lang.setISOAbbreviation("mn");
      client.addLanguage(lang);
      lang = client.getLanguage("MAN");
      if (lang != null) {
        addToLog("            Language: " + lang.toString());
      } else {
        addToLog("    6.1. Test Failed");
        thisTestFailed();
      }

      //
      // 6.2. Test setLanguage(Language), getLanguage(String)
      //      
      addToLog(
        "    6.2. Test setLanguage(Language), getLanguage(String) ... "
        + date_format.format(timestamp));

      lang = new Language.Default("Mandarin2", "MAN");
      client.setLanguage(lang);
      lang = client.getLanguage("MAN");
      if (lang != null) {
        addToLog("            Language.getAbbreviation: " + lang.getAbbreviation());
      } else {
        addToLog("    6.2. Test Failed");
        thisTestFailed();
      }

      //
      // 6.3. Test removeLanguage(Language)
      //      
      addToLog(
        "    6.3. Test removeLanguage(Language) ... "
        + date_format.format(timestamp));

      client.removeLanguage(lang);

      //
      // 6.4. Test getLanguages()
      //      
      addToLog(
        "    6.4. Test getLanguages() ... "
        + date_format.format(timestamp));

      Language[] languages = client.getLanguages();
      for (int i=0; i<languages.length; i++) {
        addToLog("            Languages["+i+"] = " + languages[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }
      
      //
      // 7.1. Test getMaxIdentifierForType(Class)
      //      
      addToLog(
        "    7.1. Test getMaxIdentifierForType(Class) ... "
        + date_format.format(timestamp));

      addToLog("            Atom.class= " + client.getMaxIdentifierForType(Atom.class));
      addToLog("            Concept.class= " + client.getMaxIdentifierForType(Concept.class));
      addToLog("            Atom.class= " + client.getMaxIdentifierForType(Atom.class));
      addToLog("            Relationship.class="+client.getMaxIdentifierForType(Relationship.class));
      addToLog("            Attribute.class="+client.getMaxIdentifierForType(Attribute.class));
      addToLog("            MolecularAction.class="+client.getMaxIdentifierForType(MolecularAction.class));
      addToLog("            AtomicAction.class="+client.getMaxIdentifierForType(AtomicAction.class));
      addToLog("            MolecularTransaction.class="+client.getMaxIdentifierForType(MolecularTransaction.class));
      addToLog("            CUI.class="+client.getMaxIdentifierForType(CUI.class).toString());
      addToLog("            LUI.class="+client.getMaxIdentifierForType(LUI.class).toString());
      addToLog("            SUI.class="+client.getMaxIdentifierForType(SUI.class).toString());
      addToLog("            ISUI.class="+client.getMaxIdentifierForType(ISUI.class).toString());

      //
      // 7.2. Test getNextIdentifierForType(Class)
      //      
      addToLog(
        "    7.2. Test getNextIdentifierForType(Class) ... "
        + date_format.format(timestamp));

      addToLog("            Atom.class="+client.getNextIdentifierForType(Atom.class));
      addToLog("            Concept.class="+client.getNextIdentifierForType(Concept.class));
      addToLog("            Relationship.class="+client.getNextIdentifierForType(Relationship.class));
      addToLog("            Attribute.class="+client.getNextIdentifierForType(Attribute.class));
      addToLog("            MolecularAction.class="+client.getNextIdentifierForType(MolecularAction.class));
      addToLog("            AtomicAction.class="+client.getNextIdentifierForType(AtomicAction.class));
      addToLog("            MolecularTransaction.class="+client.getNextIdentifierForType(MolecularTransaction.class));
      addToLog("            CUI.class="+client.getNextIdentifierForType(CUI.class).toString());
      addToLog("            LUI.class="+client.getNextIdentifierForType(LUI.class).toString());
      addToLog("            SUI.class="+client.getNextIdentifierForType(SUI.class).toString());
      addToLog("            ISUI.class="+client.getNextIdentifierForType(ISUI.class).toString());

      //
      // 8.1. Test getWorkLogs()
      //      
      addToLog(
        "    8.1. Test getWorkLogs() ... "
        + date_format.format(timestamp));

      WorkLog[] work_logs = client.getWorkLogs();
      for (int i = 0; i < work_logs.length; i++) {
        addToLog("            work id["+i+"] = " + work_logs[i].getIdentifier().intValue());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 8.2. Test getWorkLog(int)
      //      
      addToLog(
        "    8.2. Test getWorkLog(int) ... "
        + date_format.format(timestamp));
      
      if (client.getWorkLog(work_logs[0].getIdentifier().intValue()) != null) {
        addToLog("            work id = " + work_logs[0].getIdentifier().intValue());
        addToLog("    8.2. Test Passed");
      } else {
        addToLog("    8.2. Test Failed");
        thisTestFailed();
      }

      //
      // 8.3. Test getActivityLog(int)
      //      
      Activity[] activities = client.getActivityLogs(work_logs[0]);
      int transaction_id = 0;
      for (int i = 0; i < activities.length; i++) {
        transaction_id = activities[i].getIdentifier().intValue();
        if (transaction_id != 0) break;
      }
      addToLog(
        "    8.3. Test getActivityLog(" + transaction_id + ") ... " +
         date_format.format(timestamp));
      Activity activity = client.getActivityLog(new MolecularTransaction(transaction_id));
      if (activity != null) {
        addToLog("            Activity = " + activity.getShortDescription());
        addToLog("    8.3. Test Passed");
      } else {
        addToLog("    8.3. Test Failed");
        thisTestFailed();
      }

      //
      // 8.4. Test getActivityLogs(Identifier)
      //      
      addToLog(
        "    8.4. Test getActivityLogs(" + work_logs[0].getIdentifier() + ") ... " +
         date_format.format(timestamp));

      boolean found = false;
      for (int i = 0; i < activities.length; i++) {
        addToLog("            Activity ["+i+"] = " + activities[i].getShortDescription());
        found = true;
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }
      
      //
      // 9.1. Test addMetaCode(MetaCode), getMetaCodes()
      //      
      addToLog(
        "    9.1. Test addMetaCode(MetaCode), getMetaCodes() ... "
        + date_format.format(timestamp));

      Identifier id = new Identifier.Default("123456");
      MetaCode mcode = new MetaCode(id, "MEME_CODE", "meme4_code", "this is a test value for metacode.");
      client.addMetaCode(mcode);
      MetaCode[] mcodes = client.getMetaCodes();
      found = false;
      for (int i=0; i<mcodes.length; i++) {
        if (mcodes[i].getCode().equals(mcode.getCode())) {
          found = true;
          break;
        }
      }
      if (found)
        addToLog("    9.1. Test Passed");
      else {
        addToLog("    9.1. Test Failed");
        thisTestFailed();
      }

      //
      // 9.2. Test getMetaCodeTypes()
      //      
      addToLog(
        "    9.2. Test getMetaCodeTypes() ... "
        + date_format.format(timestamp));

      String[] types = client.getMetaCodeTypes();
      for (int i=0; i<types.length; i++) {
        addToLog("            Types["+i+"] = " + types[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 9.3. Test getMetaCodesByType(String)
      //      
      addToLog(
        "    9.3. Test getMetaCodesByType(String) ... "
        + date_format.format(timestamp));

      mcodes = client.getMetaCodesByType("meme4_code");
      for (int i=0; i<mcodes.length; i++) {
        addToLog("            MetaCode["+i+"] = " + mcodes[i].getCode());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 9.4. Test removeMetaCode(MetaCode)
      //      
      addToLog(
        "    9.4. Test removeMetaCode(MetaCode) ... "
        + date_format.format(timestamp));

      mcode = client.getMetaCode(mcode.getCode(), mcode.getType());
      client.removeMetaCode(mcode);
      mcodes = client.getMetaCodes();
      found = false;
      for (int i=0; i<mcodes.length; i++) {
        if (mcodes[i].getCode().equals(mcode.getCode())) {
          found = true;
          break;
        }
      }
      if (!found)
        addToLog("    9.4. Test Passed");
      else {
        addToLog("    9.4. Test Failed");
        thisTestFailed();
      }

      //
      // 9.5. Test addMetaProperty(MetaProperty), getMetaProperties()
      //      
      addToLog(
        "    9.5. Test addMetaProperty(MetaProperty), getMetaProperties() ... "
        + date_format.format(timestamp));

      id = new Identifier.Default("123456");
      MetaProperty meta_prop = new MetaProperty(id,
        "MEME_KEY", "M4_FILES", "test value", "test description",
        "test definition", "test example", "test reference");

      client.addMetaProperty(meta_prop);
      MetaProperty[] meta_props = client.getMetaProperties();
      found = false;
      for (int i=0; i<meta_props.length; i++) {
        if (meta_props[i].getKey().equals(meta_prop.getKey()) &&
            meta_props[i].getKeyQualifier().equals(meta_prop.getKeyQualifier()) &&
            meta_props[i].getValue().equals(meta_prop.getValue())) {
          found = true;
          break;
        }
      }
      if (found)
        addToLog("    9.5. Test Passed");
      else {
        addToLog("    9.5. Test Failed");
        thisTestFailed();
      }

      //
      // 9.6. Test getMetaPropertyKeyQualifiers()
      //      
      addToLog(
        "    9.6. Test getMetaPropertyKeyQualifiers() ... "
        + date_format.format(timestamp));

      String[] key_qualifiers = client.getMetaPropertyKeyQualifiers();
      for (int i=0; i<key_qualifiers.length; i++) {
        addToLog("            Key qualifiers["+i+"] = " + key_qualifiers[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 9.7. Test getMetaPropertiesByKeyQualifier(String)
      //      
      addToLog(
        "    9.7. Test getMetaPropertiesByKeyQualifier(String) ... "
        + date_format.format(timestamp));

      MetaProperty[] meme_props = client.getMetaPropertiesByKeyQualifier("M4_FILES");
      for (int i=0; i<meme_props.length; i++) {
        addToLog("            MetaProperty["+i+"] = " + meme_props[i].getKey());
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 9.8. Test getMetaProperty(String, String, String)
      //      
      addToLog(
        "    9.8. Test getMetaProperty(String, String, String) ... "
        + date_format.format(timestamp));

      MetaProperty meme_prop = client.getMetaProperty("MEME_KEY", "M4_FILES", "test value");
      if (meme_prop.getKey().equals("MEME_KEY") &&
          meme_prop.getKeyQualifier().equals("M4_FILES") &&
          meme_prop.getValue().equals("test value"))
        addToLog("    9.8. Test Passed");
      else {
        addToLog("    9.8. Test Failed");
        thisTestFailed();
      }

      //
      // 9.9. Test removeMetaProperty(MetaProperty)
      //      
      addToLog(
        "    9.9. Test removeMetaProperty(MetaProperty) ... "
        + date_format.format(timestamp));

      meta_prop = client.getMetaProperty(meta_prop.getKey(), meta_prop.getKeyQualifier(), meta_prop.getValue());
      client.removeMetaProperty(meta_prop);
      meta_props = client.getMetaProperties();
      found = false;
      for (int i=0; i<meta_props.length; i++) {
        if (meta_props[i].getKey().equals(meta_prop.getKey()) &&
            meta_props[i].getKeyQualifier().equals(meta_prop.getKeyQualifier()) &&
            meta_props[i].getValue().equals(meta_prop.getValue())) {
          found = true;
          break;
        }
      }
      if (!found)
        addToLog("    9.9. Test Passed");
      else {
        addToLog("    9.9. Test Failed");
        thisTestFailed();
      }

      //
      // 9.10. Test getCodeByValue(Type, Value)
      //      
      addToLog(
        "    9.10. Test getCodeByValue(Type, Value) ... "
        + date_format.format(timestamp));

      addToLog("            "+client.getCodeByValue("filter_type","designates a non-chemical sty."));

      //
      // 9.11. Test getValueByCode(Type, Code)
      //      
      addToLog(
        "    9.11. Test getValueByCode(Type, Code) ... "
        + date_format.format(timestamp));

      addToLog("            "+client.getValueByCode("filter_type","NON_CHEM_STY"));

      //
      // 10.1. Test getValidStatusValuesForType(Atom)
      //      
      addToLog(
        "    10.1. Test getValidStatusValuesForType(Atom) ... "
        + date_format.format(timestamp));

      char[] status = client.getValidStatusValuesForType(Atom.class);
      for (int i = 0; i < status.length; i++) {
        addToLog("            "+status[i]);
      }

      //
      // 10.2. Test getValidStatusValuesForType(Attribute)
      //      
      addToLog(
        "    10.2. Test getValidStatusValuesForType(Attribute) ... "
        + date_format.format(timestamp));

      status = client.getValidStatusValuesForType(Attribute.class);
      for (int i = 0; i < status.length; i++) {
        addToLog("            "+status[i]);
      }

      //
      // 10.3. Test getValidStatusValuesForType(Concept)
      //      
      addToLog(
        "    10.3. Test getValidStatusValuesForType(Concept) ... "
        + date_format.format(timestamp));

      status = client.getValidStatusValuesForType(Concept.class);
      for (int i = 0; i < status.length; i++) {
        addToLog("            "+status[i]);
      }

      //
      // 10.4. Test getValidStatusValuesForType(Relationship)
      //      
      addToLog(
        "    10.4. Test getValidStatusValuesForType(Relationship) ... "
        + date_format.format(timestamp));

      status = client.getValidStatusValuesForType(Relationship.class);
      for (int i = 0; i < status.length; i++) {
        addToLog("            "+status[i]);
      }

      //
      // 10.5. Test getValidStatusValuesForAtoms()
      //      
      addToLog(
        "    10.5. Test getValidStatusValuesForAtoms() ... "
        + date_format.format(timestamp));

      status = client.getValidStatusValuesForAtoms();
      for (int i = 0; i < status.length; i++) {
        addToLog("            "+status[i]);
      }

      //
      // 10.6. Test getValidStatusValuesForAttributes()
      //      
      addToLog(
        "    10.6. Test getValidStatusValuesForAttributes() ... "
        + date_format.format(timestamp));

      status = client.getValidStatusValuesForAttributes();
      for (int i = 0; i < status.length; i++) {
        addToLog("            "+status[i]);
      }

      //
      // 10.7. Test getValidStatusValuesForConcepts()
      //      
      addToLog(
        "    10.7. Test getValidStatusValuesForConcepts() ... "
        + date_format.format(timestamp));

      status = client.getValidStatusValuesForConcepts();
      for (int i = 0; i < status.length; i++) {
        addToLog("            "+status[i]);
      }

      //
      // 10.8. Test getValidStatusValuesForRelationships()
      //      
      addToLog(
        "    10.8. Test getValidStatusValuesForRelationships() ... "
        + date_format.format(timestamp));

      status = client.getValidStatusValuesForRelationships();
      for (int i = 0; i < status.length; i++) {
        addToLog("            "+status[i]);
      }

      //
      // 11.1. Test getValidLevelValuesForType(Relationship)
      //      
      addToLog(
        "    11.1. Test getValidLevelValuesForType(Class) ... "
        + date_format.format(timestamp));

      char[] level = client.getValidLevelValuesForType(Relationship.class);
      for (int i = 0; i < level.length; i++) {
        addToLog("            "+level[i]);
      }

      //
      // 11.2. Test getValidLevelValuesForType(Attribute)
      //      
      addToLog(
        "    11.2. Test getValidLevelValuesForType(Class) ... "
        + date_format.format(timestamp));

      level = client.getValidLevelValuesForType(Attribute.class);
      for (int i = 0; i < level.length; i++) {
        addToLog("            "+level[i]);
      }

      //
      // 11.3. Test getValidLevelValuesForRelationships()
      //      
      addToLog(
        "    11.3. Test getValidLevelValuesForRelationships() ... "
        + date_format.format(timestamp));

      level = client.getValidLevelValuesForRelationships();
      for (int i = 0; i < level.length; i++) {
        addToLog("            "+level[i]);
      }

      //
      // 11.4. Test getValidLevelValuesForAttributes()
      //      
      addToLog(
        "    11.4. Test getValidLevelValuesForAttributes() ... "
        + date_format.format(timestamp));

      level = client.getValidLevelValuesForAttributes();
      for (int i = 0; i < level.length; i++) {
        addToLog("            "+level[i]);
      }
        
      //
      // 12.1. Test getValidReleasedValues()
      //      
      addToLog(
        "    12.1. Test getValidReleasedValues() ... "
        + date_format.format(timestamp));

      char[] released = client.getValidReleasedValues();
      for (int i = 0; i < released.length; i++) {
        addToLog("            "+released[i]);
      }

      //
      // 12.2. Test getValidTobeleasedValues()
      //      
      addToLog(
        "    12.2. Test getValidTobereleasedValues() ... "
        + date_format.format(timestamp));

      char[] tobereleased = client.getValidTobereleasedValues();
      for (int i = 0; i < tobereleased.length; i++) {
        addToLog("            "+tobereleased[i]);
      }
        
      //
      // 13.1. Test getEditorPreferences()
      //      
      addToLog(
        "    13.1. Test getEditorPreferences() ... "
        + date_format.format(timestamp));

      EditorPreferences[] efs = client.getEditorPreferences();
      for (int i = 0; i < efs.length; i++) {
        addToLog("            getUserName()=" + efs[i].getUserName());
        addToLog("            getInitials()=" + efs[i].getInitials());
        addToLog("            getEditorLevel()=" + efs[i].getEditorLevel());
        addToLog("            isCurrent()=" + efs[i].isCurrent());
        addToLog("            getEditorGroup()=" + efs[i].getEditorGroup());
        addToLog("            showConcept()=" + efs[i].showConcept());
        addToLog("            showAtoms()=" + efs[i].showAtoms());
        addToLog("            showAttributes()" + efs[i].showAttributes());
        addToLog("            showRelationships()" + efs[i].showRelationships());
        break;
      }

      //
      // 13.2. Test addEditorPreferences(EditorPreferences), getEditorPreferencesByInitials(String)
      //      
      addToLog(
        "    13.2. Test addEditorPreferences(editorPreferences), getEditorPreferencesByInitials(String) ... "
        + date_format.format(timestamp));

      EditorPreferences add_ep = new EditorPreferences.Default();
      add_ep.setUserName("meme4");
      add_ep.setInitials("M4");
      add_ep.setEditorLevel(5);
      add_ep.setEditorGroup("NLM");
      add_ep.setIsCurrent(true);
      add_ep.setShowConcept(true);
      add_ep.setShowAtoms(true);
      add_ep.setShowAttributes(true);
      add_ep.setShowRelationships(true);
      client.addEditorPreferences(add_ep);

      EditorPreferences ep = client.getEditorPreferencesByUsername(add_ep.getUserName());
      if (ep != null)
        addToLog("    13.2. Test Passed");
      else {
        addToLog("    13.2. Test Failed");
        thisTestFailed();
      }

      //
      // 13.3. Test setEditorPreferences(EditorPreferences), getEditorPreferencesByInitials(String)
      //      
      addToLog(
        "    13.3. Test setEditorPreferences(editorPreferences), getEditorPreferencesByInitials(String) ... "
        + date_format.format(timestamp));

      String initials = add_ep.getInitials();
      ep = add_ep;
      ep.setInitials("M3");
      ep.setEditorLevel(3);
      ep.setEditorGroup("NLM");
      ep.setIsCurrent(true);
      ep.setShowConcept(false);
      ep.setShowAtoms(false);
      ep.setShowAttributes(false);
      ep.setShowRelationships(false);

      client.setEditorPreferences(ep);

      if (!ep.getInitials().equals(initials))
        addToLog("    13.3. Test Passed");
      else {
        addToLog("    13.3. Test Failed");
        thisTestFailed();
      }

      //
      // 13.4. Test removeEditorPreferences(EditorPreferences)
      //      
      addToLog(
        "    13.4. Test removeEditorPreferences(editorPreferences) ... "
        + date_format.format(timestamp));

      client.removeEditorPreferences(add_ep);

      ep = client.getEditorPreferencesByUsername(add_ep.getUserName());
      if (ep == null)
        addToLog("    13.4. Test Passed");
      else {
        addToLog("    13.4. Test Failed");
        thisTestFailed();
      }
      
      //
      // 14.1. Test getAuthority(String)
      //      
      addToLog(
        "    14.1. Test getAuthority(String) ... "
        + date_format.format(timestamp));
      
      addToLog("            "+client.getAuthority("AUTHOR"));

      //
      // 15.1. Test getMolecularAction(int)
      //

      addToLog(
        "    15.1. Test getMolecularAction(int) ... "
        + date_format.format(timestamp));

      addToLog("            "+client.getMolecularAction(45199494).getSourceIdentifier().toString());

      //
      // 15.2. Test getFullMolecularAction(int)
      //      
      addToLog(
        "    15.2. Test getFullMolecularAction(int) ... "
        + date_format.format(timestamp));

      addToLog("            "+client.getFullMolecularAction(45199494).getSourceIdentifier().toString());

      //
      // 15.3. Test getAtomicAction(int)
      //      
      addToLog(
        "    15.3. Test getAtomicAction(int) ... "
        + date_format.format(timestamp));

      addToLog("            "+client.getAtomicAction(337551382).getIdentifier().toString());

      //
      // 16.1. Test addApplicationVector(String, IntegrityVector)
      //      
      addToLog(
        "    16.1. Test addApplicationVector(String, IntegrityVector) ... "
        + date_format.format(timestamp));
      
      // BEGIN IC SYSTEM TEST
      admin_client.setSystemStatus("ic_system", "ON");

      // Create an IntegrityVector object to work with
      IntegrityVector new_vector = new IntegrityVector.Default();        
      client.addApplicationVector("IV1", new_vector);

      //
      // 16.2. Test addCheckToApplicationVector(String, IntegrityVector, String)
      //      
      addToLog(
        "    16.2. Test addCheckToApplicationVector(String, IntegrityCheck, String) ... "
        + date_format.format(timestamp));

      IntegrityCheck ic = new IntegrityCheck.Default();
      ic.setName("IC1");
      ic.setIsActive(true);
      ic.setIsFatal(true);
      ic.setShortDescription("Short description");
      ic.setDescription("Description of atom.");
      new_vector.addIntegrityCheck(ic, "E");
      client.addCheckToApplicationVector("IV1", ic, "E");

      //
      // 16.3. Test setApplicationVector(String, IntegrityVector)
      //      
      addToLog(
        "    16.3. Test setApplicationVector(String, IntegrityVector) ... "
        + date_format.format(timestamp));
      
      client.setApplicationVector("IV1", new_vector);
      IntegrityVector vector = client.getApplicationVector("IV1");
      if (vector != null)
        addToLog("    16.3. Test Passed");
      else {
        addToLog("    16.3. Test Failed");
        thisTestFailed();
      }

      //
      // 16.4. Test getApplicationVector(String)
      //      
      addToLog(
        "    16.4. Test getApplicationVector(String) ... "
        + date_format.format(timestamp));
       
      vector = client.getApplicationVector("IV1");
      if (vector != null)
        addToLog("    16.4. Test Passed");
      else {
        addToLog("    16.4. Test Failed");
        thisTestFailed();
      }

      //
      // 16.5. Test getApplicationsWithVectors()
      //      
      addToLog(
        "    16.5. Test getApplicationsWithVectors() ... "
        + date_format.format(timestamp));
      
      String[] vectors = client.getApplicationsWithVectors();
      for (int i=0; i<vectors.length; i++) {
        addToLog("            vectors[i]: " + vectors[i]);      	
      }
      
      //
      // 16.6. Test removeCheckFromApplicationVector(String, IntegrityCheck)
      //      
      addToLog(
        "    16.6. Test removeCheckFromApplicationVector(String, IntegrityCheck) ... "
        + date_format.format(timestamp));

      IntegrityCheck ic2 = client.getIntegrityCheck("IC1");
      client.removeCheckFromApplicationVector("IV1", ic2);
      if (client.getIntegrityCheck("IC1") == null)
        addToLog("    16.6. Test Passed");
      else {
        addToLog("    16.6. Test Failed");
        thisTestFailed();
      }

      //
      // 16.7. Test removeApplicationVector(String)
      //      
      addToLog(
        "    16.7. Test removeApplicationVector(String) ... "
        + date_format.format(timestamp));

      client.removeApplicationVector("IV1");

      //
      // 16.8. Test getApplicationsWithVectors()
      //      
      addToLog(
        "    16.8. Test getApplicationsWithVectors() ... "
        + date_format.format(timestamp));
        
      vectors = client.getApplicationsWithVectors();
      for (int i=0; i<vectors.length; i++) {
        addToLog("            vectors[i]: " + vectors[i]);      	
      }

      //
      // 17.1. Test addOverrideVector(int, IntegrityVector)
      //      
      addToLog(
        "    17.1. Test addOverrideVector(int, IntegrityVector) ... "
        + date_format.format(timestamp));

      // Create a IntegrityVector object to work with
      new_vector = new IntegrityVector.Default();
          
      client.addOverrideVector(2, new_vector);

      //
      // 17.2. Test addCheckToOverrideVector(int, IntegrityCheck, String)
      //      
      addToLog(
        "    17.2. Test addCheckToOverrideVector(int, IntegrityCheck, String) ... "
        + date_format.format(timestamp));

      ic = new IntegrityCheck.Default();
      ic.setName("IC1");
      ic.setIsActive(true);
      ic.setIsFatal(true);
      ic.setShortDescription("Short description");
      ic.setDescription("Description of atom.");
      new_vector.addIntegrityCheck(ic, "C");
      client.addCheckToOverrideVector(2, ic, "C");

      //
      // 17.3. Test setOverrideVector(int, IntegrityVector)
      //      
      addToLog(
        "    17.3. Test setOverrideVector(int, IntegrityVector) ... "
        + date_format.format(timestamp));
        
      client.setOverrideVector(2, new_vector);
      vector = client.getOverrideVector(2);
      if (vector != null)
        addToLog("    17.3. Test Passed");
      else {
        addToLog("    17.3. Test Failed");
        thisTestFailed();
      }

      //
      // 17.4. Test getOverrideVector(int)
      //      
      addToLog(
        "    17.4. Test getOverrideVector(int) ... "
        + date_format.format(timestamp));
         
      vector = client.getOverrideVector(2);
      if (vector != null)
        addToLog("    17.4. Test Passed");
      else {
        addToLog("    17.4. Test Failed");
        thisTestFailed();
      }

      //
      // 17.5. Test getLevelsWithOverrideVectors()
      //      
      addToLog(
        "    17.5. Test getLevelsWithOverrideVectors() ... "
        + date_format.format(timestamp));
        
      int[] levels = client.getLevelsWithOverrideVectors();
      for (int i=0; i<levels.length; i++) {
        addToLog("            levels[i]: " + levels[i]);      	
      }

      //
      // 17.6. Test removeCheckToOverrideVector(int, IntegrityCheck)
      //      
      addToLog(
        "    17.6. Test removeCheckToOverrideVector(int, IntegrityCheck) ... "
        + date_format.format(timestamp));
      
      ic2 = client.getIntegrityCheck("IC1");
      client.removeCheckFromOverrideVector(5, ic2);
      if (client.getIntegrityCheck("IC1") == null)
        addToLog("    17.6. Test Passed");
      else {
        addToLog("    17.6. Test Failed");
        thisTestFailed();
      }

      //
      // 17.7. Test removeOverrideVector(int)
      //      
      addToLog(
      "    17.7. Test removeOverrideVector(int) ... "
      + date_format.format(timestamp));

      client.removeOverrideVector(2);
      
      //
      // 17.8. Test getLevelsWithOverrideVectors()
      //      
      addToLog(
        "    17.8. Test getLevelsWithOverrideVectors() ... "
        + date_format.format(timestamp));
          
      levels = client.getLevelsWithOverrideVectors();
      for (int i=0; i<levels.length; i++) {
        addToLog("            levels[i]: " + levels[i]);      	
      }
      
      //
      // 18.1. Test getIntegrityCheck(String), removeIntegrityCheck(IntegrityCheck)
      //      
      addToLog(
        "    18.1. Test getIntegrityCheck(String), removeIntegrityCheck(IntegrityCheck) ... "
        + date_format.format(timestamp));

      IntegrityCheck old_check = null;
      IntegrityCheck new_check = new IntegrityCheck.Default("MGV_A2");
      old_check = client.getIntegrityCheck("MGV_A2");
      client.removeIntegrityCheck(old_check);

      //
      // 18.2. Test addIntegrityCheck(IntegrityCheck)
      //      
      addToLog(
        "    18.2. Test addIntegrityCheck(IntegrityCheck) ... "
        + date_format.format(timestamp));
      new_check.setName(old_check.getName());
      new_check.setDescription(old_check.getDescription());
      new_check.setIsActive(old_check.isActive());
      new_check.setIsFatal(old_check.isFatal());
      new_check.setShortDescription(old_check.getShortDescription());

      client.addIntegrityCheck(new_check);

      //
      // 18.3. Test activateIntegrityCheck(IntegrityCheck)
      //      
      addToLog(
        "    18.3. Test activateIntegrityCheck(IntegrityCheck) ... "
        + date_format.format(timestamp));

      client.activateIntegrityCheck(new_check);

      //
      // 18.4. Test deactivateIntegrityCheck(IntegrityCheck)
      //      
      addToLog(
        "    18.4. Test deactivateIntegrityCheck(IntegrityCheck) ... "
        + date_format.format(timestamp));

      client.deactivateIntegrityCheck(new_check);

      //
      // 18.5. Test setIntegrityCheck(IntegrityCheck)
      //      
      addToLog(
        "    18.5. Test setIntegrityCheck(IntegrityCheck) ... "
        + date_format.format(timestamp));
      new_check.setIsActive(false);
      new_check.setIsFatal(true);

      client.setIntegrityCheck(new_check);

      //
      // 18.6. Test removeIntegrityCheck(IntegrityCheck)
      //      
      addToLog(
        "    18.6. Test removeIntegrityCheck(IntegrityCheck) ... "
        + date_format.format(timestamp));

      client.removeIntegrityCheck(new_check);

      //
      // 18.7. Test addIntegrityCheck(IntegrityCheck)
      //      
      addToLog(
        "    18.7. Test addIntegrityCheck(IntegrityCheck) ... "
        + date_format.format(timestamp));

      client.addIntegrityCheck(old_check);

      // END IC SYSTEM TEST
      admin_client.setSystemStatus("ic_system", "OFF");

      //
      // 19.1. Test getValidRelationshipNames()
      //      
      addToLog(
        "    19.1. Test getValidRelationshipNames() ... "
        + date_format.format(timestamp));

      String[] rel_names = client.getValidRelationshipNames();
      for (int i = 0; i < rel_names.length; i++) {
        addToLog("            "+rel_names[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }

      //
      // 20.1. Test getValidRelationshipAttributes()
      //      
      addToLog(
        "    20.1. Test getValidRelationshipAttributes() ... "
        + date_format.format(timestamp));

      String[] rel_attrs = client.getValidRelationshipAttributes();
      for (int i = 0; i < rel_attrs.length; i++) {
        addToLog("            "+rel_attrs[i]);
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
      }
      
      //
      // 21.1. Test addValidSemanticType(SemanticType), getValidSemanticTypes()
      //      
      addToLog(
        "    21.1. Test addValidSemanticType(SemanticType), getValidSemanticTypes() ... "
        + date_format.format(timestamp));

      SemanticType new_sty = new SemanticType.Default();
      new_sty.setTypeIdentifier(new Identifier.Default(1234));
      new_sty.setValue("Alien");
      new_sty.setIsChemical(false);
      new_sty.setChemicalType(null);
      new_sty.setIsEditingChemical(false);
      client.addValidSemanticType(new_sty);
      SemanticType[] stys = client.getValidSemanticTypes();
      found = false;
      for (int i = 0; i < stys.length; i++) {
        if (stys[i].getValue().equals(new_sty.getValue())) {
          found = true;
          break;
        }
      }
      if (found)
        addToLog("    21.1. Test Passed");
      else {
        addToLog("    21.1. Test Failed");
        thisTestFailed();
      }

      //
      // 21.2. Test removeValidSemanticType(SemanticType)
      //      
      addToLog(
        "    21.2. Test removeValidSemanticType(SemanticType) ... "
        + date_format.format(timestamp));

      client.removeValidSemanticType(new_sty);
      stys = client.getValidSemanticTypes();
      found = false;
      for (int i = 0; i < stys.length; i++) {
        if (stys[i].getValue().equals(new_sty.getValue())) {
          found = true;
          break;
        }
      }
      if (!found)
        addToLog("    21.2. Test Passed");
      else {
        addToLog("    21.2. Test Failed");
        thisTestFailed();
      }

      //
      // 21.3. Test getValidSemanticTypeValues()
      //      
      addToLog(
        "    21.3. Test getValidSemanticTypeValues() ... "
        + date_format.format(timestamp));

      String[] sty_values = client.getValidSemanticTypeValues();
      for (int i = 0; i < sty_values.length; i++) {
        addToLog("            "+sty_values[i]);
        found = true;
        if (i > 5) {
          addToLog("          >>> Loop terminated. Only few records displayed.");
          break;
        }
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
    addToLog("Finished AuxiliaryDataClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

  private void displaySource(Source source) {
    addToLog("            CUI: " + source.getCUI());
    addToLog("            Cui Frequency: " + source.getCuiFrequency());
    addToLog("            Inverter: " + source.getInverter());
    addToLog("            Normalized Source Abbreviation: " + source.getNormalizedSourceAbbreviation());
    addToLog("            Rank: " + source.getRank().toString());
    addToLog("            Restriction Level: " + source.getRestrictionLevel());
    addToLog("            Root Source Abbreviation: " + source.getRootSourceAbbreviation());
    addToLog("            Source Abbreviation: " + source.getSourceAbbreviation());
    addToLog("            Source Version: " + source.getSourceVersion());
    addToLog("            Stripped Source Abbreviation: " + source.getStrippedSourceAbbreviation());
    addToLog("            Date Created: " + source.getDateCreated());
    addToLog("            Meta Year: " + source.getMetaYear());
    addToLog("            Initial Receipt Date: " + source.getInitialReceiptDate());
    addToLog("            Clean Receipt Date: " + source.getCleanReceiptDate());
    addToLog("            Test Insertion Date: " + source.getTestInsertionDate());
    addToLog("            Real Insertion Date: " + source.getRealInsertionDate());
    addToLog("            Source Contact: " + source.getSourceContact());
    addToLog("            Inverter Contact: " + source.getInverterContact());
    addToLog("            NLM Path: " + source.getNLMPath());
    addToLog("            Apelon Path: " + source.getApelonPath());
    addToLog("            Inversion Script: " + source.getInversionScript());
    addToLog("            Inverter Notes File: " + source.getInverterNotesFile());
    addToLog("            Conserve File: " + source.getConserveFile());
    addToLog("            SAB List: " + source.getSABList());
    addToLog("            Meow Display Name: " + source.getMeowDisplayName());
    addToLog("            Source Description: " + source.getSourceDescription());
    addToLog("            Status: " + source.getStatus());
    addToLog("            Worklist Sortkey Location: " + source.getWorklistSortkeyLocation());
    addToLog("            Termgroup List: " + source.getTermGroupList());
    addToLog("            Attribute List: " + source.getAttributeList());
    addToLog("            Inversion Name: " + source.getInversionNotes());
    addToLog("            Get Notes: " + source.getNotes());
    addToLog("            Inverse Recipe Location: " + source.getInverseRecipeLocation());
    addToLog("            Suppressible Editable Record: " + source.getSuppressibleEditableRecord());
    addToLog("            Versioned CUI: " + source.getVersionedCui());
    addToLog("            Root CUI: " + source.getRootCui());
    addToLog("            Official Name: " + source.getOfficialName());
    addToLog("            Short Name: " + source.getShortName());
    addToLog("            Attribute Name List: " + source.getAttributeNameList());
    addToLog("            Term Type List: " + source.getTermTypeList());
    addToLog("            Term Frequency: " + source.getTermFrequency());
    addToLog("            CUI Frequency: " + source.getCuiFrequency());
    addToLog("            Citation: " + source.getCitation());
    addToLog("            Last Contacted Date: " + source.getLastContactedDate());
    addToLog("            License Information: " + source.getLicenseInformation());
    addToLog("            Character Encoding: " + source.getCharacterEncoding());
    addToLog("            Insertion Date: " + source.getInsertionDate());
    addToLog("            Expiration Date: " + source.getExpirationDate());
    addToLog("            Invert Meta Version: " + source.getInsertMetaVersion());
    addToLog("            Remove Meta Version: " + source.getRemoveMetaVersion());
    addToLog("            NLM Contact: " + source.getNLMContact());
    addToLog("            Acquisition Contact: " + source.getAcquisitionContact());
    addToLog("            Content Contact: " + source.getContentContact());
    addToLog("            License Contact: " + source.getLicenseContact());
    addToLog("            Context Type: " + source.getContextType());
    addToLog("            Language: " + source.getLanguage());
    addToLog("            Test Insertion Start Date: " + source.getTestInsertionStartDate());
    addToLog("            Test Insertion End Date: " + source.getTestInsertionEndDate());
    addToLog("            Real Insertion Start Date: " + source.getRealInsertionStartDate());
    addToLog("            Real Insertion End Date: " + source.getRealInsertionEndDate());
    addToLog("            Editing Start Date: " + source.getEditingStartDate());
    addToLog("            Editing End Date: " + source.getEditingEndDate());
    addToLog("            Latest Available: " + source.getLatestAvailable());
    addToLog("            Release URL List: " + source.getReleaseUrlList());
    addToLog("            Internal URL List: " + source.getInternalUrlList());
    addToLog("            Relationship Directionality Flag: " + source.getRelationshipDirectionalityFlag());
  }

  private void displayTermgroup(Termgroup tg) {
    addToLog("            Termgroup: " + tg.toString());
    addToLog("            Notes: " + tg.getNotes());
    addToLog("            Suppressible: " + tg.getSuppressible());
    addToLog("            Term Type: " + tg.getTermType());
    addToLog("            Exclude: " + tg.exclude());
    addToLog("            Rank: " + tg.getRank().toString());
    addToLog("            Released Rank: " + tg.getReleaseRank());
    addToLog("            Source: " + tg.getSource());
    addToLog("            Termgroup to outrank: " + tg.getTermgroupToOutrank());
    addToLog("            Norm Exclude: " + tg.normExclude());
 }
}