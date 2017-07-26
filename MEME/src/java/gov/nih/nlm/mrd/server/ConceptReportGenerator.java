package gov.nih.nlm.mrd.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.MRDAtom;
import gov.nih.nlm.meme.common.MRDConcept;
import gov.nih.nlm.meme.common.MRDRelationship;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.server.MEMEApplicationService;
import gov.nih.nlm.meme.server.SessionContext;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.common.SourceType;
import gov.nih.nlm.mrd.sql.MRDDataSource;

public class ConceptReportGenerator implements MEMEApplicationService{

 //
 // Implementation of MEMEApplicationService interface
 //
	  /**
	   * Returns <code>false</code>.
	   * @return <code>false</code>
	   */
	  public boolean requiresSession() {
	    return false;
	  }
	  /**
	   * Returns <code>false</code>.
	   * @return <code>false</code>
	   */
	  public boolean isRunning() {
	    return false;
	  }

	  /**
	   * Returns <code>false</code>.
	   * @return <code>false</code>
	   */
	  public boolean isReEntrant(SessionContext sc) {
	    return false;
	  }

 /**
  * Processes request from the {@link MRDApplicationServer}.
  * @param context the {@link SessionContext}
  * @throws MEMEException if failed to process the request
  */
 	public void processRequest(SessionContext context) throws MEMEException {
	   final MRDDataSource data_source = (MRDDataSource) context.getDataSource();
	   final MEMEServiceRequest request = context.getServiceRequest();
	   final String function = (String) request.getParameter("function").getValue();
	   try {
		   if (function.equals("cuiConceptReport")) {
		      String cui = (String) request.getParameter("cui").getValue();
		      MRDConcept concept = data_source.getConcept(cui);
		      request.addReturnValue(new Parameter.Default("ConceptReport", concept));
		   } else if (function.equals("conceptIdConceptReport")) {
			   Integer concept_id = Integer.parseInt((String)(request.getParameter("conceptId").getValue())) ;
			   String cui = (String) data_source.getCUIForConceptId(concept_id.intValue());
			   MRDConcept concept = data_source.getConcept(cui);
			   request.addReturnValue(new Parameter.Default("ConceptReport", concept));
		   } else if (function.equals("auiConceptReport")) {
			   String aui = (String) request.getParameter("aui").getValue();
			   String cui = data_source.getCUIForAUI(aui);
			   MRDConcept concept = data_source.getConcept(cui);
			   request.addReturnValue(new Parameter.Default("ConceptReport", concept));
		   } else if (function.equals("codeConceptReport")) {
			   String code = (String) request.getParameter("code").getValue();
			   int size = request.getParameter("count").getInt() + 1;
			   MRDAtom[] auis = data_source.getMRDAtomsForCode(code, size);
			   request.addReturnValue(new Parameter.Default("CodeData", auis));
		   } else if (function.equals("ttyConceptReport")) {
			   String tty = (String) request.getParameter("tty").getValue();
			   Parameter source = request.getParameter("source");
			   int size = request.getParameter("count").getInt() + 1;
			   MRDAtom[] auis = data_source.getMRDAtomsForTty(tty, 
					   source != null ? (String)source.getValue() : null, size);
			   request.addReturnValue(new Parameter.Default("TtyData", auis));
		   } else if (function.equals("relConceptReport")) {
			   String rel = (String) request.getParameter("rel").getValue();
			   Parameter rela = request.getParameter("rela");
			   Parameter source = request.getParameter("source");
			   int size = request.getParameter("count").getInt() + 1;
			   MRDRelationship[] ruis = data_source.getMRDRelationsForRelName(rel,
					   rela != null ? (String)rela.getValue() : null,
					   source != null ? (String) source.getValue() : null,
					   size);
			   request.addReturnValue(new Parameter.Default("RelationsData", ruis));
		   } else if (function.equals("relaConceptReport")) {
			   String rela = (String) request.getParameter("rela").getValue();
			   Parameter rel = request.getParameter("rel");
			   Parameter source = request.getParameter("source");
			   int size = request.getParameter("count").getInt() + 1;
			   MRDRelationship[] ruis = data_source.getMRDRelationsForRelAttribute(rela,
					   rel != null ? (String)rel.getValue() : null,
					   source != null ? (String) source.getValue() : null,
					   size);
			   request.addReturnValue(new Parameter.Default("RelationsData", ruis));
		   } else if (function.equals("getSources")) {
		      // Return an array of valid source names
              Source[] sources = data_source.getMRDSources();
		      String[] source_abbs = new String[sources.length];
		      for (int i = 0; i < sources.length; i++) {
		        source_abbs[i] = sources[i].getSourceAbbreviation();
		      }
		      request.addReturnValue(new Parameter.Default("sources", source_abbs));
			}
		   else
		   {
			   MEMEToolkit.logComment("$$$$$$$$$$$$$$$$$$$$$$$$$$NOW IN THE THE ELSE BLOCK");
		   }

		   
	   } catch (Exception ex) {
	//	 Something went wrong
	       ServerToolkit.handleError(ex);
	   }
	   finally {
		   ServerToolkit.returnDataSource(data_source);
	   }
   }
}