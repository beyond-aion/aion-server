package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.emotion.Emotion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

public class SM_EMOTION_LIST extends AionServerPacket {

	public enum Action {
		/** Replaces the whole list. */
		LIST(0),
		/** Adds the emotions, the client prints the learn message for each one. */
		ADD(1),
		/** Removes the emotions, the client prints the expiry message for each one. */
		REMOVE(2);

		private final int id;

		Action(int id) {
			this.id = id;
		}
	}

	private final Action action;
	private final Collection<Emotion> emotions;

	public SM_EMOTION_LIST(Action action, Collection<Emotion> emotions) {
		this.action = action;
		this.emotions = emotions == null ? List.of() : emotions;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action.id);
		writeH(emotions.size());
		for (Emotion emotion : emotions) {
			writeH(emotion.getId());
			writeD(action == Action.REMOVE ? 0 : emotion.secondsUntilExpiration());
		}
	}
}
