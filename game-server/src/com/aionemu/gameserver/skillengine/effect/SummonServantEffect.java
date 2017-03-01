package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.NpcObjectType;
import com.aionemu.gameserver.model.gameobjects.Servant;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.properties.FirstTargetAttribute;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonServantEffect")
public class SummonServantEffect extends SummonEffect {

	@Override
	public void applyEffect(Effect effect) {
		Creature effector = effect.getEffector();
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(effect.getEffector().getHeading()));
		float x = effector.getX() + (float) (Math.cos(radian) * 2);
		float y = effector.getY() + (float) (Math.sin(radian) * 2);
		float z = effector.getZ();
		spawnServant(effect, time, NpcObjectType.SERVANT, x, y, z);
	}

	/**
	 * @param effect
	 * @param time
	 */
	protected Servant spawnServant(Effect effect, int time, NpcObjectType npcObjectType, float x, float y, float z) {
		Creature effector = effect.getEffector();
		byte heading = effector.getHeading();
		int worldId = effector.getWorldId();
		int instanceId = effector.getInstanceId();

		final Creature target = effector.getTarget() instanceof Creature ? (Creature) effector.getTarget() : null; // null in case StaticDoor, Pet, ...
		final Creature effected = effect.getEffected();

		SkillTemplate template = effect.getSkillTemplate();

		if (target == null && template.getProperties().getFirstTarget() != FirstTargetAttribute.POINT
			&& template.getProperties().getFirstTarget() != FirstTargetAttribute.ME)
			throw new IllegalArgumentException("Servant " + npcId + "cannot be spawned by " + effector + " (target = null)");

		SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, x, y, z, heading);
		final Servant servant = VisibleObjectSpawner.spawnServant(spawn, instanceId, effector, effect.getSkillLevel(), npcObjectType);

		Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				servant.getController().delete();
			}
		}, (time + 5) * 1000);
		servant.getController().addTask(TaskId.DESPAWN, task);
		if (servant.getNpcObjectType() != NpcObjectType.TOTEM)
			servant.getAi().onCreatureEvent(AIEventType.ATTACK, (target != null ? target : effected));
		return servant;
	}
}
