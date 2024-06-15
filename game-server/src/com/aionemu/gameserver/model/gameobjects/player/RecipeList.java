package com.aionemu.gameserver.model.gameobjects.player;

import java.util.HashSet;
import java.util.Set;

import com.aionemu.gameserver.dao.PlayerRecipesDAO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEARN_RECIPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECIPE_DELETE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, Neon
 */
public class RecipeList {

	private Set<Integer> recipeList = new HashSet<>();

	public RecipeList(HashSet<Integer> recipeList) {
		this.recipeList = recipeList;
	}

	public RecipeList() {
	}

	public Set<Integer> getRecipeList() {
		return recipeList;
	}

	public boolean addRecipe(Player player, int recipeId) {
		if (!isRecipePresent(recipeId) && PlayerRecipesDAO.addRecipe(player.getObjectId(), recipeId)) {
			recipeList.add(recipeId);
			PacketSendUtility.sendPacket(player, new SM_LEARN_RECIPE(recipeId));
			return true;
		}
		return false;
	}

	public boolean deleteRecipe(Player player, int recipeId) {
		if (recipeList.contains(recipeId) && PlayerRecipesDAO.delRecipe(player.getObjectId(), recipeId)) {
			recipeList.remove(recipeId);
			PacketSendUtility.sendPacket(player, new SM_RECIPE_DELETE(recipeId));
			return true;
		}
		return false;
	}

	public boolean isRecipePresent(int recipeId) {
		return recipeList.contains(recipeId);
	}

	public int size() {
		return this.recipeList.size();
	}
}
