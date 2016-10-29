package com.aionemu.gameserver.model.drop;

import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.World;

import javolution.util.FastTable;

/**
 * @author ATracer
 */
public class DropItem {

	private int index = 0;
	private long count = 0;
	private final Drop dropTemplate;
	private List<Integer> playerObjIds;
	private boolean isFreeForAll = false;
	private long highestValue = 0;
	private Player winningPlayer = null;
	private boolean isItemWonNotCollected = false;
	private boolean isDistributeItem = false;
	private int npcObj;
	private int optionalSocket = 0;

	public DropItem(Drop dropTemplate) {
		this.dropTemplate = dropTemplate;
		this.playerObjIds = new FastTable<>();
		if (DataManager.ITEM_DATA.getItemTemplate(dropTemplate.getItemId()).getOptionSlotBonus() != 0)
			optionalSocket = -1;
	}

	/**
	 * Regenerates item count upon each call // TODO input parameters - based on attacker stats // TODO more precise calculations (non-linear)
	 */
	public void calculateCount() {
		long rndCount = Rnd.get(dropTemplate.getMinAmount(), dropTemplate.getMaxAmount());
		count = rndCount == 0 ? 1 : rndCount;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 *          the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @param count
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/**
	 * @return the dropTemplate
	 */
	public Drop getDropTemplate() {
		return dropTemplate;
	}

	/**
	 * @return the playerObjId
	 */
	public List<Integer> getPlayerObjIds() {
		return playerObjIds;
	}

	public boolean canViewDropItem(int objId) {
		return playerObjIds.isEmpty() || playerObjIds.contains(objId);
	}

	/**
	 * @param playerObjId
	 *          the playerObjId to set
	 */
	public void setPlayerObjId(int playerObjId) {
		if (playerObjId > 0 && !playerObjIds.contains(playerObjId))
			this.playerObjIds.add(playerObjId);
	}

	/**
	 * @param isFreeForAll
	 *          to set
	 */
	public void isFreeForAll(boolean isFreeForAll) {
		this.isFreeForAll = isFreeForAll;
	}

	/**
	 * @return isFreeForAll
	 */
	public boolean isFreeForAll() {
		return isFreeForAll;
	}

	/**
	 * @return highestValue
	 */
	public long getHighestValue() {
		return highestValue;
	}

	/**
	 * @param highestValue
	 *          to set
	 */
	public void setHighestValue(long highestValue) {
		this.highestValue = highestValue;
	}

	/**
	 * @param WinningPlayer
	 *          to set
	 */
	public void setWinningPlayer(Player winningPlayer) {
		this.winningPlayer = winningPlayer;

	}

	/**
	 * @return winningPlayer
	 */
	public Player getWinningPlayer() {
		if (winningPlayer != null) {
			if (winningPlayer.isOnline()) {
				return winningPlayer;
			} else {
				Player player = World.getInstance().findPlayer(winningPlayer.getObjectId());
				if (player != null) {
					return player;
				} else {
					return winningPlayer;
				}
			}
		}
		return winningPlayer;
	}

	/**
	 * @param isItemWonNotCollected
	 *          to set
	 */
	public void isItemWonNotCollected(boolean isItemWonNotCollected) {
		this.isItemWonNotCollected = isItemWonNotCollected;
	}

	/**
	 * @return isItemWonNotCollected
	 */
	public boolean isItemWonNotCollected() {
		return isItemWonNotCollected;
	}

	/**
	 * @param isDistributeItem
	 *          to set
	 */
	public void isDistributeItem(boolean isDistributeItem) {
		this.isDistributeItem = isDistributeItem;
	}

	/**
	 * @return isDistributeItem
	 */
	public boolean isDistributeItem() {
		return isDistributeItem;
	}

	public int getNpcObj() {
		return npcObj;
	}

	public void setNpcObj(int npcObj) {
		this.npcObj = npcObj;
	}

	public int getOptionalSocket() {
		return optionalSocket;
	}
}
