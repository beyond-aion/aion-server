package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.concurrent.Future;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author antness thx to Guapo for sniffing
 */
public class CM_READ_EXPRESS_MAIL extends AionClientPacket {

	private int action;

	public CM_READ_EXPRESS_MAIL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		this.action = readC();
	}

	@Override
	protected void runImpl() {

		final Player player = getConnection().getActivePlayer();
		boolean haveUnreadExpress = player.getMailbox().haveUnreadByType(LetterType.EXPRESS);
		boolean haveUnreadBlackcloud = player.getMailbox().haveUnreadByType(LetterType.BLACKCLOUD);

		switch (this.action) {
			case 0:
				// window is closed
				if (player.getPostman() != null) {
					player.getPostman().getController().delete();
					player.setPostman(null);
				}
				break;
			case 1:
				// click on icon
				if (player.getPostman() != null) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_POSTMAN_ALREADY_SUMMONED());
				} else if (player.isFlying()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_POSTMAN_UNABLE_IN_FLIGHT());
				} else if (haveUnreadBlackcloud) {
					VisibleObjectSpawner.spawnPostman(player);
				} else if (haveUnreadExpress) {
					if (player.getController().hasScheduledTask(TaskId.EXPRESS_MAIL_USE)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_POSTMAN_UNABLE_IN_COOLTIME());
						return;
					}

					VisibleObjectSpawner.spawnPostman(player);
					Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
						}

					}, 600000); // 10 min
					player.getController().addTask(TaskId.EXPRESS_MAIL_USE, task);
				}
				break;
		}
	}

}
