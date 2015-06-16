package com.aionemu.gameserver.world.geo;

import com.aionemu.gameserver.geoEngine.models.GeoMap;

/**
 * @author ATracer
 */
public interface GeoData {

	void loadGeoMaps();

	GeoMap getMap(int worldId);
}
