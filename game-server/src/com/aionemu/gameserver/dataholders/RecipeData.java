package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;

/**
 * @author ATracer, MrPoke, KID
 */
@XmlRootElement(name = "recipe_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class RecipeData {

	@XmlElement(name = "recipe_template")
	protected List<RecipeTemplate> list;

	@XmlTransient
	private final Map<Integer, RecipeTemplate> recipeData = new HashMap<>();
	@XmlTransient
	private final List<RecipeTemplate> autoLearnRecipes = new ArrayList<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (RecipeTemplate it : list) {
			recipeData.put(it.getId(), it);
			if (it.getAutoLearn() != 0)
				autoLearnRecipes.add(it);
		}
		list = null;
	}

	public List<RecipeTemplate> getAutolearnRecipes(Race race, int skillId, int maxLevel) {
		List<RecipeTemplate> list = new ArrayList<>();
		for (RecipeTemplate recipe : autoLearnRecipes) {
			if (recipe.getSkillId() != skillId || recipe.getSkillpoint() > maxLevel)
				continue;
			if (recipe.getRace() != Race.PC_ALL && recipe.getRace() != race)
				continue;
			list.add(recipe);
		}
		return list;
	}

	public RecipeTemplate getRecipeTemplateById(int id) {
		return recipeData.get(id);
	}

	public Collection<RecipeTemplate> getRecipeTemplates() {
		return recipeData.values();
	}

	public int size() {
		return recipeData.size();
	}
}
