package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * Packet that reads Whisper chat messages.<br>
 * 
 * @author SoulKeeper
 */
public class CM_CHAT_MESSAGE_WHISPER extends AionClientPacket {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger("CHAT_LOG");

	/**
	 * To whom this message is sent
	 */
	private String name;

	/**
	 * Message text
	 */
	private String message;

	/**
	 * Constructs new client packet instance.
	 * 
	 * @param opcode
	 */
	public CM_CHAT_MESSAGE_WHISPER(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);

	}

	/**
	 * Read message
	 */
	@Override
	protected void readImpl() {
		name = readS();
		message = readS();
	}

	/**
	 * Print debug info
	 */
	@Override
	protected void runImpl() {
		String realName = ChatUtil.getRealAdminName(name);
		Player sender = getConnection().getActivePlayer();
		Player receiver = World.getInstance().findPlayer(realName);

		if (sender.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_CHATMESSAGES) {
			PacketSendUtility.sendPacket(sender, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(sender, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		if (LoggingConfig.LOG_CHAT)
			log.info(String.format("[MESSAGE] [%s] Whisper To: %s, Message: %s", sender.getName(), realName, message));

		if (receiver == null) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(realName));
		} else if (!receiver.isWispable() && !sender.isGM()) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_WHISPER_REFUSE(receiver.getName(AdminConfig.CUSTOMTAG_ENABLE)));
		} else if (sender.getLevel() < CustomConfig.LEVEL_TO_WHISPER) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_CANT_WHISPER_LEVEL(String.valueOf(CustomConfig.LEVEL_TO_WHISPER)));
		} else if (receiver.getBlockList().contains(sender.getObjectId())) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_YOU_EXCLUDED(receiver.getName()));
		} else if (!CustomConfig.SPEAKING_BETWEEN_FACTIONS && (sender.getRace().getRaceId() != receiver.getRace().getRaceId()) && !sender.isGM()
			&& !receiver.isGM()) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(realName));
		} else {
			if (RestrictionsManager.canChat(sender))
				PacketSendUtility.sendPacket(receiver, new SM_MESSAGE(sender, NameRestrictionService.filterMessage(message), ChatType.WHISPER));
		}
	}
}
