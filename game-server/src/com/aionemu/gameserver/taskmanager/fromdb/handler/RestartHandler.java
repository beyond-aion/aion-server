package com.aionemu.gameserver.taskmanager.fromdb.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ShutdownHook;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Divinity, nrg
 */
public class RestartHandler extends TaskFromDBHandler {

	private static final Logger log = LoggerFactory.getLogger(RestartHandler.class);
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
				log.warn("Invalid parameters for RestartHandler. Only valid integers allowed - not registered", e);
			}
		}
		log.warn("RestartHandler has more or less than 3 parameters - not registered");
		return false;
	}

	@Override
	public void trigger() {
		log.info("Task[" + taskId + "] launched : restarting the server !");

		World.getInstance().forEachPlayer(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				PacketSendUtility.sendMessage(player, "Automatic Task: The server will restart in " + warnCountDown
					+ " seconds ! Please find a safe place and disconnect your character.", ChatType.BRIGHT_YELLOW_CENTER);
			}
		});

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				ShutdownHook.getInstance().doShutdown(countDown, announceInterval, ShutdownHook.ShutdownMode.RESTART);
			}
		}, warnCountDown * 1000);
	}
}
