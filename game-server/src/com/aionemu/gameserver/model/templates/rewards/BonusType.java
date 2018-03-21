package com.aionemu.gameserver.model.templates.rewards;

/**
 * @author Rolandas
 */

public enum BonusType {
	BOSS, // %Quest_L_boss, items having suffix _g_
	COIN, // %Quest_L_coin -- not used, 99 lvl quests replaced with trade
	ENCHANT,
	EVENTS, // %Quest_Branch_Cashquest_Event
	FOOD, // %Quest_L_food
	FORTRESS, // %Quest_L_fortress
	GATHER,
	GOODS, // %Quest_D_Goods
	ISLAND,
	LUNAR, // %Quest_A_BranchLunarEvent, exchange charms
	MAGICAL, // %Quest_L_magical -- unknown
	MANASTONE, // %Quest_L_matter_option
	MASTER_RECIPE, // %Quest_ws_master_recipe -- not used, 99 lvl quests, heart exchange now
	MATERIAL, // %Quest_D_material (Only Asmodian)
	MEDAL, // %Quest_L_medal, fountain rewards
	MEDICINE, // %Quest_L_medicine; potions and remedies
	MOVIE, // %Quest_L_Christmas; cut scenes
	NONE,
	RECIPE, // %Quest_L_Recipe_20a_LF2A (Only Elyos, Theobomos)
	REDEEM, // %Quest_L_redeem, exchange angel's/demon's eyes + kinah
	RIFT, // %Quest_L_BranchRiftEvent
	TASK, // %Quest_L_task; craft related
	WINTER // %Quest_A_BranchWinterEvent
}
