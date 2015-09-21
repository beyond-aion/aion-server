package com.aionemu.gameserver.world.geo;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Map;
import java.util.Set;

import javolution.util.FastSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ConsoleUtil;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.GeoWorldLoader;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.SpatialEx;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;

/**
 * @author ATracer
 */
public class RealGeoData implements GeoData {

	private static final Logger log = LoggerFactory.getLogger(RealGeoData.class);

	private TIntObjectHashMap<GeoMap> geoMaps = new TIntObjectHashMap<GeoMap>();

	@Override
	public void loadGeoMaps() {
		log.info("Loading meshes...");
		Map<String, SpatialEx> models = GeoWorldLoader.loadMeshes("models/geo.mesh");

		log.info("Loading geo maps...");
		ConsoleUtil c = ConsoleUtil.newInstance();
		c.printProgressBar(DataManager.WORLD_MAPS_DATA.size());
		Set<String> missingMeshes = new FastSet<>();
		Set<String> mapsWithErrors = new FastSet<>();

		for (WorldMapTemplate map : DataManager.WORLD_MAPS_DATA) {
			GeoMap geoMap = new GeoMap(Integer.toString(map.getMapId()), map.getWorldSize());
			if (!GeoWorldLoader.loadWorld(map.getMapId(), models, geoMap, missingMeshes)) {
				mapsWithErrors.add(map.getMapId() + " (" + map.getName() + ")");
				geoMap = DummyGeoData.DUMMY_MAP;
			}
			geoMaps.put(map.getMapId(), geoMap);
			c.printCurrentProgress();
		}

		log.info("Geodata: " + geoMaps.size() + " geo maps loaded!");
		if (missingMeshes.size() > 0)
			log.warn(missingMeshes.size() + " mesh(es) were not found and therefore not loaded:\n" + missingMeshes.toString());
		if (mapsWithErrors.size() > 0)
			log.warn(mapsWithErrors.size() + " map(s) were not loaded correctly and reverted to dummy implementation:\n" + mapsWithErrors.toString());
	}

	@Override
	public GeoMap getMap(int worldId) {
		GeoMap geoMap = geoMaps.get(worldId);
		return geoMap != null ? geoMap : DummyGeoData.DUMMY_MAP;
	}
}
