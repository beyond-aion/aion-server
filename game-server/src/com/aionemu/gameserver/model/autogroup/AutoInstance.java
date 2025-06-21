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
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Estrayl
 */
public abstract class AutoInstance extends AbstractLockManager implements AutoInstanceHandler {

	protected final Map<Integer, AGPlayer> registeredAGPlayers = new ConcurrentHashMap<>();
	protected final AutoGroupType agt;
	protected WorldMapInstance instance;
	protected long startInstanceTime;

	public AutoInstance(AutoGroupType agt) {
		this.agt = agt;
	}

	protected boolean removeItem(Player player, int itemId, long requiredCount) {
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
	public AGQuestion addLookingForParty(LookingForParty lookingForParty) {
		return AGQuestion.FAILED;
	}

	@Override
	public void onEnterInstance(Player player) {
	}

	@Override
	public void onLeaveInstance(Player player) {
	}

	@Override
	public void onPressEnter(Player player) {
		long instanceCoolTime = DataManager.INSTANCE_COOLTIME_DATA.calculateInstanceEntranceCooltime(player, instance.getMapId());
		if (instanceCoolTime > 0)
			player.getPortalCooldownList().addPortalCooldown(instance.getMapId(), instanceCoolTime);
	}

	@Override
	public void unregister(Player player) {
		registeredAGPlayers.remove(player.getObjectId());
	}

	protected boolean isRegistrationDisabled(LookingForParty lfp) {
		if (instance != null) {
			InstanceScore<?> instanceScore = instance.getInstanceHandler().getInstanceScore();
			if (instanceScore != null && instanceScore.getInstanceProgressionType() == InstanceProgressionType.END_PROGRESS)
				return true;
		}
		if (startInstanceTime == 0)
			return false;
		else if (lfp.getEntryRequestType() == EntryRequestType.QUICK_GROUP_ENTRY)
			return System.currentTimeMillis() - startInstanceTime > agt.getMaximumJoinTime();
		return true;
	}

	public AutoGroupType getAutoGroupType() {
		return agt;
	}

	public Map<Integer, AGPlayer> getRegisteredAGPlayers() {
		return registeredAGPlayers;
	}

	public WorldMapInstance getInstance() {
		return instance;
	}

	public long getStartInstanceTime() {
		return startInstanceTime;
	}

	protected List<AGPlayer> getAGPlayersByRace(Race race) {
		return registeredAGPlayers.values().stream().filter(p -> p.race() == race).collect(Collectors.toList());
	}

	protected List<AGPlayer> getAGPlayersByClass(PlayerClass playerClass) {
		return registeredAGPlayers.values().stream().filter(p -> p.playerClass() == playerClass).collect(Collectors.toList());
	}

	protected List<Player> getPlayersByRace(Race race) {
		return instance.getPlayersInside().stream().filter(p -> p.getRace() == race).collect(Collectors.toList());
	}

	public int getMaxPlayers() {
		if (instance != null)
			return instance.getMaxPlayers();
		Race race = registeredAGPlayers.isEmpty() ? Race.ELYOS : registeredAGPlayers.values().iterator().next().race();
		return DataManager.INSTANCE_COOLTIME_DATA.getMaxMemberCount(agt.getTemplate().getInstanceMapId(), race);
	}
}
