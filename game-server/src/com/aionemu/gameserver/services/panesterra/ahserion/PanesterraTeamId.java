package com.aionemu.gameserver.services.panesterra.ahserion;

/**
 * @author Yeats
 *
 */
public enum PanesterraTeamId {

	GAB1_SUB_DEST_69(69),
	GAB1_SUB_DEST_70(70),
	GAB1_SUB_DEST_71(71),
	GAB1_SUB_DEST_72(72),
	BALAUR(1),
	
	AHSERION_TEAM(GAB1_SUB_DEST_69.getId() | GAB1_SUB_DEST_70.getId() | GAB1_SUB_DEST_71.getId() | GAB1_SUB_DEST_72.getId());
	
	private int teamId;
	
	private PanesterraTeamId(int teamId) {
		
		this.teamId = teamId;
	}

	public int getId() {
		return teamId;
	}

	
}
