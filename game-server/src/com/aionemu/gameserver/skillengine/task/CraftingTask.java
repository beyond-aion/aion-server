package com.aionemu.gameserver.skillengine.task;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.StaticObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CRAFT_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CRAFT_UPDATE;
import com.aionemu.gameserver.services.craft.CraftService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Mr. Poke, synchro2
 * @rework Yeats
 */
public class CraftingTask extends AbstractCraftTask {

	protected RecipeTemplate recipeTemplate;
	protected ItemTemplate itemTemplate;
	protected int critCount;
	protected boolean crit = false;
	protected int maxCritCount;
	private int bonus;
	private int delay;
	private int executionSpeed;

	/**
	 * @param requestor
	 * @param responder
	 * @param successValue
	 * @param failureValue
	 */
	public CraftingTask(Player requestor, StaticObject responder, RecipeTemplate recipeTemplate, int skillLvlDiff, int bonus) {
		super(requestor, responder, skillLvlDiff);
		this.recipeTemplate = recipeTemplate;
		this.maxCritCount = recipeTemplate.getComboProductSize();
		this.bonus = bonus;
	}

	@Override
	protected void onFailureFinish() {
		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue, currentFailureValue,
			6, 0, 0));
		PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), 0, 3), true);
	}

	@Override
	protected boolean onSuccessFinish() {
		if (crit && recipeTemplate.getComboProduct(critCount) != null) {
			PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 3, 0, 0));
			onInteractionStart();
			return false;
		} else {
			PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue,
				currentFailureValue, 5, 0, 0));
			PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), 0, 2), true);
			CraftService.finishCrafting(requestor, recipeTemplate, critCount, bonus);
			return true;
		}
	}

	@Override
	protected void sendInteractionUpdate() {
		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue, currentFailureValue, craftType.getCritId(), executionSpeed, delay));
	}

	@Override
	protected void onInteractionAbort() {
		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 4, 0, 0));
		PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), 0, 2), true);
		requestor.setCraftingTask(null);
	}

	@Override
	protected void onInteractionFinish() {
		requestor.setCraftingTask(null);
	}

	@Override
	protected void onInteractionStart() {
		currentSuccessValue = 0;
		currentFailureValue = 0;
		checkCrit();

		int chance = requestor.getRates().getCraftCritRate();
		if (maxCritCount > 0) {
			if (critCount > 0 && maxCritCount > 1) {
				chance = requestor.getRates().getComboCritRate();
				House house = requestor.getActiveHouse();
				if (house != null)
					switch (house.getHouseType()) {
						case ESTATE:
						case PALACE:
							chance += 5;
							break;
					}
			}

			if ((critCount < maxCritCount) && (Rnd.get(100) < chance)) {
				critCount++;
				crit = true;
			}
		}

		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, fullBarValue, fullBarValue, 0, 0, 0));
		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 1, 0, 0));
		PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(),
			recipeTemplate.getSkillid(), 0), true);
		PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(),
			recipeTemplate.getSkillid(), 1), true);
	}

	protected void checkCrit() {
		if (crit) {
			crit = false;
			this.itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getComboProduct(critCount));
		} else
			this.itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getProductid());
	}

	@Override
	protected final void analyzeInteraction() {
		if (recipeTemplate.getSkillid() == 40009) { // morph
			currentSuccessValue = fullBarValue;
			return;
		} else if (skillLvlDiff < 0) {
			currentFailureValue = fullBarValue;
			return;
		}

		craftType = CraftType.NORMAL;
		float multi = Rnd.get() + 1f;
		float failReduction = Math.max(1 - skillLvlDiff * 0.015f, 0.25f); // dynamic fail rate multiplier
		boolean success = Rnd.get() * 100 > CraftConfig.MAX_CRAFT_FAILURE_CHANCE * failReduction;

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
			if (Rnd.get() * 100 <= (15 + skillLvlDiff / 3f))
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
		executionSpeed = speed < 300 ? 300 : speed;
		int showDelay = bonusModifier < 1 ? 1200 : (1200 - (skillLvlDiff * 30));
		delay = showDelay < 500 ? 500 : showDelay;
	}
}
