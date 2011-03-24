package uk.ac.ebi.mydas.writeback.datasource.model;

import java.util.Set;

public class Segment {
	private Long id;
	private String idSegment;
	private Integer start;
	private Integer stop;
	private String version;
	private String label;
	private Set<Feature> features;
	
	public Segment(){}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIdSegment() {
		return idSegment;
	}

	public void setIdSegment(String idSegment) {
		this.idSegment = idSegment;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getStop() {
		return stop;
	}

	public void setStop(Integer stop) {
		this.stop = stop;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Set<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(Set<Feature> features) {
		this.features = features;
	}
	
	public void addFeature(Feature feature){
		this.features.add(feature);
		feature.setSegment(this);
	}
	
	public void removeFeature(Feature feature){
		this.features.remove(feature);
		feature.setSegment(null);
	}
	
}
