package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author kecimis
 */
public class DelayedFpAtkInstantEffect extends EffectTemplate implements DelayedAttackEffect {

	@XmlAttribute
	protected int delay;
	@XmlAttribute(name = "delaydelta")
	protected int delayDelta;
	@XmlAttribute
	protected boolean percent;

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, null, null);
	}

	@Override
	public void applyEffect(final Effect effect) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				calculateAndApplyDamage(effect);
			}
		}, getRemainingDelay(effect));
	}

	@Override
	public int getDelay(int skillLevel) {
		return DelayedAttackEffect.calculateDelay(delay, delayDelta, skillLevel);
	}

	private void calculateAndApplyDamage(Effect effect) {
		if (!(effect.getEffected() instanceof Player player) || !effect.getEffector().isEnemy(player))
			return;
		int valueWithDelta = calculateBaseValue(effect);
		int maxFP = player.getLifeStats().getMaxFp();

		int newValue = valueWithDelta;
		// Support for values in percentage
		if (percent)
			newValue = (maxFP * valueWithDelta) / 100;

		player.getLifeStats().reduceFp(TYPE.FP_DAMAGE, newValue, effect.getSkillId(), LOG.FPATTACK);
	}

}
