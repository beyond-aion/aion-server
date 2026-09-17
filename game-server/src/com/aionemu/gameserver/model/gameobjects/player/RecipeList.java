package com.aionemu.gameserver.model.gameobjects.player;

import java.util.*;

import com.aionemu.gameserver.dao.PlayerRecipesDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEARN_RECIPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECIPE_DELETE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, Neon, SVDNESS
 */
public class RecipeList {
	// Value is the remaining production count; 0: unlimited, matching retail.
	private Map<Integer, Integer> recipeList = new HashMap<>();

	public RecipeList(HashMap<Integer, Integer> recipeList) {
		this.recipeList = recipeList;
		// Recipes without stored counts are loaded from templates; a zero count on a limited recipe is treated as legacy data.
		for (Map.Entry<Integer, Integer> recipe : recipeList.entrySet()) {
			if (recipe.getValue() == 0) {
				recipe.setValue(resolveProductionCount(recipe.getKey()));
			}
		}
	}

	public RecipeList() {}

	public Set<Integer> getRecipeList() {
		return recipeList.keySet();
	}

	public Map<Integer, Integer> getRecipes() {
		return Collections.unmodifiableMap(recipeList);
	}

	public boolean addRecipe(Player player, int recipeId) {
		int productionCount = resolveProductionCount(recipeId);
		if (!isRecipePresent(recipeId) && PlayerRecipesDAO.addRecipe(player.getObjectId(), recipeId, productionCount)) {
			recipeList.put(recipeId, productionCount);
			PacketSendUtility.sendPacket(player, new SM_LEARN_RECIPE(recipeId, productionCount));
			return true;
		}
		return false;
	}

	public void deleteRecipe(Player player, int recipeId) {
		if (recipeList.containsKey(recipeId) && PlayerRecipesDAO.delRecipe(player.getObjectId(), recipeId)) {
			recipeList.remove(recipeId);
			PacketSendUtility.sendPacket(player, new SM_RECIPE_DELETE(recipeId));
		}
	}

	// Retail consumes a charge at craft start and removes the recipe when none remain.
	public void decreaseProductionCount(Player player, int recipeId) {
		Integer productionCount = recipeList.get(recipeId);
		if (productionCount == null || productionCount == 0) {
			return;
		}
		int left = productionCount - 1;
		if (left == 0) {
			deleteRecipe(player, recipeId);
			return;
		}
		if (PlayerRecipesDAO.updateProductionCount(player.getObjectId(), recipeId, left)) {
			recipeList.put(recipeId, left);
		}
	}

	public boolean isRecipePresent(int recipeId) {
		return recipeList.containsKey(recipeId);
	}

	public int size() {
		return this.recipeList.size();
	}

	private static int resolveProductionCount(int recipeId) {
		RecipeTemplate template = DataManager.RECIPE_DATA.getRecipeTemplateById(recipeId);
		if (template == null || template.getMaxProductionCount() == null) {
			return 0;
		}
		return template.getMaxProductionCount();
	}
}