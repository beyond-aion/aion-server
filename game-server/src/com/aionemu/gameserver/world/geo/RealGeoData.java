package com.aionemu.gameserver.world.geo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.GeoWorldLoader;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.Node;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ATracer
 */
public class RealGeoData implements GeoData {

	private static final Logger log = LoggerFactory.getLogger(RealGeoData.class);

	private final TIntObjectHashMap<GeoMap> geoMaps = new TIntObjectHashMap<>();

	@Override
	public void loadGeoMaps() {
		Map<String, Node> models = loadMeshes();
		loadWorldMaps(models);
		log.info("Geodata: " + geoMaps.size() + " geo maps loaded!");
	}

	private void loadWorldMaps(Map<String, Node> models) {
		log.info("Loading geo maps..");
		Set<String> missingMeshes = ConcurrentHashMap.newKeySet();
		List<Integer> missingGeos = new CopyOnWriteArrayList<>();

		DataManager.WORLD_MAPS_DATA.forEachParalllel(map -> {
			GeoMap geoMap = GeoWorldLoader.loadWorld(map, models, missingMeshes);
			if (geoMap == DummyGeoData.DUMMY_MAP && map.getWorldSize() != 0) // don't warn about maps with size 0 (test maps which cannot be entered)
				missingGeos.add(map.getMapId());
			synchronized (geoMaps) {
				geoMaps.put(map.getMapId(), geoMap);
			}
		});

		if (missingMeshes.size() > 0)
			log.warn(missingMeshes.size() + " meshes are missing:\n" + missingMeshes.stream().sorted().collect(Collectors.joining("\n")));
		if (missingGeos.size() > 0)
			log.warn(missingGeos.size() + " maps are missing and reverted to dummy implementation: " + missingGeos);
	}

	private Map<String, Node> loadMeshes() {
		log.info("Loading meshes..");
		try {
			return GeoWorldLoader.loadMeshes("data/geo/models/geo.mesh");
		} catch (IOException e) {
			throw new GameServerError("Could not load meshes", e);
		}
	}

	@Override
	public GeoMap getMap(int worldId) {
		GeoMap geoMap = geoMaps.get(worldId);
		return geoMap != null ? geoMap : DummyGeoData.DUMMY_MAP;
	}
}
