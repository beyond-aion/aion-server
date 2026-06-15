package com.aionemu.gameserver.model.gameobjects;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;

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
	private WeakReference<TemporaryPlayerTeam<? extends TeamMember<Player>>> lootingTeam;
	private int lootingTeamId;
	private int maxRoll;
	private LootGroupRules lastLootGroupRules;
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

	public void setLootingPlayer(Player player) {
		this.lootingPlayer = player;
	}

	public Player getLootingPlayer() {
		return lootingPlayer;
	}

	public boolean isBeingLooted() {
		return lootingPlayer != null;
	}

	public void setDistributionId(int distributionId) {
		this.distributionId = distributionId;
	}

	public int getDistributionId() {
		return distributionId;
	}

	public void setDistributionType(boolean distributionType) {
		this.distributionType = distributionType;
	}

	public boolean getDistributionType() {
		return distributionType;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public int getLootingTeamId() {
		return lootingTeamId;
	}

	public int getMaxRoll() {
		return maxRoll;
	}

	public LootGroupRules getLootGroupRules() {
		var team = lootingTeam == null ? null : lootingTeam.get();
		if (team != null)
			lastLootGroupRules = team.getLootGroupRules();
		return lastLootGroupRules;
	}

	public void setLootingTeam(TemporaryPlayerTeam<? extends TeamMember<Player>> team) {
		lootingTeam = new WeakReference<>(team);
		lootingTeamId = team.getTeamId();
		maxRoll = team instanceof PlayerAlliance alli ? alli.isInLeague() ? 10000 : 1000 : 100;
		lastLootGroupRules = team.getLootGroupRules();
	}

	public void setInRangePlayers(Collection<Player> inRangePlayers) {
		this.inRangePlayers = inRangePlayers;
	}

	public Collection<Player> getInRangePlayers() {
		return inRangePlayers;
	}

	public void addPlayerStatus(Player player) {
		playerStatus.add(player);
	}

	public void delPlayerStatus(Player player) {
		playerStatus.remove(player);
	}

	public Collection<Player> getPlayerStatus() {
		return playerStatus;
	}

	public boolean containsPlayerStatus(Player player) {
		return playerStatus.contains(player);
	}

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
}