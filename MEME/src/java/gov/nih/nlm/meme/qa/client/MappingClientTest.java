/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.qa.client
 * Object:  MappingClientTest
 *
 * 09/21/2007 BAC (1-F73I3): Additional fixes to allow TestCenter.java to run properly
 * 01/30/2006 RBE (1-763IU): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.qa.client;

import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.MappingClient;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.MapObject;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.common.Mapping;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.qa.TestSuite;
import gov.nih.nlm.meme.qa.TestSuiteUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Test suite for Mapping
 */
public class MappingClientTest extends TestSuite {

  public MappingClientTest() {
    setName("MappingClientTest");
    setDescription("Test Suite for Mapping");
  }

  /**
   * Perform Test Suite Mapping
   */
  public void run() {

    TestSuiteUtils.printHeader(this);

    //
    // Initial Setup
    //
    SimpleDateFormat date_format = TestSuiteUtils.getDateFormat();

    MappingClient client = null;

    try {
      client = new MappingClient("");

      AuxiliaryDataClient aux_data_client = new AuxiliaryDataClient("");
      Identifier work_id = aux_data_client.getNextIdentifierForType(WorkLog.class);
      client.setWorkIdentifier(work_id);
      Identifier transaction_id = aux_data_client.getNextIdentifierForType(MolecularTransaction.class);
      client.setTransactionIdentifier(transaction_id);
      client.setChangeStatus(true);
      client.setAuthority(new Authority.Default("L-MEME4"));

	    //
	    // 1.1. Test getMapSets()
	    //      
      addToLog(
        "    1.1. Test getMapSets() ... "
        + date_format.format(new Date()));

      MapSet[] mapsets = client.getMapSets();
      for (int i=0; i<mapsets.length; i++) {
        addToLog(
          mapsets[i].getMapSetIdentifier() +
          " = " + mapsets[i].getName() +
          " " + mapsets[i].getFromSource() +
          " " + mapsets[i].getToSource() +
          " " + mapsets[i].getMapSetSource() +
          " " + mapsets[i].isFromExhaustive() +
          " " + mapsets[i].isToExhaustive() +
          " " + mapsets[i].getFromComplexity() +
          " " + mapsets[i].getToComplexity() +
          " " + mapsets[i].getMapSetComplexity() +
          " " + mapsets[i].getDescription());
        MapObject[] map_to = mapsets[i].getMapTo();
        for (int j=0; j<map_to.length; j++) {
          addToLog(map_to[j].getIdentifier().toString());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
        MapObject[] map_from = mapsets[i].getMapFrom();
        for (int j=0; j<map_from.length; j++) {
          addToLog(map_from[j].getIdentifier().toString());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
      }

	    //
	    // 1.2. Test getMappingCount(Mapset)
	    //      
      addToLog(
        "    1.2. Test getMappingCount(Mapset) ... "
        + date_format.format(new Date()));

      // Avoid SNOMEDCT_US map sets (very large)
      MapSet mapset = null;
      for (int i = 0; i<mapsets.length;i++) {
      	if (!mapsets[i].getMapSetSource().getRootSourceAbbreviation().equals("SNOMEDCT_US")) {
          mapset = mapsets[i];
      		break;
      	}
      }
      int count = client.getMappingCount(mapset);
      addToLog("Count is: " + count);

	    //
	    // 1.3. Test getMapSet(int)
	    //      
      addToLog(
          "    1.3. Test getMapSet("+mapset.getIdentifier()+") ... "
          + date_format.format(new Date()));

      addToLog(
        mapset.getMapSetIdentifier() +
        " = " + mapset.getName() +
        " " + mapset.getFromSource() +
        " " + mapset.getToSource() +
        " " + mapset.getMapSetSource() +
        " " + mapset.isFromExhaustive() +
        " " + mapset.isToExhaustive() +
        " " + mapset.getFromComplexity() +
        " " + mapset.getToComplexity() +
        " " + mapset.getMapSetComplexity() +
        " " + mapset.getDescription());

      if (mapset != null) {
  	    //
  	    // 1.3.1. Test getMappings(MapSet, int, int)
  	    //      
        Mapping[] mappings = client.getMappings(mapset, 0, 10);
        for (int j = 0; j < mappings.length; j++) {
          addToLog("\tmappings["+j+"] FROM: "+
            mappings[j].getFrom().getMapObjectIdentifier() + " TO: " +
            mappings[j].getTo().getMapObjectIdentifier() + ", " +
            mappings[j].getMappingAttributeName() + ", " +
            mappings[j].getMappingAttributeValue() + ", " +
            mappings[j].getMapRank() + ", " +
            mappings[j].getRelationshipAttribute() + ", " +
            mappings[j].getRelationshipName() + ", " +
            mappings[j].getRule() + ", " +
            mappings[j].getSubsetIdentifier() + ", " +
            mappings[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

        addToLog("\n");

  	    //
  	    // 1.4. Test getMappings(MapSet, int, int)
  	    //      
        mappings = client.getMappings(mapset, 20000, 20100);
        for (int j = 0; j < mappings.length; j++) {
          addToLog("\tmappings["+j+"] FROM: "+
            mappings[j].getFrom().getMapObjectIdentifier() + " TO: " +
            mappings[j].getTo().getMapObjectIdentifier() + ", " +
            mappings[j].getMappingAttributeName() + ", " +
            mappings[j].getMappingAttributeValue() + ", " +
            mappings[j].getMapRank() + ", " +
            mappings[j].getRelationshipAttribute() + ", " +
            mappings[j].getRelationshipName() + ", " +
            mappings[j].getRule() + ", " +
            mappings[j].getSubsetIdentifier() + ", " +
            mappings[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

      }

	    //
	    // 1.5. Test getMapSetWithoutMappings(int)
	    //      
      addToLog(
          "    1.5. Test getMapSetWithoutMappings(int) ... "
          + date_format.format(new Date()));

      mapset = client.getMapSetWithoutMappings(mapset.getIdentifier().intValue());
      addToLog(
        mapset.getMapSetIdentifier() +
        " = " + mapset.getName() +
        " " + mapset.getFromSource() +
        " " + mapset.getToSource() +
        " " + mapset.getMapSetSource() +
        " " + mapset.isFromExhaustive() +
        " " + mapset.isToExhaustive() +
        " " + mapset.getFromComplexity() +
        " " + mapset.getToComplexity() +
        " " + mapset.getMapSetComplexity() +
        " " + mapset.getDescription());

      if (mapset != null) {
        Mapping[] mappings = mapset.getMappings();
        for (int j = 0; j < mappings.length; j++) {
          addToLog("\tmappings["+j+"] FROM: "+
            mappings[j].getFrom().getMapObjectIdentifier() + " TO: " +
            mappings[j].getTo().getMapObjectIdentifier() + ", " +
            mappings[j].getMappingAttributeName() + ", " +
            mappings[j].getMappingAttributeValue() + ", " +
            mappings[j].getMapRank() + ", " +
            mappings[j].getRelationshipAttribute() + ", " +
            mappings[j].getRelationshipName() + ", " +
            mappings[j].getRule() + ", " +
            mappings[j].getSubsetIdentifier() + ", " +
            mappings[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

        addToLog("\n");

  	    //
  	    // 1.6. Test getMappings(MapSet, int, int)
  	    //      
        mappings = client.getMappings(mapset, 20000, 20100);
        for (int j = 0; j < mappings.length; j++) {
          addToLog("\tmappings["+j+"] FROM: "+
            mappings[j].getFrom().getMapObjectIdentifier() + " TO: " +
            mappings[j].getTo().getMapObjectIdentifier() + ", " +
            mappings[j].getMappingAttributeName() + ", " +
            mappings[j].getMappingAttributeValue() + ", " +
            mappings[j].getMapRank() + ", " +
            mappings[j].getRelationshipAttribute() + ", " +
            mappings[j].getRelationshipName() + ", " +
            mappings[j].getRule() + ", " +
            mappings[j].getSubsetIdentifier() + ", " +
            mappings[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }

      }

	    //
	    // 1.7. Test getMapSet(int)
	    //      
      addToLog(
          "    1.7. Test getMapSet(int) ... "
          + date_format.format(new Date()));

      mapset = client.getMapSet(mapset.getIdentifier().intValue());
      addToLog(
        mapset.getMapSetIdentifier() +
        " = " + mapset.getName() +
        " " + mapset.getFromSource() +
        " " + mapset.getToSource() +
        " " + mapset.getMapSetSource() +
        " " + mapset.isFromExhaustive() +
        " " + mapset.isToExhaustive() +
        " " + mapset.getFromComplexity() +
        " " + mapset.getToComplexity() +
        " " + mapset.getMapSetComplexity() +
        " " + mapset.getDescription());

      if (mapset != null && mapset.getMappings() != null) {
        Mapping[] mappings = mapset.getMappings();
        for (int j = 0; j < mappings.length; j++) {
          addToLog("\tmappings["+j+"] = FROM: "+
            mappings[j].getFrom().getMapObjectIdentifier() + " TO: " +
            mappings[j].getTo().getMapObjectIdentifier() + ", " +
            mappings[j].getMappingAttributeName() + ", " +
            mappings[j].getMappingAttributeValue() + ", " +
            mappings[j].getMapRank() + ", " +
            mappings[j].getRelationshipAttribute() + ", " +
            mappings[j].getRelationshipName() + ", " +
            mappings[j].getRule() + ", " +
            mappings[j].getSubsetIdentifier() + ", " +
            mappings[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
        MapObject[] map_to = mapset.getMapTo();
        for (int j = 0; j < map_to.length; j++) {
          addToLog("\tmap_to["+j+"] = TO: "+
            map_to[j].getMapObjectIdentifier() + ", " +
            map_to[j].getMapObjectSourceIdentifier() + ", " +
            map_to[j].getExpression() + ", " +
            map_to[j].getMapObjectSource() + ", " +
            map_to[j].getRestriction() + ", " +
            map_to[j].getRule() + ", " +
            map_to[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
        MapObject[] map_from = mapset.getMapFrom();
        for (int j = 0; j < map_from.length; j++) {
          addToLog("\tmap_from["+j+"] = FROM: "+
            map_from[j].getMapObjectIdentifier() + ", " +
            map_from[j].getMapObjectSourceIdentifier() + ", " +
            map_from[j].getExpression() + ", " +
            map_from[j].getMapObjectSource() + ", " +
            map_from[j].getRestriction() + ", " +
            map_from[j].getRule() + ", " +
            map_from[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
      }

	    //
	    // 1.8. Test getMapSet(MapSet)
  	  //      
      addToLog(
          "    1.8. Test getMapSet(MapSet) ... "
          + date_format.format(new Date()));

      mapset = client.getMapSet(mapset);
      addToLog(
        mapset.getMapSetIdentifier() +
        " = " + mapset.getName() +
        " " + mapset.getFromSource() +
        " " + mapset.getToSource() +
        " " + mapset.getMapSetSource() +
        " " + mapset.isFromExhaustive() +
        " " + mapset.isToExhaustive() +
        " " + mapset.getFromComplexity() +
        " " + mapset.getToComplexity() +
        " " + mapset.getMapSetComplexity() +
        " " + mapset.getDescription());

      if (mapset != null && mapset.getMappings() != null) {
        Mapping[] mappings = mapset.getMappings();
        for (int j = 0; j < mappings.length; j++) {
          addToLog("\tmappings["+j+"] = FROM: "+
            mappings[j].getFrom().getMapObjectIdentifier() + " TO: " +
            mappings[j].getTo().getMapObjectIdentifier() + ", " +
            mappings[j].getMappingAttributeName() + ", " +
            mappings[j].getMappingAttributeValue() + ", " +
            mappings[j].getMapRank() + ", " +
            mappings[j].getRelationshipAttribute() + ", " +
            mappings[j].getRelationshipName() + ", " +
            mappings[j].getRule() + ", " +
            mappings[j].getSubsetIdentifier() + ", " +
            mappings[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
        MapObject[] map_to = mapset.getMapTo();
        for (int j = 0; j < map_to.length; j++) {
          addToLog("\tmap_to["+j+"] = TO: "+
            map_to[j].getMapObjectIdentifier() + ", " +
            map_to[j].getMapObjectSourceIdentifier() + ", " +
            map_to[j].getExpression() + ", " +
            map_to[j].getMapObjectSource() + ", " +
            map_to[j].getRestriction() + ", " +
            map_to[j].getRule() + ", " +
            map_to[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
        MapObject[] map_from = mapset.getMapFrom();
        for (int j = 0; j < map_from.length; j++) {
          addToLog("\tmap_from["+j+"] = FROM: "+
            map_from[j].getMapObjectIdentifier() + ", " +
            map_from[j].getMapObjectSourceIdentifier() + ", " +
            map_from[j].getExpression() + ", " +
            map_from[j].getMapObjectSource() + ", " +
            map_from[j].getRestriction() + ", " +
            map_from[j].getRule() + ", " +
            map_from[j].getType());
          if (j > 5) {
            addToLog("          >>> Loop terminated. Only few records displayed.");
            break;
          }
        }
      }

     Source[] sources = aux_data_client.getSources();
     for (int i=0; i < sources.length; i++) {
       if (sources[i].toString().equals("MAP")) {
           addToLog("sources[" + i + "] = " + sources[i] + ", " +
                              sources[i].getSourceAbbreviation() + ", " +
                              sources[i].getStrippedSourceAbbreviation());
       }
     }

      /*
       * TEST SEQUENCE
       *
       * 1. createMapSet()
       * 2. getMapSet()
       * 3. getMapSets()
       * 4. addMapping()
       * 5-10. setX()/getX()
       * 11. removeMapping()
       * 12. deleteMapSet()
       */

      addToLog("    1. Test createMapset(MapSet) ... "
        + date_format.format(new Date()));

      mapset = client.createMapSet(
        "MSH Associated Expressions",
        new Identifier.Default(999),
        aux_data_client.getSource("MTH"),
        aux_data_client.getSource("MTH"),
        aux_data_client.getSource("MTH")
      );

      addToLog("    2. Test getMapSet(int) ... "
        + date_format.format(new Date()));

      mapset = client.getMapSet(mapset.getIdentifier().intValue());
      if (mapset != null)
        addToLog("    2. Test Passed");
      else {
        addToLog("    2. Test Failed");
        thisTestFailed();
      }
      addToLog(
        mapset.getMapSetIdentifier() +
        " = " + mapset.getName() +
        " " + mapset.getFromSource() +
        " " + mapset.getToSource() +
        " " + mapset.getMapSetSource() +
        " " + mapset.isFromExhaustive() +
        " " + mapset.isToExhaustive() +
        " " + mapset.getFromComplexity() +
        " " + mapset.getToComplexity() +
        " " + mapset.getMapSetComplexity() +
        " " + mapset.getDescription());

      addToLog("    3. Test getMapSets() ... "
        + date_format.format(new Date()));

      mapsets = client.getMapSets();
      boolean found = false;
      for (int i = 0; i < mapsets.length; i++) {
        if (mapsets[i].equals(mapset)) {
          found = true;
          break;
        }
      }
      if (found)
        addToLog("    3. Test Passed");
      else {
        addToLog("    3. Test Failed");
        thisTestFailed();
      }

      addToLog("    4. Test addMapping() ... "
        + date_format.format(new Date()));

      Mapping mapping = new Mapping.Default();

      MapObject mo_from = new MapObject.Default();
      mo_from.setMapObjectIdentifier(new Identifier.Default(999));
      mo_from.setSource(aux_data_client.getSource("MTH"));
      mo_from.setConcept(mapset);
      mo_from.setStatus('R');
      mo_from.setTobereleased('Y');
      mo_from.setReleased('N');
      mo_from.setLevel('C');
      mo_from.setName("MAPFROM");
      mo_from.setValue("XMAP|~~212874005~RN~~9533976055~~2~~");
      mapping.setFrom(mo_from);

      MapObject mo_to = new MapObject.Default();
      mo_to.setMapObjectIdentifier(new Identifier.Default(1011));
      mo_to.setSource(aux_data_client.getSource("MTH"));
      mo_to.setConcept(mapset);
      mo_to.setStatus('R');
      mo_to.setTobereleased('Y');
      mo_to.setReleased('N');
      mo_to.setLevel('C');
      mo_to.setName("MAPTO");
      mo_to.setValue("XMAP|~~212874005~RN~~9533976055~~2~~");

      mapping.setTo(mo_to);
      mapping.setMappingAttributeName("Test mapping attribute name");
      mapping.setMappingAttributeValue("Test mapping attribute value");
      mapping.setMapRank("map rank");
      mapping.setRelationshipAttribute("relationship attribute");
      mapping.setRelationshipName("relationship name");
      mapping.setRule("this rule");
      mapping.setSubsetIdentifier(new Identifier.Default(999));
      mapping.setType("this type");

      mapping.setSource(aux_data_client.getSource("MTH"));
      mapping.setStatus('R');
      mapping.setTobereleased('Y');
      mapping.setReleased('N');
      mapping.setLevel('C');
      mapping.setConcept(mapset);
      mapping.setName("MAPSETNAME");
      mapping.setValue("XMAP|~~212874005~RN~~9533976055~~2~~");

      client.addMapping(mapset, mapping);

      addToLog("    5. Test setDescription(Mapset, String), getDescription() ... "
               + date_format.format(new Date()));

      String desc = "Test setDescription";
      client.setDescription(mapset, desc);
      if (mapset.getDescription().equals(desc))
        addToLog("    5. Test Passed");
      else {
        addToLog("    5. Test Failed");
        thisTestFailed();
      }

      addToLog(
          "    6. Test setMapSetName(Mapset, String), getName() ... "
          + date_format.format(new Date()));

      String name = "Test setMapSetName";
      client.setMapSetName(mapset, name);
      if (mapset.getName().equals(name))
        addToLog("    6. Test Passed");
      else {
        addToLog("    6. Test Failed");
        thisTestFailed();
      }

      addToLog("    7. Test setMapSetIdentifier(Mapset, Identifier), getMapSetIdentifier() ... "
               + date_format.format(new Date()));

      Identifier id = new Identifier.Default(1001);
      client.setMapSetIdentifier(mapset, id);
      if (mapset.getMapSetIdentifier().equals(id))
        addToLog("    7. Test Passed");
      else {
        addToLog("    7. Test Failed");
        thisTestFailed();
      }

      addToLog(
          "    8. Test setFromSource(Mapset, Source), getFromSource() ... "
          + date_format.format(new Date()));
      Source from_source = aux_data_client.getSource("MTH");
      client.setFromSource(mapset, from_source);
      if (mapset.getFromSource().equals(from_source))
        addToLog("    8. Test Passed");
      else {
        addToLog("    8. Test Failed");
        thisTestFailed();
      }

      addToLog(
          "    9. Test setToSource(Mapset, Source), getToSource() ... "
          + date_format.format(new Date()));
      Source to_source = aux_data_client.getSource("MTH");
      client.setToSource(mapset, to_source);
      if (mapset.getToSource().equals(to_source))
        addToLog("    9. Test Passed");
      else {
        addToLog("    9. Test Failed");
        thisTestFailed();
      }

      addToLog("    10. Test setMapSetSource(Mapset, Source), getMapSetSource() ... "
               + date_format.format(new Date()));

      Source source = aux_data_client.getSource("MTH");
      client.setMapSetSource(mapset, source);

      if (mapset.getMapSetSource().equals(source))
        addToLog("    10. Test Passed");
      else {
        addToLog("    10. Test Failed");
        thisTestFailed();
      }

      addToLog(
          "    11. Test removeMapping(MapSet, Mapping) ... "
          + date_format.format(new Date()));

      client.removeMapping(mapset, mapping);

      addToLog("    12. Test deleteMapSet(MapSet) ... "
               + date_format.format(new Date()));

      client.deleteMapSet(mapset);
      
	    //
	    // 1.9. Test setAttribute(MapSet, String, String)
	    //      
    
      //client.setAttribute(MapSet, String, String);
      
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
    addToLog("Finished MappingClientTest at " +
             date_format.format(new Date(System.currentTimeMillis())));
    addToLog("-------------------------------------------------------");

  }

}