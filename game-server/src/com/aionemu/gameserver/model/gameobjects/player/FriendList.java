package com.aionemu.gameserver.model.gameobjects.player;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_NOTIFY;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_UPDATE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * Represents a player's Friend list
 * 
 * @author Ben
 */
public class FriendList implements Iterable<Friend> {

	private final Map<Integer, Friend> friends = new ConcurrentHashMap<>();
	private final Player player;
	private Status status = Status.OFFLINE;

	/**
	 * Constructs a friend list for the given player, with the given friends
	 * 
	 * @param player
	 *          Player who has this friend list
	 * @param friends
	 *          Friends on the list
	 */
	public FriendList(Player owner, Collection<Friend> friends) {
		for (Friend friend : friends)
			this.friends.put(friend.getObjectId(), friend);
		this.player = owner;
	}

	/**
	 * Gets the friend with this objId<br />
	 * Returns null if it is not our friend
	 * 
	 * @param objId
	 *          objId of friend
	 * @return Friend
	 */
	public Friend getFriend(int objId) {
		return friends.get(objId);
	}

	/**
	 * Returns number of friends in list
	 * 
	 * @return Num Friends in list
	 */
	public int getSize() {
		return friends.size();
	}

	/**
	 * Adds the given friend to the list<br />
	 * To add a friend in the database, see <tt>PlayerService</tt>
	 * 
	 * @param friend
	 */
	public void addFriend(Friend friend) {
		friends.put(friend.getObjectId(), friend);
	}

	/**
	 * Gets the Friend by this name
	 * 
	 * @param name
	 *          Name of friend
	 * @return Friend matching name
	 */
	public Friend getFriend(String name) {
		for (Friend friend : friends.values())
			if (friend.getName().equalsIgnoreCase(name))
				return friend;
		return null;
	}

	/**
	 * Deletes given friend from this friends list<br />
	 * <ul>
	 * <li>Note: This will only affect this player, not the friend.</li>
	 * <li>Note: Sends the packet to update the client automatically</li>
	 * <li>Note: You should use requestDel to delete from both lists</li>
	 * </ul>
	 * 
	 * @param friend
	 */
	public void delFriend(int friendOid) {
		friends.remove(friendOid);
	}

	public boolean isFull() {
		return getSize() >= CustomConfig.FRIENDLIST_SIZE;
	}

	/**
	 * Gets players status
	 * 
	 * @return Status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the status of the player<br />
	 * <ul>
	 * <li>Note: Does not update friends</li>
	 * </ul>
	 * 
	 * @param status
	 */
	public void setStatus(Status status, PlayerCommonData pcd) {
		Status previousStatus = this.status;
		this.status = status;

		for (Friend friend : friends.values()) {
			Player friendPlayer = World.getInstance().getPlayer(friend.getObjectId());
			if (friendPlayer == null)
				continue;

			friendPlayer.getFriendList().getFriend(pcd.getPlayerObjId()).setPCD(pcd);
			PacketSendUtility.sendPacket(friendPlayer, new SM_FRIEND_UPDATE(player.getObjectId()));

			if (previousStatus == Status.OFFLINE) {
				// Show LOGIN message
				PacketSendUtility.sendPacket(friendPlayer, new SM_FRIEND_NOTIFY(SM_FRIEND_NOTIFY.LOGIN, player.getName()));
			} else if (status == Status.OFFLINE) {
				// Show LOGOUT message
				PacketSendUtility.sendPacket(friendPlayer, new SM_FRIEND_NOTIFY(SM_FRIEND_NOTIFY.LOGOUT, player.getName()));
			}
		}

	}

	@Override
	public Iterator<Friend> iterator() {
		return friends.values().iterator();
	}

	public enum Status {
		/**
		 * User is offline or invisible
		 */
		OFFLINE((byte) 0),
		/**
		 * User is online
		 */
		ONLINE((byte) 1),
		/**
		 * User is away or busy
		 */
		AWAY((byte) 3);

		byte value;

		private Status(byte value) {
			this.value = value;
		}

		public byte getId() {
			return value;
		}

		/**
		 * Gets the Status from its int value<br />
		 * Returns null if out of range
		 * 
		 * @param value
		 *          range 0-3
		 * @return Status
		 */
		public static Status getByValue(byte value) {
			for (Status stat : values())
				if (stat.getId() == value)
					return stat;
			return null;
		}
	}
}
