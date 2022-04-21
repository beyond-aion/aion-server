package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PONG;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author -Nemesiss-, Undertrey, Neon
 */
public class CM_PING extends AionClientPacket {

	public static final int CLIENT_PING_INTERVAL = 180 * 1000; // client sends this packet every 180 seconds

	public CM_PING(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readH(); // unk
	}

	@Override
	protected void runImpl() {
		long lastPingMillis = getConnection().getLastPingTime();
		long nowMillis = System.currentTimeMillis();
		getConnection().setLastPingTime(nowMillis);
		sendPacket(new SM_PONG());

		if (lastPingMillis > 0) {
			long pingInterval = nowMillis - lastPingMillis;
			if (pingInterval + 2000 < CLIENT_PING_INTERVAL) { // client timer cheat
				if (getConnection().increaseAndGetPingFailCount() == 3) {
					if (SecurityConfig.PINGCHECK_KICK) {
						AuditLogger.log(getConnection().getActivePlayer(),
								"possibly using time/speed hack (client ping interval: " + pingInterval + "/" + CLIENT_PING_INTERVAL + "), kicking player");
						getConnection().close();
					} else {
						AuditLogger.log(getConnection().getActivePlayer(), "possibly using time/speed hack (client ping interval: " + pingInterval + "/" + CLIENT_PING_INTERVAL + ")");
						getConnection().resetPingFailCount();
					}
				}
			} else {
				getConnection().resetPingFailCount();
			}
		}
	}
}
