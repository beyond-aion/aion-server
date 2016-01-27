package com.aionemu.gameserver.network.aion.serverpackets;

import java.time.LocalDateTime;
import java.util.TimeZone;

import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.NetworkController;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.ChatService;
import com.aionemu.gameserver.services.EventService;

/**
 * @author -Nemesiss- CC fix
 * @modified by Novo, cura, Neon
 */
public class SM_VERSION_CHECK extends AionServerPacket {

	/**
	 * Aion Client version
	 */
	private int version;

	public SM_VERSION_CHECK(int version) {
		this.version = version;
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

		// aion 3.0.0.0 = 194
		// aion 3.1.0.0 = 195
		// aion 3.5.0.0 = 196
		// aion 4.0.0.0 = 201
		// aion 4.5.0.0 = 203
		// aion 4.5.0.15 = 204
		// aion 4.7.0.5 = 205
		// aion 4.7.5.0 = 206
		// aion 4.8.0.0 = 207
		if (version != 207) {
			writeC(0x01); // Send wrong client version
			return;
		}
		writeC(0x00); // version ok
		writeC(NetworkConfig.GAMESERVER_ID);
		writeD(150602);// start year month day
		writeD(150326);// start year month day
		writeD(0x00);// spacing
		writeD(150317);// year month day
		writeD(GameServer.START_TIME_SECONDS);// start server time in seconds
		writeC(0x00);// unk
		writeC(GSConfig.SERVER_COUNTRY_CODE);// country code;
		writeC(0x00);// unk
		writeC((characterLimitCount * NetworkController.getInstance().getServerCount() * 0x10) | (limitFactionMode * 4) | GSConfig.CHARACTER_CREATION_MODE);
		writeD((int) LocalDateTime.now().atZone(TimeZone.getTimeZone(GSConfig.TIME_ZONE_ID).toZoneId()).toEpochSecond()); // server time
		writeH(350);// unk
		writeH(2561);// unk
		writeH(2561);// unk
		writeC(GSConfig.CHAT_SERVER_MIN_LEVEL); // min level to write in channel chats
		writeC(20); // some other restriction
		writeH(276);// unk
		writeH(2); // unk
		writeC(GSConfig.CHARACTER_REENTRY_TIME);
		writeC(EventService.getInstance().getEventType().getId()); // city decoration
		writeC(0);// unk
		writeC(0);// unk
		writeD(0 * 65536); // negative server time offset (timeInSeconds * 2^16, accepts only positive numbers)
		writeC(0x00);// unk (server time related)
		writeC(0x00);// unk (server time related)
		writeC(0x04);// unk
		writeC(120);// unk
		writeH(25233);// unk
		writeC(2);// 4.0
		writeC(0x01);// unk
		writeD(0);// 4.0
		writeD(0);// 4.5
		writeD(68536);// 4.5
		writeC(0);// 4.7
		writeC(1);// 4.7
		writeC(0);//4.8
		writeC(0);//4.8
		writeC(0);//4.8
		writeC(0);//4.8
		writeC(1);//4.8
		writeC(1);//4.8 Legion Siege Bases 1 = enabled; 0 = disabled
		writeD(0);//4.8 maybe master server is here now?
		writeD(0);//4.8
		writeD(0);//4.8
		writeD(0);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeD(1000);//4.8
		writeC(0);//4.8
		writeC(0);//4.8
		writeC(0);//4.8
		writeC(0x40);//4.8
		writeC(0x40);//4.8
		writeH(0x01);// its loop size
		// for... chat servers?
		{
			writeC(0x00);// spacer
			// if the correct ip is not sent it will not work
			writeB(ChatService.getIp());
			writeH(ChatService.getPort());
		}
	}
}
