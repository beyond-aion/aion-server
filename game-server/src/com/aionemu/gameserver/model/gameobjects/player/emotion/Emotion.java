package com.aionemu.gameserver.model.gameobjects.player.emotion;

import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */

public class Emotion implements Expirable {
	private final int id;
	private final int expireTime;

	public Emotion(int id, int expireTime) {
		this.id = id;
		this.expireTime = expireTime;
	}

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
	}
}
