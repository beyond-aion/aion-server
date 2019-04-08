package com.aionemu.gameserver.dataholders;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.templates.itemgroups.BonusItemGroup;
import com.aionemu.gameserver.model.templates.itemgroups.BossGroup;
import com.aionemu.gameserver.model.templates.itemgroups.CraftItemGroup;
import com.aionemu.gameserver.model.templates.itemgroups.CraftRecipeGroup;
import com.aionemu.gameserver.model.templates.itemgroups.EnchantGroup;
import com.aionemu.gameserver.model.templates.itemgroups.EventGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.AetherCherryGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.AetherCrystalBiscuitGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.AetherGemBiscuitGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.AetherPowderBiscuitGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.FeedArmorGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.FeedBalaurGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.FeedBoneGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.FeedExcludeGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.FeedFluidGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.FeedSoulGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.FeedThornGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.HealthyFoodAllGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.HealthyFoodSpicyGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.PoppySnackGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.PoppySnackNutritiousGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.PoppySnackTastyGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.ShugoEventCoinGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FeedGroups.StinkingJunkGroup;
import com.aionemu.gameserver.model.templates.itemgroups.FoodGroup;
import com.aionemu.gameserver.model.templates.itemgroups.GatherGroup;
import com.aionemu.gameserver.model.templates.itemgroups.ItemRaceEntry;
import com.aionemu.gameserver.model.templates.itemgroups.ManastoneGroup;
import com.aionemu.gameserver.model.templates.itemgroups.MedalGroup;
import com.aionemu.gameserver.model.templates.itemgroups.MedicineGroup;
import com.aionemu.gameserver.model.templates.itemgroups.OreGroup;
import com.aionemu.gameserver.model.templates.pet.FoodType;

/**
 * @author Rolandas
 */
@XmlRootElement(name = "item_groups")
@XmlAccessorType(XmlAccessType.NONE)
public class ItemGroupsData {

	@XmlElement(name = "craft_materials")
	private CraftItemGroup craftMaterials;

	@XmlElement(name = "craft_shop")
	private CraftItemGroup craftShop;

	@XmlElement(name = "craft_bundles")
	private CraftRecipeGroup craftBundles;

	@XmlElement(name = "craft_recipes")
	private CraftRecipeGroup craftRecipes;

	@XmlElement(name = "manastones_common")
	private ManastoneGroup manastonesCommon;

	@XmlElement(name = "manastones_rare")
	private ManastoneGroup manastonesRare;

	@XmlElement(name = "medals")
	private MedalGroup medals;

	@XmlElement(name = "food")
	private FoodGroup food;

	@XmlElement(name = "medicine_common")
	private MedicineGroup medicineCommon;

	@XmlElement(name = "medicine_rare")
	private MedicineGroup medicineRare;

	@XmlElement(name = "medicine_legendary")
	private MedicineGroup medicineLegendary;

	@XmlElement(name = "ores_rare")
	private OreGroup oresRare;

	@XmlElement(name = "ores_legendary")
	private OreGroup oresLegendary;

	@XmlElement(name = "ores_unique")
	private OreGroup oresUnique;

	@XmlElement(name = "ores_epic")
	private OreGroup oresEpic;

	@XmlElement(name = "gather_rare")
	private GatherGroup gatherRare;

	@XmlElement(name = "enchants")
	private EnchantGroup enchants;

	@XmlElement(name = "events")
	private EventGroup events;

	@XmlElement(name = "boss_rare")
	private BossGroup bossRare;

	@XmlElement(name = "boss_legendary")
	private BossGroup bossLegendary;

	@XmlElement(name = "feed_fluid")
	private FeedFluidGroup feedFluids;

	@XmlElement(name = "feed_armor")
	private FeedArmorGroup feedArmor;

	@XmlElement(name = "feed_thorn")
	private FeedThornGroup feedThorns;

	@XmlElement(name = "feed_bone")
	private FeedBoneGroup feedBones;

	@XmlElement(name = "feed_balaur_material")
	private FeedBalaurGroup feedBalaurScales;

	@XmlElement(name = "feed_soul")
	private FeedSoulGroup feedSouls;

	@XmlElement(name = "feed_exclude")
	private FeedExcludeGroup feedExcludes;

	@XmlElement(name = "stinking_junk")
	private StinkingJunkGroup stinkingJunk;

	@XmlElement(name = "feed_healthy_all")
	private HealthyFoodAllGroup healthyFoodAll;

	@XmlElement(name = "feed_healthy_spicy")
	private HealthyFoodSpicyGroup healthyFoodSpicy;

	@XmlElement(name = "feed_powder_biscuit")
	private AetherPowderBiscuitGroup aetherPowderBiscuit;

	@XmlElement(name = "feed_crystal_biscuit")
	private AetherCrystalBiscuitGroup aetherCrystalBiscuit;

	@XmlElement(name = "feed_gem_biscuit")
	private AetherGemBiscuitGroup aetherGemBiscuit;

	@XmlElement(name = "poppy_snack")
	private PoppySnackGroup poppySnack;

	@XmlElement(name = "tasty_poppy_snack")
	private PoppySnackTastyGroup poppySnackTasty;

	@XmlElement(name = "nutritious_poppy_snack")
	private PoppySnackNutritiousGroup poppySnackNutritious;

	@XmlElement(name = "feed_shugo_event_coin")
	private ShugoEventCoinGroup shugoCoins;

	@XmlElement(name = "feed_aether_cherry")
	private AetherCherryGroup aetherCherries;

	private List<BonusItemGroup> craftGroups;
	private List<BonusItemGroup> manastoneGroups;
	private List<BonusItemGroup> medalGroups;
	private List<BonusItemGroup> foodGroups;
	private List<BonusItemGroup> medicineGroups;
	private List<BonusItemGroup> gatherGroups;
	private List<BonusItemGroup> enchantGroups;
	private List<BonusItemGroup> eventGroups;
	private List<BonusItemGroup> bossGroups;

	private Map<FoodType, Set<Integer>> petFood = new EnumMap<>(FoodType.class);

	private int petFoodCount = 0;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		craftGroups = Arrays.asList(craftMaterials, craftShop, craftBundles, craftRecipes);
		manastoneGroups = Arrays.asList(manastonesCommon, manastonesRare);
		medalGroups = Collections.singletonList(medals);
		foodGroups = Collections.singletonList(food);
		medicineGroups = Arrays.asList(medicineCommon, medicineRare, medicineLegendary);
		gatherGroups = Arrays.asList(gatherRare, oresRare, oresLegendary, oresUnique, oresEpic);
		enchantGroups = Collections.singletonList(enchants);
		eventGroups = Collections.singletonList(events);
		bossGroups = Arrays.asList(bossRare, bossLegendary);

		for (FoodType foodType : FoodType.values()) {
			List<ItemRaceEntry> food = getPetFood(foodType);
			if (food == null)
				continue;
			Set<Integer> itemIds = food.stream().map(ItemRaceEntry::getId).collect(Collectors.toSet());
			petFood.put(foodType, itemIds);
			if (foodType != FoodType.EXCLUDES && foodType != FoodType.STINKY)
				petFoodCount += itemIds.size();
			food.clear();
		}
	}

	public List<BonusItemGroup> getCraftGroups() {
		return craftGroups;
	}

	public List<BonusItemGroup> getManastoneGroups() {
		return manastoneGroups;
	}

	public List<BonusItemGroup> getMedalGroups() {
		return medalGroups;
	}

	public List<BonusItemGroup> getFoodGroups() {
		return foodGroups;
	}

	public List<BonusItemGroup> getMedicineGroups() {
		return medicineGroups;
	}

	public List<BonusItemGroup> getGatherGroups() {
		return gatherGroups;
	}

	public List<BonusItemGroup> getEnchantGroups() {
		return enchantGroups;
	}

	public List<BonusItemGroup> getEventGroups() {
		return eventGroups;
	}

	public List<BonusItemGroup> getBossGroups() {
		return bossGroups;
	}

	public boolean isFood(int itemId, FoodType foodType) {
		Set<Integer> food = petFood.get(FoodType.EXCLUDES);
		if (food.contains(itemId))
			return false;
		food = petFood.get(FoodType.STINKY);
		if (food.contains(itemId))
			return false;
		if (foodType != FoodType.MISCELLANEOUS) {
			food = petFood.get(foodType);
			return food.contains(itemId);
		} else {
			food = petFood.get(FoodType.ARMOR);
			if (food.contains(itemId))
				return true;
			food = petFood.get(FoodType.BALAUR_SCALES);
			if (food.contains(itemId))
				return true;
			food = petFood.get(FoodType.BONES);
			if (food.contains(itemId))
				return true;
			food = petFood.get(FoodType.FLUIDS);
			if (food.contains(itemId))
				return true;
			food = petFood.get(FoodType.SOULS);
			if (food.contains(itemId))
				return true;
			food = petFood.get(FoodType.THORNS);
			if (food.contains(itemId))
				return true;
		}
		return false;
	}

	private List<ItemRaceEntry> getPetFood(FoodType foodType) {
		switch (foodType) {
			// Biscuits bought from shop
			case AETHER_CRYSTAL_BISCUIT:
				return aetherCrystalBiscuit.getItems();
			case AETHER_GEM_BISCUIT:
				return aetherGemBiscuit.getItems();
			case AETHER_POWDER_BISCUIT:
				return aetherPowderBiscuit.getItems();
			case AETHER_CHERRY:
				return aetherCherries.getItems();

			// Specific Junk
			case ARMOR:
				return feedArmor.getItems();
			case BALAUR_SCALES:
				return feedBalaurScales.getItems();
			case BONES:
				return feedBones.getItems();
			case FLUIDS:
				return feedFluids.getItems();
			case SOULS:
				return feedSouls.getItems();
			case THORNS:
				return feedThorns.getItems();

			// Healthy Pet Food bought from vendors
			case HEALTHY_FOOD_ALL:
				return healthyFoodAll.getItems();
			case HEALTHY_FOOD_SPICY:
				return healthyFoodSpicy.getItems();

			// Runaway Poppy's Food
			case POPPY_SNACK:
				return poppySnack.getItems();
			case POPPY_SNACK_TASTY:
				return poppySnackTasty.getItems();
			case POPPY_SNACK_NUTRITIOUS:
				return poppySnackNutritious.getItems();

			// Shugo Tomb Event pet food
			case SHUGO_EVENT_COIN:
				return shugoCoins.getItems();

			// Exclusions
			case STINKY:
				return stinkingJunk.getItems();
			case EXCLUDES:
				return feedExcludes.getItems();
			case MISCELLANEOUS:
				break;
			default:
				LoggerFactory.getLogger(ItemGroupsData.class).warn("Unhandled food type " + foodType);
		}
		return null;
	}

	public int bonusSize() {
		return craftMaterials.getItems().size() + craftShop.getItems().size() + craftBundles.getItems().size() + craftRecipes.getItems().size()
			+ manastonesCommon.getItems().size() + manastonesRare.getItems().size() + food.getItems().size() + medicineCommon.getItems().size()
			+ medicineRare.getItems().size() + medicineLegendary.getItems().size() + oresRare.getItems().size() + oresLegendary.getItems().size()
			+ oresUnique.getItems().size() + oresEpic.getItems().size() + gatherRare.getItems().size() + enchants.getItems().size()
			+ events.getItems().size() + bossRare.getItems().size() + bossLegendary.getItems().size();
	}

	public int petFoodSize() {
		return petFoodCount;
	}

}
