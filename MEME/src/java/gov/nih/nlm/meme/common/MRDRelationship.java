package gov.nih.nlm.meme.common;

import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.mrd.sql.MRDDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class MRDRelationship {
	private String rui;
	private String source_rui;
	private String rel_group;
	private String source_label;
	private String sType_1;
	private String sType_2;
	private String aui_1;
	private String aui_2;
	private String aui1_name;
	private String aui2_name;
	private String cui_1;
	private String cui1_name;
	private String cui_2;
	private String cui2_name;
	private String relationship_name;
	private String relationship_attribute;
	private String suppressible;
	private String rel_direction_flag;
	private boolean inverse_flag;
	private Source source;
	
	
	private String level;
	
	public String getAui_1() {
		return aui_1;
	}
	public void setAui_1(String aui_1) {
		this.aui_1 = aui_1;
	}
	public String getAui1_name() {
		return aui1_name;
	}
	public void setAui1_name(String aui1_name) {
		this.aui1_name = aui1_name;
	}
	public String getAui_2() {
		return aui_2;
	}
	public void setAui_2(String aui2_2) {
		this.aui_2 = aui2_2;
	}
	public String getAui2_name() {
		return aui2_name;
	}
	public void setAui2_name(String aui21_name) {
		this.aui2_name = aui21_name;
	}
	public String getCui_1() {
		return cui_1;
	}
	public void setCui_1(String cui1) {
		this.cui_1 = cui1;
	}
	public String getCui1_name() {
		return cui1_name;
	}
	public void setCui1_name(String cui1_name) {
		this.cui1_name = cui1_name;
	}
	public String getCui_2() {
		return cui_2;
	}
	public void setCui_2(String cui2) {
		this.cui_2 = cui2;
	}
	public String getCui2_name() {
		return cui2_name;
	}
	public void setCui2_name(String cui2_name) {
		this.cui2_name = cui2_name;
	}
	public boolean isInverse_flag() {
		return inverse_flag;
	}
	public void setInverse_flag(boolean inverse_flag) {
		this.inverse_flag = inverse_flag;
	}
	public String getRel_direction_flag() {
		return rel_direction_flag;
	}
	public void setRel_direction_flag(String rel_direction_flag) {
		this.rel_direction_flag = rel_direction_flag;
	}
	public String getRel_group() {
		return rel_group;
	}
	public void setRel_group(String rel_group) {
		this.rel_group = rel_group;
	}
	public String getRelationship_attribute() {
		return relationship_attribute;
	}
	public void setRelationship_attribute(String relationship_attribute) {
		this.relationship_attribute = relationship_attribute;
	}
	public String getRelationship_name() {
		return relationship_name;
	}
	public void setRelationship_name(String relationship_name) {
		this.relationship_name = relationship_name;
	}
	public String getRui() {
		return rui;
	}
	public void setRui(String rui) {
		this.rui = rui;
	}
	public String getSource_label() {
		return source_label;
	}
	public void setSource_label(String source_label) {
		this.source_label = source_label;
	}
	public String getSource_rui() {
		return source_rui;
	}
	public void setSource_rui(String source_rui) {
		this.source_rui = source_rui;
	}
	public String getSType_1() {
		return sType_1;
	}
	public void setSType_1(String type_1) {
		sType_1 = type_1;
	}
	public String getSType_2() {
		return sType_2;
	}
	public void setSType_2(String type_2) {
		sType_2 = type_2;
	}
	public String getSuppressible() {
		return suppressible;
	}
	public void setSuppressible(String suppressible) {
		this.suppressible = suppressible;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	
	
	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}
}
