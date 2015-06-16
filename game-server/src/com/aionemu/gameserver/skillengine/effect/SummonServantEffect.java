package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai2.event.AIEventType;
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
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonServantEffect")
public class SummonServantEffect extends SummonEffect {

	private static final Logger log = LoggerFactory.getLogger(SummonServantEffect.class);

	@Override
	public void applyEffect(Effect effect) {
		Creature effector = effect.getEffector();
		float x = effector.getX();
		float y = effector.getY();
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

		final Creature target = (Creature) effector.getTarget();
		final Creature effected = (Creature) effect.getEffected();

		SkillTemplate template = effect.getSkillTemplate();

		if (template.getProperties().getFirstTarget() != FirstTargetAttribute.ME && target == null) {
			log.warn("Servant trying to attack null target!!");
			return null;
		}

		SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, x, y, z, heading);
		final Servant servant = VisibleObjectSpawner.spawnServant(spawn, instanceId, effector, effect.getSkillLevel(), npcObjectType);
		
		Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				servant.getController().onDelete();
			}
		}, time * 1000);
		servant.getController().addTask(TaskId.DESPAWN, task);
		if (servant.getNpcObjectType() != NpcObjectType.TOTEM)
			servant.getAi2().onCreatureEvent(AIEventType.ATTACK, (target != null ? target: effected));
		return servant;
	}
}
