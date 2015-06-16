package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author -Enomine-
 */

public class CM_BONUS_TITLE extends AionClientPacket {
	
	private int bonusTitleId;
	
	/**
	 * Constructs new instance of <tt>CM_TITLE_BONUS </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_BONUS_TITLE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		bonusTitleId = readH();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (bonusTitleId != 0xFFFF)
			if (!player.getTitleList().contains(bonusTitleId) && !player.havePermission(MembershipConfig.TITLES_ADDITIONAL_ENABLE))
				return;

		player.getTitleList().setBonusTitle(bonusTitleId);
	}

}