package com.aionemu.gameserver.world.zone;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rolandas
 */
public final class ZoneName {

	private final static Logger log = LoggerFactory.getLogger(ZoneName.class);

	private static final Map<String, ZoneName> zoneNames = new HashMap<>();
	public static final String NONE = "NONE";

	static {
		zoneNames.put(NONE, new ZoneName(NONE));
	}

	private String _name;

	private ZoneName(String name) {
		this._name = name;
	}

	public String name() {
		return _name;
	}

	public int id() {
		return _name.hashCode();
	}

	public static final ZoneName createOrGet(String name) {
		name = name.toUpperCase();
		if (zoneNames.containsKey(name))
			return zoneNames.get(name);
		ZoneName newZone = new ZoneName(name);
		zoneNames.put(name, newZone);
		return newZone;
	}

	public static final int getId(String name) {
		name = name.toUpperCase();
		if (zoneNames.containsKey(name))
			return zoneNames.get(name).id();
		return zoneNames.get(NONE).id();
	}

	public static final ZoneName get(String name) {
		name = name.toUpperCase();
		if (zoneNames.containsKey(name))
			return zoneNames.get(name);
		log.warn("Missing zone : " + name);
		return zoneNames.get(NONE);
	}

	@Override
	public String toString() {
		return _name;
	}

}
