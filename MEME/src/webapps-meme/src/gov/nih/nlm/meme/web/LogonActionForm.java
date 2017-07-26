package gov.nih.nlm.meme.web;

import org.apache.struts.validator.ValidatorActionForm;

public class LogonActionForm
    extends ValidatorActionForm {
  private String host;
  private String midService;
  private String password;
  private String dbaUsername;
  private String dbaPassword;
  private String confirmPassword;
  private String port;
  private String username;
  private String oldPassword;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setMidService(String midService) {
    this.midService = midService;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  public void setDbaUsername(String dbaUsername) {
    this.dbaUsername = dbaUsername;
  }

  public void setDbaPassword(String dbaPassword) {
    this.dbaPassword = dbaPassword;
  }

  public String getMidService() {
    return midService;
  }

  public String getPassword() {
    return password;
  }

  public String getPort() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public String getDbaUsername() {
    return dbaUsername;
  }

  public String getDbaPassword() {
    return dbaPassword;
  }

}
