package com.aionemu.gameserver.model.instance;

/**
 * @author Estrayl
 */
public enum InstanceScoreType {

	UPDATE_INSTANCE_PROGRESS(2),
	INIT_PLAYER(3),
	UPDATE_PLAYER_BUFF_STATUS(4),
	SHOW_REWARD(5),
	UPDATE_INSTANCE_BUFFS_AND_SCORE(6),
	UPDATE_ALL_PLAYER_INFO(7),
	PLAYER_QUIT(8),
	UPDATE_RANK(10),
	UPDATE_FACTION_SCORE(11);

	private final int id;

	private InstanceScoreType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
