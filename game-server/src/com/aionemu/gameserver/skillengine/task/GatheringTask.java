package com.aionemu.gameserver.skillengine.task;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.gather.Material;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_UPDATE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, Yeats
 */
public class GatheringTask extends AbstractCraftTask {

	private final GatherableTemplate template;
	private final ActionObserver gathererObserver;
	private final Material material;
	private int showBarDelay;
	private int executionSpeed;

	public GatheringTask(Player requester, Gatherable gatherable, Material material, int skillLvlDiff) {
		super(requester, gatherable, skillLvlDiff);
		this.template = gatherable.getObjectTemplate();
		this.gathererObserver = createGathererObserver();
		this.material = material;
		this.delay = Rnd.get(200, 600);
		int gatherInterval = 2500 - (skillLvlDiff * 60);
		this.interval = gatherInterval < 1200 ? 1200 : gatherInterval;
	}

	@Override
	protected void onInteractionAbort() {
		PacketSendUtility.broadcastPacket(requester, new SM_GATHER_ANIMATION(requester.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 4));
		PacketSendUtility.sendPacket(requester, new SM_GATHER_UPDATE(template, material, 0, 0, 5, 0, 0));
	}

	@Override
	protected void onInteractionFinish() {
		requester.getObserveController().removeObserver(gathererObserver);
		((Gatherable) responder).getController().completeInteraction();
	}

	@Override
	protected void onInteractionStart() {
		requester.getObserveController().attach(gathererObserver);
		PacketSendUtility.sendPacket(requester, new SM_GATHER_UPDATE(template, material, fullBarValue, fullBarValue, 0, 0, 0));
		PacketSendUtility.sendPacket(requester, new SM_GATHER_UPDATE(template, material, 0, 0, 1, 0, 0));
		// TODO: missing packet for initial failure/success
		PacketSendUtility.broadcastPacket(requester, new SM_GATHER_ANIMATION(requester.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 0), true);
		PacketSendUtility.broadcastPacket(requester, new SM_GATHER_ANIMATION(requester.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 1), true);
	}

	@Override
	protected void sendInteractionUpdate() {
		PacketSendUtility.sendPacket(requester, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, craftType.getProgressId(), executionSpeed, showBarDelay));
	}

	@Override
	protected void onFailureFinish() {
		PacketSendUtility.sendPacket(requester, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 1, 0, 0));
		PacketSendUtility.sendPacket(requester, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 7, 0, 0));
		PacketSendUtility.broadcastPacket(requester, new SM_GATHER_ANIMATION(requester.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 3), true);
	}

	@Override
	protected boolean onSuccessFinish() {
		PacketSendUtility.broadcastPacket(requester, new SM_GATHER_ANIMATION(requester.getObjectId(), responder.getObjectId(), template.getHarvestSkill(), 2), true);
		PacketSendUtility.sendPacket(requester, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 6, 0, 0));
		if (template.getEraseValue() > 0)
			requester.getInventory().decreaseByItemId(template.getRequiredItemId(), template.getEraseValue());
		ItemService.addItem(requester, material.getItemId(), Rates.GATHERING_COUNT.calcResult(requester, 1));
		requester.getPosition().getWorldMapInstance().getInstanceHandler().onGather(requester, (Gatherable) responder);
		((Gatherable) responder).getController().rewardPlayer(requester);
		return true;
	}

	@Override
	protected final void analyzeInteraction() {
		if (skillLvlDiff >= 41) {
			currentSuccessValue = fullBarValue;
			executionSpeed = 300;
			showBarDelay = 500;
			return;
		} else if (skillLvlDiff < 0) {
			currentFailureValue = fullBarValue;
			return;
		}

		craftType = CraftType.NORMAL;
		float multi = Rnd.nextFloat(1f, 2f);
		float failReduction = Math.max(1 - skillLvlDiff * 0.015f, 0.25f); // dynamic fail rate multiplier
		boolean success = Rnd.chance() >= CraftConfig.MAX_GATHER_FAILURE_CHANCE * failReduction;

		if (success) {
			float critChance = Rnd.chance();
			if (critChance < (1 + skillLvlDiff / 10f)) { // PURPLE CRIT = 100%
				craftType = CraftType.CRIT_PURPLE;
				currentSuccessValue = fullBarValue;
				executionSpeed = 300;
				showBarDelay = 500;
				return;
			} else if (critChance < (5 + skillLvlDiff / 3f)) { // LIGHT BLUE CRIT = +10%
				craftType = CraftType.CRIT_BLUE;
			}

			int lvlBoni = skillLvlDiff > 10 ? ((skillLvlDiff - 10) * 2) : 0;
			currentSuccessValue += Math.round(70 + ((craftType == CraftType.CRIT_BLUE ? 100 : 0) + (((skillLvlDiff + 1) / 2f) + lvlBoni) * 10) * multi);
		} else {
			currentFailureValue += Math.round(120 + (((skillLvlDiff + 1) / 2f * 10) * multi));
		}

		if (currentSuccessValue > fullBarValue) {
			currentSuccessValue = fullBarValue;
		} else if (currentFailureValue > fullBarValue) {
			currentFailureValue = fullBarValue;
		}

		int speed = 900 - (skillLvlDiff * 30);
		executionSpeed = speed < 300 ? 300 : speed;
		showBarDelay = Math.max(500, 1200 - (skillLvlDiff * 30));
	}

	public int getGathererId() {
		return requester.getObjectId();
	}

	private ActionObserver createGathererObserver() {
		return new ActionObserver(ObserverType.ALL) {
			@Override
			public void startSkillCast(Skill skill) {
				abort();
			}

			@Override
			public void attack(Creature creature, int skillId) {
				abort();
			}

			@Override
			public void attacked(Creature creature, int skillId) {
				abort();
			}

			@Override
			public void moved() {
				abort();
			}

			@Override
			public void dotattacked(Creature creature, Effect dotEffect) {
				abort();
			}
		};
	}
}
