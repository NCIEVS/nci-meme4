/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ContentViewService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.action.ContentViewAction;
import gov.nih.nlm.meme.common.ContentView;
import gov.nih.nlm.meme.common.ContentViewMember;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * Handles requests related to {@link ContentView}s.
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

    //
    // ContentView
    //

    if (function.equals("add_content_view")) {
      if (request.getParameter("content_view") != null) {
        ContentView cv = (ContentView) request.getParameter("content_view").
            getValue();
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
      }
    } else if (function.equals("set_content_view")) {
      if (request.getParameter("content_view") != null) {
        ContentView cv = (ContentView) request.getParameter("content_view").
            getValue();
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
        ContentView cv =
            (ContentView) request.getParameter("content_view").getValue();
        ContentViewMember[] members = data_source.getContentViewMembers(cv);
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
   * @return <code>false</code>
   */
  public boolean isReEntrant() {
    return false;
  }
}
