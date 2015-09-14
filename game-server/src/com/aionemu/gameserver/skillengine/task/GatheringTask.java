package com.aionemu.gameserver.skillengine.task;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.gather.Material;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class GatheringTask extends AbstractCraftTask {

	private GatherableTemplate template;
	private Material material;
	private int barType = 1;

	public GatheringTask(Player requestor, Gatherable gatherable, Material material, int skillLvlDiff) {
		super(requestor, gatherable, skillLvlDiff);
		this.template = gatherable.getObjectTemplate();
		this.material = material;
		this.delay = Rnd.get(200, 600);
		this.interval = 1850;
	}

	@Override
	protected void onInteractionAbort() {
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 4));
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, 0, 0, 5));
	}

	@Override
	protected void onInteractionFinish() {
		((Gatherable) responder).getController().completeInteraction();
	}

	@Override
	protected void onInteractionStart() {
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, 0, 0, 0));
		// TODO: missing packet for initial failure/success
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 0), true);
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 1), true);
	}

	@Override
	protected void sendInteractionUpdate() {
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, barType));
	}

	@Override
	protected void onFailureFinish() {
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 1));
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 7));
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 3), true);
	}

	@Override
	protected boolean onSuccessFinish() {
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 2), true);
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 6));
		PacketSendUtility.sendPacket(requestor, SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_SUCCESS_1_BASIC(new DescriptionId(material.getNameid())));
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
}
