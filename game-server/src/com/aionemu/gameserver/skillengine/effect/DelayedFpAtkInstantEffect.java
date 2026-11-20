package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author kecimis
 */
public class DelayedFpAtkInstantEffect extends EffectTemplate {

	@XmlAttribute
	protected int delay;
	@XmlAttribute
	protected boolean percent;

	@Override
	public void calculate(Effect effect) {
		if (getPlayerOrSummonMaster(effect.getEffected()) != null)
			super.calculate(effect, null, null);
	}

	@Override
	public void applyEffect(Effect effect) {
		ThreadPoolManager.getInstance().schedule(() -> calculateAndApplyDamage(effect), delay);
	}

	private void calculateAndApplyDamage(Effect effect) {
		if (!effect.getEffector().isEnemy(effect.getEffected()))
			return;
		Player owner = getPlayerOrSummonMaster(effect.getEffected());
		if (owner == null)
			return;
		int baseValue = calculateBaseValue(effect);
		int amount = percent ? owner.getLifeStats().getMaxFp() * baseValue / 100 : baseValue;
		owner.getLifeStats().reduceFp(TYPE.DELAYED_FP_DAMAGE, amount, effect.getSkillId(), LOG.FPATTACK);
	}
}
