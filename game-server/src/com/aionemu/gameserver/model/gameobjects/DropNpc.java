package com.aionemu.gameserver.model.gameobjects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Simple
 */
public class DropNpc {

	private final int objectIdId;
	private Set<Integer> allowedLooters = new HashSet<>();
	private Collection<Player> inRangePlayers = new ArrayList<>();
	private Collection<Player> playerStatus = new ArrayList<>();
	private Player lootingPlayer = null;
	private int distributionId = 0;
	private boolean distributionType;
	private int currentIndex = 0;
	private int groupSize = 0;
	private boolean isFreeForAll = false;
	private long remaingDecayTime;

	public DropNpc(int objectIdId) {
		this.objectIdId = objectIdId;
	}

	public void setAllowedLooters(Set<Integer> allowedLooters) {
		this.allowedLooters = allowedLooters;
	}

	public void setAllowedLooter(Player player) {
		allowedLooters.add(player.getObjectId());
	}

	public Set<Integer> getAllowedLooters() {
		return allowedLooters;
	}

	public boolean isAllowedToLoot(Player player) {
		return isFreeForAll || allowedLooters.contains(player.getObjectId());
	}

	/**
	 * @param player
	 *          the lootingPlayer to set
	 */
	public void setLootingPlayer(Player player) {
		this.lootingPlayer = player;
	}

	/**
	 * @return lootingPlayer
	 */
	public Player getLootingPlayer() {
		return lootingPlayer;
	}

	/**
	 * @return the beingLooted
	 */
	public boolean isBeingLooted() {
		return lootingPlayer != null;
	}

	/**
	 * @param distributionId
	 */
	public void setDistributionId(int distributionId) {
		this.distributionId = distributionId;
	}

	/**
	 * @return the DistributionId
	 */
	public int getDistributionId() {
		return distributionId;
	}

	/**
	 * @param distributionType
	 */
	public void setDistributionType(boolean distributionType) {
		this.distributionType = distributionType;
	}

	/**
	 * @return the DistributionType
	 */
	public boolean getDistributionType() {
		return distributionType;
	}

	/**
	 * @param currentIndex
	 */
	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	/**
	 * @return currentIndex
	 */
	public int getCurrentIndex() {
		return currentIndex;
	}

	/**
	 * @param groupSize
	 */
	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}

	/**
	 * @return groupSize
	 */
	public int getGroupSize() {
		return groupSize;
	}

	/**
	 * @param inRangePlayers
	 */
	public void setInRangePlayers(Collection<Player> inRangePlayers) {
		this.inRangePlayers = inRangePlayers;
	}

	/**
	 * @return the inRangePlayers
	 */
	public Collection<Player> getInRangePlayers() {
		return inRangePlayers;
	}

	/**
	 * @param addPlayerStatus
	 */
	public void addPlayerStatus(Player player) {
		playerStatus.add(player);
	}

	/**
	 * @param delPlayerStatus
	 */
	public void delPlayerStatus(Player player) {
		playerStatus.remove(player);
	}

	/**
	 * @return the playerStatus
	 */
	public Collection<Player> getPlayerStatus() {
		return playerStatus;
	}

	/**
	 * @return true if player is found in list
	 */
	public boolean containsPlayerStatus(Player player) {
		return playerStatus.contains(player);
	}

	/**
	 * @return isFreeForAll.
	 */
	public boolean isFreeForAll() {
		return isFreeForAll;
	}

	public void startFreeForAll() {
		isFreeForAll = true;
		distributionId = 0;
		allowedLooters.clear();
	}

	public final int getObjectId() {
		return objectIdId;
	}

	public long getRemaingDecayTime() {
		return remaingDecayTime;
	}

	public void setRemaingDecayTime(long remaingDecayTime) {
		this.remaingDecayTime = remaingDecayTime;
	}

	// TODO remove all this after fixing invalid loot requests
	private final Map<Integer, LooterInfo> looterInfos = new ConcurrentHashMap<>();
	public void addLooterInfo(Player player, int lootedItemIndex, boolean autoloot) {
		looterInfos.put(lootedItemIndex, new LooterInfo(player.toString(), autoloot, System.currentTimeMillis()));
	}
	public String getLooterInfo(int lootedItemIndex) {
		LooterInfo looterInfo = looterInfos.get(lootedItemIndex);
		return looterInfo == null ? "" : looterInfo.looter + " (autoloot=" + looterInfo.autoloot + ") " + (System.currentTimeMillis() - looterInfo.time) + " ms ago";
	}
	record LooterInfo(String looter, boolean autoloot, long time) {}
}
