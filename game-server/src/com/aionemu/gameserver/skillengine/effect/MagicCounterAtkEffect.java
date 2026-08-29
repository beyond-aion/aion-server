package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.Skill.SkillMethod;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MagicCounterAtkEffect")
public class MagicCounterAtkEffect extends EffectTemplate {

	@XmlAttribute
	protected int maxdmg;
	
	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(final Effect effect) {
		Creature effected = effect.getEffected();
		effect.addObserver(effected, new ActionObserver(ObserverType.ENDSKILLCAST) {

			@Override
			public void endSkillCast(Skill skill) {
				if (skill.getSkillMethod() != SkillMethod.ITEM && skill.getSkillTemplate().getType() == SkillType.MAGICAL) {
					float maxHpDamage = effected.getGameStats().getMaxHp().getBase() * calculateBaseValue(effect) / 100f;

					float adjustedDamage = StatFunctions.adjustDamageByPvpOrPveModifiers(effect.getEffector(), effect.getEffected(), maxHpDamage,
						effect.getSkillTemplate().getPvpDamage(), false, element);

					int finalDamage = (int) Math.min(maxdmg, adjustedDamage);

					effected.getController().onAttack(effect, TYPE.MAGICCOUNTERATK, finalDamage, true, LOG.MAGICCOUNTERATK, hopType);
				}
			}
		});
	}
}
