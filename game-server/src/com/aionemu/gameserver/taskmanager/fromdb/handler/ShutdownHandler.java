package com.aionemu.gameserver.taskmanager.fromdb.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ShutdownHook;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Divinity, nrg
 */
public class ShutdownHandler extends TaskFromDBHandler {

	private static final Logger log = LoggerFactory.getLogger(ShutdownHandler.class);
	private int countDown;
	private int announceInterval;
	private int warnCountDown;

	@Override
	public boolean isValid() {
		if (params.length == 3) {
			try {
				countDown = Integer.parseInt(params[0]);
				announceInterval = Integer.parseInt(params[1]);
				warnCountDown = Integer.parseInt(params[2]);

				return true;
			} catch (NumberFormatException e) {
				log.warn("Invalid parameters for ShutdownHandler. Only valid integers allowed - not registered", e);
			}
		}
		log.warn("ShutdownHandler has more or less than 3 parameters - not registered");
		return false;
	}

	@Override
	public void trigger() {
		log.info("Task[" + taskId + "] launched : shuting down the server !");

		PacketSendUtility.broadcastToWorld(SM_SYSTEM_MESSAGE.STR_SERVER_SHUTDOWN(warnCountDown + countDown));

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				ShutdownHook.getInstance().shutdown(countDown, announceInterval, ShutdownHook.ShutdownMode.SHUTDOWN);
			}
		}, warnCountDown * 1000);
	}
}
