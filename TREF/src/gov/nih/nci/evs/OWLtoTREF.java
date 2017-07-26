package gov.nih.nci.evs;

// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.FileInputStream;
import java.util.Properties;


/*
 * Main program for OWL to TREF converter
 */
public class OWLtoTREF {
  public static void main(String[] args) {
  	String configFile = "tref.config";
  	
		int arg = 0;
		while (args.length > arg) {
			if (args[arg].equals("-h")) {
				System.err.println("Usage: java OWLtoTREF -c <config-file>");
				arg++;
			} else if (args[arg].equals("-c")) {
				arg++;
				if (args.length >= arg) {
					configFile = args[arg];
					arg++;
				} else {
					System.err.println("Missing filename after -c flag");
					System.exit(1);
				}
			} else {
				System.err.println("Unknown argument: "+args[arg]);
				System.exit(1);
			}
		}
  	
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    m.getDocumentManager().addAltEntry("http://protege.stanford.edu/plugins/owl/protege",
                                       "file:protege.owl");
    m.setStrictMode(false);  // prevent problems with DeprecatedClass

    Properties config = new Properties();
    try {
      config.load(new FileInputStream(configFile));
    } catch (Exception e) {
    	e.printStackTrace();
      System.exit(1);
    }

    String inputURI=config.getProperty("inputURI");
    if (inputURI == null || inputURI.equals("")) {
    	System.err.println("config file must specify inputURI property");
    	System.exit(1);
    }
    m.read(inputURI);
    
    new writeTREF().writeTREF(m, config);
  }
}
