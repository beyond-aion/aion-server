package com.aionemu.gameserver.model.items;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;

/**
 * @author xTz
 */
public class RandomBonusEffect implements StatOwner {

	private final int statBonusId;
	private final List<StatFunction> stats;

	public RandomBonusEffect(StatBonusType type, int statBonusSetId, int statBonusId) {
		this.statBonusId = statBonusId;
		this.stats = DataManager.ITEM_RANDOM_BONUSES.getTemplate(type, statBonusSetId, statBonusId).getModifiers();
	}

	public int getStatBonusId() {
		return statBonusId;
	}

	public void applyEffect(Player player) {
		player.getGameStats().addEffect(this, stats);
	}

	public void endEffect(Player player) {
		player.getGameStats().endEffect(this);
	}
}
