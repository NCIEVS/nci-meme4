/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme
 * Object:  MEMERelaEditorKit
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * This client API is a quick and dirty implementation of "molecular actions"
 * functionality needed for Vladimir's Rela Editor application.  Aside from
 * that use, THIS CLASS SHOULD NEVER BE USED.
 * <p>
 * To use the editor kit you will need to instantiate it by passing
 * the constructor a valid <i>mid services</i> name.
 * <p>
 * There are several methods which allow the application to ask for
 * valid sets of relationship names, relationship attributes, or sources.
 * The purpose of these methods is to allow the application to supply
 * a data-driven select list instead of having users enter values manually.
 * <p>
 * The remaining editor kit methods allow you to insert relationships
 * between various different identifiers, or to make changes to known
 * relationships that already span those identifiers.  All relationships
     * are inserted in an unreleasable state, so a <code>changeRelationshipTBR</code>
 * method is supplied to allow you to make these relationships
 * releasable.
 * <p>
 * It is wort noting that if the
 * <code>source</code> or <code>source_of_label</code> parameters passed
 * to the insert methods do not exist in the <code>source_rank</code> table
 * but they do match the form <code>'NLM%'</code> then a special private
 * procedure is called to add that new source to <code>source_rank</code>
 * with a rank equal to the <code>NLM</code> source.  These sources will
 * have to be more fully edited later by using the
 * <a href="/cgi-lti-oracle/src_info.cgi">SRC Info editor</a><p>
 *
 * Here is an example of how to use the kit.<p>
 *
 * <pre>
 * import gov.nih.nlm.meme.MEMERelaEditorKit;
 * import java.sql.*;
 * ...
 *
 * public class Test {
 *
 *   ...
 *
 *   public void useEditorKit() throws MEMEException {
 *     String mid = "editing-db";
 *
 *     // Create editor kit
 *     MEMERelaEditorKit editor_kit = new MEMERelaEditorKit(mid);
 *
 *     // Call accessor methods
 *     String [] rels = editor_kit.getRelationshipNames();
 *     String [] relas = editor_kit.getRelationshipAttributes();
 *     String [] sources = editor_kit.getSources();
 *     String [] tobereleased = editor_kit.getTobereleased();
 *
 *     // Add a new RELA to the system
 *     // It only gets added if it is not already there
 *     insertRelationshipAttribute("tradename_of","has_tradename");
 *
 *     // Update a relationship using CUIs
 *     try {
 *        editor_kit.processRelationshipBetweenCUIs("C0000001","C0000002",
 *          "BT", "mapped_to", "NLM01", "NLM01", "F01-BAC", 12345);
 *     } catch (MEMEException e) {
 *        System.err.println("Error processing relationship (" +
 *        e.getMessage() + ")");
 *     }
 *
 *     // Update a relationship using atom ids
 *     try {
 *        editor_kit.processRelationshipBetweenAtoms(12345,12346,
 *          "BT", "mapped_to", "NLM01", "NLM01", "F01-BAC", 12345);
 *     } catch (MEMEException e) {
 *        System.err.println("Error processing relationship (" +
 *        e.getMessage() + ")");
 *     }
 *
 *     // Update a relationship using concept ids
 *     try {
 *        editor_kit.processRelationshipBetweenConcepts(102345,102346,
 *          "BT", "mapped_to", "NLM01", "NLM01", "F01-BAC", 12345);
 *     } catch (MEMEException e) {
 *        System.err.println("Error processing relationship (" +
 *        e.getMessage() + ")");
 *     }
 *
 *     // Insert a relationship using CUIs
 *     int rel_id = 0;
 *     try {
 *        rel_id = editor_kit.insertRelationshipBetweenCUIs("C0000001","C0000002",
 *          "BT", "mapped_to", "NLM01", "NLM01", "F01-BAC");
 *
 *     } catch (MEMEException e) {
 *        System.err.println("Error inserting relationship (" +
 *        e.getMessage() + ")");
 *     }
 *
 *     // Insert a relationship using atom ids
 *     int rel_id = 0;
 *     try {
 *        rel_id = editor_kit.processRelationshipBetweenAtoms(12345, 12346,
 *          "BT", "mapped_to", "NLM01", "NLM01", "F01-BAC");
 *
 *     } catch (MEMEException e) {
 *        System.err.println("Error inserting relationship (" +
 *        e.getMessage() + ")");
 *     }
 *
 *     // Insert a relationship using atom ids
 *     int rel_id = 0;
 *     try {
 *        rel_id = editor_kit.processRelationshipBetweenConcepts(12345, 12346,
 *          "BT", "mapped_to", "NLM01", "NLM01", "F01-BAC");
 *
 *     } catch (MEMEException e) {
 *        System.err.println("Error inserting relationship (" +
 *        e.getMessage() + ")");
 *     }
 *
 *     // Change relationship's tobereleased
 *     try {
 *        editor_kit.changeRelationshipTBR(rel_id,"F01-BAC", "n");
 *
 *     } catch (MEMEException e) {
     *        System.err.println("Error changing relationship tobereleased value (" +
 *        e.getMessage() + ")");
 *     }
 *
 *   }; // end useEditorKit
 *
 * } // end class Test
 * </pre>
 *
 * Each of the methods in this class throws an MEMEException if there is
 * any kind of error.  This class no longer directly connects to
 * the database, instead it accesses the MEME application server
 * by extending the standard Client API.
 * <p>
 * And finally, each of the <code>insertXXX</code> and <code>processXXX</code>
 * methods specify the identifier type in the method name, but in the past
 * the methods <code>insertRelationship()</code> and <code>processRelationship()</code>
 * were overloaded to support both CUIs and atom ids.  For backwards compatability
 * we have kept these methods and they still function as they did in the past,
 * however the methods shown in the example above are now preferred.
 *
 * @see ClientAPI
 * @author MEME Group
 */

public class MEMERelaEditorKit extends ClientAPI {

  //
  // Fields
  //

  /**
   * Connection to the MID.
   */
  private String mid_service;

  //
  // Constructors
  //

  /**
   * Instantiate the editor kit with a default mid connection.
   * @param mid_service A valid mid service name.
   * @throws MEMEException if the class could not be instantiated.
   */
  public MEMERelaEditorKit(String mid_service) throws MEMEException {
    super();
    this.mid_service = mid_service;
  }

  //
  // Accessor Methods
  //

  /**
   * Get a list of valid <code>relationship_name</code> values.
   * @return An array valid <code>relationship_name</code> values.
   * @throws MEMEException
   */
  public String[] getRelationshipNames() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "getRelationshipNames"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (String[]) request.getReturnValue("rel_names").getValue();
  }

  /**
   * Add a new relationship attribute (and its inverse) to the system.
   * You only need to call this method for one direction.  For example,
   * if you call
   * <code>insertRelationshipAttribute("has_tradename","tradename_of");</code>
   * then you do not need to also call,
   * <code>insertRelationshipAttribute("tradename_of","has_tradename");</code>
   *
   * @param rela A <code>relationship_attribute</code> value to add
   * @param inverse The inverse <code>relationship_attribute</code> value
   *        corresponding with <code>rela</code>
   * @throws MEMEException
   */
  public void insertRelationshipAttribute(String rela, String inverse) throws
      MEMEException {
    insertRelationshipAttribute(rela, inverse, 1);
  }

  /**
   * Add a new relationship attribute (and its inverse) to the system.
   * You only need to call this method for one direction.  For example,
   * if you call
   * <code>insertRelationshipAttribute("has_tradename","tradename_of");</code>
   * then you do not need to also call,
   * <code>insertRelationshipAttribute("tradename_of","has_tradename");</code>
   *
   * @param rela A <code>relationship_attribute</code> value to add
   * @param inverse The inverse <code>relationship_attribute</code> value
   *        corresponding with <code>rela</code>
   * @param rank the rank, typically 1
   * @throws MEMEException
   */
  public void insertRelationshipAttribute(String rela, String inverse, int rank) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "insertRelationshipAttribute"));
    request.addParameter(new Parameter.Default("rela", rela));
    request.addParameter(new Parameter.Default("inverse", inverse));
    request.addParameter(new Parameter.Default("rank", rank));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Remove a rationship attribute (and its inverse) to the system.
   * You only need to call this method for one direction.  For example,
   * if you call
   * <code>insertRelationshipAttribute("has_tradename","tradename_of");</code>
   * then you do not need to also call,
   * <code>insertRelationshipAttribute("tradename_of","has_tradename");</code>
   *
   * @param rela A <code>relationship_attribute</code> value to remove
   * @throws MEMEException
   */
  public void removeRelationshipAttribute(String rela) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "removeRelationshipAttribute"));
    request.addParameter(new Parameter.Default("rela", rela));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Add a new relationship attribute (and its inverse) to the system.
   * You only need to call this method for one direction.  For example,
   * if you call
   * <code>insertRelationshipAttribute("has_tradename","tradename_of");</code>
   * then you do not need to also call,
   * <code>insertRelationshipAttribute("tradename_of","has_tradename");</code>
   *
   * @param name The relationship name
   * @param inverse_name The relationship inverse name
   * @param weak The <code>boolean</code> represents weak flag
   * @param long_name The relationship long name
   * @param inverse_long_name The relationship inverse long name
   * @param release_name The relationship release name
   * @param inverse_release_name The relationship inverse release name
   * @throws MEMEException
   */
  public void insertRelationshipName(String name, String inverse_name,
                                     boolean weak,
                                     String long_name, String inverse_long_name,
                                     String release_name,
                                     String inverse_release_name) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "insertRelationshipName"));
    request.addParameter(new Parameter.Default("name", name));
    request.addParameter(new Parameter.Default("inverse_name", inverse_name));
    request.addParameter(new Parameter.Default("weak", weak));
    request.addParameter(new Parameter.Default("long_name", long_name));
    request.addParameter(new Parameter.Default("inverse_long_name",
                                               inverse_long_name));
    request.addParameter(new Parameter.Default("release_name", release_name));
    request.addParameter(new Parameter.Default("inverse_release_name",
                                               inverse_release_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Remove a rationship name (and its inverse) to the system.
   * You only need to call this method for one direction.  For example,
   * if you call
   * <code>insertRelationshipAttribute("has_tradename","tradename_of");</code>
   * then you do not need to also call,
   * <code>insertRelationshipAttribute("tradename_of","has_tradename");</code>
   *
   * @param name A <code>relationship_name</code> value to remove
   * @throws MEMEException
   */
  public void removeRelationshipName(String name) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "removeRelationshipName"));
    request.addParameter(new Parameter.Default("name", name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Get a list of valid <code>relationship_attribute</code> values.
   * @return An array of valid <code>relationship_attribute</code> values.
   * @throws MEMEException
   */
  public String[] getRelationshipAttributes() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "getRelationshipAttributes"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (String[]) request.getReturnValue("rela_attributes").getValue();
  }

  /**
   * Get a list of valid <code>source</code> values.
   * @return a array of valid <code>source</code> values.
   * @throws MEMEException
   */
  public String[] getSources() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "getSources"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (String[]) request.getReturnValue("sources").getValue();

  }

  /**
   * Get a list of valid <code>tobereleased</code> values.
   * @return An array of valid <code>tobereleased</code> values.
   * @throws MEMEException
   */
  public String[] getTobereleased() throws MEMEException {
    return new String[] {
        "Y", "y", "N", "n"};
  }

  /**
   * Calls {@link #processRelationshipBetweenCUIs(String,String,String,String,String,String,String,int)},
   * for backwards compatability.
   * @param pr_cui_1 is the possible CUI that this relationship connects.
   * @param pr_cui_2 is the possible CUI that this relationship connects.
   * @param pr_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param pr_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param pr_source is the source asserting the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param pr_source_of_label is the source asserting the nature of the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param pr_authority is the one responsible for the Update/Insert actions.
   * @param pr_rel_id is the ID of the relationships to be processed.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if failed to process relationship.
   */
  public int processRelationship(
      String pr_cui_1, String pr_cui_2,
      String pr_rel_name, String pr_rel_attr, String pr_source,
      String pr_source_of_label, String pr_authority, int pr_rel_id) throws
      MEMEException {

    return processRelationshipBetweenCUIs(pr_cui_1, pr_cui_2,
                                          pr_rel_name, pr_rel_attr,
                                          pr_source, pr_source_of_label,
                                          pr_authority, pr_rel_id);
  }

  /**
   * Calls {@link #processRelationshipBetweenAtoms(int,int,String,String,String,String,String,int)},
   * for backwards compatability.
   * @throws MEMEException if failed to process relationship.
   * @param pr_atom_id_1 is the possible CUI that this relationship connects.
   * @param pr_atom_id_2 is the possible CUI that this relationship connects.
   * @param pr_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param pr_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param pr_source is the source asserting the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param pr_source_of_label is the source asserting the nature of the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param pr_authority is the one responsible for the Update/Insert actions.
   * @param pr_rel_id is the ID of the relationships to be processed.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if failed to process relationship.
   */
  public int processRelationship(
      int pr_atom_id_1, int pr_atom_id_2,
      String pr_rel_name, String pr_rel_attr, String pr_source,
      String pr_source_of_label, String pr_authority, int pr_rel_id) throws
      MEMEException {

    return processRelationshipBetweenAtoms(pr_atom_id_1, pr_atom_id_2,
                                           pr_rel_name, pr_rel_attr,
                                           pr_source, pr_source_of_label,
                                           pr_authority, pr_rel_id);
  }

  /**
   * Modifies or inserts a relationship between CUIs.
   * This method is responsible for taking data from an application
   * and either inserting a new source-level relationship or
   * updating an existing one so its fields match the parameters.<p>
   *
   * This procedure must "fake" a <code>MOLECULAR_CHANGE_FIELD</code> action by locking
   * <code>max_tab</code> and calling the appropriate series of atomic actions (<code>MEME_APROCS</code>
   * procedures).<p>
   *
   * It is an update if a source-level relationship match <code>relationship_id</code>. Otherwise,
   * it is an insert if no source-level relationship match <code>relationship_id</code>.<p>
   *
   * @param pr_cui_1 is the possible CUI that this relationship connects.
   * @param pr_cui_2 is the possible CUI that this relationship connects.
   * @param pr_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param pr_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param pr_source is the source asserting the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param pr_source_of_label is the source asserting the nature of the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param pr_authority is the one responsible for the Update/Insert actions.
   * @param pr_rel_id is the ID of the relationships to be processed.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if program validation failed.
   * @see #getRelationshipNames()
   * @see #getRelationshipAttributes()
   */
  public int processRelationshipBetweenCUIs(
      String pr_cui_1, String pr_cui_2,
      String pr_rel_name, String pr_rel_attr, String pr_source,
      String pr_source_of_label, String pr_authority, int pr_rel_id) throws
      MEMEException {

    MEMEToolkit.trace("\tDisplaying arguments value...");
    MEMEToolkit.trace("\t\tcui_1= " + pr_cui_1);
    MEMEToolkit.trace("\t\tcui_2= " + pr_cui_2);
    MEMEToolkit.trace("\t\trelationship_name= " + pr_rel_name);
    MEMEToolkit.trace("\t\trelationship_attribute= " + pr_rel_attr);
    MEMEToolkit.trace("\t\tsource= " + pr_source);
    MEMEToolkit.trace("\t\tsource_of_label= " + pr_source_of_label);
    MEMEToolkit.trace("\t\tauthority= " + pr_authority);
    MEMEToolkit.trace("\t\trelationship_id= " + pr_rel_id);

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "processRelationship"));

    // Add process request parameters
    request.addParameter(new Parameter.Default("pr_cui_1", pr_cui_1));
    request.addParameter(new Parameter.Default("pr_cui_2", pr_cui_2));
    request.addParameter(new Parameter.Default("pr_rel_name", pr_rel_name));
    request.addParameter(new Parameter.Default("pr_rel_attr", pr_rel_attr));
    request.addParameter(new Parameter.Default("pr_source", pr_source));
    request.addParameter(new Parameter.Default("pr_source_of_label",
                                               pr_source_of_label));
    request.addParameter(new Parameter.Default("pr_authority", pr_authority));
    request.addParameter(new Parameter.Default("pr_rel_id", pr_rel_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return request.getReturnValue("process_rel").getInt();
  }

  /**
   * Modifies or inserts a relationship between atoms.
   * This method is responsible for taking data from an application
   * and either inserting a new source-level relationship or
   * updating an existing one so its fields match the parameters.<p>
   *
   * This procedure must "fake" a <code>MOLECULAR_CHANGE_FIELD</code> action by locking
   * <code>max_tab</code> and calling the appropriate series of atomic actions (<code>MEME_APROCS</code>
   * procedures).<p>
   *
   * It is an update if a source-level relationship match <code>relationship_id</code>. Otherwise,
   * it is an insert if no source-level relationship match <code>relationship_id</code>.<p>
   *
       * @param pr_atom_id_1 is the possible atom id that this relationship connects.
       * @param pr_atom_id_2 is the possible atom id that this relationship connects.
   * @param pr_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param pr_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param pr_source is the source asserting the relationship between
   *        <code>atom_id_1</code> and <code>atom_id_2</code>.
   * @param pr_source_of_label is the source asserting the nature of the relationship between
   *        <code>atom_id_1</code> and <code>atom_id_2</code>.
   * @param pr_authority is the one responsible for the Update/Insert actions.
   * @param pr_rel_id is the ID of the relationships to be processed.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if program validation failed.
   * @see #getRelationshipNames()
   * @see #getRelationshipAttributes()
   */
  public int processRelationshipBetweenAtoms(
      int pr_atom_id_1, int pr_atom_id_2,
      String pr_rel_name, String pr_rel_attr, String pr_source,
      String pr_source_of_label, String pr_authority, int pr_rel_id) throws
      MEMEException {

    MEMEToolkit.trace("\tDisplaying arguments value...");
    MEMEToolkit.trace("\t\tatom_id_1= " + pr_atom_id_1);
    MEMEToolkit.trace("\t\tatom_id_2= " + pr_atom_id_2);
    MEMEToolkit.trace("\t\trelationship_name= " + pr_rel_name);
    MEMEToolkit.trace("\t\trelationship_attribute= " + pr_rel_attr);
    MEMEToolkit.trace("\t\tsource= " + pr_source);
    MEMEToolkit.trace("\t\tsource_of_label= " + pr_source_of_label);
    MEMEToolkit.trace("\t\tauthority= " + pr_authority);
    MEMEToolkit.trace("\t\trelationship_id= " + pr_rel_id);

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "processRelationship"));

    // Add process request parameters
    request.addParameter(new Parameter.Default("pr_atom_id_1", pr_atom_id_1));
    request.addParameter(new Parameter.Default("pr_atom_id_2", pr_atom_id_2));
    request.addParameter(new Parameter.Default("pr_rel_name", pr_rel_name));
    request.addParameter(new Parameter.Default("pr_rel_attr", pr_rel_attr));
    request.addParameter(new Parameter.Default("pr_source", pr_source));
    request.addParameter(new Parameter.Default("pr_source_of_label",
                                               pr_source_of_label));
    request.addParameter(new Parameter.Default("pr_authority", pr_authority));
    request.addParameter(new Parameter.Default("pr_rel_id", pr_rel_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return request.getReturnValue("process_rel").getInt();
  }

  /**
   * Modifies or inserts a relationship between atoms.
   * This method is responsible for taking data from an application
   * and either inserting a new concept-level relationship or
   * updating an existing one so its fields match the parameters.<p>
   *
   * This procedure must "fake" a <code>MOLECULAR_CHANGE_FIELD</code> action by locking
   * <code>max_tab</code> and calling the appropriate series of atomic actions (<code>MEME_APROCS</code>
   * procedures).<p>
   *
   * It is an update if a source-level relationship match <code>relationship_id</code>. Otherwise,
   * it is an insert if no source-level relationship match <code>relationship_id</code>.<p>
   *
   * @param pr_concept_id_1 is the possible concept id that this relationship connects.
   * @param pr_concept_id_2 is the possible concept id that this relationship connects.
   * @param pr_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param pr_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param pr_source is the source asserting the relationship between
   *        <code>atom_id_1</code> and <code>atom_id_2</code>.
   * @param pr_source_of_label is the source asserting the nature of the relationship between
   *        <code>atom_id_1</code> and <code>atom_id_2</code>.
   * @param pr_authority is the one responsible for the Update/Insert actions.
   * @param pr_rel_id is the ID of the relationships to be processed.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if program validation failed.
   * @see #getRelationshipNames()
   * @see #getRelationshipAttributes()
   */
  public int processRelationshipBetweenConcepts(
      int pr_concept_id_1, int pr_concept_id_2,
      String pr_rel_name, String pr_rel_attr, String pr_source,
      String pr_source_of_label, String pr_authority, int pr_rel_id) throws
      MEMEException {

    MEMEToolkit.trace("\tDisplaying arguments value...");
    MEMEToolkit.trace("\t\tconcept_id_1= " + pr_concept_id_1);
    MEMEToolkit.trace("\t\tconcept_id_2= " + pr_concept_id_2);
    MEMEToolkit.trace("\t\trelationship_name= " + pr_rel_name);
    MEMEToolkit.trace("\t\trelationship_attribute= " + pr_rel_attr);
    MEMEToolkit.trace("\t\tsource= " + pr_source);
    MEMEToolkit.trace("\t\tsource_of_label= " + pr_source_of_label);
    MEMEToolkit.trace("\t\tauthority= " + pr_authority);
    MEMEToolkit.trace("\t\trelationship_id= " + pr_rel_id);

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "processRelationship"));

    // Add process request parameters
    request.addParameter(new Parameter.Default("pr_concept_id_1",
                                               pr_concept_id_1));
    request.addParameter(new Parameter.Default("pr_concept_id_2",
                                               pr_concept_id_2));
    request.addParameter(new Parameter.Default("pr_rel_name", pr_rel_name));
    request.addParameter(new Parameter.Default("pr_rel_attr", pr_rel_attr));
    request.addParameter(new Parameter.Default("pr_source", pr_source));
    request.addParameter(new Parameter.Default("pr_source_of_label",
                                               pr_source_of_label));
    request.addParameter(new Parameter.Default("pr_authority", pr_authority));
    request.addParameter(new Parameter.Default("pr_rel_id", pr_rel_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return request.getReturnValue("process_rel").getInt();
  }

  /**
   * Calls {@link #insertRelationshipBetweenCUIs(String,String,String,String,String,String,String)},
   * for backwards compatability.
   * @param ir_cui_1 is the possible CUI that this relationship connects.
   * @param ir_cui_2 is the possible CUI that this relationship connects.
   * @param ir_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param ir_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param ir_source is the source asserting the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param ir_source_of_label is the source asserting the nature of the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param ir_authority is the one responsible for the Update/Insert actions.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if failed to process relationship.
   */
  public int insertRelationship(
      String ir_cui_1, String ir_cui_2,
      String ir_rel_name, String ir_rel_attr, String ir_source,
      String ir_source_of_label, String ir_authority) throws MEMEException {
    return insertRelationshipBetweenCUIs(
        ir_cui_1, ir_cui_2, ir_rel_name, ir_rel_attr,
        ir_source, ir_source_of_label, ir_authority);
  }

  /**
   * Calls {@link #insertRelationshipBetweenAtoms(int,int,String,String,String,String,String)},
   * for backwards compatability.
   * @throws MEMEException if failed to process relationship.
   * @param ir_id_1 is the possible CUI that this relationship connects.
   * @param ir_id_2 is the possible CUI that this relationship connects.
   * @param ir_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param ir_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param ir_source is the source asserting the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param ir_source_of_label is the source asserting the nature of the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param ir_authority is the one responsible for the Update/Insert actions.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if failed to process relationship.
   */
  public int insertRelationship(
      int ir_id_1, int ir_id_2,
      String ir_rel_name, String ir_rel_attr, String ir_source,
      String ir_source_of_label, String ir_authority) throws MEMEException {
    return insertRelationshipBetweenAtoms(
        ir_id_1, ir_id_2, ir_rel_name, ir_rel_attr,
        ir_source, ir_source_of_label, ir_authority);
  }

  /**
   * Inserts a relationship between two CUIs (<B>SERVER CALL</B>).
   * This method is responsible for taking data from an application
   * and inserting a new source level relationship between cuis.<p>
   *
       * This procedure must "fake" a <code>MOLECULAR_INSERT</code> action by locking
   * <code>max_tab</code> and calling the appropriate series of atomic actions (<code>MEME_APROCS</code>
   * procedures).<p>
   *
   * The following defaults are to be assumed for the unspecified
   * <code>relationships</code> fields:<br>
   *    <ul><code>relationship_level</code> = "S"</ul>
   *    <ul><code>status</code> = "R"</ul>
   *    <ul><code>atom_id_1</code> = <code>atom_id_2</code> = 0</ul>
   *    <ul><code>generated</code> = "N"</ul>
   *    <ul><code>dead</code> = "N"</ul>
   *    <ul><code>released</code> = "N"</ul>
   *    <ul><code>tobereleased</code> = "Y"</ul>
   *    <ul><code>suppressible</code> = "N"</ul><p>
   *
   * @param ir_cui_1 is the possible CUI that this relationship connects.
   * @param ir_cui_2 is the possible CUI that this relationship connects.
   * @param ir_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param ir_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param ir_source is the source asserting the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param ir_source_of_label is the source asserting the nature of the relationship between
   *        <code>cui_1</code> and <code>cui_2</code>.
   * @param ir_authority is the one responsible for the Update/Insert actions.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if program validation failed.
   * @see #getRelationshipNames()
   * @see #getRelationshipAttributes()
   */
  public int insertRelationshipBetweenCUIs(
      String ir_cui_1, String ir_cui_2,
      String ir_rel_name, String ir_rel_attr, String ir_source,
      String ir_source_of_label, String ir_authority) throws MEMEException {

    // Parameter validation
    if (ir_cui_1 == null || ir_cui_2 == null || ir_rel_name == null ||
        ir_source == null || ir_source_of_label == null ||
        ir_authority == null) {
      throw new MEMEException("Invalid null parameter(s) found.");
    }

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "insertRelationship"));

    // Add process request parameters
    request.addParameter(new Parameter.Default("ir_cui_1", ir_cui_1));
    request.addParameter(new Parameter.Default("ir_cui_2", ir_cui_2));
    request.addParameter(new Parameter.Default("ir_rel_name", ir_rel_name));
    request.addParameter(new Parameter.Default("ir_rel_attr", ir_rel_attr));
    request.addParameter(new Parameter.Default("ir_source", ir_source));
    request.addParameter(new Parameter.Default("ir_source_of_label",
                                               ir_source_of_label));
    request.addParameter(new Parameter.Default("ir_authority", ir_authority));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return request.getReturnValue("insert_rel").getInt();
  }

  /**
   * Inserts a relationship between two atoms (<B>SERVER CALL</B>).
   * This method is responsible for taking data from an application
   * and inserting a new source level relationship between atoms.<p>
   *
       * This procedure must "fake" a <code>MOLECULAR_INSERT</code> action by locking
   * <code>max_tab</code> and calling the appropriate series of atomic actions (<code>MEME_APROCS</code>
   * procedures).<p>
   *
   * The following defaults are to be assumed for the unspecified
   * <code>relationships</code> fields:<br>
   *    <ul><code>relationship_level</code> = "S"</ul>
   *    <ul><code>status</code> = "R"</ul>
   *    <ul><code>atom_id_1</code> = <code>atom_id_2</code> = 0</ul>
   *    <ul><code>generated</code> = "N"</ul>
   *    <ul><code>dead</code> = "N"</ul>
   *    <ul><code>released</code> = "N"</ul>
   *    <ul><code>tobereleased</code> = "Y"</ul>
   *    <ul><code>suppressible</code> = "N"</ul><p>
   *
       * @param ir_atom_id_1 is the possible atom id that this relationship connects.
       * @param ir_atom_id_2 is the possible atom id that this relationship connects.
   * @param ir_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param ir_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param ir_source is the source asserting the relationship between
   *        <code>atom_id_1</code> and <code>atom_id_2</code>.
   * @param ir_source_of_label is the source asserting the nature of the relationship between
   *        <code>atom_id_1</code> and <code>atom_id_2</code>.
   * @param ir_authority is the one responsible for the Update/Insert actions.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if program validation failed.
   * @see #getRelationshipNames()
   * @see #getRelationshipAttributes()
   */
  public int insertRelationshipBetweenAtoms(
      int ir_atom_id_1, int ir_atom_id_2,
      String ir_rel_name, String ir_rel_attr, String ir_source,
      String ir_source_of_label, String ir_authority) throws MEMEException {

    // Parameter validation
    if (ir_atom_id_1 < 0 || ir_atom_id_2 < 0 || ir_rel_name == null ||
        ir_source == null || ir_source_of_label == null ||
        ir_authority == null) {
      throw new MEMEException("Invalid null parameter(s) found.");
    }

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "insertRelationship"));

    // Add process request parameters
    request.addParameter(new Parameter.Default("ir_atom_id_1", ir_atom_id_1));
    request.addParameter(new Parameter.Default("ir_atom_id_2", ir_atom_id_2));
    request.addParameter(new Parameter.Default("ir_rel_name", ir_rel_name));
    request.addParameter(new Parameter.Default("ir_rel_attr", ir_rel_attr));
    request.addParameter(new Parameter.Default("ir_source", ir_source));
    request.addParameter(new Parameter.Default("ir_source_of_label",
                                               ir_source_of_label));
    request.addParameter(new Parameter.Default("ir_authority", ir_authority));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return request.getReturnValue("insert_rel").getInt();
  }

  /**
   * Inserts a relationship between two concepts (<B>SERVER CALL</B>).
   * This method is responsible for taking data from an application
   * and inserting a new concept level relationship between concepts.<p>
   *
       * This procedure must "fake" a <code>MOLECULAR_INSERT</code> action by locking
   * <code>max_tab</code> and calling the appropriate series of atomic actions (<code>MEME_APROCS</code>
   * procedures).<p>
   *
   * The following defaults are to be assumed for the unspecified
   * <code>relationships</code> fields:<br>
   *    <ul><code>relationship_level</code> = "C"</ul>
   *    <ul><code>status</code> = "R"</ul>
   *    <ul><code>atom_id_1</code> = 0<code>atom_id_2</code> = 0</ul>
   *    <ul><code>generated</code> = "N"</ul>
   *    <ul><code>dead</code> = "N"</ul>
   *    <ul><code>released</code> = "N"</ul>
   *    <ul><code>tobereleased</code> = "Y"</ul>
   *    <ul><code>suppressible</code> = "N"</ul><p>
   *
   * @param ir_concept_id_1 is the possible concept id that this relationship connects.
   * @param ir_concept_id_2 is the possible concept id that this relationship connects.
   * @param ir_rel_name is the relationship name that should be one of the values returned by
   *        <code>getRelationshipNames</code>.
   * @param ir_rel_attr is the relationship attribute that should be one of the values returned by
   *        <code>getRelationshipAttributes</code>.
   * @param ir_source is the source asserting the relationship between
   *        <code>atom_id_1</code> and <code>atom_id_2</code>.
   * @param ir_source_of_label is the source asserting the nature of the relationship between
   *        <code>atom_id_1</code> and <code>atom_id_2</code>.
   * @param ir_authority is the one responsible for the Update/Insert actions.
   * @return <code>relationship_id</code>.
   * @throws MEMEException if program validation failed.
   * @see #getRelationshipNames()
   * @see #getRelationshipAttributes()
   */
  public int insertRelationshipBetweenConcepts(
      int ir_concept_id_1, int ir_concept_id_2,
      String ir_rel_name, String ir_rel_attr, String ir_source,
      String ir_source_of_label, String ir_authority) throws MEMEException {

    // Parameter validation
    if (ir_concept_id_1 < 0 || ir_concept_id_2 < 0 || ir_rel_name == null ||
        ir_source == null || ir_source_of_label == null ||
        ir_authority == null) {
      throw new MEMEException("Invalid null parameter(s) found.");
    }

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "insertRelationship"));

    // Add process request parameters
    request.addParameter(new Parameter.Default("ir_concept_id_1",
                                               ir_concept_id_1));
    request.addParameter(new Parameter.Default("ir_concept_id_2",
                                               ir_concept_id_2));
    request.addParameter(new Parameter.Default("ir_rel_name", ir_rel_name));
    request.addParameter(new Parameter.Default("ir_rel_attr", ir_rel_attr));
    request.addParameter(new Parameter.Default("ir_source", ir_source));
    request.addParameter(new Parameter.Default("ir_source_of_label",
                                               ir_source_of_label));
    request.addParameter(new Parameter.Default("ir_authority", ir_authority));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return request.getReturnValue("insert_rel").getInt();
  }

  /**
       * Changes the releasability of an existing relationship (<B>SERVER CALL</B>).
   * This method is responsible for taking data from an application
   * and changing relationship's tobereleased value.<p>
   * @param crtbr_rel_id is the ID of the relationships to be processed.
   * @param crtbr_authority is the one responsible for the change actions.
   * @param crtbr_tbr is the new value of tobereleased.
   * @throws MEMEException if program validation failed.
   * @see #getTobereleased()
   */
  public void changeRelationshipTBR(
      int crtbr_rel_id, String crtbr_authority, String crtbr_tbr) throws
      MEMEException {

    // Parameter validation
    if (crtbr_rel_id < 0 || crtbr_authority == null || crtbr_tbr == null) {
      throw new MEMEException("Invalid parameter(s) found.");
    }

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MEMERelaEditorService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function",
                                               "changeRelationshipTBR"));

    // Add process request parameters
    request.addParameter(new Parameter.Default("crtbr_rel_id", crtbr_rel_id));
    request.addParameter(new Parameter.Default("crtbr_authority",
                                               crtbr_authority));
    request.addParameter(new Parameter.Default("crtbr_tbr", crtbr_tbr));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();

    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

}
