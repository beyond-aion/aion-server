package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Trap;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.summons.TrapService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonTrapEffect")
public class SummonTrapEffect extends SummonEffect {

	@Override
	public void applyEffect(Effect effect) {
		Creature effector = effect.getEffector();
		// should only be set if player has no target to avoid errors
		if (effect.getEffector().getTarget() == null)
			effect.getEffector().setTarget(effect.getEffector());
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(effect.getEffector().getHeading()));
		float x = effect.getX();
		float y = effect.getY();
		float z = effect.getZ();
		if (effect.getSkill().isFirstTargetSelf()) {
			Creature effected = effect.getEffected();
			Vector3f pos = GeoService.getInstance().getClosestCollision(effector, effected.getX() + (float) (Math.cos(radian) * 2), effected.getY() + (float) (Math.sin(radian) * 2), effected.getZ(), true, CollisionIntention.DEFAULT_COLLISIONS.getId(), IgnoreProperties.of(effector.getRace()));
			x = pos.getX();
			y = pos.getY();
			z = pos.getZ();
		}
		byte heading = effector.getHeading();
		int worldId = effector.getWorldId();
		int instanceId = effector.getInstanceId();

		SpawnTemplate spawn = SpawnEngine.newSingleTimeSpawn(worldId, npcId, x, y, z, heading);
		Trap trap = VisibleObjectSpawner.spawnTrap(spawn, instanceId, effector);
		TrapService.registerTrap(effector.getObjectId(), trap, true);
		trap.getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> trap.getController().delete(), time * 1000L));
	}
}
