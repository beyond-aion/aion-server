package com.aionemu.gameserver.world.geo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ConsoleUtil;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.GeoWorldLoader;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ATracer
 */
public class RealGeoData implements GeoData {

	private static final Logger log = LoggerFactory.getLogger(RealGeoData.class);

	private TIntObjectHashMap<GeoMap> geoMaps = new TIntObjectHashMap<>();

	@Override
	public void loadGeoMaps() {
		Map<String, Spatial> models = loadMeshes();
		loadWorldMaps(models);
		models.clear();
		models = null;
		log.info("Geodata: " + geoMaps.size() + " geo maps loaded!");
	}

	protected void loadWorldMaps(Map<String, Spatial> models) {
		log.info("Loading geo maps..");
		ConsoleUtil.initAndPrintProgressBar(DataManager.WORLD_MAPS_DATA.size());
		Set<String> missingMeshes = new HashSet<>();
		List<String> missingDoors = new ArrayList<>();
		List<Integer> missingGeos = new ArrayList<>();

		for (WorldMapTemplate map : DataManager.WORLD_MAPS_DATA) {
			GeoMap geoMap = new GeoMap(Integer.toString(map.getMapId()), map.getWorldSize());

			if (GeoWorldLoader.loadWorld(map.getMapId(), models, geoMap, missingMeshes, missingDoors)) {
				geoMaps.put(map.getMapId(), geoMap);
			} else {
				if (map.getWorldSize() != 0) // don't warn about maps with size 0 (test maps which cannot be entered)
					missingGeos.add(map.getMapId());
				geoMaps.put(map.getMapId(), DummyGeoData.DUMMY_MAP);
			}
			ConsoleUtil.increaseAndPrintProgress();
		}

		if (missingMeshes.size() > 0)
			log.warn(missingMeshes.size() + " meshes are missing:\n" + missingMeshes.stream().sorted().collect(Collectors.joining("\n")));
		if (missingDoors.size() > 0)
			log.warn(missingDoors.size() + " door templates are missing:\n" + missingDoors.stream().sorted().collect(Collectors.joining("\n")));
		if (missingGeos.size() > 0)
			log.warn(missingGeos.size() + " maps are missing and reverted to dummy implementation:\n" + missingGeos);
	}

	protected Map<String, Spatial> loadMeshes() {
		log.info("Loading meshes..");
		Map<String, Spatial> models = null;
		try {
			models = GeoWorldLoader.loadMeshs("data/geo/models/geo.mesh");
		} catch (IOException e) {
			throw new IllegalStateException("Problem loading meshes", e);
		}
		return models;
	}

	@Override
	public GeoMap getMap(int worldId) {
		GeoMap geoMap = geoMaps.get(worldId);
		return geoMap != null ? geoMap : DummyGeoData.DUMMY_MAP;
	}
}
