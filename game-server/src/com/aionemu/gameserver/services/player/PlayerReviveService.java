package com.aionemu.gameserver.services.player;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.templates.item.ItemUseLimits;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.services.panesterra.PanesterraService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Jego, xTz
 */
public class PlayerReviveService {

	public static void duelRevive(Player player) {
		revive(player, 30, 30, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		player.unsetResPosState();
	}

	public static void skillRevive(Player player) {
		if (!player.getResStatus()) {
			AuditLogger.log(player, "possibly tried to use a selfres hack (accepted missing res by another player)");
			return;
		}
		revive(player, 35, 35, true, player.getResurrectionSkill());
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		// if player was flying before res, start flying
		if (player.getIsFlyingBeforeDeath()) {
			player.getFlyController().startFly(true, true);
		} else {
			player.getGameStats().updateStatsAndSpeedVisually();
		}

		if (player.isInPrison())
			TeleportService.teleportToPrison(player);
		else if (player.isInResPostState())
			TeleportService.teleportTo(player, player.getWorldId(), player.getInstanceId(), player.getResPosX(), player.getResPosY(), player.getResPosZ());
		player.unsetResPosState();
		player.setIsFlyingBeforeDeath(false);
	}

	public static void rebirthRevive(Player player) {
		if (!player.canUseRebirthRevive()) {
			AuditLogger.log(player, "possibly tried to use a selfres hack (no rebirth effect present)");
			return;
		}

		boolean soulSickness = true;
		int rebirthResurrectPercent, rebirthSkillId;
		if (player.hasAccess(AdminConfig.AUTO_RES)) {
			rebirthSkillId = 0;
			rebirthResurrectPercent = 100;
			soulSickness = false;
		} else {
			rebirthSkillId = player.getRebirthEffect().getSkillId();
			rebirthResurrectPercent = player.getRebirthEffect().getResurrectPercent();
			if (rebirthResurrectPercent <= 0) {
				LoggerFactory.getLogger(PlayerReviveService.class).warn("Rebirth effect missing percent.");
				rebirthResurrectPercent = 5;
			}
		}

		revive(player, rebirthResurrectPercent, rebirthResurrectPercent, soulSickness, rebirthSkillId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		// if player was flying before res, start flying
		if (player.getIsFlyingBeforeDeath()) {
			player.getFlyController().startFly(true, true);
		} else {
			player.getGameStats().updateStatsAndSpeedVisually();
		}

		if (player.isInPrison())
			TeleportService.teleportToPrison(player);
		player.unsetResPosState();
		player.setIsFlyingBeforeDeath(false);
	}

	public static void bindRevive(Player player) {
		bindRevive(player, 0);
	}

	public static void bindRevive(Player player, int skillId) {
		if (player.isInCustomState(CustomPlayerState.EVENT_MODE))
			revive(player, 100, 100, false, skillId);
		else
			revive(player, 25, 25, true, skillId);
		if (skillId > 0)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		player.getGameStats().updateStatsAndSpeedVisually();
		if (player.isInPrison()) {
			TeleportService.teleportToPrison(player);
		} else if (player.isInCustomState(CustomPlayerState.EVENT_MODE)) {
			TeleportService.teleportToEvent(player);
		} else if (WorldMapType.getWorld(player.getWorldId()) == WorldMapType.BELUS) {
			PanesterraService.getInstance().reviveInEventLocation(player);
		} else if (!PanesterraService.getInstance().teleportToStartPosition(player)) {
			WorldPosition resPos = null;
			for (VortexLocation loc : VortexService.getInstance().getVortexLocations().values()) {
				if (loc.isInsideActiveVotrex(player) && player.getRace().equals(loc.getInvadersRace())) {
					resPos = loc.getResurrectionPoint();
					break;
				}
			}

			if (resPos != null)
				TeleportService.teleportTo(player, resPos);
			else
				TeleportService.moveToBindLocation(player);
		}
		player.unsetResPosState();
	}

	public static void kiskRevive(Player player) {
		kiskRevive(player, 0);
	}

	public static void kiskRevive(Player player, int skillId) {
		if (player.isInPrison())
			TeleportService.teleportToPrison(player);
		else if (player.isInCustomState(CustomPlayerState.EVENT_MODE))
			TeleportService.teleportToEvent(player);

		Kisk kisk = player.getKisk();
		if (kisk != null && kisk.isActive()) {
			kisk.resurrectionUsed();
			if (skillId > 0)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
			revive(player, 30, 30, false, skillId);
			player.getGameStats().updateStatsAndSpeedVisually();
			player.unsetResPosState();
			TeleportService.teleportTo(player, kisk.getPosition());
		}
	}

	public static void instanceRevive(Player player) {
		instanceRevive(player, 0);
	}

	public static void instanceRevive(Player player, int skillId) {
		if (player.isInCustomState(CustomPlayerState.EVENT_MODE)) {
			revive(player, 100, 100, false, skillId);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
			player.getGameStats().updateStatsAndSpeedVisually();
			TeleportService.teleportToEvent(player);
			return;
		}
		if (player.getPosition().getWorldMapInstance().getInstanceHandler().onReviveEvent(player))
			return;
		WorldMap map = World.getInstance().getWorldMap(player.getWorldId());
		if (map == null) {
			bindRevive(player);
			return;
		}
		revive(player, 25, 25, true, skillId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player));
		PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
		if (map.isInstanceType() && player.getPosition().getWorldMapInstance().getStartPos() != null) {
			WorldPosition pos = player.getPosition().getWorldMapInstance().getStartPos();
			TeleportService.teleportTo(player, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ());
		} else
			bindRevive(player);
		player.unsetResPosState();
	}

	public static void revive(Player player, int hpPercent, int mpPercent, boolean setSoulSickness, int resurrectionSkill) {
		player.getKnownList().forEachPlayer(p -> {
			if (player.equals(p.getTarget())) {
				p.setTarget(null);
				PacketSendUtility.sendPacket(p, new SM_TARGET_SELECTED(null));
			}
		});
		boolean isNoResurrectPenalty = player.getEffectController().hasAbnormalEffect(Effect::isNoResurrectPenalty);
		player.setPlayerResActivate(false);
		player.getLifeStats().setCurrentHpPercent(isNoResurrectPenalty ? 100 : hpPercent);
		player.getLifeStats().setCurrentMpPercent(isNoResurrectPenalty ? 100 : mpPercent);
		if (player.getCommonData().getDp() > 0 && !isNoResurrectPenalty)
			player.getCommonData().setDp(0);
		if (!isNoResurrectPenalty && setSoulSickness) {
			player.getController().updateSoulSickness(resurrectionSkill);
		}
		player.setResurrectionSkill(0);
		player.getAggroList().clear();
		player.getController().onBeforeSpawn();
		if (player.isInGroup()) {
			PlayerGroupService.updateGroup(player, GroupEvent.MOVEMENT);
		}
		if (player.isInAlliance()) {
			PlayerAllianceService.updateAlliance(player, PlayerAllianceEvent.MOVEMENT);
		}
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
	}

	public static void itemSelfRevive(Player player) {
		Item item = player.getSelfRezStone();
		if (item == null) {
			AuditLogger.log(player, "tried to use selfres without having the required selfres stone");
			return;
		}

		// Add Cooldown and use item
		ItemUseLimits useLimits = item.getItemTemplate().getUseLimits();
		int useDelay = useLimits.getDelayTime();
		player.addItemCoolDown(useLimits.getDelayId(), System.currentTimeMillis() + useDelay, useDelay / 1000);
		player.getController().cancelUseItem();
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemTemplate().getTemplateId()), true);
		if (!player.getInventory().decreaseByObjectId(item.getObjectId(), 1)) {
			AuditLogger.log(player, "tried to use selfres without having the required selfres stone");
			return;
		}
		// Tombstone Self-Rez retail verified 15%
		revive(player, 15, 15, true, player.getResurrectionSkill());
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		// if player was flying before res, start flying
		if (player.getIsFlyingBeforeDeath()) {
			player.getFlyController().startFly(true, true);
		} else {
			player.getGameStats().updateStatsAndSpeedVisually();
		}

		if (player.isInPrison())
			TeleportService.teleportToPrison(player);
		player.unsetResPosState();
		player.setIsFlyingBeforeDeath(false);

	}

	public static void scheduleReviveAtBase(Player player, int delayMillis, int skillId) {
		player.getController().addTask(TaskId.TELEPORT, ThreadPoolManager.getInstance().schedule(() -> {
			player.getController().getAndRemoveTask(TaskId.TELEPORT); // remove manually as it won't get removed automatically
			if (player.isInInstance())
				PlayerReviveService.instanceRevive(player, skillId);
			else if (player.getKisk() != null)
				PlayerReviveService.kiskRevive(player, skillId);
			else
				PlayerReviveService.bindRevive(player, skillId);
		}, delayMillis));
	}
}
