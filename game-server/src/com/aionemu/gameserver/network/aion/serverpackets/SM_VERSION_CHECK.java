package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.EventType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.loginserver.LoginServer;

/**
 * @author -Nemesiss- CC fix
 * @modified by Novo, cura, Neon
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
	private EventType cityDecoration;

	public SM_VERSION_CHECK(EventType cityDecoration) {
		this(INTERNAL_VERSION, cityDecoration);
	}

	public SM_VERSION_CHECK(int version, EventType cityDecoration) {
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
			writeC(0x01); // Send wrong client version
			return;
		}
		writeC(0x00); // version ok
		writeC(NetworkConfig.GAMESERVER_ID);
		writeD(150602); // start year month day
		writeD(150326); // start year month day
		writeD(0x00); // spacing
		writeD(150317); // year month day
		writeD(GameServer.START_TIME_SECONDS); // start server time in seconds
		writeC(0x00); // unk
		writeC(GSConfig.SERVER_COUNTRY_CODE); // country code;
		writeC(0x00); // unk
		writeC((characterLimitCount * LoginServer.getInstance().getGameServerCount() * 0x10) | (limitFactionMode * 4) | GSConfig.CHARACTER_CREATION_MODE);
		writeD((int) (System.currentTimeMillis() / 1000)); // current UTC time in seconds
		writeH(350); // unk
		writeC(1); // unk (always 1)
		writeC(10); // time or level restriction (now 5 on official)
		writeC(1); // time or level restriction (now 15 on official)
		writeC(10); // time or level restriction
		writeC(GSConfig.CHAT_SERVER_MIN_LEVEL); // min level to write in channel chats
		writeC(20); // time or level restriction (now 1 on official)
		writeC(20); // level restriction (before 30, now 66 on official)
		writeC(1); // unk (always 1)
		writeH(2); // unk (always 2)
		writeC(GSConfig.CHARACTER_REENTRY_TIME);
		writeC(cityDecoration.getId());
		writeD(0); // unk
		writeD(-(GSConfig.TIME_ZONE.getRawOffset() / 1000)); // server time zone offset relative to UTC in seconds
		writeC(0x04); // unk
		writeC(120); // unk
		writeH(25233); // unk
		writeC(2); // 4.0
		writeC(1); // unk
		writeD(0); // 4.0
		writeD(0); // 4.5
		writeH(3000); // 4.5
		writeH(1); // 4.5
		writeC(0); // 4.7
		writeC(1); // 4.7
		writeD(-(GSConfig.TIME_ZONE.getDSTSavings() / 1000)); // servers current daylight saving time offset in seconds
		writeC(1); // 4.8
		writeC(1); // 1 = activate stonespear siege
		writeC(0); // 1 = master Server
		writeC(0);
		writeH(0);
		writeD(0); // 4.8
		writeD(0); // 4.8
		writeD(0); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeD(1000); // 4.8
		writeC(0); // 4.8
		writeC(0); // 4.8
		writeC(0); // 4.8
		writeC(64); // 4.8
		writeC(64); // 4.8
		writeH(ChatServer.getInstance().getPublicIP().length > 0 ? 1 : 0);
		if (ChatServer.getInstance().getPublicIP().length > 0) {
			writeC(0); // spacer or maybe id
			writeB(ChatServer.getInstance().getPublicIP());
			writeH(ChatServer.getInstance().getPublicPort());
		}
	}
}
