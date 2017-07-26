/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ContentViewService
 *
 * 12/06/2007 TTN (1-FXI1H): change "Sub Category" and "Previous Meta" property of Content View to be optional. 
 * 10/30/2007 TTN (1-FN4GD): add paginated feature to getContentViewMembers method 
 *  10/10/2007 BAC (1-FH5HH): fix formatter problem
 *  06/09/2006 TTN (1-BFPCX): add CVF concept for content view
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.action.ContentViewAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ContentView;
import gov.nih.nlm.meme.common.ContentViewMember;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.sql.Ticket;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles requests related to {@link ContentView}s.
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant to take a SessionContext argument 
 * 
 * @author MEME Group
 */
public class ContentViewService implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives requests from the {@link MEMEApplicationServer}
   * Handles the request based on the "function" parameter.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    MEMEServiceRequest request = context.getServiceRequest();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();
    String function = (String) request.getParameter("function").getValue();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

     Ticket ticket = Ticket.getReportsTicket();


    //
    // ContentView
    //
     Source source = data_source.getSource("MTH");

    if (function.equals("add_content_view")) {
      if (request.getParameter("content_view") != null) {
        ContentView cv = (ContentView) request.getParameter("content_view").
            getValue();

        Concept concept = new Concept.Default();

        // Create an atom
        Atom atom = new Atom.Default();
        atom.setString(cv.getName());
        atom.setTermgroup(data_source.getTermgroup("MTH/CV"));
        atom.setSource(source);
        atom.setCode(Code.newCode("NOCODE"));
        atom.setStatus('R');
        atom.setGenerated(true);
        atom.setReleased('N');
        atom.setTobereleased('Y');
        atom.setSuppressible("N");
        atom.setConcept(concept);

        concept.addAtom(atom);

        Authority authority = new Authority.Default("E-CVF");

        MolecularAction ma = new MolecularInsertConceptAction(concept);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);

        Attribute attr = new Attribute.Default();
        attr.setAtom(atom);
        attr.setLevel('C');
        attr.setName("CV_CONTRIBUTOR");
        attr.setValue(cv.getContributor());
        attr.setSource(source);
        attr.setStatus('R');
        attr.setGenerated(true);
        attr.setReleased('A');
        attr.setTobereleased('Y');
        attr.setSuppressible("N");
        attr.setConcept(concept);
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);

        attr.setName("CV_CONTRIBUTOR_VERSION");
        attr.setValue(cv.getContributorVersion());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_CONTRIBUTOR_DATE");
        attr.setValue(formatter.format(cv.getContributorDate()));
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_CONTRIBUTOR_URL");
        attr.setValue(cv.getContributorURL());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_MAINTAINER");
        attr.setValue(cv.getMaintainer());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_MAINTAINER_VERSION");
        attr.setValue(cv.getMaintainerVersion());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_MAINTAINER_DATE");
        attr.setValue(formatter.format(cv.getMaintainerDate()));
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_MAINTAINER_URL");
        attr.setValue(cv.getMaintainerURL());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_DESCRIPTION");
        attr.setValue(cv.getDescription());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_ALGORITHM");
        attr.setValue(cv.getAlgorithm());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_CATEGORY");
        attr.setValue(cv.getCategory());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_CLASS");
        attr.setValue(cv.getContentViewClass());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        if(cv.getSubCategory() != null) {
        attr.setName("CV_SUBCATEGORY");
        attr.setValue(cv.getSubCategory());
        ma = new MolecularInsertAttributeAction(attr);
        }
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_CODE");
        attr.setValue(String.valueOf(cv.getCode()));
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        attr.setName("CV_IS_GENERATED");
        attr.setValue((cv.isGeneratedByQuery()?"Y":"N"));
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        if(cv.getPreviousMeta() != null) {
        attr.setName("CV_PREVIOUS_META");
        attr.setValue(cv.getPreviousMeta());
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
        }
        attr.setName("SEMANTIC_TYPE");
        attr.setValue("Intellectual Product");
        ma = new MolecularInsertAttributeAction(attr);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);

        cv.setIdentifier(concept.getIdentifier());
        ContentViewAction cva = ContentViewAction.newAddContentViewAction(cv);
        data_source.getActionEngine().processAction(cva);
        request.addReturnValue(new Parameter.Default("id", cv.getIdentifier()));
      }
    } else if (function.equals("remove_content_view")) {
      if (request.getParameter("content_view") != null) {
        ContentView cv = (ContentView) request.getParameter("content_view").
            getValue();
        ContentViewAction cva = ContentViewAction.newRemoveContentViewAction(cv);
        data_source.getActionEngine().processAction(cva);
        Concept concept = data_source.getConcept(cv.getIdentifier().intValue(),ticket);
        Authority authority = new Authority.Default("E-CVF");
        MolecularAction ma = new MolecularDeleteConceptAction(concept);
        ma.setAuthority(authority);
        data_source.getActionEngine().processAction(ma);
      }
    } else if (function.equals("set_content_view")) {
      if (request.getParameter("content_view") != null) {
        ContentView cv = (ContentView) request.getParameter("content_view").
            getValue();
        Concept concept = data_source.getConcept(cv.getIdentifier().intValue(),ticket);
        Atom atom = concept.getPreferredAtom();
        Authority authority = new Authority.Default("E-CVF");
        Attribute[] attributes = concept.getAttributesByName("CV_CONTRIBUTOR");
        if(!attributes[0].getValue().equals(cv.getContributor())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getContributor());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_CONTRIBUTOR_VERSION");
        if(!attributes[0].getValue().equals(cv.getContributorVersion())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getContributorVersion());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_CONTRIBUTOR_DATE");
        if(!attributes[0].getValue().equals(formatter.format(cv.getContributorDate()))) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(formatter.format(cv.getContributorDate()));
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_CONTRIBUTOR_URL");
        if(!attributes[0].getValue().equals(cv.getContributorURL())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getContributorURL());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_MAINTAINER");
        if(!attributes[0].getValue().equals(cv.getMaintainer())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getMaintainer());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_MAINTAINER_VERSION");
        if(!attributes[0].getValue().equals(cv.getMaintainerVersion())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getMaintainerVersion());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_MAINTAINER_DATE");
        if(!attributes[0].getValue().equals(formatter.format(cv.getMaintainerDate()))) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(formatter.format(cv.getMaintainerDate()));
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_MAINTAINER_URL");
        if(!attributes[0].getValue().equals(cv.getMaintainerURL())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getMaintainerURL());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_DESCRIPTION");
        if(!attributes[0].getValue().equals(cv.getDescription())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getDescription());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_ALGORITHM");
        if(!attributes[0].getValue().equals(cv.getAlgorithm())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getAlgorithm());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_CATEGORY");
        if(!attributes[0].getValue().equals(cv.getCategory())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getCategory());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_CLASS");
        if(!attributes[0].getValue().equals(cv.getContentViewClass())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getContentViewClass().toString());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_SUBCATEGORY");
        if(attributes.length > 0 && cv.getSubCategory() == null) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        } else
        if(attributes.length == 0 && cv.getSubCategory() != null ) {
          Attribute attr = new Attribute.Default();
          attr.setAtom(atom);
          attr.setLevel('C');
          attr.setName("CV_SUBCATEGORY");
          attr.setValue(cv.getSubCategory());
          attr.setSource(source);
          attr.setStatus('R');
          attr.setGenerated(true);
          attr.setReleased('A');
          attr.setTobereleased('Y');
          attr.setSuppressible("N");
          attr.setConcept(concept);
        	MolecularAction ma = new MolecularInsertAttributeAction(attr);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        } else
        if(attributes.length > 0 && !attributes[0].getValue().equals(cv.getSubCategory())) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(cv.getSubCategory());
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_CODE");
        if(!attributes[0].getValue().equals(String.valueOf(cv.getCode()))) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(String.valueOf(cv.getCode()));
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_IS_GENERATED");
        if(!attributes[0].getValue().equals((cv.isGeneratedByQuery() ? "Y" : "N"))) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue((cv.isGeneratedByQuery() ? "Y" : "N"));
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        attributes = concept.getAttributesByName("CV_PREVIOUS_META");
        if(cv.getPreviousMeta() == null && attributes.length > 0) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
          ma.setAuthority(authority);
      	data_source.getActionEngine().processAction(ma);
        } else
        if(cv.getPreviousMeta() != null && attributes.length == 0) {
          Attribute attr = new Attribute.Default();
          attr.setAtom(atom);
          attr.setLevel('C');
          attr.setName("CV_PREVIOUS_META");
          attr.setValue(cv.getPreviousMeta());
          attr.setSource(source);
          attr.setStatus('R');
          attr.setGenerated(true);
          attr.setReleased('A');
          attr.setTobereleased('Y');
          attr.setSuppressible("N");
          attr.setConcept(concept);
        	MolecularAction ma = new MolecularInsertAttributeAction(attr);
          ma.setAuthority(authority);
      	data_source.getActionEngine().processAction(ma);
        } else
        if(attributes.length > 0 && !attributes[0].getValue().equals(String.valueOf(cv.getPreviousMeta()))) {
        	MolecularAction ma = new MolecularDeleteAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        	attributes[0].setValue(String.valueOf(cv.getPreviousMeta()));
        	ma = new MolecularInsertAttributeAction(attributes[0]);
            ma.setAuthority(authority);
        	data_source.getActionEngine().processAction(ma);
        }
        ContentViewAction cva = ContentViewAction.newSetContentViewAction(cv);
        data_source.getActionEngine().processAction(cva);
      }
    } else if (function.equals("get_content_view")) {
      if (request.getParameter("content_view_id") != null) {
        Identifier id =
            (Identifier) request.getParameter("content_view_id").getValue();
        ContentView cv = data_source.getContentView(id.intValue());
        request.addReturnValue(new Parameter.Default("get_content_view", cv));
      }
    } else if (function.equals("get_content_views")) {
      ContentView[] cvs = data_source.getContentViews();
      request.addReturnValue(new Parameter.Default("get_content_views", cvs));

      //
      // ContentViewMember
      //

    } else if (function.equals("add_content_view_member")) {
      if (request.getParameter("content_view_member") != null) {
        ContentViewMember member =
            (ContentViewMember) request.getParameter("content_view_member").
            getValue();
        ContentViewAction cva = ContentViewAction.newAddContentViewMemberAction(
            member);
        data_source.getActionEngine().processAction(cva);
        request.addReturnValue(new Parameter.Default("id", member.getIdentifier()));
      }
    } else if (function.equals("remove_content_view_member")) {
      if (request.getParameter("content_view_member") != null) {
        ContentViewMember member =
            (ContentViewMember) request.getParameter("content_view_member").
            getValue();
        ContentViewAction cva = ContentViewAction.
            newRemoveContentViewMemberAction(member);
        data_source.getActionEngine().processAction(cva);
      }

    } else if (function.equals("add_content_view_members")) {
      if (request.getParameter("content_view_members") != null) {
        ContentViewMember[] members =
            (ContentViewMember[]) request.getParameter("content_view_members").
            getValue();
        ContentViewAction cva = ContentViewAction.
            newAddContentViewMembersAction(members);
        data_source.getActionEngine().processAction(cva);
      }
    } else if (function.equals("remove_content_view_members")) {
      if (request.getParameter("content_view") != null) {
        ContentView cv = (ContentView) request.getParameter("content_view").
            getValue();
        ContentViewAction cva = ContentViewAction.
            newRemoveContentViewMembersAction(cv);
        data_source.getActionEngine().processAction(cva);
      } else if (request.getParameter("content_view_members") != null) {
        ContentViewMember[] members =
            (ContentViewMember[]) request.getParameter("content_view_members").
            getValue();
        ContentViewAction cva = ContentViewAction.
            newRemoveContentViewMembersAction(members);
        data_source.getActionEngine().processAction(cva);
      }
    } else if (function.equals("generate_content_view_members")) {
      if (request.getParameter("content_view") != null) {
        ContentView cv =
            (ContentView) request.getParameter("content_view").getValue();
        ContentViewAction cva = ContentViewAction.
            newGenerateContentViewMembersAction(cv);
        data_source.getActionEngine().processAction(cva);
      }
    } else if (function.equals("get_content_view_members")) {
      if (request.getParameter("content_view") != null) {
      	int start = request.getParameter("start").getInt();
    	int end = request.getParameter("end").getInt();
        ContentView cv =
            (ContentView) request.getParameter("content_view").getValue();
        ContentViewMember[] members = data_source.getContentViewMembers(cv, start, end);
        request.addReturnValue(
            new Parameter.Default("get_content_view_members", members));
      }
    }

  }

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
   * @param context the {@link SessionContext}
   * @return <code>false</code>
   */
  public boolean isReEntrant(SessionContext context) {
    return false;
  }
}
