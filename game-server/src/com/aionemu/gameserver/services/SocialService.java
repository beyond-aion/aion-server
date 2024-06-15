package com.aionemu.gameserver.services;

import com.aionemu.gameserver.dao.BlockListDAO;
import com.aionemu.gameserver.dao.FriendListDAO;
import com.aionemu.gameserver.model.gameobjects.player.BlockedPlayer;
import com.aionemu.gameserver.model.gameobjects.player.Friend;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * Handles activities related to social groups ingame such as the buddy list, block list, etc
 * 
 * @author Ben, Neon
 */
public class SocialService {

	public static boolean addBlockedUser(Player player, Player blockedPlayer, String reason) {
		if (BlockListDAO.addBlockedUser(player.getObjectId(), blockedPlayer.getObjectId(), reason)) {
			player.getBlockList().add(new BlockedPlayer(blockedPlayer.getObjectId(), blockedPlayer.getName(), reason));
			PacketSendUtility.sendPacket(player, new SM_BLOCK_LIST());
			PacketSendUtility.sendPacket(player, new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.BLOCK_SUCCESSFUL, blockedPlayer.getName()));
			return true;
		}
		return false;
	}

	public static boolean deleteBlockedUser(Player player, BlockedPlayer target) {
		if (BlockListDAO.delBlockedUser(player.getObjectId(), target.getObjId())) {
			player.getBlockList().remove(target.getObjId());
			PacketSendUtility.sendPacket(player, new SM_BLOCK_LIST());
			PacketSendUtility.sendPacket(player, new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.UNBLOCK_SUCCESSFUL, target.getName()));
			return true;
		}
		return false;
	}

	/**
	 * Sets the reason for blocking a user
	 * 
	 * @param player
	 *          Player whos block list is to be edited
	 * @param target
	 *          Whom to block
	 * @param reason
	 *          Reason to set
	 * @return True on success - May be false if the reason was the same and therefore not edited
	 */
	public static boolean setBlockedReason(Player player, BlockedPlayer target, String reason) {
		if (!target.getReason().equals(reason)) {
			if (BlockListDAO.setReason(player.getObjectId(), target.getObjId(), reason)) {
				target.setReason(reason);
				PacketSendUtility.sendPacket(player, new SM_BLOCK_LIST());
				PacketSendUtility.sendPacket(player, new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.EDIT_NOTE, target.getName()));
				return true;
			}
		}
		return false;
	}

	/**
	 * @return True on success - May be false if the memo was the same and therefore not edited
	 */
	public static boolean setFriendMemo(Player player, Friend target, String memo) {
		if (!target.getFriendMemo().equals(memo)) {
			if (FriendListDAO.setFriendMemo(player.getObjectId(), target.getObjectId(), memo)) {
				target.setFriendMemo(memo);
				PacketSendUtility.sendPacket(player, new SM_FRIEND_LIST());
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds two players to each others friend lists, and updates the database
	 * 
	 * @return True on success
	 */
	public static boolean makeFriends(Player friend1, Player friend2) {
		if (friend1.getFriendList().getFriend(friend2.getObjectId()) != null)
			return false;
		if (FriendListDAO.addFriends(friend1, friend2)) {
			friend1.getFriendList().addFriend(new Friend(friend2.getCommonData(), ""));
			friend2.getFriendList().addFriend(new Friend(friend1.getCommonData(), ""));

			PacketSendUtility.sendPacket(friend1, new SM_FRIEND_LIST());
			PacketSendUtility.sendPacket(friend2, new SM_FRIEND_LIST());

			PacketSendUtility.sendPacket(friend1, SM_FRIEND_RESPONSE.TARGET_ADDED(friend2.getName()));
			PacketSendUtility.sendPacket(friend2, SM_FRIEND_RESPONSE.TARGET_ADDED(friend1.getName()));
			return true;
		}
		return false;
	}

	/**
	 * Deletes two players from eachother's friend lists, and updates the database
	 * 
	 * @param deleter
	 *          Player deleting a friend
	 * @param friend
	 *          Friend he is deleting
	 * @return True on success
	 */
	public static boolean deleteFriend(Player deleter, Friend friend) {
		int friendObjId = friend.getObjectId();
		if (FriendListDAO.delFriends(deleter.getObjectId(), friendObjId)) {
			Player friendPlayer = World.getInstance().getPlayer(friendObjId);
			if (friendPlayer != null) {
				friendPlayer.getFriendList().delFriend(deleter.getObjectId());
				PacketSendUtility.sendPacket(friendPlayer, new SM_FRIEND_LIST());
				PacketSendUtility.sendPacket(friendPlayer, new SM_FRIEND_NOTIFY(SM_FRIEND_NOTIFY.DELETED, deleter.getName()));
			}
			// Delete from deleter's friend list and send packets
			deleter.getFriendList().delFriend(friendObjId);
			PacketSendUtility.sendPacket(deleter, new SM_FRIEND_LIST());
			PacketSendUtility.sendPacket(deleter, SM_FRIEND_RESPONSE.TARGET_REMOVED(friend.getName()));
			return true;
		}
		return false;
	}
}
