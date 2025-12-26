package com.aionemu.gameserver.services;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.CollisionDieActor;
import com.aionemu.gameserver.controllers.observer.ShieldObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeShield;
import com.aionemu.gameserver.model.siege.SiegeType;
import com.aionemu.gameserver.model.templates.shield.ShieldTemplate;
import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.bounding.BoundingVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

public class ShieldService {
	Logger log = LoggerFactory.getLogger(ShieldService.class);
	private final Map<Integer, ShieldTemplate> sphereShields = new ConcurrentHashMap<>();
	private final Map<Integer, List<SiegeShield>> registeredShields = new ConcurrentHashMap<>();
	private static final Set<Integer> IGNORE_DETACHED_SHIELDS_MAPS = Set.of(400010000, 310100000);
	private static final String[] IGNORE_DETACHED_SHIELDS_PREFIXES = {
            "PR_A_AIRBUNKER_EFFECT_",
            "BU_AB_SAMJUNG_BASE_01_SHIELD_",
            "BU_DG_DRAGONSHIELD_"
    };

	private ShieldService() {
		for (var template : DataManager.SHIELD_DATA.getShieldTemplates()) {
			sphereShields.put(template.getId(), template);
		}
	}

	   public void logDetachedShields() {
        registeredShields.forEach((mapId, shields) -> {
            if (shields == null || shields.isEmpty()) {
                return;
            }
            List<SiegeShield> important = null;
            for (var s : shields) {
                if (!shouldIgnoreDetachedShield(mapId, s)) {
                    if (important == null) {
                        important = new ArrayList<>();
                    }
                    important.add(s);
                }
            }
            if (important == null || important.isEmpty()) {
                return;
            }
            log.warn("{} geo shield(s) are not attached to a SiegeLocation on map {}: {}.", important.size(), mapId, important);
        });
    }

	public ShieldObserver createShieldObserver(FortressLocation location, Creature observed) {
		var template = sphereShields.get(location.getLocationId());
		return template == null ? null : new ShieldObserver(location, template, observed);
	}

	public ActionObserver createShieldObserver(SiegeShield geoShield, Creature observed) {
		return GeoDataConfig.GEO_SHIELDS_ENABLE ? new CollisionDieActor(observed, geoShield.getGeometry()) : null;
	}

	public void registerShield(int worldId, SiegeShield shield) {
		registeredShields.computeIfAbsent(worldId, k -> new ArrayList<>()).add(shield);
	}

	public void attachShield(SiegeLocation location) {
        var mapId = location.getTemplate().getWorldId();
        var mapShields = registeredShields.get(mapId);
        if (mapShields == null || mapShields.isEmpty()) {
            return;
        }
        var zones = location.getZone();
        if (zones == null || zones.isEmpty()) {
            return;
        }
        List<SiegeShield> attached = new ArrayList<>();
        for (int i = mapShields.size() - 1; i >= 0; i--) {
            var shield = mapShields.get(i);
            var geo = shield.getGeometry();
            if (geo == null) {
                continue;
            }
            var wb = geo.getWorldBound();
            if (wb == null) {
                continue;
            }
            var center = wb.getCenter();
            boolean inside = false;
            for (var z : zones) {
                var area = z.getAreaTemplate();
                if (area == null) {
                    continue;
                }
                //1. Center.
                if (area.isInside3D(center.x, center.y, center.z)) {
                    inside = true;
                    break;
                }
                //2. Corners AABB.
                if (wb.getType() == BoundingVolume.Type.AABB) {
                    var bb = (BoundingBox) wb;
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
                        inside = true;
                        break;
                    }
                }
            }
            if (inside) {
                attached.add(shield);
                mapShields.remove(i);
                sphereShields.remove(location.getLocationId());
                shield.setSiegeLocationId(location.getLocationId());
            }
        }
        if (attached.isEmpty()) {
            if (location.getType() != SiegeType.OUTPOST && location.getLocationId() != 1241) {
                log.warn("Could not find a shield for locId: {}.", location.getLocationId());
            }
        } else {
            location.setShields(attached);
        }
    }

	private static boolean shouldIgnoreDetachedShield(int mapId, SiegeShield shield) {
        if (IGNORE_DETACHED_SHIELDS_MAPS.contains(mapId)) {
            return true;
        }
        var geo = shield.getGeometry();
        if (geo == null) {
            return false;
        }
        var name = geo.getName();
        if (name == null) {
            return false;
        }
        for (var p : IGNORE_DETACHED_SHIELDS_PREFIXES) {
            if (name.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

		private static class SingletonHolder {
		protected static final ShieldService instance = new ShieldService();
	}

	public static ShieldService getInstance() {
		return SingletonHolder.instance;
	}
}
