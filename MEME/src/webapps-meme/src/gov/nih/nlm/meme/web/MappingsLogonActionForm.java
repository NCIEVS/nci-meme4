package gov.nih.nlm.meme.web;

import org.apache.struts.validator.ValidatorActionForm;

public class MappingsLogonActionForm
extends ValidatorActionForm {
private String username;
private String password;
private boolean isAuthenticated = false;
public void setUsername(String username) {
this.username = username;
}

public void setPassword(String password) {
this.password = password;
}

public String getUsername() {
return username;
}

public String getPassword() {
return password;
}

public boolean isAuthenticated() {
	return isAuthenticated;
}

public void setAuthenticated(boolean isAuthenticated) {
	this.isAuthenticated = isAuthenticated;
}

}

