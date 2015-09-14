package com.aionemu.gameserver.services;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.BlockListDAO;
import com.aionemu.gameserver.dao.FriendListDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.BlockedPlayer;
import com.aionemu.gameserver.model.gameobjects.player.Friend;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BLOCK_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BLOCK_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_NOTIFY;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_RESPONSE;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.world.World;

/**
 * Handles activities related to social groups ingame such as the buddy list, legions, etc
 * 
 * @author Ben
 */
public class SocialService {

	/**
	 * Blocks the given object ID for the given player.<br />
	 * <ul>
	 * <li>Does not send packets</li>
	 * </ul>
	 * 
	 * @param player
	 * @param blockedPlayer
	 * @param reason
	 * @return Success
	 */
	public static boolean addBlockedUser(Player player, Player blockedPlayer, String reason) {
		if (DAOManager.getDAO(BlockListDAO.class).addBlockedUser(player.getObjectId(), blockedPlayer.getObjectId(), reason)) {
			player.getBlockList().add(new BlockedPlayer(blockedPlayer.getCommonData(), reason));

			player.getClientConnection().sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.BLOCK_SUCCESSFUL, blockedPlayer.getName()));
			player.getClientConnection().sendPacket(new SM_BLOCK_LIST());

			return true;
		}
		return false;
	}

	/**
	 * Unblocks the given object ID for the given player.<br />
	 * <ul>
	 * <li>Does not send packets</li>
	 * </ul>
	 * 
	 * @param player
	 * @param blockedUserId
	 *          ID of player to unblock
	 * @return Success
	 */
	public static boolean deleteBlockedUser(Player player, int blockedUserId) {
		if (DAOManager.getDAO(BlockListDAO.class).delBlockedUser(player.getObjectId(), blockedUserId)) {
			player.getBlockList().remove(blockedUserId);
			player.getClientConnection()
				.sendPacket(
					new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.UNBLOCK_SUCCESSFUL, DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonData(blockedUserId)
						.getName()));

			player.getClientConnection().sendPacket(new SM_BLOCK_LIST());
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
	 * @return Success - May be false if the reason was the same and therefore not edited
	 */
	public static boolean setBlockedReason(Player player, BlockedPlayer target, String reason) {

		if (!target.getReason().equals(reason)) {
			if (DAOManager.getDAO(BlockListDAO.class).setReason(player.getObjectId(), target.getObjId(), reason)) {
				target.setReason(reason);
				player.getClientConnection().sendPacket(new SM_BLOCK_LIST());
				return true;
			}
		}
		return false;
	}

	public static boolean setFriendMemo(Player player, Friend target, String memo) {

		// if (!target.getReason().equals(reason)) {
		if (DAOManager.getDAO(FriendListDAO.class).setFriendMemo(player.getObjectId(), target.getOid(), memo)) {
			target.setFriendMemo(memo);
			player.getClientConnection().sendPacket(new SM_FRIEND_LIST());
			return true;
		}

		// public abstract boolean setFriendMemo(int playerObjId, int friendObjId, String memo);
		// }
		return false;
	}

	/**
	 * Adds two players to each others friend lists, and updates the database<br />
	 * 
	 * @param friend1
	 * @param friend2
	 */
	public static void makeFriends(Player friend1, Player friend2) {
		DAOManager.getDAO(FriendListDAO.class).addFriends(friend1, friend2);

		friend1.getFriendList().addFriend(new Friend(friend2.getCommonData(), ""));
		friend2.getFriendList().addFriend(new Friend(friend1.getCommonData(), ""));

		friend1.getClientConnection().sendPacket(new SM_FRIEND_LIST());
		friend2.getClientConnection().sendPacket(new SM_FRIEND_LIST());

		friend1.getClientConnection().sendPacket(new SM_FRIEND_RESPONSE(friend2.getName(), SM_FRIEND_RESPONSE.TARGET_ADDED));
		friend2.getClientConnection().sendPacket(new SM_FRIEND_RESPONSE(friend1.getName(), SM_FRIEND_RESPONSE.TARGET_ADDED));
	}

	/**
	 * Deletes two players from eachother's friend lists, and updates the database
	 * <ul>
	 * <li>Note: Does not send notification packets, and does not send new list packet
	 * </ul>
	 * </li>
	 * 
	 * @param deleter
	 *          Player deleting a friend
	 * @param exFriend2Id
	 *          Object ID of the friend he is deleting
	 */
	public static void deleteFriend(Player deleter, int exFriend2Id) {

		// If the DAO is successful
		if (DAOManager.getDAO(FriendListDAO.class).delFriends(deleter.getObjectId(), exFriend2Id)) {
			// Try to get the target player from the cache
			Player friend2Player = PlayerService.getCachedPlayer(exFriend2Id);
			// If the cache doesn't have this player, try to get him from the world
			if (friend2Player == null)
				friend2Player = World.getInstance().findPlayer(exFriend2Id);

			String friend2Name = friend2Player != null ? friend2Player.getName() : DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonData(exFriend2Id)
				.getName();

			// Delete from deleter's friend list and send packets
			deleter.getFriendList().delFriend(exFriend2Id);

			deleter.getClientConnection().sendPacket(new SM_FRIEND_LIST());
			deleter.getClientConnection().sendPacket(new SM_FRIEND_RESPONSE(friend2Name, SM_FRIEND_RESPONSE.TARGET_REMOVED));

			if (friend2Player != null) {
				friend2Player.getFriendList().delFriend(deleter.getObjectId());

				if (friend2Player.isOnline()) {
					friend2Player.getClientConnection().sendPacket(new SM_FRIEND_NOTIFY(SM_FRIEND_NOTIFY.DELETED, deleter.getName()));
					friend2Player.getClientConnection().sendPacket(new SM_FRIEND_LIST());
				}
			}
		}

	}
}
