package com.aionemu.gameserver.services.player;

import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.EmotionType;
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
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Jego, xTz
 */
public class PlayerReviveService {

	public static final void duelRevive(Player player) {
		revive(player, 30, 30, false, 0);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
		player.getGameStats().updateStatsAndSpeedVisually();
		player.unsetResPosState();
	}

	public static final void skillRevive(Player player) {
		if (!(player.getResStatus())) {
			cancelRes(player);
			return;
		}
		revive(player, 10, 10, true, player.getResurrectionSkill());
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
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

	public static final void rebirthRevive(Player player) {
		if (!player.canUseRebirthRevive()) {
			return;
		}

		boolean soulSickness = true;
		int rebirthResurrectPercent = player.getRebirthResurrectPercent();
		if (player.getAccessLevel() >= AdminConfig.ADMIN_AUTO_RES) {
			rebirthResurrectPercent = 100;
			soulSickness = false;
		} else if (rebirthResurrectPercent <= 0) {
			LoggerFactory.getLogger(PlayerReviveService.class).warn("Rebirth effect missing percent.");
			rebirthResurrectPercent = 5;
		}

		revive(player, rebirthResurrectPercent, rebirthResurrectPercent, soulSickness, player.getRebirthSkill());
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
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

	public static final void bindRevive(Player player) {
		bindRevive(player, 0);
	}

	public static final void bindRevive(Player player, int skillId) {
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
		} else {
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

	public static final void kiskRevive(Player player) {
		kiskRevive(player, 0);
	}

	public static final void kiskRevive(Player player, int skillId) {

		if (player.isInPrison())
			TeleportService.teleportToPrison(player);
		else if (player.isInCustomState(CustomPlayerState.EVENT_MODE))
			TeleportService.teleportToEvent(player);

		// TODO: find right place for this
		if (player.getSKInfo().getRank() > 1) {
			bindRevive(player);
			return;
		}

		Kisk kisk = player.getKisk();
		if (kisk != null && kisk.isActive()) {
			kisk.resurrectionUsed();
			if (skillId > 0)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
			revive(player, 25, 25, false, skillId);
			player.getGameStats().updateStatsAndSpeedVisually();
			player.unsetResPosState();
			TeleportService.teleportTo(player, kisk.getPosition());
		}
	}

	public static final void instanceRevive(Player player) {
		instanceRevive(player, 0);
	}

	public static final void instanceRevive(Player player, int skillId) {
		if (player.isInCustomState(CustomPlayerState.EVENT_MODE)) {
			revive(player, 100, 100, false, skillId);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
			player.getGameStats().updateStatsAndSpeedVisually();
			TeleportService.teleportToEvent(player);
			return;
		}
		if (player.getPanesterraTeam() != null)
			if (AhserionRaid.getInstance().revivePlayer(player, skillId))
				return;
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
			float[] coords = player.getPosition().getWorldMapInstance().getStartPos();
			TeleportService.teleportTo(player, player.getWorldId(), coords[0], coords[1], coords[2]);
		} else
			bindRevive(player);
		player.unsetResPosState();
	}

	public static final void revive(final Player player, int hpPercent, int mpPercent, boolean setSoulsickness, int resurrectionSkill) {
		player.getKnownList().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player visitor) {
				if (player.equals(visitor.getTarget())) {
					visitor.setTarget(null);
					PacketSendUtility.sendPacket(visitor, new SM_TARGET_SELECTED(null));
				}
			}
		});
		boolean isNoResurrectPenalty = player.getController().isNoResurrectPenaltyInEffect();
		player.setPlayerResActivate(false);
		player.getLifeStats().setCurrentHpPercent(isNoResurrectPenalty ? 100 : hpPercent);
		player.getLifeStats().setCurrentMpPercent(isNoResurrectPenalty ? 100 : mpPercent);
		if (player.getCommonData().getDp() > 0 && !isNoResurrectPenalty)
			player.getCommonData().setDp(0);
		player.getLifeStats().triggerRestoreOnRevive();
		if (!isNoResurrectPenalty && setSoulsickness) {
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
	}

	public static final void itemSelfRevive(Player player) {
		Item item = player.getSelfRezStone();
		if (item == null) {
			cancelRes(player);
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
			cancelRes(player);
			return;
		}
		// Tombstone Self-Rez retail verified 15%
		revive(player, 15, 15, true, player.getResurrectionSkill());
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
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

	private static final void cancelRes(Player player) {
		AuditLogger.info(player, "Possible selfres hack.");
		player.getController().sendDie();
	}
}
