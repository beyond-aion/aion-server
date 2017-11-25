package com.aionemu.gameserver.model.gameobjects.player.emotion;

import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 */
public class Emotion implements Expirable {

	private int id;
	private int expireTime;

	/**
	 * @param id
	 * @param expireTime
	 */
	public Emotion(int id, int expireTime) {
		this.id = id;
		this.expireTime = expireTime;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	@Override
	public int getExpireTime() {
		return expireTime;
	}

	@Override
	public void onExpire(Player player) {
		player.getEmotions().remove(id);
		// TODO emotion templates -> parse nameIds for system message, like 600228 for STR_EMOTION_CASH_DISCODANCE (Aion Boogie) etc.
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DELETE_CASH_SOCIALACTION_BY_TIMEOUT(/* nameId */));
	}

}
