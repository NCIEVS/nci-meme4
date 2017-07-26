/*****************************************************************************
 * Package: gov.nih.nlm.meme.client
 * Object:  MappingClient
 *
 * 03/04/2009 TTN (1-KO9BH): replace attribute only if it's value changed.
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.MapObject;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.common.Mapping;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * Supports editing operations on {@link MapSet}s.
 *
 * @author MEME Group
 */
public class MappingClient extends ActionClient {

  //
  // Constructors
  //

  /**
       * Instantiates an {@link MappingClient} connected to the default mid service.
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated
   */
  public MappingClient() throws MEMEException {
    this("editing-db");
  }

  /**
       * Instantiates an {@link MappingClient} connected to the specified mid service.
   * @param service A service name.
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated
   */
  public MappingClient(String service) throws MEMEException {
    super(service);
  }

  //
  // Methods
  //

  /**
   * Returns all {@link Mapping}s.
   * @param map_set an object {@link MapSet}
   * @param start the starting point of a data block
   * @param end the ending point of a data block
   * @return all {@link Mapping}s
   * @throws MEMEException if anything goes wrong
   */
  public Mapping[] getMappings(MapSet map_set, int start, int end) throws
      MEMEException {

    if (end <= start) {
      BadValueException bve =
          new BadValueException("End must not be less than or equal to start.");
      bve.setDetail("end", String.valueOf(end));
      bve.setDetail("start", String.valueOf(start));
      throw bve;
    }

    MEMEServiceRequest request = getServiceRequest();
    request.setService("MappingService");
    request.addParameter(new Parameter.Default("function", "get_mappings"));
    request.addParameter(new Parameter.Default("concept_id",
                                               map_set.getIdentifier().intValue()));
    request.addParameter(new Parameter.Default("start", start));
    request.addParameter(new Parameter.Default("end", end));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (Mapping[]) request.getReturnValue("mappings").getValue();

  }

  /**
   * Returns the mapping count
   * @param map_set an object {@link MapSet}
   * @return the mapping count
   * @throws MEMEException if anything goes wrong
   */
  public int getMappingCount(MapSet map_set) throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.setService("MappingService");
    request.addParameter(new Parameter.Default("function", "get_mapping_count"));
    request.addParameter(new Parameter.Default("concept_id",
                                               map_set.getIdentifier().intValue()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (int) request.getReturnValue("mapping_count").getInt();

  }

  /**
   * Returns all {@link MapSet}s (<B>SERVER CALL).
   * @return all {@link MapSet}s
   * @throws MEMEException if anything goes wrong
   */
  public MapSet[] getMapSets() throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.setService("MappingService");
    request.addParameter(new Parameter.Default("function", "get_map_sets"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (MapSet[]) request.getReturnValue("map_sets").getValue();

  }

  /**
   * Returns a non fully populated {@link MapSet} with the
   * specified concept {@link Identifier} (<B>SERVER CALL).
   * @param concept_id a concept id.
   * @return the {@link MapSet}
   * @throws MEMEException if anything goes wrong
   */
  public MapSet getMapSetWithoutMappings(int concept_id) throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.setService("MappingService");
    request.addParameter(new Parameter.Default("function",
                                               "get_map_set_no_mappings"));
    request.addParameter(new Parameter.Default("concept_id", concept_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    MapSet mapset = null;
    if (request.getReturnValue("map_set") != null) {
      mapset = (MapSet) request.getReturnValue("map_set").getValue();

    }
    return mapset;

  }

  /**
   * Returns a fully populated {@link MapSet} with the
   * specified concept {@link Identifier} (<B>SERVER CALL).
   * @param concept_id a concept id.
   * @return the {@link MapSet}
   * @throws MEMEException if anything goes wrong
   */
  public MapSet getMapSet(int concept_id) throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.setService("MappingService");
    request.addParameter(new Parameter.Default("function", "get_map_set"));
    request.addParameter(new Parameter.Default("concept_id", concept_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    MapSet mapset = null;
    if (request.getReturnValue("map_set") != null) {
      mapset = (MapSet) request.getReturnValue("map_set").getValue();

    }
    return mapset;

  }

  /**
   * Returns the fully populated {@link MapSet} (<B>SERVER CALL).
   * @param mapset the {@link MapSet}, most likely returned by <codE>getMapSets()</code>
   * @return the fully populatedc {@link MapSet}
   * @throws MEMEException if anything goes wrong
   */
  public MapSet getMapSet(MapSet mapset) throws MEMEException {
    return getMapSet(mapset.getIdentifier().intValue());
  }

  /**
   * Creates a {@link MapSet} from the specified parameters (<B>SERVER CALL).  This uses
   * {@link MolecularAction}s in the background to do the work.
   * @param mapset_name the mapset name
   * @param mapset_id the map set {@link Identifier} (not the concept id)
   * @param from_source the mapped from {@link Source}
   * @param to_source the mapped to {@link Source}
   * @param mapset_source the {@link MapSet} {@link Source}
   * @return an object {@link MapSet}
   * @throws MEMEException if anything goes wrong
   */
  public MapSet createMapSet(String mapset_name,
                             Identifier mapset_id,
                             Source from_source,
                             Source to_source,
                             Source mapset_source) throws MEMEException {

    //
    // Create and configure MapSet
    //
    MapSet mapset = new MapSet.Default();
    mapset.setIdentifier(mapset_id);
    mapset.setMapSetSource(mapset_source);
    mapset.setFromSource(from_source);
    mapset.setToSource(to_source);

    //
    // Add the XM atom
    //
    Atom atom = new Atom.Default();
    atom.setSource(mapset_source);
    atom.setTermgroup(new Termgroup.Default(mapset_source.toString() + "/XM"));
    atom.setString(mapset_name);
    atom.setCode(new Code(mapset_id.toString()));
    atom.setConcept(mapset);
    atom.setStatus('R');
    atom.setReleased('N');
    atom.setTobereleased('Y');
    atom.setLanguage(new Language.Default("English", "ENG"));
    mapset.addAtom(atom);

    //
    // Add the required attribute MAPSETNAME
    //
    Attribute attr = new Attribute.Default();
    attr.setSource(mapset_source);
    attr.setStatus('R');
    attr.setTobereleased('Y');
    attr.setReleased('N');
    attr.setLevel('S');
    attr.setConcept(mapset);
    attr.setName("MAPSETNAME");
    attr.setValue(mapset_name);
    attr.setAtom(atom);
    mapset.addAttribute(attr);

    //
    // Add the required attribute FROMVSAB
    //
    attr = new Attribute.Default();
    attr.setSource(mapset_source);
    attr.setStatus('R');
    attr.setTobereleased('Y');
    attr.setReleased('N');
    attr.setLevel('S');
    attr.setConcept(mapset);
    attr.setName("FROMVSAB");
    attr.setValue(from_source.getSourceAbbreviation());
    attr.setAtom(atom);
    mapset.addAttribute(attr);

    //
    // Add the required attribute FROMRSAB
    //
    attr = new Attribute.Default();
    attr.setSource(mapset_source);
    attr.setStatus('R');
    attr.setTobereleased('Y');
    attr.setReleased('N');
    attr.setLevel('S');
    attr.setConcept(mapset);
    attr.setName("FROMRSAB");
    attr.setValue(from_source.getStrippedSourceAbbreviation());
    attr.setAtom(atom);
    mapset.addAttribute(attr);

    //
    // Add the required attribute TOVSAB
    //
    attr = new Attribute.Default();
    attr.setSource(mapset_source);
    attr.setStatus('R');
    attr.setTobereleased('Y');
    attr.setReleased('N');
    attr.setLevel('S');
    attr.setConcept(mapset);
    attr.setName("TOVSAB");
    attr.setValue(to_source.getSourceAbbreviation());
    attr.setAtom(atom);
    mapset.addAttribute(attr);

    //
    // Add the required attribute TORSAB
    //
    attr = new Attribute.Default();
    attr.setSource(mapset_source);
    attr.setStatus('R');
    attr.setTobereleased('Y');
    attr.setReleased('N');
    attr.setLevel('S');
    attr.setConcept(mapset);
    attr.setName("TORSAB");
    attr.setValue(to_source.getStrippedSourceAbbreviation());
    attr.setAtom(atom);
    mapset.addAttribute(attr);

    //
    // Add the required attribute MAPSETVSAB
    //
    attr = new Attribute.Default();
    attr.setSource(mapset_source);
    attr.setStatus('R');
    attr.setTobereleased('Y');
    attr.setReleased('N');
    attr.setLevel('S');
    attr.setConcept(mapset);
    attr.setName("MAPSETVSAB");
    attr.setValue(mapset_source.getSourceAbbreviation());
    attr.setAtom(atom);
    mapset.addAttribute(attr);

    //
    // Add the required attribute MAPSETRSAB
    //
    attr = new Attribute.Default();
    attr.setSource(mapset_source);
    attr.setStatus('R');
    attr.setTobereleased('Y');
    attr.setReleased('N');
    attr.setLevel('S');
    attr.setConcept(mapset);
    attr.setName("MAPSETRSAB");
    attr.setValue(mapset_source.getStrippedSourceAbbreviation());
    attr.setAtom(atom);
    mapset.addAttribute(attr);

    //
    // Insert the concept
    //
    MolecularAction ma = new MolecularInsertConceptAction(mapset);
    processAction(ma);

    return getMapSet(mapset);

  }

  /**
   * Removes the specified {@link MapSet} (<B>SERVER CALL).
   * This uses {@link MolecularAction}s in
   * the background to do the work.
   * @param mapset the {@link MapSet} to delete
   * @throws MEMEException if anything goes wrong
   */
  public void deleteMapSet(MapSet mapset) throws MEMEException {
    MolecularAction ma = new MolecularDeleteConceptAction(mapset);
    processAction(ma);
  }

  /**
   * Sets the {@link MapSet} name (<B>SERVER CALL).
   * This uses {@link MolecularAction}s
   * in the background to do the work.
   * @param ms the {@link MapSet}
   * @param mapset_name the new mapset name
   * @throws MEMEException if anything goes wrong
   */
  public void setMapSetName(MapSet ms, String mapset_name) throws MEMEException {
    setAttribute(ms, mapset_name, "MAPSETNAME", false);
  }

  /**
   * Sets the {@link MapSet} {@link Identifier} (<B>SERVER CALL).  This uses {@link MolecularAction}s
   * in the background to do the work.
   * @param ms the {@link MapSet}
   * @param mapset_id the new {@link Identifier}
   * @throws MEMEException if anything goes wrong
   */
  public void setMapSetIdentifier(MapSet ms, Identifier mapset_id) throws
      MEMEException {
    setAttribute(ms, mapset_id.toString(), "MAPSETSID", false);
  }

  /**
   * Sets the mapped from {@link Source} (<B>SERVER CALL).  This uses {@link MolecularAction}s
   * in the background to do the work.
   * @param ms the {@link MapSet}
   * @param source the new from {@link Source}
   * @throws MEMEException if anything goes wrong
   */
  public void setFromSource(MapSet ms, Source source) throws MEMEException {
    ms.setFromSource(source);
    setAttribute(ms, source.getSourceAbbreviation(), "FROMVSAB", true);
    setAttribute(ms, source.getStrippedSourceAbbreviation(), "FROMRSAB", true);
  }

  /**
   * Sets the mapped to {@link Source} (<B>SERVER CALL).  This uses {@link MolecularAction}s
   * in the background to do the work.
   * @param ms the {@link MapSet}
   * @param source the new to {@link Source}
   * @throws MEMEException if anything goes wrong
   */
  public void setToSource(MapSet ms, Source source) throws MEMEException {
    ms.setToSource(source);
    setAttribute(ms, source.getSourceAbbreviation(), "TOVSAB", true);
    setAttribute(ms, source.getStrippedSourceAbbreviation(), "TORSAB", true);
  }

  /**
   * Sets the {@link MapSet} {@link Source} (<B>SERVER CALL).  This uses {@link MolecularAction}s
   * in the background to do the work.
   * @param ms the {@link MapSet}
   * @param source the new {@link MapSet} {@link Source}
   * @throws MEMEException if anything goes wrong
   */
  public void setMapSetSource(MapSet ms, Source source) throws MEMEException {
    ms.setMapSetSource(source);
    setAttribute(ms, source.getSourceAbbreviation(), "MAPSETVSAB", true);
    setAttribute(ms, source.getStrippedSourceAbbreviation(), "MAPSETRSAB", true);
  }

  /**
   * Sets the {@link MapSet} description (SOS) (<B>SERVER CALL).  This uses {@link MolecularAction}s
   * in the background to do the work.
   * @param ms the {@link MapSet}
   * @param desc the new scope note, or description
   * @throws MEMEException if anything goes wrong
   */
  public void setDescription(MapSet ms, String desc) throws MEMEException {
    setAttribute(ms, desc, "SOS", false);
  }

  /**
   * Adds a {@link Mapping} to the {@link MapSet} (<B>SERVER CALL).
   * @param ms the {@link MapSet}
   * @param mapping the {@link Mapping} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addMapping(MapSet ms, Mapping mapping) throws MEMEException {

    //
    // Insert Mapping
    //
    MolecularInsertAttributeAction miaa =
        new MolecularInsertAttributeAction(mapping);
    processAction(miaa);
    ms.addAttribute(mapping);

    //
    // Insert mapped to if not already in map set
    //
    MapObject map_to = mapping.getTo();
    if (!ms.containsMapTo(map_to)) {
      // insert MapObject
      miaa = new MolecularInsertAttributeAction(map_to);
      processAction(miaa);
      ms.addAttribute(map_to);
    }

    //
    // Insert mapped from if not already in map set
    //
    MapObject map_from = mapping.getFrom();
    if (!ms.containsMapFrom(map_from)) {
      // insert MapObject
      miaa = new MolecularInsertAttributeAction(map_from);
      processAction(miaa);
      ms.addAttribute(map_from);
    }

  }

  /**
   * Removes the specified {@link Mapping} from the {@link MapSet} (<B>SERVER CALL). This uses
   * {@link MolecularAction}s in the background to do the work.
   * @param ms the {@link MapSet}
   * @param mapping the {@link Mapping} to remove
   * @throws MEMEException if anything goes wrong
   */
  public void removeMapping(MapSet ms, Mapping mapping) throws MEMEException {
    MolecularDeleteAttributeAction mdaa =
        new MolecularDeleteAttributeAction(mapping);
    processAction(mdaa);
    ms.removeAttribute(mapping);
  }

  /**
   * Helper method for the various <code>set</code> methods.
   * @param ms the {@link MapSet}
   * @param value the new attribute value
   * @param attr_name the attribute name
   * @throws MEMEException if anything goes wrong
   */
  public void setAttribute(MapSet ms, String value, String attr_name) throws MEMEException {
    setAttribute(ms,value,attr_name,false);
  }


  //
  // Private Methods
  //

  /**
   * Helper method for the various <code>set</code> methods.
   * @param ms the {@link MapSet}
   * @param value the new attribute value
   * @param attr_name the attribute name
   * @param use_mth indicates whether or not to use MTH as the source
   *   (otherwise use map set source)
   * @throws MEMEException if anything goes wrong
   */
  private void setAttribute(MapSet ms, String value, String attr_name,
                            boolean use_mth) throws MEMEException {

    //
    // Remove existing attribute
    //
    Attribute[] atts = ms.getAttributesByName(attr_name);
    Attribute att = null;
    if (atts.length > 0) {
    	for(int i=0; i < atts.length; i++) {
    		if(atts[i].getValue() == null || !atts[i].getValue().equals(value)) {
		      MolecularDeleteAttributeAction mdaa =
		          new MolecularDeleteAttributeAction(atts[0]);
		      mdaa.setChangeStatus(false);
		      processAction(mdaa);
		      ms.removeAttribute(atts[i]);
		      att = atts[i];
	    	}
    	}
    }
    
    if(value == null || value.isEmpty()) {
    	return;
    }

    if(atts.length > 0 && att == null) {
    	return;
    }
    //
    // Insert new attribute
    //
    if (att == null) {

      //
      // Create attribute
      //
      att = new Attribute.Default();
      att.setConcept(ms);
      att.setName(attr_name);
      if (use_mth) {
        att.setSource(new Source.Default("MTH"));
      } else {
        att.setSource(ms.getMapSetSource());
      }
      att.setStatus('R');
      att.setTobereleased('Y');
      att.setReleased('N');
      att.setLevel('S');
      att.setAtom(ms.getPreferredAtom());
    }
    att.setValue(value);
    MolecularInsertAttributeAction miaa =
        new MolecularInsertAttributeAction(att);
    miaa.setChangeStatus(false);
    processAction(miaa);
    ms.addAttribute(att);
  }

  /**
   * Returns the {@link MEMEServiceRequest}.
   * @return the {@link MEMEServiceRequest}
   */
  protected MEMEServiceRequest getServiceRequest() {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(getMidService());

    if (getSessionId() == null) {
      request.setNoSession(true);
    } else {
      request.setSessionId(getSessionId());

    }
    return request;
  }

}
