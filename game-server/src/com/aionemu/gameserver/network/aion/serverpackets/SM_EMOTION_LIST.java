package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.model.gameobjects.player.emotion.Emotion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

public class SM_EMOTION_LIST extends AionServerPacket {

	byte action;
	Collection<Emotion> emotions;

	/**
	 * @param action
	 */
	public SM_EMOTION_LIST(byte action, Collection<Emotion> emotions) {
		this.action = action;
		this.emotions = emotions;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
		if (con.getActivePlayer().hasPermission(MembershipConfig.EMOTIONS_ALL)) {
			writeH(86);
			for (int i = 0; i < 86; i++) {
				writeD(64 + i);
				writeH(0x00);
			}
		} else {
			writeH(emotions.size());
			for (Emotion emotion : emotions) {
				writeD(emotion.getId());
				writeH(emotion.secondsUntilExpiration());
			}
		}
	}
}
