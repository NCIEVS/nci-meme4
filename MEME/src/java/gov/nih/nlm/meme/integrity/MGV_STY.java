/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_STY
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.Date;
import java.util.HashSet;

/**
 * Validates merges between two {@link Concept}s where
 * both contain releasable semantic types from the same
 * {@link Source} and the {@link Source} is in <code>ic_single</code>.
 *
 * @author MEME Group
 */
public class MGV_STY extends AbstractBinaryDataMergeInhibitor {

  //
  // Fields
  //
  private static HashSet sources_set = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_STY} check.
   */
  public MGV_STY() {
    super();
    setName("MGV_STY");
  }

  //
  // Methods
  //

  /**
   * Validates the pair of {@link Concept}s.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @return <code>true</code> if constraint violated, <code>false</code>
   * otherwise
   */
  public boolean validate(Concept source, Concept target) {

    //
    // Get binary check data
    //
    BinaryCheckData[] data = getCheckData();

    //
    // Get source and target STYs
    //
    ConceptSemanticType[] source_stys = source.getSemanticTypes();
    ConceptSemanticType[] target_stys = target.getSemanticTypes();

    //
    // Get source pairs
    //
    if (sources_set == null) {
      sources_set = new HashSet(data.length);
      for (int i = 0; i < data.length; i++) {
        sources_set.add(data[i].getValue1() + data[i].getValue2());
        sources_set.add(data[i].getValue2() + data[i].getValue1());
      }
    }

    //
    // Find cases of merges where source and target stys
    // have sources in the pairs from above
    //
    for (int i = 0; i < source_stys.length; i++) {
      for (int j = 0; j < target_stys.length; j++) {
        if (sources_set.contains(source_stys[i].getValue() +
                                 target_stys[j].getValue()) ||
            sources_set.contains(target_stys[j].getValue() +
                                 source_stys[i].getValue())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Self-qa test.
   * @param args command line arguments
   */
  public static void main(String[] args) {

    try {
      MEMEToolkit.initialize(null, null);
    } catch (InitializationException ie) {
      MEMEToolkit.handleError(ie);
    }
    MEMEToolkit.setProperty(MEMEConstants.DEBUG, "true");

    //
    // Main Header
    //

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Starting test of MGV_STY ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    boolean failed = false;

    String test_message = "Testing MGV_STY- validate():";
    String test_result = null;

    // Create an MGV_STY object to work with
    MGV_STY mgv_sty = new MGV_STY();

    BinaryCheckData bcd1 = new BinaryCheckData("Name1", "Type1", "Type2", "NLM",
                                               "MTH", false);

    BinaryCheckData[] bcds = new BinaryCheckData[1];
    bcds[0] = bcd1;

    mgv_sty.setCheckData(bcds);

    Concept source = new Concept.Default(1000);
    Concept target = new Concept.Default(1001);
    ConceptSemanticType attr1 = new ConceptSemanticType(12345);
    ConceptSemanticType attr2 = new ConceptSemanticType(12346);

    Source src = new Source.Default();
    Source src2 = new Source.Default();
    src.setSourceAbbreviation("MTH");
    src2.setSourceAbbreviation("NLM");

    attr1.setValue("NLM");
    attr1.setTobereleased('Y');
    attr1.setSource(src);

    attr2.setValue("MTH");
    attr2.setTobereleased('Y');
    attr2.setSource(src2);

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_sty.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Concept has no attribute.");

    // Add attribute
    source.addAttribute(attr1);
    target.addAttribute(attr2);

    test_result = " VIOLATION:    "; // should return true
    if (mgv_sty.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts.");

    attr2.setValue("MSH");

    test_result = " NO VIOLATION: "; // should return false
    if (!mgv_sty.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result +
        "One of the merging two concepts contain value that are not listed in ic_pair.");

    attr2.setValue("MTH");

    test_result = " VIOLATION:    "; // should return true
    if (mgv_sty.validate(source, target)) {
      test_result += " PASSED: ";
    } else {
      test_result += " FAILED: ";
      failed = true;
    }
    MEMEToolkit.trace(test_message + test_result + "Merging two concepts.");

    //
    // Main Footer
    //

    MEMEToolkit.trace("");

    if (failed) {
      MEMEToolkit.trace("AT LEAST ONE TEST DID NOT COMPLETE SUCCESSFULLY");
    } else {
      MEMEToolkit.trace("ALL TESTS PASSED");

    }
    MEMEToolkit.trace("");

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Finished test of MGV_STY ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }
}
