package com.aionemu.gameserver.world.zone;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.ScriptManager;
import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.model.templates.zone.MaterialZoneTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.services.ShieldService;
import com.aionemu.gameserver.world.zone.handler.MaterialZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;
import com.aionemu.gameserver.world.zone.handler.ZoneHandlerClassListener;
import com.aionemu.gameserver.world.zone.handler.ZoneNameAnnotation;

/**
 * @author ATracer, antness
 */
public final class ZoneService implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(ZoneService.class);
	private final Map<String, Class<? extends ZoneHandler>> zoneHandlers = new HashMap<>();
	private final Map<Integer, List<MaterialZone>> materialZonesByMapId = new HashMap<>();

	private ZoneService() {
	}

	@Override
	public void init() {
		ScriptManager scriptManager = new ScriptManager();
		AggregatedClassListener acl = new AggregatedClassListener();
		acl.addClassListener(new OnClassLoadUnloadListener());
		acl.addClassListener(new ZoneHandlerClassListener());
		scriptManager.setGlobalClassListener(acl);
		scriptManager.load(WorldConfig.ZONE_HANDLER_DIRECTORY);
		log.info("Loaded " + zoneHandlers.size() + " zone handlers.");
	}

	public ZoneHandler getNewZoneHandler(String zoneName) {
		Class<? extends ZoneHandler> zoneClass = zoneHandlers.get(zoneName);
		if (zoneClass != null) {
			try {
				return zoneClass.getDeclaredConstructor().newInstance();
			} catch (Exception ex) {
				log.warn("Can't instantiate zone handler " + zoneName, ex);
			}
		}
		return null;
	}

	public void addZoneHandlerClass(Class<? extends ZoneHandler> handler) {
		ZoneNameAnnotation idAnnotation = handler.getAnnotation(ZoneNameAnnotation.class);
		if (idAnnotation != null) {
			String[] zoneNames = idAnnotation.value().split(" +");
			for (String zoneName : zoneNames) {
				if (DataManager.ZONE_DATA.validateZoneName(zoneName))
					zoneHandlers.put(zoneName, handler);
			}
		}
	}

	public List<ZoneInstance> createZoneInstances(int mapId) {
		List<ZoneInstance> zones = new ArrayList<>();
		VortexLocation vortex = getAndValidateVortexLocation(mapId);
		for (ZoneInfo area : DataManager.ZONE_DATA.getZones(mapId)) {
			ZoneInstance instance;
			switch (area.getZoneTemplate().getZoneType()) {
				case FLY -> instance = new FlyZoneInstance(mapId, area);
				case NO_FLY -> instance = new NoFlyZoneInstance(mapId, area);
				case FORT -> {
					instance = new SiegeZoneInstance(mapId, area);
					SiegeLocation siege = DataManager.SIEGE_LOCATION_DATA.getSiegeLocations().get(area.getZoneTemplate().getSiegeId().getFirst());
					if (siege != null) {
						siege.addZone((SiegeZoneInstance) instance);
						ShieldService.getInstance().attachShield(siege);
					}
				}
				case ARTIFACT -> {
					instance = new SiegeZoneInstance(mapId, area);
					for (int artifactId : area.getZoneTemplate().getSiegeId()) {
						SiegeLocation artifact = DataManager.SIEGE_LOCATION_DATA.getArtifacts().get(artifactId);
						if (artifact == null) {
							log.warn("Missing siege location data for zone {}", area.getZoneTemplate().getName());
						} else {
							artifact.addZone((SiegeZoneInstance) instance);
						}
					}
				}
				case PVP -> instance = area.getZoneTemplate().hasZoneAttribute(ZoneAttributes.PVP_ENABLED) ? new PvPZoneInstance(mapId, area) : new DisablePvPZoneInstance(mapId, area);
				default -> {
					instance = tryRegisterInvasionZone(vortex, area);
					if (instance == null)
						instance = new ZoneInstance(mapId, area);
				}
			}
			ZoneHandler zoneHandler = getNewZoneHandler(area.getZoneTemplate().getName());
			if (zoneHandler != null)
				instance.addHandler(zoneHandler);
			zones.add(instance);
		}
		for (MaterialZone materialZone : materialZonesByMapId.getOrDefault(mapId, Collections.emptyList())) {
			zones.add(new ZoneInstance(mapId, materialZone.zoneInfo, List.of(materialZone.handler)));
		}
		return zones;
	}

	private static VortexLocation getAndValidateVortexLocation(int mapId) {
		VortexLocation vortexLocation = DataManager.VORTEX_DATA.getVortexLocation(mapId);
		if (vortexLocation != null)
			vortexLocation.getTemplate().getZones().forEach(DataManager.ZONE_DATA::validateZoneName);
		return vortexLocation;
	}

	private InvasionZoneInstance tryRegisterInvasionZone(VortexLocation vortex, ZoneInfo area) {
		if (vortex != null && vortex.getTemplate().getZones().contains(area.getZoneTemplate().getName())) {
			InvasionZoneInstance instance = new InvasionZoneInstance(area.getZoneTemplate().getMapid(), area);
			vortex.addZone(instance);
			return instance;
		}
		return null;
	}

	public synchronized void createMaterialZoneTemplate(Spatial geometry, int worldId) {
		ZoneHandler handler;
		if (geometry.getMaterialId() == 11) {
			handler = ShieldService.getInstance().tryRegisterShield(worldId, geometry);
			if (handler == null)
				return;
		} else {
			MaterialTemplate template = DataManager.MATERIAL_DATA.getTemplate(geometry.getMaterialId());
			if (template == null)
				return;
			handler = new MaterialZoneHandler(geometry, template);
		}
		List<MaterialZone> materialZones = materialZonesByMapId.computeIfAbsent(worldId, _ -> new ArrayList<>());
		materialZones.add(new MaterialZone(new ZoneInfo(new MaterialZoneTemplate(geometry, worldId)), handler));
	}

	public static ZoneService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final ZoneService instance = new ZoneService();
	}

	record MaterialZone(ZoneInfo zoneInfo, ZoneHandler handler) {}
}
