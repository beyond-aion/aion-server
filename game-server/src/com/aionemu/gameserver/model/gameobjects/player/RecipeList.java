package com.aionemu.gameserver.model.gameobjects.player;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.dao.PlayerRecipesDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEARN_RECIPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECIPE_DELETE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, Neon, SVDNESS
 */
public class RecipeList {

	/**
	 * Recipe ID -> remaining production count (0 = unlimited)
	 */
	private final Map<Integer, Integer> productionLimitByRecipeId = new HashMap<>();


	public synchronized Map<Integer, Integer> getRecipes() {
		return Map.copyOf(productionLimitByRecipeId);
	}

	public boolean addRecipe(Player player, int recipeId) {
		return addRecipe(player, recipeId, DataManager.RECIPE_DATA.getMaxProductionCount(recipeId));
	}

	/**
	 * @param productionLimit
	 *          the allowed number of usages (0 = unlimited)
	 */
	public boolean addRecipe(int recipeId, int productionLimit) {
		return addRecipe(null, recipeId, productionLimit);
	}

	public synchronized boolean addRecipe(Player player, int recipeId, int productionLimit) {
		if (productionLimitByRecipeId.putIfAbsent(recipeId, productionLimit) != null)
			return false;
		if (player != null) {
			if (!PlayerRecipesDAO.addRecipe(player.getObjectId(), recipeId, productionLimit)) {
				productionLimitByRecipeId.remove(recipeId);
				return false;
			}
			PacketSendUtility.sendPacket(player, new SM_LEARN_RECIPE(recipeId, productionLimit));
		}
		return true;
	}

	public synchronized boolean deleteRecipe(Player player, int recipeId) {
		if (productionLimitByRecipeId.containsKey(recipeId) && PlayerRecipesDAO.delRecipe(player.getObjectId(), recipeId)) {
			productionLimitByRecipeId.remove(recipeId);
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
	public synchronized void decreaseProductionLimit(Player player, int recipeId) {
		Integer productionLimit = productionLimitByRecipeId.get(recipeId);
		if (productionLimit == null || productionLimit == 0)
			return;
		if (--productionLimit > 0) {
			productionLimitByRecipeId.put(recipeId, productionLimit);
			PlayerRecipesDAO.updateProductionLimit(player.getObjectId(), recipeId, productionLimit);
		} else {
			productionLimitByRecipeId.remove(recipeId);
			PlayerRecipesDAO.delRecipe(player.getObjectId(), recipeId);
			PacketSendUtility.sendPacket(player, new SM_RECIPE_DELETE(recipeId));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMBINE_USAGE_OVER(ChatUtil.recipeName(recipeId, player.getName())));
		}
	}

	public synchronized boolean isRecipePresent(int recipeId) {
		return productionLimitByRecipeId.containsKey(recipeId);
	}

	public synchronized int size() {
		return productionLimitByRecipeId.size();
	}
}
