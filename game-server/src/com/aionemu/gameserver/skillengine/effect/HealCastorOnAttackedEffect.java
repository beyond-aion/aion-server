package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;
import com.aionemu.gameserver.utils.PositionUtil;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HealCastorOnAttackedEffect")
public class HealCastorOnAttackedEffect extends EffectTemplate {

	@XmlAttribute
	protected HealType type;// useless
	@XmlAttribute
	protected float range;

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(final Effect effect) {
		effect.addObserver(effect.getEffected(), new ActionObserver(ObserverType.ATTACKED) {

			@Override
			public void attacked(Creature creature, int skillId) {
				Creature effector = effect.getEffector();
				var group = effector instanceof Player p ? p.getCurrentGroup() : null;
				int healValue = calculateBaseValue(effect);
				if (group == null) {
					if (PositionUtil.isInRange(effect.getEffected(), effector, range, false))
						effector.getLifeStats().increaseHp(TYPE.HP, healValue, effect, LOG.REGULAR);
				} else {
					for (Player p : group.getOnlineMembers()) {
						if (PositionUtil.isInRange(effect.getEffected(), p, range, false))
							p.getLifeStats().increaseHp(TYPE.HP, healValue, effect, LOG.REGULAR);
					}
				}
			}
		});
	}
}
