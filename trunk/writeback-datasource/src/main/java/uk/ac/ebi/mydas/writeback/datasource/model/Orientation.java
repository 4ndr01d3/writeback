package uk.ac.ebi.mydas.writeback.datasource.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class Orientation implements Serializable {

	private String name;

	public static final Orientation ORIENTATION_NOT_APPLICABLE		=	new Orientation("0");
	public static final Orientation ORIENTATION_SENSE_STRAND		=	new Orientation("+");
	public static final Orientation ORIENTATION_ANTISENSE_STRAND	=	new Orientation("-");

	private static final Map<String,Orientation> INSTANCES = new HashMap<String,Orientation>();

	static {
		INSTANCES.put(ORIENTATION_NOT_APPLICABLE.toString(), ORIENTATION_NOT_APPLICABLE);
		INSTANCES.put(ORIENTATION_SENSE_STRAND.toString(), ORIENTATION_SENSE_STRAND);
		INSTANCES.put(ORIENTATION_ANTISENSE_STRAND.toString(), ORIENTATION_ANTISENSE_STRAND);
	}
	private Orientation(String name) {
		this.name=name;
	}
	public String toString() {
		return name;
	}

	private Object readResolve() {
		return getInstance(name);
	}

	public static Orientation getInstance(String name) {
		return (Orientation)INSTANCES.get(name);
	}
}
