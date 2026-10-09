package com.aionemu.gameserver.model.gameobjects.player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aionemu.gameserver.dao.PlayerRecipesDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEARN_RECIPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECIPE_DELETE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, Neon, SVDNESS
 */
public class RecipeList {

	/**
	 * Recipe ID -> remaining production count (0 = unlimited)
	 */
	private final Map<Integer, Integer> recipeList;

	public RecipeList(Map<Integer, Integer> recipeList) {
		this.recipeList = recipeList;
		// limited recipes stored before production counts were tracked have a count of 0, they start with the full count
		for (Map.Entry<Integer, Integer> recipe : recipeList.entrySet()) {
			if (recipe.getValue() == 0)
				recipe.setValue(getMaxProductionCount(recipe.getKey()));
		}
	}

	public RecipeList() {
		this(new HashMap<>());
	}

	public Set<Integer> getRecipeList() {
		return recipeList.keySet();
	}

	public Map<Integer, Integer> getRecipes() {
		return Collections.unmodifiableMap(recipeList);
	}

	public boolean addRecipe(Player player, int recipeId) {
		return addRecipe(player, recipeId, 0);
	}

	/**
	 * @param productionCount
	 *          remaining production count, 0 or anything above the template limit means the full count
	 */
	public boolean addRecipe(Player player, int recipeId, int productionCount) {
		int maxProductionCount = getMaxProductionCount(recipeId);
		if (productionCount <= 0 || productionCount > maxProductionCount)
			productionCount = maxProductionCount;
		if (!isRecipePresent(recipeId) && PlayerRecipesDAO.addRecipe(player.getObjectId(), recipeId, productionCount)) {
			recipeList.put(recipeId, productionCount);
			PacketSendUtility.sendPacket(player, new SM_LEARN_RECIPE(recipeId, productionCount));
			return true;
		}
		return false;
	}

	public boolean deleteRecipe(Player player, int recipeId) {
		if (recipeList.containsKey(recipeId) && PlayerRecipesDAO.delRecipe(player.getObjectId(), recipeId)) {
			recipeList.remove(recipeId);
			PacketSendUtility.sendPacket(player, new SM_RECIPE_DELETE(recipeId));
			return true;
		}
		return false;
	}

	/**
	 * Consumes one production of a limited recipe. The recipe is removed when its last production is used up. The client is not informed about the
	 * new count, it requests the recipe list again when needed. The production is used up even if the database write fails, so a failing database
	 * cannot make a limited recipe reusable.
	 */
	public void decreaseProductionCount(Player player, int recipeId) {
		Integer productionCount = recipeList.get(recipeId);
		if (productionCount == null || productionCount == 0)
			return;
		int left = productionCount - 1;
		if (left == 0) {
			recipeList.remove(recipeId);
			PlayerRecipesDAO.delRecipe(player.getObjectId(), recipeId);
			PacketSendUtility.sendPacket(player, new SM_RECIPE_DELETE(recipeId));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_USAGE_OVER("[recipe_ex:" + recipeId + ";" + player.getName() + "]"));
		} else {
			recipeList.put(recipeId, left);
			PlayerRecipesDAO.updateProductionCount(player.getObjectId(), recipeId, left);
		}
	}

	public boolean isRecipePresent(int recipeId) {
		return recipeList.containsKey(recipeId);
	}

	public int size() {
		return this.recipeList.size();
	}

	private static int getMaxProductionCount(int recipeId) {
		RecipeTemplate template = DataManager.RECIPE_DATA.getRecipeTemplateById(recipeId);
		return template == null || template.getMaxProductionCount() == null ? 0 : template.getMaxProductionCount();
	}
}
