package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.Util;

/**
 * @author Simple
 */
public class CM_LEGION extends AionClientPacket {

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

	public CM_LEGION(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		exOpcode = readUC();

		switch (exOpcode) {
			// Create a legion
			case 0x00:
				readD(); // 00 78 19 00 40
				legionName = readS();
				break;
			// Invite to legion
			case 0x01:
				readD(); // empty
				charName = readS();
				break;
			// Leave legion
			case 0x02:
				readD(); // empty
				readH(); // empty
				break;
			// Kick member from legion
			case 0x04:
				readD(); // empty
				charName = readS();
				break;
			// Appoint a new Brigade General
			case 0x05:
				readD();
				charName = readS();
				break;
			// Change rank
			case 0x06:
				rank = readD();
				charName = readS();
				break;
			// Show current announcement (via /gnotice)
			case 0x07:
			// Refresh legion info
			case 0x08:
				readD(); // 0
				readH(); // empty
				break;
			// Edit current announcement (from legion window or via /gnotice New text)
			case 0x09:
				readD(); // empty or char id?
				announcement = readS();
				break;
			// Change self introduction
			case 0x0A:
				readD(); // empty char id?
				newSelfIntro = readS();
				break;
			// Edit permissions
			case 0x0D:
				deputyPermission = readH();
				centurionPermission = readH();
				legionarPermission = readH();
				volunteerPermission = readH();
				break;
			// Level legion up
			case 0x0E:
				readD(); // empty
				readH(); // empty
				break;
			case 0x0F:
				charName = readS();
				newNickname = readS();
				break;
			case 0x10: // selected legion dominion
				legionDominionId = readD();
				break;
			default:
				LoggerFactory.getLogger(CM_LEGION.class).warn("Unknown Legion exOpcode 0x" + Integer.toHexString(exOpcode).toUpperCase());
				break;
		}
	}

	@Override
	protected void runImpl() {
		final Player activePlayer = getConnection().getActivePlayer();
		if (activePlayer.isLegionMember()) {
			final Legion legion = activePlayer.getLegion();
			if (charName != null)
				charName = Util.convertName(charName);
			switch (exOpcode) {
				// invite to legion
				case 0x01 -> LegionService.getInstance().invitePlayerToLegion(activePlayer, charName);
				// leave legion
				case 0x02 -> LegionService.getInstance().leaveLegion(activePlayer, false);
				// kick member
				case 0x04 -> LegionService.getInstance().kickMember(activePlayer, charName);
				// appoint a new Brigade General
				case 0x05 -> LegionService.getInstance().startBrigadeGeneralChangeProcess(activePlayer, charName);
				// change rank
				case 0x06 -> LegionService.getInstance().appointRank(activePlayer, charName, rank);
				// show legion notice (from /gnotice chat command)
				case 0x07 -> {
					Legion.Announcement currentAnnouncement = legion.getAnnouncement();
					if (currentAnnouncement == null)
						sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_NOSET_GUILD_NOTICE());
					else
						sendPacket(SM_SYSTEM_MESSAGE.STR_GUILD_NOTICE(currentAnnouncement.message(), currentAnnouncement.time().getTime() / 1000));
				}
				// refresh legion info
				case 0x08 -> sendPacket(new SM_LEGION_INFO(legion));
				// edit announcements
				case 0x09 -> LegionService.getInstance().changeAnnouncement(activePlayer, announcement);
				// change self introduction
				case 0x0A ->LegionService.getInstance().changeSelfIntro(activePlayer, newSelfIntro);
				// edit permissions
				case 0x0D -> LegionService.getInstance().changePermissions(activePlayer, deputyPermission, centurionPermission, legionarPermission,
						volunteerPermission);
				// level up legion
				case 0x0E -> LegionService.getInstance().requestChangeLevel(activePlayer);
				// change nickname
				case 0x0F -> LegionService.getInstance().changeNickname(activePlayer, charName, newNickname);
				// select Legion Dominion to participate
				case 0x10 -> LegionService.getInstance().joinLegionDominion(activePlayer, legionDominionId);
			}
		} else {
			switch (exOpcode) {
				case 0x00: // create a legion
					LegionService.getInstance().createLegion(activePlayer, legionName);
					break;
			}
		}
	}
}
