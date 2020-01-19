package com.aionemu.gameserver.controllers.observer;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.materials.MaterialSkill;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
public class ZoneCollisionMaterialActor extends AbstractCollisionObserver implements IActor {

	private final AtomicReference<MaterialSkill> currentSkill = new AtomicReference<>();
	private final AtomicBoolean isCanceled = new AtomicBoolean();
	private final List<MaterialSkill> matchingSkills;
	private volatile Future<?> task;
	private boolean isTouched = false;

	public ZoneCollisionMaterialActor(Creature creature, Spatial geometry, List<MaterialSkill> matchingSkills, CheckType checkType, Vector3f initialPosition) {
		super(creature, geometry, CollisionIntention.MATERIAL.getId(), checkType, initialPosition);
		this.matchingSkills = matchingSkills;
	}

	public ZoneCollisionMaterialActor(Creature creature, Spatial geometry, List<MaterialSkill> matchingSkills, Vector3f initialPosition) {
		this(creature, geometry, matchingSkills, CheckType.TOUCH, initialPosition);
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
			abort();
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
		MaterialSkill skill = findFirstSkillWithMatchingCondition(matchingSkills, creature);
		if (skill != null && currentSkill.getAndSet(skill) != skill) {
			task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
				if (!isTouched)
					return;
				if (!creature.isSpawned() || creature.isDead())
					return;
				if (creature instanceof Player && ((Player) creature).isProtectionActive())
					return;
				if (System.currentTimeMillis() - creature.getMoveController().getLastMoveUpdate() > 5000
					&& findFirstSkillWithMatchingCondition(matchingSkills, creature) != currentSkill.get()) { // some conditions changed, reevaluate skill to apply
					act();
					return;
				}
				if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
					Player player = (Player) creature;
					if (player.isStaff())
						PacketSendUtility.sendMessage(player, "Zone use skill=" + skill.getId());
				}
				SkillEngine.getInstance().applyEffectDirectly(skill.getId(), skill.getSkillLevel(), creature, creature, null, ForceType.MATERIAL_SKILL);
			}, 0, (long) (skill.getFrequency() * 1000));
			creature.getController().addTask(TaskId.ZONE_MATERIAL_ACTION, task);
		}
	}

	@Override
	public void abort() {
		if (isCanceled.compareAndSet(false, true)) {
			creature.getController().cancelTaskIfPresent(TaskId.ZONE_MATERIAL_ACTION, task);
			currentSkill.set(null);
			isCanceled.set(false);
		}
	}

	@Override
	public void died(Creature creature) {
		abort();
	}
}
