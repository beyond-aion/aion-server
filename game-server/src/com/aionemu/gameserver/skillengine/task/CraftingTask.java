package com.aionemu.gameserver.skillengine.task;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.StaticObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
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
	private int delay = 1200;
	private int executionSpeed = 900;

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
			PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 3, 0 ,0));
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
		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue, currentFailureValue, this.craftType.getPacketId(), this.executionSpeed, this.delay));
		//PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue, currentFailureValue,
		//	1));
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

		PacketSendUtility.sendPacket(requestor, new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, completeValue, completeValue, 0, 0, 0));
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
		//BEYOND AION CALCULATION
		if (recipeTemplate.getSkillid() == 40009) { //morph
			currentSuccessValue = completeValue;
			return;
		} else if (skillLvlDiff >= 41) {
			currentSuccessValue = completeValue;
			this.executionSpeed = 300;
			this.delay = 500;
			return;
		} else if (skillLvlDiff < 0) {
			currentFailureValue = completeValue;
			return;
		}
		
		int lvlDiff = skillLvlDiff <= 0 ? 1 : skillLvlDiff;
		int maxFailureChance = ((int) (CraftConfig.MAX_CRAFT_FAILURE_CHANCE - (skillLvlDiff/5)));
		boolean success = Rnd.get(1, 100) <= maxFailureChance ? false : true;
		
		if (success) {
			int critChance = Rnd.get(1, 100);
			if (critChance <= (15 + getRnd(0, skillLvlDiff/5))) {
				craftType = CraftType.CRIT_BLUE; // LIGHT BLUE + 10%
			} else {
				craftType = CraftType.NORMAL;
			}
		} else {
			craftType = CraftType.NORMAL;
		}
		
		ItemQuality quality = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getProductid()).getItemQuality();
		float qualy = 1;
		int minValue = 70;
		switch (quality) {
			case LEGEND:
				minValue = 60;
				qualy = 0.9f;
				break;
			case UNIQUE:
				minValue = 50;
				qualy = 0.8f;
				break;
			case EPIC:
				minValue = 30;
				qualy = 0.5f;
				break;
			case MYTHIC:
				minValue = 20;
				qualy = 0.3f;
				break;
				default:
					break;
		}
		
		if (success) {
			int lvlBoni = skillLvlDiff > 10 ? ((skillLvlDiff - 10) * 2) : 0;
			int currentValue = (int) Math.round(((this.craftType.getCritId() == CraftType.CRIT_BLUE.getCritId() ? 100 : 0) + 10 * (getRnd(1+(lvlDiff/2.5), (lvlDiff/2.5) +3) + (lvlDiff/5) + lvlBoni)));
			currentSuccessValue += (int) Math.round((minValue + (currentValue * qualy)));
		} else {
			currentFailureValue += (int) Math.round((minValue + (getRnd(7 + (lvlDiff/3), 12 + (lvlDiff/1.5)) * 10) * qualy));
		}
		
		if (currentSuccessValue > completeValue) {
			currentSuccessValue = completeValue;
		} else if (currentFailureValue > completeValue) {
			currentFailureValue = completeValue;
		}
		
		int speed = 900 - (skillLvlDiff * 30);
		this.executionSpeed = speed < 300 ? 300 : speed;
		int showDelay = 1200 - (skillLvlDiff * 30);
		this.delay = showDelay < 500 ? 500 : showDelay;
	}
	
	private int getRnd(double min, double max) {
		return (int) Math.round((min + Math.floor(Rnd.nextDouble() * (max - min + 1))));
	}
		//OLD AL 4.7.5 CALCULATION
		/*// TODO: handle progress speed and display light blue / purple progress bar
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
	*/
}
