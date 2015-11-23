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
 * @rework Yeats
 */
public class GatheringTask extends AbstractCraftTask {

	private GatherableTemplate template;
	private Material material;
	private int showBarDelay = 1000;
	private int executionSpeed = 900;

	public GatheringTask(Player requestor, Gatherable gatherable, Material material, int skillLvlDiff) {
		super(requestor, gatherable, skillLvlDiff);
		this.template = gatherable.getObjectTemplate();
		this.material = material;
		this.delay = Rnd.get(200, 600);
		int gatherInterval = 2500 - (skillLvlDiff * 60);
		this.interval = gatherInterval < 1200 ? 1200 : gatherInterval;
	}

	@Override
	protected void onInteractionAbort() {
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
	}

	@Override
	protected void sendInteractionUpdate() {
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, craftType.getCritId(), executionSpeed, showBarDelay));
	}

	@Override
	protected void onFailureFinish() {
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 1, 0, 0));
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 7, 0, 0));
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 3), true);
	}

	@Override
	protected boolean onSuccessFinish() {
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 2), true);
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 6, 0, 0));
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
		
		float multi = Rnd.get() + 1f;
		boolean success = Rnd.get(1, 100) > (CraftConfig.MAX_GATHER_FAILURE_CHANCE - skillLvlDiff/3);
		
		if (success) {
			int critChance = Rnd.get(1, 100);
			if (critChance <= (3 + skillLvlDiff/10)) { //PURPLE CRIT = 100%
				craftType = CraftType.CRIT_PURPLE;
				currentSuccessValue = completeValue;
				this.executionSpeed = 300;
				this.showBarDelay = 500;
				return;
			} else if (critChance <= (15 + skillLvlDiff/3)) { //LIGHT BLUE CRIT = +10%
				craftType = CraftType.CRIT_BLUE;
			} else {
				craftType = CraftType.NORMAL;
			}
		} else {
			craftType = CraftType.NORMAL;
		}
		
		if (success) {
			int lvlBoni = skillLvlDiff > 10 ? ((skillLvlDiff - 10) * 2) : 0;
			currentSuccessValue += Math.round((70 + ((craftType == CraftType.CRIT_BLUE ? 100 :0) + ((skillLvlDiff/3) + (skillLvlDiff/5) + lvlBoni) * 10) * multi)); //minValue = 70
		} else {
			currentFailureValue += Math.round((140 + ((skillLvlDiff/2 * 10) * multi))); //minFailValue = 140
		}
		
		if (currentSuccessValue > completeValue) {
			currentSuccessValue = completeValue;
		} else if (currentFailureValue > completeValue) {
			currentFailureValue = completeValue;
		}
		
		int speed = 900 - (skillLvlDiff * 30);
		executionSpeed = speed < 300 ? 300 : speed;
		int showDelay = 1200 - (skillLvlDiff * 30);
		showBarDelay = showDelay < 500 ? 500 : showDelay;
		
	}
}
