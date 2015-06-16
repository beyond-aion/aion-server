package com.aionemu.gameserver.world.geo;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.geoEngine.models.GeoMap;

/**
 * @author ATracer
 */
public class DummyGeoData implements GeoData {

	public static final DummyGeoMap DUMMY_MAP = new DummyGeoMap(StringUtils.EMPTY, 0);

	@Override
	public void loadGeoMaps() {
	}

	@Override
	public GeoMap getMap(int worldId) {
		return DUMMY_MAP;
	}
}
