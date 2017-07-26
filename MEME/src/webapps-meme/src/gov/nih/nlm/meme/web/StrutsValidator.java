package gov.nih.nlm.meme.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.util.ValidatorUtils;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.validator.Resources;

public class StrutsValidator {
  public static boolean validateTwoFields(
      Object bean,
      ValidatorAction va,
      Field field,
      ActionMessages errors,
      HttpServletRequest request) {

    String value = ValidatorUtils.getValueAsString(
        bean,
        field.getProperty());
    String sProperty2 = field.getVarValue("secondProperty");
    String value2 = ValidatorUtils.getValueAsString(
        bean,
        sProperty2);

    if (!GenericValidator.isBlankOrNull(value)) {
      try {
        if (value.equals(value2)) {
          errors.add(field.getKey(),
                     Resources.getActionMessage(
                         request,
                         va,
                         field));
          return false;
        }
      }
      catch (Exception e) {
        e.printStackTrace(System.out);
        errors.add(field.getKey(),
                   Resources.getActionMessage(
                       request,
                       va,
                       field));
        return false;
      }
    }

    return true;
  }
  public static boolean validateIdentical(
      Object bean,
      ValidatorAction va,
      Field field,
      ActionMessages errors,
      HttpServletRequest request) {

    String value = ValidatorUtils.getValueAsString(
        bean,
        field.getProperty());
    String sProperty2 = field.getVarValue("secondProperty");
    String value2 = ValidatorUtils.getValueAsString(
        bean,
        sProperty2);

    if (!GenericValidator.isBlankOrNull(value)) {
      try {
        if (!value.equals(value2)) {
          errors.add(field.getKey(),
                     Resources.getActionMessage(
                         request,
                         va,
                         field));
          return false;
        }
      }
      catch (Exception e) {
        e.printStackTrace(System.out);
        errors.add(field.getKey(),
                   Resources.getActionMessage(
                       request,
                       va,
                       field));
        return false;
      }
    }

    return true;
  }
}
