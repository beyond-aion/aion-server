package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.services.player.PlayerChatService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * Packet that reads normal chat messages.<br>
 *
 * @author SoulKeeper
 */
public class CM_CHAT_MESSAGE_PUBLIC extends AionClientPacket {

	/**
	 * Chat type
	 */
	private ChatType type;

	/**
	 * Chat message
	 */
	private String message;

	public CM_CHAT_MESSAGE_PUBLIC(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		type = ChatType.getChatType(readC());
		message = readS();
	}

	@Override
	protected void runImpl() {
		final Player player = getConnection().getActivePlayer();

		if (ChatProcessor.getInstance().handleChatCommand(player, message))
			return;

		if (!RestrictionsManager.canChat(player))
			return;

		PlayerChatService.logMessage(player, type, message);
		message = NameRestrictionService.filterMessage(message);

		switch (type) {
			case GROUP:
				if (!player.isInTeam())
					return;
				broadcastToGroupMembers(player);
				break;
			case ALLIANCE:
				if (!player.isInAlliance())
					return;
				broadcastToAllianceMembers(player);
				break;
			case GROUP_LEADER:
				if (!player.isInTeam())
					return;
				// Alert must go to entire group or alliance.
				if (player.isInGroup())
					broadcastToGroupMembers(player);
				else
					broadcastToAllianceMembers(player);
				break;
			case LEGION:
				if (!player.isLegionMember())
					return;
				broadcastToLegionMembers(player);
				break;
			case LEAGUE:
			case LEAGUE_ALERT:
				if (!player.isInLeague())
					return;
				broadcastToLeagueMembers(player);
				break;
			case NORMAL:
			case SHOUT:
				broadcastToPlayers(player);
				break;
			case COMMAND:
				if (player.getAbyssRank().getRank() == AbyssRankEnum.COMMANDER || player.getAbyssRank().getRank() == AbyssRankEnum.SUPREME_COMMANDER)
					broadcastFromCommander(player);
				break;
			default:
				if (!player.isGM())
					return;
				broadcastToPlayers(player);
				break;
		}
	}

	private void broadcastFromCommander(final Player player) {
		final int senderRace = player.getRace().getRaceId();
		PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, message, type), true,
			p -> senderRace == p.getRace().getRaceId() || player.isGM() || p.isGM());
	}

	/**
	 * Sends message to all players that are not in blocklist (except GMs)
	 *
	 * @param player
	 */
	private void broadcastToPlayers(final Player player) {
		PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, message, type), true,
			p -> !p.getBlockList().contains(player.getObjectId()) || player.isGM() || p.isGM());
	}

	/**
	 * Sends message to all group members.
	 *
	 * @param player
	 */
	private void broadcastToGroupMembers(final Player player) {
		player.getCurrentGroup().sendPacket(new SM_MESSAGE(player, message, type));
	}

	/**
	 * Sends message to all alliance members
	 *
	 * @param player
	 */
	private void broadcastToAllianceMembers(final Player player) {
		player.getPlayerAlliance().sendPacket(new SM_MESSAGE(player, message, type));
	}

	/**
	 * Sends message to all league members
	 *
	 * @param player
	 */
	private void broadcastToLeagueMembers(final Player player) {
		player.getPlayerAlliance().getLeague().sendPacket(new SM_MESSAGE(player, message, type));
	}

	/**
	 * Sends message to all legion members
	 *
	 * @param player
	 */
	private void broadcastToLegionMembers(final Player player) {
		PacketSendUtility.broadcastToLegion(player.getLegion(), new SM_MESSAGE(player, message, type));
	}
}
