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
 */
public class CraftingTask extends AbstractCraftTask {

	protected RecipeTemplate recipeTemplate;
	protected ItemTemplate itemTemplate;
	protected int critCount;
	protected boolean crit = false;
	protected int maxCritCount;
	private int bonus;

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
			6));
		PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), 0, 3), true);
	}

	@Override
	protected boolean onSuccessFinish() {
		if (crit && recipeTemplate.getComboProduct(critCount) != null) {
			PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 3));
			onInteractionStart();
			return false;
		} else {
			PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue,
				currentFailureValue, 5));
			PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), 0, 2), true);
			CraftService.finishCrafting(requestor, recipeTemplate, critCount, bonus);
			return true;
		}
	}

	@Override
	protected void sendInteractionUpdate() {
		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue, currentFailureValue,
			1));
	}

	@Override
	protected void onInteractionAbort() {
		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 4));
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

		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, completeValue, completeValue, 0));
		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 1));
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
		// TODO: handle progress speed and display light blue / purple progress bar
		int easeLevel = skillLvlDiff / 10;
		easeLevel += easeLevel < skillLvlDiff ? 1 : 0;

		int maxFailureChance = CraftConfig.MAX_CRAFT_FAILURE_CHANCE;
		while (easeLevel > 1)
			maxFailureChance /= easeLevel--;

		int multi = Math.max(1, maxFailureChance);
		if (recipeTemplate.getSkillid() == 40009)
			multi = 0;

		if (Rnd.get(100) > multi)
			currentSuccessValue += Rnd.get(completeValue / (multi + 1) / 2, completeValue / (Math.max(multi / 3, 1)));
		else
			currentFailureValue += Rnd.get(completeValue / (multi + 1) / 2, completeValue / (Math.max(multi / 5, 1)));

		if (currentSuccessValue >= completeValue) {
			currentSuccessValue = completeValue;
		} else if (currentFailureValue >= completeValue) {
			currentFailureValue = completeValue;
		}
	}

}
