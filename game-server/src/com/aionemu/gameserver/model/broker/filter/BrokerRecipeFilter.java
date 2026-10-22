package com.aionemu.gameserver.model.broker.filter;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.CraftLearnAction;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;

/**
 * @author xTz
 */
public class BrokerRecipeFilter extends BrokerContainsFilter {

	private final int craftSkillId;

	public BrokerRecipeFilter(int craftSkillId, int... masks) {
		super(masks);
		this.craftSkillId = craftSkillId;
	}

	@Override
	public boolean accept(ItemTemplate template) {
		CraftLearnAction craftAction = template.getActions() == null ? null : template.getActions().getCraftLearnAction();
		if (craftAction == null)
			return false;
		if (!super.accept(template))
			return false;
		int id = craftAction.getRecipeId();
		RecipeTemplate recipeTemplate = DataManager.RECIPE_DATA.getRecipeTemplateById(id);
		return recipeTemplate != null && recipeTemplate.getSkillId() == craftSkillId;
	}

}
