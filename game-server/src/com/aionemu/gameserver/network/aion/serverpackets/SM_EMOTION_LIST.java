package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.emotion.Emotion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author SVDNESS
 */

public class SM_EMOTION_LIST extends AionServerPacket {
	byte action;
	private final Collection<Emotion> emotions;

	public SM_EMOTION_LIST(byte action, Collection<Emotion> emotions) {
		this.action = action;
		this.emotions = emotions == null ? List.of() : emotions;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
		writeH(emotions.size());
		for (Emotion emotion : emotions) {
			writeH(emotion.id());
			writeD(emotion.secondsUntilExpiration());
		}
	}
}