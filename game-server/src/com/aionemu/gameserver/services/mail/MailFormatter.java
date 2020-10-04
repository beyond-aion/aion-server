package com.aionemu.gameserver.services.mail;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.templates.mail.MailPart;
import com.aionemu.gameserver.model.templates.mail.MailTemplate;

/**
 * @author Rolandas
 */
public final class MailFormatter {

	public static void sendBlackCloudMail(String recipientName, int itemObjectId, int itemCount) {
		MailTemplate template = DataManager.SYSTEM_MAIL_TEMPLATES.getMailTemplate("$$CASH_ITEM_MAIL", "", Race.PC_ALL);

		MailPart formatter = new MailPart() {

			@Override
			public String getParamValue(String name) {
				if ("itemid".equals(name))
					return Integer.toString(itemObjectId);
				else if ("count".equals(name))
					return Integer.toString(itemCount);
				else if ("unk1".equals(name))
					return "0";
				else if ("purchasedate".equals(name))
					return Long.toString(System.currentTimeMillis() / 1000);
				return "";
			}

		};

		String title = template.getFormattedTitle(formatter);
		String body = template.getFormattedMessage(formatter);

		SystemMailService.sendMail("$$CASH_ITEM_MAIL", recipientName, title, body, itemObjectId, itemCount, 0, LetterType.BLACKCLOUD);
	}

	public static void sendHouseMaintenanceMail(House ownedHouse, String ownerName, long impoundTimeMillis, long kinah) {
		String templateName;
		long daysUntilImpoundment = Duration.ofMillis(impoundTimeMillis - System.currentTimeMillis()).toDays();
		if (daysUntilImpoundment <= 0)
			templateName = "$$HS_OVERDUE_3RD";
		else if (daysUntilImpoundment <= 7)
			templateName = "$$HS_OVERDUE_2ND";
		else if (daysUntilImpoundment <= 14)
			templateName = "$$HS_OVERDUE_1ST";
		else
			return;

		MailTemplate template = DataManager.SYSTEM_MAIL_TEMPLATES.getMailTemplate(templateName, "", Race.PC_ALL);

		MailPart formatter = new MailPart() {

			@Override
			public String getParamValue(String name) {
				if ("address".equals(name))
					return Integer.toString(ownedHouse.getAddress().getId());
				else if ("datetime".equals(name))
					return Long.toString(impoundTimeMillis / 60000);
				return "";
			}

		};

		String title = template.getFormattedTitle(null);
		String message = template.getFormattedMessage(formatter);

		SystemMailService.sendMail(templateName, ownerName, title, message, 0, 0, kinah, LetterType.NORMAL);
	}

	public static void sendHouseAuctionMail(House ownedHouse, PlayerCommonData playerData, AuctionResult result, long time, long returnKinah) {
		MailTemplate template = DataManager.SYSTEM_MAIL_TEMPLATES.getMailTemplate("$$HS_AUCTION_MAIL", "", playerData.getRace());
		if (ownedHouse == null || result == null)
			return;

		MailPart formatter = new MailPart() {

			@Override
			public String getParamValue(String name) {
				if ("address".equals(name))
					return Integer.toString(ownedHouse.getAddress().getId());
				else if ("datetime".equals(name))
					return Long.toString(time / 1000);
				else if ("resultid".equals(name))
					return Integer.toString(result.getId());
				else if ("raceid".equals(name))
					return Integer.toString(playerData.getRace().getRaceId());
				return "";
			}
		};

		String title = template.getFormattedTitle(formatter);
		String message = template.getFormattedMessage(formatter);

		SystemMailService.sendMail("$$HS_AUCTION_MAIL", playerData.getName(), title, message, 0, 0, returnKinah, LetterType.NORMAL);
	}

	public static void sendAbyssRewardMail(SiegeLocation siegeLocation, PlayerCommonData playerData, AbyssSiegeLevel level, SiegeResult result,
		long time, int attachedItemObjId, long attachedItemCount, long attachedKinahCount) {

		MailTemplate template = DataManager.SYSTEM_MAIL_TEMPLATES.getMailTemplate("$$ABYSS_REWARD_MAIL", "", playerData.getRace());

		MailPart formatter = new MailPart() {

			@Override
			public String getParamValue(String name) {
				if ("siegelocid".equals(name))
					return Integer.toString(siegeLocation.getTemplate().getId());
				else if ("datetime".equals(name))
					return Long.toString(time / 1000);
				else if ("rankid".equals(name))
					return Integer.toString(level.getId());
				else if ("raceid".equals(name))
					return Integer.toString(playerData.getRace().getRaceId());
				else if ("resultid".equals(name))
					return Integer.toString(result.getId());
				return "";
			}
		};

		String title = template.getFormattedTitle(formatter);
		String message = template.getFormattedMessage(formatter);

		SystemMailService.sendMail("$$ABYSS_REWARD_MAIL", playerData.getName(), title, message, attachedItemObjId, attachedItemCount, attachedKinahCount,
			LetterType.NORMAL);
	}

	public static void sendGuildDominionRewardMail(Player player, int territorialId, Timestamp participantDate, int itemId, int itemCount) {
		MailTemplate template = DataManager.SYSTEM_MAIL_TEMPLATES.getMailTemplate("$$GD_REWARD_MAIL", "", player.getRace());
		LocalDateTime participationDate = participantDate.toLocalDateTime();
		MailPart formatter = new MailPart() {

			@Override
			public String getParamValue(String name) {
				String val = "";
				if ("month".equals(name)) {
					val = Integer.toString(participationDate.getMonthValue());
				} else if ("day".equals(name)) {
					val = Integer.toString(participationDate.getDayOfMonth());
				} else if ("territorial".equals(name)) {
					val = Integer.toString(territorialId);
				} else if ("legionName".equals(name)) {
					val = player.getLegion() == null ? "" : player.getLegion().getName();
				}
				return val;
			}
		};

		String title = template.getFormattedTitle(formatter);
		String body = template.getFormattedMessage(formatter);

		SystemMailService.sendMail("$$GD_REWARD_MAIL", player.getName(), title, body, itemId, itemCount, 0, LetterType.NORMAL);
	}

	public static void sendCustomAbyssDefeatRewardMail(PlayerCommonData playerCommonData, int itemId, int itemCount) {
		SystemMailService.sendMail(playerCommonData.getRace() == Race.ELYOS ? "%NPC:203700" : "%NPC:204052", // Fasimedes, Vidar
				playerCommonData.getName(), "$901513", // Reward Statement
				"", itemId, itemCount, 0, LetterType.NORMAL);
	}

}
