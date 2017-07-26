package gov.nih.nlm.meme.common;

	import gov.nih.nlm.meme.exception.BadValueException;
	import gov.nih.nlm.meme.exception.DataSourceException;
	import gov.nih.nlm.mrd.sql.MRDDataSource;

	import java.sql.ResultSet;
	import java.sql.SQLException;
	import java.util.Map;

public class MRDContextRelationship {
		private String rui;
		private String source_rui;
		private String rel_group;
		private Source source;
		private String aui;
		private String parent;
		private String aui_name;
		private String par_name;
		private String hier_code;
		private String rel_mode;
		private String relationship_attribute;
		private String hierarchy_String;
		
		private String level;
		
		
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
		
		public String getRui() {
			return rui;
		}
		public void setRui(String rui) {
			this.rui = rui;
		}
		
		public String getSource_rui() {
			return source_rui;
		}
		public void setSource_rui(String source_rui) {
			this.source_rui = source_rui;
		}
		
		public String getLevel() {
			return level;
		}
		public void setLevel(String level) {
			this.level = level;
		}
		
	
		public String getAui() {
			return aui;
		}
		public void setAui(String aui) {
			this.aui = aui;
		}
		public String getAui_name() {
			return aui_name;
		}
		public void setAui_name(String aui_name) {
			this.aui_name = aui_name;
		}
		public String getHier_code() {
			return hier_code;
		}
		public void setHier_code(String hier_code) {
			this.hier_code = hier_code;
		}
		public String getHierarchy_String() {
			return hierarchy_String;
		}
		public void setHierarchy_String(String hierarchy_String) {
			this.hierarchy_String = hierarchy_String;
		}
		public String getPar_name() {
			return par_name;
		}
		public void setPar_name(String par_name) {
			this.par_name = par_name;
		}
		public String getParent() {
			return parent;
		}
		public void setParent(String parent) {
			this.parent = parent;
		}
		public String getRel_mode() {
			return rel_mode;
		}
		public void setRel_mode(String rel_mode) {
			this.rel_mode = rel_mode;
		}
		public Source getSource() {
			return source;
		}
		
		public void setSource(Source source) {
			this.source = source;
		}
	}
