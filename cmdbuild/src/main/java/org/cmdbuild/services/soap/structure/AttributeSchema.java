package org.cmdbuild.services.soap.structure;

import org.cmdbuild.services.soap.types.Metadata;

public class AttributeSchema {

	private int idClass;
	private String name;
	private String description;
	private String type;
	private int precision;
	private int length;
	private int scale;
	private int index;
	private int classorder;
	private boolean baseDSP;
	private boolean unique;
	private boolean notnull;
	private boolean inherited;
	private String fieldmode;
	private String defaultValue;
	private String lookupType;
	private String referencedClassName;
	private Integer referencedIdClass;
	private Integer idDomain;
	private String visibility;
	private Metadata[] metadata;
	
	public AttributeSchema(){}

	public int getIdClass() {
		return idClass;
	}

	public void setIdClass(int idClass) {
		this.idClass = idClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isBaseDSP() {
		return baseDSP;
	}

	public void setBaseDSP(boolean baseDSP) {
		this.baseDSP = baseDSP;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isNotnull() {
		return notnull;
	}

	public void setNotnull(boolean notnull) {
		this.notnull = notnull;
	}

	public boolean isInherited() {
		return inherited;
	}

	public void setInherited(boolean inherited) {
		this.inherited = inherited;
	}

	public String getFieldmode() {
		return fieldmode;
	}

	public void setFieldmode(String fieldmode) {
		this.fieldmode = fieldmode;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getLookupType() {
		return lookupType;
	}

	public void setLookupType(String lookupType) {
		this.lookupType = lookupType;
	}

	public String getReferencedClassName() {
		return referencedClassName;
	}

	public void setReferencedClassName(String referencedClassName) {
		this.referencedClassName = referencedClassName;
	}

	public Integer getReferencedIdClass() {
		return referencedIdClass;
	}

	public void setReferencedIdClass(Integer referencedIdClass) {
		this.referencedIdClass = referencedIdClass;
	}

	public Integer getIdDomain() {
		return idDomain;
	}

	public void setIdDomain(Integer idDomain) {
		this.idDomain = idDomain;
	}

	public int getClassorder() {
		return classorder;
	}

	public void setClassorder(int classorder) {
		this.classorder = classorder;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public Metadata[] getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata[] metadata) {
		this.metadata = metadata;
	}

}
