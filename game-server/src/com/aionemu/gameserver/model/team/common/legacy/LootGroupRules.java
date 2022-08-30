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
	private final int commonItemAbove;
	private final int superiorItemAbove;
	private final int heroicItemAbove;
	private final int fabledItemAbove;
	private final int eternalItemAbove;
	private final int mythicItemAbove;
	private int nrMisc;
	private int nrRoundRobin;
	private final Deque<DropItem> itemsToBeDistributed = new ConcurrentLinkedDeque<>();

	public LootGroupRules() {
		lootRule = LootRuleType.ROUNDROBIN;
		commonItemAbove = 0;
		superiorItemAbove = 2;
		heroicItemAbove = 2;
		fabledItemAbove = 2;
		eternalItemAbove = 2;
		mythicItemAbove = 2;
	}

	public LootGroupRules(LootRuleType lootRule, int misc, int commonItemAbove, int superiorItemAbove, int heroicItemAbove, int fabledItemAbove,
		int eternalItemAbove, int mythicItemAbove) {
		super();
		this.lootRule = lootRule;
		this.misc = misc;
		this.commonItemAbove = commonItemAbove;
		this.superiorItemAbove = superiorItemAbove;
		this.heroicItemAbove = heroicItemAbove;
		this.fabledItemAbove = fabledItemAbove;
		this.eternalItemAbove = eternalItemAbove;
		this.mythicItemAbove = mythicItemAbove;
	}

	public boolean getQualityRule(ItemQuality quality) {
		return switch (quality) {
			case COMMON -> commonItemAbove != 0; // White
			case RARE -> superiorItemAbove != 0; // Green
			case LEGEND -> heroicItemAbove != 0; // Blue
			case UNIQUE -> fabledItemAbove != 0; // Yellow
			case EPIC -> eternalItemAbove != 0; // Orange
			case MYTHIC -> mythicItemAbove != 0; // Purple
			default -> false;
		};
	}

	public boolean isMisc(ItemQuality quality) {
		return quality.equals(ItemQuality.JUNK) && misc == 1;
	}

	public LootRuleType getLootRule() {
		return lootRule;
	}

	public int getAutodistributionId() {
		boolean isBid = mythicItemAbove == 3;
		boolean isRoll = mythicItemAbove == 2;
		return isBid ? 3 : isRoll ? 2 : 0;
	}

	public int getCommonItemAbove() {
		return commonItemAbove;
	}

	public int getSuperiorItemAbove() {
		return superiorItemAbove;
	}

	public int getHeroicItemAbove() {
		return heroicItemAbove;
	}

	public int getFabledItemAbove() {
		return fabledItemAbove;
	}

	public int getEternalItemAbove() {
		return eternalItemAbove;
	}

	public int getMythicItemAbove() {
		return mythicItemAbove;
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
