package com.aionemu.gameserver.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.RecipeList;

/**
 * @author lord_rex, SVDNESS
 */
public class PlayerRecipesDAO {

	private static final String SELECT_QUERY = "SELECT `recipe_id`, `production_limit` FROM player_recipes WHERE `player_id`=?";
	private static final String ADD_QUERY = "INSERT INTO player_recipes (`player_id`, `recipe_id`, `production_limit`) VALUES (?, ?, ?)";
	private static final String UPDATE_QUERY = "UPDATE player_recipes SET `production_limit`=? WHERE `player_id`=? AND `recipe_id`=?";
	private static final String DELETE_QUERY = "DELETE FROM player_recipes WHERE `player_id`=? AND `recipe_id`=?";

	public static RecipeList load(int playerId) {
		RecipeList recipeList = new RecipeList();
		DB.select(SELECT_QUERY, new ParamReadStH() {
			@Override
			public void setParams(PreparedStatement ps) throws SQLException {
				ps.setInt(1, playerId);
			}

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					int recipeId = rs.getInt("recipe_id");
					int productionLimit = rs.getInt("production_limit");
					if (productionLimit == 0) // recipes stored before production counts were tracked have a count of 0, they start with the full count
						productionLimit = DataManager.RECIPE_DATA.getMaxProductionCount(recipeId);
					recipeList.addRecipe(recipeId, productionLimit);
				}
			}
		});
		return recipeList;
	}

	public static boolean addRecipe(int playerId, int recipeId, int productionLimit) {
		return DB.insertUpdate(ADD_QUERY, ps -> {
			ps.setInt(1, playerId);
			ps.setInt(2, recipeId);
			ps.setInt(3, productionLimit);
			ps.execute();
		});
	}

	public static boolean updateProductionLimit(int playerId, int recipeId, int productionLimit) {
		return DB.insertUpdate(UPDATE_QUERY, ps -> {
			ps.setInt(1, productionLimit);
			ps.setInt(2, playerId);
			ps.setInt(3, recipeId);
			ps.execute();
		});
	}

	public static boolean delRecipe(int playerId, int recipeId) {
		return DB.insertUpdate(DELETE_QUERY, ps -> {
			ps.setInt(1, playerId);
			ps.setInt(2, recipeId);
			ps.execute();
		});
	}
}
