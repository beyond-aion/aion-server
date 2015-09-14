package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

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
		// should only be set if player has no target to avoid errors
		if (effect.getEffector().getTarget() == null)
			effect.getEffector().setTarget(effect.getEffector());
		float x = effect.getX();
		float y = effect.getY();
		float z = effect.getZ();
		if (x == 0 && y == 0) {
			Creature effected = effect.getEffected();
			x = effected.getX();
			y = effected.getY();
			z = effected.getZ();
		}
		// fix for summon whirlwind
		// TODO revisit later and find better fix - kecimis
		int useTime = time;
		switch (effect.getSkillId()) {
			case 2291:
			case 2292:
			case 2293:
			case 2294:
				useTime = 7;
				break;
			case 2711:
			case 2712:
				useTime = 10;
				break;
		}

		final Servant servant = spawnServant(effect, useTime, NpcObjectType.SKILLAREA, x, y, z);

		int delay = 3000;
		if (effect.getSkillTemplate().getGroup() != null && effect.getSkillTemplate().getGroup().equalsIgnoreCase("KN_THREATENINGWAVE"))
			delay = 2000;
		Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			int position = 1;

			@Override
			public void run() {
				servant.getController().useSkill(servant.getSkillList().getSkillOnPosition(position).getSkillId());
				position++;
			}
		}, 0, delay);
		servant.getController().addTask(TaskId.SKILL_USE, task);
	}
}
