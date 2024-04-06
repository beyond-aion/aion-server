package com.aionemu.gameserver.utils;

import java.util.function.Predicate;

import com.aionemu.gameserver.ai.event.AIEventType;
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
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;

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
		sendPacket(player, new SM_SYSTEM_MESSAGE(ChatType.NPC, npc, msgId, params));
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
		scheduleOrRun(() -> {
			if (npc.isSpawned())
				broadcastPacket(npc, new SM_SYSTEM_MESSAGE(ChatType.NPC, npc, msgId, msgParams), player -> PositionUtil.isInRange(npc, player, 50, false));
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
	 * Broadcasts a packet to all players that the given object knows.
	 */
	public static void broadcastPacket(VisibleObject object, AionServerPacket packet) {
		object.getKnownList().forEachPlayer(player -> sendPacket(player, packet));
	}

	/**
	 * Broadcasts a packet to all players that the given object knows and which match the filter.
	 */
	public static void broadcastPacket(VisibleObject object, AionServerPacket packet, Predicate<Player> filter) {
		object.getKnownList().forEachPlayer(player -> {
			if (filter.test(player))
				sendPacket(player, packet);
		});
	}

	/**
	 * Broadcasts a packet to all players, that the given object knows and the object itself, if it's a player.
	 */
	public static void broadcastPacketAndReceive(VisibleObject visibleObject, AionServerPacket packet) {
		if (visibleObject instanceof Player player)
			sendPacket(player, packet);

		broadcastPacket(visibleObject, packet);
	}

	public static void broadcastPacketAndReceive(Creature creature, AionServerPacket packet, AIEventType et) {
		if (creature instanceof Player player)
			sendPacket(player, packet);

		broadcastPacketAndAIEvent(creature, packet, et);
	}

	public static void broadcastPacketAndAIEvent(Creature creature, AionServerPacket packet, AIEventType et) {
		creature.getKnownList().forEachObject(object -> {
			if (object instanceof Player player)
				sendPacket(player, packet);
			else if (et != null && object instanceof Npc npc)
				npc.getAi().onCreatureEvent(et, creature);
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
		if (toSelf && object instanceof Player player)
			sendPacket(player, packet);

		object.getKnownList().forEachPlayer(player -> {
			if (filter.test(player))
				sendPacket(player, packet);
		});
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
		broadcastToMap(object.getPosition().getWorldMapInstance(), new SM_SYSTEM_MESSAGE(msgId), 0, p -> !object.equals(p));
	}

	/**
	 * Broadcasts the system message after the specified delay (in milliseconds) to all players that are on the same map instance as the object.
	 */
	public static void broadcastToMap(VisibleObject object, int msgId, int delay) {
		broadcastToMap(object.getPosition().getWorldMapInstance(), new SM_SYSTEM_MESSAGE(msgId), delay, p -> !object.equals(p));
	}

	/**
	 * Broadcasts a packet to all players that are on the same map instance as the object.
	 */
	public static void broadcastToMap(VisibleObject object, AionServerPacket packet) {
		broadcastToMap(object.getPosition().getWorldMapInstance(), packet, 0, p -> !object.equals(p));
	}

	/**
	 * Broadcasts a packet after the specified delay (in milliseconds) to all players that are on the same map instance as the object.
	 */
	public static void broadcastToMap(VisibleObject object, AionServerPacket packet, int delay) {
		broadcastToMap(object.getPosition().getWorldMapInstance(), packet, delay, p -> !object.equals(p));
	}

	/**
	 * Broadcasts a packet after the specified delay (in milliseconds) to all players that are on the same map instance as the object and match the
	 * given filter.
	 */
	public static void broadcastToMap(VisibleObject object, AionServerPacket packet, int delay, Predicate<Player> filter) {
		broadcastToMap(object.getPosition().getWorldMapInstance(), packet, delay, filter);
	}

	/**
	 * Broadcasts a packet to all players that are on the specified map instance.
	 */
	public static void broadcastToMap(WorldMapInstance mapInstance, AionServerPacket packet) {
		broadcastToMap(mapInstance, packet, 0, player -> true);
	}

	/**
	 * Broadcasts a packet after the specified delay (in milliseconds) to all players that are on the specified map instance.
	 */
	public static void broadcastToMap(WorldMapInstance mapInstance, AionServerPacket packet, int delay) {
		broadcastToMap(mapInstance, packet, delay, player -> true);
	}

	/**
	 * Broadcasts a packet after the specified delay (in milliseconds) to all players that are on the specified map instance and match the given filter.
	 */
	public static void broadcastToMap(WorldMapInstance mapInstance, AionServerPacket packet, int delay, Predicate<Player> filter) {
		scheduleOrRun(() -> mapInstance.forEachPlayer(player -> {
			if (filter.test(player))
				sendPacket(player, packet);
		}), delay);
	}

	public static void broadcastToZone(ZoneInstance zone, AionServerPacket packet) {
		scheduleOrRun(() -> zone.forEach(creature -> {
			if (creature instanceof Player player)
				sendPacket(player, packet);
		}), 0);
	}

	private static void scheduleOrRun(Runnable r, int delay) {
		if (delay <= 0)
			r.run();
		else
			ThreadPoolManager.getInstance().schedule(r, delay);
	}
}
