package com.aionemu.gameserver.model;

/**
 * @author zdead
 */
public class AbyssRankingResult {

	private String playerName;
	private int playerAbyssRank;
	private int oldRankPos;
	private int rankPos;
	private int ap;
	private int gp;
	private int title;
	private PlayerClass playerClass;
	private int playerLevel;
	private int playerId;
	private Gender gender;

	private String legionName;
	private long cp;
	private int legionId;
	private int legionLevel;
	private int legionMembers;

	public AbyssRankingResult(String playerName, int playerAbyssRank, int playerId, int ap, int gp,
		int title, PlayerClass playerClass, Gender gender, int playerLevel, String legionName, int oldRankPos, int rankPos) {
		this.playerName = playerName;
		this.playerAbyssRank = playerAbyssRank;
		this.playerId = playerId;
		this.ap = ap;
		this.gp = gp;
		this.title = title;
		this.playerClass = playerClass;
		this.gender = gender;
		this.playerLevel = playerLevel;
		this.legionName = legionName;
		this.oldRankPos = oldRankPos;
		this.rankPos = rankPos;
	}

	public AbyssRankingResult(long cp, String legionName, int legionId, int legionLevel, int legionMembers, int oldRankPos, int rankPos) {
		this.oldRankPos = oldRankPos;
		this.rankPos = rankPos;
		this.cp = cp;
		this.legionName = legionName;
		this.legionId = legionId;
		this.legionLevel = legionLevel;
		this.legionMembers = legionMembers;
	}

	public String getPlayerName() {
		return playerName;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getPlayerAbyssRank() {
		return playerAbyssRank;
	}

	
	/**
	 * @return the oldRankPos
	 */
	public int getOldRankPos() {
		return oldRankPos;
	}
	
	public int getRankPos() {
		return rankPos;
	}

	public int getPlayerAP() {
		return ap;
	}
	
	public int getPlayerGP() {
		return gp;
	}

	public int getPlayerTitle() {
		return title;
	}

	public int getPlayerLevel() {
		return playerLevel;
	}

	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public Gender getGender() {
		return gender;
	}

	public String getLegionName() {
		return legionName;
	}

	public long getLegionCP() {
		return cp;
	}

	public int getLegionId() {
		return legionId;
	}

	public int getLegionLevel() {
		return legionLevel;
	}

	public int getLegionMembers() {
		return legionMembers;
	}
}
