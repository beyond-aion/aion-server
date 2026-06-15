package com.aionemu.gameserver.network.aion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.Crypt;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.clientpackets.*;

/**
 * @author Neon
 */
public class AionClientPacketFactory {

	private static final Logger log = LoggerFactory.getLogger(AionClientPacketFactory.class);
	private static final PacketInfo<? extends AionClientPacket>[] packets = new PacketInfo<?>[250];

	static {
		try {
			packets[0] = new PacketInfo<>(CM_VERSION_CHECK.class, State.CONNECTED); // [C_VERSION (VersionPacket)]
			// packets[1] = [C_QUERY_PASSPORT (QueryPassportPacket)]
			packets[2] = new PacketInfo<>(CM_DISCONNECT.class, State.AUTHED, State.IN_GAME); // [C_LOGOUT (LogoutPacket)]
			packets[3] = new PacketInfo<>(CM_QUIT.class, State.AUTHED, State.IN_GAME); // [C_ASK_QUIT (AskQuitPacket)]
			packets[4] = new PacketInfo<>(CM_MAY_QUIT.class, State.IN_GAME); // [C_READY_TO_QUIT (ReadyToQuitPacket)]
			packets[5] = new PacketInfo<>(CM_REVIVE.class, State.IN_GAME); // [C_DEAD_RESTART (DeadRestartPacket)]
			// packets[6] = [C_CHECK_LEVEL_DATA_VERSION (CheckLevelDataVersionPacket)] sent when executing "\g_send_level_version 1" from the in-game console
			packets[7] = new PacketInfo<>(CM_CHARACTER_EDIT.class, State.AUTHED); // [C_EDIT_CHARACTER (EditCharacterPacket)]
			packets[8] = new PacketInfo<>(CM_ENTER_WORLD.class, State.AUTHED); // [C_ENTER_WORLD (EnterWorldPacket)]
			packets[9] = new PacketInfo<>(CM_LEVEL_READY.class, State.IN_GAME); // [C_LEVEL_READY (LevelReadyPacket)]
			packets[10] = new PacketInfo<>(CM_UI_SETTINGS.class, State.IN_GAME); // [C_SAVE_CLIENT_SETTINGS (SaveClientSettingsPacket)]
			packets[11] = new PacketInfo<>(CM_OBJECT_SEARCH.class, State.IN_GAME); // [C_FIND_NPC_POS (FindNpcPosPacket)]
			packets[12] = new PacketInfo<>(CM_CUSTOM_SETTINGS.class, State.IN_GAME); // [C_CHANGE_OPTION_FLAGS (ChangeOptionFlagsPacket)]
			// packets[13] = [C_CHANGE_DIRECTION (ChangeDirectionPacket)]
			packets[14] = new PacketInfo<>(CM_CAPTCHA.class, State.IN_GAME); // [C_CAPTCHA (ReceiveCaptchaAnswerPacket)]
			packets[15] = new PacketInfo<>(CM_TELEPORT_ANIMATION_DONE.class, State.IN_GAME); // [C_ACCEPT_TELEPORT (AcceptTeleportPacket)]
			packets[16] = new PacketInfo<>(CM_LEGION_SEND_EMBLEM_INFO.class, State.IN_GAME); // [C_REQUEST_GUILD_NAME (RequestGuildName)]
			packets[17] = new PacketInfo<>(CM_POSITION_SELF.class, State.IN_GAME); // [C_BLINK (ReturnBlinkPacket)]
			packets[18] = new PacketInfo<>(CM_TIME_CHECK.class, State.CONNECTED, State.AUTHED, State.IN_GAME); // [C_SYNC_TIME (SyncTimePacket)]
			packets[19] = new PacketInfo<>(CM_GATHER.class, State.IN_GAME); // [C_GATHER (GatherPacket)]
			// packets[20] = [C_MINIGAME (MinigamePacket)] likely tied to -minigame client start parameter
			packets[21] = new PacketInfo<>(CM_PET_EMOTE.class, State.IN_GAME); // [C_FUNCTIONAL_PET_MOVE (FunctionalPetActionMoveOrActionPacket)]
			packets[22] = new PacketInfo<>(CM_PET.class, State.IN_GAME); // [C_FUNCTIONAL_PET (FunctionalPetPacket)]
			packets[23] = new PacketInfo<>(CM_OPEN_STATICDOOR.class, State.IN_GAME); // [C_TOGGLE_DOOR (ToggleDoorPacket)]
			// packets[24] = [C_TOGGLE_CHEST (ToggleChestPacket)]
			// packets[25] = [C_GIVE_ITEM (GiveItemPacket)]
			// packets[26] = [C_PETITION (PetitionPacket)]
			packets[27] = new PacketInfo<>(CM_CHAT_MESSAGE_PUBLIC.class, State.IN_GAME); // [C_SAY (SayPacket)]
			packets[28] = new PacketInfo<>(CM_CHAT_MESSAGE_WHISPER.class, State.IN_GAME); // [C_WHISPER (WhisperPacket)]
			packets[29] = new PacketInfo<>(CM_LEGION_DOMINION_REQUEST_RANKING.class, State.IN_GAME); // [C_REQUEST_LEGION_DOMINION_RANKIN]
			packets[30] = new PacketInfo<>(CM_HOUSE_SCRIPT.class, State.IN_GAME); // [C_SAVE_HOUSE_SCRIPT (SaveHouseScriptPacket)]
			packets[31] = new PacketInfo<>(CM_TARGET_SELECT.class, State.IN_GAME); // [C_CHANGE_TARGET (ChangeTargetPacket)]
			packets[32] = new PacketInfo<>(CM_ATTACK.class, State.IN_GAME); // [C_ATTACK (AttackPacket)]
			packets[33] = new PacketInfo<>(CM_CASTSPELL.class, State.IN_GAME); // [C_USE_SKILL (UseSkillPacket)]
			packets[34] = new PacketInfo<>(CM_TOGGLE_SKILL_DEACTIVATE.class, State.IN_GAME); // [C_TURN_OFF_TOGGLE_SKILL (FUN_14060eed0)]
			packets[35] = new PacketInfo<>(CM_REMOVE_ALTERED_STATE.class, State.IN_GAME); // [C_TURN_OFF_ABNORMAL_STATUS (TurnOffAbnormalStatusPacket)]
			// packets[36] = [C_TURN_OFF_MAINTAIN_SKILL (&LAB_14060f880)]
			packets[37] = new PacketInfo<>(CM_USE_ITEM.class, State.IN_GAME); // [C_USE_ITEM (&LAB_14060fc40)]
			packets[38] = new PacketInfo<>(CM_EQUIP_ITEM.class, State.IN_GAME); // [C_USE_EQUIPMENT_ITEM (&LAB_140610070)]
			packets[39] = new PacketInfo<>(CM_CHAT_PLAYER_INFO.class, State.IN_GAME); // [C_ASK_PC_INFO (AskPCInfoPacket)]
			packets[40] = new PacketInfo<>(CM_PLAYER_LISTENER.class, State.IN_GAME); // [C_SAVE (SavePacket)]
			packets[41] = new PacketInfo<>(CM_BUILDER_COMMAND.class, State.IN_GAME); // [C_BUILDER_COMMAND (BuilderCommandPacket)]
			packets[42] = new PacketInfo<>(CM_BUILDER_CONTROL.class, State.IN_GAME); // [C_BUILDER_CONTROL (&LAB_140610e40)]
			packets[43] = new PacketInfo<>(CM_EMOTION.class, State.IN_GAME); // [C_ACTION (ActionPacket)]
			packets[44] = new PacketInfo<>(CM_PING.class, State.IN_GAME, State.AUTHED); // [C_ALIVE (AlivePacket)]
			packets[45] = new PacketInfo<>(CM_LEGION.class, State.IN_GAME); // [C_GUILD (GuildPacket)]
			packets[46] = new PacketInfo<>(CM_INSTANCE_LEAVE.class, State.IN_GAME); // [C_LEAVE_INSTANTDUNGEON (LeaveInstantDungeonPacket)]
			packets[47] = new PacketInfo<>(CM_LEGION_SEND_EMBLEM.class, State.IN_GAME); // [C_REQUEST_GUILD_EMBLEM_IMG (RequestGuildEmblemPacket)]
			packets[48] = new PacketInfo<>(CM_MOVE.class, State.IN_GAME); // [C_MOVE_NEW (MoveNewPacket)]
			packets[49] = new PacketInfo<>(CM_MOVE_IN_AIR.class, State.IN_GAME); // [C_PATH_FLY (PathFlyPacket)]
			packets[50] = new PacketInfo<>(CM_QUESTION_RESPONSE.class, State.IN_GAME); // [C_ANSWER (AnswerPacket)]
			packets[51] = new PacketInfo<>(CM_BUY_ITEM.class, State.IN_GAME); // [C_BUY_SELL (&LAB_140615590)]
			packets[52] = new PacketInfo<>(CM_SHOW_DIALOG.class, State.IN_GAME); // [C_START_DIALOG (StartDialogPacket)]
			packets[53] = new PacketInfo<>(CM_CLOSE_DIALOG.class, State.IN_GAME); // [C_END_DIALOG (EndDialogPacket)]
			packets[54] = new PacketInfo<>(CM_DIALOG_SELECT.class, State.IN_GAME); // [C_HACTION (HActionPacket)]
			packets[55] = new PacketInfo<>(CM_LEGION_HISTORY.class, State.IN_GAME); // [C_REQUEST_GUILD_HISTORY (RequestGuildHistoryPacket)]
			// packets[56] = [C_BOOKMARK (BookmarkPacket)]
			// packets[57] = [C_DELETE_BOOKMARK (DeleteBookmarkPacket)]
			packets[58] = new PacketInfo<>(CM_SET_NOTE.class, State.IN_GAME); // [C_TODAY_WORDS (TodayWordsPacket)]
			packets[59] = new PacketInfo<>(CM_LEGION_MODIFY_EMBLEM.class, State.IN_GAME); // [C_CHANGE_EMBLEM_VER (ChangeEmblemVerPacket)]
			// packets[60] = [C_REQUEST_ABYSS_OP_POINTS]
			packets[61] = new PacketInfo<>(CM_CHAT_GROUP_INFO.class, State.IN_GAME); // [C_ASK_PARTY_INFO (AskPartyInfoPacket)]
			packets[62] = new PacketInfo<>(CM_CHECK_PAK.class, State.IN_GAME); // [C_ASK_LOG (AskLogPacket)]
			packets[63] = new PacketInfo<>(CM_EXCHANGE_REQUEST.class, State.IN_GAME); // [C_ASK_XCHG (AskExchangePacket)]
			packets[64] = new PacketInfo<>(CM_EXCHANGE_ADD_ITEM.class, State.IN_GAME); // [C_ADD_XCHG (AddExchangePacket)]
			// packets[65] = [C_REMOVE_XCHG (RemoveExchangePacket)]
			packets[66] = new PacketInfo<>(CM_EXCHANGE_ADD_KINAH.class, State.IN_GAME); // [C_XCHG_GOLD (ExchangeGoldPacket)]
			packets[67] = new PacketInfo<>(CM_EXCHANGE_LOCK.class, State.IN_GAME); // [C_CHECK_XCHG (CheckExchangePacket)]
			packets[68] = new PacketInfo<>(CM_EXCHANGE_OK.class, State.IN_GAME); // [C_ACCEPT_XCHG (&LAB_140618cd0)]
			packets[69] = new PacketInfo<>(CM_EXCHANGE_CANCEL.class, State.IN_GAME); // [C_CANCEL_XCHG (CancelExchangePacket)]
			packets[70] = new PacketInfo<>(CM_WINDSTREAM.class, State.IN_GAME); // [C_WIND_PATH (WindPathPacket)]
			packets[71] = new PacketInfo<>(CM_MOTION.class, State.IN_GAME); // [C_CUSTOM_ANIM (CustomAnimPacket)]
			packets[72] = new PacketInfo<>(CM_HOUSE_KICK.class, State.IN_GAME); // [C_HOUSING_KICK (HousingKickPacket)]
			packets[73] = new PacketInfo<>(CM_HOUSE_SETTINGS.class, State.IN_GAME); // [C_HOUSING_CONFIG (HousingConfigPacket)]
			packets[74] = new PacketInfo<>(CM_MANASTONE.class, State.IN_GAME); // [C_ENCHANT_ITEM (EnchantItemPacket)]
			packets[75] = new PacketInfo<>(CM_HOUSE_DECORATE.class, State.IN_GAME); // [C_HOUSING_CUSTOMIZE (HousingCustomizePacket)]
			packets[76] = new PacketInfo<>(CM_LEGION_WH_KINAH.class, State.IN_GAME); // [C_GUILD_FUND (GuildFundPacket)]
			packets[77] = new PacketInfo<>(CM_FIND_GROUP.class, State.IN_GAME); // [C_PARTY_MATCH (PartyMatchPacket)]
			packets[78] = new PacketInfo<>(CM_CHARGE_ITEM.class, State.IN_GAME); // [C_CHARGE_ITEM (&LAB_14061f4c0)]
			packets[79] = new PacketInfo<>(CM_GROUP_DATA_EXCHANGE.class, State.IN_GAME); // [C_CLIENT_BROADCAST (ClientBroadcastPacket)]
			packets[80] = new PacketInfo<>(CM_DELETE_QUEST.class, State.IN_GAME); // [C_GIVE_UP_QUEST (GiveUpQuestPacket)]
			packets[81] = new PacketInfo<>(CM_PLAY_MOVIE_END.class, State.IN_GAME); // [C_QUIT_CUTSCENE (QuitCutScenePacket)]
			packets[82] = new PacketInfo<>(CM_HOUSE_EDIT.class, State.IN_GAME); // [C_HOUSING_OBJECT (HousingObjectPacket)]
			// packets[83] = [C_HOUSING_OBJECT_LIST (HousingObjectListPacket)]
			packets[84] = new PacketInfo<>(CM_STOP_TRAINING.class, State.IN_GAME); // [C_ACCOUNT_INSTANTDUNGEON (AccountInstantDungeon)]
			// packets[85] = [C_UNUSED_NEW_5]
			// packets[86] = [C_QUERY_NUMBER_RESULT (QueryNumberResultPacket)]
			// packets[87] = [C_FATIGUE_KOREA (&LAB_140621240)]
			packets[88] = new PacketInfo<>(CM_BUY_TRADE_IN_TRADE.class, State.IN_GAME); // [C_TRADE_IN (&LAB_140621590)]
			packets[89] = new PacketInfo<>(CM_RECIPE_DELETE.class, State.IN_GAME); // [C_RECIPE_DELETE (RecipeDeletePacket)]
			packets[90] = new PacketInfo<>(CM_ITEM_REMODEL.class, State.IN_GAME); // [C_CHANGE_ITEM_SKIN (ChangeItemSkinPacket)]
			// packets[91] = new PacketInfo<>(CM_GODSTONE_SOCKET.class, State.IN_GAME); // [C_GIVE_ITEM_PROC (GiveItemProcPacket)] happens via CM_MANASTONE now (no npc required anymore)
			packets[92] = new PacketInfo<>(CM_SECURITY_TOKEN.class, State.CONNECTED, State.AUTHED, State.IN_GAME); // [C_REQ_WEB_SESSIONKEY (RequestWebSessionKey)]
			// packets[93] = [C_GET_ON_VEHICLE (GetOnVehiclePacket)]
			// packets[94] = [C_GET_OFF_VEHICLE (GetOffVehiclePacket)]
			packets[95] = new PacketInfo<>(CM_HOUSE_TELEPORT_BACK.class, State.IN_GAME); // [C_RETURN_TO_HOUSEGATE (ReturnToHouseGatePacket)]
			packets[96] = new PacketInfo<>(CM_PLAYER_STATUS_INFO.class, State.IN_GAME); // [C_PARTY (PartyPacket)]
			packets[97] = new PacketInfo<>(CM_INVITE_TO_GROUP.class, State.IN_GAME); // [C_PARTY_BY_NAME (&LAB_1406224a0)]
			// packets[98] = [C_ALLI_CHANGE_GROUP (AllianceChangeGroupPacket)]
			// packets[99] = [C_UNUSED_19]
			packets[100] = new PacketInfo<>(CM_VIEW_PLAYER_DETAILS.class, State.IN_GAME); // [C_VIEW_OTHER_INVENTORY (ViewOtherInventoryPacket)]
			// packets[101] = [C_USE_CP_RESET_COST_REQ]
			// packets[102] = [C_UPDATE_USE_CP]
			packets[103] = new PacketInfo<>(CM_PING_REQUEST.class, State.IN_GAME); // [C_PING (&LAB_140632c80)]
			packets[104] = new PacketInfo<>(CM_GAMEGUARD.class, State.IN_GAME, State.AUTHED); // [C_NCGUARD (FUN_140632e50)]
			// packets[105] = [C_UNUSED_21]
			// packets[106] = [C_PLATE (PlatePacket)]
			packets[107] = new PacketInfo<>(CM_CLIENT_COMMAND_ROLL.class, State.IN_GAME); // [C_SIMPLE_DICE (SimpleDicePacket)]
			packets[108] = new PacketInfo<>(CM_GROUP_DISTRIBUTION.class, State.IN_GAME); // [C_SPLIT_GOLD (SplitGoldPacket)]
			// packets[109] = new PacketInfo<>(CM_SHOW_LOCATION.class, State.IN_GAME); // [C_GET_PK_COUNT (CheckPkPacket)] when writing /loc or /location in chat (response is SM_SYSTEM_MESSAGE.STR_CMD_LOCATION_DESC)
			packets[110] = new PacketInfo<>(CM_MARK_FRIENDLIST.class, State.IN_GAME); // [C_QUERY_BUDDY (QueryBuddyPacket)]
			packets[111] = new PacketInfo<>(CM_FRIEND_ADD.class, State.IN_GAME); // [C_ADD_BUDDY (AddBuddyPacket)]
			packets[112] = new PacketInfo<>(CM_FRIEND_DEL.class, State.IN_GAME); // [C_REMOVE_BUDDY (RemoveBuddyPacket)]
			// packets[113] = [C_SMS (SMSPacket)]
			packets[114] = new PacketInfo<>(CM_DUEL_REQUEST.class, State.IN_GAME); // [C_DUEL (DuelPacket)]
			// packets[115] = [C_UNUSED__03]
			packets[116] = new PacketInfo<>(CM_DELETE_ITEM.class, State.IN_GAME); // [C_DESTROY_ITEM (DestroyItemPacket)]
			packets[117] = new PacketInfo<>(CM_BROKER_SELL_WINDOW.class, State.IN_GAME); // [C_VENDOR_AVG_SOLDPRICE]
			packets[118] = new PacketInfo<>(CM_ABYSS_RANKING_LEGIONS.class, State.IN_GAME); // [C_REQUEST_ABYSS_GUILD_INFO (RequestAbyssGuildInfoPacket)]
			packets[119] = new PacketInfo<>(CM_PRIVATE_STORE.class, State.IN_GAME); // [C_PERSONAL_SHOP (PersonalShopPacket)]
			packets[120] = new PacketInfo<>(CM_PRIVATE_STORE_NAME.class, State.IN_GAME); // [C_SHOP_MSG (ShopMsgPacket)]
			packets[121] = new PacketInfo<>(CM_SUMMON_COMMAND.class, State.IN_GAME); // [C_PET_ORDER (PetOrderPacket)]
			// packets[122] = [C_GIVE_EXP_TO_PET (GiveExpToPetPacket)]
			packets[123] = new PacketInfo<>(CM_BROKER_LIST.class, State.IN_GAME); // [C_VENDOR_ITEMLIST_CATEGORY (VendorItemListCategoryPacket)]
			packets[124] = new PacketInfo<>(CM_BROKER_SEARCH.class, State.IN_GAME); // [C_VENDOR_ITEMLIST_NAME (VendorItemListNamePacket)]
			packets[125] = new PacketInfo<>(CM_BROKER_REGISTERED.class, State.IN_GAME); // [C_VENDOR_MYLIST (VendorMyListPacket)]
			packets[126] = new PacketInfo<>(CM_BUY_BROKER_ITEM.class, State.IN_GAME); // [C_VENDOR_BUY (VendorBuyPacket)]
			packets[127] = new PacketInfo<>(CM_REGISTER_BROKER_ITEM.class, State.IN_GAME); // [C_VENDOR_COMMIT (&LAB_1406272c0)]
			packets[128] = new PacketInfo<>(CM_BROKER_CANCEL_REGISTERED.class, State.IN_GAME); // [C_VENDOR_CANCEL (VendorCancelPacket)]
			packets[129] = new PacketInfo<>(CM_BROKER_SETTLE_LIST.class, State.IN_GAME); // [C_VENDOR_MYLOG (VendorMyLogPacket)]
			packets[130] = new PacketInfo<>(CM_BROKER_SETTLE_ACCOUNT.class, State.IN_GAME); // [C_VENDOR_COLLECT (VendorCollectPacket)]
			// packets[131] = [C_COMMPACKET (CommPacket)]
			packets[132] = new PacketInfo<>(CM_SEND_MAIL.class, State.IN_GAME); // [C_MAIL_WRITE (MailWritePacket)]
			packets[133] = new PacketInfo<>(CM_CHECK_MAIL_LIST.class, State.IN_GAME); // [C_MAIL_LIST (MailListPacket)]
			packets[134] = new PacketInfo<>(CM_READ_MAIL.class, State.IN_GAME); // [C_MAIL_READ (MailReadPacket)]
			// packets[135] = [C_MAIL_SETREAD (MailSetReadPacket)]
			packets[136] = new PacketInfo<>(CM_GET_MAIL_ATTACHMENT.class, State.IN_GAME); // [C_MAIL_GETITEM (MailGetItemPacket)]
			packets[137] = new PacketInfo<>(CM_DELETE_MAIL.class, State.IN_GAME); // [C_MAIL_DELETE (MailDeletePacket)]
			// packets[138] = [C_DICE (DicePacket)]
			packets[139] = new PacketInfo<>(CM_TITLE_SET.class, State.IN_GAME); // [C_CHANGE_TITLE (ChangeTitlePacket)]
			// packets[140] = [C_REMOVE_TITLE (&LAB_14062bca0)]
			packets[141] = new PacketInfo<>(CM_CRAFT.class, State.IN_GAME); // [C_COMBINE (&LAB_14062c030)]
			// packets[142] = [C_LOCATION (LocationPacket)]
			// packets[143] = [C_MOVEBACK (MoveBackPacket)]
			// packets[144] = [C_RECONNECT (ReconnectPacket)]
			packets[145] = new PacketInfo<>(CM_QUESTIONNAIRE.class, State.IN_GAME); // [C_POLL_ANSWER (PollAnswer)]
			packets[146] = new PacketInfo<>(CM_REJECT_REVIVE.class, State.IN_GAME); // [C_REJECT_RESURRECT_BY_OTHER (RejectResurrectByOther)]
			packets[147] = new PacketInfo<>(CM_HEADING_UPDATE.class, State.IN_GAME); // [C_SPIN (SpinPacket)]
			packets[148] = new PacketInfo<>(CM_TELEPORT_SELECT.class, State.IN_GAME); // [C_DESTINATION_AIRPORT (DestinationAirport)]
			packets[149] = new PacketInfo<>(CM_L2AUTH_LOGIN_CHECK.class, State.CONNECTED); // [C_L2AUTH_LOGIN (L2AuthLoginPacket)]
			packets[150] = new PacketInfo<>(CM_CHARACTER_LIST.class, State.AUTHED); // [C_CHARACTER_LIST (&LAB_14062dce0)]
			packets[151] = new PacketInfo<>(CM_CREATE_CHARACTER.class, State.AUTHED); // [C_CREATE_CHARACTER (CreateCharacterPacket)]
			packets[152] = new PacketInfo<>(CM_DELETE_CHARACTER.class, State.AUTHED); // [C_DELETE_CHARACTER (DeleteCharacterPacket)]
			packets[153] = new PacketInfo<>(CM_RESTORE_CHARACTER.class, State.AUTHED); // [C_RESTORE_CHARACTER (RestoreCharacterPacket)]
			packets[154] = new PacketInfo<>(CM_START_LOOT.class, State.IN_GAME); // [C_LOOT (LootPacket)]
			packets[155] = new PacketInfo<>(CM_LOOT_ITEM.class, State.IN_GAME); // [C_LOOT_ITEM (&LAB_140630080)]
			packets[156] = new PacketInfo<>(CM_MOVE_ITEM.class, State.IN_GAME); // [C_MOVE_ITEM_TO_ANOTHER_SLOT (&LAB_140630410)]
			packets[157] = new PacketInfo<>(CM_SPLIT_ITEM.class, State.IN_GAME); // [C_MOVE_STACKABLE_ITEM (MoveStackableItemPacket)]
			packets[158] = new PacketInfo<>(CM_SHOW_BLOCKLIST.class, State.IN_GAME); // [C_RECIPE_LIST (RecipeListPacket)]
			packets[159] = new PacketInfo<>(CM_PLAYER_SEARCH.class, State.IN_GAME); // [C_SEARCH_USERS (SearchUserPacket)]
			packets[160] = new PacketInfo<>(CM_LEGION_UPLOAD_INFO.class, State.IN_GAME); // [C_UPLOAD_GUILD_EMBLEM_IMG_BEGIN (UploadGuildEmblemImgBegin)]
			packets[161] = new PacketInfo<>(CM_LEGION_UPLOAD_EMBLEM.class, State.IN_GAME); // [C_UPLOAD_GUILD_EMBLEM_IMG_DATA (&LAB_1406317e0)]
			packets[162] = new PacketInfo<>(CM_READ_EXPRESS_MAIL.class, State.IN_GAME); // [C_MAIL_POSTMAN (&LAB_14062af30)]
			packets[163] = new PacketInfo<>(CM_SUBZONE_CHANGE.class, State.IN_GAME); // [C_ALL_FOG_CLEARED (AllFogClearedPacket)]
			packets[164] = new PacketInfo<>(CM_QUEST_SHARE.class, State.IN_GAME); // [C_SHARE_QUEST (ShareQuestPacket)]
			// packets[165] = [C_ADD_BUDDY_ANS (&LAB_140633c60)]
			packets[166] = new PacketInfo<>(CM_BLOCK_ADD.class, State.IN_GAME); // [C_ADD_BLOCK (AddBlockPacket)]
			packets[167] = new PacketInfo<>(CM_BLOCK_DEL.class, State.IN_GAME); // [C_REMOVE_BLOCK (RemoveBlockPacket)]
			// packets[168] = [C_QUERY_BLOCK (QueryBlockPacket)]
			// packets[169] = [C_CHANGE_BLOCK_NAME (ChangeBlockNamePacket)]
			packets[170] = new PacketInfo<>(CM_FRIEND_STATUS.class, State.IN_GAME); // [C_CUR_STATUS (CurrentStatusPacket)]
			// packets[171] = new PacketInfo<>(CM_VIRTUAL_AUTH.class, State.AUTHED, State.IN_GAME); // [C_VIRTUAL_AUTH (VirtualAuthPacket)]
			packets[172] = new PacketInfo<>(CM_CHANGE_CHANNEL.class, State.IN_GAME); // [C_CHANGE_CHANNEL (ChangeChannelPacket)]
			// packets[173] = [C_FOLLOW_CHANNEL (FollowChannelPacket)]
			packets[174] = new PacketInfo<>(CM_CHAT_AUTH.class, State.IN_GAME); // [C_SIGN_CLIENT (SignClientPacket)]
			packets[175] = new PacketInfo<>(CM_MACRO_CREATE.class, State.IN_GAME); // [C_SAVE_MACRO (SaveMacroPacket)]
			packets[176] = new PacketInfo<>(CM_MACRO_DELETE.class, State.IN_GAME); // [C_DELETE_MACRO (DeleteMacroPacket)]
			packets[177] = new PacketInfo<>(CM_CHECK_NICKNAME.class, State.AUTHED); // [C_CHECK_EXIST (CheckExistPacket)]
			packets[178] = new PacketInfo<>(CM_REPLACE_ITEM.class, State.IN_GAME); // [C_SWAP_ITEM_SLOT (SwapItemSlotPacket)]
			packets[179] = new PacketInfo<>(CM_BLOCK_SET_REASON.class, State.IN_GAME); // [C_CHANGE_BLOCK_MEMO (&LAB_140636e00)]
			packets[180] = new PacketInfo<>(CM_DEBUG_COMMAND.class, State.IN_GAME); // [C_DEBUG_COMMAND (DebugCommandPacket)]
			packets[181] = new PacketInfo<>(CM_SHOW_BRAND.class, State.IN_GAME); // [C_TACTICS_SIGN (&LAB_1406375b0)]
			// packets[182] = new PacketInfo<>(CM_GM_COMMAND_ACTION.class, State.IN_GAME); // [C_SPECTATOR_MODE (SpectatorModePacket)]
			packets[183] = new PacketInfo<>(CM_RECONNECT_AUTH.class, State.AUTHED); // [C_RECONNECT_AUTH (&LAB_1406384c0)]
			packets[184] = new PacketInfo<>(CM_GROUP_LOOT.class, State.IN_GAME); // [C_GROUP_ITEM_DIST (&LAB_140638740)]
			packets[185] = new PacketInfo<>(CM_DISTRIBUTION_SETTINGS.class, State.IN_GAME); // [C_GROUP_CHANGE_LOOTDIST (GroupChangeLootDistPacket)]
			packets[186] = new PacketInfo<>(CM_MAY_LOGIN_INTO_GAME.class, State.AUTHED); // [C_SA_ACCOUNT_ITEM_QUERY (SAAccountItemQueryPacket)]
			// packets[187] = [C_SA_ACCOUNT_ITEM_ACK (SAAccountItemAckPacket)]
			packets[188] = new PacketInfo<>(CM_ABYSS_RANKING_PLAYERS.class, State.IN_GAME); // [C_REQUEST_ABYSS_RANKER_INFO (RequestAbyssRankerInfoPacket)]
			packets[189] = new PacketInfo<>(CM_MAC_ADDRESS.class, State.CONNECTED); // [C_ROUTE_INFO (RouteInfoPacket)]
			// packets[190] = // [C_CHECK_MESSAGE (&LAB_140639ed0)] sent when receiving S_CHECK_MESSAGE (opcode 80), contains 16 (static) bytes which change every 5s, maybe some kind of consistency check
			packets[191] = new PacketInfo<>(CM_REPORT_PLAYER.class, State.IN_GAME); // [C_ACCUSE_CHARACTER (AccuseCharacterPacket)]
			packets[192] = new PacketInfo<>(CM_INSTANCE_INFO.class, State.IN_GAME); // [C_INSTANCE_DUNGEON_COOLTIMES (InstanceDungeonCooltimePacket)]
			// packets[193] = // [C_SHOP_REQUEST]
			packets[194] = new PacketInfo<>(CM_SHOW_RESTRICTIONS.class, State.IN_GAME); // [C_ASK_BOT_POINT (&LAB_14063b3e0)] when writing /restriction in chat
			// packets[195] = new PacketInfo<>(CM_SUMMON_TELEPORT_RESPONSE.class, State.IN_GAME); // [C_RECALLED_BY_OTHER_ANSWER (&LAB_14063b5b0)] when player accepts/declines SM_SUMMON_TELEPORT_REQUEST window
			packets[196] = new PacketInfo<>(CM_SHOW_MAP.class, State.IN_GAME); // [C_REQUEST_SERIAL_KILLER_LIST (RequestSerialKillerListPacket)]
			packets[197] = new PacketInfo<>(CM_APPEARANCE.class, State.IN_GAME); // [C_ADDED_SERVICE_REQUEST (&LAB_14063ba60)]
			// packets[198] = [C_SNDC_CHECK_MESSAGE (SndcCheckMessagePacket)]
			// packets[199] = [C_GGAUTH_CHECK_ANSWER (GGAuthCheckAnswerPacket)]
			packets[200] = new PacketInfo<>(CM_AUTO_GROUP.class, State.IN_GAME); // [C_MATCHMAKER_REQ (MatchMakerReqPacket)]
			packets[201] = new PacketInfo<>(CM_SUMMON_MOVE.class, State.IN_GAME); // [C_CLIENTSIDE_NPC_MOVE (NpcMovePacket)]
			packets[202] = new PacketInfo<>(CM_SUMMON_EMOTION.class, State.IN_GAME); // [C_CLIENTSIDE_NPC_ACTION (NpcActionPacket)]
			packets[203] = new PacketInfo<>(CM_SUMMON_ATTACK.class, State.IN_GAME); // [C_CLIENTSIDE_NPC_ATTACK (NpcAttackPacket)]
			// packets[204] = // [C_CLIENTSIDE_NPC_BLINK (NpcReturnBlinkPacket)] sent when receiving S_CLIENTSIDE_NPC_BLINK (opcode 186)
			packets[205] = new PacketInfo<>(CM_SUMMON_CASTSPELL.class, State.IN_GAME); // [C_CLIENTSIDE_NPC_USE_SKILL (NpcUseSkillPacket)]
			packets[206] = new PacketInfo<>(CM_FUSION_WEAPONS.class, State.IN_GAME); // [C_COMPOUND_2H_WEAPON (&LAB_14063f670)]
			packets[207] = new PacketInfo<>(CM_BREAK_WEAPONS.class, State.IN_GAME); // [C_REMOVE_COMPOUND (RemoveCompoundOfTwoHandWeaponPacket)]
			packets[208] = new PacketInfo<>(CM_COMPOSITE_STONES.class, State.IN_GAME); // [C_COMPOUND_ENCHANT_ITEM (FUN_1406457b0)]
			// packets[209] = new PacketInfo<>(CM_TIME_CHECK_QUIT.class, State.IN_GAME); // [C_ASK_GLOBAL_PLAYTIME_FATIGUE_INFO (AskGlobalPlaytimeFatigueInfoPacket)]
			packets[210] = new PacketInfo<>(CM_CHARACTER_PASSKEY.class, State.AUTHED); // [C_2ND_PASSWORD (SecondPasswordPacket)]
			// packets[211] = [C_UNUSED_2ND_PASSWORD1]
			// packets[212] = [C_UNUSED_2ND_PASSWORD2]
			packets[213] = new PacketInfo<>(CM_CHECK_MAIL_UNK.class, State.IN_GAME); // [C_SA_GOODSLIST (ShopAgent2GoodsList)] TODO
			// packets[214] = [C_SA_CONFIRMGOODS (ShopAgent2ConfirmGoods)]
			// packets[215] = new PacketInfo<>(CM_DIRECT_ENTER_WORLD.class, State.IN_GAME); // [C_REQUEST_DIRECT_ENTER_WORLD (RequestDirectEnterWorldPacket)]
			// packets[216] = new PacketInfo<>(CM_REQUEST_BEGINNER_SERVER.class, State.IN_GAME); // [C_REQUEST_BEGINNER_SERVER (RequestBeginnerServerPacket)]
			// packets[217] = new PacketInfo<>(CM_REQUEST_RETURN_SERVER.class, State.IN_GAME); // [C_REQUEST_RETURN_SERVER (RequestReturnServerPacket)]
			packets[218] = new PacketInfo<>(CM_GET_HOUSE_BIDS.class, State.IN_GAME); // [C_REQUEST_AUCTION_LIST (RequestAuctionList)]
			packets[219] = new PacketInfo<>(CM_REGISTER_HOUSE.class, State.IN_GAME); // [C_REQUEST_AUCTION_REGISTER (&LAB_140642310)]
			// packets[220] = [C_REQUEST_AUCTION_CANCEL]
			packets[221] = new PacketInfo<>(CM_PLACE_BID.class, State.IN_GAME); // [C_REQUEST_AUCTION_BET (RequestAuctionBet)]
			packets[222] = new PacketInfo<>(CM_HOUSE_TELEPORT.class, State.IN_GAME); // [C_REQUEST_HOUSING_TELEPORT (RequestHousingTeleport)]
			packets[223] = new PacketInfo<>(CM_HOUSE_PAY_RENT.class, State.IN_GAME); // [C_REQUEST_HOUSING_CHARGE_FEE (RequestHousingChargeFee)]
			packets[224] = new PacketInfo<>(CM_USE_HOUSE_OBJECT.class, State.IN_GAME); // [C_USE_HOUSING_OBJECT (UseHousingObjectPacket)]
			packets[225] = new PacketInfo<>(CM_RELEASE_OBJECT.class, State.IN_GAME); // [C_CANCEL_USE_HOUSING_OBJECT (CancelUseHousingObjectPacket)]
			packets[226] = new PacketInfo<>(CM_HOUSE_OPEN_DOOR.class, State.IN_GAME); // [C_USE_HOUSING_DOOR (UseHousingDoorPacket)]
			// packets[227] = new PacketInfo<>(CM_IN_GAME_SHOP_INFO.class, State.IN_GAME); // [C_REQUEST_WEBNOTIFY_CLEAR (WebNotifyClearPacket)]
			// packets[228] = [C_HOUSING_REFRESH_TOKEN_REQ (HousingRefreshTokenReqPacket)]
			packets[229] = new PacketInfo<>(CM_GF_WEBSHOP_TOKEN_REQUEST.class, State.IN_GAME); // [C_GF_WEBSHOP_TOKEN_REQ (GFWebshopTokenReqPacket)]
			packets[230] = new PacketInfo<>(CM_SHOW_FRIENDLIST.class, State.IN_GAME); // [C_OFFLINE_BUDDY_LIST (OfflineBuddyList)]
			// packets[231] = [C_ANSWER_OFFLINE_BUDDY_REQUEST (AnswerOfflineBuddy)]
			packets[232] = new PacketInfo<>(CM_CHALLENGE_LIST.class, State.IN_GAME); // [C_CHALLENGE_TASK (&LAB_140645410)]
			packets[233] = new PacketInfo<>(CM_BONUS_TITLE.class, State.IN_GAME); // [C_CHANGE_ATTR_TITLE (ChangeAttrTitlePacket)]
			packets[234] = new PacketInfo<>(CM_USE_CHARGE_SKILL.class, State.IN_GAME); // [C_FIRE_CHARGE_SKILL (FUN_14060ed30)]
			packets[235] = new PacketInfo<>(CM_TUNE.class, State.IN_GAME); // [C_IDENTIFY_ITEM (IdentifyItem)]
			packets[236] = new PacketInfo<>(CM_SELECT_DECOMPOSABLE.class, State.IN_GAME); // [C_SELECT_DISASSEMBLY_ITEM (&LAB_140646480)]
			packets[237] = new PacketInfo<>(CM_MEGAPHONE.class, State.IN_GAME); // [C_MEGAPHONE (MegaphonePacket)]
			packets[238] = new PacketInfo<>(CM_TUNE_RESULT.class, State.IN_GAME); // [C_ANSWER_REIDENTIFY (AnswerReidentifyPacket)]
			packets[239] = new PacketInfo<>(CM_FRIEND_SET_MEMO.class, State.IN_GAME); // [C_CHANGE_BUDDY_MEMO (FUN_140647090)]
			packets[240] = new PacketInfo<>(CM_UNWRAP_ITEM.class, State.IN_GAME); // [C_UNPACK_ITEM (UnpackItemPacket)]
			// packets[241] = [C_REQUEST_NP_LOGIN_GAMESVR]
			// packets[242] = [C_REQUEST_NP_CONSUME_TOKEN]
			// packets[243] = [C_REQUEST_NP_AUTH_TOKEN]
			packets[244] = new PacketInfo<>(CM_BIND_POINT_TELEPORT.class, State.IN_GAME);// [C_HOTSPOT]
			// packets[245] = [C_ACCUSE_CHAT_SPAMMER]
			packets[246] = new PacketInfo<>(CM_UPGRADE_ARCADE.class, State.IN_GAME); // [C_GOTCHA_REQUEST]
			packets[247] = new PacketInfo<>(CM_ITEM_PURIFICATION.class, State.IN_GAME); // [C_ITEM_UPGRADE]
			packets[248] = new PacketInfo<>(CM_ATREIAN_PASSPORT.class, State.IN_GAME); // [C_REQ_LOGIN_EVENT_REWARD]
			// packets[249] = [C_REQ_REGISTER_MONEY_TRADE]
		} catch (NoSuchMethodException e) { // should never happen
			throw new ExceptionInInitializerError(e);
		}
	}

	public static AionClientPacket tryCreatePacket(ByteBuffer data, AionConnection client) {
		State state = client.getState();
		int opcode = Crypt.decodeClientPacketOpcode(data.getShort() & 0xffff);
		data.position(data.position() + 3); // skip static code (short) and secondary opcode (byte)
		PacketInfo<? extends AionClientPacket> packetInfo = opcode < 0 || opcode >= packets.length ? null : packets[opcode];
		if (packetInfo == null) {
			client.sendUnknownClientPacketInfo(opcode);
			if (NetworkConfig.LOG_UNKNOWN_PACKETS)
				log.warn(String.format("Aion client sent data with unknown opcode: 0x%03X, state=%s %n%s", opcode, state.toString(), NetworkUtils.toHex(data)));
			return null;
		}
		if (!packetInfo.isValid(state)) {
			if (NetworkConfig.LOG_IGNORED_PACKETS)
				log.warn(client + " sent " + packetInfo.getPacketClassName() + " but the connections current state (" + state
					+ ") is invalid for this packet. Packet won't be instantiated.");
			return null;
		}
		return packetInfo.newPacket(opcode, data, client);
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
