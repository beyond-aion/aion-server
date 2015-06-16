package com.aionemu.gameserver.model.instance.playerreward;

/**
 *
 * @author xTz
 */
public class DredgionPlayerReward extends InstancePlayerReward {

	private int zoneCaptured;

	public DredgionPlayerReward(Integer object) {
		super(object);
	}

	public void captureZone() {
		zoneCaptured++;
	}

	public int getZoneCaptured() {
		return zoneCaptured;
	}
}
