package com.aionemu.gameserver.controllers;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.gather.Material;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.task.GatheringTask;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.captcha.CAPTCHAUtil;

/**
 * @author ATracer, sphinx, Cura
 */
public class GatherableController extends VisibleObjectController<Gatherable> {

	private int gatherCount;
	private AtomicInteger currentGatherer = new AtomicInteger();
	private GatheringTask task;

	/**
	 * Start gathering process
	 * 
	 * @param player
	 */
	public void onStartUse(final Player player) {
		// basic actions, need to improve here
		final GatherableTemplate template = this.getOwner().getObjectTemplate();
		if (template.getLevelLimit() > 0) {
			if (player.getLevel() < template.getLevelLimit()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_GATHERING_B_LEVEL_CHECK(template.getLevelLimit()));
				return;
			}
		}

		if (player.isInPlayerMode(PlayerMode.RIDE) && !player.hasPermission(MembershipConfig.GATHERING_ALLOW_ON_MOUNT)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GATHER_RESTRICTION_RIDE());
			return;
		}
		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GATHER_INVENTORY_IS_FULL());
			return;
		}
		if (PositionUtil.getDistance(getOwner(), player) > 6)
			return;

		if (player.isGatherRestricted()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CAPTCHA_REMAIN_RESTRICT_TIME(player.getGatherRestrictionDurationSeconds()));
			return;
		}

		if (!checkPlayerSkill(player, template))
			return;

		// check for extractor in inventory
		byte result = checkPlayerRequiredExtractor(player, template);
		if (result == 0)
			return;

		// CAPTCHA
		if (SecurityConfig.CAPTCHA_ENABLE) {
			if (SecurityConfig.CAPTCHA_APPEAR.equals(template.getSourceType()) || SecurityConfig.CAPTCHA_APPEAR.equals("ALL")) {
				int rate = SecurityConfig.CAPTCHA_APPEAR_RATE;
				if (template.getCaptchaRate() > 0)
					rate = (int) (template.getCaptchaRate() * 0.1f);

				if (Rnd.chance() < rate) {
					player.setCaptchaWord(CAPTCHAUtil.getRandomWord());
					player.setCaptchaImage(CAPTCHAUtil.createCAPTCHA(player.getCaptchaWord()).array());
					PunishmentService.setIsNotGatherable(player, 0, true, SecurityConfig.CAPTCHA_EXTRACTION_BAN_TIME * 1000L);
				}
			}
		}

		List<Material> materials = null;
		switch (result) {
			case 1: // player has equipped item, or have a consumable in inventory, so he will obtain extra items
				materials = template.getExtraMaterials().getMaterial();
				break;
			default:// regular thing
				materials = template.getMaterials().getMaterial();
				break;
		}

		int chance = Rnd.nextInt(10000000);
		int current = 0;
		Material curMaterial = null;
		for (Material mat : materials) {
			current += mat.getRate();
			if (current >= chance) {
				curMaterial = mat;
				break;
			}
		}

		int gatherer = currentGatherer.get();
		if (gatherer > 0 && getOwner().getPosition().getWorldMapInstance().getPlayer(gatherer) == null)
			currentGatherer.set(0);

		if (currentGatherer.compareAndSet(0, player.getObjectId())) {
			player.getObserveController().attach(new ActionObserver(ObserverType.ALL) {

				@Override
				public void attacked(Creature creature, int skillId) {
					finishGathering(player);
				};

				@Override
				public void moved() {
					finishGathering(player);
				}

				@Override
				public void dotattacked(Creature creature, Effect dotEffect) {
					finishGathering(player);
				}
			});
			int skillLvlDiff = player.getSkillList().getSkillLevel(template.getHarvestSkill()) - template.getSkillLevel();
			task = new GatheringTask(player, getOwner(), curMaterial, skillLvlDiff);
			task.start();
		} else {
			// dummy failure, value doesn't matter
			PacketSendUtility.sendPacket(player, new SM_GATHER_UPDATE(template, curMaterial, 0, 17, 8, 0, 0));
		}
	}

	/**
	 * Checks whether player have needed skill for gathering and skill level is sufficient
	 * 
	 * @param player
	 * @param template
	 * @return
	 */
	private boolean checkPlayerSkill(final Player player, final GatherableTemplate template) {
		int harvestSkillId = template.getHarvestSkill();
		if (!player.getSkillList().isSkillPresent(harvestSkillId)) {
			if (harvestSkillId == 30001) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GATHER_INCORRECT_SKILL());
			} else {
				PacketSendUtility.sendPacket(player,
					SM_SYSTEM_MESSAGE.STR_GATHER_LEARN_SKILL(DataManager.SKILL_DATA.getSkillTemplate(harvestSkillId).getL10n()));
			}
			return false;
		}
		if (player.getSkillList().getSkillLevel(harvestSkillId) < template.getSkillLevel()) {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_GATHER_OUT_OF_SKILL_POINT(DataManager.SKILL_DATA.getSkillTemplate(harvestSkillId).getL10n()));
			return false;
		}
		return true;
	}

	private byte checkPlayerRequiredExtractor(final Player player, final GatherableTemplate template) {
		if (template.getRequiredItemId() > 0) {
			if (template.getCheckType() == 1) {
				List<Item> items = player.getEquipment().getEquippedItemsByItemId(template.getRequiredItemId());
				boolean condOk = false;
				for (Item item : items) {
					if (item.isEquipped()) {
						condOk = true;
						break;
					}
				}
				return (byte) (condOk ? 1 : 2);

			} else if (template.getCheckType() == 2) {
				if (player.getInventory().getItemCountByItemId(template.getRequiredItemId()) < template.getEraseValue()) {
					String requiredItemL10n = ChatUtil.l10n(template.getRequiredItemNameId());
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_GATHERING_B_ITEM_CHECK(requiredItemL10n));
					return 0;
				} else
					return 1;
			}
		}

		return 2;
	}

	public void completeInteraction() {
		if (++gatherCount == getOwner().getObjectTemplate().getHarvestCount()) {
			if (!getOwner().isInInstance())
				RespawnService.scheduleRespawn(getOwner());
			getOwner().getController().delete();
			gatherCount = 0;
		}
		currentGatherer.set(0);
	}

	@SuppressWarnings("lossy-conversions")
	public void rewardPlayer(Player player) {
		if (player != null) {
			int skillLvl = getOwner().getObjectTemplate().getSkillLevel();
			int xpReward = (int) ((0.0031 * (skillLvl + 5.3) * (skillLvl + 1592.8) + 60));

			int skillId = getOwner().getObjectTemplate().getHarvestSkill();
			int gainedGatherXp = (int) Rates.SKILL_XP_GATHERING.calcResult(player, xpReward);
			StatEnum boostStat = StatEnum.getModifier(skillId);
			if (boostStat != null)
				gainedGatherXp *= player.getGameStats().getStat(boostStat, 100).getCurrent() / 100f;
			gainedGatherXp = Math.max(1, gainedGatherXp);

			if (player.getSkillList().addSkillXp(player, skillId, gainedGatherXp, skillLvl)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHERING_SUCCESS_GETEXP());
				player.getCommonData().addExp(xpReward, Rates.XP_GATHERING);
			} else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE
					.STR_MSG_DONT_GET_PRODUCTION_EXP(DataManager.SKILL_DATA.getSkillTemplate(skillId).getL10n()));
		}
	}

	/**
	 * Called by client when some action is performed or on finish gathering Called by move observer on player move
	 * 
	 * @param player
	 */
	public void finishGathering(Player player) {
		if (currentGatherer.compareAndSet(player.getObjectId(), 0))
			task.abort();
	}

}
