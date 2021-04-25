package com.aionemu.gameserver.world.geo;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.GeoWorldLoader;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.Geometry;
import com.aionemu.gameserver.geoEngine.scene.Node;
import com.aionemu.gameserver.utils.ThreadPoolManager;

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
		File[] meshFiles = new File("data/geo").listFiles((file, name) -> name.endsWith(".mesh"));
		if (meshFiles == null || meshFiles.length == 0) {
			log.warn("No *.mesh files present in ./data/geo");
			return Collections.emptyMap();
		}
		Map<String, Node> meshes = new HashMap<>();
		for (File meshFile : meshFiles)
			meshes.putAll(GeoWorldLoader.loadMeshes(meshFile));
		triggerCollisionDataInitialization(meshes.values()); // async since there's no need to wait for completion
		return meshes;
	}

	public static void triggerCollisionDataInitialization(Collection<Node> meshes) {
		meshes.forEach(node -> node.getChildren().forEach(child -> {
			if (child instanceof Geometry geometry)
				ThreadPoolManager.getInstance().execute(() -> geometry.getMesh().createCollisionData());
		}));
	}

	@Override
	public GeoMap getMap(int worldId) {
		GeoMap geoMap = geoMaps.get(worldId);
		return geoMap != null ? geoMap : DummyGeoData.DUMMY_MAP;
	}
}
