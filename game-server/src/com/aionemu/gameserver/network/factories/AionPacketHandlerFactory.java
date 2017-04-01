package com.aionemu.gameserver.network.factories;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.AionPacketHandler;
import com.aionemu.gameserver.network.aion.clientpackets.*;

/**
 * This factory is responsible for creating {@link AionPacketHandler} object. It also initializes created handler with a set of packet prototypes.<br>
 * Object of this classes uses <tt>Injector</tt> for injecting dependencies into prototype objects.<br>
 * <br>
 * 
 * @author Luno
 */
public class AionPacketHandlerFactory {

	private AionPacketHandler handler;

	public static AionPacketHandlerFactory getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Creates new instance of <tt>AionPacketHandlerFactory</tt><br>
	 */
	public AionPacketHandlerFactory() {
		handler = new AionPacketHandler();

		// ////////////////////// 4.7.5.0 ///////////////////////
		addPacket(new CM_ABYSS_RANKING_LEGIONS(0x159, State.IN_GAME));
		addPacket(new CM_ABYSS_RANKING_PLAYERS(0x19F, State.IN_GAME));
		addPacket(new CM_APPEARANCE(0x168, State.IN_GAME));
		addPacket(new CM_ATREIAN_PASSPORT(0x1DB, State.IN_GAME));
		addPacket(new CM_ATTACK(0x0E3, State.IN_GAME));
		addPacket(new CM_AUTO_GROUP(0x16B, State.IN_GAME));
		addPacket(new CM_BIND_POINT_TELEPORT(0x1D7, State.IN_GAME));
		addPacket(new CM_BLOCK_ADD(0x149, State.IN_GAME));
		addPacket(new CM_BLOCK_DEL(0x14A, State.IN_GAME));
		addPacket(new CM_BLOCK_SET_REASON(0x196, State.IN_GAME));
		addPacket(new CM_BONUS_TITLE(0x18C, State.IN_GAME));
		addPacket(new CM_BREAK_WEAPONS(0x192, State.IN_GAME));
		addPacket(new CM_BROKER_CANCEL_REGISTERED(0x143, State.IN_GAME));
		addPacket(new CM_BROKER_LIST(0x15E, State.IN_GAME));
		addPacket(new CM_BROKER_REGISTERED(0x140, State.IN_GAME));
		addPacket(new CM_BROKER_SEARCH(0x15F, State.IN_GAME));
		addPacket(new CM_BROKER_SELL_WINDOW(0x158, State.IN_GAME));
		addPacket(new CM_BROKER_SETTLE_ACCOUNT(0x125, State.IN_GAME));
		addPacket(new CM_BROKER_SETTLE_LIST(0x124, State.IN_GAME));
		addPacket(new CM_BUY_BROKER_ITEM(0x141, State.IN_GAME));
		addPacket(new CM_BUY_ITEM(0x116, State.IN_GAME));
		addPacket(new CM_BUY_TRADE_IN_TRADE(0x13B, State.IN_GAME));
		addPacket(new CM_CAPTCHA(0x0D1, State.IN_GAME));
		addPacket(new CM_CASTSPELL(0x0C4, State.IN_GAME));
		addPacket(new CM_CHALLENGE_LIST(0x18B, State.IN_GAME));
		addPacket(new CM_CHARACTER_EDIT(0x0AA, State.AUTHED));
		addPacket(new CM_CHARACTER_LIST(0x179, State.AUTHED));
		addPacket(new CM_CHARACTER_PASSKEY(0x1B5, State.AUTHED));
		addPacket(new CM_CHARGE_ITEM(0x111, State.IN_GAME));
		addPacket(new CM_CHECK_NICKNAME(0x194, State.AUTHED));
		addPacket(new CM_CHANGE_CHANNEL(0x14F, State.IN_GAME));
		addPacket(new CM_CHAT_AUTH(0x171, State.IN_GAME));
		addPacket(new CM_CHAT_GROUP_INFO(0x100, State.IN_GAME));
		addPacket(new CM_CHAT_MESSAGE_PUBLIC(0x0FE, State.IN_GAME));
		addPacket(new CM_CHAT_MESSAGE_WHISPER(0x0FF, State.IN_GAME));
		addPacket(new CM_CHAT_PLAYER_INFO(0x0CA, State.IN_GAME));
		addPacket(new CM_CHECK_MAIL_LIST(0x128, State.IN_GAME));
		addPacket(new CM_CHECK_MAIL_UNK(0x1B8, State.IN_GAME)); // TODO
		addPacket(new CM_CHECK_PAK(0x101, State.IN_GAME));
		addPacket(new CM_CLIENT_COMMAND_ROLL(0x10E, State.IN_GAME));
		addPacket(new CM_CLOSE_DIALOG(0x118, State.IN_GAME));
		addPacket(new CM_COMPOSITE_STONES(0x193, State.IN_GAME));
		addPacket(new CM_CRAFT(0x150, State.IN_GAME));
		addPacket(new CM_CREATE_CHARACTER(0x17A, State.AUTHED));
		addPacket(new CM_CUSTOM_SETTINGS(0x0AF, State.IN_GAME));
		addPacket(new CM_DELETE_CHARACTER(0x17B, State.AUTHED));
		addPacket(new CM_DELETE_ITEM(0x157, State.IN_GAME));
		addPacket(new CM_DELETE_MAIL(0x12C, State.IN_GAME));
		addPacket(new CM_DELETE_QUEST(0x113, State.IN_GAME));
		addPacket(new CM_DIALOG_SELECT(0x119, State.IN_GAME));
		addPacket(new CM_DISTRIBUTION_SETTINGS(0x19C, State.IN_GAME));
		addPacket(new CM_DUEL_REQUEST(0x155, State.IN_GAME));
		addPacket(new CM_EMOTION(0x0CE, State.IN_GAME));
		addPacket(new CM_ENTER_WORLD(0x0AB, State.AUTHED));
		addPacket(new CM_EQUIP_ITEM(0x0C9, State.IN_GAME));
		addPacket(new CM_EXCHANGE_ADD_ITEM(0x103, State.IN_GAME));
		addPacket(new CM_EXCHANGE_ADD_KINAH(0x2E5, State.IN_GAME));
		addPacket(new CM_EXCHANGE_CANCEL(0x2E8, State.IN_GAME));
		addPacket(new CM_EXCHANGE_LOCK(0x2E6, State.IN_GAME));
		addPacket(new CM_EXCHANGE_OK(0x2E7, State.IN_GAME));
		addPacket(new CM_EXCHANGE_REQUEST(0x102, State.IN_GAME));
		addPacket(new CM_FIND_GROUP(0x110, State.IN_GAME));
		addPacket(new CM_FRIEND_ADD(0x132, State.IN_GAME));
		addPacket(new CM_FRIEND_DEL(0x133, State.IN_GAME));
		addPacket(new CM_FRIEND_SET_MEMO(0x1B2, State.IN_GAME));
		addPacket(new CM_FRIEND_STATUS(0x14D, State.IN_GAME));
		addPacket(new CM_FUSION_WEAPONS(0x191, State.IN_GAME));
		addPacket(new CM_GAMEGUARD(0x10B, State.IN_GAME, State.AUTHED));
		addPacket(new CM_GATHER(0x0F6, State.IN_GAME));
		addPacket(new CM_GET_HOUSE_BIDS(0x1BD, State.IN_GAME));
		addPacket(new CM_GET_MAIL_ATTACHMENT(0x12B, State.IN_GAME));
		addPacket(new CM_GM_COMMAND(0x0CC, State.IN_GAME));
		addPacket(new CM_GM_COMMAND_SEND(0x0CD, State.IN_GAME));
		addPacket(new CM_GROUP_DATA_EXCHANGE(0x112, State.IN_GAME));
		addPacket(new CM_GROUP_DISTRIBUTION(0x10F, State.IN_GAME));
		addPacket(new CM_GROUP_LOOT(0x19B, State.IN_GAME));
		addPacket(new CM_HOUSE_EDIT(0x135, State.IN_GAME));
		addPacket(new CM_HOUSE_DECORATE(0x2EE, State.IN_GAME));
		addPacket(new CM_HOUSE_KICK(0x2EB, State.IN_GAME));
		addPacket(new CM_HOUSE_OPEN_DOOR(0x185, State.IN_GAME));
		addPacket(new CM_HOUSE_PAY_RENT(0x1A2, State.IN_GAME));
		addPacket(new CM_HOUSE_SCRIPT(0x0E1, State.IN_GAME));
		addPacket(new CM_HOUSE_SETTINGS(0x2EC, State.IN_GAME));
		addPacket(new CM_HOUSE_TELEPORT(0x1A1, State.IN_GAME));
		addPacket(new CM_HOUSE_TELEPORT_BACK(0x122, State.IN_GAME));
		addPacket(new CM_INSTANCE_INFO(0x183, State.IN_GAME));
		addPacket(new CM_INSTANCE_LEAVE(0x0F1, State.IN_GAME));
		addPacket(new CM_INVITE_TO_GROUP(0x104, State.IN_GAME));
		addPacket(new CM_ITEM_PURIFICATION(0x1DA, State.IN_GAME));
		addPacket(new CM_ITEM_REMODEL(0x13D, State.IN_GAME));
		addPacket(new CM_L2AUTH_LOGIN_CHECK(0x178, State.CONNECTED));
		addPacket(new CM_LEGION(0x0F0, State.IN_GAME));
		addPacket(new CM_LEGION_DOMINION_REQUEST_RANKING(0x0E0, State.IN_GAME));
		addPacket(new CM_LEGION_MODIFY_EMBLEM(0x11E, State.IN_GAME));
		addPacket(new CM_LEGION_SEND_EMBLEM(0x0F2, State.IN_GAME));
		addPacket(new CM_LEGION_SEND_EMBLEM_INFO(0x0D3, State.IN_GAME));
		addPacket(new CM_LEGION_TABS(0x11A, State.IN_GAME));
		addPacket(new CM_LEGION_UPLOAD_EMBLEM(0x144, State.IN_GAME));
		addPacket(new CM_LEGION_UPLOAD_INFO(0x163, State.IN_GAME));
		addPacket(new CM_LEGION_WH_KINAH(0x2EF, State.IN_GAME));
		addPacket(new CM_LEVEL_READY(0x0AC, State.IN_GAME));
		addPacket(new CM_LOOT_ITEM(0x17E, State.IN_GAME));
		addPacket(new CM_MAC_ADDRESS(0x180, State.CONNECTED, State.AUTHED, State.IN_GAME));
		addPacket(new CM_MACRO_CREATE(0x172, State.IN_GAME));
		addPacket(new CM_MACRO_DELETE(0x173, State.IN_GAME));
		addPacket(new CM_MANASTONE(0x2ED, State.IN_GAME));
		addPacket(new CM_MARK_FRIENDLIST(0x131, State.IN_GAME));
		addPacket(new CM_MAY_LOGIN_INTO_GAME(0x19D, State.AUTHED));
		addPacket(new CM_MAY_QUIT(0x0A7, State.AUTHED, State.IN_GAME));
		addPacket(new CM_MEGAPHONE(0x1B0, State.IN_GAME));
		addPacket(new CM_MOTION(0x2EA, State.IN_GAME));
		addPacket(new CM_MOVE(0x0F3, State.IN_GAME));
		addPacket(new CM_MOVE_IN_AIR(0x114, State.IN_GAME));
		addPacket(new CM_MOVE_ITEM(0x17F, State.IN_GAME));
		addPacket(new CM_OBJECT_SEARCH(0x0AE, State.IN_GAME));
		addPacket(new CM_OPEN_STATICDOOR(0x0FA, State.IN_GAME));
		addPacket(new CM_PET(0x0F9, State.IN_GAME));
		addPacket(new CM_PET_EMOTE(0x0F8, State.IN_GAME));
		addPacket(new CM_PING(0x0CF, State.IN_GAME, State.AUTHED));
		addPacket(new CM_PING_REQUEST(0x10A, State.IN_GAME));
		addPacket(new CM_PLACE_BID(0x1A0, State.IN_GAME));
		addPacket(new CM_PLAY_MOVIE_END(0x134, State.IN_GAME));
		addPacket(new CM_PLAYER_LISTENER(0x0CB, State.IN_GAME));
		addPacket(new CM_PLAYER_SEARCH(0x162, State.IN_GAME));
		addPacket(new CM_PLAYER_STATUS_INFO(0x123, State.IN_GAME));
		addPacket(new CM_PRIVATE_STORE(0x15A, State.IN_GAME));
		addPacket(new CM_PRIVATE_STORE_NAME(0x15B, State.IN_GAME));
		addPacket(new CM_QUEST_SHARE(0x147, State.IN_GAME));
		addPacket(new CM_QUESTION_RESPONSE(0x115, State.IN_GAME));
		addPacket(new CM_QUESTIONNAIRE(0x174, State.IN_GAME));
		addPacket(new CM_QUIT(0x0A6, State.AUTHED, State.IN_GAME));
		addPacket(new CM_READ_EXPRESS_MAIL(0x145, State.IN_GAME));
		addPacket(new CM_READ_MAIL(0x129, State.IN_GAME));
		addPacket(new CM_RECIPE_DELETE(0x13C, State.IN_GAME));
		addPacket(new CM_RECONNECT_AUTH(0x19A, State.AUTHED));
		addPacket(new CM_REGISTER_BROKER_ITEM(0x142, State.IN_GAME));
		addPacket(new CM_REGISTER_HOUSE(0x1BE, State.IN_GAME));
		addPacket(new CM_RELEASE_OBJECT(0x184, State.IN_GAME));
		addPacket(new CM_REMOVE_ALTERED_STATE(0x0C6, State.IN_GAME));
		addPacket(new CM_REPLACE_ITEM(0x195, State.IN_GAME));
		addPacket(new CM_REPORT_PLAYER(0x182, State.IN_GAME));
		addPacket(new CM_RESTORE_CHARACTER(0x17C, State.AUTHED));
		addPacket(new CM_REVIVE(0x0A8, State.IN_GAME));
		addPacket(new CM_SECURITY_TOKEN(0x13F, State.CONNECTED, State.AUTHED, State.IN_GAME));
		addPacket(new CM_SELECT_DECOMPOSABLE(0x18F, State.IN_GAME));
		addPacket(new CM_SEND_MAIL(0x127, State.IN_GAME));
		addPacket(new CM_SET_NOTE(0x11D, State.IN_GAME));
		addPacket(new CM_SHOW_BLOCKLIST(0x161, State.IN_GAME));
		addPacket(new CM_SHOW_BRAND(0x198, State.IN_GAME));
		addPacket(new CM_SHOW_DIALOG(0x117, State.IN_GAME));
		addPacket(new CM_SHOW_FRIENDLIST(0x189, State.IN_GAME));
		addPacket(new CM_SHOW_MAP(0x167, State.IN_GAME));
		addPacket(new CM_SPLIT_ITEM(0x160, State.IN_GAME));
		addPacket(new CM_START_LOOT(0x17D, State.IN_GAME));
		addPacket(new CM_STOP_TRAINING(0x137, State.IN_GAME));
		addPacket(new CM_SUBZONE_CHANGE(0x146, State.IN_GAME));
		addPacket(new CM_SUMMON_ATTACK(0x16E, State.IN_GAME));
		addPacket(new CM_SUMMON_CASTSPELL(0x190, State.IN_GAME));
		addPacket(new CM_SUMMON_COMMAND(0x15C, State.IN_GAME));
		addPacket(new CM_SUMMON_EMOTION(0x16D, State.IN_GAME));
		addPacket(new CM_SUMMON_MOVE(0x16C, State.IN_GAME));
		addPacket(new CM_TARGET_SELECT(0x0E2, State.IN_GAME));
		addPacket(new CM_TELEPORT_ANIMATION_DONE(0x0D2, State.IN_GAME));
		addPacket(new CM_TELEPORT_SELECT(0x177, State.IN_GAME));
		addPacket(new CM_TIME_CHECK(0x0F5, State.CONNECTED, State.AUTHED, State.IN_GAME));
		addPacket(new CM_TITLE_SET(0x12E, State.IN_GAME));
		addPacket(new CM_TOGGLE_SKILL_DEACTIVATE(0x0C5, State.IN_GAME));
		addPacket(new CM_TUNE(0x18E, State.IN_GAME));
		addPacket(new CM_UI_SETTINGS(0x0AD, State.IN_GAME));
		addPacket(new CM_UNWARP_ITEM(0x1B3, State.IN_GAME));
		addPacket(new CM_UPGRADE_ARCADE(0x1D9, State.IN_GAME));
		addPacket(new CM_USE_CHARGE_SKILL(0x18D, State.IN_GAME));
		addPacket(new CM_USE_HOUSE_OBJECT(0x1A3, State.IN_GAME));
		addPacket(new CM_USE_ITEM(0x0C8, State.IN_GAME));
		addPacket(new CM_VERSION_CHECK(0x0C3, State.CONNECTED));
		addPacket(new CM_VIEW_PLAYER_DETAILS(0x107, State.IN_GAME));
		addPacket(new CM_WINDSTREAM(0x2E9, State.IN_GAME));

		// addPacket(new CM_UNK(0x0A9, State.IN_GAME));
		// addPacket(new CM_UNK(0x181, State.IN_GAME));
		// addPacket(new CM_UNK(0x175, State.IN_GAME));
		// addPacket(new CM_UNK(0x166, State.IN_GAME));
		// addPacket(new CM_UNK(0x19E, State.IN_GAME));
		// addPacket(new CM_UNK(0x0F4, State.IN_GAME));
		// addPacket(new CM_UNK(0x176, State.IN_GAME));
		// addPacket(new CM_UNK(0x120, State.IN_GAME));
		// addPacket(new CM_UNK(0x121, State.IN_GAME));
		// addPacket(new CM_UNK(0x1B1, State.IN_GAME));
		// addPacket(new CM_UNK(0x151, State.IN_GAME));
		// addPacket(new CM_UNK(0x1D8, State.IN_GAME));
		// addPacket(new CM_UNK(0x165, State.IN_GAME));
		// addPacket(new CM_UNK(0x1B4, State.IN_GAME));
		// addPacket(new CM_UNK(0x13A, State.IN_GAME));
		// addPacket(new CM_GM_COMMAND_ACTION(0x199, State.IN_GAME));
		// addPacket(new CM_UNK(0x18A, State.IN_GAME));
		// addPacket(new CM_UNK(0x14C, State.IN_GAME));
		// addPacket(new CM_UNK(0x197, State.IN_GAME));
		// addPacket(new CM_UNK(0x0FD, State.IN_GAME));
		// addPacket(new CM_UNK(0x188, State.IN_GAME));
		// addPacket(new CM_UNK(0x1B9, State.IN_GAME));
		// addPacket(new CM_UNK(0x16F, State.IN_GAME));
		// addPacket(new CM_UNK(0x186, State.IN_GAME));
		// addPacket(new CM_UNK(0x1DC, State.IN_GAME));
		// addPacket(new CM_UNK(0x1AF, State.IN_GAME));

		/* ////////////////////// Not Added //////////////////////
		 * addPacket(new CM_VIRTUAL_AUTH(0x14E, State.AUTHED, State.IN_GAME));
		 * addPacket(new CM_LOGIN_OUT(0xA5, State.AUTHED, State.IN_GAME));
		 * addPacket(new CM_DIRECT_ENTER_WORLD(0x1BA, State.IN_GAME));
		 * addPacket(new CM_REQUEST_BEGINNER_SERVER(0x1BB, State.IN_GAME));
		 * addPacket(new CM_REQUEST_RETURN_SERVER(0x1BC, State.IN_GAME));
		 */

		/* ////////////////////// 4.7.0.5 REMOVED(?) //////////////////////
		 * addPacket(new CM_GODSTONE_SOCKET(0x13A, State.IN_GAME)); // happens via CM_MANASTONE now, since no npc is required anymore
		 * addPacket(new CM_IN_GAME_SHOP_INFO(0x184, State.IN_GAME));
		 * addPacket(new CM_SECURITY_TOKEN_REQUEST(0x1B6, State.IN_GAME));
		 * addPacket(new CM_TIME_CHECK_QUIT(0x2A9, State.IN_GAME));
		 */

		/* ////////////////////// TODO //////////////////////
		 * addPacket(new CM_ADMIN_PANEL(0x149, State.IN_GAME));
		 * addPacket(new CM_UNK_4_5(0x133, State.IN_GAME));
		 */

		/* ////////////////////// 3.0 //////////////////////
		 * addPacket(new CM_UNK(0x136, State.IN_GAME));
		 */
	}

	public AionPacketHandler getPacketHandler() {
		return handler;
	}

	private void addPacket(AionClientPacket prototype) {
		handler.addPacketPrototype(prototype);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final AionPacketHandlerFactory instance = new AionPacketHandlerFactory();
	}
}
