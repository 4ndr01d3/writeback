package uk.ac.ebi.mydas.writeback.datasource.model;

public class Method {
	private Long id;
	private String methodId;
	private String cvId;
	private String label;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getMethodId() {
		return methodId;
	}
	public void setMethodId(String methodId) {
		this.methodId = methodId;
	}
	public String getCvId() {
		return cvId;
	}
	public void setCvId(String cvId) {
		this.cvId = cvId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
