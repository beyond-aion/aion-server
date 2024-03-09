package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Homing;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonHomingEffect")
public class SummonHomingEffect extends SummonEffect {

	@XmlAttribute(name = "npc_count", required = true)
	protected int npcCount;
	@XmlAttribute(name = "attack_count", required = true)
	protected int attackCount;

	@Override
	public void applyEffect(Effect effect) {
		Creature effector = effect.getEffector();
		float x = effector.getX();
		float y = effector.getY();
		float z = effector.getZ();
		byte heading = effector.getHeading();
		int worldId = effector.getWorldId();
		int instanceId = effector.getInstanceId();

		for (int i = 0; i < npcCount; i++) {
			SpawnTemplate spawn = SpawnEngine.newSingleTimeSpawn(worldId, npcId, x, y, z, heading);
			final Homing homing = VisibleObjectSpawner.spawnHoming(spawn, instanceId, effector, attackCount, effect.getSkillId());

			if (attackCount > 0) {
				effect.addObserver(homing, new ActionObserver(ObserverType.ATTACK) {

					@Override
					public void attack(Creature creature, int skillId) {
						homing.setAttackCount(homing.getAttackCount() - 1);
						if (homing.getAttackCount() <= 0)
							homing.getController().delete();
					}
				});
			}
			// Schedule a despawn just in case
			Future<?> task = ThreadPoolManager.getInstance().schedule(() -> homing.getController().delete(), 15 * 1000);
			homing.getController().addTask(TaskId.DESPAWN, task);
			homing.getAi().onCreatureEvent(AIEventType.ATTACK, effect.getEffected());
		}
	}

}
