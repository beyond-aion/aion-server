package com.aionemu.gameserver.utils;

import java.util.function.Predicate;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.SiegeZoneInstance;

/**
 * This class contains static methods, which are utility methods, all of them are interacting only with objects passed as parameters.<br>
 * These methods could be placed directly into Player class, but we want to keep Player class as a pure data holder.
 *
 * @author Luno, Neon
 */
public class PacketSendUtility {

	public static void sendMessage(Player player, String msg) {
		sendPacket(player, new SM_MESSAGE(0, null, msg, ChatType.GOLDEN_YELLOW));
	}

	public static void sendMessage(Player player, String msg, ChatType chatType) {
		sendPacket(player, new SM_MESSAGE(0, null, msg, chatType));
	}

	/**
	 * Lets the player say a client message to himself. He (but no one else) will see the message in /s chat and in a speech bubble above his head.
	 */
	public static void sendMonologue(Player player, int msgId, Object... params) {
		sendPacket(player, new SM_SYSTEM_MESSAGE(ChatType.NORMAL, player, msgId, params));
	}

	/**
	 * Lets the npc say a client message to the player. He will see the message in /s chat and in a speech bubble above the npcs head.
	 */
	public static void sendMessage(Player player, Npc npc, int msgId, Object... params) {
		if (npc != null)
			broadcastPacket(npc, new SM_SYSTEM_MESSAGE(ChatType.NPC, npc, msgId, params));
	}

	/**
	 * Lets the npc say a client message to all known players. They will see the message in /s chat and in a speech bubble above the npcs head. Note
	 * that this method does not support optional message parameters, to avoid ambiguity with {@link #broadcastMessage(Npc, int, int, Object...)}. Use
	 * that method instead, if you need to pass parameters.
	 */
	public static void broadcastMessage(Npc npc, int msgId) {
		broadcastMessage(npc, msgId, 0);
	}

	/**
	 * Lets the npc say a client message after the specified delay (in milliseconds) to all known players. They will see the message in /s chat and in a
	 * speech bubble above the npcs head.
	 */
	public static void broadcastMessage(Npc npc, int msgId, int delay, Object... msgParams) {
		if (npc == null)
			return;
		schedule(() -> {
			if (npc.isSpawned())
				broadcastPacket(npc, new SM_SYSTEM_MESSAGE(ChatType.NPC, npc, msgId, msgParams));
		}, delay);
	}

	/**
	 * Sends a packet to the given player
	 */
	public static void sendPacket(Player player, AionServerPacket packet) {
		if (player.isOnline())
			player.getClientConnection().sendPacket(packet);
	}

	/**
	 * Broadcasts a packet to all players, that the given player knows.
	 *
	 * @param player
	 * @param packet
	 *          ServerPacket that will be broadcast
	 * @param toSelf
	 *          true if packet should also be sent to this player
	 */
	public static void broadcastPacket(Player player, AionServerPacket packet, boolean toSelf) {
		if (toSelf)
			sendPacket(player, packet);

		broadcastPacket(player, packet);
	}

	/**
	 * Broadcasts a packet to all players, that the given player knows and are in/not in his team.
	 *
	 * @param player
	 * @param packet
	 *          ServerPacket that will be broadcast
	 * @param toSelf
	 *          true if packet should also be sent to this player
	 * @param toTeam
	 *          true if packet should be sent to this player team or all others w/o team members
	 */
	public static void broadcastPacketTeam(Player player, AionServerPacket packet, boolean toSelf, boolean toTeam) {
		if (toSelf)
			sendPacket(player, packet);

		player.getKnownList().forEachPlayer(p -> {
			if (toTeam) {
				if (p.isInTeam() && p.getCurrentTeamId() == player.getCurrentTeamId()) {
					sendPacket(p, packet);
				}
			} else if (!p.isInTeam() || p.getCurrentTeamId() != player.getCurrentTeamId()) {
				sendPacket(p, packet);
			}
		});
	}

	/**
	 * Broadcasts a packet to all players, that the given object knows and the object itself, if it's a player.
	 *
	 * @param visibleObject
	 * @param packet
	 */
	public static void broadcastPacketAndReceive(VisibleObject visibleObject, AionServerPacket packet) {
		if (visibleObject instanceof Player)
			sendPacket((Player) visibleObject, packet);

		broadcastPacket(visibleObject, packet);
	}

	public static void broadcastPacketAndReceive(Creature creature, AionServerPacket packet, AIEventType et) {
		if (creature instanceof Player)
			sendPacket((Player) creature, packet);

		broadcastPacketAndAIEvent(creature, packet, et);
	}

	/**
	 * Broadcasts a packet to all players that the given object knows.
	 *
	 * @param visibleObject
	 * @param packet
	 */
	public static void broadcastPacket(VisibleObject visibleObject, AionServerPacket packet) {
		visibleObject.getKnownList().forEachPlayer(player -> sendPacket(player, packet));
	}

	public static void broadcastPacketAndAIEvent(Creature creature, AionServerPacket packet, AIEventType et) {
		creature.getKnownList().forEachObject(object -> {
			if (object instanceof Player)
				sendPacket((Player) object, packet);
			else if (et != null && object instanceof Npc)
				((Npc) object).getAi2().onCreatureEvent(et, creature);
		});
	}

	/**
	 * Broadcasts a packet to all players that the given object knows and matches the filter.
	 *
	 * @param object
	 * @param packet
	 *          ServerPacket to be broadcast
	 * @param toSelf
	 *          true if packet should also be sent to this player
	 * @param filter
	 *          filter determining who should be messaged
	 */
	public static void broadcastPacket(VisibleObject object, AionServerPacket packet, boolean toSelf, Predicate<Player> filter) {
		if (toSelf && object instanceof Player)
			sendPacket((Player) object, packet);

		object.getKnownList().forEachPlayer(player -> {
			if (filter.test(player))
				sendPacket(player, packet);
		});
	}

	/**
	 * Broadcasts a packet after the specified delay (in milliseconds) to all players that the given object knows.
	 */
	public static void broadcastPacket(VisibleObject object, AionServerPacket packet, int delay) {
		schedule(() -> broadcastPacket(object, packet), delay);
	}

	/**
	 * Broadcasts a packet to all logged in players.
	 */
	public static void broadcastToWorld(AionServerPacket packet) {
		World.getInstance().forEachPlayer(player -> sendPacket(player, packet));
	}

	/**
	 * Broadcasts a packet to all logged in players matching a filter.
	 *
	 * @param packet
	 *          ServerPacket to be broadcast
	 * @param filter
	 *          filter determining who should be messaged
	 */
	public static void broadcastToWorld(AionServerPacket packet, Predicate<Player> filter) {
		World.getInstance().forEachPlayer(player -> {
			if (filter.test(player))
				sendPacket(player, packet);
		});
	}

	/**
	 * Broadcasts packet to all legion members of a legion
	 *
	 * @param legion
	 *          Legion to broadcast packet to
	 * @param packet
	 *          ServerPacket to be broadcast
	 */
	public static void broadcastToLegion(Legion legion, AionServerPacket packet) {
		for (Player onlineLegionMember : legion.getOnlineLegionMembers()) {
			sendPacket(onlineLegionMember, packet);
		}
	}

	public static void broadcastToLegion(Legion legion, AionServerPacket packet, int playerObjId) {
		for (Player onlineLegionMember : legion.getOnlineLegionMembers()) {
			if (onlineLegionMember.getObjectId() != playerObjId)
				sendPacket(onlineLegionMember, packet);
		}
	}

	/**
	 * Broadcasts a packet to all players who can see the specified object.
	 */
	public static void broadcastToSightedPlayers(VisibleObject object, AionServerPacket packet) {
		broadcastToSightedPlayers(object, packet, false);
	}

	/**
	 * Broadcasts a packet to all players who can see the specified object.
	 */
	public static void broadcastToSightedPlayers(VisibleObject object, AionServerPacket packet, boolean toSelf) {
		broadcastPacket(object, packet, toSelf, other -> other.getKnownList().sees(object));
	}

	/**
	 * Broadcasts the system message to all players that are on the same map instance as the object.
	 */
	public static void broadcastToMap(VisibleObject object, int msgId) {
		broadcastToMap(object, new SM_SYSTEM_MESSAGE(msgId), 0);
	}

	/**
	 * Broadcasts the system message after the specified delay (in milliseconds) to all players that are on the same map instance as the object.
	 */
	public static void broadcastToMap(VisibleObject object, int msgId, int delay) {
		broadcastToMap(object, new SM_SYSTEM_MESSAGE(msgId), delay);
	}

	/**
	 * Broadcasts a packet to all players that are on the same map instance as the object.
	 */
	public static void broadcastToMap(VisibleObject object, AionServerPacket packet) {
		broadcastToMap(object, packet, 0);
	}

	/**
	 * Broadcasts a packet after the specified delay (in milliseconds) to all players that are on the same map instance as the object.
	 */
	public static void broadcastToMap(VisibleObject object, AionServerPacket packet, int delay) {
		schedule(() -> object.getPosition().getWorldMapInstance().forEachPlayer(player -> {
			if (!object.equals(player))
				sendPacket(player, packet);
		}), delay);
	}

	/**
	 * Broadcasts a packet to all players that are on the default map instance.
	 */
	public static void broadcastToMap(WorldMap map, AionServerPacket packet) {
		broadcastToMap(map.getMainWorldMapInstance(), packet, 0);
	}

	/**
	 * Broadcasts a packet after the specified delay (in milliseconds) to all players that are on the default map instance.
	 */
	public static void broadcastToMap(WorldMap map, AionServerPacket packet, int delay) {
		broadcastToMap(map.getMainWorldMapInstance(), packet, delay);
	}

	/**
	 * Broadcasts a packet after the specified delay (in milliseconds) to all players that are on the specified map instance.
	 */
	public static void broadcastToMap(WorldMapInstance mapInstance, AionServerPacket packet, int delay) {
		schedule(() -> mapInstance.forEachPlayer(player -> sendPacket(player, packet)), delay);
	}

	public static void broadcastToZone(SiegeZoneInstance zone, AionServerPacket packet) {
		schedule(() -> zone.forEachPlayer(player -> sendPacket(player, packet)), 0);
	}

	private static void schedule(Runnable r, int delay) {
		if (delay <= 0)
			ThreadPoolManager.getInstance().execute(r);
		else
			ThreadPoolManager.getInstance().schedule(r, delay);
	}
}
