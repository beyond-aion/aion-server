package com.aionemu.gameserver.model.autogroup;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.aionemu.commons.taskmanager.AbstractLockManager;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public abstract class AutoInstance extends AbstractLockManager implements AutoInstanceHandler {

	public final AutoGroupType agt;
	public long startInstanceTime;
	public WorldMapInstance instance;
	public Map<Integer, AGPlayer> players = new ConcurrentHashMap<>();

	public AutoInstance(AutoGroupType agt) {
		this.agt = agt;
	}

	protected boolean decrease(Player player, int itemId, long requiredCount) {
		long itemCount = 0;
		List<Item> items = player.getInventory().getItemsByItemId(itemId);
		for (Item item : items)
			itemCount += item.getItemCount();
		if (itemCount < requiredCount)
			return false;
		items.sort(Comparator.comparingInt(Item::getExpireTime));
		for (Item item : items) {
			requiredCount = player.getInventory().decreaseItemCount(item, requiredCount);
			if (requiredCount == 0)
				break;
		}
		return true;
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		this.instance = instance;
		startInstanceTime = System.currentTimeMillis();
	}

	@Override
	public AGQuestion addPlayer(Player player, SearchInstance searchInstance) {
		return AGQuestion.FAILED;
	}

	@Override
	public void onEnterInstance(Player player) {
		AGPlayer autoGroupPlayer = players.get(player.getObjectId());
		autoGroupPlayer.setInInstance(true);
		autoGroupPlayer.setOnline(true);
	}

	@Override
	public void onLeaveInstance(Player player) {
	}

	@Override
	public void onPressEnter(Player player) {
		players.get(player.getObjectId()).setPressEnter(true);
	}

	@Override
	public void unregister(Player player) {
		players.remove(player.getObjectId());
	}

	@Override
	public void clear() {
		players.clear();
	}

	protected boolean satisfyTime(SearchInstance searchInstance) {
		if (instance != null) {
			InstanceScore<?> instanceScore = instance.getInstanceHandler().getInstanceScore();
			if ((instanceScore != null && instanceScore.getInstanceProgressionType().isEndProgress()))
				return false;
		}
		if (!searchInstance.getEntryRequestType().isQuickGroupEntry())
			return startInstanceTime == 0;
		int time = agt.getTime();
		if (time == 0 || startInstanceTime == 0)
			return true;
		return System.currentTimeMillis() - startInstanceTime < time;
	}

	protected List<AGPlayer> getAGPlayersByRace(Race race) {
		return players.values().stream().filter(p -> p.getRace() == race).collect(Collectors.toList());
	}

	protected List<AGPlayer> getAGPlayersByClass(PlayerClass playerClass) {
		return players.values().stream().filter(p -> p.getPlayerClass() == playerClass).collect(Collectors.toList());
	}

	protected List<Player> getPlayersByRace(Race race) {
		return instance.getPlayersInside().stream().filter(p -> p.getRace() == race).collect(Collectors.toList());
	}

	public int getMaxPlayers() {
		if (instance != null)
			return instance.getMaxPlayers();
		Race race = players.isEmpty() ? Race.ELYOS : players.values().iterator().next().getRace();
		int maxMemberCount = DataManager.INSTANCE_COOLTIME_DATA.getMaxMemberCount(agt.getInstanceMapId(), race);
		if (agt.isIconInvite()) // fix for dreds and battlefields, since instance_cooltime limits are per faction, not total. arena counts are ok though
			maxMemberCount += DataManager.INSTANCE_COOLTIME_DATA.getMaxMemberCount(agt.getInstanceMapId(), race == Race.ELYOS ? Race.ASMODIANS : Race.ELYOS);
		return maxMemberCount;
	}
}
