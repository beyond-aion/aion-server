package com.aionemu.gameserver.model.team.legion;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;

/**
 * @author Simple
 */
public class LegionMember {

	private final int objectId;
	private final Legion legion;
	private LegionRank rank = LegionRank.VOLUNTEER;
	private String nickname = "";
	private String selfIntro = "";
	private int challengeScore;
	// --- below are cached player fields (not in legion_members table) ---
	private String name;
	private PlayerClass playerClass;
	private int level;
	private int worldId;
	private int lastOnlineEpochSeconds;
	private boolean online = false;

	public LegionMember(int objectId, Legion legion) {
		this.objectId = objectId;
		this.legion = legion;
	}

	public int getObjectId() {
		return objectId;
	}

	public Legion getLegion() {
		return legion;
	}

	public void setRank(LegionRank rank) {
		this.rank = rank;
	}

	public LegionRank getRank() {
		return rank;
	}

	public boolean isBrigadeGeneral() {
		return rank == LegionRank.BRIGADE_GENERAL;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getNickname() {
		return nickname;
	}

	public void setSelfIntro(String selfIntro) {
		this.selfIntro = selfIntro;
	}

	public String getSelfIntro() {
		return selfIntro;
	}

	public int getChallengeScore() {
		return challengeScore;
	}

	public void setChallengeScore(int challengeScore) {
		this.challengeScore = challengeScore;
	}

	public void increaseChallengeScore(int amount) {
		this.challengeScore += amount;
	}

	public void setPlayerData(Player player) {
		setPlayerData(player.getCommonData());
	}

	public void setPlayerData(PlayerCommonData playerCommonData) {
		name = playerCommonData.getName();
		playerClass = playerCommonData.getPlayerClass();
		level = playerCommonData.getLevel();
		worldId = playerCommonData.getMapId();
		lastOnlineEpochSeconds = playerCommonData.getLastOnline() == null ? 0 : (int) (playerCommonData.getLastOnline().getTime() / 1000);
		online = playerCommonData.isOnline();
	}

	public String getName() {
		return name;
	}

	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public int getLevel() {
		return level;
	}

	public int getWorldId() {
		return worldId;
	}

	public int getLastOnlineEpochSeconds() {
		return lastOnlineEpochSeconds;
	}

	public boolean isOnline() {
		return online;
	}

	public boolean hasRights(LegionPermissionsMask permissions) {
		return switch (rank) {
			case BRIGADE_GENERAL -> true;
			case DEPUTY -> permissions.can(legion.getDeputyPermission());
			case CENTURION -> permissions.can(legion.getCenturionPermission());
			case LEGIONARY -> permissions.can(legion.getLegionaryPermission());
			case VOLUNTEER -> permissions.can(legion.getVolunteerPermission());
		};
	}
}
