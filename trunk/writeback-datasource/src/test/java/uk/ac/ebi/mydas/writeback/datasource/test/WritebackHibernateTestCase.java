package uk.ac.ebi.mydas.writeback.datasource.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;


import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.WritebackException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.writeback.datasource.hibernate.Hibernate2MyDas;
import uk.ac.ebi.mydas.writeback.datasource.hibernate.HibernateManager;
import uk.ac.ebi.mydas.writeback.datasource.hibernate.MyDas2Hibernate;
import uk.ac.ebi.mydas.writeback.datasource.model.Feature;
import uk.ac.ebi.mydas.writeback.datasource.model.Method;
import uk.ac.ebi.mydas.writeback.datasource.model.Orientation;
import uk.ac.ebi.mydas.writeback.datasource.model.Phase;
import uk.ac.ebi.mydas.writeback.datasource.model.Segment;
import uk.ac.ebi.mydas.writeback.datasource.model.Target;
import uk.ac.ebi.mydas.writeback.datasource.model.Type;
import uk.ac.ebi.mydas.writeback.datasource.model.Users;


public class WritebackHibernateTestCase extends TestCase {
	public static String[] featuresIds= new String[2];
	
	public void testSavingType() {
		HibernateManager hibernate = new HibernateManager(); 
		Type type = new Type();
		type.setCategory("swissprot");
		type.setCvId("SO:12345");
		type.setLabel("JustGuessing");
		type.setTypeId("12345");
		type.setReference(true);

		type = hibernate.getType(type, true, true);

		assertEquals(type.getCategory(), "swissprot");
		assertEquals(type.getCvId(), "SO:12345");
		assertEquals(type.getLabel(), "JustGuessing");
		assertEquals(type.getTypeId(), "12345");
	}
	public void testSavingMethod() {
		HibernateManager hibernate = new HibernateManager(); 
		Method method=new Method();
		method.setCvId("ECO:12345");
		method.setLabel("MoreGuessing");
		method.setMethodId("12345");
		method = hibernate.getMethod(method, true, true);

		assertEquals(method.getCvId(), "ECO:12345");
		assertEquals(method.getLabel(), "MoreGuessing");
		assertEquals(method.getMethodId(), "12345");
	}
	public void testSavingTarget() {
		HibernateManager hibernate = new HibernateManager(); 
		Target target=new Target();

		target.setTargetId("0987");
		target.setLabel("TheNewTarget");
		target.setStart(10);
		target.setStop(20);

		target = hibernate.getTarget(target, true, true);

		assertEquals(target.getLabel(), "TheNewTarget");
		assertEquals(target.getTargetId(), "0987");
		assertEquals(target.getStart(), new Integer(10));
		assertEquals(target.getStop(), new Integer(20));
	}
	public void testCreateUser() {
		HibernateManager hibernate = new HibernateManager();
		Users user = hibernate.createUser("theuser", "thepassword");
		assertNotNull(user);
		assertEquals(user.getLogin(), "theuser");
	}
	public void testCreateUserWithSameLogin() {
		HibernateManager hibernate = new HibernateManager();
		Users user = hibernate.createUser("theuser", "theotherpassword");
		assertNull(user);
	}
	public void testAuthenticateUser() {
		HibernateManager hibernate = new HibernateManager();
		Users user = hibernate.authenticate("theuser", "thepassword");
		assertNotNull(user);
		assertEquals(user.getLogin(), "theuser");
	}
	public void testAuthenticateBadUser() {
		HibernateManager hibernate = new HibernateManager();
		Users user = hibernate.authenticate("theuser", "theotherpassword");
		assertNull(user);
	}
	public void testAddingFeatureToNewSegment(){
		HibernateManager hibernate = new HibernateManager(); 

		Feature feature=new Feature();
		feature.setLabel("thelabel");
		feature.setOrientation(Orientation.ORIENTATION_NOT_APPLICABLE);
		feature.setPhase(Phase.PHASE_NOT_APPLICABLE);
		feature.setScore(0.1);
		feature.setStart(10);
		feature.setStop(100);

		Users user = new Users();
		user.setLogin("theuser");
		user.setPassword("thepassword");
		feature.setUsers(user);
		Target target=new Target();
		target.setTargetId("0987");
		target.setLabel("TheNewTarget");
		target.setStart(10);
		target.setStop(20);
		target = hibernate.getTarget(target, true, true);
		Set<Target> targets= new HashSet<Target>();
		targets.add(target);
		feature.setTargets(targets);

		Method method=new Method();
		method.setCvId("ECO:12345");
		method.setLabel("MoreGuessing");
		method.setMethodId("12345");
		method = hibernate.getMethod(method, true, true);
		feature.setMethod(method);

		Type type = new Type();
		type.setCategory("swissprot");
		type.setCvId("SO:12345");
		type.setLabel("JustGuessing");
		type.setTypeId("12345");
		type.setReference(true);
		type = hibernate.getType(type, true, true);
		feature.setType(type);

		Set<String> notes= new HashSet<String>();
		notes.add("first note");
		feature.setNotes(notes);

		Map<String,String> links=new HashMap<String,String>();
		links.put("http://www.uct.ac.za", "UCT");
		links.put("http://www.ebi.ac.uk", "EBI");
		feature.setLinks(links);

		Set<String> parents= new HashSet<String>();
		parents.add("first parent");
		feature.setParents(parents);

		Set<String> parts= new HashSet<String>();
		parts.add("first part");
		feature.setParts(parts);

		Segment segment=new Segment();
		Set<Feature> features= new HashSet<Feature>();
		features.add(feature);
		segment.setFeatures(features);
		segment.setIdSegment("firstSegment");
		segment.setLabel("the segment");
		segment.setStart(1);
		segment.setStop(1000);
		segment.setVersion("1234567890");


		Segment resultSegment=null;
		try {
			resultSegment = hibernate.addFeaturesFromSegment(segment,true,true);
		} catch (WritebackException e1) {
			fail("failed because an excpetion was arose:"+e1.toString());
		}

		Feature result=resultSegment.getFeatures().iterator().next();//hibernate.addFeature(feature);
		
		assertLocalContains(result.getFeatureId(),"http://writeback/");
		WritebackHibernateTestCase.featuresIds[0]=result.getFeatureId();

		assertEquals(((Target)result.getTargets().iterator().next()).getLabel(),"TheNewTarget");
		assertEquals(result.getMethod().getLabel(),"MoreGuessing");
		assertEquals(result.getType().getLabel(),"JustGuessing");
		assertEquals((String)result.getNotes().iterator().next(),"first note");
		assertEquals("UCT", result.getLinks().get("http://www.uct.ac.za"));
		assertEquals((String)result.getParts().iterator().next(),"first part");
	}

	public void testAddingFeatureToAnExistingSegment(){
		HibernateManager hibernate = new HibernateManager(); 

		Feature feature=new Feature();
		feature.setLabel("thesecondlabel");
		feature.setOrientation(Orientation.ORIENTATION_NOT_APPLICABLE);
		feature.setPhase(Phase.PHASE_NOT_APPLICABLE);
		feature.setScore(0.2);
		feature.setStart(20);
		feature.setStop(200);

		Users user = new Users();
		user.setLogin("theuser");
		user.setPassword("thepassword");
		feature.setUsers(user);
		Target target=new Target();
		target.setTargetId("20987");
		target.setLabel("TheSecondNewTarget");
		target.setStart(10);
		target.setStop(20);
		target = hibernate.getTarget(target, true, true);
		Set<Target> targets= new HashSet<Target>();
		targets.add(target);
		feature.setTargets(targets);

		Method method=new Method();
		method.setCvId("ECO:12345");
		method.setLabel("MoreGuessing");
		method.setMethodId("12345");
		method = hibernate.getMethod(method, true, true);
		feature.setMethod(method);

		Type type = new Type();
		type.setCategory("swissprot");
		type.setCvId("SO:12345");
		type.setLabel("JustGuessing");
		type.setTypeId("12345");
		type.setReference(true);
		type = hibernate.getType(type, true, true);
		feature.setType(type);

		Set<String> notes= new HashSet<String>();
		notes.add("second note");
		feature.setNotes(notes);

		Map<String,String> links=new HashMap<String,String>();
		links.put("http://www.second.ac.za", "second");
		feature.setLinks(links);


		Segment segment=new Segment();
		Set<Feature> features= new HashSet<Feature>();
		features.add(feature);
		segment.setFeatures(features);
		segment.setIdSegment("firstSegment");
		segment.setLabel("the segment");
		segment.setStart(1);
		segment.setStop(1000);
		segment.setVersion("1234567890");

		Segment resultSegment=null;
		try{
			resultSegment = hibernate.addFeaturesFromSegment(segment,true,true);
		} catch (WritebackException e1) {
			fail("failed because an excpetion was arose:"+e1.toString());
		}


		Iterator<Feature> iterator = resultSegment.getFeatures().iterator();

		Feature result=null;
		while (iterator.hasNext()){
			Feature temp=iterator.next();//hibernate.addFeature(feature);
			if (!temp.getFeatureId().equals(WritebackHibernateTestCase.featuresIds[0]))
				result=temp;
		}
		if (result==null)
			fail("there is not an edition of the feature");

		//		Feature result=resultSegment.getFeatures().iterator().next();//hibernate.addFeature(feature);

		assertLocalContains(result.getFeatureId(),"http://writeback/");
		WritebackHibernateTestCase.featuresIds[1]=result.getFeatureId();
		
		assertEquals(((Target)result.getTargets().iterator().next()).getLabel(),"TheSecondNewTarget");
		assertEquals(result.getMethod().getLabel(),"MoreGuessing");
		assertEquals(result.getType().getLabel(),"JustGuessing");
		assertEquals((String)result.getNotes().iterator().next(),"second note");
		assertEquals("second", result.getLinks().get("http://www.second.ac.za"));
	}

	public void testEditingExistingFeature(){
		HibernateManager hibernate = new HibernateManager(); 

		Feature feature=new Feature();
		feature.setFeatureId(WritebackHibernateTestCase.featuresIds[0]);

		feature.setLabel("theEditedlabel");
		feature.setOrientation(Orientation.ORIENTATION_NOT_APPLICABLE);
		feature.setPhase(Phase.PHASE_NOT_APPLICABLE);
		feature.setScore(0.1);
		feature.setStart(10);
		feature.setStop(100);

		Users user = new Users();
		user.setLogin("theuser");
		user.setPassword("thepassword");
		feature.setUsers(user);
		Target target=new Target();
		target.setTargetId("0987");
		target.setLabel("TheNewTarget");
		target.setStart(10);
		target.setStop(20);
		target = hibernate.getTarget(target, true, true);
		Set<Target> targets= new HashSet<Target>();
		targets.add(target);
		feature.setTargets(targets);

		Method method=new Method();
		method.setCvId("ECO:12345");
		method.setLabel("MoreGuessing");
		method.setMethodId("12345");
		method = hibernate.getMethod(method, true, true);
		feature.setMethod(method);

		Type type = new Type();
		type.setCategory("swissprot");
		type.setCvId("SO:12345");
		type.setLabel("JustGuessing");
		type.setTypeId("12345");
		type.setReference(true);
		type = hibernate.getType(type, true, true);
		feature.setType(type);

		Set<String> notes= new HashSet<String>();
		notes.add("first edited note");
		feature.setNotes(notes);

		Map<String,String> links=new HashMap<String,String>();
		links.put("http://www.uct.ac.za", "UCT");
		links.put("http://www.ebi.ac.uk", "EBI");
		feature.setLinks(links);

		Set<String> parents= new HashSet<String>();
		parents.add("first parent");
		feature.setParents(parents);

		Set<String> parts= new HashSet<String>();
		parts.add("first part");
		feature.setParts(parts);

		Segment segment=new Segment();
		Set<Feature> features= new HashSet<Feature>();
		features.add(feature);
		segment.setFeatures(features);
		segment.setIdSegment("firstSegment");
		segment.setLabel("the segment");
		segment.setStart(1);
		segment.setStop(1000);
		segment.setVersion("1234567890");

		Segment resultSegment=null;
		try{
			resultSegment = hibernate.updateFeaturesFromSegment(segment,true,true);
		} catch (WritebackException e1) {
			fail("failed because an excpetion was arose:"+e1.toString());
		}

		Iterator<Feature> iterator = resultSegment.getFeatures().iterator();

		Feature result=null;
		while (iterator.hasNext()){
			Feature temp=iterator.next();//hibernate.addFeature(feature);
			if (temp.getFeatureId().equals(WritebackHibernateTestCase.featuresIds[0])&&temp.getVersion()>1)
				result=temp;
		}
		if (result==null)
			fail("there is not an edition of the feature");
		assertEquals(WritebackHibernateTestCase.featuresIds[0], result.getFeatureId());
		assertEquals("theEditedlabel", result.getLabel());
		assertEquals(((Target)result.getTargets().iterator().next()).getLabel(),"TheNewTarget");
		assertEquals(result.getMethod().getLabel(),"MoreGuessing");
		assertEquals(result.getType().getLabel(),"JustGuessing");
		assertEquals((String)result.getNotes().iterator().next(),"first edited note");
		assertEquals("UCT", result.getLinks().get("http://www.uct.ac.za"));
		assertEquals((String)result.getParts().iterator().next(),"first part");
	}
	public void testEditingNewFeature(){
		HibernateManager hibernate = new HibernateManager(); 

		Feature feature=new Feature();
		feature.setFeatureId("http://theserver.com/thefeatureid");

		feature.setLabel("theNewEditedlabel");
		feature.setOrientation(Orientation.ORIENTATION_NOT_APPLICABLE);
		feature.setPhase(Phase.PHASE_NOT_APPLICABLE);
		feature.setScore(0.4);
		feature.setStart(40);
		feature.setStop(400);

		Users user = new Users();
		user.setLogin("theuser");
		user.setPassword("thepassword");
		feature.setUsers(user);
		Target target=new Target();
		target.setTargetId("40987");
		target.setLabel("TheEditedNewTarget2");
		target.setStart(30);
		target.setStop(40);
		target = hibernate.getTarget(target, true, true);
		Set<Target> targets= new HashSet<Target>();
		targets.add(target);
		feature.setTargets(targets);

		Method method=new Method();
		method.setCvId("ECO:11345");
		method.setLabel("ByGuessing");
		method.setMethodId("11345");
		method = hibernate.getMethod(method, true, true);
		feature.setMethod(method);

		Type type = new Type();
		type.setCategory("swissprot");
		type.setCvId("SO:12335");
		type.setLabel("testing");
		type.setTypeId("12335");
		type.setReference(true);
		type = hibernate.getType(type, true, true);
		feature.setType(type);

		Set<String> notes= new HashSet<String>();
		notes.add("another note");
		feature.setNotes(notes);

		Map<String,String> links=new HashMap<String,String>();
		links.put("http://www.uct.ac.za", "UCT");
		links.put("http://www.ebi.ac.uk", "EBI");
		feature.setLinks(links);

		Segment segment=new Segment();
		Set<Feature> features= new HashSet<Feature>();
		features.add(feature);
		segment.setFeatures(features);
		segment.setIdSegment("firstSegment");
		segment.setLabel("the segment");
		segment.setStart(1);
		segment.setStop(1000);
		segment.setVersion("1234567890");

		Segment resultSegment =null;
		try{
			resultSegment = hibernate.updateFeaturesFromSegment(segment,true,true);
		} catch (WritebackException e1) {
			fail("failed because an excpetion was arose:"+e1.toString());
		}

		Iterator<Feature> iterator = resultSegment.getFeatures().iterator();

		Feature result=null;
		while (iterator.hasNext()){
			Feature temp=iterator.next();//hibernate.addFeature(feature);
			if (temp.getFeatureId().equals("http://theserver.com/thefeatureid"))
				result=temp;
		}
		if (result==null)
			fail("there is not an edition of the feature");
		assertEquals("http://theserver.com/thefeatureid", result.getFeatureId());
		assertEquals("theNewEditedlabel", result.getLabel());
		assertEquals(((Target)result.getTargets().iterator().next()).getLabel(),"TheEditedNewTarget2");
		assertEquals(result.getMethod().getLabel(),"ByGuessing");
		assertEquals(result.getType().getLabel(),"testing");
		assertEquals((String)result.getNotes().iterator().next(),"another note");
		assertEquals("UCT", result.getLinks().get("http://www.uct.ac.za"));
	}
	public void testDeletingFeatureFromExistingSegment(){
		HibernateManager hibernate = new HibernateManager(); 

		Feature feature=new Feature();
		feature.setFeatureId("http://theserver.com/thefeatureid");

		Users user = new Users();
		user.setLogin("theuser");
		user.setPassword("thepassword");
		feature.setUsers(user);


		Segment segment=new Segment();
		Set<Feature> features= new HashSet<Feature>();
		features.add(feature);
		segment.setFeatures(features);
		segment.setIdSegment("firstSegment");

		Segment resultSegment=null;
		try{
			resultSegment = hibernate.deleteFeaturesFromSegment(segment,true);
		} catch (WritebackException e1) {
			fail("failed because an excpetion was arose:"+e1.toString());
		}

		Iterator<Feature> iterator = resultSegment.getFeatures().iterator();

		Feature result=null;
		while (iterator.hasNext()){
			Feature temp=iterator.next();//hibernate.addFeature(feature);
			if (temp.getFeatureId().equals("http://theserver.com/thefeatureid")&&temp.getVersion()>1)
				result=temp;
		}
		if (result==null)
			fail("there is not an edition of the feature");
		assertEquals("http://theserver.com/thefeatureid", result.getFeatureId());
		assertEquals("DELETED", result.getLabel());
	}
	public void testDeletingFeatureFromNewSegment(){
		HibernateManager hibernate = new HibernateManager(); 

		Feature feature=new Feature();
		feature.setFeatureId("http://theotherserver.com/thefeatureid");

		Users user = new Users();
		user.setLogin("theuser");
		user.setPassword("thepassword");
		feature.setUsers(user);

		Segment segment=new Segment();
		Set<Feature> features= new HashSet<Feature>();
		features.add(feature);
		segment.setFeatures(features);
		segment.setIdSegment("anewSegment");

		Segment resultSegment=null;
		try{
			resultSegment = hibernate.deleteFeaturesFromSegment(segment,true);
		} catch (WritebackException e1) {
			fail("failed because an excpetion was arose:"+e1.toString());
		}

		Iterator<Feature> iterator = resultSegment.getFeatures().iterator();

		Feature result=null;
		while (iterator.hasNext()){
			Feature temp=iterator.next();//hibernate.addFeature(feature);
			if (temp.getFeatureId().equals("http://theotherserver.com/thefeatureid"))
				result=temp;
		}
		if (result==null)
			fail("there is not an edition of the feature");
		assertEquals("http://theotherserver.com/thefeatureid", result.getFeatureId());
		assertEquals("DELETED", result.getLabel());
	}

	public void testQueringSegment(){
		HibernateManager hibernate = new HibernateManager(); 
		Segment resultSegment = hibernate.getSegmentFromId("firstSegment");

		assertEquals(new Integer(1), resultSegment.getStart());
		assertEquals(new Integer(1000), resultSegment.getStop());
		assertEquals("1234567890", resultSegment.getVersion());
		assertEquals("the segment", resultSegment.getLabel());

		for (Feature feature:resultSegment.getFeatures()){
			if (feature.getFeatureId().equals(WritebackHibernateTestCase.featuresIds[0])){
				assertEquals(new Integer(2),feature.getVersion());
				assertEquals("theEditedlabel",feature.getLabel());
				assertEquals(new Integer(10),feature.getStart());
				assertEquals(new Integer(100),feature.getStop());
				assertEquals("12345",feature.getType().getTypeId());
				assertEquals("swissprot",feature.getType().getCategory());
				assertEquals("JustGuessing",feature.getType().getLabel());
			}else if (feature.getFeatureId().equals(WritebackHibernateTestCase.featuresIds[1])){
				assertEquals(new Integer(1),feature.getVersion());
				assertEquals("thesecondlabel",feature.getLabel());
				assertEquals(new Integer(20),feature.getStart());
				assertEquals(new Integer(200),feature.getStop());
				assertEquals("12345",feature.getType().getTypeId());
				assertEquals("swissprot",feature.getType().getCategory());
				assertEquals("JustGuessing",feature.getType().getLabel());
			}else if (feature.getFeatureId().equals("http://theserver.com/thefeatureid")){
				assertEquals(new Integer(2),feature.getVersion());
				assertEquals("DELETED",feature.getLabel());
			}else{
				fail("Got a feature different to expected. ("+feature.getId()+")");
			}
		}
	}
	public void testQueringSegmentWithRange(){
		HibernateManager hibernate = new HibernateManager(); 
		Segment resultSegment = hibernate.getSegmentFromIdAndRange("firstSegment",1,100);

		assertEquals(new Integer(1), resultSegment.getStart());
		assertEquals(new Integer(1000), resultSegment.getStop());
		assertEquals("1234567890", resultSegment.getVersion());
		assertEquals("the segment", resultSegment.getLabel());

		for (Feature feature:resultSegment.getFeatures()){
			if (feature.getFeatureId().equals(featuresIds[0])){
				assertEquals(new Integer(2),feature.getVersion());
				assertEquals("theEditedlabel",feature.getLabel());
				assertEquals(new Integer(10),feature.getStart());
				assertEquals(new Integer(100),feature.getStop());
				assertEquals("12345",feature.getType().getTypeId());
				assertEquals("swissprot",feature.getType().getCategory());
				assertEquals("JustGuessing",feature.getType().getLabel());
			}else{
				fail("Got a feature different to expected. ("+feature.getId()+")");
			}
		}
	}
	public void testQueringFeatureHistory(){
		HibernateManager hibernate = new HibernateManager(); 
		Segment resultSegment = hibernate.getFeatureHistoryFromId(WritebackHibernateTestCase.featuresIds[0]);

		assertNotNull(resultSegment);

		assertEquals(new Integer(1), resultSegment.getStart());
		assertEquals(new Integer(1000), resultSegment.getStop());
		assertEquals("1234567890", resultSegment.getVersion());
		assertEquals("the segment", resultSegment.getLabel());
		int times=0;
		for (Feature feature:resultSegment.getFeatures()){
			if (feature.getFeatureId().equals(WritebackHibernateTestCase.featuresIds[0])){
				times++;
				assertEquals(new Integer(10),feature.getStart());
				assertEquals(new Integer(100),feature.getStop());
				assertEquals("12345",feature.getType().getTypeId());
				assertEquals("swissprot",feature.getType().getCategory());
				assertEquals("JustGuessing",feature.getType().getLabel());
			}else{
				fail("Got a feature different to expected. ("+feature.getId()+")");
			}
		}
		if(times!=2)
			fail("Got "+times+" features, and 2 were expected");
	}

	public void testQueringAndTranslating(){
		HibernateManager hibernate = new HibernateManager(); 
		Segment resultSegment = hibernate.getSegmentFromId("firstSegment");
		Hibernate2MyDas h2m=new Hibernate2MyDas();
		DasAnnotatedSegment seg=null;
		try {
			seg = h2m.map(resultSegment);
		} catch (DataSourceException e) {
			fail("Error mapping!");
		}
		assertEquals(new Integer(1), seg.getStartCoordinate());
		assertEquals(new Integer(1000), seg.getStopCoordinate());
		assertEquals("1234567890", seg.getVersion());
		assertEquals("firstSegment", seg.getSegmentId());

		for (DasFeature feature: seg.getFeatures()){
			if (feature.getFeatureId().equals(WritebackHibernateTestCase.featuresIds[0])){
				assertEquals("theEditedlabel",feature.getFeatureLabel());
				assertEquals(10,feature.getStartCoordinate());
				assertEquals(100,feature.getStopCoordinate());
				assertEquals("12345",feature.getType().getId());
				assertEquals("swissprot",feature.getType().getCategory());
				assertEquals("JustGuessing",feature.getType().getLabel());
			}else if (feature.getFeatureId().equals(WritebackHibernateTestCase.featuresIds[1])){
				//				assertEquals(new Integer(1),feature.getVersion());
				assertEquals("thesecondlabel",feature.getFeatureLabel());
				assertEquals(20,feature.getStartCoordinate());
				assertEquals(200,feature.getStopCoordinate());
				assertEquals("12345",feature.getType().getId());
				assertEquals("swissprot",feature.getType().getCategory());
				assertEquals("JustGuessing",feature.getType().getLabel());
			}else if (feature.getFeatureId().equals("http://theserver.com/thefeatureid")){
				//				assertEquals(new Integer(2),feature.getVersion());
				assertEquals("DELETED",feature.getFeatureLabel());
			}else{
				fail("Got a feature different to expected. ("+feature.getFeatureId()+")");
			}
		}
	}
	public void testQueringAndDoubleTranslating(){
		HibernateManager hibernate = new HibernateManager(); 
		Segment originalSegment = hibernate.getSegmentFromId("firstSegment");
		Segment resultSegment=null;
		Hibernate2MyDas h2m=new Hibernate2MyDas();
		DasAnnotatedSegment seg=null;
		try {
			seg = h2m.map(originalSegment);
		} catch (DataSourceException e) {
			fail("Error in first mapping!");
		}
		MyDas2Hibernate m2h = new MyDas2Hibernate();
		resultSegment = m2h.map(seg);

		assertEquals(new Integer(1), resultSegment.getStart());
		assertEquals(new Integer(1000), resultSegment.getStop());
		assertEquals("1234567890", resultSegment.getVersion());
		assertEquals("the segment", resultSegment.getLabel());

		for (Feature feature:resultSegment.getFeatures()){
			if (feature.getFeatureId().equals(WritebackHibernateTestCase.featuresIds[0])){
				assertEquals(new Integer(2),feature.getVersion());
				assertEquals("theEditedlabel",feature.getLabel());
				assertEquals(new Integer(10),feature.getStart());
				assertEquals(new Integer(100),feature.getStop());
				assertEquals("12345",feature.getType().getTypeId());
				assertEquals("swissprot",feature.getType().getCategory());
				assertEquals("JustGuessing",feature.getType().getLabel());
			}else if (feature.getFeatureId().equals(WritebackHibernateTestCase.featuresIds[1])){
				assertEquals(new Integer(1),feature.getVersion());
				assertEquals("thesecondlabel",feature.getLabel());
				assertEquals(new Integer(20),feature.getStart());
				assertEquals(new Integer(200),feature.getStop());
				assertEquals("12345",feature.getType().getTypeId());
				assertEquals("swissprot",feature.getType().getCategory());
				assertEquals("JustGuessing",feature.getType().getLabel());
			}else if (feature.getFeatureId().equals("http://theserver.com/thefeatureid")){
				assertEquals(new Integer(2),feature.getVersion());
				assertEquals("DELETED",feature.getLabel());
			}else{
				fail("Got a feature different to expected. ("+feature.getId()+")");
			}
		}
	}
	private void assertLocalContains(String text,String subtext){
		assertTrue("The text ["+subtext+"] was not found in ["+text+"]",text.contains(subtext));
	}

}
