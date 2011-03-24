package uk.ac.ebi.mydas.writeback.datasource.model;

public class Type {
	private Long id;
	private String typeId; 
	private String category;
	private String cvId;
	private boolean reference;
	private boolean subparts;
	private boolean superparts;
	private String label;
	
	public Type() {}
	
	public String getTypeId() {
		return typeId;
	}
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getCvId() {
		return cvId;
	}
	public void setCvId(String cvId) {
		this.cvId = cvId;
	}
	public boolean isReference() {
		return reference;
	}
	public void setReference(boolean reference) {
		this.reference = reference;
	}
	public boolean isSubparts() {
		return subparts;
	}
	public void setSubparts(boolean subparts) {
		this.subparts = subparts;
	}
	public boolean isSuperparts() {
		return superparts;
	}
	public void setSuperparts(boolean superparts) {
		this.superparts = superparts;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
