package com.aionemu.gameserver.model.items;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author xTz
 */
public class RandomBonusEffect implements StatOwner {

	private final ModifiersTemplate template;

	public RandomBonusEffect(StatBonusType type, int polishSetId, int polishNumber) {
		template = DataManager.ITEM_RANDOM_BONUSES.getTemplate(type, polishSetId, polishNumber);
	}

	public void applyEffect(Player player) {
		player.getGameStats().addEffect(this, template.getModifiers());
	}

	public void endEffect(Player player) {
		player.getGameStats().endEffect(this);
	}
}
