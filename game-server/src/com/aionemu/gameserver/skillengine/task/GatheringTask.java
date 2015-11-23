package com.aionemu.gameserver.skillengine.task;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.gather.Material;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_UPDATE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class GatheringTask extends AbstractCraftTask {

	private GatherableTemplate template;
	private Material material;
	//private int barType = 1;
	private int showBarDelay = 1000;
	private int executionSpeed = 900;

	public GatheringTask(Player requestor, Gatherable gatherable, Material material, int skillLvlDiff) {
		super(requestor, gatherable, skillLvlDiff);
		this.template = gatherable.getObjectTemplate();
		this.material = material;
		this.delay = Rnd.get(200, 600);
		//this.interval = 1850;
		int gatherInterval = 2500 - (skillLvlDiff * 60);
		this.interval = gatherInterval < 1200 ? 1200 : gatherInterval;
	}

	@Override
	protected void onInteractionAbort() {
		//PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 4));
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 4));
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, 0, 0, 5, 0, 0));
	}

	@Override
	protected void onInteractionFinish() {
		((Gatherable) responder).getController().completeInteraction();
	}

	@Override
	protected void onInteractionStart() {
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, completeValue, completeValue, 0, 0, 0));
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, 0, 0, 1, 0, 0));
		// TODO: missing packet for initial failure/success
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 0), true);
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 1), true);
		//PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 0), true);
		//PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 1), true);
	}

	@Override
	protected void sendInteractionUpdate() {
		//PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, barType));
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, this.craftType.getPacketId(), this.executionSpeed, this.showBarDelay));
	}

	@Override
	protected void onFailureFinish() {
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 1, 0, 0));
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 7, 0, 0));
		//PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 3), true);
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 3), true);
	}

	@Override
	protected boolean onSuccessFinish() {
		//PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 2), true);
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 2), true);
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 6, 0, 0));
		//PacketSendUtility.sendPacket(requestor, SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_SUCCESS_1_BASIC(new DescriptionId(material.getNameid())));
		if (template.getEraseValue() > 0)
			requestor.getInventory().decreaseByItemId(template.getRequiredItemId(), template.getEraseValue());
		ItemService.addItem(requestor, material.getItemid(), requestor.getRates().getGatheringCountRate(), ItemService.DEFAULT_UPDATE_PREDICATE);
		if (requestor.isInInstance()) {
			requestor.getPosition().getWorldMapInstance().getInstanceHandler().onGather(requestor, (Gatherable) responder);
		}
		((Gatherable) responder).getController().rewardPlayer(requestor);
		return true;
	}

	/**
	 * Explanation of formulas are in the following graph: <img src="doc-files/gathering_bounds.png" />
	 * 
	 * @see com.aionemu.gameserver.skillengine.task.AbstractCraftTask#analyzeInteraction()
	 */
	@Override
	protected final void analyzeInteraction() {
		//BEYOND AION CALCULATION
		if (skillLvlDiff >= 41) {
			currentSuccessValue = completeValue;
			this.executionSpeed = 300;
			this.showBarDelay = 500;
			return;
		} else if (skillLvlDiff < 0) {
			currentFailureValue = completeValue;
			return;
		}
		
		int lvlDiff = skillLvlDiff <= 0 ? 1 : skillLvlDiff;
		int maxFailureChance = ((int) (CraftConfig.MAX_GATHER_FAILURE_CHANCE -/* (skillLvlDiff/1.5) -*/ (skillLvlDiff/5)));
		boolean success = Rnd.get(1, 100) <= maxFailureChance ? false : true;
		
		if (success) {
			int critChance = Rnd.get(1, 100);
			if (critChance <= (5 + getRnd(0, skillLvlDiff/5))) { //PURPLE CRIT = 100%
				craftType = CraftType.CRIT_PURPLE;
				currentSuccessValue = completeValue;
				this.executionSpeed = 300;
				this.showBarDelay = 500;
				return;
			} else if (critChance <= (15 + getRnd(0, skillLvlDiff/5))) { //LIGHT BLUE CRIT = +10%
				craftType = CraftType.CRIT_BLUE;
			} else {
				craftType = CraftType.NORMAL;
			}
		} else {
			craftType = CraftType.NORMAL;
		}
		
		int minValue = 70;
		if (success) {
			int currentValue = (int) Math.round(((this.craftType.getCritId() == CraftType.CRIT_BLUE.getCritId() ? 100 : 0) + 10 * (getRnd(1+(lvlDiff/2.5), (lvlDiff/2.5) +3) + (lvlDiff/3))));
			currentSuccessValue += (minValue + currentValue);
		} else {
			currentFailureValue += (int) Math.round(((minValue + (getRnd(7+(lvlDiff/3), 12+(lvlDiff/1.5)) * 10))));
		}
		
		if (currentSuccessValue > completeValue) {
			currentSuccessValue = completeValue;
		} else if (currentFailureValue > completeValue) {
			currentFailureValue = completeValue;
		}
		
		int speed = 900 - (skillLvlDiff * 30);
		this.executionSpeed = speed < 300 ? 300 : speed;
		int showDelay = 1200 - (skillLvlDiff * 30);
		this.showBarDelay = showDelay < 500 ? 500 : showDelay;
		
	}
	
	private int getRnd(double min, double max) {
		return (int) Math.round((min + Math.floor(Rnd.nextDouble() * (max - min + 1))));
	}
	
		//OLD AL 4.7.5 CALCULATION
		/*
		int easeLevel = skillLvlDiff / 10;
		easeLevel += easeLevel < skillLvlDiff ? 1 : 0;

		int lowerBound = 13 * easeLevel - 40;
		int upperBound = 14 * easeLevel + 30;

		// Display of purple bar is quite speculative. The purpose of it is not clear
		// Anyway, display it when some small chance was hit on both success/failure

		// There's always 38% chance of failure, but values are different
		// Reduced twice each 100 points
		int failChance = CraftConfig.MAX_GATHER_FAILURE_CHANCE;
		int lvlHundreds = easeLevel / 10;
		while (lvlHundreds-- > 0)
			failChance /= 2;

		boolean failed = Rnd.get(0, 100) <= failChance;

		int failValue = 0;
		int successValue = 0;
		if (failed) {
			if (lowerBound > 0) {
				// skillLvlDiff > 30; lowerBound is always positive
				failValue = Rnd.get(1, 2); // Always constant (1.6667 if exactly)
			} else {
				failValue = Rnd.get(1, -lowerBound);
			}
			// There's 0.57% chance to recover from failure (display as purple bar)
			if (Rnd.get(0, 1000) < 6) {
				successValue = Rnd.get(1, upperBound);
				failValue = 0;
				barType = 3;
			}
		} else {
			// There's 6% chance to boost value
			// boost values decrease and the chance to go through 100% increases with the level diff
			// i.e. randomness disappears, constant boost becomes the main boost type
			int boostChance = Rnd.get(0, 1000);
			if (boostChance < 60) {
				int boostFullBarChance = 9 * easeLevel;
				if (boostFullBarChance > 60 || Rnd.get(0, 100) <= boostFullBarChance) {
					boostFullBarChance = 100; // becomes constant
					successValue = 100;
				} else {
					successValue = Rnd.get(1, 100 - easeLevel * 10);
				}
				barType = 2;
				if (boostChance < 6) {
					// display as purple bar (no idea when it should be displayed)
					barType = 3;
				}
			} else {
				successValue = Rnd.get(Math.max(1, lowerBound), upperBound);
				barType = 1;
			}
		}

		currentSuccessValue += Math.min(completeValue, successValue);
		currentFailureValue += Math.min(completeValue, failValue);

		if (currentSuccessValue >= completeValue) {
			currentSuccessValue = completeValue;
		} else if (currentFailureValue >= completeValue) {
			currentFailureValue = completeValue;
		}
	}
	*/
}
