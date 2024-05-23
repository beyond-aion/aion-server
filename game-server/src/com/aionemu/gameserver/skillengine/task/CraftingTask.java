package com.aionemu.gameserver.skillengine.task;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.StaticObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CRAFT_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CRAFT_UPDATE;
import com.aionemu.gameserver.services.craft.CraftService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Mr. Poke, synchro2, Yeats
 */
public class CraftingTask extends AbstractCraftTask {

	private final RecipeTemplate recipeTemplate;
	private final int maxCritCount;
	private final int bonus;
	private ItemTemplate itemTemplate;
	private int critCount;
	private int showBarDelay;
	private int executionSpeed;

	public CraftingTask(Player requester, StaticObject responder, RecipeTemplate recipeTemplate, int skillLvlDiff, int bonus) {
		super(requester, responder, skillLvlDiff);
		this.recipeTemplate = recipeTemplate;
		this.maxCritCount = recipeTemplate.getComboProductSize();
		this.bonus = bonus;
		this.itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getProductId());
	}

	@Override
	protected void onFailureFinish() {
		PacketSendUtility.sendPacket(requester,
			new SM_CRAFT_UPDATE(recipeTemplate.getSkillId(), itemTemplate, currentSuccessValue, currentFailureValue, 6, 0, 0));
		PacketSendUtility.broadcastPacket(requester, new SM_CRAFT_ANIMATION(requester.getObjectId(), responder.getObjectId(), 0, 3), true);
	}

	@Override
	protected boolean onSuccessFinish() {
		if (calculateCrit()) {
			onInteractionStart();
			return false;
		} else {
			PacketSendUtility.sendPacket(requester,
				new SM_CRAFT_UPDATE(recipeTemplate.getSkillId(), itemTemplate, currentSuccessValue, currentFailureValue, 5, 0, 0));
			PacketSendUtility.broadcastPacket(requester, new SM_CRAFT_ANIMATION(requester.getObjectId(), responder.getObjectId(), 0, 2), true);
			CraftService.finishCrafting(requester, recipeTemplate, critCount, bonus);
			return true;
		}
	}

	private boolean calculateCrit() {
		if (critCount >= maxCritCount)
			return false;

		if (recipeTemplate.getComboProduct(critCount + 1) == null)
			return false;

		// first crit uses base rate, subsequent crits use combo rate
		float chance;
		if (critCount == 0)
			chance = Rates.get(requester, RatesConfig.CRAFT_CRIT_CHANCES);
		else
			chance = Rates.get(requester, RatesConfig.CRAFT_COMBO_CHANCES);
		House house = requester.getActiveHouse();
		if (house != null)
			switch (house.getHouseType()) {
				case ESTATE:
				case PALACE:
					chance += 5;
					break;
			}

		if (Rnd.chance() >= chance)
			return false;

		critCount++;
		itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getComboProduct(critCount));
		return true;
	}

	@Override
	protected void sendInteractionUpdate() {
		PacketSendUtility.sendPacket(requester, new SM_CRAFT_UPDATE(recipeTemplate.getSkillId(), itemTemplate, currentSuccessValue, currentFailureValue,
			craftType.getProgressId(), executionSpeed, showBarDelay));
	}

	@Override
	protected void onInteractionAbort() {
		PacketSendUtility.sendPacket(requester, new SM_CRAFT_UPDATE(recipeTemplate.getSkillId(), itemTemplate, 0, 0, 4, 0, 0));
		PacketSendUtility.broadcastPacket(requester, new SM_CRAFT_ANIMATION(requester.getObjectId(), responder.getObjectId(), 0, 2), true);
		requester.setCraftingTask(null);
	}

	@Override
	protected void onInteractionFinish() {
		requester.setCraftingTask(null);
	}

	@Override
	protected void onInteractionStart() {
		currentSuccessValue = 0;
		currentFailureValue = 0;

		PacketSendUtility.sendPacket(requester,
			new SM_CRAFT_UPDATE(recipeTemplate.getSkillId(), itemTemplate, fullBarValue, fullBarValue, critCount == 0 ? 0 : 3, 0, 0));
		PacketSendUtility.sendPacket(requester, new SM_CRAFT_UPDATE(recipeTemplate.getSkillId(), itemTemplate, 0, 0, 1, 0, 0));
		PacketSendUtility.broadcastPacket(requester,
			new SM_CRAFT_ANIMATION(requester.getObjectId(), responder.getObjectId(), recipeTemplate.getSkillId(), 0), true);
		PacketSendUtility.broadcastPacket(requester,
			new SM_CRAFT_ANIMATION(requester.getObjectId(), responder.getObjectId(), recipeTemplate.getSkillId(), 1), true);
	}

	@Override
	protected final void analyzeInteraction() {
		if (recipeTemplate.getSkillId() == 40009) { // morph
			currentSuccessValue = fullBarValue;
			return;
		} else if (skillLvlDiff < 0) {
			currentFailureValue = fullBarValue;
			return;
		}

		craftType = CraftType.NORMAL;
		float multi = Rnd.nextFloat(1f, 2f);
		float failReduction = Math.max(1 - skillLvlDiff * 0.015f, 0.25f); // dynamic fail rate multiplier
		boolean success = skillLvlDiff >= 41 || Rnd.chance() >= CraftConfig.MAX_CRAFT_FAILURE_CHANCE * failReduction;

		float bonusModifier = 1;
		switch (itemTemplate.getItemQuality()) {
			case LEGEND:
				bonusModifier = 0.9f;
				break;
			case UNIQUE:
				bonusModifier = 0.7f;
				break;
			case EPIC:
				bonusModifier = 0.5f;
				break;
			case MYTHIC:
				bonusModifier = 0.3f;
				break;
		}

		if (success) {
			if (Rnd.chance() < (15 + skillLvlDiff / 3f))
				craftType = CraftType.CRIT_BLUE; // LIGHT BLUE + 10-20%

			int minStep = 70;
			int lvlBoni = skillLvlDiff > 10 ? ((skillLvlDiff - 10) * 2) : 0;
			int bonus = (int) (((craftType == CraftType.CRIT_BLUE ? 100 : 0) + (((skillLvlDiff + 1) / 2f) + lvlBoni) * 10) * multi);
			currentSuccessValue += Math.round(minStep + (bonus * bonusModifier));
		} else {
			int minStep = recipeTemplate.getMaxProductionCount() != null ? 70 : 120;
			int bonus = (int) (((skillLvlDiff + 1) / 1.5f * 10) * multi);
			currentFailureValue += Math.round(minStep + (bonus * bonusModifier));
		}

		if (currentSuccessValue > fullBarValue)
			currentSuccessValue = fullBarValue;
		else if (currentFailureValue > fullBarValue)
			currentFailureValue = fullBarValue;

		int speed = bonusModifier < 1 ? Math.round(900 * (2 - bonusModifier)) : (900 - (skillLvlDiff * 30));
		executionSpeed = Math.max(speed, 300);
		showBarDelay = bonusModifier < 1 ? 1200 : Math.max(500, 1200 - (skillLvlDiff * 30));
	}
}
