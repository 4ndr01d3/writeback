package uk.ac.ebi.mydas.writeback.datasource.hibernate;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import uk.ac.ebi.mydas.exceptions.WritebackException;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.writeback.datasource.model.Feature;
import uk.ac.ebi.mydas.writeback.datasource.model.Method;
import uk.ac.ebi.mydas.writeback.datasource.model.Orientation;
import uk.ac.ebi.mydas.writeback.datasource.model.Phase;
import uk.ac.ebi.mydas.writeback.datasource.model.Segment;
import uk.ac.ebi.mydas.writeback.datasource.model.Target;
import uk.ac.ebi.mydas.writeback.datasource.model.Type;
import uk.ac.ebi.mydas.writeback.datasource.model.Users;

public class HibernateManager {


	public Type getType(Type type,boolean addIfnotInDB,boolean updateIfDifferent){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Type result = (Type) session.createQuery("FROM Type WHERE typeid = ?").setString(0, type.getTypeId()).uniqueResult();
		if (result==null){
			if (addIfnotInDB){
				result=type;
				session.save(result);
			}
		}else if (updateIfDifferent){
			result.setCategory(type.getCategory());
			result.setCvId(type.getCvId());
			result.setLabel(type.getLabel());
			result.setTypeId(type.getTypeId());
			result.setReference(type.isReference());
			result.setSubparts(type.isSubparts());
			result.setSuperparts(type.isSuperparts());
			session.update(result);
		}
		session.getTransaction().commit();		
		return result;
	}
	public Method getMethod(Method method,boolean addIfnotInDB,boolean updateIfDifferent){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Method result = (Method) session.createQuery("FROM Method WHERE methodid = ?").setString(0, method.getMethodId()).uniqueResult();
		if (result==null){
			if (addIfnotInDB){
				result=method;
				session.save(result);
			}
		}else if (updateIfDifferent){
			result.setCvId(method.getCvId());
			result.setLabel(method.getLabel());
			result.setMethodId(method.getMethodId());
			session.update(result);
		}
		session.getTransaction().commit();		
		return result;
	}
	public Target getTarget(Target target,boolean addIfnotInDB,boolean updateIfDifferent){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Target result = (Target) session.createQuery("FROM Target WHERE targetid = ?").setString(0, target.getTargetId()).uniqueResult();
		if (result==null){
			if (addIfnotInDB){
				result=target;
				session.save(result);
			}
		}else if (updateIfDifferent){
			result.setTargetId(target.getTargetId());
			result.setLabel(target.getLabel());
			result.setStart(target.getStart());
			result.setStop(target.getStop());
			session.update(result);
		}
		session.getTransaction().commit();		
		return result;
	}
	public Users createUser(String username, String password){
		password = this.getMD5(password);
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();

		Users result = (Users) session.createQuery("FROM Users WHERE login = ?").setString(0, username).uniqueResult();
		if (result==null){
			result=new Users();
			result.setLogin(username);
			result.setPassword(password);
			session.save(result);
		}else{
			session.getTransaction().commit();		
			return null;
		}
		session.getTransaction().commit();		
		return result;
	}
	public Feature addFeature(Feature feature, Session session) throws WritebackException{
		Long result = (Long) session.createQuery("SELECT max(id) FROM Feature").uniqueResult();
		result=(result==null)?0:result++;
		Users user = feature.getUsers();
		user=this.authenticate(user.getLogin(), user.getPassword());
		if (user==null)
			throw new WritebackException("Authentication Error");
		feature.setUsers(user);
		feature.setFeatureId("http://writeback/"+result);
		feature.setVersion(1);
		feature.setDatecreated(new Date());
		feature.setType(this.getType(feature.getType(), true, true));
		feature.setMethod(this.getMethod(feature.getMethod(), true, true));
		Set<Target> tempTargets = feature.getTargets();
		if (tempTargets!=null){
			feature.setTargets(new HashSet<Target>());
			for (Target target:tempTargets){
				feature.getTargets().add(this.getTarget(target, true, true));
			}
		}
		session.save(feature);
		//		session.getTransaction().commit();		
		return feature;
	}
	public Feature updateFeature(Feature feature, Session session) throws WritebackException{
		//		Session session = HibernateUtil.getSessionFactory().openSession();
		//		session.beginTransaction();
		Users user = feature.getUsers();
		user=this.authenticate(user.getLogin(), user.getPassword());
		if (user==null)
			throw new WritebackException("Authentication Error");
		feature.setUsers(user);
		
		try {
			new URL(feature.getFeatureId());
		} catch (MalformedURLException e) {
			try {
				feature.setFeatureId((new URL(feature.getHref()+"/"+feature.getFeatureId())).toString());
			} catch (MalformedURLException e1) {
				feature.setFeatureId("http://writeback/"+feature.getFeatureId());
			}
		}
		
		Integer result = (Integer) session.createQuery("SELECT MAX(version) FROM Feature WHERE featureId = ?").setString(0, feature.getFeatureId()).uniqueResult();
		if (result==null)
			feature.setVersion(1);
		else
			feature.setVersion(result+1);

		feature.setDatecreated(new Date());
		feature.setType(this.getType(feature.getType(), true, true));
		feature.setMethod(this.getMethod(feature.getMethod(), true, true));
		Set<Target> tempTargets = feature.getTargets();
		if (tempTargets!=null){
			feature.setTargets(new HashSet<Target>());
			for (Target target:tempTargets){
				feature.getTargets().add(this.getTarget(target, true, true));
			}
		}
		session.save(feature);
		//		session.getTransaction().commit();		
		return feature;
	}
	public Users authenticate(String username, String password){
		password = this.getMD5(password);
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();

		Users result = (Users) session.createQuery("FROM Users WHERE login = ? AND password =?").setString(0, username).setString(1, password).uniqueResult();
		session.getTransaction().commit();		
		return result;

	}	
	private String getMD5(String string){
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
			byte[] data = string.getBytes(); 
			m.update(data,0,data.length);
			BigInteger i = new BigInteger(1,m.digest());
			return String.format("%1$032X", i);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

	}
	public Segment addFeaturesFromSegment(Segment segment,boolean addIfnotInDB,boolean updateIfDifferent) throws WritebackException{
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Segment result = (Segment) session.createQuery("FROM Segment WHERE idSegment = ?").setString(0, segment.getIdSegment()).uniqueResult();
		try{
			if (result==null){
				if (addIfnotInDB){
					result=segment;
					Set<Feature> features = segment.getFeatures();
					result.setFeatures(new HashSet<Feature>());
					for(Feature featuretoadd:features){
						featuretoadd.setSegment(result);
						this.addFeature(featuretoadd,session);
						result.addFeature(featuretoadd);
					}
					session.save(result);
				}
			}else if (updateIfDifferent){

				result.setIdSegment(segment.getIdSegment());
				result.setLabel(segment.getLabel());
				result.setStart(segment.getStart());
				result.setStop(segment.getStop());
				result.setVersion(segment.getVersion());
				for(Feature featuretoadd:segment.getFeatures()){
					featuretoadd.setSegment(result);
					this.addFeature(featuretoadd,session);
					result.addFeature(featuretoadd);
				}
				session.update(result);
			}
		}catch(WritebackException wbe){
			throw new WritebackException("The feature couldn't be added",wbe);
		}
		session.getTransaction().commit();		
		return this.getSegmentFromId(segment.getIdSegment());
	}
	public Segment updateFeaturesFromSegment(Segment segment,boolean addIfnotInDB,boolean updateIfDifferent) throws WritebackException{
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Segment result = (Segment) session.createQuery("FROM Segment WHERE idSegment = ?").setString(0, segment.getIdSegment()).uniqueResult();
		try{
			if (result==null){
				if (addIfnotInDB){
					result=segment;
					Set<Feature> features = segment.getFeatures();
					result.setFeatures(new HashSet<Feature>());
					for(Feature featuretoedit:features){
						featuretoedit.setSegment(result);
						this.updateFeature(featuretoedit,session);
						result.addFeature(featuretoedit);
					}
					session.save(result);
				}
			}else if (updateIfDifferent){

				result.setIdSegment(segment.getIdSegment());
				result.setLabel(segment.getLabel());
				result.setStart(segment.getStart());
				result.setStop(segment.getStop());
				result.setVersion(segment.getVersion());
				for(Feature featuretoedit:segment.getFeatures()){
					featuretoedit.setSegment(result);
					this.updateFeature(featuretoedit,session);
					result.addFeature(featuretoedit);
				}
				session.update(result);
			}
		}catch(WritebackException wbe){
			throw new WritebackException("The feature couldn't be added",wbe);
		}
		session.getTransaction().commit();		
		return this.getSegmentFromId(segment.getIdSegment());
	}
	public Segment deleteFeaturesFromSegment(Segment segment,boolean addIfnotInDB) throws WritebackException{
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Segment result = (Segment) session.createQuery("FROM Segment WHERE idSegment = ?").setString(0, segment.getIdSegment()).uniqueResult();
		Type delType= new Type();
		delType.setTypeId("DELETED");
		delType=getType(delType,true,false);
		Method delMethod= new Method();
		delMethod.setMethodId("DELETED");
		delMethod=getMethod(delMethod,true,false);
		try{
			if (result==null){
				if (addIfnotInDB){
					result=segment;
					result.setStart(0);
					result.setStop(0);
					result.setVersion("FROM_DELETION");
					Set<Feature> features = segment.getFeatures();
					result.setFeatures(new HashSet<Feature>());
					for(Feature featuretoedit:features){
						featuretoedit.setSegment(result);
						featuretoedit.setDeleted(true);
						featuretoedit.setLabel("DELETED");
						featuretoedit.setType(delType);
						featuretoedit.setMethod(delMethod);
						featuretoedit.setStart(0);
						featuretoedit.setStop(0);
						featuretoedit.setScore(0.0);
						featuretoedit.setPhase(Phase.PHASE_NOT_APPLICABLE);
						featuretoedit.setOrientation(Orientation.ORIENTATION_NOT_APPLICABLE);
						this.updateFeature(featuretoedit,session);
						result.addFeature(featuretoedit);
					}
					session.save(result);
					session.getTransaction().commit();		
				}
			}else {
				Set<Feature> deletedFeatures=new HashSet<Feature>();
				result.setIdSegment(segment.getIdSegment());
				for(Feature featuretoedit:segment.getFeatures()){
					featuretoedit.setDeleted(true);
					featuretoedit.setLabel("DELETED");
					featuretoedit.setType(delType);
					featuretoedit.setMethod(delMethod);
					featuretoedit.setStart(0);
					featuretoedit.setStop(0);
					featuretoedit.setScore(0.0);
					featuretoedit.setPhase(Phase.PHASE_NOT_APPLICABLE);
					featuretoedit.setOrientation(Orientation.ORIENTATION_NOT_APPLICABLE);
					featuretoedit.setSegment(result);
					deletedFeatures.add(this.updateFeature(featuretoedit,session));
					result.addFeature(featuretoedit);
				}
				session.update(result);
				session.getTransaction().commit();		
				result.setFeatures(deletedFeatures);
			}
		}catch(WritebackException wbe){
			throw new WritebackException("The feature couldn't be added",wbe);
		}

		return this.getSegmentFromId(segment.getIdSegment());
	}
	@SuppressWarnings("unchecked")
	public Segment getSegmentFromId(String segmentId) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Segment result = (Segment) session.createQuery("FROM Segment WHERE idSegment = ?").setString(0, segmentId).uniqueResult();
		if (result==null){
			return null;
		}else{
			Iterator<Feature> iterator=session.createSQLQuery("SELECT f.* from FEATURE f, (select max(f.version) as maxi,f.featureid from SEGMENT_FEATURE sf, FEATURE f,SEGMENT s where sf.feature_id=f.id and sf.segment_id=s.id and s.idsegment=? group by f.featureid) f2 where f.version=f2.maxi and f.featureid=f2.featureid" ).addEntity(Feature.class).setString(0, segmentId).list().iterator();
			result.setFeatures(new HashSet<Feature>());
			while (iterator.hasNext()){
				result.addFeature((Feature)iterator.next());
			}
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	public Segment getSegmentFromFeatureId(String featureId) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Segment result = (Segment) session.createQuery("SELECT s FROM Segment as s JOIN s.features as f WITH f.featureId=?").setString(0, featureId).uniqueResult();
		if (result!=null){
			Iterator<Feature> iterator=session.createQuery("SELECT f FROM Segment as s JOIN s.features as f WITH f.featureId=? ORDER by f.version DESC" ).setString(0, featureId).list().iterator();
			result.setFeatures(new HashSet<Feature>());
			if (iterator.hasNext()){
				result.addFeature((Feature)iterator.next());
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Segment getSegmentFromIdAndRange(String segmentId,int start, int stop) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Segment result = (Segment) session.createQuery("FROM Segment WHERE idSegment = ?").setString(0, segmentId).uniqueResult();
		if (result==null){
			return null;
		}else{
			Iterator<Feature> iterator=session.createSQLQuery("SELECT f.* from FEATURE f, (select max(f.version) as maxi,f.featureid from SEGMENT_FEATURE sf, FEATURE f,SEGMENT s where sf.feature_id=f.id and sf.segment_id=s.id and s.idsegment=? group by f.featureid) f2 where f.version=f2.maxi and f.featureid=f2.featureid and f.start>="+start+" AND f.stop<="+stop ).addEntity(Feature.class).setString(0, segmentId).list().iterator();
			result.setFeatures(new HashSet<Feature>());
			while (iterator.hasNext()){
				result.addFeature((Feature)iterator.next());
			}
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	public Segment getFeatureHistoryFromId(String featureId) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Segment result = (Segment) session.createQuery("SELECT s FROM Segment as s JOIN s.features as f WITH f.featureId=?").setString(0, featureId).uniqueResult();
		if (result!=null){
			Iterator<Feature> iterator=session.createQuery("SELECT f FROM Segment as s JOIN s.features as f WITH f.featureId=? ORDER BY f.version" ).setString(0, featureId).list().iterator();
			result.setFeatures(new HashSet<Feature>());
			while (iterator.hasNext()){
				result.addFeature((Feature)iterator.next());
			}

		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Segment> getSegments(Integer from, Integer to){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List<Segment> result = session.createQuery("SELECT s FROM Segment as s").list();
		return result.subList(from-1, to);		
	}
	public int getTotalSegments() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Long result = (Long) session.createQuery("SELECT count(*) FROM Segment").uniqueResult();
		if (result==null)
			return 0;	
		return new Integer(result.toString());
	}
	public int getTotalTypes(DasType type) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Long result = (Long) session.createQuery("SELECT count(*) FROM Feature WHERE type.typeId=?").setString(0, type.getId()).uniqueResult();
		if (result==null)
			return 0;	
		return new Integer(result.toString());
	}

	@SuppressWarnings("unchecked")
	public List<Type> getTypes(){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		return session.createQuery("SELECT t FROM Type as t").list();
	}

}
