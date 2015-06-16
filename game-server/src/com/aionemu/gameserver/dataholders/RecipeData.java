package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javolution.util.FastList;

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
	private TIntObjectHashMap<RecipeTemplate> recipeData;
	private FastList<RecipeTemplate> elyos, asmos, any;
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		recipeData = new TIntObjectHashMap<RecipeTemplate>();
		elyos = FastList.newInstance();
		asmos = FastList.newInstance();
		any = FastList.newInstance();
		for (RecipeTemplate it : list) {
			recipeData.put(it.getId(), it);
			if (it.getAutoLearn() == 0)
				continue;
			
			switch(it.getRace()) {
				case ASMODIANS:
					asmos.add(it);
					break;
				case ELYOS:
					elyos.add(it);
					break;
				case PC_ALL:
					any.add(it);
					break;
			}
		}
		list = null;
	}
	
	public FastList<RecipeTemplate> getAutolearnRecipes(Race race, int skillId, int maxLevel) {
		FastList<RecipeTemplate> list = FastList.newInstance();
		switch(race) {
			case ASMODIANS:
				for(RecipeTemplate recipe : asmos)
					if(recipe.getSkillid() == skillId && recipe.getSkillpoint() <= maxLevel)
						list.add(recipe);
				break;
			case ELYOS:
				for(RecipeTemplate recipe : elyos)
					if(recipe.getSkillid() == skillId && recipe.getSkillpoint() <= maxLevel)
						list.add(recipe);
				break;
		}
		
		for(RecipeTemplate recipe : any)
			if(recipe.getSkillid() == skillId && recipe.getSkillpoint() <= maxLevel)
				list.add(recipe);
		
		return list;
	}

	public RecipeTemplate getRecipeTemplateById(int id) {
		return recipeData.get(id);
	}

	public TIntObjectHashMap<RecipeTemplate> getRecipeTemplates() {
		return recipeData;
	}

	/**
	 * @return recipeData.size()
	 */
	public int size() {
		return recipeData.size();
	}
}
