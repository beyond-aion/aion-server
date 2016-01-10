package com.aionemu.gameserver.model.gameobjects.player;

import java.util.HashSet;
import java.util.Set;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerRecipesDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECIPE_DELETE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 */
public class RecipeList {

	private Set<Integer> recipeList = new HashSet<Integer>();

	public RecipeList(HashSet<Integer> recipeList) {
		this.recipeList = recipeList;
	}

	public RecipeList() {
	}

	public Set<Integer> getRecipeList() {
		return recipeList;
	}

	public boolean addRecipe(int playerId, int recipeId) {
		if (!isRecipePresent(recipeId) && DAOManager.getDAO(PlayerRecipesDAO.class).addRecipe(playerId, recipeId)) {
			recipeList.add(recipeId);
			return true;
		}
		return false;
	}

	public void deleteRecipe(Player player, int recipeId) {
		if (recipeList.contains(recipeId)) {
			if (DAOManager.getDAO(PlayerRecipesDAO.class).delRecipe(player.getObjectId(), recipeId)) {
				recipeList.remove(recipeId);
				PacketSendUtility.sendPacket(player, new SM_RECIPE_DELETE(recipeId));
			}
		}
	}

	public void autoLearnRecipe(Player player, int skillId, int skillLvl) {
		for (RecipeTemplate recipe : DataManager.RECIPE_DATA.getAutolearnRecipes(player.getRace(), skillId, skillLvl))
			addRecipe(player.getObjectId(), recipe.getId());
	}

	public boolean isRecipePresent(int recipeId) {
		return recipeList.contains(recipeId);
	}

	public int size() {
		return this.recipeList.size();
	}
}
