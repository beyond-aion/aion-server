package com.aionemu.gameserver.controllers.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.templates.materials.MaterialSkill;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.world.geo.GeoService;


public class TerrainZoneCollisionMaterialActor extends AbstractMaterialSkillActor {

	private volatile int lastMatId = 0;

	public TerrainZoneCollisionMaterialActor(Creature creature) {
		super(creature, null, CollisionIntention.MATERIAL.getId(), CheckType.TOUCH, TaskId.TERRAIN_MATERIAL_ACTION, Collections.emptyList());
	}

	@Override
	public void onMoved(CollisionResults collisionResults) {
	}

	@Override
	public void moved() {
		if (GeoService.getInstance().worldHasTerrainMaterials(creature.getWorldId())) {
			int matId = GeoService.getInstance().getTerrainMaterialAt(creature.getWorldId(), creature.getX(), creature.getY(), creature.getZ(), creature.getInstanceId());
			if (matId != lastMatId || !isTouched) {
				lastMatId = matId;
				isTouched = true;
				MaterialTemplate template = matId == 0 ? null : DataManager.MATERIAL_DATA.getTemplate(matId);
				if (template != null) {
					List<MaterialSkill> matchingSkills = new ArrayList<>();
					for (MaterialSkill skill : template.getSkills()) {
						if (skill.getTarget().matches(creature))
							matchingSkills.add(skill);
					}
					if (!matchingSkills.isEmpty()) {
						skills = matchingSkills;
						act();
						return;
					}
				}
			}
		}
		if (!skills.isEmpty()) {
			synchronized (skills) {
				skills.clear();
				isTouched = false;
				abort();
			}
		}
	}
}
