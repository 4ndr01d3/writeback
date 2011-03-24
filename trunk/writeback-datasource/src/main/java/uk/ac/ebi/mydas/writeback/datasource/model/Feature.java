package uk.ac.ebi.mydas.writeback.datasource.model;

import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class Feature {
	private Long id;
	private String featureId;
	private String label;
	private Type type;
	private Method method;
	private Integer start;
	private Integer stop;
	private Double score; 
	private Orientation orientation;
	private Phase phase;
	private Set<String> notes;
	private Map<String,String> links;
	private Set<Target> targets;
	private Set<String> parents;
	private Set<String> parts;
	private Integer version; 
	private Date datecreated;
	private Users users; 
	private String href;
	
	private Segment segment;
	
	private boolean deleted;
	
	public Feature(){}

	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public Date getDatecreated() {
		return datecreated;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public void setDatecreated(Date datecreated) {
		this.datecreated = datecreated;
	}

	public Users getUsers() {
		return users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFeatureId() {
		return featureId;
	}
	public void setFeatureId(String featureId) {
		this.featureId = featureId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
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
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public Orientation getOrientation() {
		return orientation;
	}
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}
	public Phase getPhase() {
		return phase;
	}
	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	public Set<String> getNotes() {
		return notes;
	}
	public void setNotes(Set<String> notes) {
		this.notes = notes;
	}
	public Map<String, String> getLinks() {
		return links;
	}
	public void setLinks(Map<String, String> links) {
		this.links = links;
	}
	public Set<Target> getTargets() {
		return targets;
	}
	public void setTargets(Set<Target> targets) {
		this.targets = targets;
	}
	public Set<String> getParents() {
		return parents;
	}
	public void setParents(Set<String> parents) {
		this.parents = parents;
	}
	public Set<String> getParts() {
		return parts;
	}
	public void setParts(Set<String> parts) {
		this.parts = parts;
	}
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

}
