package uk.ac.ebi.mydas.writeback.datasource.hibernate;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.writeback.datasource.model.Feature;
import uk.ac.ebi.mydas.writeback.datasource.model.Method;
import uk.ac.ebi.mydas.writeback.datasource.model.Orientation;
import uk.ac.ebi.mydas.writeback.datasource.model.Phase;
import uk.ac.ebi.mydas.writeback.datasource.model.Segment;
import uk.ac.ebi.mydas.writeback.datasource.model.Target;
import uk.ac.ebi.mydas.writeback.datasource.model.Type;
import uk.ac.ebi.mydas.writeback.datasource.model.Users;

public class MyDas2Hibernate {
	private HashMap<String,String> wbnotes= null;
	private Set<String> otherNotes=null;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	
	public Segment map(DasAnnotatedSegment segment){
		Segment response= new Segment();
		response.setIdSegment(segment.getSegmentId());
		response.setLabel(segment.getSegmentLabel());
		response.setStart(segment.getStartCoordinate());
		response.setStop(segment.getStopCoordinate());
		response.setVersion(segment.getVersion());
		response.setFeatures(map(segment.getFeatures(),response));
		return response;
	}

	private Set<Feature> map(Collection<DasFeature> features, Segment segment) {
		Set<Feature> response=new HashSet<Feature>();
		for (DasFeature feature: features){
			this.wbnotes=null;
			this.otherNotes=null;
			response.add(map(feature,segment));
		}
		return response;
	}

	private Feature map(DasFeature feature, Segment segment) {
		Feature response =null;
		response = new Feature();
		response.setDatecreated(getDateCreated(feature.getNotes()));
		response.setDeleted(feature.getFeatureLabel().equals("DELETE"));
		response.setFeatureId(feature.getFeatureId());
		response.setLabel(feature.getFeatureLabel());
		response.setLinks(mapLinks(feature.getLinks()));
		response.setMethod(map(feature.getMethod()));
		response.setNotes(mapNotes(feature.getNotes()));
		response.setOrientation(map(feature.getOrientation()));
		response.setParents(mapString(feature.getParents()));
		response.setParts(mapString(feature.getParts()));
		response.setPhase(map(feature.getPhase()));
		response.setScore(feature.getScore());
		response.setSegment(segment);
		response.setStart(feature.getStartCoordinate());
		response.setStop(feature.getStopCoordinate());
		response.setTargets(mapTarget(feature.getTargets()));
		response.setType(map(feature.getType(), feature));
		response.setUsers(mapUser(feature.getNotes()));
		response.setVersion(mapVersion(feature.getNotes()));
		response.setHref(mapHref(feature.getNotes()));
		return response;
	}
	private Map<String, String> mapLinks(Map<URL, String> links) {
		Map<String, String> newlinks=new HashMap<String, String>();
		for (URL key:links.keySet()){
			newlinks.put(key.toString(), links.get(key));
		}
		return newlinks;
	}

	private String mapHref(Collection<String> notes) {
		loadNotes(notes);
		return this.wbnotes.get("HREF");
	}

	private Integer mapVersion(Collection<String> notes) {
		loadNotes(notes);
		if (this.wbnotes.get("VERSION")==null) return null;
		return Integer.parseInt(this.wbnotes.get("VERSION"));
	}

	private Users mapUser(Collection<String> notes) {
		loadNotes(notes);
		Users user=new Users();
		user.setLogin(this.wbnotes.get("USER"));
		user.setPassword(this.wbnotes.get("PASSWORD"));
		return user;
	}

	private Type map(DasType type,DasFeature feature) {
		Type response=new Type();
		response.setCategory(type.getCategory());
		response.setCvId(type.getCvId());
		response.setLabel(type.getLabel());
		response.setTypeId(type.getId());
		response.setSubparts(feature.getParts()!=null && feature.getParts().size()>0);
		response.setSuperparts(feature.getParents()!=null && feature.getParents().size()>0);
		response.setReference(response.isSubparts() || response.isSuperparts());
		return response;
	}

	private Set<Target> mapTarget(Collection<DasTarget> targets) {
		Set<Target> response= new HashSet<Target>();
		for (DasTarget target:targets)
			response.add(map(target));
		return response;
	}

	private Target map(DasTarget target) {
		Target response= new Target();
		response.setLabel(target.getTargetName());
		response.setStart(target.getStartCoordinate());
		response.setStop(target.getStopCoordinate());
		response.setTargetId(target.getTargetId());
		return response;
	}

	private Phase map(DasPhase phase) {
		if (phase==DasPhase.PHASE_NOT_APPLICABLE)
			return Phase.PHASE_NOT_APPLICABLE;
		if (phase==DasPhase.PHASE_READING_FRAME_0)
			return Phase.PHASE_READING_FRAME_0;
		if (phase==DasPhase.PHASE_READING_FRAME_1)
			return Phase.PHASE_READING_FRAME_1;
		if (phase==DasPhase.PHASE_READING_FRAME_2)
			return Phase.PHASE_READING_FRAME_2;
		return null;
	}

	private Set<String> mapString(Collection<String> strings) {
		Set<String> ss= new HashSet<String>();
		for (String s:strings)
			ss.add(s);
		return ss;
	}

	private Orientation map(DasFeatureOrientation orientation) {
		if (orientation==DasFeatureOrientation.ORIENTATION_ANTISENSE_STRAND)
			return Orientation.ORIENTATION_ANTISENSE_STRAND;
		if (orientation==DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE)
			return Orientation.ORIENTATION_NOT_APPLICABLE;
		if (orientation==DasFeatureOrientation.ORIENTATION_SENSE_STRAND)
			return Orientation.ORIENTATION_SENSE_STRAND;
		return null;
	}

	private Set<String> mapNotes(Collection<String> notes) {
		loadNotes(notes);
		return this.otherNotes;
	}

	private Method map(DasMethod method) {
		Method response=new Method();
		response.setCvId(method.getCvId());
		response.setLabel(method.getLabel());
		response.setMethodId(method.getId());
		return response;	}

	private Date getDateCreated(Collection<String> notes) {
		loadNotes(notes);
		if (null==this.wbnotes.get("DATE"))
			return null;
		try {
			return formatter.parse(this.wbnotes.get("DATE"));
		} catch (ParseException e) {
			return null;
		}
	}

	private void loadNotes(Collection<String> notes){
		if (this.wbnotes!=null)
			return;
		this.wbnotes= new HashMap<String,String>();
		this.otherNotes= new HashSet<String>();
		for(String note:notes){
			String[] parts=note.split("=");
			if (parts.length==2 && parts[0].toUpperCase().equals(parts[0]))
				this.wbnotes.put(parts[0],parts[1]);
			else
				this.otherNotes.add(note);
		}
	}
}
