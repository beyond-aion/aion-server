package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.CollisionDieActor;
import com.aionemu.gameserver.controllers.observer.ShieldObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeShield;
import com.aionemu.gameserver.model.siege.SiegeType;
import com.aionemu.gameserver.model.templates.shield.ShieldTemplate;

/**
 * @author xavier, Rolandas, SVDNESS
 */
public class ShieldService {

	private static final Logger log = LoggerFactory.getLogger(ShieldService.class);
	private static final Map<Integer, Set<String>> IGNORED_SHIELDS_BY_MAP_ID = Map.of(
		310100000, Set.of("BU_AB_CASTLESHIELD_SAMJUNG_03C_TYPE2_487543"), // Azoturan Fortress
		400010000, Set.of("BU_AB_SAMJUNG_BASE_01_SHIELD_313626", "BU_AB_SAMJUNG_BASE_01_SHIELD_299314", "BU_AB_SAMJUNG_BASE_01_SHIELD_137227") // artifact
	);
	private final Map<Integer, ShieldTemplate> sphereShields = new ConcurrentHashMap<>();
	private final Map<Integer, List<SiegeShield>> registeredShields = new ConcurrentHashMap<>();

	private ShieldService() {
		for (ShieldTemplate template : DataManager.SHIELD_DATA.getShieldTemplates()) {
			sphereShields.put(template.getId(), template);
		}
	}

	public void logDetachedShields() {
		registeredShields.forEach((mapId, shields) -> {
			if (!shields.isEmpty())
				log.warn("{} geo shield(s) are not attached to a SiegeLocation on map {}: {}", shields.size(), mapId, shields);
		});
	}

	public ShieldObserver createShieldObserver(FortressLocation location, Creature observed) {
		ShieldTemplate template = sphereShields.get(location.getLocationId());
		return template == null ? null : new ShieldObserver(location, template, observed);
	}

	public ActionObserver createShieldObserver(SiegeShield geoShield, Creature observed) {
		return GeoDataConfig.GEO_SHIELDS_ENABLE ? new CollisionDieActor(observed, geoShield.getGeometry()) : null;
	}

	/**
	 * Registers geo shield for zone lookup
	 */
	public void registerShield(int worldId, SiegeShield shield) {
		if (!isIgnored(worldId, shield))
			registeredShields.computeIfAbsent(worldId, _ -> new ArrayList<>()).add(shield);
	}

	/**
	 * Attaches geo shield and removes obsolete sphere shield if such exists. Should be called when geo shields and SiegeZoneInstance were created.
	 */
	public void attachShield(SiegeLocation location) {
		var mapId = location.getTemplate().getWorldId();
		var mapShields = registeredShields.get(mapId);
		if (mapShields == null) {
			return;
		}
		List<SiegeShield> attached = new ArrayList<>();
		for (int i = mapShields.size() - 1; i >= 0; i--) {
			var shield = mapShields.get(i);
			if (isShieldInsideLocation(shield, location)) {
				attached.add(shield);
				mapShields.remove(i);
				sphereShields.remove(location.getLocationId());
				shield.setSiegeLocationId(location.getLocationId());
			}
		}
		if (attached.isEmpty()) {
			if (location.getType() != SiegeType.OUTPOST && location.getLocationId() != 1241) { // outposts and miren don't have any shields
				log.warn("Could not find a shield for location ID {}.", location.getLocationId());
			}
		} else {
			location.setShields(attached);
		}
	}

	private static boolean isShieldInsideLocation(SiegeShield shield, SiegeLocation location) {
		var wb = shield.getGeometry().getWorldBound();
		var center = wb.getCenter();
		for (var z : location.getZone()) {
			var area = z.getAreaTemplate();
			//1. Center.
			if (area.isInside3D(center.x, center.y, center.z)) {
				return true;
			}
			//2. Corners AABB.
			if (wb instanceof BoundingBox bb) {
				var min = bb.getMin(null);
				var max = bb.getMax(null);
				if (area.isInside3D(min.x, min.y, min.z) ||
					area.isInside3D(min.x, min.y, max.z) ||
					area.isInside3D(min.x, max.y, min.z) ||
					area.isInside3D(min.x, max.y, max.z) ||
					area.isInside3D(max.x, min.y, min.z) ||
					area.isInside3D(max.x, min.y, max.z) ||
					area.isInside3D(max.x, max.y, min.z) ||
					area.isInside3D(max.x, max.y, max.z)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isIgnored(int mapId, SiegeShield shield) {
		var ignoredShields = IGNORED_SHIELDS_BY_MAP_ID.get(mapId);
		return ignoredShields != null && ignoredShields.contains(shield.getGeometry().getName());
	}

	private static class SingletonHolder {

		protected static final ShieldService instance = new ShieldService();
	}

	public static ShieldService getInstance() {
		return SingletonHolder.instance;
	}
}
