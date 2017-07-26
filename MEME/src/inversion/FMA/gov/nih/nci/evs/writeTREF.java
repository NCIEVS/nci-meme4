package gov.nih.nci.evs;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.hp.hpl.jena.vocabulary.OWL;


/*
 * writes TREF from an OWL model
 */
public class writeTREF {

	// inferred form of the base model we're given
	// private OntModel infModel = null;
	
	// *** Note: this program is intended to accommodate all the OWL structures
	// *** that occur in NCI Thesaurus, NPO, and RADLEX. It is currently under
	// *** modification to accommodate the additional structures that occur in
	// *** FMA4_0, but these modifications are currently incomplete, so the
	// *** program in its current state is likely not to work!

  // map output file names to file objects
  private HashMap<String,PrintStream> files =
      new HashMap<String,PrintStream>();

  // map output file names to their schema (list of field names)
  private HashMap<String,ArrayList<String>> fieldList =
      new HashMap<String,ArrayList<String>>();

  // map OWL property names to lists of output lines to print for them
  private HashMap<String,ArrayList<String>> outputLines =
      new HashMap<String,ArrayList<String>>();

  // map output line items to info about their constituent fields, etc.
  private HashMap<String,HashMap<String,String>> lineInfo =
      new HashMap<String,HashMap<String,String>>();
  
  private HashSet<OntClass> seen = new HashSet<OntClass>();
  
  private boolean bycode=false;
  private HashMap<String,String> propNames =
  	  new HashMap<String,String>();
  
  private int RUI=0;
  private int relGroupID=0;
  private String rela = "";
  private String qualRela = "";

  /*
   * Dump the given model in TREF format
   */
  public void writeModel(OntModel m, Properties config) {
//  	infModel =
//  		ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, m);

  	analyzeConfig(config);
    printLines(outputLines.get("*onetime"), null, null, null, null, null);
    
    if (bycode) {
    	Iterator<OntProperty> i = m.listAllOntProperties();
    	while (i.hasNext()) {
    		OntProperty p = (OntProperty) i.next();
    		String code = p.getLocalName();
    		String name = p.getLabel(null);
    		propNames.put(code, name);
    	}
    }

  	Iterator<OntClass> i = m.listNamedClasses();

    while (i.hasNext()) {
    	OntClass c = (OntClass) i.next();
    	if (!c.hasRDFType(OWL.DeprecatedClass)) {
    		writeClass(c);
    	}
    }
  }

  /*
   * Dump a class, then recurse down to the sub-classes.
   * Use occurs check to prevent getting stuck in a loop
   */
  protected void writeTree(OntClass cls) {
  	// Note: this method is no longer used
    if (cls.canAs(OntClass.class) && !seen.contains(cls)) {
      seen.add(cls);
      // exclude DeprecatedClasses
      if (!cls.hasRDFType(OWL.DeprecatedClass)) {
        writeClass(cls);
      }
      // recurse to the next level down
      for (Iterator<OntClass> i = cls.listSubClasses(true); i.hasNext(); ) {
        writeTree((OntClass) i.next());
      }
    }
  }


  /*
   * Analyzes the given configuration and creates the structures we
   * need when scanning the OWL model.
   */
  public void analyzeConfig(Properties config) {
    // scan the properties in config
    for (Iterator<Object> i = config.keySet().iterator(); i.hasNext(); ) {
      String prop = (String)i.next();
      if (prop.equals("inputURI")) continue;
      if (prop.equals("mode")) {
      	if (((String)config.getProperty(prop)).equals("bycode")) {
      		bycode = true;
      	}
    		continue;
      }
      
      String val = (String)config.get(prop);
      StringTokenizer keyParts = new StringTokenizer(prop, ".");
      String outFile = keyParts.nextToken();
      String keyItem = keyParts.nextToken();
      
      // handle <file>.name entries
      if (keyItem.equals("name")) {
        try {
          files.put(outFile, new PrintStream(new FileOutputStream(val)));
        } catch (Exception e) {
          e.printStackTrace();
          System.exit(1);
        }

      // handle <file>.fields entries
      } else if (keyItem.equals("fields")) {
        StringTokenizer valParts = new StringTokenizer(val, "|");
        ArrayList<String> theseFields = new ArrayList<String>();
        while (valParts.hasMoreTokens()) {
          theseFields.add(valParts.nextToken());
        }
        fieldList.put(outFile, theseFields);

      // handle <file>.<line#> entries
      } else {
        String line = outFile+"."+keyItem;
        String keyField = keyParts.nextToken();
        HashMap<String,String> info = lineInfo.get(line);
        if (info == null) {
          info = new HashMap<String,String>();
          lineInfo.put(line, info);
        }
        
        // handle <file>.<item>.key entries
        if (keyField.equals("key")) {
          StringTokenizer keys = new StringTokenizer(val, ",");
          while (keys.hasMoreTokens()) {
            String thisKey = keys.nextToken();
            if (!outputLines.containsKey(thisKey)) {
              outputLines.put(thisKey, new ArrayList<String>());
            }
            ArrayList<String> lines = outputLines.get(thisKey);
            lines.add(line);
          }

          info.put("file", outFile);
          
        } else {
          // handle <file>.<item>.<field> entries
          info.put(keyField, val);
        }
      }
    }
  }

  /*
   * Print a description of the given class to the given output stream.
   * @param c The class to print
   */
  public void writeClass(OntClass c) {
  	// now b = this class in the base model
  	// get c = this class in the inferred model
//  	OntClass c = infModel.getOntClass(b.getURI());
  	
    if (c.isRestriction()) {
      System.err.println("Error: owl file contains restrictions");
      return;
    }
    if (c.isAnon()) {
      System.err.println("Error: owl file contains anonymous classes");
      return;
    }

    // don't invert the "Thing" concept
    String localName=c.getLocalName();
    if (localName != null && localName.equals("Thing")) return;

    // print *localname lines, if any
    printLines(outputLines.get("*localname"), c, null, null, null, null);

    // go through the properties of this class
    for (StmtIterator i = c.listProperties(); i.hasNext(); ) {
      Statement s = (Statement) i.next();
      String prop = s.getPredicate().getLocalName();
      if (bycode && propNames.containsKey(prop)) {
      	prop = propNames.get(prop);
      }
      
      // print the output lines for this property
      printLines(outputLines.get(prop), c,
    		    (OntProperty)s.getPredicate().as(OntProperty.class),
    		    s.getObject(), null, null);
    }

    // go through the comments of this class
    OntProperty commentProp = c.getOntModel().createOntProperty("#comment");
    for (Iterator<RDFNode> i = c.listComments(null); i.hasNext(); ) {
      Literal l = (Literal) i.next();
      // print the output lines for this property
      printLines(outputLines.get("*comments"), c, commentProp, l, null, null);
    }

    // go through the subClassOf and equivalentClass items for this class
    for (Iterator<OntClass> i = new SingletonIterator<OntClass>(c)
                          .andThen(c.listEquivalentClasses()); i.hasNext(); ) {
      OntClass iItem = (OntClass) i.next();

      // for the current class's item (c), go through the superclasses; for
      // equivalentClass items, go through their intersectionOf components
      for (Iterator j = (iItem == c) ? iItem.listSuperClasses()
//    		  								.filterDrop(new Filter<OntClass>() {
//    		        public boolean accept(OntClass o) {
//    		                return (((OntClass)((RDFNode)o)
//    		                                           .as(OntClass.class))
//    		                                .isRestriction());
//    		        }} )
                        : iItem.isIntersectionClass()
                        ? iItem.asIntersectionClass().getOperands().iterator()
                        : new SingletonIterator<OntClass>(iItem);
           j.hasNext(); ) {
        OntClass jItem = (OntClass) ((RDFNode)j.next()).as(OntClass.class);

        // if this is a unionOf item, iterate through its members; else just
        // do the current item
        for (Iterator k = jItem.isUnionClass()
                          ? jItem.asUnionClass().getOperands().iterator()
                          : new SingletonIterator<OntClass>(jItem); k.hasNext(); ) {
          OntClass kItem = (OntClass) ((RDFNode) k.next()).as(OntClass.class);

          // if this is an intersectionOf item, tentatively plan to iterate
          // through its members; else just do the current item
          Iterator l = kItem.isIntersectionClass()
                       ? kItem.asIntersectionClass().getOperands().iterator()
                       : new SingletonIterator<OntClass>(kItem);

          // However, if this item is an intersectionOf Restrictions or
          // Classes (rather than of an inner intersection or union), push
          // it into a Singleton to iterate below at a lower level
          if (kItem.isIntersectionClass()) {
          	OntClass inner = (OntClass) kItem.asIntersectionClass()
          	                                 .getOperands().getHead()
          	                                 .as(OntClass.class);
          	if (!(inner.isIntersectionClass() || inner.isUnionClass())) {
          		l = new SingletonIterator<OntClass>(kItem);
          	}
          }

          while (l.hasNext()) {
            OntClass lItem = (OntClass) ((RDFNode) l.next()).as(OntClass.class);
            
            // if this is a unionOf item, iterate through its members; else just
            // do the current item
            for (Iterator m = lItem.isUnionClass()
                              ? lItem.asUnionClass().getOperands().iterator()
                              : new SingletonIterator<OntClass>(lItem); m.hasNext(); ) {
              OntClass mItem = (OntClass) ((RDFNode) m.next())
                                          .as(OntClass.class);

              // an intersectionOf item here is a rel group; assign it an ID
              if (mItem.isIntersectionClass()) {
                ++relGroupID;
              }

              // if this is an intersectionOf item, iterate through its members,
              // which are a rel-group; else just do the current item
              for (Iterator n = mItem.isIntersectionClass()
                                ? mItem.asIntersectionClass()
                                       .getOperands().iterator()
                                : new SingletonIterator<OntClass>(mItem); n.hasNext(); ) {
                OntClass nItem = (OntClass) ((RDFNode) n.next())
                                            .as(OntClass.class);

                // this is now either a Restriction item, which is a
                // relationship or a DatatypeProperty, or a Class item, which is a parent or an
                // ancestor; next, find the output lines for this item based on
                // its type, and find the "rel" (related item): the related or
                // parent class (but ignore ancestors)

                ArrayList<String> lines = null;
                Resource rel = null;

                boolean isUnionRestriction=false;
                if (nItem.isRestriction() && nItem.asRestriction().isHasValueRestriction()) {
                  Restriction r = nItem.asRestriction();
                  OntProperty ontProp = r.getOnProperty();
                  String prop = ontProp.getLocalName();
                  RDFNode val = r.asHasValueRestriction().getHasValue();
                  printLines(outputLines.get(prop), c, ontProp, val, null, null);
                } else if (nItem.isRestriction()) {
                  ++RUI;  // assign this relationship a RUI
                  lines = outputLines.get("*restrictions");

                  // for restriction rels, find their rel and rela values
                  Restriction r = nItem.asRestriction();
                  rela = r.getOnProperty().getLocalName();
                  if (bycode) rela = propNames.get(rela);
                  rela = rela.replaceAll("[|\n]", "");
                  if (r.isAllValuesFromRestriction()) {
                    rel = r.asAllValuesFromRestriction().getAllValuesFrom();
                  } else if (r.isSomeValuesFromRestriction()) {
                    rel = r.asSomeValuesFromRestriction().getSomeValuesFrom();
                  } else {
                    System.err.println("found non-values-from Restriction");
                    continue;
                  }
                  
                  // if this is an anonymous restriction, parse it
                  if (rel.isAnon()) {
                	  Resource qualRel = null;
                	  String qualAttr = "";

                	  OntClass restr = rel.as(OntClass.class);
                	  if (restr.isIntersectionClass()) {
                          for (Iterator<RDFNode> o = restr.asIntersectionClass().getOperands().iterator();
                               o.hasNext(); ) {
                        	  OntClass oItem = (OntClass) ((RDFNode) o.next())
                              .as(OntClass.class);
                        	  
                        	  if (oItem.isRestriction()) {
                                  Restriction r2 = oItem.asRestriction();
                                  if (r2.isAllValuesFromRestriction()) {
                                    qualRel = r2.asAllValuesFromRestriction().getAllValuesFrom();
                                  } else if (r2.isSomeValuesFromRestriction()) {
                                    qualRel = r2.asSomeValuesFromRestriction().getSomeValuesFrom();
                                  } else if (r2.isHasValueRestriction()) {
// handle attributes of "implicit target" restrictions (such as "related_part")
                                    qualRela = r2.getOnProperty().getLocalName();
                                    if (bycode) qualRela = propNames.get(qualRela);
                                    qualRela = qualRela.replaceAll("[|\n]", "");
                                	qualAttr = r2.asHasValueRestriction().getHasValue().asLiteral().getString();
                                  } else {
                                    System.err.println("found non-values-from qualifying Restriction");
                                    continue;
                                  }
//                                  ArrayList<String> lines2 = null;
//                                  lines2 = outputLines.get("*restrictionquals");

//                                  printLines(lines2, c, null, null, qualRel, qualAttr);
                        	  } else {
                        		  rel = oItem;
                        	  }
                          }
                          ArrayList<String> lines2 = null;
                          lines2 = outputLines.get("*restrictionquals");

                          printLines(lines, c, null, qualRel, null, qualAttr);
                          printLines(lines2, c, null, null, qualRel, qualAttr);
                	  } else if (restr.isUnionClass()) {
                          for (Iterator<RDFNode> o = restr.asUnionClass().getOperands().iterator();
                               o.hasNext(); ) {
                        	  OntClass oItem = (OntClass) ((RDFNode) o.next())
                        	  .as(OntClass.class);
                        	  
                        	  isUnionRestriction=true;
                              printLines(lines, c, null, null, oItem, null);
                              ++RUI;  // assign next relationship a RUI
                          }
                          --RUI;  // no next relationship, so reclaim the last RUI
          		  
                	  }
                  }
                } else {
                  // if this is an ancestor rather than a direct parent, skip it
                  if (mItem.isIntersectionClass() && mItem.equals(jItem)) {
                    continue;
                  }
                  // don't convert equivalentClass->unionOf->Description items
                  if (iItem != c && jItem == iItem && jItem.isUnionClass() && nItem == kItem) {
                	continue;
                  }
                  // don't convert equivalentClass->intersectionOf->unionOf->Description items
                  if (iItem != c && jItem != iItem && jItem.isUnionClass() && nItem == kItem) {
                	continue;
                  }

                  lines = outputLines.get("*superclasses");
                  rel = nItem;

                  // if this parent is "Thing", ignore it
                  String name=rel.getLocalName();
                  if (name != null && name.equals("Thing")) continue;
                }

                // print output lines for this relationship
                if (!isUnionRestriction) {
                    printLines(lines, c, null, null, rel, null);
                }

                // if this is in a rel group, print rel-group lines
                if (mItem.isIntersectionClass()) {
                  printLines(outputLines.get("*relgroups"), c, null, null, null, null);
                }
              }
            }
          }
        }
      }
    }
  }

  private void printLines(ArrayList<String> lines, OntClass c, OntProperty p,
                          RDFNode o, Resource rel, String value) {
    if (lines == null) return;
    
    for (Iterator<String> i = lines.iterator(); i.hasNext(); ) {
      String line = i.next();
      HashMap<String,String> info = lineInfo.get(line);
      String file = info.get("file");
      PrintStream out = files.get(file);
      ArrayList<String> fields = fieldList.get(file);
      
      // compute the field values for this line from this class's properties
      HashMap<String,String> fieldVals = getFields(c, p, o, info);
      if (!fieldVals.get("*success*").equals("true")) continue;
      
      // print each field of this output line
      for (Iterator<String> j = fields.iterator(); j.hasNext(); ) {
        String nextField = j.next();
        StringTokenizer fieldParts = new StringTokenizer(nextField, "&");
        while (fieldParts.hasMoreTokens()) {
          String nextPart = fieldParts.nextToken();
          String separator = "";
          if (nextPart.startsWith("_")) {
            separator = "_";
            nextPart = nextPart.substring(1);
          }
          String fieldVal = fieldVals.get(nextPart);
          if (fieldVal==null) fieldVal="";
          
          // fill in special '*' references
          if (fieldVal.equals("**rp**")) {
              fieldVal = rela;
          } else if (fieldVal.equals("**rr**")) {
              fieldVal = (rela.equals("has_part")) ? "RB" :
            	         (rela.equals("part_of")) ? "RN" : "RO";
          } else if (fieldVal.equals("**qra**")) {
//            fieldVal = "UI1_" + qualRela;
            fieldVal = qualRela;
          } else if (fieldVal.equals("**RUI**")) {
            fieldVal = new Integer(RUI).toString();
          } else if (fieldVal.equals("**rg**")) {
            fieldVal = new Integer(relGroupID).toString();
          } else if (fieldVal.equals("**qr**")) {
        	fieldVal = value;
        	if (fieldVal == null) {
              Statement stmp = rel.getProperty(rel.getModel()
                                  .createProperty(rel.getNameSpace(),
                     				              "preferred_Name"));
              if (stmp == null) {
                boolean found=false;
                for (StmtIterator k = rel.listProperties(); k.hasNext(); ) {
                  stmp = (Statement) k.next();
              	  if (stmp.getPredicate().getLocalName().equals("preferred_Name")) {
               	    found=true;
               	    break;
               	  }          	      
                }
                if (!found) {
                  System.exit(1);
                }               	
              }
              fieldVal = stmp.getString().replaceAll("[|\n]", "") + "~";
              stmp = rel.getProperty(rel.getModel()
                                        .createProperty(rel.getNameSpace(),
                                        		        "code"));
              if (stmp == null) {
                boolean found=false;
                for (StmtIterator k = rel.listProperties(); k.hasNext(); ) {
                  stmp = (Statement) k.next();
              	  if (stmp.getPredicate().getLocalName().equals("code")) {
              	    found=true;
              	    break;
              	  }          	      
                }
                if (!found) {
                  System.exit(1);
                }               	
              }
            
          	  fieldVal = fieldVal + stmp.getString().replaceAll("[|\n]", "");
        	}
          } else if (fieldVal.length()>0 && fieldVal.charAt(0) == '@') {
            // fill in '@' (superclass-field) references
          	if (fieldVal.substring(1).equals("code")) {
          		// in ByCode (and ByName too I think) data, the code is the local name
          		fieldVal = rel.getLocalName();
          	} else {
          		String propName = fieldVal.substring(1);
                Statement stmp = rel.getProperty(rel.getModel()
                                                 	.createProperty(rel.getNameSpace(),
                                                 					propName));
                if (stmp == null) {
              		boolean found=false;
              	    for (StmtIterator k = rel.listProperties(); k.hasNext(); ) {
              	      stmp = (Statement) k.next();
              	      if (stmp.getPredicate().getLocalName().equals(propName)) {
              	    	found=true;
              	    	break;
              	      }          	      
              		}
              	    if (!found) {
              	    	System.exit(1);
              	    }               	
                }
            	fieldVal = stmp.getString().replaceAll("[|\n]", "");

      //    		fieldVal = rel.getProperty(rel.getModel()
      //                                 	 .createProperty(rel.getNameSpace(),
      //                                                 	 fieldVal.substring(1)))
      //                      .getString().replaceAll("[|\n]", "");
          	}
          }
          
          if (!fieldVal.equals("")) {
        	fieldVal = separator + fieldVal;
          }

          // translate &gt; to > to handle new parseType="Literal" format
          out.print(fieldVal.replaceAll("&gt;", ">"));
        }
        out.print("|");
      }
      out.println();
    }
  }

  /*
   * Make a HashMap mapping field names to field values for a given output line.
   * Adds a special key, "*success*", to the HashMap that is "true" unless
   * one of the fields is a regexp field that didn't match.
   * @param c The class to get field values from
   * @param p The "key" property we're printing a line for
   *          (null if the key is *superclasses or *restrictions)
   * @param o The object/value of the property p
   * @param info A HashMap specifying the desired contents of each field
   */
  public HashMap<String,String> getFields(OntClass c, OntProperty p, RDFNode o,
                                          HashMap<String,String> info) {
    // create the HashMap (field name->value) that this will eventually return
    HashMap<String,String> fieldVals = new HashMap<String,String>();
    fieldVals.put("*success*", "true");
    
    String prop = null;
    String val = null;

    if (p != null) {
      prop = p.getLocalName();
      if (!(p.isObjectProperty())) {
    	Literal l = o.asLiteral();
      	val = l.getString().replace('\u00A0', ' ')
                           .replace("\u00B0", "&#176;").replace("\u00B5", "&#181;")
                           .replace("\u00BA", "&#186;").replace("\u00C4", "&#196;")
                           .replace("\u00D6", "&#214;").replace("\u00D7", "&#215;")
                           .replace("\u00DC", "&#220;").replace("\u00DF", "&#223;")
                           .replace("\u00E0", "&#224;").replace("\u00E1", "&#225;")
                           .replace("\u00E4", "&#228;").replace("\u00E7", "&#231;")
                           .replace("\u00E9", "&#233;").replace("\u00F6", "&#246;")
                           .replace("\u00F8", "&#248;").replace("\u00F9", "&#249;")
                           .replace("\u00FC", "&#252;").replace("\u00FF", "&#255;")
                           .replace("\u03B1", "&#945;").replace("\u03B2", "&#946;")
                           .replace("\u03B6", "&#950;").replace("\u03C9", "&#969;")
                           .replace('\u2012', '-').replace('\u2013', '-')
                           .replace("\u2014", " - ").replace('\u2019', '\'')
                           .replace('\u201C', '"').replace('\u201D', '"')
                           .replace('\u201E', '"').replace('\u2212', '-')
                           .replace("\u2260", "&#8800;").replace("\u2264", "&#8804;")
                           .replace("\u2265",  "&#8805;");
      }
    }

    // compute the field values for this output line from the class's properties
    for (Iterator<String> i=info.keySet().iterator(); i.hasNext(); ) {
      String fieldDest = i.next();
      if (fieldDest.equals("file")) {
        continue;
      }	
      String fieldSpec = info.get(fieldDest);
      StringTokenizer specParts = new StringTokenizer(fieldSpec, "|");
      String specType = specParts.nextToken();
        
      // "const" field: next token is value
      if (specType.equals("const")) {
      	// use fieldSpec.substring(6) instead of specParts.nextToken() so that
      	// const values can include vertical bars (to specify a whole line)
        String fieldVal = fieldSpec.substring(6);
        if (fieldVal.equals("*key")) {
        	fieldVal = (bycode) ? propNames.get(prop) : prop;
        }
        fieldVals.put(fieldDest, fieldVal.replaceAll("[\n]", ""));
        
      // "superclassname" field: value is the name of the superclass
      } else if (specType.equals("superclassname")) {
        fieldVals.put(fieldDest, c.getSuperClass().getLocalName());

      // "superclassfield" field: next token is field name from superclass;
      // use "@fieldname" to represent the value of fieldname in a superclass
      } else if (specType.equals("superclassfield")) {
        String field = specParts.nextToken();
        fieldVals.put(fieldDest, "@"+field);

      // "restrictionproperty" field: value is the name of the restriction's
      // "onProperty" - represented by "**rp**" in fieldVals
      } else if (specType.equals("restrictionproperty")) {
        fieldVals.put(fieldDest, "**rp**");

      // "restrictionrel" field: value is the proper REL corresponding to the
      // value of the restriction's onProperty, "RO" for most values, but "RN"
      // for "has_part" - represented by "**rr**" in fieldVals
      } else if (specType.equals("restrictionrel")) {
        fieldVals.put(fieldDest, "**rr**");

      // "generatedRUI" field: value is a RUI value we generate
      } else if (specType.equals("generatedRUI")) {
        fieldVals.put(fieldDest, "**RUI**");

      // "generatedRelGroupID" field: value is a rel group ID value we generate
      } else if (specType.equals("generatedRelGroupID")) {
        fieldVals.put(fieldDest, "**rg**");

      // "qualrel" field: value is the name and code of the qualifying restriction's
      // "someValuesFrom" - represented by "**qr**" in fieldVals
      } else if (specType.equals("qualrel")) {
        fieldVals.put(fieldDest, "**qr**");

      // "qualrela" field: value is the name of the qualifying restriction's
      // "onProperty" - represented by "**qra**" in fieldVals
      } else if (specType.equals("qualrela")) {
        fieldVals.put(fieldDest, "**qra**");

      // "comment" field: value is the Literal passed as o
      } else if (specType.equals("comment")) {
        fieldVals.put(fieldDest, val);

      // "localname" field: value is the localName of the class
      } else if (specType.equals("localname")) {
        fieldVals.put(fieldDest, c.getLocalName());
        
      // "targetname" field: next token is the localName of the target class
      } else if (specType.equals("targetname")) {
        if (p == null) {
          System.err.println("can't use targetname for *superclasses or *restrictions");
          System.exit(1);
        }

        if (p.isObjectProperty()) {
          OntClass target = (OntClass)o.as(OntClass.class);
          String fieldVal = target.getLocalName();
          fieldVals.put(fieldDest, fieldVal.replaceAll("[|\n]", ""));
        } else {
          System.err.println("targetname can't be used for non-Object Property "+prop);
        }
          
      // "targetfield" field: next token is field name from the class
      // referred to by the value of an ObjectProperty
      } else if (specType.equals("targetfield")) {
        if (p == null) {
          System.err.println("can't use targetfield for *superclasses or *restrictions");
          System.exit(1);
        }

        String field = specParts.nextToken();
        if (p.isObjectProperty()) {
          OntClass target = (OntClass)o.as(OntClass.class);

          String fieldVal = null;
          if (bycode && field.equals("code")) {
          	// in ByCode data, the code is the local name
          	fieldVal = target.getLocalName();
          } else {
            // find the (presumably only) instance of the specified property
          	fieldVal = target.getProperty(c.getModel()
                                          .createProperty(c.getNameSpace(),
                                                          field))
                             .getString();
          }

          fieldVals.put(fieldDest, fieldVal.replaceAll("[|\n]", ""));
        } else {
          System.err.println("targetfield can't be used for non-Object Property "+prop);
        }
        
      // "field" field: next token is property whose value is the value
      } else if (specType.equals("field")) {
        String valField = specParts.nextToken();
        if (valField.equals("*key")) valField = prop;
        if (valField.equals(prop)) {
          // use *this* instance of this property
          fieldVals.put(fieldDest, val.replaceAll("[|\n]", ""));
        } else {
          // find the (presumably only) instance of the specified property

          // modified the following section to try the namespace
          // "http://purl.bioontology.org/ontology/npo#" to make
          // this work for NPO owl file
          Model mtmp = c.getModel();
          Property ptmp = mtmp.createProperty(c.getNameSpace(),
                                              valField);
          Statement stmp = c.getProperty(ptmp);
          if (stmp == null) {
            ptmp = mtmp.createProperty("http://purl.bioontology.org/ontology/npo#",
                                       valField);
            stmp = c.getProperty(ptmp);
          }
          String fieldVal = stmp.getString();
//          String fieldVal = c.getProperty(c.getModel()
//                                          .createProperty(c.getNameSpace(),
//                                                          valField))
//                             .getString();
          fieldVals.put(fieldDest, fieldVal.replaceAll("[|\n]", ""));
        }

      // "regexp" field: next token is a regexp to fill fieldDest fields
      } else if (specType.equals("regexp")) {
        String regexField = specParts.nextToken();
        if (regexField.equals("*key")) regexField = prop;
        String regexVal = null;
        if (regexField.equals(prop)) {
          regexVal = val;
        } else if (regexField.equals("*localname")) {
          regexVal = c.getLocalName();
        } else if (regexField.equals("*qualrel")) {
          if (o == null) {
            fieldVals.put("*success*", "false");
            return(fieldVals);
          }
          regexVal = o.as(OntClass.class).getLocalName();
        } else {
          // find the (presumably only) instance of the specified property
          regexVal = c.getProperty(c.getModel()
                                   .createProperty(c.getNameSpace(),
                                                   regexField))
                      .getString();
        }
        if (regexVal == null) {
          fieldVals.put("*success*", "false");
          return(fieldVals);
        }
        
        String regex = specParts.nextToken();
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(regexVal);
        
        StringTokenizer destFields = new StringTokenizer(fieldDest, "_");
        if (matcher.lookingAt()) {
          int group=1;
          while (destFields.hasMoreTokens()) {
            String nextDest = destFields.nextToken();
            String nextMatch = matcher.group(group++);
            fieldVals.put(nextDest, nextMatch.replaceAll("[|\n]", "")
            		                             .replaceAll("&#39;", "\'")
            		                             .replaceAll("&amp;", "&")
            		                             .replaceAll("&amp;", "&")
            		                             .replaceAll("&quot;", "\"")
            		                             .replaceAll("&lt;", "<")
            		                             .replaceAll("&apos;", "\'"));
          }
        } else {
          fieldVals.put("*success*", "false");
        }
      }
    }
    return fieldVals;
  }
}
