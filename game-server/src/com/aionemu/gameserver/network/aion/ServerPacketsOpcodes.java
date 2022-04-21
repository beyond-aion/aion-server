package com.aionemu.gameserver.network.aion;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.network.aion.serverpackets.*;

/**
 * This class is holding opcodes for all server packets. It's used only to have all opcodes in one place
 *
 * @author Luno, alexa026, ATracer, avol, orz, cura
 */
public class ServerPacketsOpcodes {

	private static Map<Class<? extends AionServerPacket>, Integer> opcodes = new HashMap<>();

	static {
		addPacketOpcode(SM_VERSION_CHECK.class, 0x00);
		addPacketOpcode(SM_STATS_INFO.class, 0x01);
		addPacketOpcode(SM_GM_SHOW_PLAYER_STATUS.class, 0x02);
		addPacketOpcode(SM_STATUPDATE_HP.class, 0x03);
		addPacketOpcode(SM_STATUPDATE_MP.class, 0x04);
		addPacketOpcode(SM_ATTACK_STATUS.class, 0x05);
		addPacketOpcode(SM_STATUPDATE_DP.class, 0x06);
		addPacketOpcode(SM_DP_INFO.class, 0x07);
		addPacketOpcode(SM_STATUPDATE_EXP.class, 0x08);
		// 0x09
		addPacketOpcode(SM_NPC_ASSEMBLER.class, 0xA);
		addPacketOpcode(SM_LEGION_UPDATE_NICKNAME.class, 0xB);
		addPacketOpcode(SM_LEGION_TABS.class, 0xC);
		addPacketOpcode(SM_ENTER_WORLD_CHECK.class, 0xD);
		addPacketOpcode(SM_NPC_INFO.class, 0xE);
		addPacketOpcode(SM_PLAYER_SPAWN.class, 0xF);
		// 16
		addPacketOpcode(SM_GATHERABLE_INFO.class, 0x11);
		// 0x12
		addPacketOpcode(SM_GM_SEARCH.class, 0x13);
		addPacketOpcode(SM_TELEPORT_LOC.class, 0x14);
		addPacketOpcode(SM_PLAYER_MOVE.class, 0x15);
		addPacketOpcode(SM_DELETE.class, 0x16);
		addPacketOpcode(SM_LOGIN_QUEUE.class, 0x17);
		addPacketOpcode(SM_MESSAGE.class, 0x18);
		addPacketOpcode(SM_SYSTEM_MESSAGE.class, 0x19);
		addPacketOpcode(SM_INVENTORY_INFO.class, 0x1A);
		addPacketOpcode(SM_INVENTORY_ADD_ITEM.class, 0x1B);
		addPacketOpcode(SM_DELETE_ITEM.class, 0x1C);
		addPacketOpcode(SM_INVENTORY_UPDATE_ITEM.class, 0x1D);
		addPacketOpcode(SM_UI_SETTINGS.class, 0x1E);
		addPacketOpcode(SM_PLAYER_STANCE.class, 0x1F);
		addPacketOpcode(SM_PLAYER_INFO.class, 0x20);
		addPacketOpcode(SM_CASTSPELL.class, 0x21);
		addPacketOpcode(SM_GATHER_ANIMATION.class, 0x22);
		addPacketOpcode(SM_GATHER_UPDATE.class, 0x23);
		addPacketOpcode(SM_UPDATE_PLAYER_APPEARANCE.class, 0x24);
		addPacketOpcode(SM_EMOTION.class, 0x25);
		addPacketOpcode(SM_GAME_TIME.class, 0x26);
		addPacketOpcode(SM_TIME_CHECK.class, 0x27);
		addPacketOpcode(SM_LOOKATOBJECT.class, 0x28);
		addPacketOpcode(SM_TARGET_SELECTED.class, 0x29);
		addPacketOpcode(SM_SKILL_CANCEL.class, 0x2A);
		addPacketOpcode(SM_CASTSPELL_RESULT.class, 0x2B);
		addPacketOpcode(SM_SKILL_LIST.class, 0x2C);
		addPacketOpcode(SM_SKILL_REMOVE.class, 0x2D);
		addPacketOpcode(SM_SKILL_ACTIVATION.class, 0x2E);
		// 46 "Ihr habt aufgehört [skill_name] einzusetzen.
		// 47
		addPacketOpcode(SM_ABNORMAL_STATE.class, 0x31);
		addPacketOpcode(SM_ABNORMAL_EFFECT.class, 0x32);
		addPacketOpcode(SM_SKILL_COOLDOWN.class, 0x33);
		addPacketOpcode(SM_QUESTION_WINDOW.class, 0x34);
		addPacketOpcode(SM_CLOSE_QUESTION_WINDOW.class, 0x35);
		addPacketOpcode(SM_ATTACK.class, 0x36);
		addPacketOpcode(SM_MOVE.class, 0x37);
		// 56
		addPacketOpcode(SM_HEADING_UPDATE.class, 0x39);
		addPacketOpcode(SM_TRANSFORM.class, 0x3A);
		addPacketOpcode(SM_GM_SHOW_PLAYER_SKILLS.class, 0x3B);
		addPacketOpcode(SM_DIALOG_WINDOW.class, 0x3C);
		addPacketOpcode(SM_HOUSE_UPDATE.class, 0x3D);
		addPacketOpcode(SM_SELL_ITEM.class, 0x3E);
		addPacketOpcode(SM_GM_SHOW_LEGION_INFO.class, 0x3F);
		addPacketOpcode(SM_GM_BOOKMARK_ADD.class, 0x40);
		addPacketOpcode(SM_VIEW_PLAYER_DETAILS.class, 0x41);
		addPacketOpcode(SM_GM_SHOW_LEGION_MEMBERLIST.class, 0x42);
		addPacketOpcode(SM_WEATHER.class, 0x43);
		addPacketOpcode(SM_PLAYER_STATE.class, 0x44);
		// 0x45 SM_SUMMON_TELEPORT_REQUEST some teleport/summon dialog. response when accepting/declining the dialog is 0x166. first D in SM and CM packets is identical (dialog ID?) //fsc 0x45 cshh unk name skill_id time_seconds
		addPacketOpcode(SM_ACTION_ANIMATION.class, 0x46);
		addPacketOpcode(SM_QUEST_LIST.class, 0x47);
		addPacketOpcode(SM_KEY.class, 0x48);
		addPacketOpcode(SM_SUMMON_PANEL_REMOVE.class, 0x49);
		addPacketOpcode(SM_EXCHANGE_REQUEST.class, 0x4A);
		addPacketOpcode(SM_EXCHANGE_ADD_ITEM.class, 0x4B);
		// 76
		addPacketOpcode(SM_EXCHANGE_ADD_KINAH.class, 0x4D);
		addPacketOpcode(SM_EXCHANGE_CONFIRMATION.class, 0x4E);
		addPacketOpcode(SM_EMOTION_LIST.class, 0x4F);
		// 80 SM_UNK_REQUEST client answers with CM_UNK_RESPONSE (190) with 16 (static) bytes which change every 5s, maybe some kind of consistency check
		addPacketOpcode(SM_TARGET_UPDATE.class, 0x51);
		addPacketOpcode(SM_HOUSE_EDIT.class, 0x52);
		addPacketOpcode(SM_PLASTIC_SURGERY.class, 0x53);
		addPacketOpcode(SM_CONQUEROR_PROTECTOR.class, 0x54);
		addPacketOpcode(SM_INFLUENCE_RATIO.class, 0x55);
		addPacketOpcode(SM_FORTRESS_STATUS.class, 0x56);
		addPacketOpcode(SM_CAPTCHA.class, 0x57);
		addPacketOpcode(SM_RENAME.class, 0x58);
		addPacketOpcode(SM_SHOW_NPC_ON_MAP.class, 0x59);
		addPacketOpcode(SM_GROUP_INFO.class, 0x5A);
		addPacketOpcode(SM_GROUP_MEMBER_INFO.class, 0x5B);
		addPacketOpcode(SM_RIDE_ROBOT.class, 0x5C);
		// 93
		// 0x5E
		// 0x5F
		// 0x60
		// 97
		addPacketOpcode(SM_QUIT_RESPONSE.class, 0x62);
		addPacketOpcode(SM_CHAT_WINDOW.class, 0x63);// 2.1
		// 100
		addPacketOpcode(SM_PET.class, 0x65); // 2.7
		// 102
		addPacketOpcode(SM_ITEM_COOLDOWN.class, 0x67); // 2.7
		addPacketOpcode(SM_UPDATE_NOTE.class, 0x68);
		addPacketOpcode(SM_PLAY_MOVIE.class, 0x69);
		// 106
		// 0x6B
		// 0x6C
		// 109 sends error message boxes like "Authorization error" (//fsc 0x6D c 1) or "Unknown error" (//fsc 0x6D c 2). clicking ok closes the client
		addPacketOpcode(SM_LEGION_INFO.class, 0x6E); // 2.7
		addPacketOpcode(SM_LEGION_ADD_MEMBER.class, 0x6F);
		addPacketOpcode(SM_LEGION_LEAVE_MEMBER.class, 0x70);
		addPacketOpcode(SM_LEGION_UPDATE_MEMBER.class, 0x71);
		addPacketOpcode(SM_LEGION_UPDATE_TITLE.class, 0x72);
		addPacketOpcode(SM_ATTACK_RESPONSE.class, 0x73);
		addPacketOpcode(SM_HOUSE_REGISTRY.class, 0x74);
		// 117
		// 118
		addPacketOpcode(SM_LEGION_UPDATE_SELF_INTRO.class, 0x77);
		// addPacketOpcode(SM_RIFT_STATUS.class, 0x78); // 1.9
		addPacketOpcode(SM_INSTANCE_SCORE.class, 0x79);
		addPacketOpcode(SM_AUTO_GROUP.class, 0x7A);
		addPacketOpcode(SM_QUEST_COMPLETED_LIST.class, 0x7B);
		addPacketOpcode(SM_QUEST_ACTION.class, 0x7C);
		addPacketOpcode(SM_GAMEGUARD.class, 0x7D);
		// addPacketOpcode(SM_BUY_LIST.class, 0x7E); // 1.5.4
		addPacketOpcode(SM_NEARBY_QUESTS.class, 0x7F);
		addPacketOpcode(SM_PING_RESPONSE.class, 0x80);
		// 129
		addPacketOpcode(SM_CUBE_UPDATE.class, 0x82);
		addPacketOpcode(SM_HOUSE_SCRIPTS.class, 0x83);
		addPacketOpcode(SM_FRIEND_LIST.class, 0x84);
		// 133
		addPacketOpcode(SM_PRIVATE_STORE.class, 0x86);
		addPacketOpcode(SM_GROUP_LOOT.class, 0x87);
		addPacketOpcode(SM_ABYSS_RANK_UPDATE.class, 0x88);
		addPacketOpcode(SM_MAY_LOGIN_INTO_GAME.class, 0x89);
		addPacketOpcode(SM_ABYSS_RANKING_PLAYERS.class, 0x8A);
		addPacketOpcode(SM_ABYSS_RANKING_LEGIONS.class, 0x8B);
		addPacketOpcode(SM_INSTANCE_STAGE_INFO.class, 0x8C);
		addPacketOpcode(SM_INSTANCE_INFO.class, 0x8D);
		addPacketOpcode(SM_PONG.class, 0x8E);
		// 143
		addPacketOpcode(SM_KISK_UPDATE.class, 0x90);
		addPacketOpcode(SM_PRIVATE_STORE_NAME.class, 0x91);
		addPacketOpcode(SM_BROKER_SERVICE.class, 0x92);
		addPacketOpcode(SM_INSTANCE_COUNT_INFO.class, 0x93);
		addPacketOpcode(SM_MOTION.class, 0x94);
		// addPacketOpcode(SM_BROKER_SETTLED_LIST.class, 0x95); / Systemfehler später erneut versuchen ??
		addPacketOpcode(SM_UNK_3_5_1.class, 0x96);
		addPacketOpcode(SM_TRADE_IN_LIST.class, 0x97);
		addPacketOpcode(SM_SECURITY_TOKEN.class, 0x98);
		addPacketOpcode(SM_SUMMON_PANEL.class, 0x99);
		addPacketOpcode(SM_SUMMON_OWNER_REMOVE.class, 0x9A);
		addPacketOpcode(SM_SUMMON_UPDATE.class, 0x9B);
		addPacketOpcode(SM_TRANSFORM_IN_SUMMON.class, 0x9C);
		addPacketOpcode(SM_LEGION_MEMBERLIST.class, 0x9D);
		addPacketOpcode(SM_LEGION_EDIT.class, 0x9E);
		addPacketOpcode(SM_TOLL_INFO.class, 0x9F); // ingameshop
		// 0xA0
		addPacketOpcode(SM_MAIL_SERVICE.class, 0xA1);
		addPacketOpcode(SM_SUMMON_USESKILL.class, 0xA2);
		addPacketOpcode(SM_WINDSTREAM.class, 0xA3);
		addPacketOpcode(SM_WINDSTREAM_ANNOUNCE.class, 0xA4);
		addPacketOpcode(SM_RECIPE_COOLDOWN.class, 0xA5);
		addPacketOpcode(SM_FIND_GROUP.class, 0xA6);
		addPacketOpcode(SM_REPURCHASE.class, 0xA7);
		addPacketOpcode(SM_WAREHOUSE_INFO.class, 0xA8);
		addPacketOpcode(SM_WAREHOUSE_ADD_ITEM.class, 0xA9);
		addPacketOpcode(SM_DELETE_WAREHOUSE_ITEM.class, 0xAA);
		addPacketOpcode(SM_WAREHOUSE_UPDATE_ITEM.class, 0xAB);
		addPacketOpcode(SM_IN_GAME_SHOP_CATEGORY_LIST.class, 0xAC); // ingameshop
		addPacketOpcode(SM_IN_GAME_SHOP_LIST.class, 0xAD); // ingameshop
		addPacketOpcode(SM_IN_GAME_SHOP_ITEM.class, 0xAE); // ingameshop
		addPacketOpcode(SM_ICON_INFO.class, 0xAF);
		addPacketOpcode(SM_TITLE_INFO.class, 0xB0);
		addPacketOpcode(SM_CHARACTER_SELECT.class, 0xB1);
		addPacketOpcode(SM_GROUP_DATA_EXCHANGE.class, 0xB2);
		// 179
		// addPacketOpcode(SM_BROKER_REGISTERED_LIST.class, 0xB1);
		addPacketOpcode(SM_CRAFT_ANIMATION.class, 0xB4);
		addPacketOpcode(SM_CRAFT_UPDATE.class, 0xB5);
		addPacketOpcode(SM_ASCENSION_MORPH.class, 0xB6);
		addPacketOpcode(SM_ITEM_USAGE_ANIMATION.class, 0xB7);
		addPacketOpcode(SM_CUSTOM_SETTINGS.class, 0xB8);
		addPacketOpcode(SM_DUEL.class, 0xB9);
		// 186 SM_UNK_REQUEST2 client answers with CM_UNK_RESPONSE2 (204). first D in CM and SM is identical (//fsc 0xBA d 1)
		addPacketOpcode(SM_PET_EMOTE.class, 0xBB);
		// 188 destroy your ui & get kicked!
		// 189
		// 190
		addPacketOpcode(SM_QUESTIONNAIRE.class, 0xBF);
		// 192 TODO: GM_NOTICE format: ddsdhs ?
		addPacketOpcode(SM_DIE.class, 0xC1);
		addPacketOpcode(SM_RESURRECT.class, 0xC2);
		addPacketOpcode(SM_FORCED_MOVE.class, 0xC3);
		addPacketOpcode(SM_TELEPORT_MAP.class, 0xC4);
		addPacketOpcode(SM_USE_OBJECT.class, 0xC5);
		// TODO! 0xC6 format: d - oid, cdd - smth related to SM_NPC_INFO
		addPacketOpcode(SM_L2AUTH_LOGIN_CHECK.class, 0xC7);
		addPacketOpcode(SM_CHARACTER_LIST.class, 0xC8);
		addPacketOpcode(SM_CREATE_CHARACTER.class, 0xC9);
		addPacketOpcode(SM_DELETE_CHARACTER.class, 0xCA);
		addPacketOpcode(SM_RESTORE_CHARACTER.class, 0xCB);
		addPacketOpcode(SM_TARGET_IMMOBILIZE.class, 0xCC);
		addPacketOpcode(SM_LOOT_STATUS.class, 0xCD);
		addPacketOpcode(SM_LOOT_ITEMLIST.class, 0xCE);
		addPacketOpcode(SM_RECIPE_LIST.class, 0x0CF);
		addPacketOpcode(SM_MANTRA_EFFECT.class, 0xD0);
		addPacketOpcode(SM_SIEGE_LOCATION_INFO.class, 0xD1);
		addPacketOpcode(SM_SIEGE_LOCATION_STATE.class, 0xD2);
		addPacketOpcode(SM_PLAYER_SEARCH.class, 0xD3);
		// 212 TODO: Legion Wappen format: c 0 = successfull 1 = already uploaded
		addPacketOpcode(SM_LEGION_SEND_EMBLEM.class, 0xD5);
		addPacketOpcode(SM_LEGION_SEND_EMBLEM_DATA.class, 0xD6);
		addPacketOpcode(SM_LEGION_UPDATE_EMBLEM.class, 0xD7);
		// skill related //fsc 0xD8 cccdd 1 0 0 15000 1000 (first d: icon timer, last d: skill lock timer)
		addPacketOpcode(SM_PLAYER_REGION.class, 0xD9);
		addPacketOpcode(SM_SHIELD_EFFECT.class, 0xDA);
		// 219 TODO: format: d 5 -> switches UI to something unk and client responds with opcode 0x199 packet.. 6 switches back to normal
		addPacketOpcode(SM_ABYSS_ARTIFACT_INFO3.class, 0xDC);
		addPacketOpcode(SM_HOUSE_TELEPORT.class, 0xDD);
		addPacketOpcode(SM_FRIEND_RESPONSE.class, 0xDE);
		addPacketOpcode(SM_BLOCK_RESPONSE.class, 0xDF);
		addPacketOpcode(SM_BLOCK_LIST.class, 0xE0);
		addPacketOpcode(SM_FRIEND_NOTIFY.class, 0xE1);
		addPacketOpcode(SM_TOWNS_LIST.class, 0xE2);
		addPacketOpcode(SM_FRIEND_STATUS.class, 0xE3);
		// addPacketOpcode(SM_VIRTUAL_AUTH.class, 0xE4); // 1.5.0 - client answers with CM_MAC_ADDRESS and CM_L2AUTH_LOGIN_CHECK
		addPacketOpcode(SM_CHANNEL_INFO.class, 0xE5);
		addPacketOpcode(SM_CHAT_INIT.class, 0xE6);
		addPacketOpcode(SM_MACRO_LIST.class, 0xE7);
		addPacketOpcode(SM_MACRO_RESULT.class, 0xE8);
		addPacketOpcode(SM_NICKNAME_CHECK_RESPONSE.class, 0xE9);
		// 234
		addPacketOpcode(SM_BIND_POINT_INFO.class, 0xEB);
		addPacketOpcode(SM_RIFT_ANNOUNCE.class, 0xEC);
		addPacketOpcode(SM_ABYSS_RANK.class, 0xED);
		addPacketOpcode(SM_ACCOUNT_PROPERTIES.class, 0xEE);
		// 239 petition/support - //fsc 239 cdhsdccd 1 0 42 TestNo. 1 127 100 5
		addPacketOpcode(SM_FRIEND_UPDATE.class, 0xF0);
		addPacketOpcode(SM_LEARN_RECIPE.class, 0xF1);
		addPacketOpcode(SM_RECIPE_DELETE.class, 0xF2);
		addPacketOpcode(SM_FORTRESS_INFO.class, 0xF3);
		addPacketOpcode(SM_FLY_TIME.class, 0xF4);
		addPacketOpcode(SM_ALLIANCE_INFO.class, 0xF5);
		addPacketOpcode(SM_ALLIANCE_MEMBER_INFO.class, 0xF6);
		addPacketOpcode(SM_LEAVE_GROUP_MEMBER.class, 0xF7);
		// 248
		addPacketOpcode(SM_SHOW_BRAND.class, 0xF9);
		addPacketOpcode(SM_ALLIANCE_READY_CHECK.class, 0xFA);
		// 251 first c or h must be size or type since nonzero leads to a client crash, because of incorrect following data 
		addPacketOpcode(SM_PRICES.class, 0xFC);
		addPacketOpcode(SM_TRADELIST.class, 0xFD);
		// 254
		addPacketOpcode(SM_RECONNECT_KEY.class, 0xFF);
		addPacketOpcode(SM_HOUSE_BIDS.class, 0x100);
		// 257 TODO: Format: d "Unknown Error d"
		// 258
		addPacketOpcode(SM_RECEIVE_BIDS.class, 0x103);
		// 260
		// 261 FastTrack not available
		addPacketOpcode(SM_HOUSE_PAY_RENT.class, 0x106);
		addPacketOpcode(SM_HOUSE_OWNER_INFO.class, 0x107);
		addPacketOpcode(SM_OBJECT_USE_UPDATE.class, 0x108);
		// 265
		addPacketOpcode(SM_PACKAGE_INFO_NOTIFY.class, 0x10A);
		// 267
		addPacketOpcode(SM_HOUSE_OBJECT.class, 0x10C);
		addPacketOpcode(SM_DELETE_HOUSE_OBJECT.class, 0x10D);
		addPacketOpcode(SM_HOUSE_OBJECTS.class, 0x10E);
		addPacketOpcode(SM_HOUSE_RENDER.class, 0x10F);
		addPacketOpcode(SM_DELETE_HOUSE.class, 0x110);
		// 273
		addPacketOpcode(SM_GF_WEBSHOP_TOKEN_RESPONSE.class, 0x112);
		addPacketOpcode(SM_HOUSE_ACQUIRE.class, 0x113);
		addPacketOpcode(SM_STATS_STATUS_UNK.class, 0x114);
		// 277 // download parts, "client is restricted youve to download part 1 first"
		// 0x116
		addPacketOpcode(SM_MARK_FRIENDLIST.class, 0x117);
		addPacketOpcode(SM_CHALLENGE_LIST.class, 0x118);
		// 281
		// 0x11A shows buff icon + symbols next to players name //fsc 0x11a dd playerObjId iconId (2 = Reward for new user, 3 = reward for returning user, 10 benefits for special user)
		// addPacketOpcode(SM_DISPUTE_LAND.class, 0x11B);
		addPacketOpcode(SM_FIRST_SHOW_DECOMPOSABLE.class, 0x11C);
		addPacketOpcode(SM_MEGAPHONE.class, 0x11D); // Megaphone
		addPacketOpcode(SM_SECONDARY_SHOW_DECOMPOSABLE.class, 0x11E);
		// 287
		addPacketOpcode(SM_TUNE_RESULT.class, 0x120);
		addPacketOpcode(SM_UNWRAP_ITEM.class, 0x121);
		addPacketOpcode(SM_QUEST_REPEAT.class, 0x122);
		// addPacketOpcode(SM_UNK_4_5.class, 0x123);
		addPacketOpcode(SM_AFTER_TIME_CHECK_4_7_5.class, 0x124);
		addPacketOpcode(SM_AFTER_SIEGE_LOCINFO_475.class, 0x125);
		// 294
		// 0x127
		addPacketOpcode(SM_BIND_POINT_TELEPORT.class, 0x128);
		// 297
		addPacketOpcode(SM_UPGRADE_ARCADE.class, 0x12A);
		addPacketOpcode(SM_ATREIAN_PASSPORT.class, 0x12B);
		// 300
		// 0x12D
		addPacketOpcode(SM_LEGION_DOMINION_RANK.class, 0x12E);
		addPacketOpcode(SM_LEGION_DOMINION_LOC_INFO.class, 0x12F);
		addPacketOpcode(SM_CUSTOM_PACKET.class, 99999); // fake packet
	}

	static int getOpcode(Class<? extends AionServerPacket> packetClass) {
		Integer opcode = opcodes.get(packetClass);
		if (opcode == null)
			throw new IllegalArgumentException("There is no opcode for " + packetClass + " defined.");

		return opcode;
	}

	private static void addPacketOpcode(Class<? extends AionServerPacket> packetClass, int opcode) {
		if (opcode < 0)
			return;

		if (opcodes.values().contains(opcode))
			throw new IllegalArgumentException(String.format("There already exists another packet with id 0x%02X", opcode));

		opcodes.put(packetClass, opcode);
	}

}
