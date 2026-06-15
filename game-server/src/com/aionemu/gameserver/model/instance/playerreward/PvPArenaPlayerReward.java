package com.aionemu.gameserver.model.instance.playerreward;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceBuff;
import com.aionemu.gameserver.model.instance.instancescore.PvPArenaScore;
import com.aionemu.gameserver.model.templates.rewards.ArenaRewardItem;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;

/**
 * @author xTz
 */
public class PvPArenaPlayerReward extends InstancePlayerReward {

	private final PlayerClass playerClass;
	private final String playerName;
	private int position;
	private int timeBonus;
	private float timeBonusModifier;
	// Default values for training arenas
	private ArenaRewardItem ap = new ArenaRewardItem(0, 0, 0, 0);
	private ArenaRewardItem gp = new ArenaRewardItem(0, 0, 0, 0);
	private ArenaRewardItem crucibleInsignia = new ArenaRewardItem(0, 0, 0, 0);
	private ArenaRewardItem courageInsignia = new ArenaRewardItem(0, 0, 0, 0);
	private RewardItem rewardItem1;
	private RewardItem rewardItem2;
	private long logoutTime;
	private boolean isRewarded = false;
	private InstanceBuff boostMorale;

	public PvPArenaPlayerReward(Player player, int timeBonus, byte buffId) {
		this(player.getObjectId(), player.getPlayerClass(), player.getName(), timeBonus, buffId);
	}

	public PvPArenaPlayerReward(int objectId, int timeBonus, byte buffId) {
		this(objectId, null, null, timeBonus, buffId);
	}

	private PvPArenaPlayerReward(int objectId, PlayerClass playerClass, String playerName, int timeBonus, byte buffId) {
		super(objectId);
		super.setPoints(13000);
		this.playerClass = playerClass;
		this.playerName = playerName;
		this.timeBonus = timeBonus;
		timeBonusModifier = ((float) this.timeBonus / (float) 660000);
		boostMorale = new InstanceBuff(buffId);
	}

	public void addPoints(int points, PvPArenaScore instanceScore) {
		super.addPoints(points);
		if (getPoints() > instanceScore.getUpperScoreCap())
			setPoints(instanceScore.getUpperScoreCap());
		else if (getPoints() < instanceScore.getLowerScoreCap())
			setPoints(instanceScore.getLowerScoreCap());
	}

	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public String getPlayerName() {
		return playerName;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getTimeBonus() {
		return Math.max(timeBonus, 0);
	}

	public void updateLogoutTime() {
		logoutTime = System.currentTimeMillis();
	}

	public void updateBonusTime() {
		int offlineTime = (int) (System.currentTimeMillis() - logoutTime);
		timeBonus -= (int) (offlineTime * timeBonusModifier);
	}

	public boolean isRewarded() {
		return isRewarded;
	}

	public void setRewarded() {
		isRewarded = true;
	}

	public float getParticipation() {
		return (float) getTimeBonus() / timeBonus;
	}

	public ArenaRewardItem getAp() {
		return ap;
	}

	public void setAp(ArenaRewardItem ap) {
		this.ap = ap;
	}

	public ArenaRewardItem getGp() {
		return gp;
	}

	public void setGp(ArenaRewardItem gp) {
		this.gp = gp;
	}

	public ArenaRewardItem getCrucibleInsignia() {
		return crucibleInsignia;
	}

	public void setCrucibleInsignia(ArenaRewardItem crucibleInsignia) {
		this.crucibleInsignia = crucibleInsignia;
	}

	public ArenaRewardItem getCourageInsignia() {
		return courageInsignia;
	}

	public void setCourageInsignia(ArenaRewardItem courageInsignia) {
		this.courageInsignia = courageInsignia;
	}

	public RewardItem getRewardItem1() {
		return rewardItem1;
	}

	public void setRewardItem1(RewardItem rewardItem1) {
		this.rewardItem1 = rewardItem1;
	}

	public RewardItem getRewardItem2() {
		return rewardItem2;
	}

	public void setRewardItem2(RewardItem rewardItem2) {
		this.rewardItem2 = rewardItem2;
	}

	public int getScorePoints() {
		return timeBonus + getPoints();
	}

	public int getRemainingTime() {
		return boostMorale.getRemainingTime();
	}

	public boolean hasBoostMorale() {
		return boostMorale.getRemainingTime() > 0;
	}

	public void applyBoostMoraleEffect(Player player, int duration) {
		boostMorale.applyEffect(player, duration);
	}

	public void endBoostMoraleEffect(Player player) {
		boostMorale.endEffect(player);
	}

}
