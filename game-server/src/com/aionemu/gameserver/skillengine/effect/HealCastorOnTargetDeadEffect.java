package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;
import com.aionemu.gameserver.utils.MathUtil;

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
	public void calculate(Effect effect) {
		if (effect.getEffected() instanceof Player)
			super.calculate(effect, null, null);
	}

	@Override
	public void startEffect(final Effect effect) {
		super.startEffect(effect);

		final Player player = (Player) effect.getEffector();
		final int valueWithDelta = value + delta * effect.getSkillLevel();

		ActionObserver observer = new ActionObserver(ObserverType.DEATH) {

			@Override
			public void died(Creature creature) {
				// Heal Caster first
				if (MathUtil.isIn3dRange(effect.getEffected(), player, range))
					player.getLifeStats().increaseHp(TYPE.HP, valueWithDelta, effect.getSkillId(), LOG.REGULAR);
				// Then check for party if healparty parameter is set
				if (healparty) {
					if (player.getPlayerGroup2() != null) {
						for (Player p : player.getPlayerGroup2().getMembers()) {
							if (p == player)
								continue;
							if (MathUtil.isIn3dRange(effect.getEffected(), p, range))
								player.getLifeStats().increaseHp(TYPE.HP, valueWithDelta, effect.getSkillId(), LOG.REGULAR);
						}
					} else if (player.isInAlliance2()) {
						for (Player p : player.getPlayerAllianceGroup2().getMembers()) {
							if (!p.isOnline())
								continue;
							if (p.equals(player))
								continue;
							if (MathUtil.isIn3dRange(effect.getEffected(), p, range))
								player.getLifeStats().increaseHp(TYPE.HP, valueWithDelta, effect.getSkillId(), LOG.REGULAR);
						}
					}
				}
			}
		};

		effect.getEffected().getObserveController().addObserver(observer);
		effect.setActionObserver(observer, position);
	}

	@Override
	public void endEffect(Effect effect) {
		super.endEffect(effect);
		ActionObserver observer = effect.getActionObserver(position);
		if ((!effect.getEffected().getLifeStats().isAlreadyDead()) && (observer != null))
			effect.getEffected().getObserveController().removeObserver(observer);
	}
}
