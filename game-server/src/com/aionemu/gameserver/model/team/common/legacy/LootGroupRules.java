package com.aionemu.gameserver.model.team.common.legacy;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.player.InRoll;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.services.drop.DropDistributionService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer, xTz
 */
public class LootGroupRules {

	private final LootRuleType lootRule;
	private int misc;
	private final int common_item_above;
	private final int superior_item_above;
	private final int heroic_item_above;
	private final int fabled_item_above;
	private final int eternal_item_above;
	private final int mythic_item_above;
	private int nrMisc;
	private int nrRoundRobin;
	private final Deque<DropItem> itemsToBeDistributed = new ConcurrentLinkedDeque<>();

	public LootGroupRules() {
		lootRule = LootRuleType.ROUNDROBIN;
		common_item_above = 0;
		superior_item_above = 2;
		heroic_item_above = 2;
		fabled_item_above = 2;
		eternal_item_above = 2;
		mythic_item_above = 2;
	}

	public LootGroupRules(LootRuleType lootRule, int misc, int commonItemAbove, int superiorItemAbove, int heroicItemAbove, int fabledItemAbove,
		int eternalItemAbove, int mythicItemAbove) {
		super();
		this.lootRule = lootRule;
		this.misc = misc;
		common_item_above = commonItemAbove;
		superior_item_above = superiorItemAbove;
		heroic_item_above = heroicItemAbove;
		fabled_item_above = fabledItemAbove;
		eternal_item_above = eternalItemAbove;
		mythic_item_above = mythicItemAbove;
	}

	public boolean getQualityRule(ItemQuality quality) {
		switch (quality) {
			case COMMON: // White
				return common_item_above != 0;
			case RARE: // Green
				return superior_item_above != 0;
			case LEGEND: // Blue
				return heroic_item_above != 0;
			case UNIQUE: // Yellow
				return fabled_item_above != 0;
			case EPIC: // Orange
				return eternal_item_above != 0;
			case MYTHIC: // Purple
				return mythic_item_above != 0;
		}
		return false;
	}

	public boolean isMisc(ItemQuality quality) {
		return quality.equals(ItemQuality.JUNK) && misc == 1;
	}

	public LootRuleType getLootRule() {
		return lootRule;
	}

	public int getAutodistributionId() {
		boolean isBid = mythic_item_above == 3;
		boolean isRoll = mythic_item_above == 2;
		return isBid ? 3 : isRoll ? 2 : 0;
	}

	public int getCommonItemAbove() {
		return common_item_above;
	}

	public int getSuperiorItemAbove() {
		return superior_item_above;
	}

	public int getHeroicItemAbove() {
		return heroic_item_above;
	}

	public int getFabledItemAbove() {
		return fabled_item_above;
	}

	public int getEternalItemAbove() {
		return eternal_item_above;
	}

	public int getMythicItemAbove() {
		return mythic_item_above;
	}

	public int getNrMisc() {
		return nrMisc;
	}

	public void setNrMisc(int nrMisc) {
		this.nrMisc = nrMisc;
	}

	public void setPlayersInRoll(final Collection<Player> players, int time, final int index, final int npcId) {
		ThreadPoolManager.getInstance().schedule(() -> {
			for (Player player : players) {
				if (player.isInPlayerMode(PlayerMode.IN_ROLL)) {
					InRoll inRoll = player.inRoll;
					if (inRoll.getIndex() == index && inRoll.getNpcId() == npcId)
						DropDistributionService.getInstance().handleRollOrBid(player, inRoll.getRollType(), 0, 0, inRoll.getItemId(), inRoll.getNpcId(),
							inRoll.getIndex());
				}
			}
		}, time);
	}

	public int getNrRoundRobin() {
		return nrRoundRobin;
	}

	public void setNrRoundRobin(int nrRoundRobin) {
		this.nrRoundRobin = nrRoundRobin;
	}

	public int getMisc() {
		return misc;
	}

	public void addItemToBeDistributed(DropItem dropItem) {
		itemsToBeDistributed.add(dropItem);
	}

	public boolean containDropItem(DropItem dropItem) {
		return itemsToBeDistributed.contains(dropItem);
	}

	public void removeItemToBeDistributed(DropItem dropItem) {
		itemsToBeDistributed.remove(dropItem);
	}

	public Deque<DropItem> getItemsToBeDistributed() {
		return itemsToBeDistributed;
	}

}
