package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionRank;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionDominionService;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple
 */
public class CM_LEGION extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_LEGION.class);

	/**
	 * exOpcode and the rest
	 */
	private int exOpcode;
	private short deputyPermission;
	private short centurionPermission;
	private short legionarPermission;
	private short volunteerPermission;
	private int rank;
	private int legionDominionId;
	private String legionName;
	private String charName;
	private String newNickname;
	private String announcement;
	private String newSelfIntro;

	/**
	 * Constructs new instance of CM_LEGION packet
	 * 
	 * @param opcode
	 */
	public CM_LEGION(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		exOpcode = readC();

		switch (exOpcode) {
		/** Create a legion **/
			case 0x00:
				readD(); // 00 78 19 00 40
				legionName = readS();
				break;
			/** Invite to legion **/
			case 0x01:
				readD(); // empty
				charName = readS();
				break;
			/** Leave legion **/
			case 0x02:
				readD(); // empty
				readH(); // empty
				break;
			/** Kick member from legion **/
			case 0x04:
				readD(); // empty
				charName = readS();
				break;
			/** Appoint a new Brigade General **/
			case 0x05:
				readD();
				charName = readS();
				break;
			/** Appoint Centurion **/
			case 0x06:
				rank = readD();
				charName = readS();
				break;
			/** Demote to Legionary **/
			case 0x07:
				readD(); // char id? 00 78 19 00 40
				charName = readS();
				break;
			/** Refresh legion info **/
			case 0x08:
				readD();
				readH();
				break;
			/** Edit announcements **/
			case 0x09:
				readD(); // empty or char id?
				announcement = readS();
				break;
			/** Change self introduction **/
			case 0x0A:
				readD(); // empty char id?
				newSelfIntro = readS();
				break;
			/** Edit permissions **/
			case 0x0D:
				deputyPermission = (short) readH();
				centurionPermission = (short) readH();
				legionarPermission = (short) readH();
				volunteerPermission = (short) readH();
				break;
			/** Level legion up **/
			case 0x0E:
				readD(); // empty
				readH(); // empty
				break;
			case 0x0F:
				charName = readS();
				newNickname = readS();
				break;
			case 0x10: //select legion dominion
				legionDominionId = readD();
				break;
			default:
				log.info("Unknown Legion exOpcode? 0x" + Integer.toHexString(exOpcode).toUpperCase());
				break;
		}
	}

	@Override
	protected void runImpl() {
		final Player activePlayer = getConnection().getActivePlayer();

		if (activePlayer.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_LEGION) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(activePlayer,
				"Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}
		if (activePlayer.isLegionMember()) {
			final Legion legion = activePlayer.getLegion();

			if (charName != null) {
				LegionService.getInstance().handleCharNameRequest(exOpcode, activePlayer, charName, newNickname, rank);
			} else {
				switch (exOpcode) {
				/** Refresh legion info **/
					case 0x08:
						sendPacket(new SM_LEGION_INFO(legion));
						break;
					/** Edit announcements **/
					case 0x09:
						LegionService.getInstance().handleLegionRequest(exOpcode, activePlayer, announcement);
						break;
					/** Change self introduction **/
					case 0x0A:
						LegionService.getInstance().handleLegionRequest(exOpcode, activePlayer, newSelfIntro);
						break;
					/** Edit permissions **/
					case 0x0D:
						if (activePlayer.getLegionMember().isBrigadeGeneral())
							LegionService.getInstance().changePermissions(legion, deputyPermission, centurionPermission, legionarPermission, volunteerPermission);
						break;
					/** Select Legion Dominion to participate **/
					case 0x10:
						if (activePlayer.getLegionMember().isBrigadeGeneral() || activePlayer.getLegionMember().getRank() == LegionRank.DEPUTY) {
							LegionService.getInstance().joinLegionDominion(activePlayer, legion, legionDominionId);
						}
						break;
					/** Misc. **/
					default:
						LegionService.getInstance().handleLegionRequest(exOpcode, activePlayer);
						break;
				}
			}
		} else {
			switch (exOpcode) {
			/** Create a legion **/
				case 0x00:
					LegionService.getInstance().createLegion(activePlayer, legionName);
					break;
			}
		}
	}
}
