package com.aionemu.gameserver.controllers.observer;

import java.util.Collections;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.world.geo.GeoService;


public class TerrainZoneCollisionMaterialActor extends AbstractMaterialSkillActor {

	public TerrainZoneCollisionMaterialActor(Creature creature) {
		super(creature, null, CollisionIntention.MATERIAL.getId(), CheckType.TOUCH, TaskId.TERRAIN_MATERIAL_ACTION, 0, Collections.emptyList());
	}

	@Override
	public void onMoved(CollisionResults collisionResults) {
	}

	@Override
	public void died(Creature creature) {
		super.died(creature);
		materialId = 0; 
	}

	@Override
	public void moved() {
		int matId = GeoService.getInstance().getTerrainMaterialAt(creature.getWorldId(), creature.getX(), creature.getY(), creature.getZ(), creature.getInstanceId());
		if (materialId == matId)
			return;
		materialId = matId;
		abort();
		MaterialTemplate template = matId == 0 ? null : DataManager.MATERIAL_DATA.getTemplate(matId);
		if (template != null) {
			skills = template.getSkills(creature);
			act();
		}
	}
}
