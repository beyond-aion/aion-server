package com.aionemu.gameserver.services;

import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.dao.PlayerPunishmentsDAO;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CAPTCHA;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.ban.ChatBanService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author lord_rex, Cura, nrg
 */
public class PunishmentService {

	/**
	 * This method will handle unbanning a character
	 * 
	 * @param player
	 * @param state
	 * @param delayInMinutes
	 */
	public static void unbanChar(int playerId) {
		PlayerPunishmentsDAO.unpunishPlayer(playerId, PunishmentType.CHARBAN);
	}

	/**
	 * This method will handle banning a character
	 * 
	 * @param player
	 * @param state
	 * @param delayInMinutes
	 */
	public static void banChar(int playerId, int dayCount, String reason) {
		PlayerPunishmentsDAO.punishPlayer(playerId, PunishmentType.CHARBAN, calculateDuration(dayCount), reason);

		// if player is online - kick him
		Player player = World.getInstance().getPlayer(playerId);
		if (player != null)
			player.getClientConnection().close(new SM_QUIT_RESPONSE());
	}

	/**
	 * Calculates the timestamp when a given number of days is over
	 * 
	 * @param dayCount
	 * @return ban duration in seconds
	 */
	public static long calculateDuration(int dayCount) {
		if (dayCount == 0)
			return Integer.MAX_VALUE; // int because client handles this with seconds timestamp in int
		return TimeUnit.DAYS.toSeconds(dayCount);
	}

	/**
	 * This method will handle moving or removing a player from prison
	 * 
	 * @param player
	 * @param state
	 * @param delayInMinutes
	 */
	public static void setIsInPrison(Player player, boolean state, long delayInMinutes, String reason) {
		if (state) {
			if (delayInMinutes > 0) {
				long duration = TimeUnit.MINUTES.toMillis(delayInMinutes);
				schedulePrisonTask(player, duration);
				ChatBanService.banPlayer(player, delayInMinutes);
				player.setPrisonEndTimeMillis(System.currentTimeMillis() + duration);
				TeleportService.teleportToPrison(player);
				PlayerPunishmentsDAO.punishPlayer(player, PunishmentType.PRISON, reason);
				PacketSendUtility.sendMessage(player, "You have been teleported to prison for a time of " + delayInMinutes
					+ " minutes.\n If you disconnect the time stops and the timer of the prison'll see at your next login.");
			}
		} else {
			player.getController().cancelTask(TaskId.PRISON);
			player.setPrisonEndTimeMillis(0);
			ChatBanService.unbanPlayer(player);
			TeleportService.moveToBindLocation(player);
			PlayerPunishmentsDAO.unpunishPlayer(player.getObjectId(), PunishmentType.PRISON);
			PacketSendUtility.sendMessage(player, "You come out of prison.");
		}
	}

	/**
	 * This method will update the prison status
	 * 
	 * @param player
	 */
	public static void updatePrisonStatus(Player player) {
		int prisonDurationSeconds = player.getPrisonDurationSeconds();
		if (prisonDurationSeconds > 0) {
			schedulePrisonTask(player, TimeUnit.SECONDS.toMillis(prisonDurationSeconds));
			int remainingMinutes = prisonDurationSeconds / 60;
			if (remainingMinutes <= 0)
				remainingMinutes = 1;

			ChatBanService.banPlayer(player, remainingMinutes);
			PacketSendUtility.sendMessage(player, "You are still in prison for " + remainingMinutes + " minute" + (remainingMinutes > 1 ? "s" : "") + ".");

			if (player.getWorldId() != WorldMapType.DF_PRISON.getId() && player.getWorldId() != WorldMapType.LF_PRISON.getId()) {
				PacketSendUtility.sendMessage(player, "You will be teleported to prison in a moment!");
				ThreadPoolManager.getInstance().schedule(() -> TeleportService.teleportToPrison(player), 10000);
			}
		}
	}

	/**
	 * This method will schedule a prison task
	 * 
	 * @param player
	 * @param prisonTimer
	 */
	private static void schedulePrisonTask(Player player, long prisonTimer) {
		player.getController().addTask(TaskId.PRISON, ThreadPoolManager.getInstance().schedule(() -> setIsInPrison(player, false, 0, ""), prisonTimer));
	}

	/**
	 * This method will handle can or cant gathering
	 * 
	 * @param player
	 * @param captchaCount
	 * @param state
	 * @param delay
	 * @author Cura
	 */
	public static void setIsNotGatherable(Player player, int captchaCount, boolean state, long delay) {
		if (state) {
			if (captchaCount < 3) {
				PacketSendUtility.sendPacket(player, new SM_CAPTCHA(captchaCount + 1, player.getCaptchaImage()));
			} else {
				player.setCaptchaWord(null);
				player.setCaptchaImage(null);
			}
			player.setGatherRestrictionExpirationTime(System.currentTimeMillis() + delay);
			PlayerPunishmentsDAO.punishPlayer(player, PunishmentType.GATHER, "Possible gatherbot");
		} else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CAPTCHA_RECOVERED());
			player.setCaptchaWord(null);
			player.setCaptchaImage(null);
			player.setGatherRestrictionExpirationTime(0);
			PlayerPunishmentsDAO.unpunishPlayer(player.getObjectId(), PunishmentType.GATHER);
		}
	}

	/**
	 * PunishmentType
	 * 
	 * @author Cura
	 */
	public enum PunishmentType {
		PRISON,
		GATHER,
		CHARBAN
	}
}
