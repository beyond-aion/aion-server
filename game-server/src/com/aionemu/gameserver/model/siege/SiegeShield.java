package com.aionemu.gameserver.model.siege;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.geoEngine.scene.DespawnableNode;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.ShieldService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;

/**
 * Shields have material ID 11 in geo.
 * 
 * @author Rolandas
 */
public class SiegeShield implements ZoneHandler {

	private final Map<Integer, ActionObserver> observed = new ConcurrentHashMap<>();
	private final Spatial geometry;
	private int siegeLocationId;
	private boolean isEnabled = false;

	public SiegeShield(Spatial geometry) {
		this.geometry = geometry;
		if (geometry != null && geometry.getParent() instanceof DespawnableNode despawnableNode) {
			despawnableNode.setId(siegeLocationId);
			despawnableNode.setType(DespawnableNode.DespawnableType.SHIELD);
		}
	}

	public Spatial getGeometry() {
		return geometry;
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		if (creature instanceof Player player && (isEnabled || siegeLocationId == 0)) {
			FortressLocation loc = SiegeService.getInstance().getFortress(siegeLocationId);
			if (loc == null || loc.getRace() != SiegeRace.getByRace(player.getRace())) {
				ActionObserver actionObserver = ShieldService.getInstance().createShieldObserver(this, creature);
				if (actionObserver != null) {
					creature.getObserveController().addObserver(actionObserver);
					observed.put(creature.getObjectId(), actionObserver);
				}
			}
		}
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		ActionObserver actionObserver = observed.remove(creature.getObjectId());
		if (actionObserver != null)
			creature.getObserveController().removeObserver(actionObserver);
	}

	public void setEnabled(boolean enable) {
		isEnabled = enable;
		if (geometry != null && geometry.getParent() instanceof DespawnableNode despawnableNode) {
			despawnableNode.setActive(1, enable);
		}
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public int getSiegeLocationId() {
		return siegeLocationId;
	}

	public void setSiegeLocationId(int siegeLocationId) {
		this.siegeLocationId = siegeLocationId;
		if (geometry != null && geometry.getParent() instanceof DespawnableNode despawnableNode) {
			despawnableNode.setId(siegeLocationId);
		}
	}

	@Override
	public String toString() {
		return "LocId=" + siegeLocationId + "; Name=" + geometry.getName() + "; Bounds=" + geometry.getWorldBound();
	}

}
