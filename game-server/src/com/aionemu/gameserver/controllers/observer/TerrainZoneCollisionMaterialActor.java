package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.materials.MaterialSkill;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;


public class TerrainZoneCollisionMaterialActor extends AbstractCollisionObserver implements IActor {

	private final AtomicReference<MaterialSkill> currentSkill = new AtomicReference<>();
	private volatile Future<?> task;
	private boolean isTouched = false;
	private List<MaterialSkill> matchingSkills = new ArrayList<>();

	public TerrainZoneCollisionMaterialActor(Creature creature) {
		super(creature, null, CollisionIntention.MATERIAL.getId(), CheckType.TOUCH);
	}

	@Override
	public void onMoved(CollisionResults collisionResults) {

	}

	@Override
	public void moved() {
		if (GeoService.getInstance().worldHasTerrainMaterials(creature.getWorldId())) {
			int matId = GeoService.getInstance().getTerrainMaterialAt(creature.getWorldId(), creature.getX(), creature.getY(), creature.getZ(), creature.getInstanceId());
			if (matId > 0) {
				MaterialTemplate template = DataManager.MATERIAL_DATA.getTemplate(matId);
				if (template != null) {
					List<MaterialSkill> matchingSkills = new ArrayList<>();
					for (MaterialSkill skill : template.getSkills()) {
						if (skill.getTarget().matches(creature))
							matchingSkills.add(skill);
					}
					if (!matchingSkills.isEmpty()) {
						this.matchingSkills = matchingSkills;
						isTouched = true;
						act();
						return;
					}
				}
			}
		}
		this.matchingSkills.clear();
		isTouched = false;
		abort();
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
						PacketSendUtility.sendMessage(player, "Terrain use skill=" + skill.getId());
				}
				SkillEngine.getInstance().applyEffectDirectly(skill.getId(), skill.getSkillLevel(), creature, creature, null, Effect.ForceType.MATERIAL_SKILL);
			}, 0, (long) (skill.getFrequency() * 1000));
			creature.getController().addTask(TaskId.TERRAIN_MATERIAL_ACTION, task);
		}
	}

	@Override
	public void abort() {
		creature.getController().cancelTaskIfPresent(TaskId.TERRAIN_MATERIAL_ACTION, task);
		currentSkill.set(null);
	}

	@Override
	public void died(Creature creature) {
		abort();
	}
}
