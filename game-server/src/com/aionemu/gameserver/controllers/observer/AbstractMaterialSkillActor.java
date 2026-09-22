package com.aionemu.gameserver.controllers.observer;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
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
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats, Neon
 */
public abstract class AbstractMaterialSkillActor extends AbstractCollisionObserver {

	private final AtomicReference<Future<?>> task = new AtomicReference<>();
	private final TaskId taskId;
	protected volatile int materialId;
	protected volatile List<MaterialSkill> skills;
	protected volatile boolean isTouched = false;

	public AbstractMaterialSkillActor(Creature creature, Spatial geometry, byte intentions, CheckType checkType, TaskId taskId, int materialId,
		List<MaterialSkill> skills) {
		super(creature, geometry, intentions, checkType, ObserverType.DEATH);
		this.taskId = taskId;
		this.materialId = materialId;
		this.skills = skills;
	}

	public static boolean hasSkillFor(Creature creature, List<MaterialSkill> skills) {
		for (MaterialSkill skill : skills) {
			if (skill.getTarget().matches(creature))
				return true;
		}
		return false;
	}

	public void act() {
		if (!skills.isEmpty() && !creature.getController().hasTask(taskId)) {
			Future<?> t = ThreadPoolManager.getInstance().scheduleAtFixedRate(new MaterialSkillTask(), 0, 1000);
			if (task.compareAndSet(null, t))
				creature.getController().addTask(taskId, task.get());
			else // should not happen
				t.cancel(false);
		}
	}

	public void abort() {
		Future<?> t = task.getAndSet(null);
		if (t != null)
			creature.getController().cancelTaskIfPresent(taskId, t);
	}

	@Override
	public void died(Creature creature) {
		isTouched = false;
		abort();
	}

	private boolean matchActConditions(MaterialSkill skill) {
		for (MaterialActCondition condition : skill.getConditions()) {
			if (condition == MaterialActCondition.NIGHT) {
				if (!GameTimeService.getInstance().getGameTime().isNight())
					return false;
			} else if (condition == MaterialActCondition.SUNNY) { // sunny actually means "not raining" (fireplaces don't burn during rain)
				WeatherEntry weatherEntry = WeatherService.getInstance().findWeatherEntry(creature);
				boolean isRain = weatherEntry.getWeatherName() != null && weatherEntry.getWeatherName().startsWith("RAIN");
				if (isRain && !weatherEntry.isBefore()) // before means "before" the weather (e.g. clouds before rain)
					return false;
			}
		}
		return true;
	}

	private class MaterialSkillTask implements Runnable {

		@Override
		public void run() {
			if (!isTouched)
				return;
			if (!creature.isSpawned() || creature.isDead())
				return;
			if (creature instanceof Player player && (player.isInFlyingState() || player.isUsingFlightTransporterOrWindstream()))
				return;
			int materialId = AbstractMaterialSkillActor.this.materialId;
			List<MaterialSkill> skills = AbstractMaterialSkillActor.this.skills;
			MaterialSkillUsage usage = creature.getController().getMaterialSkillUsage();
			for (int slot = 0; slot < skills.size(); slot++) {
				MaterialSkill skill = skills.get(slot);
				if (!skill.getTarget().matches(creature) || !matchActConditions(skill) || !usage.tryUse(materialId, slot, skill.getFrequency()))
					continue;
				if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player player && player.isStaff())
					PacketSendUtility.sendMessage(player, AbstractMaterialSkillActor.this.getClass().getSimpleName() + " use skill=" + skill.getId());
				SkillEngine.getInstance().applyEffectDirectly(skill.getId(), skill.getSkillLevel(), creature, creature, null, Effect.ForceType.MATERIAL_SKILL);
			}
		}
	}
}
