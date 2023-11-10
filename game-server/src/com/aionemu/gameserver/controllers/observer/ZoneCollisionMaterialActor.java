package com.aionemu.gameserver.controllers.observer;

import java.util.List;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.materials.MaterialSkill;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class ZoneCollisionMaterialActor extends AbstractMaterialSkillActor {

	public ZoneCollisionMaterialActor(Creature creature, Spatial geometry, List<MaterialSkill> matchingSkills, CheckType checkType) {
		super(creature, geometry, CollisionIntention.MATERIAL.getId(), checkType, TaskId.ZONE_MATERIAL_ACTION, matchingSkills);
	}

	@Override
	public void onMoved(CollisionResults collisionResults) {
		boolean oldTouched = isTouched;
		isTouched = collisionResults.size() > 0;
		if (oldTouched != isTouched) {
			if (isTouched)
				act();
			else
				abort();
			if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player player && player.isStaff()) {
				Spatial geom = collisionResults.size() > 0 ? collisionResults.getClosestCollision().getGeometry() : geometry;
				PacketSendUtility.sendMessage(player, (isTouched ? "Touched " : "Untouched ") + geom.getName());
			}
		}
	}
}
