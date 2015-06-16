package com.aionemu.gameserver.model.guide;

/**
 * @author xTz
 */
public class Guide {

	private int guide_id;
	private int player_id;
	private String title;

	public Guide(int guide_id, int player_id, String title) {
		this.guide_id = guide_id;
		this.player_id = player_id;
		this.title = title;
	}

	public int getGuideId() {
		return guide_id;
	}

	public int getPlayerId() {
		return player_id;
	}

	public String getTitle() {
		return title;
	}
}
