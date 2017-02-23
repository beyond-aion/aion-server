package com.aionemu.gameserver.model.instance.playerreward;

/**
 * @author xTz
 */
public class DredgionPlayerReward extends InstancePlayerReward {

	private int zoneCaptured;

	public DredgionPlayerReward(int objectId) {
		super(objectId);
	}

	public void captureZone() {
		zoneCaptured++;
	}

	public int getZoneCaptured() {
		return zoneCaptured;
	}
}
