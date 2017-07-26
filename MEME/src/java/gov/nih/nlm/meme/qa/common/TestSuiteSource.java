/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.common
 * Object:  TestSuiteSource
 *
 *****************************************************************************/

package gov.nih.nlm.meme.qa.common;

import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Test suite for Source
 */
public class TestSuiteSource extends TestSuite {

  public TestSuiteSource() {
    setName("TestSuiteSource");
    setDescription("Test Suite for Atom");
  }

  /**
   * Perform Test Suite Atom
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();
    Date timestamp = new Date(System.currentTimeMillis());

    Source source = new Source.Default();

    addToLog(
        "    1. Test Source: setSourceAbbreviation(), getSourceAbbreviation() ... "
        + date_format.format(timestamp));

    source.setSourceAbbreviation("MTH");
    if (source.getSourceAbbreviation().equals("MTH"))
      addToLog("    1. Test Passed");
    else {
      addToLog("    1. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    2. Test Source: setStrippedSourceAbbreviation(), getStrippedSourceAbbreviation() ... "
        + date_format.format(timestamp));

    source.setStrippedSourceAbbreviation("MTH");
    if (source.getStrippedSourceAbbreviation().equals("MTH"))
      addToLog("    2. Test Passed");
    else {
      addToLog("    2. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    3. Test Source: setRootSourceAbbreviation(), getRootSourceAbbreviation() ... "
        + date_format.format(timestamp));

    source.setRootSourceAbbreviation("MTH");
    if (source.getRootSourceAbbreviation().equals("MTH"))
      addToLog("    3. Test Passed");
    else {
      addToLog("    3. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    4. Test Source: setSourceVersion(), getSourceVersion() ... "
        + date_format.format(timestamp));

    source.setSourceVersion("11");
    if (source.getSourceVersion().equals("11"))
      addToLog("    4. Test Passed");
    else {
      addToLog("    4. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    5. Test Source: setNormalizedSourceAbbreviation(), getNormalizedSourceAbbreviation() ... "
        + date_format.format(timestamp));

    source.setNormalizedSourceAbbreviation("MTH");
    if (source.getNormalizedSourceAbbreviation().equals("MTH"))
      addToLog("    5. Test Passed");
    else {
      addToLog("    5. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    6. Test Source: setSourceFamilyAbbreviation(), getSourceFamilyAbbreviation() ... "
        + date_format.format(timestamp));

    source.setSourceFamilyAbbreviation("MTH");
    if (source.getSourceFamilyAbbreviation().equals("MTH"))
      addToLog("    6. Test Passed");
    else {
      addToLog("    6. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    7. Test Source: setOfficialName(), getOfficialName() ... "
        + date_format.format(timestamp));

    source.setOfficialName("OFFICIAL_NAME");
    if (source.getOfficialName().equals("OFFICIAL_NAME"))
      addToLog("    7. Test Passed");
    else {
      addToLog("    7. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    8. Test Source: setCUI(), getCUI() ... "
        + date_format.format(timestamp));

    CUI cui = new CUI("C000001");
    source.setCUI(cui);
    if (source.getCUI().equals(cui))
      addToLog("    8. Test Passed");
    else {
      addToLog("    8. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    9. Test Source: setVersionedCUI(), getVersionedCUI() ... "
        + date_format.format(timestamp));

    source.setVersionedCui(cui);
    if (source.getVersionedCui().equals(cui))
      addToLog("    9. Test Passed");
    else {
      addToLog("    9. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    10. Test Source: setRootCUI(), getRootCUI() ... "
        + date_format.format(timestamp));

    source.setRootCui(cui);
    if (source.getRootCui().equals(cui))
      addToLog("    10. Test Passed");
    else {
      addToLog("    10. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    11. Test Source: setlanguage(), getLanguage() ... "
        + date_format.format(timestamp));

    Language lang = new Language.Default("ENGLISH", "ENG");
    source.setLanguage(lang);
    if (source.getLanguage().getAbbreviation().equals(lang.getAbbreviation()))
      addToLog("    11. Test Passed");
    else {
      addToLog("    11. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    12. Test Source: setInsertMetaVersion(), getInsertMetaVersion() ... "
        + date_format.format(timestamp));

    source.setInsertMetaVersion("Version 12");
    if (source.getInsertMetaVersion().equals("Version 12"))
      addToLog("    12. Test Passed");
    else {
      addToLog("    12. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    13. Test Source: setRemoveMetaVersion(), getRemoveMetaVersion() ... "
        + date_format.format(timestamp));

    source.setRemoveMetaVersion("Version 12");
    if (source.getRemoveMetaVersion().equals("Version 12"))
      addToLog("    13. Test Passed");
    else {
      addToLog("    13. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    14. Test Source: setNLMContact(), getNLMContact() ... "
        + date_format.format(timestamp));

    source.setNLMContact("NLM CONTACT");
    if (source.getNLMContact().equals("NLM CONTACT"))
      addToLog("    14. Test Passed");
    else {
      addToLog("    14. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    15. Test Source: setInverter(), getInverter() ... "
        + date_format.format(timestamp));

    source.setInverter("INVERTER");
    if (source.getInverter().equals("INVERTER"))
      addToLog("    15. Test Passed");
    else {
      addToLog("    15. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    16. Test Source: setAcquisitionContact(), getAcquisitionContact() ... "
        + date_format.format(timestamp));

    source.setAcquisitionContact("CONTACT");
    if (source.getAcquisitionContact().equals("CONTACT"))
      addToLog("    16. Test Passed");
    else {
      addToLog("    16. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    17. Test Source: setContentContact(), getContentContact() ... "
        + date_format.format(timestamp));

    source.setContentContact("CONTENT_CONTACT");
    if (source.getContentContact().equals("CONTENT_CONTACT"))
      addToLog("    17. Test Passed");
    else {
      addToLog("    17. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    18. Test Source: setLicenseContact(), getLicenseContact() ... "
        + date_format.format(timestamp));

    source.setLicenseContact("LICENSE_CONTACT");
    if (source.getLicenseContact().equals("LICENSE_CONTACT"))
      addToLog("    18. Test Passed");
    else {
      addToLog("    18. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    19. Test Source: setLicenseInformation(), getLicenseInformation() ... "
        + date_format.format(timestamp));

    source.setLicenseInformation("LICENSE_INFO");
    if (source.getLicenseInformation().equals("LICENSE_INFO"))
      addToLog("    19. Test Passed");
    else {
      addToLog("    19. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    20. Test Source: setRestrictionLevel(), getRestrictionLevel() ... "
        + date_format.format(timestamp));

    source.setRestrictionLevel("RESTRICTION_LEVEL");
    if (source.getRestrictionLevel().equals("RESTRICTION_LEVEL"))
      addToLog("    20. Test Passed");
    else {
      addToLog("    20. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    21. Test Source: setTermFrequency(), getTermFrequency() ... "
        + date_format.format(timestamp));

    source.setTermFrequency(3);
    if (source.getTermFrequency() == 3)
      addToLog("    21. Test Passed");
    else {
      addToLog("    21. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    22. Test Source: setCuiFrequency(), getCuiFrequency() ... "
        + date_format.format(timestamp));

    source.setCuiFrequency(3);
    if (source.getCuiFrequency() == 3)
      addToLog("    22. Test Passed");
    else {
      addToLog("    22. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    23. Test Source: setContextType(), getContextType() ... "
        + date_format.format(timestamp));

    source.setContextType("CONTEXT_TYPE");
    if (source.getContextType().equals("CONTEXT_TYPE"))
      addToLog("    23. Test Passed");
    else {
      addToLog("    23. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    24. Test Source: setAttributeList(), getAttributeList() ... "
        + date_format.format(timestamp));

    source.setAttributeList("ATTRIBUTE_LIST");
    if (source.getAttributeList().equals("ATTRIBUTE_LIST"))
      addToLog("    24. Test Passed");
    else {
      addToLog("    24. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    25. Test Source: setAttributeNameList(), getAttributeNameList() ... "
        + date_format.format(timestamp));

    source.setAttributeNameList("ATTRIBUTE_NAME_LIST");
    if (source.getAttributeNameList().equals("ATTRIBUTE_NAME_LIST"))
      addToLog("    25. Test Passed");
    else {
      addToLog("    25. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    26. Test Source: setTermTypeList(), getTermTypeList() ... "
        + date_format.format(timestamp));

    source.setTermTypeList("TERM_TYPE_LIST");
    if (source.getTermTypeList().equals("TERM_TYPE_LIST"))
      addToLog("    26. Test Passed");
    else {
      addToLog("    26. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    27. Test Source: setTermGroupList(), getTermGroupList() ... "
        + date_format.format(timestamp));

    source.setTermGroupList("TERM_GROUP_LIST");
    if (source.getTermGroupList().equals("TERM_GROUP_LIST"))
      addToLog("    27. Test Passed");
    else {
      addToLog("    27. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    28. Test Source: setCharacterEncoding(), getCharacterEncoding() ... "
        + date_format.format(timestamp));

    source.setCharacterEncoding("CHARACTER_ENCODING");
    if (source.getCharacterEncoding().equals("CHARACTER_ENCODING"))
      addToLog("    28. Test Passed");
    else {
      addToLog("    28. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    29. Test Source: setShortName(), getShortName() ... "
        + date_format.format(timestamp));

    source.setShortName("SHORT_NAME");
    if (source.getShortName().equals("SHORT_NAME"))
      addToLog("    29. Test Passed");
    else {
      addToLog("    29. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    30. Test Source: setCitation(), getCitation() ... "
        + date_format.format(timestamp));

    source.setCitation("CITATION");
    if (source.getCitation().equals("CITATION"))
      addToLog("    30. Test Passed");
    else {
      addToLog("    30. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    31. Test Source: setInsertionDate(), getInsertionDate() ... "
        + date_format.format(timestamp));

    java.util.Date today = new java.util.Date();
    source.setInsertionDate(today);
    if (source.getInsertionDate().equals(today))
      addToLog("    31. Test Passed");
    else {
      addToLog("    31. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    32. Test Source: setExpirationDate(), getExpirationDate() ... "
        + date_format.format(timestamp));

    source.setExpirationDate(today);
    if (source.getExpirationDate().equals(today))
      addToLog("    32. Test Passed");
    else {
      addToLog("    32. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    33. Test Source: setIsCurrent(), isCurrent() ... "
        + date_format.format(timestamp));

    source.setIsCurrent(true);
    if (source.isCurrent())
      addToLog("    33. Test Passed");
    else {
      addToLog("    33. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    34. Test Source: setIsPrevious(), isPrevious() ... "
        + date_format.format(timestamp));

    source.setIsPrevious(true);
    if (source.isPrevious())
      addToLog("    34. Test Passed");
    else {
      addToLog("    34. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    35. Test Source: setSourceToOutrank(), getSourceToOutrank() ... "
        + date_format.format(timestamp));

    Source sto = new Source.Default("NEC");
    source.setSourceToOutrank(sto);
    if (source.getSourceToOutrank().equals(sto))
      addToLog("    35. Test Passed");
    else {
      addToLog("    35. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    36. Test Source: setDateCreated(), getDateCreated() ... "
        + date_format.format(timestamp));

    source.setDateCreated(today);
    if (source.getDateCreated().equals(today))
      addToLog("    36. Test Passed");
    else {
      addToLog("    36. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    37. Test Source: setMetaYear(), getMetaYear() ... "
        + date_format.format(timestamp));

    source.setMetaYear(3);
    if (source.getMetaYear() == 3)
      addToLog("    37. Test Passed");
    else {
      addToLog("    37. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    37. Test Source: setInitialReceiptDate(), getInitialReceiptDate() ... "
        + date_format.format(timestamp));

    source.setInitialReceiptDate(today);
    if (source.getInitialReceiptDate().equals(today))
      addToLog("    37. Test Passed");
    else {
      addToLog("    37. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    38. Test Source: setCleanReceiptDate(), getCleanReceiptDate() ... "
        + date_format.format(timestamp));

    source.setCleanReceiptDate(today);
    if (source.getCleanReceiptDate().equals(today))
      addToLog("    38. Test Passed");
    else {
      addToLog("    38. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    39. Test Source: setTestInsertionDate(), getTestInsertionDate() ... "
        + date_format.format(timestamp));

    source.setTestInsertionDate(today);
    if (source.getTestInsertionDate().equals(today))
      addToLog("    39. Test Passed");
    else {
      addToLog("    39. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    40. Test Source: setRealInsertionDate(), getRealInsertionDate() ... "
        + date_format.format(timestamp));

    source.setRealInsertionDate(today);
    if (source.getRealInsertionDate().equals(today))
      addToLog("    40. Test Passed");
    else {
      addToLog("    40. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    41. Test Source: setSourceContact(), getSourceContact() ... "
        + date_format.format(timestamp));

    source.setSourceContact("SOURCE_CONTACT");
    if (source.getSourceContact().equals("SOURCE_CONTACT"))
      addToLog("    41. Test Passed");
    else {
      addToLog("    41. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    42. Test Source: setInverterContact(), getInverterContact() ... "
        + date_format.format(timestamp));

    source.setInverterContact("INVERTER_CONTACT");
    if (source.getInverterContact().equals("INVERTER_CONTACT"))
      addToLog("    42. Test Passed");
    else {
      addToLog("    42. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    43. Test Source: setNLMPath(), getNLMPath() ... "
        + date_format.format(timestamp));

    source.setNLMPath("NLM_PATH");
    if (source.getNLMPath().equals("NLM_PATH"))
      addToLog("    43. Test Passed");
    else {
      addToLog("    43. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    44. Test Source: setApelonPath(), getApelonPath() ... "
        + date_format.format(timestamp));

    source.setApelonPath("PATH");
    if (source.getApelonPath().equals("PATH"))
      addToLog("    44. Test Passed");
    else {
      addToLog("    44. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    45. Test Source: setInversionScript(), getInversionScript() ... "
        + date_format.format(timestamp));

    source.setInversionScript("INVERSION_SCRIPT");
    if (source.getInversionScript().equals("INVERSION_SCRIPT"))
      addToLog("    45. Test Passed");
    else {
      addToLog("    45. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    46. Test Source: setInverterNotesFile(), getInverterNotesFile() ... "
        + date_format.format(timestamp));

    source.setInverterNotesFile("INVERTER_NOTES_FILE");
    if (source.getInverterNotesFile().equals("INVERTER_NOTES_FILE"))
      addToLog("    46. Test Passed");
    else {
      addToLog("    46. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    47. Test Source: setConserveFile(), getConserveFile() ... "
        + date_format.format(timestamp));

    source.setConserveFile("CONSERVE_FILE");
    if (source.getConserveFile().equals("CONSERVE_FILE"))
      addToLog("    47. Test Passed");
    else {
      addToLog("    47. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    48. Test Source: setSABList(), getSABList() ... "
        + date_format.format(timestamp));

    source.setSABList("SAB_LIST");
    if (source.getSABList().equals("SAB_LIST"))
      addToLog("    48. Test Passed");
    else {
      addToLog("    48. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    49. Test Source: setMeowDisplayName(), getMeowDisplayName() ... "
        + date_format.format(timestamp));

    source.setMeowDisplayName("MEOW_DISPLAY_NAME");
    if (source.getMeowDisplayName().equals("MEOW_DISPLAY_NAME"))
      addToLog("    49. Test Passed");
    else {
      addToLog("    49. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    50. Test Source: setSourceDescription(), getSourceDescription() ... "
        + date_format.format(timestamp));

    source.setSourceDescription("SOURCE_DESC");
    if (source.getSourceDescription().equals("SOURCE_DESC"))
      addToLog("    50. Test Passed");
    else {
      addToLog("    50. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    51. Test Source: setStatus(), getStatus() ... "
        + date_format.format(timestamp));

    source.setStatus("STATUS");
    if (source.getStatus().equals("STATUS"))
      addToLog("    51. Test Passed");
    else {
      addToLog("    51. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    52. Test Source: setWorklistSortkeyLocation(), getWorklistSortkeyLocation() ... "
        + date_format.format(timestamp));

    source.setWorklistSortkeyLocation("SORTKEY_LOCATION");
    if (source.getWorklistSortkeyLocation().equals("SORTKEY_LOCATION"))
      addToLog("    52. Test Passed");
    else {
      addToLog("    52. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    53. Test Source: setInversionNotes(), getInversionNotes() ... "
        + date_format.format(timestamp));

    source.setInversionNotes("INVERSION_NOTES");
    if (source.getInversionNotes().equals("INVERSION_NOTES"))
      addToLog("    53. Test Passed");
    else {
      addToLog("    53. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    54. Test Source: setNotes(), getNotes() ... "
        + date_format.format(timestamp));

    source.setNotes("NOTES");
    if (source.getNotes().equals("NOTES"))
      addToLog("    54. Test Passed");
    else {
      addToLog("    54. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    55. Test Source: setInverseRecipeLocation(), getInverseRecipeLocation() ... "
        + date_format.format(timestamp));

    source.setInverseRecipeLocation("INVERSE_RECIPE_LOCATION");
    if (source.getInverseRecipeLocation().equals("INVERSE_RECIPE_LOCATION"))
      addToLog("    55. Test Passed");
    else {
      addToLog("    55. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    56. Test Source: setSuppressibleEditableRecord(), getSuppressibleEditableRecord() ... "
        + date_format.format(timestamp));

    source.setSuppressibleEditableRecord(true);
    if (source.getSuppressibleEditableRecord())
      addToLog("    56. Test Passed");
    else {
      addToLog("    56. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    57. Test Source: setLastContactedDate(), getLastContactedDate() ... "
        + date_format.format(timestamp));

    source.setLastContactedDate(today);
    if (source.getLastContactedDate().equals(today))
      addToLog("    57. Test Passed");
    else {
      addToLog("    57. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    58. Test Source: setTestInsertionStartDate(), getTestInsertionStartDate() ... "
        + date_format.format(timestamp));

    source.setTestInsertionStartDate(today);
    if (source.getTestInsertionStartDate().equals(today))
      addToLog("    58. Test Passed");
    else {
      addToLog("    58. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    59. Test Source: setTestInsertionEndDate(), getTestInsertionEndDate() ... "
        + date_format.format(timestamp));

    source.setTestInsertionEndDate(today);
    if (source.getTestInsertionEndDate().equals(today))
      addToLog("    59. Test Passed");
    else {
      addToLog("    59. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    60. Test Source: setRealInsertionStartDate(), getRealInsertionStartDate() ... "
        + date_format.format(timestamp));

    source.setRealInsertionStartDate(today);
    if (source.getRealInsertionStartDate().equals(today))
      addToLog("    60. Test Passed");
    else {
      addToLog("    60. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    61. Test Source: setRealInsertionEndDate(), getRealInsertionEndDate() ... "
        + date_format.format(timestamp));

    source.setRealInsertionEndDate(today);
    if (source.getRealInsertionEndDate().equals(today))
      addToLog("    61. Test Passed");
    else {
      addToLog("    61. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    62. Test Source: setEditingStartDate(), getEditingStartDate() ... "
        + date_format.format(timestamp));

    source.setEditingStartDate(today);
    if (source.getEditingStartDate().equals(today))
      addToLog("    62. Test Passed");
    else {
      addToLog("    62. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    63. Test Source: setEditingEndDate(), getEditingEndDate() ... "
        + date_format.format(timestamp));

    source.setEditingEndDate(today);
    if (source.getEditingEndDate().equals(today))
      addToLog("    63. Test Passed");
    else {
      addToLog("    63. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    64. Test Source: setLatestAvailable(), getLatestAvailable() ... "
        + date_format.format(timestamp));

    source.setLatestAvailable("LATEST_AVAILABLE");
    if (source.getLatestAvailable().equals("LATEST_AVAILABLE"))
      addToLog("    64. Test Passed");
    else {
      addToLog("    64. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    65. Test Source: setReleaseUrlList(), getReleaseUrlList() ... "
        + date_format.format(timestamp));

    source.setReleaseUrlList("RELEASE_URL_LIST");
    if (source.getReleaseUrlList().equals("RELEASE_URL_LIST"))
      addToLog("    65. Test Passed");
    else {
      addToLog("    65. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    66. Test Source: setInternalUrlList(), getInternalUrlList() ... "
        + date_format.format(timestamp));

    source.setInternalUrlList("INTERNAL_URL_LIST");
    if (source.getInternalUrlList().equals("INTERNAL_URL_LIST"))
      addToLog("    66. Test Passed");
    else {
      addToLog("    66. Test Failed");
      thisTestFailed();
    }

    addToLog(
        "    67. Test Source: setRelationshipDirectionalityFlag(), getRelationshipDirectionalityFlag() ... "
        + date_format.format(timestamp));

    source.setRelationshipDirectionalityFlag(true);
    if (source.getRelationshipDirectionalityFlag())
      addToLog("    67. Test Passed");
    else {
      addToLog("    67. Test Failed");
      thisTestFailed();
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
    addToLog("Finished TestSuiteSource at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}