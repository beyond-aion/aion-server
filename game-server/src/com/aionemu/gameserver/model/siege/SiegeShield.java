package com.aionemu.gameserver.model.siege;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.geoEngine.scene.DespawnableNode;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.services.ShieldService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
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
        if (!(creature instanceof Player player)) {
            return;
        }
        if (!isEnabled) {
            return;
        }
        var loc = SiegeService.getInstance().getFortress(siegeLocationId);
        if (loc == null || loc.getRace() != SiegeRace.getByRace(player.getRace())) {
            var actionObserver = ShieldService.getInstance().createShieldObserver(this, creature);
            if (actionObserver != null) {
                creature.getObserveController().addObserver(actionObserver);
                observed.put(creature.getObjectId(), actionObserver);
            }
        }
    }

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		var actionObserver = observed.remove(creature.getObjectId());
		if (actionObserver != null)
			creature.getObserveController().removeObserver(actionObserver);
	}

   public void setEnabled(boolean enable) {
        if (isEnabled == enable) {
            return;
        }
        isEnabled = enable;
        if (!enable) {
            clearObservers();
        }
        if (geometry != null && geometry.getParent() instanceof DespawnableNode despawnableNode) {
            despawnableNode.setActive(1, enable);
        }
    }

     //Insurance just in case.
	   public void clearObservers() {
        if (observed.isEmpty()) {
            return;
        }
        observed.forEach((objectId, obs) -> {
            var obj = World.getInstance().findVisibleObject(objectId);
            if (obj instanceof Creature creature) {
                creature.getObserveController().removeObserver(obs);
            }
        });
        observed.clear();
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
