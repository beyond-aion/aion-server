package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.guide.Guide;

/**
 * @author xTz
 */
public abstract class GuideDAO implements IDFactoryAwareDAO {

	@Override
	public final String getClassName() {
		return GuideDAO.class.getName();
	}

	public abstract boolean deleteGuide(int guide_id);

	public abstract List<Guide> loadGuides(int playerId);

	public abstract Guide loadGuide(int player_id, int guide_id);

	public abstract void saveGuide(int guide_id, Player player, String title);
}
