/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe
 * Object:     GenericRecipe.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 * Remarks:   This is a Generic recipe
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;


/**
 * This is a Generic subclass of Recipe
 *
 * @author Brian Carlsen
 * @version 1.5
 */
public class GenericRecipe extends Recipe {

  //
  // Constructors
  //

  /**
   * Recipe constructor comment.
   */
  public GenericRecipe() {
    super();
    RxToolkit.trace("GenericRecipe::GenericRecipe()");
  }
  
  //
  // This is the implementation of the Recipe methods
  //

  /** 
   * This method returns the work type
   * @return String
   */
  public String getWorkType () {
    return RxConstants.MAINTENANCE_WORK;
  };

  /**
   * This method returns HTML which is used as a section header
   * when rendering this recipe as an HTML document.
   * @return String
   */
  public String typeToHTML () {
    return "<center><h1>"+typeToString()+"</h1></center>";
  };

  /**
   * This method generates a view
   * @return JPanel
   */
  public javax.swing.JPanel getView () {
    RxToolkit.trace("GenericRecipe::getView()");
    return new Recipe.View();
  }

  /**
   * Inserting sections is not allowed
   * @return String []
   */
  public String [] getPossibleInsertSections (RxSection current) 
  throws SectionNotAllowedException {
    RxToolkit.trace("GenericRecipe::getPossibleInsertSections()");
    return getPossibleNextSections(current);
  };

  /**
   * Get section subclasses
   * @return String []
   */
  public String [] getPossibleNextSections(RxSection current) {
    RxToolkit.trace("GenericRecipe::getPossibleNextSections()");
    try {
      return RxToolkit.getSubclasses(
		   RxConstants.RX_SECTION_PACKAGE_NAME,
		   RxConstants.RECIPE_PACKAGE_NAME + ".RxSection");
    } catch (ClassNotFoundException e) {
      RxToolkit.reportError("Class not found: "+
		   RxConstants.RECIPE_PACKAGE_NAME + ".RxSection");
    }
    return null;
  };

  /**
   * Any # of sections is allowed
   * @return boolean
   */
  public boolean allowsMoreSections() {
    RxToolkit.trace("GenericRecipe::allowsMoreSections()");
    return true;
  };

  /**
   * This static method returns a name describing the 
   * recipe type.  This is used by anything that dynamically 
   * loads the class. To see a string representation of what the 
   * class is.
   * @return String
   */
  public static String typeToString () {
    RxToolkit.trace("GenericRecipe::typeToString()");
    return "Generic Recipe";
  };

  /**
   * Get String representation of recipe, name
   * @return String
   */ 
  public String toString () {
    return "GenericRecipe";
  }


  /**
   * Get XML representation of recipe (No longer in use)
   * @param String indent
   * @return String
   *
  public String fieldsToXML(String indent) {
    MEMEToolkit.trace("GenericRecipe::fieldsToXML()");
    StringBuffer sb = new StringBuffer(super.fieldsToXML(indent));
    return sb.toString();
  }
   */ 
}

