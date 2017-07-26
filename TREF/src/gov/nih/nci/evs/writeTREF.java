package gov.nih.nci.evs;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/*
 * writes TREF from an OWL model
 */
public class writeTREF {

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

  /*
   * Dump the given model in TREF format
   */
  public void writeTREF(OntModel m, Properties config) {
    // iterate over root classes other than anonymous class expressions
    analyzeConfig(config);

    Iterator i = m.listHierarchyRootClasses()
                  .filterDrop(new Filter() {
                               public boolean accept(Object o) {
                                 return ((Resource) o).isAnon();
                               }} );

    while (i.hasNext()) {
      writeTree((OntClass) i.next());
    }
  }

  /*
   * Dump a class, then recurse down to the sub-classes.
   * Use occurs check to prevent getting stuck in a loop
   */
  protected void writeTree(OntClass cls) {
    if (cls.canAs(OntClass.class) && !seen.contains(cls)) {
      seen.add(cls);
      writeClass(cls);
      // recurse to the next level down
      for (Iterator i = cls.listSubClasses(true); i.hasNext(); ) {
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
    for (Iterator i = config.keySet().iterator(); i.hasNext(); ) {
    	String prop = (String)i.next();
    	if (prop.equals("inputURI")) continue;
    	
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
    if (c.isRestriction()) {
      System.err.println("Error: owl file contains restrictions");
      return;
    }
    if (c.isAnon()) {
      System.err.println("Error: owl file contains anonymous classes");
      return;
    }

    // go through the properties of this class
    boolean parentsDone = false;
    for (StmtIterator i = c.listProperties(); i.hasNext(); ) {
    	Statement s = (Statement) i.next();
    	String prop = s.getPredicate().getLocalName();
    	
  		// find properties that have associated output lines
    	ArrayList<String> lines = null;
    	if (prop.equals("subClassOf")) {
    		if (parentsDone) continue;
    		lines = outputLines.get("*superclasses");
    	} else {
    		lines = outputLines.get(prop);
    	}
    	if (lines == null) continue;
    	
    	// print each output line for this property
    	for (Iterator<String> j = lines.iterator(); j.hasNext(); ) {
    		String line = j.next();
    		HashMap<String,String> info = lineInfo.get(line);
    		String file = info.get("file");
    		PrintStream out = files.get(file);
    		ArrayList<String> fields = fieldList.get(file);
    		
    		// compute the field values for this line from this class's properties
    		HashMap<String,String> fieldVals = getFields(c, s, info);
  			
    		if (prop.equals("subClassOf") && !parentsDone) {
    			// output a line for each superclass
    			for (ExtendedIterator k = c.listSuperClasses(true); k.hasNext(); ) {
    				OntClass par = (OntClass) k.next();
						Statement parCodeProp = par.getProperty(par.getModel()
                                                    .createProperty(par.getNameSpace(),
                                                                    "code"));
						String parCode = "";
            if (parCodeProp != null) parCode = parCodeProp.getString();
            if (parCode == "") {
            	// ignore subClassOf Restrictions
            	continue;
            }
            
    				// print each field of this output line
      			for (Iterator<String> l = fields.iterator(); l.hasNext(); ) {
    					String nextField = l.next();
    					StringTokenizer fieldParts = new StringTokenizer(nextField, "_");
    					while (fieldParts.hasMoreTokens()) {
    						String nextPart = fieldParts.nextToken();
    						String fieldVal = fieldVals.get(nextPart);
    						if (fieldVal==null) fieldVal="";
    						
    						// fill in '*' (superclass-field) references
    						if (fieldVal.length()>0 && fieldVal.charAt(0) == '*') {
    							fieldVal = par.getProperty(par.getModel()
                                             .createProperty(par.getNameSpace(),
                                                             fieldVal.substring(1)))
                                .getString();

    						}
    						out.print(fieldVal);
    					}
    					out.print("|");
    				}
    				out.println();
    			}
    			
    		// print each field of this output line
    		} else {
    			for (Iterator<String> k = fields.iterator(); k.hasNext(); ) {
  					String nextField = k.next();
  					StringTokenizer fieldParts = new StringTokenizer(nextField, "_");
  					while (fieldParts.hasMoreTokens()) {
  						String nextPart = fieldParts.nextToken();
  						String fieldVal = fieldVals.get(nextPart);
  						if (fieldVal==null) fieldVal="";
  						out.print(fieldVal);
  					}
  				out.print("|");
  				}
  				out.println();
    		}
    	}
    	if (prop.equals("subClassOf")) parentsDone = true;
    }
    
    /******
    indent(out, depth);
    out.print("Class ");
    printURI(out, c.getModel(), c.getURI());
    out.println();

    for (StmtIterator i = c.listProperties(); i.hasNext(); ) {
      Statement s = (Statement) i.next();

      Property p = s.getPredicate();
      RDFNode v = s.getObject();

      indent(out, depth);
      out.println("...property " + p.getLocalName() + "=" + v);
    }
    *******/
  }

  /*
   * Make a HashMap mapping field names to field values for a given output line.
   * @param c The class to get field values from
   * @param s The "key" Statement property we're printing a line for
   * @param info A HashMap specifying the desired contents of each field
   */
  public HashMap<String,String> getFields(OntClass c, Statement s,
                                          HashMap<String,String> info) {
    // create the HashMap (field name->value) that this will eventually return
    HashMap<String,String> fieldVals = new HashMap<String,String>();
    
  	String prop = s.getPredicate().getLocalName();
  	String val = s.getObject().toString();

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
      	String fieldVal = specParts.nextToken();
      	if (fieldVal.equals("*key")) fieldVal = prop;
        fieldVals.put(fieldDest, fieldVal);
        
      // "superclassfield" field: next token is field name from superclass
      // use "*fieldname" to represent the value of fieldname in a superclass
      } else if (specType.equals("superclassfield")) {
      	String field = specParts.nextToken();
      	fieldVals.put(fieldDest, "*"+field);
      	
      // "targetfield" field: next token is field name from the class
      // referred to by the value of an ObjectProperty
      } else if (specType.equals("targetfield")) {
      	String field = specParts.nextToken();
      	if (((OntProperty)s.getPredicate().as(OntProperty.class)).isObjectProperty()) {
      		OntClass target = (OntClass)s.getObject().as(OntClass.class);

      		// find the (presumably only) instance of the specified property
          String fieldVal = target.getProperty(c.getModel()
                                               .createProperty(c.getNameSpace(),
                                                               field))
                                  .getString();

      		fieldVals.put(fieldDest, fieldVal);
      	} else {
      		System.err.println("targetfield can't be used for non-Object Property "+prop);
      	}
      	
      // "field" field: next token is property whose value is the value
      } else if (specType.equals("field")) {
        String valField = specParts.nextToken();
        if (valField.equals("*key")) valField = prop;
        if (valField.equals(prop)) {
          // use *this* instance of this property
          fieldVals.put(fieldDest, val);
        } else {
          // find the (presumably only) instance of the specified property
          String fieldVal = c.getProperty(c.getModel()
                                          .createProperty(c.getNameSpace(),
                                                          valField))
                             .getString();
          fieldVals.put(fieldDest, fieldVal);
        }

      // "regexp" field: next token is a regexp to fill fieldDest fields
      } else if (specType.equals("regexp")) {
        String regexField = specParts.nextToken();
        String regexVal = null;
        if (regexField.equals(prop)) {
          regexVal = val;
        } else {
          // find the (presumably only) instance of the specified property
          regexVal = c.getProperty(c.getModel()
                                   .createProperty(c.getNameSpace(),
                                                   regexField))
                      .getString();
        }
        
        String regex = specParts.nextToken();
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(regexVal);
        
        if (matcher.lookingAt()) {
          StringTokenizer destFields = new StringTokenizer(fieldDest, "_");
          int group=1;
          while (destFields.hasMoreTokens()) {
            String nextDest = destFields.nextToken();
            String nextMatch = matcher.group(group++);
            fieldVals.put(nextDest, nextMatch);
          }
        }
      }
    }
    return fieldVals;
  }
}
