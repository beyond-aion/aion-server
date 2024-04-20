package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.gameobjects.player.DeniedStatus;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SocialService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.world.World;

/**
 * Received when a user tries to add someone as his friend
 * 
 * @author Ben, Neon
 */
public class CM_FRIEND_ADD extends AionClientPacket {

	private String targetName;
	private String message;

	public CM_FRIEND_ADD(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetName = readS();
		message = readS();
	}

	@Override
	protected void runImpl() {
		final Player activePlayer = getConnection().getActivePlayer();
		final Player targetPlayer = World.getInstance().getPlayer(Util.convertName(targetName));
		
		if (targetPlayer == null || !targetPlayer.isOnline()) {
			sendPacket(SM_FRIEND_RESPONSE.TARGET_OFFLINE);
		}	else if (activePlayer.equals(targetPlayer)) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_BUSY());
		} else if (CustomConfig.FRIENDLIST_GM_RESTRICT && ((targetPlayer.isStaff() && !activePlayer.isStaff()) || (activePlayer.isStaff() && !targetPlayer.isStaff()))) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDY_CANT_ADD_WHEN_HE_IS_ASKED_QUESTION(targetPlayer.getName(true)));
		} else if (activePlayer.getFriendList().getFriend(targetPlayer.getObjectId()) != null) {
			sendPacket(SM_FRIEND_RESPONSE.TARGET_ALREADY_FRIEND);
		} else if (activePlayer.getRace() != targetPlayer.getRace()) {
			sendPacket(SM_FRIEND_RESPONSE.TARGET_NOT_FOUND);
		} else if (activePlayer.getBlockList().contains(targetPlayer.getObjectId())) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_NO_BLOCKED_CHARACTER());
		} else if (targetPlayer.getBlockList().contains(activePlayer.getObjectId())) {
			sendPacket(SM_FRIEND_RESPONSE.TARGET_BLOCKED_YOU);
		} else if (activePlayer.getFriendList().isFull()) {
			sendPacket(SM_FRIEND_RESPONSE.LIST_FULL);
		} else if (targetPlayer.getFriendList().isFull()) {
			sendPacket(SM_FRIEND_RESPONSE.TARGET_LIST_FULL(targetPlayer.getName()));
		} else if (targetPlayer.getPlayerSettings().isInDeniedStatus(DeniedStatus.FRIEND)) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_FRIEND(targetPlayer.getName()));
		} else {
			RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<Player>(activePlayer) {

				@Override
				public void acceptRequest(Player requester, Player responder) {
					if (requester.getFriendList().isFull())
						PacketSendUtility.sendPacket(responder, SM_FRIEND_RESPONSE.REQUESTER_LIST_FULL_CANT_ACCEPT(requester.getName()));
					else if (!responder.getFriendList().isFull())
						SocialService.makeFriends(requester, responder);
				}

				@Override
				public void denyRequest(Player requester, Player responder) {
					sendPacket(SM_FRIEND_RESPONSE.TARGET_DENIED(responder.getName()));
				}
			};

			boolean requested = targetPlayer.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_BUDDYLIST_ADD_BUDDY_REQUEST, responseHandler);
			// If the player is busy and could not be asked
			if (!requested) {
				sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_BUSY());
				return;
			}

			// Send question packet to buddy
			PacketSendUtility.sendPacket(targetPlayer,
				new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_BUDDYLIST_ADD_BUDDY_REQUEST, activePlayer.getObjectId(), 0, activePlayer.getName(), message));
		}
	}

}
