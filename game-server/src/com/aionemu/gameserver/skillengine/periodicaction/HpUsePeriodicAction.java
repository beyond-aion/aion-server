package com.aionemu.gameserver.skillengine.periodicaction;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author antness
 */
public class HpUsePeriodicAction extends PeriodicAction {

	@XmlAttribute(name = "value")
	protected int value;
	@XmlAttribute(name = "delta")
	protected int delta;
	@XmlAttribute
	protected boolean ratio;

	@Override
	public void act(Effect effect) {
		Creature effected = effect.getEffected();
		int maxHp = effected.getGameStats().getMaxHp().getCurrent();
		int requiredHp = ratio ? (int) (maxHp * (value / 100f)) : value;
		if (effected.getLifeStats().getCurrentHp() < requiredHp) {
			effect.endEffect();
			return;
		}
		effected.getLifeStats().reduceHp(SM_ATTACK_STATUS.TYPE.USED_HP, requiredHp, 0, LOG.REGULAR, effected);
	}

}
