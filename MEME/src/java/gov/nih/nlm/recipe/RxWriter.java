/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe
 * Object:     RxWriter.java
 * 
 * Author:     Brian Carlsen, Owen Carlsen, Yun-Jung Kim
 *
 * Remarks:    This is the business logic behind the recipe writer.
 *
 * Testing:
 *             The following actions were tested to ensure that the flow
 *             of requests was correct no matter what the user clicks.
 * PASSED      1. File->New Recipe.  Cancel NextSection
 * PASSED      2. File->New Recipe.  Choose NextSection.  Cancel NextStep.
 * PASSED      3. File->New Recipe.  Choose NextSection.  Choose NextStep.
 *                Skip Step.  Request Next Section. Cancel next section request
 * PASSED      4. File->New Recipe.  Choose NextSection. Choose Next Step.
 *                Set Step.  Delete Step. Cancel next section
 *             5. File->New Recipe.  Choose Load Section. Skip step.
 *                Request Next Section. choose section
 *             5. File->New Recipe.  Choose Load Section. Skip step.
 *                Request Next Section. cancel selection
 *             6. File->New Recipe.  Cancel NextSection
 * 
 *****************************************************************************/
package gov.nih.nlm.recipe;

import java.io.File;
import java.util.HashMap;

import javax.swing.ToolTipManager;


/**
 * This object controls the objects involved in creating a recipe
 * and operations on the recipe.
 *
 * @author Brian Carlsen, Owen J. Carlsen, Yun-Jung Kim
 * @version 1.5
 */
public class RxWriter {

  //
  // Fields
  //
  protected RxWriterView rxw_frame = null;
  protected Recipe recipe = null;
  protected RxSection deleted_section = null;
  protected RxSection current_section = null;
  protected RxStep deleted_step = null;
  protected RxStep current_step = null;
  protected RxStep working_step = null;
  protected RxStep.View working_step_view = null;
  protected int current_section_index;
  protected int current_step_index;

  protected boolean save_and_exit_flag = false;
  protected boolean has_data_changed = false;

  protected String save_file_directory = null;
  protected String save_file_name = null;

  public int major_version = 1;
  public int minor_version = 5;
  
  /**
   * Default constructor.
   */
  public RxWriter() {
    this(null, null);
  }

  /**
   * Constructor
   * @param rx Recipe
   */
  public RxWriter( Recipe rx, String directory ) {

    recipe = rx;
    if (recipe != null) {
      current_section_index = Integer.valueOf((String)recipe.getAttribute("current_section_index")).intValue();
      current_section = (RxSection)recipe.sections.get(current_section_index);
      if ((current_section != null) && (Integer.valueOf((String)recipe.getAttribute("current_step_index")).intValue() != -1)){
        current_step_index = Integer.valueOf((String)recipe.getAttribute("current_step_index")).intValue();
        current_step = (RxStep)current_section.steps.get(current_step_index);
      }
      else {
	current_step = null;
      }
      working_step = (RxStep)recipe.getAttribute("working_step");
      if(working_step == null) 
	working_step = current_step;
    };

    save_file_directory = directory;
  }
  
  //
  // Methods
  //

  /**
   * This method is used when recipes are created or loaded
   * to connect all of the object up and prepare the display
   * This method should be called when the recipe is opened or
   * whenever the working_step changes.
   */
  public void refreshView() {
    refreshView(true);
  }

  public void refreshView( boolean get_values_flag ) {
    RxToolkit.trace("RxWriter::refreshView()");

    if (recipe == null) {
      // Set the enable map
      rxw_frame.setFunctionEnableMap();
      rxw_frame.setView(null,null);
    } else if (recipe.isEmpty()) {
      rxw_frame.setFunctionEnableMap();
      rxw_frame.setView(null,getEnableFunctionMask());
    } else {
      
      // display recipe name
      String name = recipe.getName();
      rxw_frame.setTitle("Recipe Writer -- " + 
			 (name == null ? "" : name) );
      
      // get the section view
      // get the working step view
      // put working step values into the view
      // add the step view to the section view
      // set the editing status in the section view
      // add the section view to the RxWriterView, enable functions
      RxToolkit.trace("RxWriter::refreshView - section, step : "+
		    current_section_index + "," +
		    current_step_index);
      RxSection.View tmp_view = current_section.getView();

      // if no step yet, display empty panel
      if (working_step != null) {
	working_step_view = working_step.getView();

	// We only want to get values in some cases 
	// because it can erase what user has entered if
	// the widget values have not been transfered to the object
	if (get_values_flag)
	  working_step_view.getValues();
	tmp_view.add(working_step_view);
	tmp_view.setStepStatus(working_step.getStatus().current_status);
      } else {
	working_step_view = null;
      };
      
      rxw_frame.setView(tmp_view, 
			getEnableFunctionMask());

      if (working_step_view != null)
	working_step_view.setFocus();

    }
  }

  /**
   * This method determines which RxWriter functions should
   * be enabled given the current state of current_section,
   * current_step, and working_step.
   * @return HashMap
   */
  public HashMap getEnableFunctionMask() {
    RxToolkit.trace("RxWriter::getEnableFunctionMask()");
    HashMap mask = new HashMap();

    // with a null recipe, the defaults in RxWriterView will take over
    if (recipe == null)
      return mask;

    if (recipe.isEmpty()) {
      mask.put(RxConstants.FN_NEXT_STEP,"false");
      mask.put(RxConstants.FN_PREVIOUS_STEP,"false");
      mask.put(RxConstants.FN_DELETE_STEP,"false");
      mask.put(RxConstants.FN_INSERT_STEP,"false");
      mask.put(RxConstants.FN_SET_STEP,"false");
      mask.put(RxConstants.FN_SKIP_STEP,"false");
      mask.put(RxConstants.FN_PREVIOUS_SECTION,"false");
    };

    // If there is no working step
    if (!recipe.isEmpty() && working_step == null) {
      mask.put(RxConstants.FN_DELETE_STEP,"false");
      mask.put(RxConstants.FN_SET_STEP,"false");
      mask.put(RxConstants.FN_SKIP_STEP,"false");
    };

    // If we are at the first section
    if (recipe.isEmpty() ||
	current_section == recipe.getFirstSection())
      mask.put(RxConstants.FN_PREVIOUS_SECTION,"false");

    // If we are at the last section and no more are allowed
     if (!recipe.isEmpty() && 
	current_section == recipe.getLastSection() &&
	!recipe.allowsMoreSections()) 
      mask.put(RxConstants.FN_NEXT_SECTION,"false");
    
    // If we are at the first section & first step (or first section
    // has no steps)
     if (!recipe.isEmpty() &&
	current_section == recipe.getFirstSection() &&
	((!current_section.isEmpty() &&
	  current_step == current_section.getFirstStep()) ||
	 current_section.isEmpty()))
      mask.put(RxConstants.FN_PREVIOUS_STEP,"false");
    
    // If we are at the last section/last step and no more steps are allowed
     if (!recipe.isEmpty() &&
	current_section == recipe.getLastSection() &&
	!recipe.allowsMoreSections() && 
	!current_section.isEmpty() &&
	current_step == current_section.getLastStep() &&
	!current_section.allowsMoreSteps()) 
      mask.put(RxConstants.FN_NEXT_STEP,"false");

    // If the working_step is untouched
     if (working_step != null &&
	working_step.getStatus().current_status == RxConstants.UNTOUCHED) {
      mask.put(RxConstants.FN_NEXT_STEP,"false");
      mask.put(RxConstants.FN_PREVIOUS_STEP,"false");
      mask.put(RxConstants.FN_DELETE_STEP,"false");
      mask.put(RxConstants.FN_INSERT_STEP,"false");
      mask.put(RxConstants.FN_NEXT_SECTION,"false");
      mask.put(RxConstants.FN_PREVIOUS_SECTION,"false");
    }
    if (working_step != null &&
	working_step.getStatus().current_status == RxConstants.SKIPPED) {
      mask.put(RxConstants.FN_SKIP_STEP,"false");
      mask.put(RxConstants.FN_DELETE_STEP,"false");
    }

    if (working_step != null &&
	working_step.getStatus().current_status == RxConstants.SAVED) {
      mask.put(RxConstants.FN_SKIP_STEP,"false");
    }
    return mask;

  }


  /**
   * This method returns true if the working step is done
   * This is true if it is null, skipped, or if it is saved
   * and the 
   * @return boolean
   */
  public boolean isWorkingStepDone () {
    RxToolkit.trace("RxWriter::isWorkingStepDone()");
    return (working_step == null || 	    
	    working_step.getStatus().current_status == RxConstants.SKIPPED ||
	    (working_step.getStatus().current_status == RxConstants.SAVED &&
	     !working_step_view.hasDataChanged() ) );
  };

  /**
   * This method returns true if the working step
   * has been skipped
   * @return boolean
   */
  public boolean isWorkingStepSkipped () {
    RxToolkit.trace("RxWriter::isWorkingStepSkipped()");
    return working_step.getStatus().current_status == RxConstants.SKIPPED;
  };

  /**
   * This method returns true if the working step
   * has been set
   * @return boolean
   */
  public boolean isWorkingStepSet () {
    RxToolkit.trace("RxWriter::isWorkingStepSet()");
    return working_step.getStatus().current_status == RxConstants.SAVED;
  };

  /**
   * This method is used to get the help screen for
   * the working step.
   */
  public void getWorkingStepHelp() {
    if (working_step != null) 
      working_step.getHelp();
    else
      RxToolkit.notifyUser("Working step is null, no help available.");
  }

  /**
   * This method returns true if the data has
   * changed since the last load/save
   * @return boolean
   */
  public boolean hasDataChanged () {
    RxToolkit.trace("RxWriter::hasDataChanged()");
    return has_data_changed;
  };

  //
  // The following are accessor methods
  //
  
  /**
   * Accessor method for getting recipe
   * @return Recipe
   */
  public Recipe getRecipe() {
    RxToolkit.trace("RxWriter::getRecipe()");
    return recipe;
  }
  
  /**
   * Accessor method for setting recipe
   * @param new_recipe Recipe
   */
  public void setRecipe(Recipe new_recipe) {
    RxToolkit.trace("RxWriter::setRecipe()");
    recipe = new_recipe;
  }

  /**
   * Accessor method for setting the view
   * @param view RxWriterView
   */
  public void setView(RxWriterView view) {
    RxToolkit.trace("RxWriter::setRxWriterView()");
    rxw_frame = view;
  }
  
  //
  //  The following methods are the "editing functions" supported by this
  //  application.  They roughly map to buttons on the RxWriterFrame
  //

  /**
   * This method is used to insert a section
   */
  public boolean requestInsertSection() {
    RxToolkit.trace("RxWriter::requestInsertSection()");

    RxStep local_cstep = current_step;
    int local_cstepi = current_step_index;
    RxStep local_wstep = working_step;
    RxSection local_csection = current_section;
    boolean local_hdc = has_data_changed;

    // Button should be disabled if there is no recipe
    // If not, this will catch it.
    if (recipe==null) {
      RxToolkit.reportError(
	  "You must create or open a recipe before\n" +
	  "inserting any sections.");
      return false;
    } 

    // cannot continue if work is uncommitted
    if (! isWorkingStepDone()) {
      RxToolkit.reportError(
	  "You have made changes, please set or skip\n"+
	  "this step before inserting a section.");
      return false;
    }

    boolean delete_request = false;
    // If section being left behind is empty, ask to delete it
    // unless its already been deleted
    if (current_section != null && current_section.isEmpty()) {
      delete_request = RxToolkit.confirmRequest(
			    "You are inserting a new section\n" +
			    "and have left the current section\n" +
			    "empty, do you want to delete it?");
    }    

    // Determine which class to create
    try {
      String [] class_names = recipe.getPossibleInsertSections(current_section);
      try {
	RxSection next_section = 
	  (RxSection)RxToolkit.classChooser(
		 class_names,"Insert Section","Select Section Type:");
	// bail if user cancelled
	if (next_section == null) {
	  return false;
	}

        deleted_section = current_section;

	next_section.setParent(recipe);
	
	current_section = next_section;
	recipe.addRxSection(++current_section_index,current_section);

	// New section current_step is null
	current_step = null;
	current_step_index = -1;
	working_step = null;
	
      } catch (Exception e) { 
	RxToolkit.reportError(
	     "Error while creating section: " +e);
	return false;
      };
    } catch (SectionNotAllowedException e2) {
      RxToolkit.notifyUser(
	  "This type of recipe does not allow sections to be inserted here.");
      return false;
    };
    
    has_data_changed = true;
    if(requestNextStep()) {
      // If user requested deletion of last section, delete it
      if (delete_request) {
        try{
          recipe.deleteSection(deleted_section);
        } catch(SectionNotFoundException e) {
        }
      }
      return true;
    }
    else {
      try{
        recipe.deleteSection(current_section);
      } catch (SectionNotFoundException e) {
      } 
      current_section = local_csection;
      current_step = local_cstep;
      current_step_index = local_cstepi;
      working_step = local_wstep;
      has_data_changed = local_hdc;
      return false;
    }
  };

  /**
   * This method is used to insert a step
   */
  public boolean requestInsertStep() {
    RxToolkit.trace("RxWriter::requestInsertStep()");

    // Button should be disabled if there is no recipe
    // If not, this will catch it.
    if (recipe==null) {
      RxToolkit.reportError(
	  "You must create or open a recipe before\n" +
	  "inserting any steps.");
      return false;
    } 

    // cannot continue if work is uncommitted
    if (! isWorkingStepDone()) {
      RxToolkit.reportError(
          "You have made changes, please set or skip\n" +
	  "this step before inserting another step.");
      return false;
    }

    // Determine which class to create
    try {
      String [] class_names = current_section.getPossibleInsertSteps(current_step);
      try {
	RxStep next_step = 
	  (RxStep)RxToolkit.classChooser(
		 class_names,"Insert Step","Select a step to insert:");
	// bail if user cancelled
	if (next_step == null) {
	 /* refreshView(); */
	  return false;
	}
	
	next_step.setParent(current_section);
	working_step = next_step;
	has_data_changed = true;
	
      } catch (Exception e) { 
	RxToolkit.reportError(
		"Error while loading step: " +e);
	return false;
      }
    } catch (StepNotAllowedException e2) {
      RxToolkit.notifyUser(
	"This type of section does not allow steps to be inserted here.");
      return false;
    }

    refreshView();
    return true;
  };

  /**
   * This method deletes a section after user confirmation
   */
  public boolean requestDeleteSection() {
    RxToolkit.trace("RxWriter::requestDeleteSection()");

    // Button should be disabled if there is no recipe
    // If not, this will catch it.
    if (recipe==null) {
      RxToolkit.reportError(
	  "You must create or open a recipe before\n" +
	  "deleting any sections.");
      return false;
    } 

    boolean request_ps = true;
    // can continue if work is uncommitted
    if (RxToolkit.confirmRequest("Are you sure you want to delete this section?")) {
      try {
	// Before we actually eliminate the section, we need to
	// request the next one, then delete the existing one
	deleted_section = current_section;

	// If the user cancelled the next section request
	// we should get the previous section
	if (!requestNextSection())
	  request_ps = requestPreviousSection();

	// current_section is likely no longer the same
	// so use deleted_section
	recipe.deleteSection(deleted_section);
	//current_section = null;
	//current_section_index = -1;
	has_data_changed = true;

	// If the previous section request failed then
	// the beginning of the recipe was reached.
	// and we should call startOfRecipeReached
	if (!request_ps)
	  startOfRecipeReached();

      } catch (SectionNotFoundException e) {
	RxToolkit.reportError(
	      "Attempt to delete nonexistent section: " +current_section);
	return false;
      }
    } else {
      return true;
    }

    return true;
  };

  /**
   * This method deletes a step after user confirmation
   */
  public boolean requestDeleteStep() {
    RxToolkit.trace("RxWriter::requestDeleteStep()");

    // Button should be disabled if there is no recipe
    // If not, this will catch it.
    if (recipe==null) {
      RxToolkit.reportError(
	  "You must create or open a recipe before\n" +
	  "deleting any steps.");
      return false;
    } 

    // If working step was skipped, don't delete it
    if (working_step != null && 
	working_step.getStatus().current_status == RxConstants.SKIPPED) {
      RxToolkit.reportError(
	  "You cannot delete a skipped step.");
      return false;
    } 

    // like with delete section, if changes were made to
    // the working step (if it was set) it doesn't matter, delete the step

    // can continue if work is uncommitted
    if (RxToolkit.confirmRequest("Are you sure you want to delete this step?")) {

      // mark step for deletion
      deleted_step = current_step;

      // We need to get the next step before deleting the current one
      // because requestNextStep uses it to determine what should come next

      // If the current_step still == deleted_step
      // the next step request failed because the user cancelled it
      // request previous step if current_step != first step of the section
      if (!requestNextStep()) {
	if(current_step != current_section.getFirstStep())
	  requestPreviousStep();
      
        // Otherwise notify user that the section has been left empty
        else
          RxToolkit.notifyUser("Deleting the step has left this section empty.");
      }

      // Delete the step
      try {
	// current_step is likely no longer the same
	// so use deleted_step
	current_section.deleteStep(deleted_step);
      } catch (StepNotFoundException e) {
	RxToolkit.reportError("The current step ("+ current_step + 
			       ")\n being deleted couldn't be found in the\n"+
			       "current section (" + current_section + ").\n" +
			       "This is a serious error.");
	return false;
      }
      //current_step = null;
      //current_step_index = -1;
      working_step = null;
      has_data_changed = true;
      
    } else {
      return false;
    }

    refreshView();
    return true;

  };

  /**
   * This method is used to open a recipe.
   *
   * It pops up a filechooser to get the file to load 
   * from the user, loads the Recipe object from a serialized 
   * form and creates the view objects for each deserialized
   * step and also workingStep of each section whose steps do 
   * not exist yet.
   */
  public boolean requestLoad() {
     return requestLoad(null);
  }

  public boolean requestLoad(File file) {
    RxToolkit.trace("RxWriter::requestLoad("+file+")");

    if (! isWorkingStepDone()) {
      RxToolkit.reportError(
	  "You have made changes, you must set or skip\n" +
	  "this step before opening a different recipe.");
      return false;
    } 
    
    saveRecipeIfChanged();
     
    // If file is passed in, use it, otherwise look up save_file_directory
    if (save_file_directory != null)
      file = new File(save_file_directory);
    
   // final File fl = file;
   // try {
   //   SwingUtilities.invokeAndWait(new Runnable(){public void run() { 
   //   rxw_frame.startWaitMode();
   //   MEMEToolkit.trace("after startWaitMode");
      try {
        recipe = (Recipe)RxToolkit.open(file);
   //     rxw_frame.stopWaitMode();
   //   MEMEToolkit.trace("after stopWaitMode");
        File f = RxToolkit.getFileForObject(recipe);
        save_file_directory = f.getParent();
        save_file_name = f.getName();
        RxToolkit.setLocation(save_file_directory);

        // When loading a recipe, get the bookmarked 
        // current_section, current_step, and working_step
        current_section_index = ((Integer)recipe.getAttribute("current_section_index")).intValue();
        if (current_section_index >= recipe.sections.size())
          current_section_index = recipe.sections.size()-1;
        current_section = (RxSection)recipe.sections.get(current_section_index);
        RxToolkit.trace("CURRENT_SECTION" + current_section.toString());
        if(((Integer)recipe.getAttribute("current_step_index")).intValue() != -1){
          current_step_index = ((Integer)recipe.getAttribute("current_step_index")).intValue();
          current_step = (RxStep)current_section.steps.get(current_step_index);
        } 
        else {
	  RxToolkit.trace("current-step = null");
	  current_step = null;
        }
        //MEMEToolkit.trace("CURRENT_STEP" + current_step.toString());
	  RxToolkit.trace("before working_step gets assigned");
        working_step = (RxStep)recipe.getAttribute("working_step");
      } catch (Exception e) {
   //    rxw_frame.stopWaitMode();
        e.printStackTrace();
        RxToolkit.reportError(e.getMessage());
        recipe = null;
      }
   //   }});
   // } catch (Exception e) {
   //   e.printStackTrace();
   //   MEMEToolkit.reportError(e.getMessage());
   //   recipe = null;
   // }

    // If it failed, recipe will be null
    if (recipe == null) {
      clearRecipe();
      refreshView();
      return false;
     }

    RxToolkit.setRecipe(recipe);
    has_data_changed = false;
   
    refreshView();
    return true;
  }
    

  /**
   * This method creates a new recipe
   * It dynamically loads a recipe type based on the user's input
   */
  public boolean requestNewRecipe() {
    RxToolkit.trace("RxWriter::requestNewRecipe()");

    Recipe local_recipe = recipe;

    saveRecipeIfChanged();

    // When a new recipe is requested,
    // We load a set of class names for recipe subclasses.
    // The user picks one and we make a new one of those.

    String [] class_names;

    try {
      class_names = RxToolkit.getSubclasses(
				RxConstants.RECIPE_PACKAGE_NAME,
				RxConstants.RECIPE_PACKAGE_NAME + ".Recipe");
    } catch (ClassNotFoundException e) {
      RxToolkit.reportError(
          "Error, class " + RxConstants.RECIPE_PACKAGE_NAME + ".Recipe" +
	  " not found.");
      return false;
    }
    
    try {
      Recipe tmp_recipe = (Recipe)RxToolkit.classChooser(
			   class_names,"New Recipe","Select a Recipe Type:");
      // bail if user cancelled
      // no change to existing recipe
      if (tmp_recipe == null) {
/*	refreshView(); */
	return false;
      }
      clearRecipe();
      recipe = tmp_recipe; 
      RxToolkit.setRecipe(recipe);
    } catch (Exception e) { 
      RxToolkit.reportError(
          "Error while creating recipe: " +e);
      return false;
    };
    
    // Upon successful Recipe creation, 
    // get the source directory & move to the first section
    if(!requestNextSection()) {
      recipe = local_recipe;
      return false;
    }

    return true;
  }
    
  /**
   * This method moves to the next section
   * Or reaches the end of the recipe
   */
  public boolean requestNextSection() {
    RxToolkit.trace("RxWriter::requestNextSection()");

    RxStep local_cstep = current_step;
    int local_cstepi = current_step_index;
    RxSection local_csection = current_section;
    int local_csectioni = current_section_index;
    boolean local_hdc = has_data_changed; 

    // Button should be disabled if there is no recipe
    // If not, this will catch it.
    if (recipe==null) {
      RxToolkit.reportError(
	  "You must create or open a recipe before\n" +
	  "moving to the next section.");
      return false;
    } ; 

    // Can't advance to next section while working step is uncommitted
    if (! isWorkingStepDone()) {
      RxToolkit.reportError(
	    "You have made changes, please set or skip\n" +
	    "this step before moving to the next section.");
      return false;
    }

    // If section being left behind is empty, ask to delete it
    // unless its already been deleted
    boolean delete_request = false;
    if (current_section != deleted_section &&
	current_section != null && current_section.isEmpty()) {
       delete_request = RxToolkit.confirmRequest(
				"You are moving to a new section\n" +
				"and have left the current section\n" +
				"empty, do you want to delete it?");
    }

    // Ask recipe for getSectionAfter(current_section)
    // If end of recipe is reached, report it
    // If no section found query for: getPossibleNextSections.
    // If just one is returned, instantiate it, set
    // the current section, null the current_step and the working
    // step and call requestNextStep();
    
    RxSection next_section;
    try {
       next_section = recipe.getSectionAfter(current_section);
    } catch (SectionNotFoundException e1) {

      String [] class_names = recipe.getPossibleNextSections(current_section);
      try {
	next_section = (RxSection)RxToolkit.classChooser(
		   class_names,"Select a Next Section","Select a Section Type:");
	// bail if user cancelled
	// no change to sections
	if (next_section == null) {

	  // If user requested to delete the current section
	  // because it is empty and we are moving on
	  // but the moving on was cancelled, let user know.
	  return false;
	}

	deleted_section = current_section;

        recipe.addRxSection(next_section);

	next_section.setParent(recipe);

      } catch (Exception e) { 
	RxToolkit.reportError(
	      "Error while creating section: " +e);
	return false;
      };

      // If we are creating a new section, current_step is null
      current_step = null;
      current_step_index=-1;
      
    
    } catch (EndOfRecipeException e2) {
      // If user chose to delete but end of recipe was reached
      // Do not execute delete request
      if (!delete_request) 
	endOfRecipeReached();

      return false;
    }  
 
    //*** there is a problem here, if end of recipe is reached
    // then this will never happen, and if it does, we need to go to
    // the previous section, etc...

    current_section = next_section;
    current_section_index = recipe.indexOf(next_section);
    has_data_changed = true;
    if(requestNextStep()) {
      // If user requested deletion of last section, delete it
      if (delete_request) {
        try{
          recipe.deleteSection(deleted_section);
        } catch(SectionNotFoundException e) {
        }
      }
      return true;
    }
    else {
      try{
        recipe.deleteSection(next_section);
      } catch (SectionNotFoundException e) {
      }
      current_step = local_cstep;
      current_step_index = local_cstepi;
      current_section = local_csection;
      current_section_index = local_csectioni;
      has_data_changed = local_hdc;
      return false;
    } 
  };

  /**
   * This method calls recipe's nextStep and sets the frame's view to the 
   * step's view. If there is no more steps left in the section, it moves to
   * the nextSection's first step. If the current Section is a merge section,
   * the user gets an option dialog to decide what type of the step s/he wants.
   **/
  public boolean requestNextStep() {
    RxToolkit.trace("RxWriter::requestNextStep()");

    // Button should be disabled if there is no recipe
    // If not, this will catch it.
    if (recipe==null) {
      RxToolkit.reportError(
	  "You must create or open a recipe before\n" +
	  "moving to the next step.");
      return false;
    } 

    // Cannot move on if work is uncommitted
    if (! isWorkingStepDone()) {
      RxToolkit.reportError(
	  "You have made changes, please set or skip\n" +
	  "this step before moving to the next step.");
      return false;
    }

    // Ask current_section for getStepAfter(current_step)
    // If end of section is reached, call requestNextSection
    // If no section found query for: getPossibleNextSteps.
    // If just one is returned, instantiate it, set
    // the working step and call refreshView

    RxStep next_step;
    try {
      // set current = next only if we are browsing 
      // (i.e. step after current one is found)
       next_step = current_section.getStepAfter(current_step);
       current_step = next_step;
       current_step_index = current_section.indexOf(current_step);
       working_step = current_step;
    } catch (StepNotFoundException e1) {

      String [] class_names = current_section.getPossibleNextSteps(current_step);
      try {
	next_step = (RxStep)RxToolkit.classChooser(
		   class_names,"Next Step","Select a Step Type:");
	// bail if user cancelled
	if (next_step == null) {
	  // It is possible that the current_step has 
	  // been deleted and this is coming off of a 
	  // delete step request or from a new recipe request.  I

	  // let requestDeleteStep figure it out
	 /* refreshView();*/
	  return false;
    	}
	next_step.setParent(current_section);
	// If new step created
	// don't adjust current_step, only working step
	working_step = next_step;
	has_data_changed = true;
      } catch (Exception e) { 
	RxToolkit.reportError(
	      "Error while creating step: " +e);
	e.printStackTrace();
	return false;
      };

    } catch (EndOfSectionException e2) {
      requestNextSection();
      return false;
    }

    refreshView();

    return true;
  };

  /**
   * This method moves to the previous section.
   */
  public boolean requestPreviousSection() {
    RxToolkit.trace("RxWriter::requestPreviousSection()");

    // Button should be disabled if there is no recipe
    // If not, this will catch it.
    if (recipe==null) {
      RxToolkit.reportError(
	  "You must create or open a recipe before\n" +
	  "moving to the previous section.");
      return false;
    } 
    
    // Cannot move on if work is uncommitted
    if (! isWorkingStepDone()) {
      RxToolkit.reportError(
	  "You have made changes, please set or skip\n" +
	  "this step before moving to the previous section.");
      return false;
    }

    // Get previous section, if it throws SectionNotFoundException
    // then call startOfRecipeReached();
    // else set current_section, and getLastStep on that section
    // and set section and set current_step and working_step
    try {
      RxSection prev_section = recipe.getSectionBefore(current_section);

      // If section being left behind is empty, ask to delete it
      // unless its already been marked for deletion 
      if (current_section != deleted_section &&
	  current_section != null && current_section.isEmpty()) {
	boolean response = RxToolkit.confirmRequest(
			    "You are moving to the previous section\n" +
			    "and have left the current section\n" +
			    "empty, do you want to delete it?");
	if (response) {
	  recipe.deleteSection(current_section);
	  deleted_section = current_section;
	}
      }

      current_section = prev_section;
      current_section_index = recipe.indexOf(prev_section);
      RxStep step = prev_section.getLastStep();
      current_step = step;
      current_step_index = current_section.indexOf(current_step);
      working_step = current_step;
    } catch (SectionNotFoundException e1) {
      // If this came via a delete section request
      // the current_section will == deleted_section.
      // in that case just return
      if (current_section != deleted_section)
	startOfRecipeReached();

    } catch (IndexOutOfBoundsException e2) {
      // If getLastStep fails this will be triggered
      RxToolkit.reportError(
	    "Empty section found ("+current_section+").\n"); 
      current_step = null;
      current_step_index = -1;
      working_step = null;
    }
    has_data_changed = true;
    refreshView();
    return true;
  }

  /**
   * This method forwards to Recipe's previousStep. 
   * If there is no previous step for the current_section,
   *  give the last step of the previous section.
   */
  public boolean requestPreviousStep() {
    RxToolkit.trace("RxWriter::requestPreviousStep()");

    // Button should be disabled if there is no recipe
    // If not, this will catch it.
    if (recipe==null) {
      RxToolkit.reportError(
	  "You must create or open a recipe before\n" +
	  "moving to the previous step.");
      return false;
    } 

    // Cannot move on if work is uncommitted
    if (! isWorkingStepDone()) {
      RxToolkit.reportError(
	  "You have made changes, please set or skip\n" +
	  "this step before moving to the previous step.");
      return false;
    }
    
    // what happens if the working step was skipped and never
    // added to the current section, and then the user clicks
    // Previous Step?  It should just return to the current step
    // if the current step is not in the current section
    // it should call requestPreviousSection();

    // If working_step is skipped and != current_step,
    // then just return current_step
    if (isWorkingStepSkipped() && working_step != current_step) {

      // If its a new section current_step == null
      if (current_step == null) {
        return requestPreviousSection();
      }
      // else set working_step = current_step 7 request new view
      working_step = current_step;

    } else {

      // Get previous step, if it throws StepNotFoundException
      // then call requestPreviousSection
      // else set current_step and working_step
      try {
	RxStep prev_step = current_section.getStepBefore(current_step);
	current_step = prev_step;
	current_step_index = current_section.indexOf(prev_step);
	working_step = current_step;
      } catch (StepNotFoundException e1) {
        return requestPreviousSection();
      }
    }
    has_data_changed = true;
    refreshView();

    return true;
  }
    
  /**
   * This method saves to the default file
   */
  public boolean requestSave() {
    RxToolkit.trace("RxWriter::requestSave()");
    requestSerialize(false);
    return true;
  }

  /**
   * This method saves the recipe to a user selected file
   */
  public boolean requestSaveAs() {
    RxToolkit.trace("RxWriter::requestSaveAs()");
    requestSerialize(true);
    return true;
  }
    
  /**
   * This method serializes the recipe object.
   * @param save_as boolean
   */
  private boolean requestSerialize(boolean save_as) {
    RxToolkit.trace("RxWriter::requestSerialize(" + save_as + ")");

    // Check for empty recipe
    if (recipe==null) {
      RxToolkit.reportError(
        "Recipe has not been created. You must create \n" +
	"or open a recipe before saving.");
      return false;
    }

    String source_name;
    if (recipe.getName().equals("")) {
      source_name = RxToolkit.getUserInput(
        "Recipe does not have a name. \n Please enter a name.");
      recipe.setName(source_name);
    }

    // When saving the recipe, bookmark 
    // current_section, current_step, and working_step
    recipe.setAttribute("current_section_index",new Integer(current_section_index));
    recipe.setAttribute("current_step_index", new Integer(current_step_index));
    recipe.setAttribute("working_step", working_step);

    RxToolkit.trace("current_section: "+current_section);
    RxToolkit.trace("current_step: "+current_step);
    RxToolkit.trace("working_step: "+working_step);
    // commit any working step changes to the working step
    working_step_view.setValues();

    try {
      if (save_as)
	RxToolkit.saveAs(recipe);
      else
	RxToolkit.save(recipe);
      File obj_file = RxToolkit.getFileForObject(recipe);
      save_file_directory = obj_file.getParent();
      save_file_name = obj_file.getName();
      File html_file = new File(save_file_directory, recipe.getHTMLFilename());
      String html = recipe.toHTML();
      RxToolkit.writeToFile(html,html_file);
      RxToolkit.logComment("Saved recipe to file \"" + obj_file + "\"",true);
      RxToolkit.logComment("Saved HTML to file \"" + html_file + "\"");

    } catch (Exception e) {
      RxToolkit.reportError(e.getMessage());
      e.printStackTrace();
      return false;
    }

    has_data_changed = false;
    return true; 

  }
    
  /**
   * This method corresponds to the "Set" button in the frame
   * it commits the builder's working step
   */
  public boolean requestSet() {
    RxToolkit.trace("RxWriter::requestSet()");
    
    // Check for empty recipe
    if (recipe==null) {
      RxToolkit.reportError(
        "Recipe has not been created. You must create \n" +
	"or open a recipe before setting steps.");
      return false;
    }

    // Commit the values in the view to the step. (this is done so that if
    //   validation fails, the user selected values stay)
    // Validate user input (the view should report errors)
    // Set status of working_step
    // Set the current step = working_step 
    // Add the working step to the current section.
    //   it must be added with the index in case we are inserting
    // Notify section that step has been set
    if (working_step_view.checkUserEntry()) {
      working_step_view.setValues();

      // Only add the step if it is not already in the list
      if (!current_section.contains(working_step)) {
	RxToolkit.trace("RxWriter::requestSet() - adding step");
	//current_section.addRxStep(current_step_index+1,working_step);
	current_section.addRxStep(working_step);
      };
      current_step = working_step;
      current_step_index = current_section.indexOf(current_step);
      current_step.getStatus().current_status = RxConstants.SAVED;
      current_section.stepSet(current_step);
      RxToolkit.trace("RxWriter::requestSet()  "+current_step+","+
			current_step_index);
      has_data_changed = true;
    };

    // optionally call
    // requestNextStep();

    refreshView(false);
    return true;
  }
    
  /**
   * This method method skips the working step
   */
  public boolean requestSkip() {
    RxToolkit.trace("RxWriter::requestSkip()");

    // Check for empty recipe
    if (recipe==null) {
      RxToolkit.reportError(
        "Recipe has not been created. You must create \n" +
	"or open a recipe before skipping steps.");
      return false;
    }

    // if the working step has already been added to the
    // list of steps, skipping it should cause it to be removed
    if (working_step.getStatus().current_status == RxConstants.SAVED) {
      RxToolkit.reportError(
	"You cannot skip a saved step.\n"+
	"Use Delete Step to remove it");
      return false;
    }

    // set editing status of working step to skipped.
    // notify section that step was skipped
    working_step.getStatus().current_status = RxConstants.SKIPPED;
    current_section.stepSkipped(working_step);
    has_data_changed = true;

    //optionally call
    //requestNextStep;

    refreshView(false);
    return true;

  }
    
  /**
   * This method creates an html file that represents the Recipe object and invokes
   * a browser. If the initialized browser does not work for some reason, this
   * application's html viewer is invoked.
   */
  public boolean requestViewAsHTML() {
    RxToolkit.trace("RxWriter::requestViewAsHTML()");

    // Check for empty recipe
    if (recipe==null) {
      RxToolkit.reportError(
        "Recipe has not been created. You must create \n" +
	"or open a recipe before viewing it.");
      return false;
    }

    String source_name;
    if (recipe.getName().equals("")) {
      source_name = RxToolkit.getUserInput(
        "Recipe does not have a name. \n Please enter a name.");
      recipe.setName(source_name);
    }

    // Render the recipe as HTML
    String html = recipe.toHTML();

    if (save_file_directory == null)
      save_file_directory = RxToolkit.getProperty(
				    RxConstants.TMP_DIRECTORY,
				    RxConstants.DEFAULT_TMP_DIRECTORY);

    File file = new File(save_file_directory, recipe.getHTMLFilename());
    try {
      RxToolkit.writeToFile( html, file );
    } catch (Exception e) {
      RxToolkit.reportError(e.getMessage());
      return false;
    }

    RxToolkit.logComment("View recipe HTML file \"" + file + "\"",true);
    Thread t = new Thread (
       new Runnable() {
  	 public void run () { 
	   RxToolkit.viewHTML(new File (save_file_directory,
					  recipe.getHTMLFilename()));
	 }
       });
    t.start();
    return true;
  }

  /**
   * This method creates an .csh script for running the recipe.
   */
  public boolean requestWriteShellScript() {
    RxToolkit.trace("RxWriter::requestWriteShellScript()");

    // Check for empty recipe
    if (recipe==null) {
      RxToolkit.reportError(
        "Recipe has not been created. You must create \n" +
	"or open a recipe before writing it to a shell script.");
      return false;
    }

    String source_name;
    if (recipe.getName().equals("")) {
      source_name = RxToolkit.getUserInput(
        "Recipe does not have a name. \n Please enter a name.");
      recipe.setName(source_name);
    }

    // Convert recipe to script
    String script = recipe.toShellScript();

    if (save_file_directory == null)
      save_file_directory = RxToolkit.getProperty(
				    RxConstants.TMP_DIRECTORY,
				    RxConstants.DEFAULT_TMP_DIRECTORY);

    File file = new File(save_file_directory, recipe.getShellScriptName());
    try {
      RxToolkit.writeToFile( script, file );
    } catch (Exception e) {
      RxToolkit.reportError(e.getMessage());
      return false;
    }
    
    RxToolkit.notifyUser("The shell script has been created.");

    return true;
  }

  //
  // These methods implement the RxWriterListnener interface
  //

  /**
   * This method warns the user that the end of the recipe
   * has been reached, either in browsing or normal editing
   */
  public void endOfRecipeReached() {
    RxToolkit.notifyUser(
       "The end of the recipe has been reached.\n" +
       "You may want to save your work now.");
  };

  /**
   * This method warns the user that the end of the recipe
   * has been reached, either in browsing or normal editing
   */
  public void startOfRecipeReached() {
    // If we have reached the start of the recipe as a result of the
    // user cancelling offers to create new steps, notify user
    // That the recipe is empty and he/she should add a section.
    if (recipe.isEmpty()) {
      Recipe tmp_recipe = recipe;
      clearRecipe();
      recipe = tmp_recipe;
      RxToolkit.notifyUser(
       "The recipe is empty, please choose a 'Next Section'.");
    } else if (current_section.isEmpty()) {
      RxToolkit.notifyUser(
       "The recipe is empty, please choose a 'Next Section'.");
    } else {
      RxToolkit.notifyUser(
       "The beginning of the recipe has been reached.");
    }
    refreshView();
  };

  /**
   * This method sets the recipe, section, and step to null;
   */
  public void clearRecipe () {
    recipe = null;
    current_section = null;
    current_step = null;
    working_step = null;
    working_step_view = null;
  }
  /**
   * This method checks if the recipe should be saved
   * it prompts the user and saves if requested
   */
  public void saveRecipeIfChanged() {
    
    if (hasDataChanged()) {
      boolean result = RxToolkit.confirmRequest(
	"Save changes to current recipe?");
      if (result) {
	rxw_frame.setCursor(RxWriterView.wait_cursor);
	requestSave();
	rxw_frame.setCursor(RxWriterView.default_cursor);
      }
    }
    // either way if user saved data is fresh, and if
    // not we're moving on so data is fresh
    has_data_changed = false;
  }

  /**
   * This method checks if the recipe should be saved before exiting
   * It also calls MEMEToolkit.Exit to cleanup resources before exiting.
   */
  public void saveAndExit () {
    if (rxw_frame != null && !save_and_exit_flag)
      rxw_frame.saveAndExit();
    save_and_exit_flag = true;
  }
  
  /**
   * main entrypoint - starts the part when it is run as an application
   * @param args String[]
   */
  public static void main(String[] args) {

    // Set tooltip delay to 3/4 second
    ToolTipManager.sharedInstance().setInitialDelay(500);
    ToolTipManager.sharedInstance().setDismissDelay(10000);

    // If no property file has been specified
    // add the DEFAULT_PROPERTY_FILE
    if (System.getProperty(RxConstants.PROPERTY_FILE) == null)
      System.setProperty(RxConstants.PROPERTY_FILE,
			 RxConstants.DEFAULT_PROPERTY_FILE);

    // record save directory
    System.setProperty(RxConstants.SAVE_DIRECTORY,
		       System.getProperty(RxConstants.SRC_DIRECTORY,""));

    // Initialize the toolkit (& meme toolkit);
    RxToolkit.initialize(RxConstants.WRITER_ALLOWABLE_PROPERTIES,
			   RxConstants.WRITER_REQUIRED_PROPERTIES);
    RxToolkit.initialize();

    RxToolkit.trace("RxWriter::main("+args+")");

    RxToolkit.setPersistenceManager(new XMLRxManager());

    // Load a recipe if there is one
    Recipe recipe = null;

    String recipefile = RxToolkit.getProperty(RxConstants.RECIPE_FILE);
    RxToolkit.trace("RxRunner::main - RECIPE_FILE is: " + recipefile);

    File f = null;
    if (recipefile == null || recipefile.equals("")) {
      recipe = null;
    }
    else {
      try {

	f = new File(recipefile);
	recipe = (Recipe)RxToolkit.open(f);
	
      } catch (Exception e) {
	RxToolkit.reportError("Error: " + e.getMessage());
	e.printStackTrace();
	recipe = null;
      };
    }

    // Create application object
    String directory = null;
    if (f != null)
      directory = f.getParent();
    final RxWriter rx_writer = new RxWriter(recipe,directory);

    RxToolkit.logComment(
       "RecipeWriter --- version " + rx_writer.major_version +
       "." + rx_writer.minor_version + " --- Lexical Technology, Inc.");

    RxToolkit.setRxWriter(rx_writer);
    new RxWriterView(rx_writer);

    // Save and exit on Control-C
   /* Runtime.getRuntime().addShutdownHook( new Thread() {
      public void run () {
	rx_writer.saveAndExit();
      }
    }); */
  }

}
