package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HealCastorOnTargetDeadEffect")
public class HealCastorOnTargetDeadEffect extends EffectTemplate {

	@XmlAttribute
	protected HealType type;// useless
	@XmlAttribute
	protected float range;
	@XmlAttribute
	protected boolean healparty;

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void endEffect(Effect effect) {
		Creature effected = effect.getEffected();
		Creature effector = effect.getEffector();
		if (effected.isDead()) {
			int healValue = calculateBaseValue(effect);
			var group = healparty && effector instanceof Player p ? p.getCurrentGroup() : null;
			if (group == null) {
				if (PositionUtil.isInRange(effected, effector, range, false))
					effector.getLifeStats().increaseHp(TYPE.HP, healValue, effect, LOG.REGULAR);
			} else {
				for (Player p : group.getOnlineMembers()) {
					if (PositionUtil.isInRange(effected, p, range, false))
						effector.getLifeStats().increaseHp(TYPE.HP, healValue, effect, LOG.REGULAR);
				}
			}
		}
	}
}
