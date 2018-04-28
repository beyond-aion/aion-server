package com.aionemu.gameserver.network.aion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.clientpackets.*;
import com.aionemu.gameserver.utils.Util;

/**
 * @author Neon
 */
public class AionClientPacketFactory {

	private static final Logger log = LoggerFactory.getLogger(AionClientPacketFactory.class);
	private static final Map<Integer, PacketInfo<? extends AionClientPacket>> packets = new HashMap<>();

	static {
		try {
			// packets.put(0x0A5, new PacketInfo<>(CM_LOGIN_OUT.class, State.AUTHED, State.IN_GAME));
			packets.put(0x0A6, new PacketInfo<>(CM_QUIT.class, State.AUTHED, State.IN_GAME));
			packets.put(0x0A7, new PacketInfo<>(CM_MAY_QUIT.class, State.AUTHED, State.IN_GAME));
			packets.put(0x0A8, new PacketInfo<>(CM_REVIVE.class, State.IN_GAME));
			packets.put(0x0AA, new PacketInfo<>(CM_CHARACTER_EDIT.class, State.AUTHED));
			packets.put(0x0AB, new PacketInfo<>(CM_ENTER_WORLD.class, State.AUTHED));
			packets.put(0x0AC, new PacketInfo<>(CM_LEVEL_READY.class, State.IN_GAME));
			packets.put(0x0AD, new PacketInfo<>(CM_UI_SETTINGS.class, State.IN_GAME));
			packets.put(0x0AE, new PacketInfo<>(CM_OBJECT_SEARCH.class, State.IN_GAME));
			packets.put(0x0AF, new PacketInfo<>(CM_CUSTOM_SETTINGS.class, State.IN_GAME));
			packets.put(0x0C3, new PacketInfo<>(CM_VERSION_CHECK.class, State.CONNECTED));
			packets.put(0x0C4, new PacketInfo<>(CM_CASTSPELL.class, State.IN_GAME));
			packets.put(0x0C5, new PacketInfo<>(CM_TOGGLE_SKILL_DEACTIVATE.class, State.IN_GAME));
			packets.put(0x0C6, new PacketInfo<>(CM_REMOVE_ALTERED_STATE.class, State.IN_GAME));
			packets.put(0x0C8, new PacketInfo<>(CM_USE_ITEM.class, State.IN_GAME));
			packets.put(0x0C9, new PacketInfo<>(CM_EQUIP_ITEM.class, State.IN_GAME));
			packets.put(0x0CA, new PacketInfo<>(CM_CHAT_PLAYER_INFO.class, State.IN_GAME));
			packets.put(0x0CB, new PacketInfo<>(CM_PLAYER_LISTENER.class, State.IN_GAME));
			packets.put(0x0CC, new PacketInfo<>(CM_GM_COMMAND.class, State.IN_GAME));
			packets.put(0x0CD, new PacketInfo<>(CM_GM_COMMAND_SEND.class, State.IN_GAME));
			packets.put(0x0CE, new PacketInfo<>(CM_EMOTION.class, State.IN_GAME));
			packets.put(0x0CF, new PacketInfo<>(CM_PING.class, State.IN_GAME, State.AUTHED));
			packets.put(0x0D1, new PacketInfo<>(CM_CAPTCHA.class, State.IN_GAME));
			packets.put(0x0D2, new PacketInfo<>(CM_TELEPORT_ANIMATION_DONE.class, State.IN_GAME));
			packets.put(0x0D3, new PacketInfo<>(CM_LEGION_SEND_EMBLEM_INFO.class, State.IN_GAME));
			packets.put(0x0E0, new PacketInfo<>(CM_LEGION_DOMINION_REQUEST_RANKING.class, State.IN_GAME));
			packets.put(0x0E1, new PacketInfo<>(CM_HOUSE_SCRIPT.class, State.IN_GAME));
			packets.put(0x0E2, new PacketInfo<>(CM_TARGET_SELECT.class, State.IN_GAME));
			packets.put(0x0E3, new PacketInfo<>(CM_ATTACK.class, State.IN_GAME));
			packets.put(0x0F0, new PacketInfo<>(CM_LEGION.class, State.IN_GAME));
			packets.put(0x0F1, new PacketInfo<>(CM_INSTANCE_LEAVE.class, State.IN_GAME));
			packets.put(0x0F2, new PacketInfo<>(CM_LEGION_SEND_EMBLEM.class, State.IN_GAME));
			packets.put(0x0F3, new PacketInfo<>(CM_MOVE.class, State.IN_GAME));
			packets.put(0x0F5, new PacketInfo<>(CM_TIME_CHECK.class, State.CONNECTED, State.AUTHED, State.IN_GAME));
			packets.put(0x0F6, new PacketInfo<>(CM_GATHER.class, State.IN_GAME));
			packets.put(0x0F8, new PacketInfo<>(CM_PET_EMOTE.class, State.IN_GAME));
			packets.put(0x0F9, new PacketInfo<>(CM_PET.class, State.IN_GAME));
			packets.put(0x0FA, new PacketInfo<>(CM_OPEN_STATICDOOR.class, State.IN_GAME));
			packets.put(0x0FE, new PacketInfo<>(CM_CHAT_MESSAGE_PUBLIC.class, State.IN_GAME));
			packets.put(0x0FF, new PacketInfo<>(CM_CHAT_MESSAGE_WHISPER.class, State.IN_GAME));
			packets.put(0x100, new PacketInfo<>(CM_CHAT_GROUP_INFO.class, State.IN_GAME));
			packets.put(0x101, new PacketInfo<>(CM_CHECK_PAK.class, State.IN_GAME));
			packets.put(0x102, new PacketInfo<>(CM_EXCHANGE_REQUEST.class, State.IN_GAME));
			packets.put(0x103, new PacketInfo<>(CM_EXCHANGE_ADD_ITEM.class, State.IN_GAME));
			packets.put(0x104, new PacketInfo<>(CM_INVITE_TO_GROUP.class, State.IN_GAME));
			packets.put(0x107, new PacketInfo<>(CM_VIEW_PLAYER_DETAILS.class, State.IN_GAME));
			packets.put(0x10A, new PacketInfo<>(CM_PING_REQUEST.class, State.IN_GAME));
			packets.put(0x10B, new PacketInfo<>(CM_GAMEGUARD.class, State.IN_GAME, State.AUTHED));
			packets.put(0x10E, new PacketInfo<>(CM_CLIENT_COMMAND_ROLL.class, State.IN_GAME));
			packets.put(0x10F, new PacketInfo<>(CM_GROUP_DISTRIBUTION.class, State.IN_GAME));
			packets.put(0x110, new PacketInfo<>(CM_FIND_GROUP.class, State.IN_GAME));
			packets.put(0x111, new PacketInfo<>(CM_CHARGE_ITEM.class, State.IN_GAME));
			packets.put(0x112, new PacketInfo<>(CM_GROUP_DATA_EXCHANGE.class, State.IN_GAME));
			packets.put(0x113, new PacketInfo<>(CM_DELETE_QUEST.class, State.IN_GAME));
			packets.put(0x114, new PacketInfo<>(CM_MOVE_IN_AIR.class, State.IN_GAME));
			packets.put(0x115, new PacketInfo<>(CM_QUESTION_RESPONSE.class, State.IN_GAME));
			packets.put(0x116, new PacketInfo<>(CM_BUY_ITEM.class, State.IN_GAME));
			packets.put(0x117, new PacketInfo<>(CM_SHOW_DIALOG.class, State.IN_GAME));
			packets.put(0x118, new PacketInfo<>(CM_CLOSE_DIALOG.class, State.IN_GAME));
			packets.put(0x119, new PacketInfo<>(CM_DIALOG_SELECT.class, State.IN_GAME));
			packets.put(0x11A, new PacketInfo<>(CM_LEGION_TABS.class, State.IN_GAME));
			packets.put(0x11D, new PacketInfo<>(CM_SET_NOTE.class, State.IN_GAME));
			packets.put(0x11E, new PacketInfo<>(CM_LEGION_MODIFY_EMBLEM.class, State.IN_GAME));
			packets.put(0x122, new PacketInfo<>(CM_HOUSE_TELEPORT_BACK.class, State.IN_GAME));
			packets.put(0x123, new PacketInfo<>(CM_PLAYER_STATUS_INFO.class, State.IN_GAME));
			packets.put(0x124, new PacketInfo<>(CM_BROKER_SETTLE_LIST.class, State.IN_GAME));
			packets.put(0x125, new PacketInfo<>(CM_BROKER_SETTLE_ACCOUNT.class, State.IN_GAME));
			packets.put(0x127, new PacketInfo<>(CM_SEND_MAIL.class, State.IN_GAME));
			packets.put(0x128, new PacketInfo<>(CM_CHECK_MAIL_LIST.class, State.IN_GAME));
			packets.put(0x129, new PacketInfo<>(CM_READ_MAIL.class, State.IN_GAME));
			packets.put(0x12B, new PacketInfo<>(CM_GET_MAIL_ATTACHMENT.class, State.IN_GAME));
			packets.put(0x12C, new PacketInfo<>(CM_DELETE_MAIL.class, State.IN_GAME));
			packets.put(0x12E, new PacketInfo<>(CM_TITLE_SET.class, State.IN_GAME));
			packets.put(0x131, new PacketInfo<>(CM_MARK_FRIENDLIST.class, State.IN_GAME));
			packets.put(0x132, new PacketInfo<>(CM_FRIEND_ADD.class, State.IN_GAME));
			packets.put(0x133, new PacketInfo<>(CM_FRIEND_DEL.class, State.IN_GAME));
			packets.put(0x134, new PacketInfo<>(CM_PLAY_MOVIE_END.class, State.IN_GAME));
			packets.put(0x135, new PacketInfo<>(CM_HOUSE_EDIT.class, State.IN_GAME));
			packets.put(0x137, new PacketInfo<>(CM_STOP_TRAINING.class, State.IN_GAME));
			packets.put(0x13B, new PacketInfo<>(CM_BUY_TRADE_IN_TRADE.class, State.IN_GAME));
			packets.put(0x13C, new PacketInfo<>(CM_RECIPE_DELETE.class, State.IN_GAME));
			packets.put(0x13D, new PacketInfo<>(CM_ITEM_REMODEL.class, State.IN_GAME));
			// packets.put(0x13E, new PacketInfo<>(CM_GODSTONE_SOCKET.class, State.IN_GAME)); // happens via CM_MANASTONE now (no npc required anymore)
			packets.put(0x13F, new PacketInfo<>(CM_SECURITY_TOKEN.class, State.CONNECTED, State.AUTHED, State.IN_GAME));
			packets.put(0x140, new PacketInfo<>(CM_BROKER_REGISTERED.class, State.IN_GAME));
			packets.put(0x141, new PacketInfo<>(CM_BUY_BROKER_ITEM.class, State.IN_GAME));
			packets.put(0x142, new PacketInfo<>(CM_REGISTER_BROKER_ITEM.class, State.IN_GAME));
			packets.put(0x143, new PacketInfo<>(CM_BROKER_CANCEL_REGISTERED.class, State.IN_GAME));
			packets.put(0x144, new PacketInfo<>(CM_LEGION_UPLOAD_EMBLEM.class, State.IN_GAME));
			packets.put(0x145, new PacketInfo<>(CM_READ_EXPRESS_MAIL.class, State.IN_GAME));
			packets.put(0x146, new PacketInfo<>(CM_SUBZONE_CHANGE.class, State.IN_GAME));
			packets.put(0x147, new PacketInfo<>(CM_QUEST_SHARE.class, State.IN_GAME));
			packets.put(0x149, new PacketInfo<>(CM_BLOCK_ADD.class, State.IN_GAME));
			packets.put(0x14A, new PacketInfo<>(CM_BLOCK_DEL.class, State.IN_GAME));
			// packets.put(0x14B, new PacketInfo<>(CM_ADMIN_PANEL.class, State.IN_GAME));
			packets.put(0x14D, new PacketInfo<>(CM_FRIEND_STATUS.class, State.IN_GAME));
			// packets.put(0x14E, new PacketInfo<>(CM_VIRTUAL_AUTH.class, State.AUTHED, State.IN_GAME));
			packets.put(0x14F, new PacketInfo<>(CM_CHANGE_CHANNEL.class, State.IN_GAME));
			packets.put(0x150, new PacketInfo<>(CM_CRAFT.class, State.IN_GAME));
			packets.put(0x155, new PacketInfo<>(CM_DUEL_REQUEST.class, State.IN_GAME));
			packets.put(0x157, new PacketInfo<>(CM_DELETE_ITEM.class, State.IN_GAME));
			packets.put(0x158, new PacketInfo<>(CM_BROKER_SELL_WINDOW.class, State.IN_GAME));
			packets.put(0x159, new PacketInfo<>(CM_ABYSS_RANKING_LEGIONS.class, State.IN_GAME));
			packets.put(0x15A, new PacketInfo<>(CM_PRIVATE_STORE.class, State.IN_GAME));
			packets.put(0x15B, new PacketInfo<>(CM_PRIVATE_STORE_NAME.class, State.IN_GAME));
			packets.put(0x15C, new PacketInfo<>(CM_SUMMON_COMMAND.class, State.IN_GAME));
			packets.put(0x15E, new PacketInfo<>(CM_BROKER_LIST.class, State.IN_GAME));
			packets.put(0x15F, new PacketInfo<>(CM_BROKER_SEARCH.class, State.IN_GAME));
			packets.put(0x160, new PacketInfo<>(CM_SPLIT_ITEM.class, State.IN_GAME));
			packets.put(0x161, new PacketInfo<>(CM_SHOW_BLOCKLIST.class, State.IN_GAME));
			packets.put(0x162, new PacketInfo<>(CM_PLAYER_SEARCH.class, State.IN_GAME));
			packets.put(0x163, new PacketInfo<>(CM_LEGION_UPLOAD_INFO.class, State.IN_GAME));
			packets.put(0x167, new PacketInfo<>(CM_SHOW_MAP.class, State.IN_GAME));
			packets.put(0x168, new PacketInfo<>(CM_APPEARANCE.class, State.IN_GAME));
			packets.put(0x16B, new PacketInfo<>(CM_AUTO_GROUP.class, State.IN_GAME));
			packets.put(0x16C, new PacketInfo<>(CM_SUMMON_MOVE.class, State.IN_GAME));
			packets.put(0x16D, new PacketInfo<>(CM_SUMMON_EMOTION.class, State.IN_GAME));
			packets.put(0x16E, new PacketInfo<>(CM_SUMMON_ATTACK.class, State.IN_GAME));
			packets.put(0x171, new PacketInfo<>(CM_CHAT_AUTH.class, State.IN_GAME));
			packets.put(0x172, new PacketInfo<>(CM_MACRO_CREATE.class, State.IN_GAME));
			packets.put(0x173, new PacketInfo<>(CM_MACRO_DELETE.class, State.IN_GAME));
			packets.put(0x174, new PacketInfo<>(CM_QUESTIONNAIRE.class, State.IN_GAME));
			packets.put(0x177, new PacketInfo<>(CM_TELEPORT_SELECT.class, State.IN_GAME));
			packets.put(0x178, new PacketInfo<>(CM_L2AUTH_LOGIN_CHECK.class, State.CONNECTED));
			packets.put(0x179, new PacketInfo<>(CM_CHARACTER_LIST.class, State.AUTHED));
			packets.put(0x17A, new PacketInfo<>(CM_CREATE_CHARACTER.class, State.AUTHED));
			packets.put(0x17B, new PacketInfo<>(CM_DELETE_CHARACTER.class, State.AUTHED));
			packets.put(0x17C, new PacketInfo<>(CM_RESTORE_CHARACTER.class, State.AUTHED));
			packets.put(0x17D, new PacketInfo<>(CM_START_LOOT.class, State.IN_GAME));
			packets.put(0x17E, new PacketInfo<>(CM_LOOT_ITEM.class, State.IN_GAME));
			packets.put(0x17F, new PacketInfo<>(CM_MOVE_ITEM.class, State.IN_GAME));
			packets.put(0x180, new PacketInfo<>(CM_MAC_ADDRESS.class, State.CONNECTED, State.AUTHED, State.IN_GAME));
			packets.put(0x182, new PacketInfo<>(CM_REPORT_PLAYER.class, State.IN_GAME));
			packets.put(0x183, new PacketInfo<>(CM_INSTANCE_INFO.class, State.IN_GAME));
			packets.put(0x184, new PacketInfo<>(CM_RELEASE_OBJECT.class, State.IN_GAME));
			packets.put(0x185, new PacketInfo<>(CM_HOUSE_OPEN_DOOR.class, State.IN_GAME));
			// packets.put(0x186, new PacketInfo<>(CM_IN_GAME_SHOP_INFO.class, State.IN_GAME));
			packets.put(0x189, new PacketInfo<>(CM_SHOW_FRIENDLIST.class, State.IN_GAME));
			packets.put(0x18B, new PacketInfo<>(CM_CHALLENGE_LIST.class, State.IN_GAME));
			packets.put(0x18C, new PacketInfo<>(CM_BONUS_TITLE.class, State.IN_GAME));
			packets.put(0x18D, new PacketInfo<>(CM_USE_CHARGE_SKILL.class, State.IN_GAME));
			packets.put(0x18E, new PacketInfo<>(CM_TUNE.class, State.IN_GAME));
			packets.put(0x18F, new PacketInfo<>(CM_SELECT_DECOMPOSABLE.class, State.IN_GAME));
			packets.put(0x190, new PacketInfo<>(CM_SUMMON_CASTSPELL.class, State.IN_GAME));
			packets.put(0x191, new PacketInfo<>(CM_FUSION_WEAPONS.class, State.IN_GAME));
			packets.put(0x192, new PacketInfo<>(CM_BREAK_WEAPONS.class, State.IN_GAME));
			packets.put(0x193, new PacketInfo<>(CM_COMPOSITE_STONES.class, State.IN_GAME));
			packets.put(0x194, new PacketInfo<>(CM_CHECK_NICKNAME.class, State.AUTHED));
			packets.put(0x195, new PacketInfo<>(CM_REPLACE_ITEM.class, State.IN_GAME));
			packets.put(0x196, new PacketInfo<>(CM_BLOCK_SET_REASON.class, State.IN_GAME));
			packets.put(0x198, new PacketInfo<>(CM_SHOW_BRAND.class, State.IN_GAME));
			// packets.put(0x199, new PacketInfo<>(CM_GM_COMMAND_ACTION.class, State.IN_GAME));
			packets.put(0x19A, new PacketInfo<>(CM_RECONNECT_AUTH.class, State.AUTHED));
			packets.put(0x19B, new PacketInfo<>(CM_GROUP_LOOT.class, State.IN_GAME));
			packets.put(0x19C, new PacketInfo<>(CM_DISTRIBUTION_SETTINGS.class, State.IN_GAME));
			packets.put(0x19D, new PacketInfo<>(CM_MAY_LOGIN_INTO_GAME.class, State.AUTHED));
			packets.put(0x19F, new PacketInfo<>(CM_ABYSS_RANKING_PLAYERS.class, State.IN_GAME));
			packets.put(0x1A0, new PacketInfo<>(CM_PLACE_BID.class, State.IN_GAME));
			packets.put(0x1A1, new PacketInfo<>(CM_HOUSE_TELEPORT.class, State.IN_GAME));
			packets.put(0x1A2, new PacketInfo<>(CM_HOUSE_PAY_RENT.class, State.IN_GAME));
			packets.put(0x1A3, new PacketInfo<>(CM_USE_HOUSE_OBJECT.class, State.IN_GAME));
			packets.put(0x1B0, new PacketInfo<>(CM_MEGAPHONE.class, State.IN_GAME));
			packets.put(0x1B1, new PacketInfo<>(CM_TUNE_RESULT.class, State.IN_GAME));
			packets.put(0x1B2, new PacketInfo<>(CM_FRIEND_SET_MEMO.class, State.IN_GAME));
			packets.put(0x1B3, new PacketInfo<>(CM_UNWARP_ITEM.class, State.IN_GAME));
			packets.put(0x1B5, new PacketInfo<>(CM_CHARACTER_PASSKEY.class, State.AUTHED));
			// packets.put(0x1B6, new PacketInfo<>(CM_SECURITY_TOKEN_REQUEST.class, State.IN_GAME));
			packets.put(0x1B8, new PacketInfo<>(CM_CHECK_MAIL_UNK.class, State.IN_GAME)); // TODO
			// packets.put(0x1BA, new PacketInfo<>(CM_DIRECT_ENTER_WORLD.class, State.IN_GAME));
			// packets.put(0x1BB, new PacketInfo<>(CM_REQUEST_BEGINNER_SERVER.class, State.IN_GAME));
			// packets.put(0x1BC, new PacketInfo<>(CM_REQUEST_RETURN_SERVER.class, State.IN_GAME));
			packets.put(0x1BD, new PacketInfo<>(CM_GET_HOUSE_BIDS.class, State.IN_GAME));
			packets.put(0x1BE, new PacketInfo<>(CM_REGISTER_HOUSE.class, State.IN_GAME));
			packets.put(0x1D7, new PacketInfo<>(CM_BIND_POINT_TELEPORT.class, State.IN_GAME));
			packets.put(0x1D9, new PacketInfo<>(CM_UPGRADE_ARCADE.class, State.IN_GAME));
			packets.put(0x1DA, new PacketInfo<>(CM_ITEM_PURIFICATION.class, State.IN_GAME));
			packets.put(0x1DB, new PacketInfo<>(CM_ATREIAN_PASSPORT.class, State.IN_GAME));
			// packets.put(0x2A9, new PacketInfo<>(CM_TIME_CHECK_QUIT.class, State.IN_GAME));
			packets.put(0x2E5, new PacketInfo<>(CM_EXCHANGE_ADD_KINAH.class, State.IN_GAME));
			packets.put(0x2E6, new PacketInfo<>(CM_EXCHANGE_LOCK.class, State.IN_GAME));
			packets.put(0x2E7, new PacketInfo<>(CM_EXCHANGE_OK.class, State.IN_GAME));
			packets.put(0x2E8, new PacketInfo<>(CM_EXCHANGE_CANCEL.class, State.IN_GAME));
			packets.put(0x2E9, new PacketInfo<>(CM_WINDSTREAM.class, State.IN_GAME));
			packets.put(0x2EA, new PacketInfo<>(CM_MOTION.class, State.IN_GAME));
			packets.put(0x2EB, new PacketInfo<>(CM_HOUSE_KICK.class, State.IN_GAME));
			packets.put(0x2EC, new PacketInfo<>(CM_HOUSE_SETTINGS.class, State.IN_GAME));
			packets.put(0x2ED, new PacketInfo<>(CM_MANASTONE.class, State.IN_GAME));
			packets.put(0x2EE, new PacketInfo<>(CM_HOUSE_DECORATE.class, State.IN_GAME));
			packets.put(0x2EF, new PacketInfo<>(CM_LEGION_WH_KINAH.class, State.IN_GAME));
		} catch (NoSuchMethodException e) { // should never happen
			throw new ExceptionInInitializerError(e);
		}
	}

	public static AionClientPacket tryCreatePacket(ByteBuffer data, AionConnection client) {
		State state = client.getState();
		int opCode = data.getShort() & 0xffff;
		data.position(data.position() + 3); // skip static code (short) and secondary opcode (byte)
		PacketInfo<? extends AionClientPacket> packetInfo = packets.get(opCode);
		if (packetInfo == null) {
			if (NetworkConfig.LOG_UNKNOWN_PACKETS)
				log.warn(String.format("Aion client sent data with unknown opcode: 0x%03X, state=%s %n%s", opCode, state.toString(), Util.toHex(data)));
			return null;
		}
		if (!packetInfo.isValid(state)) {
			if (NetworkConfig.LOG_IGNORED_PACKETS)
				log.warn(client + " sent " + packetInfo.getPacketClassName() + " but the connections current state (" + state
					+ ") is invalid for this packet. Packet won't be instantiated.");
			return null;
		}
		return packetInfo.newPacket(opCode, data, client);
	}

	private static class PacketInfo<T extends AionClientPacket> {

		private final Constructor<T> packetConstructor;
		private final Set<State> validStates;

		private PacketInfo(Class<T> packetClass, State state, State... otherStates) throws NoSuchMethodException {
			this.packetConstructor = packetClass.getConstructor(int.class, Set.class);
			this.validStates = EnumSet.of(state, otherStates);
		}

		private boolean isValid(State state) {
			return validStates.contains(state);
		}

		private T newPacket(int opCode, ByteBuffer buffer, AionConnection con) {
			try {
				T packet = packetConstructor.newInstance(opCode, validStates);
				packet.setBuffer(buffer);
				packet.setConnection(con);
				return packet;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) { // should never happen
				throw new InstantiationError("Couldn't instantiate packet " + getPacketClassName() + ": " + e);
			}
		}

		private String getPacketClassName() {
			return packetConstructor.getDeclaringClass().getSimpleName();
		}
	}
}
