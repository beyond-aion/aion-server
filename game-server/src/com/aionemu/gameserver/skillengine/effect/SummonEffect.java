package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Simple
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonEffect")
public class SummonEffect extends EffectTemplate {

	@XmlAttribute(name = "npc_id", required = true)
	protected int npcId;
	@XmlAttribute(name = "time", required = true)
	protected int time; // in seconds

	@Override
	public void applyEffect(Effect effect) {
		Player effected = (Player) effect.getEffected();
		SummonsService.createSummon(effected, npcId, effect.getSkillId(), effect.getSkillLevel(), time);
		if (time > 0 && effect.getEffected() instanceof Player) {
			final Player effector = (Player) effect.getEffected();
			final Summon summon = effector.getSummon();
			Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (summon != null && summon.isSpawned()) {
						SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.UNSPECIFIED);
					}
				}
			}, time * 1000);
			summon.getController().addTask(TaskId.DESPAWN, task);
			effected.getEffectController().removePetOrderUnSummonEffects();
		}
	}

	@Override
	public void calculate(Effect effect) {
		effect.addSucessEffect(this);
	}
}
