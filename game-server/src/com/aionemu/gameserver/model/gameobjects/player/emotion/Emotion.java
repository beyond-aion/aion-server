package com.aionemu.gameserver.model.gameobjects.player.emotion;

import com.aionemu.gameserver.model.IExpirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;


/**
 * @author MrPoke
 *
 */
public class Emotion implements IExpirable{
	private int id;
	private int dispearTime;

	/**
	 * @param id
	 * @param dispearTime
	 */
	public Emotion(int id, int dispearTime) {
		this.id = id;
		this.dispearTime = dispearTime;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public int getRemainingTime(){
		if (dispearTime == 0)
			return 0;
		return dispearTime-(int)(System.currentTimeMillis()/1000);
	}

	@Override
	public int getExpireTime() {
		return dispearTime;
	}

	@Override
	public void expireEnd(Player player) {
		player.getEmotions().remove(id);
		
	}

	@Override
	public void expireMessage(Player player, int time) {
	}

	@Override
	public boolean canExpireNow() {
		return true;
	}
}
