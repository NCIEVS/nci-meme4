/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe;
 * Interface:  XMLRxManager.java
 * 
 * Author:     BAC, DSS 
 *
 * Remarks:    This class is used to provide a XML serialization scheme for 
 *	       saving and retrieving a recipe.  
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import gov.nih.nlm.util.ObjectXMLSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;


/**
 *
 * @author: Deborah Shapiro, Brian Carlsen (2/2001)
 * @version: 1.0
 *
 **/

public class XMLRxManager implements PersistenceManager {
  /**
   * This method reads an XML representation of the recipe.
   * @param file File 
   * @return Object 
   */
  public Object read (File file) throws Exception {
    RxToolkit.trace("XMLRxManager::read(" + file.toString() + ")");
    ObjectXMLSerializer in = new ObjectXMLSerializer();
    return in.fromXML(file.getPath());
  };


  /**
   * This method writes a recipe out in XML representation. 
   * @param o Object 
   * @param f File 
   */
  public void write(Object o, File f) throws Exception {
    RxToolkit.trace("XMLRxManager::write(" + f.toString() + ")");
    ObjectXMLSerializer ser = new ObjectXMLSerializer();
    String document = ser.toXML(o);
    PrintWriter out = new PrintWriter(new FileWriter(f));
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    out.println(document);
    out.close();
  };
   

}
