package com.aionemu.gameserver.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.model.gameobjects.player.RecipeList;

/**
 * @author lord_rex, SVDNESS
 */
public class PlayerRecipesDAO {
	private static final String SELECT_QUERY = "SELECT `recipe_id`, `production_count` FROM player_recipes WHERE `player_id`=?";
	private static final String ADD_QUERY = "INSERT INTO player_recipes (`player_id`, `recipe_id`, `production_count`) VALUES (?, ?, ?)";
	private static final String UPDATE_QUERY = "UPDATE player_recipes SET `production_count`=? WHERE `player_id`=? AND `recipe_id`=?";
	private static final String DELETE_QUERY = "DELETE FROM player_recipes WHERE `player_id`=? AND `recipe_id`=?";

	public static RecipeList load(final int playerId) {
		final HashMap<Integer, Integer> recipeList = new HashMap<>();
		DB.select(SELECT_QUERY, new ParamReadStH() {
			@Override
			public void setParams(PreparedStatement ps) throws SQLException {
				ps.setInt(1, playerId);
			}

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					recipeList.put(rs.getInt("recipe_id"), rs.getInt("production_count"));
				}
			}
		});
		return new RecipeList(recipeList);
	}

	public static boolean addRecipe(final int playerId, final int recipeId, final int productionCount) {
		return DB.insertUpdate(ADD_QUERY, ps -> {
			ps.setInt(1, playerId);
			ps.setInt(2, recipeId);
			ps.setInt(3, productionCount);
			ps.execute();
		});
	}

	public static boolean updateProductionCount(final int playerId, final int recipeId, final int productionCount) {
		return DB.insertUpdate(UPDATE_QUERY, ps -> {
			ps.setInt(1, productionCount);
			ps.setInt(2, playerId);
			ps.setInt(3, recipeId);
			ps.execute();
		});
	}

	public static boolean delRecipe(final int playerId, final int recipeId) {
		return DB.insertUpdate(DELETE_QUERY, ps -> {
			ps.setInt(1, playerId);
			ps.setInt(2, recipeId);
			ps.execute();
		});
	}
}