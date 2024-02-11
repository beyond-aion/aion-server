package com.aionemu.gameserver.model.instance.playerreward;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceBuff;
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
	private int basicAP;
	private int rankingAP;
	private int scoreAP;
	private int basicGP;
	private int rankingGP;
	private int scoreGP;
	private int basicCrucible;
	private int rankingCrucible;
	private int scoreCrucible;
	private int basicCourage;
	private int rankingCourage;
	private int scoreCourage;
	private int opportunity;
	private int gloryTicket;
	private int mithrilMedal;
	private int platinumMedal;
	private int ceramiumMedal;
	private int gloriousInsignia;
	private int lifeSerum;
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
		super.addPoints(13000);
		this.playerClass = playerClass;
		this.playerName = playerName;
		this.timeBonus = timeBonus;
		timeBonusModifier = ((float) this.timeBonus / (float) 660000);
		boostMorale = new InstanceBuff(buffId);
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

	public void updateLogOutTime() {
		logoutTime = System.currentTimeMillis();
	}

	public void updateBonusTime() {
		int offlineTime = (int) (System.currentTimeMillis() - logoutTime);
		timeBonus -= offlineTime * timeBonusModifier;
	}

	public boolean isRewarded() {
		return isRewarded;
	}

	public void setRewarded() {
		isRewarded = true;
	}

	public int getBasicAP() {
		return basicAP;
	}

	public int getRankingAP() {
		return rankingAP;
	}

	public int getScoreAP() {
		return scoreAP;
	}

	public void setBasicAP(int ap) {
		this.basicAP = ap;
	}

	public void setRankingAP(int ap) {
		this.rankingAP = ap;
	}

	public void setScoreAP(int ap) {
		this.scoreAP = ap;
	}

	public void setBasicGP(int gp) {
		this.basicGP = gp;
	}

	public void setRankingGP(int gp) {
		this.rankingGP = gp;
	}

	public void setScoreGP(int gp) {
		this.scoreGP = gp;
	}

	public int getBasicGP() {
		return basicGP;
	}

	public int getRankingGP() {
		return rankingGP;
	}

	public int getScoreGP() {
		return scoreGP;
	}

	public float getParticipation() {
		return (float) getTimeBonus() / timeBonus;
	}

	public int getBasicCrucible() {
		return basicCrucible;
	}

	public int getRankingCrucible() {
		return rankingCrucible;
	}

	public int getScoreCrucible() {
		return scoreCrucible;
	}

	public void setCeramiumMedal(int ceramiumMedal) {
		this.ceramiumMedal = ceramiumMedal;
	}

	public void setBasicCrucible(int basicCrucible) {
		this.basicCrucible = basicCrucible;
	}

	public void setRankingCrucible(int rankingCrucible) {
		this.rankingCrucible = rankingCrucible;
	}

	public void setScoreCrucible(int scoreCrucible) {
		this.scoreCrucible = scoreCrucible;
	}

	public void setBasicCourage(int basicCourage) {
		this.basicCourage = basicCourage;
	}

	public void setRankingCourage(int rankingCourage) {
		this.rankingCourage = rankingCourage;
	}

	public void setScoreCourage(int scoreCourage) {
		this.scoreCourage = scoreCourage;
	}

	public int getBasicCourage() {
		return basicCourage;
	}

	public int getRankingCourage() {
		return rankingCourage;
	}

	public int getScoreCourage() {
		return scoreCourage;
	}

	public int getOpportunity() {
		return opportunity;
	}

	public void setOpportunity(int opportunity) {
		this.opportunity = opportunity;
	}

	public int getGloryTicket() {
		return gloryTicket;
	}

	public void setGloryTicket(int gloryTicket) {
		this.gloryTicket = gloryTicket;
	}

	public int getMithrilMedal() {
		return mithrilMedal;
	}

	public int getCeramiumMedal() {
		return ceramiumMedal;
	}

	public void setMithrilMedal(int mithrilMedal) {
		this.mithrilMedal = mithrilMedal;
	}

	public int getPlatinumMedal() {
		return platinumMedal;
	}

	public void setPlatinumMedal(int platinumMedal) {
		this.platinumMedal = platinumMedal;
	}

	public int getGloriousInsignia() {
		return gloriousInsignia;
	}

	public void setGloriousInsignia(int gloriousInsignia) {
		this.gloriousInsignia = gloriousInsignia;
	}

	public int getLifeSerum() {
		return lifeSerum;
	}

	public void setLifeSerum(int lifeSerum) {
		this.lifeSerum = lifeSerum;
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

	public boolean hasBoostMorale() {
		return boostMorale.hasInstanceBuff();
	}

	public void applyBoostMoraleEffect(Player player) {
		boostMorale.applyEffect(player, 20000);
	}

	public void endBoostMoraleEffect(Player player) {
		boostMorale.endEffect(player);
	}

	public int getRemaningTime() {
		int time = boostMorale.getRemaningTime();
		if (time >= 0 && time < 20) {
			return 20 - time;
		}
		return 0;
	}
}
