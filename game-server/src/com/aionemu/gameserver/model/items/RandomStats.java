package com.aionemu.gameserver.model.items;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;

/**
 *
 * @author xTz
 */
public class RandomStats {

	private final RandomBonusEffect rndBonusEffect;

	public RandomStats(int setId, int setNumber) {
		rndBonusEffect = new RandomBonusEffect(StatBonusType.INVENTORY, setId, setNumber);
	}

	public void onEquip(final Player player) {
		rndBonusEffect.applyEffect(player);
	}

	public void onUnEquip(Player player) {
		rndBonusEffect.endEffect(player);
	}
}
