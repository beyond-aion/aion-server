package com.aionemu.gameserver.controllers;

import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.gather.Material;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.skillengine.task.GatheringTask;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.captcha.CAPTCHAUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer, sphinx, Cura
 */
public class GatherableController extends VisibleObjectController<Gatherable> {

	private int gatherCount;
	private GatheringTask gatheringTask;

	public void startGathering(Player player) {
		GatherableTemplate template = getOwner().getObjectTemplate();
		if (player.getLevel() < template.getLevelLimit()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_GATHERING_B_LEVEL_CHECK(template.getLevelLimit()));
			return;
		}
		if (player.isInPlayerMode(PlayerMode.RIDE) && !player.hasPermission(MembershipConfig.GATHERING_ALLOW_ON_MOUNT)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GATHER_RESTRICTION_RIDE());
			return;
		}
		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GATHER_INVENTORY_IS_FULL());
			return;
		}
		if (player.getController().isUnderStance()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_GATHER_WHILE_IN_CURRENT_STANCE());
			return;
		}
		if (!PositionUtil.isInRange(getOwner(), player, 3, false)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GATHER_TOO_FAR_FROM_GATHER_SOURCE());
			return;
		}
		if (!GeoService.getInstance().canSee(player, getOwner())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GATHER_OBSTACLE_EXIST());
			return;
		}
		if (player.isGatherRestricted()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CAPTCHA_REMAIN_RESTRICT_TIME(player.getGatherRestrictionDurationSeconds()));
			return;
		}

		if (!checkPlayerSkill(player, template))
			return;

		List<Material> materials = getMaterials(player, template);
		if (materials == null)
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

		synchronized (this) {
			if (gatheringTask != null) {
				// sends STR_EXTRACT_GATHER_OCCUPIED_BY_OTHER and makes the client deselect the targeted gatherable
				PacketSendUtility.sendPacket(player, new SM_GATHER_UPDATE(template, curMaterial, 0, 0, 8, 0, 0));
				return;
			}
			int skillLvlDiff = player.getSkillList().getSkillLevel(template.getHarvestSkill()) - template.getSkillLevel();
			gatheringTask = new GatheringTask(player, getOwner(), curMaterial, skillLvlDiff);
			gatheringTask.start();
		}
	}

	/**
	 * Checks whether player have needed skill for gathering and skill level is sufficient
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

	private List<Material> getMaterials(Player player, GatherableTemplate template) {
		if (template.getRequiredItemId() > 0) {
			if (template.getCheckType() == 1) {
				boolean hasRequiredItemEquipped = !player.getEquipment().getEquippedItemsByItemId(template.getRequiredItemId()).isEmpty();
				if (hasRequiredItemEquipped)
					return template.getExtraMaterials().getMaterial();
			} else if (template.getCheckType() == 2) {
				if (player.getInventory().getItemCountByItemId(template.getRequiredItemId()) < template.getEraseValue()) {
					String requiredItemL10n = ChatUtil.l10n(template.getRequiredItemNameId());
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_GATHERING_B_ITEM_CHECK(requiredItemL10n));
					return null;
				}
				return template.getExtraMaterials().getMaterial();
			}
		}
		return template.getMaterials().getMaterial();
	}

	public void completeInteraction() {
		synchronized (this) {
			gatheringTask = null;
			if (++gatherCount == getOwner().getObjectTemplate().getHarvestCount()) {
				if (getOwner().isInInstance())
					getOwner().getController().delete();
				else
					getOwner().getController().deleteAndScheduleRespawn();
			}
		}
	}

	@SuppressWarnings("lossy-conversions")
	public void rewardPlayer(Player player) {
		if (player != null) {
			int skillLvl = getOwner().getObjectTemplate().getSkillLevel();
			int xpReward = (int) ((0.0031 * (skillLvl + 5.3) * (skillLvl + 1592.8) + 60));

			int skillId = getOwner().getObjectTemplate().getHarvestSkill();
			int gainedGatherXp = Rates.SKILL_XP_GATHERING.calcResult(player, xpReward);
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

	@Override
	public void onDespawn() {
		cancelGathering();
		super.onDespawn();
	}

	public void cancelGathering() {
		synchronized (this) {
			if (gatheringTask == null)
				return;
			gatheringTask.abort();
			gatheringTask = null;
		}
	}

	public int getGatheringPlayerId() {
		synchronized (this) {
			return gatheringTask == null ? 0 : gatheringTask.getGathererId();
		}
	}
}
