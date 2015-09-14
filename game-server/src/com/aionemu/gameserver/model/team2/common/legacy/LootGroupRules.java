package com.aionemu.gameserver.model.team2.common.legacy;

import java.util.Collection;

import javolution.util.FastTable;

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
	private final int ethernal_item_above;
	private final int mythic_item_above;
	private int nrMisc;
	private int nrRoundRobin;
	private final FastTable<DropItem> itemsToBeDistributed = new FastTable<>();

	public LootGroupRules() {
		lootRule = LootRuleType.ROUNDROBIN;
		common_item_above = 0;
		superior_item_above = 2;
		heroic_item_above = 2;
		fabled_item_above = 2;
		ethernal_item_above = 2;
		mythic_item_above = 2;
	}

	public LootGroupRules(LootRuleType lootRule, int misc, int commonItemAbove, int superiorItemAbove, int heroicItemAbove, int fabledItemAbove,
		int ethernalItemAbove, int mythicItemAbove) {
		super();
		this.lootRule = lootRule;
		this.misc = misc;
		common_item_above = commonItemAbove;
		superior_item_above = superiorItemAbove;
		heroic_item_above = heroicItemAbove;
		fabled_item_above = fabledItemAbove;
		ethernal_item_above = ethernalItemAbove;
		mythic_item_above = mythicItemAbove;
	}

	/**
	 * @param quality
	 * @return
	 */
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
			case MYTHIC: // Orange
				return ethernal_item_above != 0;
			case EPIC: // Purple
				return mythic_item_above != 0;
		}
		return false;
	}

	/**
	 * @param quality
	 * @return
	 */
	public boolean isMisc(ItemQuality quality) {
		return quality.equals(ItemQuality.JUNK) && misc == 1;
	}

	/**
	 * @return the lootRule
	 */
	public LootRuleType getLootRule() {
		return lootRule;
	}

	/**
	 * @return the autodistributionId
	 */
	public int getAutodistributionId() {
		boolean isBid = mythic_item_above == 3;
		boolean isRoll = mythic_item_above == 2;
		return isBid ? 3 : isRoll ? 2 : 0;
	}

	/**
	 * @return the common_item_above
	 */
	public int getCommonItemAbove() {
		return common_item_above;
	}

	/**
	 * @return the superior_item_above
	 */
	public int getSuperiorItemAbove() {
		return superior_item_above;
	}

	/**
	 * @return the heroic_item_above
	 */
	public int getHeroicItemAbove() {
		return heroic_item_above;
	}

	/**
	 * @return the fabled_item_above
	 */
	public int getFabledItemAbove() {
		return fabled_item_above;
	}

	/**
	 * @return the ethernal_item_above
	 */
	public int getEthernalItemAbove() {
		return ethernal_item_above;
	}

	/**
	 * @return the mythic_item_above
	 */
	public int getMythicItemAbove() {
		return mythic_item_above;
	}

	/**
	 * @return the nrMisc
	 */
	public int getNrMisc() {
		return nrMisc;
	}

	/**
	 * @param nrMisc
	 *          .
	 */
	public void setNrMisc(int nrMisc) {
		this.nrMisc = nrMisc;
	}

	public void setPlayersInRoll(final Collection<Player> players, int time, final int index, final int npcId) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				for (Player player : players) {
					if (player.isInPlayerMode(PlayerMode.IN_ROLL)) {
						InRoll inRoll = player.inRoll;
						switch (inRoll.getRollType()) {
							case 2:
								if (inRoll.getIndex() == index && inRoll.getNpcId() == npcId)
									DropDistributionService.getInstance().handleRoll(player, 0, inRoll.getItemId(), inRoll.getNpcId(), inRoll.getIndex());
								break;
							case 3:
								if (inRoll.getIndex() == index && inRoll.getNpcId() == npcId)
									DropDistributionService.getInstance().handleBid(player, 0, inRoll.getItemId(), inRoll.getNpcId(), inRoll.getIndex());
								break;
						}
					}
				}
			}

		}, time);
	}

	/**
	 * @return the nrRoundRobin
	 */
	public int getNrRoundRobin() {
		return nrRoundRobin;
	}

	/**
	 * @param nrRoundRobin
	 *          .
	 */
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

	public FastTable<DropItem> getItemsToBeDistributed() {
		return itemsToBeDistributed;
	}

}
