package com.aionemu.gameserver.world.geo;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ConsoleUtil;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.GeoWorldLoader;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.jme3.scene.Spatial;

/**
 * @author ATracer
 */
public class RealGeoData implements GeoData {

	private static final Logger log = LoggerFactory.getLogger(RealGeoData.class);

	private TIntObjectHashMap<GeoMap> geoMaps = new TIntObjectHashMap<GeoMap>();

	@Override
	public void loadGeoMaps() {
		log.info("Loading meshes...");
		Map<String, Spatial> models = GeoWorldLoader.loadMeshes("models/geo.mesh");

		log.info("Loading geo maps...");
		ConsoleUtil.printProgressBarHeader(DataManager.WORLD_MAPS_DATA.size());
		List<Integer> mapsWithErrors = new ArrayList<Integer>();

		for (WorldMapTemplate map : DataManager.WORLD_MAPS_DATA) {
			GeoMap geoMap = new GeoMap(Integer.toString(map.getMapId()), map.getWorldSize());
			try {
				if (GeoWorldLoader.loadWorld(map.getMapId(), models, geoMap)) {
					geoMaps.put(map.getMapId(), geoMap);
				}
			} catch (Throwable t) {
				mapsWithErrors.add(map.getMapId());
				geoMaps.put(map.getMapId(), DummyGeoData.DUMMY_MAP);
			}
			ConsoleUtil.printCurrentProgress();
		}
		ConsoleUtil.printEndProgress();
		models.clear();
		models = null;

		log.info("Geodata: " + geoMaps.size() + " geo maps loaded!");
		if (mapsWithErrors.size() > 0)
			log.warn("Some maps were not loaded correctly and reverted to dummy implementation:\n" + mapsWithErrors.toString());
	}

	@Override
	public GeoMap getMap(int worldId) {
		GeoMap geoMap = geoMaps.get(worldId);
		return geoMap != null ? geoMap : DummyGeoData.DUMMY_MAP;
	}
}
