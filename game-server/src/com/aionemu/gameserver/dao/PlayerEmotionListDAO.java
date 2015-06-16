package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.emotion.Emotion;

/**
 * @author Mr. Poke
 */
public abstract class PlayerEmotionListDAO implements DAO {

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.commons.database.dao.DAO#getClassName()
	 */
	@Override
	public String getClassName() {
		return PlayerEmotionListDAO.class.getName();
	}

	/**
	 * @param player
	 */
	public abstract void loadEmotions(Player player);

	/**
	 * @param player
	 */
	public abstract void insertEmotion(Player player, Emotion emotion);
	
	
	public abstract void deleteEmotion(int playerId, int emotionId);
}
