package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.CollisionDieActor;
import com.aionemu.gameserver.controllers.observer.ShieldObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.shield.Shield;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeShield;
import com.aionemu.gameserver.model.templates.shield.ShieldTemplate;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.jme3.math.Vector3f;

/**
 * @author xavier
 * @modified Rolandas
 */
public class ShieldService {

	Logger log = LoggerFactory.getLogger(ShieldService.class);

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final ShieldService instance = new ShieldService();
	}

	private final FastMap<Integer, Shield> sphereShields = new FastMap<Integer, Shield>();
	private final FastMap<Integer, List<SiegeShield>> registeredShields = new FastMap<Integer, List<SiegeShield>>();

	public static final ShieldService getInstance() {
		return SingletonHolder.instance;
	}

	private ShieldService() {
	}

	public void load(int mapId) {
		for (ShieldTemplate template : DataManager.SHIELD_DATA.getShieldTemplates()) {
			if (template.getMap() != mapId)
				continue;
			Shield f = new Shield(template);
			sphereShields.put(f.getId(), f);
		}
	}

	public void spawnAll() {
		for (Shield shield : sphereShields.values()) {
			shield.spawn();
			log.debug("Added " + shield.getName() + " at m=" + shield.getWorldId() + ",x=" + shield.getX() + ",y=" + shield.getY() + ",z="
				+ shield.getZ());
		}
		// TODO: check this list of not bound meshes (would remain inactive)
		for (List<SiegeShield> otherShields : registeredShields.values()) {
			for (SiegeShield shield : otherShields)
				log.debug("Not bound shield " + shield.getGeometry().getName());
		}
	}

	public ActionObserver createShieldObserver(int locationId, Creature observed) {
		if (sphereShields.containsKey(locationId))
			return new ShieldObserver(sphereShields.get(locationId), observed);
		return null;
	}

	public ActionObserver createShieldObserver(SiegeShield geoShield, Creature observed) {
		ActionObserver observer = null;
		if (GeoDataConfig.GEO_SHIELDS_ENABLE) {
			observer = new CollisionDieActor(observed, geoShield.getGeometry());
			((CollisionDieActor) observer).setEnabled(true);
		}
		return observer;
	}

	/**
	 * Registers geo shield for zone lookup
	 * 
	 * @param shield
	 *          - shield to be registered
	 */
	public void registerShield(int worldId, SiegeShield shield) {
		List<SiegeShield> mapShields = registeredShields.get(worldId);
		if (mapShields == null) {
			mapShields = new ArrayList<SiegeShield>();
			registeredShields.put(worldId, mapShields);
		}
		mapShields.add(shield);
	}

	/**
	 * Attaches geo shield and removes obsolete sphere shield if such exists. Should be called when geo shields and
	 * SiegeZoneInstance were created.
	 * 
	 * @param location
	 *          - siege location id
	 */
	public void attachShield(SiegeLocation location) {
		List<SiegeShield> mapShields = registeredShields.get(location.getTemplate().getWorldId());
		if (mapShields == null)
			return;

		ZoneInstance zone = location.getZone().get(0);
		List<SiegeShield> shields = new ArrayList<SiegeShield>();

		for (int index = mapShields.size() - 1; index >= 0; index--) {
			SiegeShield shield = mapShields.get(index);
			Vector3f center = shield.getGeometry().getWorldBound().getCenter();
			if (zone.getAreaTemplate().isInside3D(center.x, center.y, center.z)) {
				shields.add(shield);
				mapShields.remove(index);
				Shield sphereShield = sphereShields.get(location.getLocationId());
				if (sphereShield != null) {
					sphereShields.remove(location.getLocationId());
				}
				shield.setSiegeLocationId(location.getLocationId());
			}
		}
		if (shields.size() == 0) {
			log.warn("Could not find a shield for locId: " + location.getLocationId());
		}
		else {
			location.setShields(shields);
		}
	}

}
