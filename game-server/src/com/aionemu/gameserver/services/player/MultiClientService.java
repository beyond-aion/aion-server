package com.aionemu.gameserver.services.player;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ENTER_WORLD_CHECK.Msg;
import com.aionemu.gameserver.world.World;

public class MultiClientService {

	private static final Logger log = LoggerFactory.getLogger(MultiClientService.class);
	private static final Map<Integer, AccountSession> sessionsByAccountId = new ConcurrentHashMap<>();

	/**
	 * @return The reason why the player is not allowed to enter the world, or null if they are.
	 */
	public static Msg tryEnterWorld(Player player, AionConnection con) {
		int maxClients = SecurityConfig.MULTI_CLIENT_MAX_CLIENTS_PER_COMPUTER;
		if (maxClients > 0 && !SecurityConfig.MULTI_CLIENT_IGNORED_MAC_ADDRESSES.contains(con.getMacAddress())) {
			List<Player> playersOnSameComputer = findPlayersOnSameComputer(con);
			if (playersOnSameComputer.size() >= maxClients) {
				log.info("Blocked {} from logging on (multi-client limit of {} reached by {})", player, maxClients, playersOnSameComputer);
				return Msg.CONNECTION_ERROR;
			}
		}
		if (!SecurityConfig.MULTI_CLIENT_ALLOW_BOTH_FACTIONS) {
			sessionsByAccountId.values().removeIf(AccountSession::isExpired);
			synchronized (sessionsByAccountId) {
				Integer matchedAccountId = checkForFactionSwitchCooldownTime(player.getRace(), con);
				if (matchedAccountId != null) {
					log.info("Blocked {} from logging on (faction switch cooldown from account ID {})", player, matchedAccountId);
					return Msg.BOTH_FACTIONS;
				}
				AccountSession accountSession = sessionsByAccountId.computeIfAbsent(player.getAccount().getId(), AccountSession::new);
				accountSession.putIdentifiers(con);
				accountSession.enterWorld(player);
			}
		}
		return null;
	}

	/**
	 * A computer is identified by the IP address along with the MAC address, so different computers behind the same NAT don't collide. HDD serials
	 * are not used, since the client detects and reads them unreliably.
	 */
	private static List<Player> findPlayersOnSameComputer(AionConnection con) {
		String ip = con.getIP();
		String mac = con.getMacAddress();
		return World.getInstance().getAllPlayers().stream().filter(onlinePlayer -> {
			AionConnection onlineCon = onlinePlayer.getClientConnection();
			return onlineCon != null && ip.equals(onlineCon.getIP()) && mac.equals(onlineCon.getMacAddress());
		}).toList();
	}

	public static void onLeaveWorld(Player player) {
		AccountSession session = sessionsByAccountId.get(player.getAccount().getId());
		if (session != null)
			session.leaveWorld(player);
	}

	public static Integer checkForFactionSwitchCooldownTime(Race race, AionConnection con) {
		if (SecurityConfig.MULTI_CLIENT_IGNORED_MAC_ADDRESSES.contains(con.getMacAddress()))
			return null;
		Race oppositeRace = race == Race.ELYOS ? Race.ASMODIANS : Race.ELYOS;
		long minLastOnlineMillis = System.currentTimeMillis() - Duration.ofMinutes(SecurityConfig.MULTI_CLIENT_FACTION_SWITCH_COOLDOWN_MINUTES).toMillis();
		return sessionsByAccountId.values().stream()
			.filter(s -> !s.isIgnored() && s.wasPlayingOnSameIpOrMac(oppositeRace, minLastOnlineMillis, con))
			.findAny()
			.map(s -> s.accountId)
			.orElse(null);
	}

	private static class AccountSession {

		private final int accountId;
		private final Map<Race, Long> lastCharOnlineTimeMillis = new ConcurrentHashMap<>();
		private final Deque<Identifiers> identifiers = new ArrayDeque<>(2);

		public AccountSession(int accountId) {
			this.accountId = accountId;
		}

		synchronized boolean isIgnored() {
			return !identifiers.isEmpty() && SecurityConfig.MULTI_CLIENT_IGNORED_MAC_ADDRESSES.contains(identifiers.getFirst().mac);
		}

		synchronized void putIdentifiers(AionConnection connection) {
			Identifiers ids = new Identifiers(connection.getIP(), connection.getMacAddress());
			if (!identifiers.contains(ids)) {
				identifiers.addFirst(ids);
				while (identifiers.size() > 3)
					identifiers.removeLast();
			}
		}

		synchronized boolean hasAny(String ip, String mac) {
			return identifiers.stream().anyMatch(identifiers -> identifiers.ip.equals(ip) || identifiers.mac.equals(mac));
		}

		boolean isExpired() {
			long minLastOnline = System.currentTimeMillis() - Duration.ofMinutes(SecurityConfig.MULTI_CLIENT_FACTION_SWITCH_COOLDOWN_MINUTES).toMillis();
			return lastCharOnlineTimeMillis.values().stream().noneMatch(t -> t > minLastOnline);
		}

		void enterWorld(Player player) {
			lastCharOnlineTimeMillis.put(player.getRace(), Long.MAX_VALUE);
		}

		void leaveWorld(Player player) {
			lastCharOnlineTimeMillis.put(player.getRace(), System.currentTimeMillis());
		}

		boolean wasPlayingOnSameIpOrMac(Race race, long minLastOnlineMillis, AionConnection con) {
			Long lastOnlineMillis = lastCharOnlineTimeMillis.get(race);
			return lastOnlineMillis != null && lastOnlineMillis > minLastOnlineMillis && hasAny(con.getIP(), con.getMacAddress());
		}
	}

	private record Identifiers(String ip, String mac) {}
}
