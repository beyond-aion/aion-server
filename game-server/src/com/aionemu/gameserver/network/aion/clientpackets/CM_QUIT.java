package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.dao.PlayerPunishmentsDAO;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.services.player.PlayerLeaveWorldService;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author -Nemesiss-, Neon
 */
public class CM_QUIT extends AionClientPacket {

	/**
	 * if true, player wants to go to the character selection or plastic surgery screen.
	 */
	private boolean stayConnected;

	public CM_QUIT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		stayConnected = readC() == 1;
	}

	@Override
	protected void runImpl() {
		AionConnection con = getConnection();
		Player player = con.getActivePlayer();
		boolean charEditScreen = false;

		if (player != null) {
			charEditScreen = player.getCommonData().isInEditMode();
			if (charEditScreen) {
				VisibleObject target = player.getTarget();
				if (!(target instanceof Npc npc) || (!npc.getObjectTemplate().supportsAction(DialogAction.EDIT_CHARACTER_ALL) && !npc.getObjectTemplate().supportsAction(DialogAction.EDIT_CHARACTER_GENDER)) || !PositionUtil.isInTalkRange(player, npc)) {
					AuditLogger.log(player, "tried to enter the plastic surgery screen without targeting the respective npc within talk distance");
					return;
				}
			}
			if (stayConnected) { // update char selection info
				player.getAccountData().setVisibleItems(player.getEquipment().getEquippedForAppearance());
				for (PlayerAccountData plAccData : con.getAccount().getPlayerAccDataList())
					plAccData.setCharBanInfo(PlayerPunishmentsDAO.getCharBanInfo(plAccData.getPlayerCommonData().getPlayerObjId()));
			}
			PlayerLeaveWorldService.leaveWorld(player);
		}

		if (stayConnected)
			sendPacket(new SM_QUIT_RESPONSE(charEditScreen));
		else
			con.close(new SM_QUIT_RESPONSE(charEditScreen)); // makes sure this packet will be sent before closing connection
	}
}
