package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.CollisionDieActor;
import com.aionemu.gameserver.controllers.observer.ShieldObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeShield;
import com.aionemu.gameserver.model.siege.SiegeType;
import com.aionemu.gameserver.model.templates.shield.ShieldTemplate;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author xavier, Rolandas
 */
public class ShieldService {

	Logger log = LoggerFactory.getLogger(ShieldService.class);

	private static class SingletonHolder {

		protected static final ShieldService instance = new ShieldService();
	}

	private final Map<Integer, ShieldTemplate> sphereShields = new ConcurrentHashMap<>();
	private final Map<Integer, List<SiegeShield>> registeredShields = new ConcurrentHashMap<>();

	public static ShieldService getInstance() {
		return SingletonHolder.instance;
	}

	private ShieldService() {
		for (ShieldTemplate template : DataManager.SHIELD_DATA.getShieldTemplates()) {
			sphereShields.put(template.getId(), template);
		}
	}

	public void logDetachedShields() {
		registeredShields.forEach((mapId, shields) -> {
			if (!shields.isEmpty())
				log.warn(shields.size() + " geo shield(s) are not attached to a SiegeLocation on map " + mapId + ": " + shields);
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
	 * 
	 * @param shield
	 *          - shield to be registered
	 */
	public void registerShield(int worldId, SiegeShield shield) {
		registeredShields.computeIfAbsent(worldId, k -> new ArrayList<>()).add(shield);
	}

	/**
	 * Attaches geo shield and removes obsolete sphere shield if such exists. Should be called when geo shields and SiegeZoneInstance were created.
	 * 
	 * @param location
	 *          - siege location id
	 */
	public void attachShield(SiegeLocation location) {
		List<SiegeShield> mapShields = registeredShields.get(location.getTemplate().getWorldId());
		if (mapShields == null)
			return;

		ZoneInstance zone = location.getZone().get(0);
		List<SiegeShield> shields = new ArrayList<>();

		for (int index = mapShields.size() - 1; index >= 0; index--) {
			SiegeShield shield = mapShields.get(index);
			Vector3f center = shield.getGeometry().getWorldBound().getCenter();
			if (zone.getAreaTemplate().isInside3D(center.x, center.y, center.z)) {
				shields.add(shield);
				mapShields.remove(index);
				sphereShields.remove(location.getLocationId());
				shield.setSiegeLocationId(location.getLocationId());
			}
		}
		if (shields.isEmpty()) {
			if (location.getType() != SiegeType.OUTPOST && location.getLocationId() != 1241) // outposts and miren don't have any shields
				log.warn("Could not find a shield for locId: " + location.getLocationId());
		} else {
			location.setShields(shields);
		}
	}

}
