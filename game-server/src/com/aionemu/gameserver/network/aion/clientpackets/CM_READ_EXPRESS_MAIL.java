package com.aionemu.gameserver.network.aion.clientpackets;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author antness thx to Guapo for sniffing
 */
public class CM_READ_EXPRESS_MAIL extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_READ_EXPRESS_MAIL.class);
	private static final Map<Integer, Long> cooldownByPlayer = new ConcurrentHashMap<>();
	private byte action;

	public CM_READ_EXPRESS_MAIL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		action = readC();
	}

	@Override
	protected void runImpl() {
		final Player player = getConnection().getActivePlayer();

		switch (action) {
			case 0: // window is closed
				if (player.getPostman() != null)
					player.getPostman().getController().delete();
				break;
			case 1: // click on icon
				boolean haveUnreadExpress = player.getMailbox().haveUnreadByType(LetterType.EXPRESS);
				boolean haveUnreadBlackcloud = player.getMailbox().haveUnreadByType(LetterType.BLACKCLOUD);
				if (player.getPostman() != null) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_POSTMAN_ALREADY_SUMMONED());
				} else if (player.isFlying()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_POSTMAN_UNABLE_IN_FLIGHT());
				} else if (!haveUnreadExpress && !haveUnreadBlackcloud) {
					return;
				} else if (!haveUnreadBlackcloud && cooldownByPlayer.getOrDefault(player.getObjectId(), 0L) > System.currentTimeMillis()) { // black cloud mail ignores the cooldown
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_POSTMAN_UNABLE_IN_COOLTIME());
				} else {
					VisibleObjectSpawner.spawnPostman(player);
					startPostmanCooldown(player);
				}
				break;
			default:
				log.warn("{} sent unknown read express mail action type: {}", player, action);
		}
	}

	public static void startPostmanCooldown(Player player) {
		long now = System.currentTimeMillis();
		cooldownByPlayer.put(player.getObjectId(), now + Duration.ofMinutes(10).toMillis());
		cooldownByPlayer.values().removeIf(expirationTime -> expirationTime < now);
	}
}
