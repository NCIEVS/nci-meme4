/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ActionSequences
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularChangeConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeRelationshipAction;
import gov.nih.nlm.meme.action.MolecularDeleteAtomAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteConceptAction;
import gov.nih.nlm.meme.action.MolecularDeleteRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.action.MolecularMergeAction;
import gov.nih.nlm.meme.action.MolecularMoveAction;
import gov.nih.nlm.meme.action.MolecularSplitAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.NextIdentifierAction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.sql.Ticket;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Used to perform action sequences for testing/debugging purposes.
 *
 * @author MEME Group
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant to take a SessionContext argument 
 * 
 */
public class ActionSequences implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives requests from the {@link MEMEApplicationServer}.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    //
    // Determine which action sequence to perform
    //
    MEMEServiceRequest request = context.getServiceRequest();
    String function = (String) request.getParameter("function").getValue();

    //
    // Create and add log into the session context
    //
    StringBuffer log = new StringBuffer(1000);
    context.put("log", log);

    try {
      if (function.equals("sequence_1")) {
        performSequence1(context);
      }
      if (function.equals("sequence_2")) {
        performSequence2(context);
      }
      if (function.equals("sequence_3")) {
        performSequence3(context);
      }
    } catch (MEMEException me) {
      context.put("progress", new Integer( -100));
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter(sw);
      me.printStackTrace(out);
      log.append(sw.toString());
      throw me;
    }

  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean requiresSession() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isRunning() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @param context the {@link SessionContext}
   * @return <code>false</code>
   */
  public boolean isReEntrant(SessionContext context) {
    return false;
  }

  /**
   * Implements action sequence 1.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to perform sequence 1
   */
  private void performSequence1(SessionContext context) throws MEMEException {

    // Get Service Request
    MEMEServiceRequest request = context.getServiceRequest();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    //
    // Molecular action parameters
    //
    Authority authority = new Authority.Default("L-MEME4");
    EnforcableIntegrityVector integrity_vector = new EnforcableIntegrityVector();
    boolean change_status = true;

    NextIdentifierAction nia = NextIdentifierAction.newSetNextIdentifierAction(
        MolecularTransaction.class);
    data_source.getActionEngine().processAction(nia);
    Identifier transaction_id = nia.getNextIdentifier();

    nia = NextIdentifierAction.newSetNextIdentifierAction(WorkLog.class);
    data_source.getActionEngine().processAction(nia);
    Identifier work_id = nia.getNextIdentifier();

    Date start = new Date();
    Ticket ticket = Ticket.getActionsTicket();

    StringBuffer log = (StringBuffer) context.get("log");
    log.append(
        "-----------------------------------------------------------------\n");
    log.append("Starting ... (")
        .append(start)
        .append(")\n");
    log.append(
        "-----------------------------------------------------------------\n");
    log.append("  Transaction ID: ")
        .append(transaction_id.toString()).append("\n");
    log.append("  Work ID: ")
        .append(work_id.toString()).append("\n");
    log.append("  Database: ")
        .append(data_source.getDataSourceName()).append("\n\n");

    int progress = 0;
    context.put("progress", new Integer(progress));
    int[] molecule_ids = new int[17];

    // Create atom (c1) object
    Atom c1 = new Atom.Default();
    Source src = data_source.getSource("MTH");
    c1.setSource(src);
    c1.setTermgroup(data_source.getTermgroup("MTH/PT"));
    c1.setCode(new Code("NOCODE"));
    c1.setString("Test Atom 1");
    c1.setStatus('R');
    c1.setReleased('N');
    c1.setTobereleased('Y');
    c1.setLanguage(data_source.getLanguage("ENG"));

    // Create concept (cs1) object
    Concept cs1 = new Concept.Default();

    cs1.setStatus('R');
    cs1.setReleased('N');
    cs1.setTobereleased('Y');

    // Connect atom (c1) to concept (cs1) and concept (cs1) to atom (c1)
    c1.setConcept(cs1);
    cs1.addAtom(c1);

    //
    // 1. MolecularInsertConcept(cs1)
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #1.");
    molecule_ids[1] =
        performAction(log, new MolecularInsertConceptAction(cs1), authority,
                      integrity_vector, change_status, transaction_id, work_id,
                      data_source);
    data_source.populateConcept(cs1, ticket);

    progress += 2;
    context.put("progress", new Integer(progress));

    // Create another concept (cs2) object
    Concept cs2 = new Concept.Default();

    // Create another atom (c2) object
    Atom c2 = new Atom.Default();
    src = data_source.getSource("MTH");
    c2.setSource(src);
    c2.setTermgroup(data_source.getTermgroup("MTH/PT"));
    c2.setCode(new Code("NOCODE"));
    c2.setString("Test Atom 2");
    c2.setStatus('R');
    c2.setReleased('N');
    c2.setTobereleased('Y');
    c2.setLanguage(data_source.getLanguage("ENG"));

    // Create another atom (c3) object
    Atom c3 = new Atom.Default();
    c3.setSource(data_source.getSource("MTH"));
    c3.setTermgroup(data_source.getTermgroup("MTH/PT"));
    c3.setCode(new Code("NOCODE"));
    c3.setString("Test Atom 3");
    c3.setStatus('R');
    c3.setReleased('N');
    c3.setTobereleased('Y');
    c3.setLanguage(data_source.getLanguage("ENG"));

    // Connect atom (c2, c3) to concept (cs2) and concept (cs2) to atom (c2, c3)
    c2.setConcept(cs2);
    c3.setConcept(cs2);
    cs2.addAtom(c2);
    cs2.addAtom(c3);
    cs2.setStatus('R');
    cs2.setReleased('N');
    cs2.setTobereleased('Y');

    //
    // 2. MolecularInsertConcept(cs2)
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #2.");
    molecule_ids[2] =
        performAction(log, new MolecularInsertConceptAction(cs2), authority,
                      integrity_vector, change_status, transaction_id, work_id,
                      data_source);
    data_source.populateConcept(cs2, ticket);

    progress += 2;
    context.put("progress", new Integer(progress));

    //
    // 3. MolecularMergeAction cs1, cs2
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #3.");
    molecule_ids[3] =
        performAction(log, new MolecularMergeAction(cs1, cs2), authority,
                      integrity_vector, change_status, transaction_id, work_id,
                      data_source);
    data_source.populateConcept(cs2, ticket);

    progress += 2;
    context.put("progress", new Integer(progress));

    //
    // 4. MolecularSplitAction cs2, c2
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #4.");
    MolecularSplitAction msa = new MolecularSplitAction(cs2);

    msa.addAtomToSplit(c2);
    molecule_ids[4] = performAction(log, msa, authority,
                                    integrity_vector, change_status,
                                    transaction_id, work_id, data_source);
    data_source.populateConcept(cs2, ticket);

    progress += 2;
    context.put("progress", new Integer(progress));

    // Create relationship object
    Relationship r1 = new Relationship.Default();
    Concept cs3 = msa.getTarget();
    cs3.addRelationship(r1);
    r1.setConcept(cs3);
    r1.setRelatedConcept(cs2);
    r1.setName("RT");
    r1.setAttribute("analyzes");
    r1.setSource(data_source.getSource("MTH"));
    r1.setSourceOfLabel(data_source.getSource("MTH"));
    r1.setLevel('C');
    r1.setStatus('R');
    r1.setReleased('N');
    r1.setTobereleased('Y');

    //
    // 5. MolecularInsertRelationship(r1)
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #5.");
    molecule_ids[5] =
        performAction(log, new MolecularInsertRelationshipAction(r1), authority,
                      integrity_vector, change_status, transaction_id, work_id,
                      data_source);
    data_source.populateConcept(cs3, ticket);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 6. MolecularMoveAction cs2, cs3
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #6.");
    MolecularMoveAction mma = new MolecularMoveAction(cs2, cs3);

    mma.addAtomToMove(c3);
    molecule_ids[6] = performAction(log, mma, authority,
                                    integrity_vector, change_status,
                                    transaction_id, work_id, data_source);
    data_source.populateConcept(cs3, ticket);

    progress += 3;
    context.put("progress", new Integer(progress));

    c3.setConcept(cs3);

    //
    // 7. MolecularApproveAction cs3
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #7.");
    MolecularApproveConceptAction maca = new MolecularApproveConceptAction(cs3);
    maca.setSource(cs3);
    molecule_ids[7] = performAction(log, maca, authority,
                                    integrity_vector, change_status,
                                    transaction_id, work_id, data_source);
    data_source.populateConcept(cs3, ticket);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 8. MolecularInsertAttributeAction a1
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #8.");
    // Create attribute (a1) object
    Attribute a1 = new Attribute.Default();
    a1.setSource(data_source.getSource("MTH"));
    a1.setName("Test Attribute");
    //a1.setValue("D25-26 qualif");
    a1.setValue("WHAT: JRA Rash. JRA Rash: a rash occurring characteristically in patients with systemic onset juvenile rheumatoid arthritis. It consists of discrete or confluent macular or maculopapular red or salmon-pink, usually non-pruritic lesions. The lesions are most prominent over the trunk, but may also be found on the face and extremities. WHY: The rheumatoid rash is an important diagnostic sign of juvenile arthritis of systemic or poly-articular onset. The rash is not commonly seen, however, in JRA of pauci-articular onset. HOW: Therheumatoid rash is characteristically fleeting, tending to appear in the evenings associated with an increase in temperature, and then disappearing, sometimes in less than an hour. The rash is salmon-pink, usually circumscribed and macular or occasionally maculopapular. The individual lesions vary in size from 2-6 mm. The larger lesions have a pale center with extreme pallor of the skin on the periphery of the rash. The rash is found predominantly on the chest, axillae, thighs, upper arms and face. It is not pruritic. The rash may be induced by rubbing or scratching the skin. The rash is of greatest extent when it first appears and does not spread in any regular manner. It may occur on and off for a few weeks or for many years. Steroids or salicylates have no specific effect on the rash. The rheumatoid rash is distinguished from a drug sensitivity rash by its characteristic daily recurrence, its fleeting quality, its non-pruritic nature, and its persistence in spite of discontinuance of drugs. It is differentiated from erythema marginatum by the smaller characteristic macules, the presence of the rash on the face (not seen in erythema marginatum) and by the fact that in erythema marginatum the rash extends in areas after its blah blah blah blah"
                );
    a1.setStatus('R');
    a1.setLevel('C');
    a1.setTobereleased('Y');
    a1.setReleased('N');
    a1.setConcept(cs3);
    cs3.addAttribute(a1);
    molecule_ids[8] =
        performAction(log, new MolecularInsertAttributeAction(a1), authority,
                      integrity_vector, change_status, transaction_id, work_id,
                      data_source);
    data_source.populateConcept(cs3, ticket);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 9. MolecularChangeAtomAction (c3)
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #9.");
    c3.setStatus('N');
    c3.setTobereleased('n');
    c3.setSuppressible("E");
    MolecularChangeAtomAction mcaa = new MolecularChangeAtomAction(c3);
    molecule_ids[9] = performAction(log, mcaa, authority,
                                    integrity_vector, change_status,
                                    transaction_id, work_id, data_source);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 10. MolecularChangeAttributeAction (a1)
    //

    MEMEToolkit.trace(
        "ActionSequences.main() - Performing action sequence #10.");
    a1.setStatus('N');
    a1.setTobereleased('n');
    a1.setSuppressible("E");
    MolecularChangeAttributeAction mcatta = new MolecularChangeAttributeAction(
        a1);
    molecule_ids[10] = performAction(log, mcatta, authority,
                                     integrity_vector, change_status,
                                     transaction_id, work_id, data_source);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 11. MolecularChangeRelationshipAction (r1)
    //

    MEMEToolkit.trace(
        "ActionSequences.main() - Performing action sequence #11.");
    r1.setStatus('N');
    r1.setTobereleased('n');
    //r1.setName("BT");
    //r1.setAttribute("mapped_to");
    MolecularChangeRelationshipAction mcra = new
        MolecularChangeRelationshipAction(r1);
    molecule_ids[11] = performAction(log, mcra, authority,
                                     integrity_vector, change_status,
                                     transaction_id, work_id, data_source);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 12. MolecularChangeConceptAction (cs3)
    //

    MEMEToolkit.trace(
        "ActionSequences.main() - Performing action sequence #12.");
    cs3.setStatus('N');
    MolecularChangeConceptAction mcca = new MolecularChangeConceptAction(cs3);
    molecule_ids[12] = performAction(log, mcca, authority,
                                     integrity_vector, change_status,
                                     transaction_id, work_id, data_source);
    data_source.populateConcept(cs3, ticket);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 13. MolecularDeleteAtomAction (c3)
    //

    MEMEToolkit.trace(
        "ActionSequences.main() - Performing action sequence #13.");
    MolecularDeleteAtomAction mdaa = new MolecularDeleteAtomAction(c3);
    molecule_ids[13] = performAction(log, mdaa, authority,
                                     integrity_vector, change_status,
                                     transaction_id, work_id, data_source);
    data_source.populateConcept(cs3, ticket);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 14. MolecularDeleteAttributeAction (a1)
    //

    MEMEToolkit.trace(
        "ActionSequences.main() - Performing action sequence #14.");
    MolecularDeleteAttributeAction mdattra = new MolecularDeleteAttributeAction(
        a1);
    molecule_ids[14] = performAction(log, mdattra, authority,
                                     integrity_vector, change_status,
                                     transaction_id, work_id, data_source);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 15. MolecularDeleteRelationshipAction (r1)
    //

    MEMEToolkit.trace(
        "ActionSequences.main() - Performing action sequence #15.");
    MolecularDeleteRelationshipAction mdra = new
        MolecularDeleteRelationshipAction(r1);
    molecule_ids[15] = performAction(log, mdra, authority,
                                     integrity_vector, change_status,
                                     transaction_id, work_id, data_source);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // 16. MolecularDeleteConceptAction (cs3)
    //

    MEMEToolkit.trace(
        "ActionSequences.main() - Performing action sequence #16.");
    MolecularDeleteConceptAction mdca = new MolecularDeleteConceptAction(cs3);
    molecule_ids[16] = performAction(log, mdca, authority,
                                     integrity_vector, change_status,
                                     transaction_id, work_id, data_source);

    progress += 3;
    context.put("progress", new Integer(progress));

    //
    // Undo action sequences 1-16 (LastInFirstOut)
    //

    for (int i = molecule_ids.length - 1; i > 0; i--) {
      MEMEToolkit.trace(
          "ActionSequences.main() - Performing undo action sequence #" + i +
          ".");
      MolecularAction ma = data_source.getFullMolecularAction(molecule_ids[i]);
      performUndo(log, ma, data_source);
      progress += 3;
      context.put("progress", new Integer(progress));
    }

    context.put("progress", new Integer(100));

    log.append("\n");
    log.append(
        "-----------------------------------------------------------------\n");
    log.append("Finished ... (")
        .append(new Date())
        .append(")\n");
    log.append(
        "-----------------------------------------------------------------\n");

    request.addReturnValue(new Parameter.Default("elapsed_time",
                                                 MEMEToolkit.timeToString(
        MEMEToolkit.timeDifference(new Date(), start))));
  }

  /**
   * Implements action sequence 2.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to perform sequence 2
   */
  private void performSequence2(SessionContext context) throws MEMEException {

    // Get Service Request
    MEMEServiceRequest request = context.getServiceRequest();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    //
    // Molecular action parameters
    //
    Authority authority = new Authority.Default("L-MEME4");
    EnforcableIntegrityVector integrity_vector = new EnforcableIntegrityVector();
    boolean change_status = true;
    NextIdentifierAction nia = NextIdentifierAction.newSetNextIdentifierAction(
        MolecularTransaction.class);
    data_source.getActionEngine().processAction(nia);
    Identifier transaction_id = nia.getNextIdentifier();

    nia = NextIdentifierAction.newSetNextIdentifierAction(WorkLog.class);
    data_source.getActionEngine().processAction(nia);
    Identifier work_id = nia.getNextIdentifier();

    Date start = new Date();

    StringBuffer log = (StringBuffer) context.get("log");
    log.append(
        "-----------------------------------------------------------------\n");
    log.append("Starting ... (")
        .append(start)
        .append(")\n");
    log.append(
        "-----------------------------------------------------------------\n");
    log.append("  Transaction ID: ")
        .append(transaction_id.toString()).append("\n");
    log.append("  Work ID: ")
        .append(work_id.toString()).append("\n");
    log.append("  Database: ")
        .append(data_source.getDataSourceName()).append("\n\n");

    int progress = 0;
    context.put("progress", new Integer(progress));
    List molecule_ids = new ArrayList();

    // Create concept (cs1) object
    Concept cs1 = new Concept.Default();

    cs1.setStatus('R');
    cs1.setReleased('N');
    cs1.setTobereleased('Y');

    Atom[] c = new Atom[10];
    for (int i = 0; i < c.length; i++) {
      // Create atom object
      c[i] = new Atom.Default();
      c[i].setSource(data_source.getSource("MTH"));
      c[i].setTermgroup(data_source.getTermgroup("MTH/PT"));
      c[i].setCode(new Code("NOCODE"));
      c[i].setString("Test Atom 1");
      c[i].setStatus('R');
      c[i].setReleased('N');
      c[i].setTobereleased('Y');
      c[i].setLanguage(data_source.getLanguage("ENG"));
      // Connect atom to concept (cs1) and concept (cs1) to atom
      c[i].setConcept(cs1);
      cs1.addAtom(c[i]);
    }

    //
    //  number_of_loop = c.length * 2
    //  actions_outside_the_loop = 3
    //  undo_all = 2
    //
    //  ((number_of_loop) + actions_outside_the_loop) * undo_all
    //

    // where:
    //   205 = total number of actions
    int pcount = 100 / 205;

    //
    // 1. MolecularInsertConcept(cs1)
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #1.");
    molecule_ids.add(new Integer(
        performAction(log, new MolecularInsertConceptAction(cs1), authority,
                      integrity_vector, change_status, transaction_id, work_id,
                      data_source)));
    progress += pcount;
    context.put("progress", new Integer(progress));

    // Create another concept (cs2) object
    Concept cs2 = new Concept.Default();
    cs2.setStatus('R');
    cs2.setReleased('N');
    cs2.setTobereleased('Y');

    c = new Atom[10];
    for (int i = 0; i < c.length; i++) {
      // Create atom object
      c[i] = new Atom.Default();
      c[i].setSource(data_source.getSource("MTH"));
      c[i].setTermgroup(data_source.getTermgroup("MTH/PT"));
      c[i].setCode(new Code("NOCODE"));
      c[i].setString("Test Atom 2");
      c[i].setStatus('R');
      c[i].setReleased('N');
      c[i].setTobereleased('Y');
      c[i].setLanguage(data_source.getLanguage("ENG"));
      // Connect atom to concept (cs2) and concept (cs2) to atom
      c[i].setConcept(cs2);
      cs2.addAtom(c[i]);
    }

    //
    // 2. MolecularInsertConcept(cs2)
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #2.");
    molecule_ids.add(new Integer(
        performAction(log, new MolecularInsertConceptAction(cs2), authority,
                      integrity_vector, change_status, transaction_id, work_id,
                      data_source)));
    progress += pcount;
    context.put("progress", new Integer(progress));

    Ticket ticket = Ticket.getActionsTicket();
    data_source.populateConcept(cs1, ticket);
    data_source.populateConcept(cs2, ticket);

    Atom[] a = cs1.getAtoms();
    Atom[] b = cs2.getAtoms();

    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < a.length; j++) {
        // Create relationship object
        Relationship r1 = new Relationship.Default();
        r1.setConcept(cs1);
        r1.setAtom(a[i]);
        r1.setRelatedConcept(cs2);
        r1.setRelatedAtom(b[j]);
        r1.setName("RT");
        r1.setAttribute("analyzes");
        r1.setSource(data_source.getSource("MTH"));
        r1.setSourceOfLabel(data_source.getSource("MTH"));
        r1.setLevel('S');
        r1.setStatus('R');
        r1.setReleased('N');
        r1.setTobereleased('Y');

        molecule_ids.add(new Integer(
            performAction(log, new MolecularInsertRelationshipAction(r1),
                          authority,
                          integrity_vector, change_status, transaction_id,
                          work_id, data_source)));
        progress += pcount;
        context.put("progress", new Integer(progress));

      }
    }

    //
    // 3. MolecularMergeAction cs1, cs2
    //

    MEMEToolkit.trace("ActionSequences.main() - Performing action sequence #3.");
    molecule_ids.add(new Integer(
        performAction(log, new MolecularMergeAction(cs1, cs2), authority,
                      integrity_vector, change_status, transaction_id, work_id,
                      data_source)));
    progress += pcount;
    context.put("progress", new Integer(progress));

    //
    // Undo action sequences
    //

    for (int i = molecule_ids.size() - 1; i > 0; i--) {
      MEMEToolkit.trace(
          "ActionSequences.main() - Performing undo action sequence #" + i +
          ".");
      int molecule_id = ( (Integer) molecule_ids.get(i)).intValue();
      MolecularAction ma = data_source.getFullMolecularAction(molecule_id);
      performUndo(log, ma, data_source);
      progress += pcount;
      context.put("progress", new Integer(progress));
    }

    context.put("progress", new Integer(100));

    log.append("\n");
    log.append(
        "-----------------------------------------------------------------\n");
    log.append("Finished ... (")
        .append(new Date())
        .append(")\n");
    log.append(
        "-----------------------------------------------------------------\n");

    request.addReturnValue(new Parameter.Default("elapsed_time",
                                                 MEMEToolkit.timeToString(
        MEMEToolkit.timeDifference(new Date(), start))));
  }

  /**
   * Implements action sequence 3.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to perform sequence 3
   */
  private void performSequence3(SessionContext context) throws MEMEException {

    // Get Service Request
    MEMEServiceRequest request = context.getServiceRequest();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    //
    // Molecular action parameters
    //
    Authority authority = new Authority.Default("L-MEME4");
    EnforcableIntegrityVector integrity_vector = new EnforcableIntegrityVector();
    boolean change_status = true;

    NextIdentifierAction nia = NextIdentifierAction.newSetNextIdentifierAction(
        MolecularTransaction.class);
    data_source.getActionEngine().processAction(nia);
    Identifier transaction_id = nia.getNextIdentifier();

    nia = NextIdentifierAction.newSetNextIdentifierAction(WorkLog.class);
    data_source.getActionEngine().processAction(nia);
    Identifier work_id = nia.getNextIdentifier();

    Date start = new Date();

    StringBuffer log = (StringBuffer) context.get("log");
    log.append(
        "-----------------------------------------------------------------\n");
    log.append("Starting ... (")
        .append(start)
        .append(")\n");
    log.append(
        "-----------------------------------------------------------------\n");
    log.append("  Transaction ID: ")
        .append(transaction_id.toString()).append("\n");
    log.append("  Work ID: ")
        .append(work_id.toString()).append("\n");
    log.append("  Database: ")
        .append(data_source.getDataSourceName()).append("\n\n");

    int progress = 0;
    context.put("progress", new Integer(progress));
    List molecule_ids = new ArrayList();

    String query =
        "SELECT concept_id FROM concept_status" +
        " WHERE concept_id > 1000000" +
        " AND rownum < 101";

    List concept_ids = new ArrayList();
    int ctr = 0;

    // Execute the query
    try {
      PreparedStatement pstmt = data_source.prepareStatement(query);
      ResultSet rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
        concept_ids.add(new Concept.Default(rs.getInt("CONCEPT_ID")));
        ctr++;
      }

      // Close statement
      pstmt.close();

    } catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Failed to get dataset fields.", this, e);
      dse.setDetail("query", query);
      throw dse;
    }

    int pcount = 100 / ctr;

    for (int i = 0; i < concept_ids.size(); i += 2) {
      Concept cs1 = (Concept) concept_ids.get(i);
      Concept cs2 = (Concept) concept_ids.get(i + 1);
      molecule_ids.add(new Integer(
          performAction(log, new MolecularMergeAction(cs1, cs2), authority,
                        integrity_vector, change_status, transaction_id,
                        work_id, data_source)));
      progress += pcount;
      context.put("progress", new Integer(progress));
    }

    //
    // Undo action sequences
    //

    for (int i = molecule_ids.size() - 1; i > 0; i--) {
      MEMEToolkit.trace(
          "ActionSequences.main() - Performing undo action sequence #" + i +
          ".");
      int molecule_id = ( (Integer) molecule_ids.get(i)).intValue();
      MolecularAction ma = data_source.getFullMolecularAction(molecule_id);
      performUndo(log, ma, data_source);
      progress += pcount;
      context.put("progress", new Integer(progress));
    }

    context.put("progress", new Integer(100));

    log.append("\n");
    log.append(
        "-----------------------------------------------------------------\n");
    log.append("Finished ... (")
        .append(new Date())
        .append(")\n");
    log.append(
        "-----------------------------------------------------------------\n");

    request.addReturnValue(new Parameter.Default("elapsed_time",
                                                 MEMEToolkit.timeToString(
        MEMEToolkit.timeDifference(new Date(), start))));

  }

  /**
   * Performs specified {@link MolecularAction}.
   * @param log the of {@link StringBuffer}
   * @param ma the {@link MolecularAction}
   * @param authority the {@link Authority}
   * @param integrity_vector the {@link IntegrityVector}
   * @param status the status
   * @param transaction_id the {@link Identifier}
   * @param work_id the {@link Identifier}
   * @param data_source the {@link MEMEDataSource}
   * @return the id of the {@link MolecularAction}
   * @throws MEMEException if failed to perform action
   */
  private int performAction(StringBuffer log, MolecularAction ma,
                            Authority authority,
                            EnforcableIntegrityVector integrity_vector,
                            boolean status,
                            Identifier transaction_id,
                            Identifier work_id,
                            MEMEDataSource data_source) throws MEMEException {

    int molecule_id = 0;

    // Set action parameter
    ma.setAuthority(authority);
    ma.setIntegrityVector(integrity_vector);
    ma.setChangeStatus(status);
    ma.setTransactionIdentifier(transaction_id);
    ma.setWorkIdentifier(work_id);

    data_source.getActionEngine().processAction(ma);
    molecule_id = ma.getIdentifier().intValue();

    AtomicAction[] aas = ma.getAtomicActions();
    boolean c_flag = false;
    boolean r_flag = false;
    boolean a_flag = false;
    for (int j = 0; j < aas.length; j++) {
      if (aas[j].getAffectedTable().equals("C")) {
        c_flag = true;
      }
      if (aas[j].getAffectedTable().equals("R")) {
        r_flag = true;
      }
      if (aas[j].getAffectedTable().equals("A")) {
        a_flag = true;
      }
    }
    String source = "";
    String target = "";
    if (ma.getSource() != null) {
      source = " " + ma.getSource().getIdentifier().toString();
    }
    if (ma.getTarget() != null) {
      target = " " + ma.getTarget().getIdentifier().toString();
    }
    log.append("    ")
        .append(ma.getActionName())
        .append(source)
        .append(target)
        .append(" (");
    if (c_flag) {
      log.append("C");
    }
    if (r_flag) {
      log.append("R");
    }
    if (a_flag) {
      log.append("A");
    }
    log.append(") performed by ").append(ma.getAuthority())
        .append(" on ").append(ma.getTimestamp())
        .append("\n");
    if (ma.isUndone()) {
      log.append("      ")
          .append("(undone by ")
          .append(ma.getAuthority())
          .append(" on ").append(ma.getTimestamp())
          .append(")\n");
    }

    return molecule_id;
  }

  /**
   * Performs an undo action.
   * @param log the {@link StringBuffer}
   * @param ma the {@link MolecularAction}
   * @param data_source the {@link MEMEDataSource}
   * @throws MEMEException if failed to perform action
   */
  private void performUndo(StringBuffer log, MolecularAction ma,
                           MEMEDataSource data_source) throws MEMEException {

    data_source.getActionEngine().processAction(
        (MolecularAction)
        data_source.getFullMolecularAction(
        ma.getIdentifier().intValue()).getInverseAction());

    AtomicAction[] aas = ma.getAtomicActions();
    boolean c_flag = false;
    boolean r_flag = false;
    boolean a_flag = false;
    for (int j = 0; j < aas.length; j++) {
      if (aas[j].getAffectedTable().equals("C")) {
        c_flag = true;
      }
      if (aas[j].getAffectedTable().equals("R")) {
        r_flag = true;
      }
      if (aas[j].getAffectedTable().equals("A")) {
        a_flag = true;
      }
    }
    String source = "";
    String target = "";
    if (ma.getSource() != null) {
      source = " " + ma.getSource().getIdentifier().toString();
    }
    if (ma.getTarget() != null) {
      target = " " + ma.getTarget().getIdentifier().toString();
    }
    log.append("    ")
        .append(ma.getActionName())
        .append(source)
        .append(target)
        .append(" (");
    if (c_flag) {
      log.append("C");
    }
    if (r_flag) {
      log.append("R");
    }
    if (a_flag) {
      log.append("A");
    }
    log.append(") performed by ").append(ma.getAuthority())
        .append(" on ").append(ma.getTimestamp())
        .append("\n");
    if (ma.isUndone()) {
      log.append("      ")
          .append("(undone by ")
          .append(ma.getAuthority())
          .append(" on ").append(ma.getTimestamp())
          .append(")\n");
    }
  }
}
