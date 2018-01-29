package com.aionemu.gameserver.controllers.observer;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.materials.MaterialActCondition;
import com.aionemu.gameserver.model.templates.materials.MaterialSkill;
import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.gametime.DayTime;

/**
 * @author Rolandas
 */
public class CollisionMaterialActor extends AbstractCollisionObserver implements IActor {

	private final AtomicReference<MaterialSkill> currentSkill = new AtomicReference<>();
	private final AtomicBoolean isCanceled = new AtomicBoolean();
	private final List<MaterialSkill> matchingSkills;
	private volatile Future<?> task;
	private boolean isTouched = false;

	public CollisionMaterialActor(Creature creature, Spatial geometry, List<MaterialSkill> matchingSkills) {
		super(creature, geometry, CollisionIntention.MATERIAL.getId(), CheckType.TOUCH);
		this.matchingSkills = matchingSkills;

	}

	@Override
	public void onMoved(CollisionResults collisionResults) {
		if (isCanceled.get())
			return;
		boolean oldTouched = isTouched;
		if (collisionResults.size() > 0) {
			isTouched = true;
			act();
		} else {
			isTouched = false;
		}
		if (oldTouched != isTouched) {
			if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
				Player player = (Player) creature;
				if (player.isStaff()) {
					Spatial geom = collisionResults.size() > 0 ? collisionResults.getClosestCollision().getGeometry() : geometry;
					PacketSendUtility.sendMessage(player, (isTouched ? "Touched " : "Untouched ") + geom.getName());
				}
			}
		}
	}

	@Override
	public void act() {
		MaterialSkill skill = findFirstSkillWithMatchingCondition(creature);
		if (skill != null && currentSkill.getAndSet(skill) != skill) {
			task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
				if (!isTouched)
					return;
				if (!creature.isSpawned())
					return;
				if (creature instanceof Player && ((Player) creature).isProtectionActive())
					return;
				if (creature.getEffectController().hasAbnormalEffect(skill.getId()))
					return;
				if (System.currentTimeMillis() - creature.getMoveController().getLastMoveUpdate() > 5000
					&& findFirstSkillWithMatchingCondition(creature) != currentSkill.get()) { // some conditions changed, reevaluate skill to apply
					act();
					return;
				}
				if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
					Player player = (Player) creature;
					if (player.isStaff())
						PacketSendUtility.sendMessage(player, "Use skill=" + skill.getId());
				}
				SkillEngine.getInstance().applyEffectDirectly(skill.getId(), skill.getSkillLevel(), creature, creature);
			}, 0, (long) (skill.getFrequency() * 1000));
			creature.getController().addTask(TaskId.MATERIAL_ACTION, task);
		}
	}

	private MaterialSkill findFirstSkillWithMatchingCondition(Creature creature) {
		for (MaterialSkill skill : matchingSkills) {
			if (matchActConditions(skill))
				return skill;
		}
		return null;
	}

	private boolean matchActConditions(MaterialSkill skill) {
		if (skill.getConditions().isEmpty())
			return true;
		for (MaterialActCondition condition : skill.getConditions()) {
			if (condition == MaterialActCondition.NIGHT && GameTimeService.getInstance().getGameTime().getDayTime() == DayTime.NIGHT)
				return true;
			if (condition == MaterialActCondition.SUNNY) { // sunny actually means "not raining" (fireplaces don't burn during rain)
				WeatherEntry weatherEntry = WeatherService.getInstance().findWeatherEntry(creature);
				boolean isRain = weatherEntry.getWeatherName() != null && weatherEntry.getWeatherName().startsWith("RAIN");
				if (!isRain || weatherEntry.isBefore()) // before means "before" the weather (e.g. clouds before rain)
					return true;
			}
		}
		return false;
	}

	@Override
	public void abort() {
		if (isCanceled.compareAndSet(false, true)) {
			creature.getController().cancelTaskIfPresent(TaskId.MATERIAL_ACTION, task);
			currentSkill.set(null);
		}
	}

	@Override
	public void died(Creature creature) {
		abort();
	}
}
