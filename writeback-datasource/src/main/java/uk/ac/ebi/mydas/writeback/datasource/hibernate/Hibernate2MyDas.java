package uk.ac.ebi.mydas.writeback.datasource.hibernate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

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

public class Hibernate2MyDas {

	public DasAnnotatedSegment map(Segment segment) throws DataSourceException{
		DasAnnotatedSegment response=null;
		try {
			response = new DasAnnotatedSegment(segment.getIdSegment(), segment.getStart(), segment.getStop(), segment.getVersion(), segment.getLabel(), map(segment.getFeatures()));
		} catch (DataSourceException e) {
			throw new DataSourceException("Problem mapping the Segment",e);
		}
		return response;
	}

	private Collection<DasFeature> map(Set<Feature> features) throws DataSourceException {
		Collection<DasFeature> response=new ArrayList<DasFeature>();
		for(Feature feature:features){
			try {
				response.add(new DasFeature(	feature.getFeatureId(), 
												feature.getLabel(), 
												map(feature.getType()), 
												map(feature.getMethod()), 
												feature.getStart(), 
												feature.getStop(), 
												feature.getScore(),
												map(feature.getOrientation()),
												map(feature.getPhase()), 
												mapNotes(feature.getNotes(),feature), 
												mapLinks(feature.getLinks()), 
												mapTargets(feature.getTargets()), 
												mapStrings(feature.getParents()), 
												mapStrings(feature.getParts())));
			} catch (DataSourceException e) {
				throw new DataSourceException("Problem mapping the Feature",e);
			}
		}
		return response;
	}

	
	private Map<URL, String> mapLinks(Map<String, String> links) throws DataSourceException {
		Map<URL, String> newlinks=new HashMap<URL, String>();
		for (String key:links.keySet()){
			try {
				newlinks.put(new URL(key), links.get(key));
			} catch (MalformedURLException e) {
				throw new DataSourceException("The URL in the map was malformed.",e);
			}
		}
		return newlinks;
	}

	private Collection<String> mapNotes(Set<String> notes,Feature feature) {
		Collection<String> allNotes=mapStrings(notes);
		allNotes.add("USER="+feature.getUsers().getLogin());
		allNotes.add("DATE="+feature.getDatecreated());
		allNotes.add("VERSION="+feature.getVersion());
		return allNotes;
	}

	private Collection<DasTarget> mapTargets(Set<Target> targets) throws DataSourceException {
		Collection<DasTarget> response=new ArrayList<DasTarget>();
		if (targets!=null)
			for(Target target:targets)
				response.add(map(target));
		return response;
	}

	private DasTarget map(Target target) throws DataSourceException {
		DasTarget response=null;
		try {
			response=new DasTarget(target.getTargetId(), target.getStart(), target.getStop(), target.getLabel());
		} catch (DataSourceException e) {
			throw new DataSourceException("Problem mapping the Target",e);
		}
		return response;
	}

	private Collection<String> mapStrings(Set<String> strings) {
		Collection<String> response=new ArrayList<String>();
		if (strings!=null)
			for(String string:strings)
				response.add(string);
		
		return response;
	}

	private DasPhase map(Phase phase) {
		if (phase==Phase.PHASE_NOT_APPLICABLE)
			return DasPhase.PHASE_NOT_APPLICABLE;
		if (phase==Phase.PHASE_READING_FRAME_0)
			return DasPhase.PHASE_READING_FRAME_0;
		if (phase==Phase.PHASE_READING_FRAME_1)
			return DasPhase.PHASE_READING_FRAME_1;
		if (phase==Phase.PHASE_READING_FRAME_2)
			return DasPhase.PHASE_READING_FRAME_2;
		return null;
	}

	private DasFeatureOrientation map(Orientation orientation) {
		if (orientation==Orientation.ORIENTATION_NOT_APPLICABLE)
			return DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE;
		else if (orientation==Orientation.ORIENTATION_SENSE_STRAND)
			return DasFeatureOrientation.ORIENTATION_SENSE_STRAND;
		else if (orientation==Orientation.ORIENTATION_ANTISENSE_STRAND)
			return DasFeatureOrientation.ORIENTATION_ANTISENSE_STRAND;
		return null;
	}

	private DasMethod map(Method method) throws DataSourceException {
		DasMethod response=null;
		try {
			response= new DasMethod(method.getMethodId(), method.getLabel(),method.getCvId());
		} catch (DataSourceException e) {
			throw new DataSourceException("Problem mapping the Type",e);
		}
		return response;
	}

	public DasType map(Type type) {
		DasType response=null;
		response = new DasType(type.getTypeId(), type.getCategory(), type.getCvId(), type.getLabel());
		return response;
	}
}
