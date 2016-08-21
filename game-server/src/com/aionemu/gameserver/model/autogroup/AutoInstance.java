package com.aionemu.gameserver.model.autogroup;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.commons.taskmanager.AbstractLockManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public abstract class AutoInstance extends AbstractLockManager implements AutoInstanceHandler {

	protected int instanceMaskId;
	public long startInstanceTime;
	public WorldMapInstance instance;
	public AutoGroupType agt;
	public Map<Integer, AGPlayer> players = new HashMap<>();

	protected boolean decrease(Player player, int itemId, long count) {
		long i = 0;
		List<Item> items = player.getInventory().getItemsByItemId(itemId);
		for (Item findedItem : items) {
			i += findedItem.getItemCount();
		}
		if (i < count) {
			return false;
		}
		items = sort(items, on(Item.class).getExpireTime());
		for (Item item : items) {
			long l = player.getInventory().decreaseItemCount(item, count);
			if (l == 0) {
				break;
			} else {
				count = l;
			}
		}
		return true;
	}

	@Override
	public void initialize(int instanceMaskId) {
		this.instanceMaskId = instanceMaskId;
		agt = AutoGroupType.getAGTByMaskId(instanceMaskId);
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
		players.get(player.getObjectId()).setInInstance(true);
		players.get(player.getObjectId()).setOnline(true);
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
		Integer obj = player.getObjectId();
		if (players.containsKey(obj)) {
			players.remove(obj);
		}
	}

	@Override
	public void clear() {
		players.clear();
	}

	protected boolean satisfyTime(SearchInstance searchInstance) {
		if (instance != null) {
			InstanceReward<?> instanceReward = instance.getInstanceHandler().getInstanceReward();
			if ((instanceReward != null && instanceReward.getInstanceScoreType().isEndProgress())) {
				return false;
			}
		}

		if (!searchInstance.getEntryRequestType().isQuickGroupEntry()) {
			return startInstanceTime == 0;
		}

		int time = agt.getTime();
		if (time == 0 || startInstanceTime == 0) {
			return true;
		}
		return System.currentTimeMillis() - startInstanceTime < time;
	}

}
