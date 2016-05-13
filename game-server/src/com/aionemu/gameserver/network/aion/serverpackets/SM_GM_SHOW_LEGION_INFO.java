package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.team.legion.Legion;

/**
 * @author Yeats.
 */
public class SM_GM_SHOW_LEGION_INFO extends SM_LEGION_INFO {

	public SM_GM_SHOW_LEGION_INFO(Legion legion) {
		super(legion);
	}
}
