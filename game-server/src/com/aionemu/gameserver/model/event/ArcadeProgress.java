package com.aionemu.gameserver.model.event;

/**
 * Created on 28.05.2016
 * 
 * @author Estrayl
 * @since AION 4.8
 */
public class ArcadeProgress {

	private final int playerObjId;
	private int frenzyPoints;
	private int currentLevel;
	private long frenzyEndTimeMillis;
	private int resumeLevel;
	private long nextTryTimeMillis;

	public ArcadeProgress(int playerObjId) {
		this.playerObjId = playerObjId;
	}

	public int getPlayerObjId() {
		return playerObjId;
	}

	public int getFrenzyPoints() {
		return frenzyPoints;
	}

	public void setFrenzyPoints(int frenzyPoints) {
		this.frenzyPoints = frenzyPoints;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public long getFrenzyEndTimeMillis() {
		return frenzyEndTimeMillis;
	}

	public void setFrenzyEndTimeMillis(long frenzyEndTimeMillis) {
		this.frenzyEndTimeMillis = frenzyEndTimeMillis;
	}

	public int getResumeLevel() {
		return resumeLevel;
	}

	public void setResumeLevel(int resumeLevel) {
		this.resumeLevel = resumeLevel;
	}

	public long getNextTryTimeMillis() {
		return nextTryTimeMillis;
	}

	public void setTimeNextTry(long nextTryTimeMillis) {
		this.nextTryTimeMillis = nextTryTimeMillis;
	}
}
