package uk.ac.ebi.mydas.writeback.datasource.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class Phase implements Serializable {

	private String name;

	public static final Phase PHASE_NOT_APPLICABLE	=	new Phase("-");
	public static final Phase PHASE_READING_FRAME_0	=	new Phase("0");
	public static final Phase PHASE_READING_FRAME_1	=	new Phase("1");
	public static final Phase PHASE_READING_FRAME_2	=	new Phase("2");

	private static final Map<String,Phase> INSTANCES = new HashMap<String,Phase>();

	static {
		INSTANCES.put(PHASE_NOT_APPLICABLE.toString(), PHASE_NOT_APPLICABLE);
		INSTANCES.put(PHASE_READING_FRAME_0.toString(), PHASE_READING_FRAME_0);
		INSTANCES.put(PHASE_READING_FRAME_1.toString(), PHASE_READING_FRAME_1);
		INSTANCES.put(PHASE_READING_FRAME_2.toString(), PHASE_READING_FRAME_2);
	}
	private Phase(String name) {
		this.name=name;
	}
	public String toString() {
		return name;
	}

	private Object readResolve() {
		return getInstance(name);
	}

	public static Phase getInstance(String name) {
		return (Phase)INSTANCES.get(name);
	}
}