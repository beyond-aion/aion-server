package com.aionemu.gameserver.controllers.observer;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResult;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.materials.MaterialActTime;
import com.aionemu.gameserver.model.templates.materials.MaterialSkill;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.gametime.DayTime;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Rolandas
 */
public class CollisionMaterialActor extends AbstractCollisionObserver implements IActor {

	private MaterialTemplate actionTemplate;
	private AtomicReference<MaterialSkill> currentSkill = new AtomicReference<>();
	private AtomicBoolean isCanceled = new AtomicBoolean();

	public CollisionMaterialActor(Creature creature, Spatial geometry, MaterialTemplate actionTemplate) {
		super(creature, geometry, CollisionIntention.MATERIAL.getId());
		this.actionTemplate = actionTemplate;
	}

	/**
	 * @param creature
	 * @param geometry
	 * @param template
	 */

	private MaterialSkill getSkillForTarget(Creature creature) {
		if (!creature.isSpawned())
			return null;

		if (creature instanceof Player) {
			Player player = (Player) creature;
			if (player.isProtectionActive())
				return null;
		}

		MaterialSkill foundSkill = null;
		for (MaterialSkill skill : actionTemplate.getSkills()) {
			if (skill.getTarget().isTarget(creature)) {
				foundSkill = skill;
				break;
			}
		}
		if (foundSkill == null)
			return null;

		if (creature.getEffectController().hasAbnormalEffect(foundSkill.getId()))
			return null;

		int weatherCode = -1;
		for (ZoneInstance regionZone : creature.findZones()) {
			if (regionZone.getZoneTemplate().getZoneType() == ZoneClassName.WEATHER) {
				Vector3f center = geometry.getWorldBound().getCenter();
				if (!regionZone.getAreaTemplate().isInside3D(center.x, center.y, center.z))
					continue;
				int weatherZoneId = DataManager.ZONE_DATA.getWeatherZoneId(regionZone.getZoneTemplate());
				weatherCode = WeatherService.getInstance().getWeatherCode(creature.getWorldId(), weatherZoneId);
				break;
			}
		}

		boolean dependsOnWeather = geometry.getName().indexOf("WEATHER") != -1;
		// TODO: fix it
		if (dependsOnWeather && weatherCode > 0)
			return null; // not active in any weather (usually, during rain and after rain, not before)

		if (foundSkill.getTime() == null)
			return foundSkill;

		if (foundSkill.getTime() == MaterialActTime.DAY && weatherCode == 0)
			return foundSkill; // Sunny day, according to client data

		if (GameTimeService.getInstance().getGameTime().getDayTime() == DayTime.NIGHT) {
			if (foundSkill.getTime() == MaterialActTime.NIGHT)
				return foundSkill;
		} else
			return foundSkill;

		return null;
	}

	@Override
	public void onMoved(CollisionResults collisionResults) {
		if (isCanceled.get())
			return;
		if (collisionResults.size() > 0) {
			if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
				Player player = (Player) creature;
				if (player.isStaff()) {
					CollisionResult result = collisionResults.getClosestCollision();
					PacketSendUtility.sendMessage(player, "Entered " + result.getGeometry().getName());
				}
			}
			act();
		}
	}

	@Override
	public void act() {
		MaterialSkill actSkill = getSkillForTarget(creature);
		if (actSkill != null && currentSkill.getAndSet(actSkill) != actSkill) {
			Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
				if (!creature.getEffectController().hasAbnormalEffect(actSkill.getId())) {
					if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
						Player player = (Player) creature;
						if (player.isStaff()) {
							PacketSendUtility.sendMessage(player, "Use skill=" + actSkill.getId());
						}
					}
					SkillEngine.getInstance().applyEffectDirectly(actSkill.getId(), creature, creature, 0);
				}
			}, 0, (long) (actSkill.getFrequency() * 1000));
			creature.getController().addTask(TaskId.MATERIAL_ACTION, task);
		}
	}

	@Override
	public void abort() {
		if (isCanceled.compareAndSet(false, true)) {
			creature.getController().cancelTask(TaskId.MATERIAL_ACTION);
			currentSkill.set(null);
		}
	}

	@Override
	public void died(Creature creature) {
		abort();
	}

	@Override
	public void setEnabled(boolean enable) {
	};
}
