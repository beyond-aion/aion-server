package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * Client sends this every 30 seconds with no data and once when entering world
 * 
 * @author Neon
 */
public class CM_PING_INGAME extends AionClientPacket {

	public static final int CLIENT_PING_INTERVAL = 30 * 1000 + 33; // client sends this packet every ~30.033 seconds

	public CM_PING_INGAME(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		long lastPingMillis = getConnection().getLastPingTime();
		long nowMillis = System.currentTimeMillis();
		getConnection().setLastPingTime(nowMillis);

		if (lastPingMillis > 0) {
			long pingInterval = nowMillis - lastPingMillis;
			if (pingInterval + 2000 < CLIENT_PING_INTERVAL) { // client timer cheat
				if (getConnection().increaseAndGetPingFailCount() == 3) { // CM_PING_INGAME + CM_PING both update ping time, so don't immediately trigger
					if (SecurityConfig.PINGCHECK_KICK) {
						AuditLogger.log(player,
								"possibly using time/speed hack (client ping interval: " + pingInterval + "/" + CLIENT_PING_INTERVAL + "), kicking player");
						getConnection().close();
					} else {
						AuditLogger.log(player, "possibly using time/speed hack (client ping interval: " + pingInterval + "/" + CLIENT_PING_INTERVAL + ")");
						getConnection().resetPingFailCount();
					}
				}
			} else {
				getConnection().resetPingFailCount();
			}
		}
	}
}
