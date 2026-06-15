package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.EventTheme;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.AtreianPassportService;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author -Nemesiss-, Novo, cura, Neon
 */
public class SM_VERSION_CHECK extends AionServerPacket {

	/**
	 * Version number used for client version validation<br>
	 * aion 3.0.0.0 = 194<br>
	 * aion 3.1.0.0 = 195<br>
	 * aion 3.5.0.0 = 196<br>
	 * aion 4.0.0.0 = 201<br>
	 * aion 4.5.0.0 = 203<br>
	 * aion 4.5.0.15 = 204<br>
	 * aion 4.7.0.5 = 205<br>
	 * aion 4.7.5.0 = 206<br>
	 * aion 4.8.0.0 = 207
	 */
	public static final int INTERNAL_VERSION = 207;

	/**
	 * Aion Client version
	 */
	private int version;
	/**
	 * City theme (for Pandemonium & Sanctum)
	 */
	private EventTheme cityDecoration;

	public SM_VERSION_CHECK(EventTheme cityDecoration) {
		this(INTERNAL_VERSION, cityDecoration);
	}

	public SM_VERSION_CHECK(int version, EventTheme cityDecoration) {
		this.version = version;
		this.cityDecoration = cityDecoration;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		int characterLimitCount = GSConfig.CHARACTER_LIMIT_COUNT;
		int limitFactionMode = GSConfig.CHARACTER_FACTION_LIMITATION_MODE;

		if (MembershipConfig.CHARACTER_ADDITIONAL_COUNT > characterLimitCount && MembershipConfig.CHARACTER_ADDITIONAL_ENABLE != 10)
			characterLimitCount = MembershipConfig.CHARACTER_ADDITIONAL_COUNT;

		if (GSConfig.ENABLE_RATIO_LIMITATION) {
			if (GameServer.getRatiosFor(Race.ELYOS) > GSConfig.RATIO_MIN_VALUE)
				limitFactionMode = 1;
			else if (GameServer.getRatiosFor(Race.ASMODIANS) > GSConfig.RATIO_MIN_VALUE)
				limitFactionMode = 2;
			else if (GameServer.getCountFor(Race.ELYOS) + GameServer.getCountFor(Race.ASMODIANS) > GSConfig.RATIO_HIGH_PLAYER_COUNT_DISABLING)
				limitFactionMode = 3;
		}

		if (version != INTERNAL_VERSION) {
			writeC(1); // answerID
			// 0 - ok (no message)
			// 1 - The client version is not compatible with the game server.
			// 2 - The NPC script version is not compatible with the game server.
			// 3, 4, ... - An unknown error has occurred while checking the game server version.
			return;
		}
		writeC(0); // answerID
		writeC(NetworkConfig.GAMESERVER_ID); // serverId
		writeD(150602); // GSServBuildDate (year month day)
		writeD(150326); // DBServBuildDate (year month day)
		writeD(0x00); // 0
		writeD(150317); // NPCServBuildDate (year month day)
		writeD(GameServer.START_TIME_SECONDS); // start server time in seconds
		writeC(0x00); // 0
		writeC(GSConfig.SERVER_COUNTRY_CODE); // country code
		writeC(0x00); // 0
		writeC((characterLimitCount * LoginServer.getInstance().getGameServerCount() * 0x10) | (limitFactionMode * 4) | GSConfig.CHARACTER_CREATION_MODE); // ServerFlag
		writeD((int) (System.currentTimeMillis() / 1000)); // PacketGenTimeOnServ (current UTC time in seconds)
		writeH(GSConfig.MIN_SKILL_CAST_INTERVAL_MILLIS); // skillPacketDelay
		writeC(1); // enableClientPet (always 1)
		writeC(10); // minSendMailLevel (now 5 on official)
		writeC(1); // minReceiveWhisperLevel (now 15 on official)
		writeC(10); // minReceiveMailLevel
		writeC(GSConfig.CHAT_SERVER_MIN_LEVEL); // ChannelChatLevel (min level to write in channel chats)
		writeC(20); // Trial_ChannelChatLevel (now 1 on official)
		writeC(20); // Trial_Channelchatwritelevel1 (before 30, now 66 on official)
		writeC(1); // Trial_Channelchatwritelevel2 (always 1)
		writeH(2); // MatchingCoolTimeSEC (always 2)
		writeC(GSConfig.CHARACTER_REENTRY_TIME);
		writeD(cityDecoration.getId()); // SceneStatus
		writeC(0); // fatigueKoreaUse
		writeD(-ServerTime.getStandardOffset()); // server time zone offset relative to UTC in seconds (excluding daylight savings)
		writeC(0x04); // MaxHousingChargePerid
		writeD(40014200); // spawn_version (4.0)
		writeC(1); // DisposableItemTrade
		writeD(0); // EnableNeutralChat (4.0)
		writeD(0); // updateServerAddr (4.5)
		writeH(3000); // updateServerPort (4.5)
		writeH(1); // updateServerVersionCheckType (4.5)
		writeC(0); // ReduceSellPriceforGold (4.7)
		writeC(1); // RestrictWareandChargebyRank (4.7)
		writeD(-ServerTime.getDaylightSavings()); // TimeDstBias (servers current daylight saving time offset in seconds)
		writeC(1); // SetDefaultAnimLength (4.7)
		writeC(1); // 1 = activate stonespear siege (4.8)
		writeD(0); // 1 = activate master server (4.8)
		writeC(0); // unk/not used? (4.8)
		writeC(AtreianPassportService.getInstance().isAtreianPassportDisabled() ? 1 : 0); // remove Atreian Passport menu entry 0/1 (4.8)
		writeC(0); // Newbie or comeback icons/etc disable/enable? 0/1 (4.8)
		writeC(0); // Disable evolution? 0/1 (4.8)
		writeC(0); // Item destroy on Enchant? 0/1 (4.8)
		writeC(0); // Disable some item wear level check? 0/1 (4.8)
		writeC(0); // Item related enable/disable? 0/1 (4.8)
		writeC(0); // Special page(rank points?) enable/disable? / CreateAllPlayerClass? 0/1 (4.8) // TODO test
		writeD(GSConfig.ITEM_WRAP_LIMIT); // incFreeTradePackCount (4.8)
		writeD(1000); // decDropProb (rate modifier, divide by 1000) not used? (4.8)
		writeD(1000); // decUserSellItemPrice (rate modifier, divide by 1000) (4.8)
		writeD(1000); // decUserSellAPPrice (rate modifier, divide by 1000) (4.8)
		writeD(1000); // incNpcSellItemMedalPrice (rate modifier, divide by 1000) (4.8)
		writeD(1000); // incNpcSellItemCoinPrice (rate modifier, divide by 1000) (4.8)
		writeD(1000); // incNpcSellItemAPPrice (rate modifier, divide by 1000) (4.8)
		writeD(1000); // incNpcSellItemQinaPrice (rate modifier, divide by 1000) (4.8)
		writeD(1000); // incNpcSellItemAPQinaPrice (rate modifier, divide by 1000) (4.8)
		writeD(1000); // incItemUpgradeItemCnt (rate modifier, divide by 1000) (4.8)
		writeD(1000); // incItemUpgradeAP (rate modifier, divide by 1000) (4.8)
		writeD(1000); // incItemUpgradeQina (rate modifier, divide by 1000) (4.8)
		writeC(0); // Augment disable or limit? 0/1 (4.8)
		writeF(3.0f); // exp rate modifier? (4.8)
		writeH(ChatServer.getInstance().getPublicIP().length > 0 ? 1 : 0); // ChatServersCount
		if (ChatServer.getInstance().getPublicIP().length > 0) {
			writeC(0); // spacer or maybe id
			writeB(ChatServer.getInstance().getPublicIP());
			writeH(ChatServer.getInstance().getPublicPort());
		}
	}
}
