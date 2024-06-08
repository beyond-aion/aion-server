package com.aionemu.gameserver.model.drop;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.World;

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
		this.playerObjIds = new ArrayList<>();
		if (DataManager.ITEM_DATA.getItemTemplate(dropTemplate.getItemId()).getOptionSlotBonus() != 0)
			optionalSocket = -1;
	}

	/**
	 * Regenerates item count upon each call
	 */
	public void calculateCount() {
		count = Rnd.get(dropTemplate.getMinAmount(), dropTemplate.getMaxAmount());
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
				Player player = World.getInstance().getPlayer(winningPlayer.getObjectId());
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

	public boolean isOnlyPossibleLooter(Player player) {
		if (playerObjIds.size() != 1)
			return false;
		return playerObjIds.contains(player.getObjectId());
	}

	public int getLootEffectId() {
		return switch (dropTemplate.getItemId()) {
			case 166020000, 166020001, 166020002, 166020003 -> 1003; // Omega Enchantment Stone
			case 168000034, 168000035, 168000073, 168000074, 168000117, 168000118, 168000120, 168000121, 168000161, 168000162, 168000164, 168000165,
					 168000213, 168000216, 168000223, 168000228, 168000230, 168000233, 168000240, 168000245 -> 1003; // Godstones
			case 188053083 -> 1003; // Tempering Solution Chest
			case 188053547, 188053548, 188053646, 188053647 -> 1002; // Nether Dragon King weapon boxes
			case 190100004, 190100052 -> 1003; // Mounts
			default -> 0;
		};
	}
}
