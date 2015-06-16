package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.RecipeList;

/**
 * @author lord_rex
 */
public abstract class PlayerRecipesDAO implements DAO {

	@Override
	public String getClassName() {
		return PlayerRecipesDAO.class.getName();
	}

	public abstract RecipeList load(final int playerId);

	public abstract boolean addRecipe(final int playerId, final int recipeId);

	public abstract boolean delRecipe(final int playerId, final int recipeId);
}
