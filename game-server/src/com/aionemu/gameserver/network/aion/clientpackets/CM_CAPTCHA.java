package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CAPTCHA;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Cura
 */
public class CM_CAPTCHA extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_CAPTCHA.class);

	private int type;
	private int count;
	private String word;

	public CM_CAPTCHA(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		type = readUC();

		switch (type) {
			case 2:
				count = readUC();
				word = readS();
				break;
			case 4: // /ExtractStatus
				break;
			default:
				log.warn("Unknown CAPTCHA packet type " + type);
				break;
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		switch (type) {
			case 2:
				if (player.getCaptchaWord().equalsIgnoreCase(word)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CAPTCHA_UNRESTRICT());
					PacketSendUtility.sendPacket(player, new SM_CAPTCHA(true, 0));

					PunishmentService.setIsNotGatherable(player, 0, false, 0);

					// fp bonus (like retail)
					player.getLifeStats().increaseFp(TYPE.FP, SecurityConfig.CAPTCHA_BONUS_FP_TIME, 0, LOG.REGULAR);
				} else {
					int banTime = SecurityConfig.CAPTCHA_EXTRACTION_BAN_TIME + (SecurityConfig.CAPTCHA_EXTRACTION_BAN_ADD_TIME * count);

					if (count < 3) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CAPTCHA_UNRESTRICT_FAILED_RETRY(3 - count));
						PacketSendUtility.sendPacket(player, new SM_CAPTCHA(false, banTime));
						PunishmentService.setIsNotGatherable(player, count, true, banTime * 1000L);
					} else {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CAPTCHA_UNRESTRICT_FAILED());
						PunishmentService.setIsNotGatherable(player, count, true, banTime * 1000L);
					}
				}
				break;
			case 4:
				if (player.isGatherRestricted())
					sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_CAPTCHA_RESTRICTED(player.getGatherRestrictionDurationSeconds()));
				else
					sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_CAPTCHA_NOT_RESTRICTED());
		}
	}
}
