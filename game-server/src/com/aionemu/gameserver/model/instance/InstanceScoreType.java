package com.aionemu.gameserver.model.instance;

/**
 * @author Estrayl
 */
public enum InstanceScoreType {

	UPDATE_PROGRESS(2),
	INIT_PLAYER(3),
	UPDATE_PLAYER_STATUS(4),
	SHOW_REWARD(5),
	UPDATE_SCORE(6),
	UPDATE_PLAYER_INFO(7),
	PLAYER_QUIT(8),
	UNK(10),
	NPC_DIED(11);

	private final int id;

	private InstanceScoreType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
