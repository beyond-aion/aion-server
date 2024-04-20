package com.aionemu.gameserver.world.zone;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.ScriptManager;
import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.ZoneData;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.geometry.*;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeShield;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.model.templates.zone.MaterialZoneTemplate;
import com.aionemu.gameserver.model.templates.zone.WorldZoneTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.services.ShieldService;
import com.aionemu.gameserver.world.zone.handler.*;

/**
 * @author ATracer, antness
 */
public final class ZoneService implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(ZoneService.class);
	private ScriptManager scriptManager = new ScriptManager();
	private Map<ZoneName, Class<? extends ZoneHandler>> zoneHandlers = new HashMap<>();
	private Map<ZoneName, ZoneHandler> collidableHandlers = new HashMap<>();
	private Map<Integer, List<ZoneInfo>> zoneByMapIdMap = DataManager.ZONE_DATA.getZones();

	private ZoneService() {
	}

	@Override
	public void load() {
		log.info("Zone engine load started");

		AggregatedClassListener acl = new AggregatedClassListener();
		acl.addClassListener(new OnClassLoadUnloadListener());
		acl.addClassListener(new ScheduledTaskClassListener());
		acl.addClassListener(new ZoneHandlerClassListener());
		scriptManager.setGlobalClassListener(acl);

		try {
			scriptManager.load(WorldConfig.ZONE_HANDLER_DIRECTORY);
			log.info("Loaded " + zoneHandlers.size() + " zone handlers.");
		} catch (Exception e) {
			throw new GameServerError("Can't initialize instance handlers.", e);
		}
	}

	@Override
	public void shutdown() {
		log.info("Zone engine shutdown started");
		scriptManager.shutdown();
		zoneHandlers.clear();
		log.info("Zone engine shutdown complete");
	}

	public ZoneHandler getNewZoneHandler(ZoneName zoneName) {
		ZoneHandler zoneHandler = collidableHandlers.get(zoneName);
		if (zoneHandler != null)
			return zoneHandler;
		Class<? extends ZoneHandler> zoneClass = zoneHandlers.get(zoneName);
		if (zoneClass != null) {
			try {
				zoneHandler = zoneClass.getDeclaredConstructor().newInstance();
			} catch (Exception ex) {
				log.warn("Can't instantiate zone handler " + zoneName, ex);
			}
		}

		return zoneHandler != null ? zoneHandler : new GeneralZoneHandler();
	}

	public final void addZoneHandlerClass(Class<? extends ZoneHandler> handler) {
		ZoneNameAnnotation idAnnotation = handler.getAnnotation(ZoneNameAnnotation.class);
		if (idAnnotation != null) {
			String[] zoneNames = idAnnotation.value().split(" ");
			for (String zoneNameString : zoneNames) {
				try {
					ZoneName zoneName = ZoneName.get(zoneNameString.trim());
					if (zoneName == ZoneName.get("NONE"))
						throw new RuntimeException();
					zoneHandlers.put(zoneName, handler);
				} catch (Exception e) {
					log.warn("Missing ZoneName: " + idAnnotation.value());
				}
			}
		}
	}

	public final void addZoneHandlerClass(ZoneName zoneName, Class<? extends ZoneHandler> handler) {
		zoneHandlers.put(zoneName, handler);
	}

	public Map<ZoneName, ZoneInstance> getZoneInstancesByWorldId(int mapId) {
		Map<ZoneName, ZoneInstance> zones = new HashMap<>();
		int worldSize = DataManager.WORLD_MAPS_DATA.getTemplate(mapId).getWorldSize();
		WorldZoneTemplate zone = new WorldZoneTemplate(worldSize, mapId);
		PolyArea fullArea = new PolyArea(zone.getName(), mapId, zone.getPoints().getPoint(), zone.getPoints().getBottom(), zone.getPoints().getTop());
		ZoneInstance fullMap = new ZoneInstance(mapId, new ZoneInfo(fullArea, zone));
		fullMap.addHandler(getNewZoneHandler(zone.getName()));
		zones.put(zone.getName(), fullMap);

		Collection<ZoneInfo> areas = this.zoneByMapIdMap.get(mapId);
		if (areas == null)
			return zones;

		for (ZoneInfo area : areas) {
			ZoneInstance instance;
			switch (area.getZoneTemplate().getZoneType()) {
				case FLY:
					instance = new FlyZoneInstance(mapId, area);
					break;
				case FORT:
					instance = new SiegeZoneInstance(mapId, area);
					SiegeLocation siege = DataManager.SIEGE_LOCATION_DATA.getSiegeLocations().get(area.getZoneTemplate().getSiegeId().get(0));
					if (siege != null) {
						siege.addZone((SiegeZoneInstance) instance);
						if (GeoDataConfig.GEO_SHIELDS_ENABLE)
							ShieldService.getInstance().attachShield(siege);
					}
					break;
				case ARTIFACT:
					instance = new SiegeZoneInstance(mapId, area);
					for (int artifactId : area.getZoneTemplate().getSiegeId()) {
						SiegeLocation artifact = DataManager.SIEGE_LOCATION_DATA.getArtifacts().get(artifactId);
						if (artifact == null) {
							log.warn("Missing siege location data for zone " + area.getZoneTemplate().getName().name());
						} else {
							artifact.addZone((SiegeZoneInstance) instance);
						}
					}
					break;
				case PVP:
					instance = new PvPZoneInstance(mapId, area);
					break;
				default:
					InvasionZoneInstance invasionZone = getIZI(area);
					if (invasionZone != null) {
						instance = invasionZone;
					} else {
						instance = new ZoneInstance(mapId, area);
					}
			}
			instance.addHandler(getNewZoneHandler(area.getZoneTemplate().getName()));
			zones.put(area.getZoneTemplate().getName(), instance);
		}
		return zones;
	}

	private InvasionZoneInstance getIZI(ZoneInfo area) {
		if (area.getZoneTemplate().getName().name().equals("WAILING_CLIFFS_220050000")
			|| area.getZoneTemplate().getName().name().equals("BALTASAR_CEMETERY_220050000")
			|| area.getZoneTemplate().getName().name().equals("THE_LEGEND_SHRINE_220050000")
			|| area.getZoneTemplate().getName().name().equals("SUDORVILLE_220050000")
			|| area.getZoneTemplate().getName().name().equals("BALTASAR_HILL_VILLAGE_220050000")
			|| area.getZoneTemplate().getName().name().equals("BRUSTHONIN_MITHRIL_MINE_220050000")) {
			return validateZone(area);
		} else if (area.getZoneTemplate().getName().name().equals("JAMANOK_INN_210060000")
			|| area.getZoneTemplate().getName().name().equals("THE_STALKING_GROUNDS_210060000")
			|| area.getZoneTemplate().getName().name().equals("BLACK_ROCK_HOT_SPRING_210060000")
			|| area.getZoneTemplate().getName().name().equals("FREGIONS_FLAME_210060000")) {
			return validateZone(area);
		}
		return null;
	}

	private InvasionZoneInstance validateZone(ZoneInfo area) {
		int mapId = area.getZoneTemplate().getMapid();
		VortexLocation vortex = DataManager.VORTEX_DATA.getVortexLocation(mapId);
		if (vortex != null) {
			InvasionZoneInstance instance = new InvasionZoneInstance(mapId, area);
			vortex.addZone(instance);
			return instance;
		}
		return null;
	}

	public void saveMaterialZones() {
		List<ZoneTemplate> templates = new ArrayList<>();
		for (WorldMapTemplate map : DataManager.WORLD_MAPS_DATA) {
			Collection<ZoneInfo> areas = this.zoneByMapIdMap.get(map.getMapId());
			if (areas == null)
				continue;
			for (ZoneInfo zone : areas) {
				if (collidableHandlers.containsKey(zone.getArea().getZoneName())) {
					templates.add(zone.getZoneTemplate());
				}
			}
		}
		templates.sort(Comparator.comparingInt(ZoneTemplate::getMapid));

		ZoneData zoneData = new ZoneData();
		zoneData.zoneList = templates;
		zoneData.saveData();
	}

	public synchronized void createMaterialZoneTemplate(Spatial geometry, int worldId, ZoneName zoneName) {
		if (zoneName == ZoneName.NONE)
			return;

		ZoneHandler handler = collidableHandlers.get(zoneName);
		if (handler == null) {
			if (geometry.getMaterialId() == 11) {
				if (GeoDataConfig.GEO_SHIELDS_ENABLE) {
					handler = new SiegeShield(geometry);
					ShieldService.getInstance().registerShield(worldId, (SiegeShield) handler);
				} else
					return;
			} else {
				MaterialTemplate template = DataManager.MATERIAL_DATA.getTemplate(geometry.getMaterialId());
				if (template == null)
					return;
				handler = new MaterialZoneHandler(geometry, template);
			}
			collidableHandlers.put(zoneName, handler);
		} else {
			log.warn("Duplicate material mesh: " + zoneName.toString());
		}

		List<ZoneInfo> areas = zoneByMapIdMap.get(worldId);
		if (areas == null) {
			areas = new ArrayList<>();
			zoneByMapIdMap.put(worldId, areas);
		}
		ZoneInfo zoneInfo = null;
		for (ZoneInfo area : areas) {
			if (area.getZoneTemplate().getName().equals(zoneName)) {
				zoneInfo = area;
				break;
			}
		}
		if (zoneInfo == null) {
			MaterialZoneTemplate zoneTemplate = new MaterialZoneTemplate(geometry, worldId);
			// maybe add to zone data if needed search ?
			Area zoneInfoArea = null;
			if (zoneTemplate.getSphere() != null) {
				zoneInfoArea = new SphereArea(zoneName, worldId, zoneTemplate.getSphere().getX(), zoneTemplate.getSphere().getY(),
					zoneTemplate.getSphere().getZ(), zoneTemplate.getSphere().getR());
			} else if (zoneTemplate.getCylinder() != null) {
				zoneInfoArea = new CylinderArea(zoneName, worldId, zoneTemplate.getCylinder().getX(), zoneTemplate.getCylinder().getY(),
					zoneTemplate.getCylinder().getR(), zoneTemplate.getCylinder().getBottom(), zoneTemplate.getCylinder().getTop());
			} else if (zoneTemplate.getSemisphere() != null) {
				zoneInfoArea = new SemisphereArea(zoneName, worldId, zoneTemplate.getSemisphere().getX(), zoneTemplate.getSemisphere().getY(),
					zoneTemplate.getSemisphere().getZ(), zoneTemplate.getSemisphere().getR());
			}
			if (zoneInfoArea != null) {
				zoneInfo = new ZoneInfo(zoneInfoArea, zoneTemplate);
				areas.add(zoneInfo);
			}
		}
	}

	public static ZoneService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final ZoneService instance = new ZoneService();
	}
}
