/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.web
 * Object:  ViewSourceMetadataReportAction
 *
 * Changes
 *
 * 06/09/2006 TTN (1-BFPDH) : add host and port parameters to be able to run the report against different server
 *
 *****************************************************************************/
package gov.nih.nlm.meme.web;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.Action;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.client.ReportsClient;
import gov.nih.nlm.meme.common.SourceMetadataReport;
import gov.nih.nlm.meme.beans.SourceMetadataReportBean;
import gov.nih.nlm.meme.common.SourceDifference;
import gov.nih.nlm.meme.common.SourceDifference.Value;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;

public class ViewSourceMetadataReportAction extends Action {
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) throws
            MEMEException {
        try {
            BaseForm baseForm = (BaseForm)actionForm;
            SourceMetadataReportBean bean = new SourceMetadataReportBean();
            if(baseForm.getHost() != null) {
                bean.setHost(baseForm.getHost());
            }
            if(baseForm.getPort() != null) {
                bean.setPort(baseForm.getPort());
            }
            bean.setMidService("");
            ReportsClient client = bean.getReportsClient();
            SourceMetadataReport report = client.getSourceMetadataReport();
            servletRequest.setAttribute("report", report);

            SourceDifference[] diff = report.getAttributeNameDifferences();
            ArrayList sourceDifferenceList = new ArrayList(diff.length);
            for (int i = 0; i < diff.length; i++) {
                Value[] values = diff[i].getNewValues();
                Value[] oldValues = diff[i].getOldValues();
                ArrayList oldList = new ArrayList(values.length);
                ArrayList newList = new ArrayList(oldValues.length);
                for (int j = 0; j < values.length; j++) {
                    if(Arrays.binarySearch(oldValues, values[j],
                                            new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1 == null) {
                                return -1;
                            }
                            if (o2 == null) {
                                return 1;
                            }
                            Value v1 = (Value) o1;
                            Value v2 = (Value) o2;
                            if (v1.getValue() == v2.getValue()) {
                                return 0;
                            }
                            if (v1.getValue() == null) {
                                return -1;
                            }
                            if (v2.getValue() == null) {
                                return 1;
                            }
                            return v1.getValue().compareTo(v2.getValue());
                        }
                    })
                         < 0)
                     {
                        newList.add(values[j]);

                    }

                }

                values = diff[i].getOldValues();
                Value[] newValues = diff[i].getNewValues();
                for (int j = 0; j < values.length; j++) {
                    if (Arrays.binarySearch(newValues, values[j],
                                            new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1 == null) {
                                return -1;
                            }
                            if (o2 == null) {
                                return 1;
                            }
                            Value v1 = (Value) o1;
                            Value v2 = (Value) o2;
                            if (v1.getValue() == v2.getValue()) {
                                return 0;
                            }
                            if (v1.getValue() == null) {
                                return -1;
                            }
                            if (v2.getValue() == null) {
                                return 1;
                            }
                            return v1.getValue().compareTo(v2.getValue());
                        }
                    })
                         < 0)
                            {
                        oldList.add(values[j]);

                    }

                }
                SourceDifference sourceDifference = diff[i];
                sourceDifference.setNewValues((Value[])newList.toArray(new Value[0]));
                sourceDifference.setOldValues((Value[])oldList.toArray(new Value[0]));
                sourceDifferenceList.add(sourceDifference);
            }
            servletRequest.setAttribute("attributeNameDifferences",
                                        (SourceDifference[])sourceDifferenceList.toArray(
                                                new SourceDifference[0]));

            diff = report.getRelationshipAttributeDifferences();
            sourceDifferenceList = new ArrayList(diff.length);
            for (int i = 0; i < diff.length; i++) {
                Value[] values = diff[i].getNewValues();
                Value[] oldValues = diff[i].getOldValues();
                ArrayList oldList = new ArrayList(values.length);
                ArrayList newList = new ArrayList(oldValues.length);
                for (int j = 0; j < values.length; j++) {
                    if (Arrays.binarySearch(oldValues, values[j],
                                            new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1 == null) {
                                return -1;
                            }
                            if (o2 == null) {
                                return 1;
                            }
                            Value v1 = (Value) o1;
                            Value v2 = (Value) o2;
                            if (v1.getValue() == v2.getValue()) {
                                return 0;
                            }
                            if (v1.getValue() == null) {
                                return -1;
                            }
                            if (v2.getValue() == null) {
                                return 1;
                            }
                            return v1.getValue().compareTo(v2.getValue());
                        }
                    })
                         < 0)
                            {
                        newList.add(values[j]);

                    }

                }
                values = diff[i].getOldValues();
                Value[] newValues = diff[i].getNewValues();
                for (int j = 0; j < values.length; j++) {
                    if (Arrays.binarySearch(newValues, values[j],
                                            new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1 == null) {
                                return -1;
                            }
                            if (o2 == null) {
                                return 1;
                            }
                            Value v1 = (Value) o1;
                            Value v2 = (Value) o2;
                            if (v1.getValue() == v2.getValue()) {
                                return 0;
                            }
                            if (v1.getValue() == null) {
                                return -1;
                            }
                            if (v2.getValue() == null) {
                                return 1;
                            }
                            return v1.getValue().compareTo(v2.getValue());
                        }
                    })
                         < 0)
                            {
                        oldList.add(values[j]);

                    }

                }
                SourceDifference sourceDifference = diff[i];
                sourceDifference.setNewValues((Value[])newList.toArray(new Value[0]));
                sourceDifference.setOldValues((Value[])oldList.toArray(new Value[0]));
                sourceDifferenceList.add(sourceDifference);
            }
            servletRequest.setAttribute("relationshipAttributeDifferences",
                                        (SourceDifference[])sourceDifferenceList.toArray(
                                                new SourceDifference[0]));
            diff = report.getTermgroupDifferences();
            sourceDifferenceList = new ArrayList(diff.length);
            for (int i = 0; i < diff.length; i++) {
                Value[] values = diff[i].getNewValues();
                Value[] oldValues = diff[i].getOldValues();
                ArrayList oldList = new ArrayList(values.length);
                ArrayList newList = new ArrayList(oldValues.length);
                for (int j = 0; j < values.length; j++) {
                    if (Arrays.binarySearch(oldValues, values[j],
                                            new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1 == null) {
                                return -1;
                            }
                            if (o2 == null) {
                                return 1;
                            }
                            Value v1 = (Value) o1;
                            Value v2 = (Value) o2;
                            if (v1.getValue() == v2.getValue()) {
                                return 0;
                            }
                            if (v1.getValue() == null) {
                                return -1;
                            }
                            if (v2.getValue() == null) {
                                return 1;
                            }
                            return v1.getValue().compareTo(v2.getValue());
                        }
                    })
                         < 0)
                            {
                        newList.add(values[j]);

                    }

                }
                values = diff[i].getOldValues();
                Value[] newValues = diff[i].getNewValues();
                for (int j = 0; j < values.length; j++) {
                    if (Arrays.binarySearch(newValues, values[j],
                                            new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1 == null) {
                                return -1;
                            }
                            if (o2 == null) {
                                return 1;
                            }
                            Value v1 = (Value) o1;
                            Value v2 = (Value) o2;
                            if (v1.getValue() == v2.getValue()) {
                                return 0;
                            }
                            if (v1.getValue() == null) {
                                return -1;
                            }
                            if (v2.getValue() == null) {
                                return 1;
                            }
                            return v1.getValue().compareTo(v2.getValue());
                        }
                    })
                         < 0)
                            {
                        oldList.add(values[j]);

                    }

                }
                SourceDifference sourceDifference = diff[i];
                sourceDifference.setNewValues((Value[])newList.toArray(new Value[0]));
                sourceDifference.setOldValues((Value[])oldList.toArray(new Value[0]));
                sourceDifferenceList.add(sourceDifference);
            }
            servletRequest.setAttribute("termgroupDifferences",
                                        (SourceDifference[])sourceDifferenceList.toArray(
                                                new SourceDifference[0]));
            diff = report.getSuppressibleDifferences();
            sourceDifferenceList = new ArrayList(diff.length);
            for (int i = 0; i < diff.length; i++) {
                Value[] values = diff[i].getNewValues();
                Value[] oldValues = diff[i].getOldValues();
                ArrayList oldList = new ArrayList(values.length);
                ArrayList newList = new ArrayList(oldValues.length);
                for (int j = 0; j < values.length; j++) {
                    if (Arrays.binarySearch(oldValues, values[j],
                                            new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1 == null) {
                                return -1;
                            }
                            if (o2 == null) {
                                return 1;
                            }
                            Value v1 = (Value) o1;
                            Value v2 = (Value) o2;
                            if (v1.getValue() == v2.getValue()) {
                                return 0;
                            }
                            if (v1.getValue() == null) {
                                return -1;
                            }
                            if (v2.getValue() == null) {
                                return 1;
                            }
                            return v1.getValue().compareTo(v2.getValue());
                        }
                    })
                         < 0)
                            {
                        newList.add(values[j]);

                    }

                }
                values = diff[i].getOldValues();
                Value[] newValues = diff[i].getNewValues();
                for (int j = 0; j < values.length; j++) {
                    if (Arrays.binarySearch(newValues, values[j],
                                            new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1 == null) {
                                return -1;
                            }
                            if (o2 == null) {
                                return 1;
                            }
                            Value v1 = (Value) o1;
                            Value v2 = (Value) o2;
                            if (v1.getValue() == v2.getValue()) {
                                return 0;
                            }
                            if (v1.getValue() == null) {
                                return -1;
                            }
                            if (v2.getValue() == null) {
                                return 1;
                            }
                            return v1.getValue().compareTo(v2.getValue());
                        }
                    })
                         < 0)
                            {
                        oldList.add(values[j]);

                    }

                }
                SourceDifference sourceDifference = diff[i];
                sourceDifference.setNewValues((Value[])newList.toArray(new Value[0]));
                sourceDifference.setOldValues((Value[])oldList.toArray(new Value[0]));
                sourceDifferenceList.add(sourceDifference);
            }
            servletRequest.setAttribute("suppressibleDifferences",
                                        (SourceDifference[])sourceDifferenceList.toArray(
                                                new SourceDifference[0]));
        } catch (MEMEException e) {
            servletRequest.setAttribute("MEMEException", e);
            throw e;
        } catch (Exception e) {
            MEMEException me = new MEMEException("Non MEMEException", e);
            me.setPrintStackTrace(true);
            servletRequest.setAttribute("MEMEException", me);
            throw me;
        }
        return actionMapping.findForward("success");
    }
}
