package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;

/**
 * Received when a player reports another player with /ReportAutoHunting
 * 
 * @author Jego, Neon
 */
public class CM_REPORT_PLAYER extends AionClientPacket {

	private int reportType;
	private String playerName;

	public CM_REPORT_PLAYER(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		reportType = readUC();
		playerName = readS(); // the name of the reported person.
	}

	@Override
	protected void runImpl() {
		switch (reportType) {
			case 0: // /accuse, /AutoReportHunting
				Player activePlayer = getConnection().getActivePlayer();
				Player player = World.getInstance().getPlayer(ChatUtil.getRealCharName(playerName));
				if (player != null && player.getRace() != activePlayer.getRace()) {
					sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_DO_NOT_ACCUSE());
				} else if (activePlayer.equals(player)) {
					sendPacket(SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
				} else {
					AuditLogger.log(activePlayer, "reported player " + playerName);
					sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_ACCUSE_SUBMIT(playerName, "∞"));
				}
				break;
			case 1: // /NumberofReports
				sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_ACCUSE_COUNT_INFO("∞"));
				break;
			default:
				LoggerFactory.getLogger(CM_REPORT_PLAYER.class).warn("Unhandled report type " + reportType + " (reported player: " + playerName + ")");
		}
	}

}
