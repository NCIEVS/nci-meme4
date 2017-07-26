package gov.nih.nlm.meme.common;

public class MRDAttribute {
	private String level = null;
	private String aui = null;
	private String cui = null;
	private String aName = null;
	private String aValue = null;
	private String sgType = null;
	private String code = null;
	private String source_atui = null;
	private Source source = null;
	private String hashcode = null;
	private String atui = null;
	private String suppressible = null;
	private String cuiName = null;
	
	public String getSuppressible() {
		return suppressible;
	}
	public void setSuppressible(String suppressible) {
		this.suppressible = suppressible;
	}
	public String getAtui() {
		return atui;
	}
	public void setAtui(String atui) {
		this.atui = atui;
	}
	public String getAName() {
		return aName;
	}
	public void setAName(String name) {
		aName = name;
	}
	public String getAui() {
		return aui;
	}
	public void setAui(String aui) {
		this.aui = aui;
	}
	public String getAValue() {
		return aValue;
	}
	public void setAValue(String value) {
		aValue = value;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCui() {
		return cui;
	}
	public void setCui(String cui) {
		this.cui = cui;
	}
	public String getHashcode() {
		return hashcode;
	}
	public void setHashcode(String hashcode) {
		this.hashcode = hashcode;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getSgType() {
		return sgType;
	}
	public void setSgType(String sgType) {
		this.sgType = sgType;
	}
	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}
	public String getSource_atui() {
		return source_atui;
	}
	public void setSource_atui(String source_atui) {
		this.source_atui = source_atui;
	}
	
	public boolean isAtomLevel() {
		if (level.equals("S")){
			return true;
		} else 
			return false;
	}
	public String getCuiName() {
		return cuiName;
	}
	public void setCuiName(String cuiName) {
		this.cuiName = cuiName;
	}
	
	
}
