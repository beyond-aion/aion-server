package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpellAtkDrainInstantEffect")
public class SpellAtkDrainInstantEffect extends DamageEffect {

	@XmlAttribute(name = "hp_percent")
	private int hpPercent;
	@XmlAttribute(name = "mp_percent")
	private int mpPercent;


	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect);
		ThreadPoolManager.getInstance().schedule(() -> {
			if (hpPercent != 0) {
				effect.getEffector().getLifeStats().increaseHp(TYPE.HP, effect.getReserveds(position).getValue() * hpPercent / 100, effect,
						LOG.SPELLATKDRAININSTANT);
			}
			if (mpPercent != 0) {
				effect.getEffector().getLifeStats().increaseMp(TYPE.ABSORBED_MP, effect.getReserveds(position).getValue() * mpPercent / 100,
						effect.getSkillId(), LOG.SPELLATKDRAININSTANT);
			}
		}, 1000); // on retail the effect is applied about 1sec later (maybe based on animationTime/hitTime?)
	}
}
