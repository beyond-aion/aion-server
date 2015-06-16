package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.DeniedStatus;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_VIEW_PLAYER_DETAILS;

/**
 * @author Avol
 */
public class CM_VIEW_PLAYER_DETAILS extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_VIEW_PLAYER_DETAILS.class);

	private int targetObjectId;

	public CM_VIEW_PLAYER_DETAILS(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		targetObjectId = readD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player player  = this.getConnection().getActivePlayer();
		VisibleObject obj = player.getKnownList().getObject(targetObjectId);
		if (obj == null) {
			// probably targetObjectId can be 0
			log.warn("CHECKPOINT: can't show player details for " + targetObjectId);
			return;
		}
		
		if (obj instanceof Player) {
			Player target = (Player) obj;
			
			if (!target.getPlayerSettings().isInDeniedStatus(DeniedStatus.VIEW_DETAILS) || player.getAccessLevel() >= AdminConfig.ADMIN_VIEW_DETAILS)
				sendPacket(new SM_VIEW_PLAYER_DETAILS(target.getEquipment().getEquippedItemsWithoutStigma(), target));
			else {
				sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_WATCH(target.getName()));
				return;
			}
		}
	}
}
