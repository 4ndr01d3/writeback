package uk.ac.ebi.mydas.writeback.datasource;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.controller.CacheManager;
import uk.ac.ebi.mydas.datasource.CommandExtender;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.datasource.WritebackDataSource;
import uk.ac.ebi.mydas.exceptions.BadCommandException;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.exceptions.WritebackException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.writeback.datasource.hibernate.Hibernate2MyDas;
import uk.ac.ebi.mydas.writeback.datasource.hibernate.HibernateManager;
import uk.ac.ebi.mydas.writeback.datasource.hibernate.MyDas2Hibernate;
import uk.ac.ebi.mydas.writeback.datasource.model.Feature;
import uk.ac.ebi.mydas.writeback.datasource.model.Segment;
import uk.ac.ebi.mydas.writeback.datasource.model.Type;
import uk.ac.ebi.mydas.writeback.datasource.model.Users;

public class Writeback implements WritebackDataSource, RangeHandlingAnnotationDataSource, CommandExtender {
	
	HibernateManager hibernate= null;
    CacheManager cacheManager = null;
    ServletContext svCon;
    Map<String, PropertyType> globalParameters;
    DataSourceConfiguration config;

	@Override
	public DasAnnotatedSegment create(DasAnnotatedSegment segment)
			throws DataSourceException {
		MyDas2Hibernate m2h= new MyDas2Hibernate();
		Hibernate2MyDas h2m= new Hibernate2MyDas();
		Segment segmentToAdd = m2h.map(segment);
		Segment segmentAdded;
		try {
			segmentAdded = hibernate.addFeaturesFromSegment(segmentToAdd, true, true);
		} catch (WritebackException e) {
			throw new DataSourceException("Some features couldn't be added",e);
		}
		return h2m.map(segmentAdded);
	}

	@Override
	public DasAnnotatedSegment delete(String segmentId, String featureId,
			Map<String, String> extraParameters) throws DataSourceException {
		Hibernate2MyDas h2m= new Hibernate2MyDas();
		Segment segmentAdded;
		Feature feature=new Feature();
		feature.setFeatureId(featureId);

		Users user = new Users();
		user.setLogin(extraParameters.get("user"));
		user.setPassword(extraParameters.get("password"));
		feature.setUsers(user);
		if (extraParameters.get("href")!=null)
			feature.setHref(extraParameters.get("href"));
		Segment segment=new Segment();
		Set<Feature> features= new HashSet<Feature>();
		features.add(feature);
		segment.setFeatures(features);
		segment.setIdSegment(segmentId);

		try {
			segmentAdded = hibernate.deleteFeaturesFromSegment(segment, true);
		} catch (WritebackException e) {
			throw new DataSourceException("Some features couldn't be deleted",e);
		}
		return h2m.map(segmentAdded);

	}

	@Override
	public DasAnnotatedSegment history(String featureId)
			throws DataSourceException {
		Hibernate2MyDas h2m= new Hibernate2MyDas();

		Segment resultSegment = hibernate.getFeatureHistoryFromId(featureId);

		return h2m.map(resultSegment);
	}

	@Override
	public DasAnnotatedSegment update(DasAnnotatedSegment segment)
			throws DataSourceException {
		MyDas2Hibernate m2h= new MyDas2Hibernate();
		Hibernate2MyDas h2m= new Hibernate2MyDas();
		Segment segmentToAdd = m2h.map(segment);
		Segment segmentAdded;
		try {
			segmentAdded = hibernate.updateFeaturesFromSegment(segmentToAdd, true, true);
		} catch (WritebackException e) {
			throw new DataSourceException("Some features couldn't be updated",e);
		}
		return h2m.map(segmentAdded);
	}

	@Override
	public DasAnnotatedSegment getFeatures(String segmentId, int start,
			int stop, Integer maxbins) throws BadReferenceObjectException,
			CoordinateErrorException, DataSourceException {
		Hibernate2MyDas h2m= new Hibernate2MyDas();
		Segment resultSegment = hibernate.getSegmentFromIdAndRange(segmentId,start,stop);
		if(resultSegment==null)
			 throw new BadReferenceObjectException(segmentId, "Not found");
		return h2m.map(resultSegment);
	}

	@Override
	public void destroy() {}

	@Override
	public String getEntryPointVersion() throws UnimplementedFeatureException,
			DataSourceException {
        this.cacheManager.emptyCache();
        return "Version 1.1";
	}

	@Override
	public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop)
			throws UnimplementedFeatureException, DataSourceException {
        List<DasEntryPoint> entryPoints = new ArrayList<DasEntryPoint>();
        List<Segment> segments = hibernate.getSegments(start, stop);
        for (Segment segment:segments)
        	entryPoints.add (new DasEntryPoint(segment.getIdSegment(), segment.getStart(), segment.getStop(), "Protein Sequence",segment.getVersion(), null, "UniProt,Protein Sequence", false));
        return entryPoints;
	}

	@Override
	public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException {
		Hibernate2MyDas h2m= new Hibernate2MyDas();
		Segment resultSegment = hibernate.getSegmentFromId(segmentId);
		if(resultSegment==null)
			 throw new BadReferenceObjectException(segmentId, "Not found");
		return h2m.map(resultSegment);
	}

	@Override
	public Collection<DasAnnotatedSegment> getFeatures(
			Collection<String> featureIdCollection, Integer maxbins)
			throws UnimplementedFeatureException, DataSourceException {
		Hibernate2MyDas h2m= new Hibernate2MyDas();
		Collection<DasAnnotatedSegment> segments=new ArrayList<DasAnnotatedSegment>();
		for(String featureId:featureIdCollection){
			Segment seg = hibernate.getSegmentFromFeatureId(featureId);
			if (seg!=null)
				segments.add(h2m.map(seg));
			else
				segments.add(new DasUnknownFeatureSegment(featureId));
		}
		return segments;
	}

	@Override
	public URL getLinkURL(String field, String id)
			throws UnimplementedFeatureException, DataSourceException {
		throw new UnimplementedFeatureException("Link has been deprecateeedd in the specification");
	}

	@Override
	public Integer getTotalCountForType(DasType type)
			throws DataSourceException {
		return hibernate.getTotalTypes(type);
	}

	@Override
	public int getTotalEntryPoints() throws UnimplementedFeatureException,
			DataSourceException {
		return hibernate.getTotalSegments();
	}

	@Override
	public Collection<DasType> getTypes() throws DataSourceException {
		Hibernate2MyDas h2m= new Hibernate2MyDas();
        List<DasType> dasTypes = new ArrayList<DasType>();
        List<Type> types = hibernate.getTypes();
        for (Type type:types)
        	dasTypes.add (h2m.map(type));
        return dasTypes;
	}

	@Override
	public void init(ServletContext servletContext,
			Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig)
			throws DataSourceException {
        this.svCon = servletContext;
        this.globalParameters = globalParameters;
        this.config = dataSourceConfig;
        hibernate= new HibernateManager();

	}

	@Override
	public void registerCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
	}

	@Override
	public void executeOtherCommand(HttpServletRequest request,
			HttpServletResponse response,
			DataSourceConfiguration dataSourceConfig, String command,
			String queryString) throws BadCommandException, DataSourceException {
		if (command.equals("authenticate")){
			String username = request.getParameter("user");
			String password = request.getParameter("password");
			Users user = hibernate.authenticate(username, password);
			if (user==null)
				throw new DataSourceException("User couldn't be created");
			response.setStatus(200);
		}else if (command.equals("createuser")){
			String username = request.getParameter("user");
			String password = request.getParameter("password");
			Users user = hibernate.createUser(username, password);
			if (user==null)
				throw new DataSourceException("User couldn't be created");
			response.setStatus(200);
		}else
			throw new BadCommandException("The command '"+command+"' has not been implemented in this datasource.");
	}

}
