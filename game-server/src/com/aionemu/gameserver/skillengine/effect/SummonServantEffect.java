package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.NpcObjectType;
import com.aionemu.gameserver.model.gameobjects.Servant;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.properties.FirstTargetAttribute;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonServantEffect")
public class SummonServantEffect extends SummonEffect {

	private final static int INITIAL_SPAWN_DELAY = 3000; // Seems to be around 2.5s

	@Override
	public void applyEffect(Effect effect) {
		Creature effector = effect.getEffector();
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(effect.getEffector().getHeading()));
		float x = effector.getX() + (float) (Math.cos(radian) * 2);
		float y = effector.getY() + (float) (Math.sin(radian) * 2);
		Vector3f pos = GeoService.getInstance().getClosestCollision(effector, x, y, effector.getZ(), true, CollisionIntention.DEFAULT_COLLISIONS.getId(),
			IgnoreProperties.of(effector.getRace()));
		Servant servant = spawnServant(effect, time, NpcObjectType.SERVANT, pos.getX(), pos.getY(), pos.getZ());
		servant.getAi().onCreatureEvent(AIEventType.ATTACK, effect.getEffected());
	}

	protected Servant spawnServant(Effect effect, int spawnDuration, NpcObjectType npcObjectType, float x, float y, float z) {
		Creature effector = effect.getEffector();
		if (effect.getEffected() == null && effect.getSkillTemplate().getProperties().getFirstTarget() != FirstTargetAttribute.POINT)
			throw new IllegalArgumentException("Servant " + npcId + "cannot be spawned by " + effector + " (target: null)");

		SpawnTemplate spawn = SpawnEngine.newSingleTimeSpawn(effector.getWorldId(), npcId, x, y, z, effector.getHeading());
		final Servant servant = VisibleObjectSpawner.spawnServant(spawn, effector.getInstanceId(), effector, effect.getSkillLevel(), npcObjectType);

		Future<?> task = ThreadPoolManager.getInstance().schedule(() -> servant.getController().delete(), spawnDuration * 1000L + INITIAL_SPAWN_DELAY);
		servant.getController().addTask(TaskId.DESPAWN, task);
		return servant;
	}
}
