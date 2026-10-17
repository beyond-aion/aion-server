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
			long blockedMillis = player.getMillisUntilQuittingAllowed();
			if (blockedMillis > 0) {
				sendPacket(SM_QUIT_RESPONSE.refused((int) ((blockedMillis + 999) / 1000)));
				return;
			}
			long countdownMillis = System.currentTimeMillis() - player.getQuitCountdownStartMillis();
			if (!charEditScreen && (countdownMillis < 8000 || countdownMillis > 20000)) {
				sendPacket(SM_QUIT_RESPONSE.refused((int) Math.min(countdownMillis, Integer.MAX_VALUE)));
				return;
			}
			if (stayConnected) { // update char selection info
				player.getAccountData().setVisibleItems(player.getEquipment().getEquippedForAppearance());
				for (PlayerAccountData plAccData : con.getAccount().getPlayerAccDataList())
					plAccData.setCharBanInfo(PlayerPunishmentsDAO.getCharBanInfo(plAccData.getPlayerCommonData().getPlayerObjId()));
			}
			PlayerLeaveWorldService.leaveWorldNow(player);
		}

		SM_QUIT_RESPONSE response = charEditScreen ? SM_QUIT_RESPONSE.quitToEditScreen() : SM_QUIT_RESPONSE.quit();
		if (stayConnected)
			sendPacket(response);
		else
			con.close(response); // makes sure this packet will be sent before closing connection
	}
}
