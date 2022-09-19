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
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonSkillAreaEffect")
public class SummonSkillAreaEffect extends SummonServantEffect {

	@Override
	public void applyEffect(Effect effect) {
		float x = effect.getX();
		float y = effect.getY();
		float z = effect.getZ();
		if (x == 0 && y == 0) {
			Creature effected = effect.getEffected();
			x = effected.getX();
			y = effected.getY();
			z = effected.getZ();
		}

		int tickDelay = 3000;
		int spawnDuration = time;

		String group = effect.getSkillTemplate().getGroup();
		if (group != null) {
			switch (group) {
				case "KN_THREATENINGWAVE" -> {
					spawnDuration = 15; // client files say 11s but description 15s
					tickDelay = 2000;
				}
				case "WI_SUMMONTORNADO" -> tickDelay = 1900;
				case "WI_DELAYEDSTRIKE" -> {
					tickDelay = 5000;
					spawnDuration = 9;
				}
			}
		}

		Servant servant = spawnServant(effect, spawnDuration, NpcObjectType.SKILLAREA, x, y, z);
		if (effect.getEffected() != null) // point skill without any initial target (we cannot trigger handleAttack with a null target)
			servant.getAi().onCreatureEvent(AIEventType.ATTACK, effect.getEffected());

		Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			private int skillPos = 0;

			@Override
			public void run() {
				servant.getController().useSkill(servant.getSkillList().getSkillOnPosition(skillPos).getSkillId());
				skillPos++;
			}
		}, 0, tickDelay);
		servant.getController().addTask(TaskId.SKILL_USE, task);
	}
}
