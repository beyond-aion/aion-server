package com.aionemu.gameserver.model.instance.instancescore;

import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

/**
 * @author Cheatkiller
 */
public class NormalScore extends InstanceScore<InstancePlayerReward> {

	private int points;
	private int basicAp;
	private int finalAp;
	private int rank = 7;
	private int rewardItem1;
	private int rewardItem2;
	private int rewardItem3;
	private int rewardItem4;
	private int rewardItem1Count;
	private int rewardItem2Count;
	private int rewardItem3Count;
	private int rewardItem4Count;

	public void addPoints(int points) {
		this.points += points;
		if (this.points < 0)
			this.points = 0;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	public int getBasicAp() {
		return basicAp;
	}

	public void setBasicAp(int value) {
		basicAp = value;
	}

	public int getFinalAp() {
		return finalAp;
	}

	public void setFinalAp(int finalAp) {
		this.finalAp = finalAp;
	}

	public int getRewardItem1() {
		return rewardItem1;
	}

	public void setRewardItem1(int rewardItem1) {
		this.rewardItem1 = rewardItem1;
	}

	public int getRewardItem2() {
		return rewardItem2;
	}

	public void setRewardItem2(int rewardItem2) {
		this.rewardItem2 = rewardItem2;
	}

	public int getRewardItem3() {
		return rewardItem3;
	}

	public void setRewardItem3(int rewardItem3) {
		this.rewardItem3 = rewardItem3;
	}

	public int getRewardItem4() {
		return rewardItem4;
	}

	public void setRewardItem4(int rewardItem4) {
		this.rewardItem4 = rewardItem4;
	}

	public int getRewardItem1Count() {
		return rewardItem1Count;
	}

	public void setRewardItem1Count(int rewardItem1Count) {
		this.rewardItem1Count = rewardItem1Count;
	}

	public int getRewardItem2Count() {
		return rewardItem2Count;
	}

	public void setRewardItem2Count(int rewardItem2Count) {
		this.rewardItem2Count = rewardItem2Count;
	}

	public int getRewardItem3Count() {
		return rewardItem3Count;
	}

	public void setRewardItem3Count(int rewardItem3Count) {
		this.rewardItem3Count = rewardItem3Count;
	}

	public int getRewardItem4Count() {
		return rewardItem4Count;
	}

	public void setRewardItem4Count(int rewardItem4Count) {
		this.rewardItem4Count = rewardItem4Count;
	}
}
