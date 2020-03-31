package com.aionemu.gameserver.world.zone;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rolandas
 */
public final class ZoneName {

	private final static Logger log = LoggerFactory.getLogger(ZoneName.class);

	private static final Map<String, ZoneName> zoneNames = new ConcurrentHashMap<>();
	public static final ZoneName NONE = new ZoneName("NONE");

	static {
		zoneNames.put(NONE.name(), NONE);
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

	public static ZoneName createOrGet(String name) {
		return zoneNames.computeIfAbsent(name.toUpperCase(), ZoneName::new);
	}

	public static int getId(String name) {
		return zoneNames.getOrDefault(name.toUpperCase(), NONE).id();
	}

	public static ZoneName get(String name) {
		name = name.toUpperCase();
		ZoneName zoneName = zoneNames.get(name);
		if (zoneName == null) {
			zoneName = NONE;
			log.warn("Missing zone : " + name);
		}
		return zoneName;
	}

	@Override
	public String toString() {
		return _name;
	}

}
