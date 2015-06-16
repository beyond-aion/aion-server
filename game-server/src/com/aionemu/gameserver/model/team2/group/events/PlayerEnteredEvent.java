package com.aionemu.gameserver.model.team2.group.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.TeamEvent;
import com.aionemu.gameserver.model.team2.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class PlayerEnteredEvent implements Predicate<Player>, TeamEvent {

	private final PlayerGroup group;
	private final Player enteredPlayer;

	public PlayerEnteredEvent(PlayerGroup group, Player enteredPlayer) {
		this.group = group;
		this.enteredPlayer = enteredPlayer;
	}

	/**
	 * Entered player should not be in group yet
	 */
	@Override
	public boolean checkCondition() {
		return !group.hasMember(enteredPlayer.getObjectId());
	}

	@Override
	public void handleEvent() {
		PlayerGroupService.addPlayerToGroup(group, enteredPlayer);
		PacketSendUtility.sendPacket(enteredPlayer, new SM_GROUP_INFO(group));
		PacketSendUtility.sendPacket(enteredPlayer, new SM_GROUP_MEMBER_INFO(group, enteredPlayer, GroupEvent.JOIN));
		PacketSendUtility.sendPacket(enteredPlayer, new SM_INSTANCE_INFO(enteredPlayer, false, group));
		PacketSendUtility.broadcastPacketTeam(enteredPlayer, new SM_ABYSS_RANK_UPDATE(1, enteredPlayer), true, false);
		PacketSendUtility.sendPacket(enteredPlayer, SM_SYSTEM_MESSAGE.STR_PARTY_ENTERED_PARTY);
		group.applyOnMembers(this);
	}

	@Override
	public boolean apply(Player player) {
		if (!player.getObjectId().equals(enteredPlayer.getObjectId())) {
			// TODO probably here JOIN event
			PacketSendUtility.sendPacket(player, new SM_GROUP_MEMBER_INFO(group, enteredPlayer, GroupEvent.ENTER));
			PacketSendUtility.sendPacket(player, new SM_INSTANCE_INFO(enteredPlayer, false, group));
			if (player.getKnownList().getKnownPlayers().containsKey(enteredPlayer.getObjectId())) {
				PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK_UPDATE(1, enteredPlayer));
			}
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_HE_ENTERED_PARTY(enteredPlayer.getName()));

			PacketSendUtility.sendPacket(enteredPlayer, new SM_GROUP_MEMBER_INFO(group, player, GroupEvent.ENTER));
		}
		return true;
	}

}