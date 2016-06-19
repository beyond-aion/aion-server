package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PONG;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author -Nemesiss-
 * @modified Undertrey, Neon
 */
public class CM_PING extends AionClientPacket {

	public static final int CLIENT_PING_INTERVAL = 180 * 1000; // client sends CM_PING every 180 seconds

	public CM_PING(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		readH(); // unk
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		long lastMS = getConnection().getLastPingTime();
		getConnection().setLastPingTime(System.currentTimeMillis());

		if (lastMS > 0 && player != null) {
			long pingInterval = System.currentTimeMillis() - lastMS;
			if (pingInterval + 5000 < CLIENT_PING_INTERVAL) { // client timer cheat
				if (getConnection().increaseAndGetPingFailCount() == 3) { // allow 2 detections in a row, before taking actions
					if (SecurityConfig.PINGCHECK_KICK) {
						AuditLogger.info(player,
							"Possible time/speed hack (client ping interval: " + pingInterval + "/" + CLIENT_PING_INTERVAL + "), kicking player");
						getConnection().close();
					} else {
						AuditLogger.info(player, "Possible time/speed hack (client ping interval: " + pingInterval + "/" + CLIENT_PING_INTERVAL + ")");
						getConnection().resetPingFailCount();
					}
				}
			} else {
				getConnection().resetPingFailCount();
			}
		}
		sendPacket(new SM_PONG());
	}
}
