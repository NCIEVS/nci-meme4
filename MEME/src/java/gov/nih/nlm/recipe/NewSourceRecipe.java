/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe
 * Object:     NewSourceRecipe.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 * Remarks:   This is a NewSource recipe
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

import gov.nih.nlm.recipe.sections.LoadSection;
import gov.nih.nlm.recipe.sections.PostMergeSection;

import java.io.File;

import javax.swing.JFileChooser;


/**
 * This is a NewSource subclass of Recipe
 *
 * @author Brian Carlsen
 * @version 1.5
 */
public class NewSourceRecipe extends Recipe {
  
  //
  // Fields
  //
  
  private int index = 0;
  private boolean contains_loadsection = false;
  private boolean contains_postmergesection = false;
  private String [] section_list = 
  { RxConstants.RX_SECTION_PACKAGE_NAME + ".LoadSection",
    RxConstants.RX_SECTION_PACKAGE_NAME + ".NewMergeSection",
    RxConstants.RX_SECTION_PACKAGE_NAME + ".PostMergeSection"
  };

  //
  // Constructors
  //

  /**
   * Recipe constructor comment.
   */
  public NewSourceRecipe() {
    super();
    RxToolkit.trace("NewSourceRecipe::NewSourceRecipe()");

    // We need to find the src file directory & set it in RxToolkit.
    String src_directory = RxToolkit.getProperty(RxConstants.SRC_DIRECTORY);
    if (src_directory.equals("")) {
      File loc_file = null;
      while (loc_file == null) {
	loc_file = RxToolkit.chooseFile(
			   "Find the directory containing the .src files",
			   "OK", JFileChooser.DIRECTORIES_ONLY, new File(""));
      };
      src_directory = loc_file.getPath();
      RxToolkit.setProperty(RxConstants.SRC_DIRECTORY,src_directory);
    }
  }
  
  //
  // This is the implementation of the Recipe methods
  //

  /** 
   * This method returns the work type
   * @return String
   */
  public String getWorkType () {
    return RxConstants.INSERTION_WORK;
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
    RxToolkit.trace("NewSourceRecipe::getView()");
    return new Recipe.View();
  }

  /**
   * No sections can be inserted.
   * @return String []
   */
  public String [] getPossibleInsertSections (RxSection current) 
  throws SectionNotAllowedException {
    RxToolkit.trace("NewSourceRecipe::getPossibleInsertSections()");
    return new String [] {RxConstants.RX_SECTION_PACKAGE_NAME + ".GenericSection"};
  };

  /**
   * A LoadSection must come first.
   * Next is a MatchSection,RelationshipMatchSection,or PostMergeSection
   * Once a PostMergeSection is added allows_more = false
   *
   * @return String []
   */
  public String [] getPossibleNextSections(RxSection current) {
    RxToolkit.trace("NewSourceRecipe::getPossibleNextSections()");
    if (contains_loadsection)
      return new String [] {section_list[index++]};
    else {
      index = 1;
      return new String [] {section_list[0]};
    }
  };
  
  /**
   * Only 3 sections are allowed and must appear in order.
   * @return boolean
   */
  public boolean allowsMoreSections() {
    RxToolkit.trace("NewSourceRecipe::allowsMoreSections()");
    return (!contains_postmergesection);
  };

  /**
   * This static method returns a name describing the 
   * recipe type.  This is used by anything that dynamically 
   * loads the class. To see a string representation of what the 
   * class is.
   * @return String
   */
  public static String typeToString () {
    RxToolkit.trace("NewSourceRecipe::typeToString()");
    return "New Source Recipe";
  };

  /**
   * Get String representation of recipe, name
   * @return String
   */ 
  public String toString () {
    return typeToString();
  }

  /**
   * Get XML representation of recipe (No longer in use)
   * @param String indent
   * @return String
   *
  public String fieldsToXML(String indent) {
    MEMEToolkit.trace("NewSourceRecipe::fieldsToXML()");
    StringBuffer sb = new StringBuffer(super.fieldsToXML(indent));
    sb.append(indent);
    sb.append("<Field name=\"allows_more\">");
    sb.append(allows_more);
    sb.append("</Field>\n");
    sb.append(indent);
    sb.append("<Field name=\"contains_loadsection\">");
    sb.append(contains_loadsection);
    sb.append("</Field>\n");
    sb.append(indent);
    sb.append("<Field name=\"contains_postmergesection\">");
    sb.append(contains_postmergesection);
    sb.append("</Field>\n");
    return sb.toString();
  };
   */ 

  //
  // Overloading of Standard Recipe methods
  //
  /**
   * This method inserts a RxSection at the end of the sections array
   * @param section RxSection
   */
  public void addRxSection (RxSection section) {
    RxToolkit.trace("Recipe::addRxSection("+section+")");
    super.addRxSection(section);
    if (section instanceof PostMergeSection)
      contains_postmergesection = true;
    else if (section instanceof LoadSection)
      contains_loadsection = true;
  };

  /**
   * This method inserts a RxSection at the end of the sections array
   * @param index int
   * @param section RxSection
   */
  public void addRxSection (int index, RxSection section) {
    RxToolkit.trace("Recipe::addRxSection("+index+","+section+")");
    super.addRxSection(index,section);
    if (section instanceof PostMergeSection)
      contains_postmergesection = true;
    else if (section instanceof LoadSection)
      contains_loadsection = true;
  };

}

