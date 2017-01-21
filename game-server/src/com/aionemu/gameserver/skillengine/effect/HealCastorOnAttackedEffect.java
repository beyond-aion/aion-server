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
	public void calculate(Effect effect) {
		if (effect.getEffected() instanceof Player)
			super.calculate(effect, null, null);
	}

	@Override
	public void startEffect(final Effect effect) {
		super.startEffect(effect);

		final Player player = (Player) effect.getEffector();
		final int valueWithDelta = value + delta * effect.getSkillLevel();

		ActionObserver observer = new ActionObserver(ObserverType.ATTACKED) {

			@Override
			public void attacked(Creature creature, int skillId) {
				if (player.getPlayerGroup() != null) {
					for (Player p : player.getPlayerGroup().getMembers()) {
						if (MathUtil.isIn3dRange(effect.getEffected(), p, range))
							p.getLifeStats().increaseHp(TYPE.HP, valueWithDelta, effect.getSkillId(), LOG.REGULAR);
					}
				} else if (player.isInAlliance()) {
					for (Player p : player.getPlayerAllianceGroup().getMembers()) {
						if (!p.isOnline())
							continue;
						if (MathUtil.isIn3dRange(effect.getEffected(), p, range))
							p.getLifeStats().increaseHp(TYPE.HP, valueWithDelta, effect.getSkillId(), LOG.REGULAR);
					}
				} else {
					if (MathUtil.isIn3dRange(effect.getEffected(), player, range))
						effect.getEffected().getLifeStats().increaseHp(TYPE.HP, valueWithDelta, effect.getSkillId(), LOG.REGULAR);
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
		if (observer != null)
			effect.getEffected().getObserveController().removeObserver(observer);
	}
}
