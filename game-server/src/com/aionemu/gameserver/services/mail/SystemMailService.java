package com.aionemu.gameserver.services.mail;

import java.sql.Timestamp;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Letter;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MAIL_SERVICE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.player.PlayerMailboxState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * @author xTz
 */
public class SystemMailService {

	private static final Logger log = LoggerFactory.getLogger("SYSMAIL_LOG");

	public static final SystemMailService getInstance() {
		return SingletonHolder.instance;
	}

	private SystemMailService() {
		log.info("SystemMailService: Initialized.");
	}

	/**
	 * @param sender
	 * @param recipientName
	 * @param title
	 * @param message
	 * @param attachedItemObjId
	 * @param attachedItemCount
	 * @param attachedKinahCount
	 * @param express
	 */
	public boolean sendMail(String sender, String recipientName, String title, String message, int attachedItemObjId, long attachedItemCount,
		long attachedKinahCount, LetterType letterType) {

		if (attachedItemObjId != 0) {
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(attachedItemObjId);
			if (itemTemplate == null) {
				log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] RETURN ITEM ID:" + itemTemplate
					+ " ITEM COUNT " + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " ITEM TEMPLATE IS MISSING ");
				return false;
			}
		}

		if (attachedItemCount == 0 && attachedItemObjId != 0)
			return false;

		if (recipientName.length() > 16) {
			log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] ITEM RETURN" + attachedItemObjId
				+ " ITEM COUNT " + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " RECIPIENT NAME LENGTH > 16 ");
			return false;
		}

		if (!sender.startsWith("$$") && sender.length() > 16) {
			log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] ITEM RETURN" + attachedItemObjId
				+ " ITEM COUNT " + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " SENDER NAME LENGTH > 16 ");
			return false;
		}

		if (title.length() > 20)
			title = title.substring(0, 20);

		if (message.length() > 1000)
			message = message.substring(0, 1000);

		PlayerCommonData recipientCommonData = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonDataByName(recipientName);

		if (recipientCommonData == null) {
			log.info("[SYSMAILSERVICE] > [RecipientName: " + recipientName + "] NO SUCH CHARACTER NAME.");
			return false;
		}

		Player recipient = World.getInstance().findPlayer(recipientCommonData.getPlayerObjId());
		if (recipient != null) {
			if (recipient.getMailbox() != null && !(recipient.getMailbox().size() < 200)) {
				log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientCommonData.getName() + "] ITEM RETURN"
					+ attachedItemObjId + " ITEM COUNT " + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " MAILBOX FULL ");
				return false;
			}
		} else if (recipientCommonData.getMailboxLetters() > 199) {
			return false;
		}
		Item attachedItem = null;
		long finalAttachedKinahCount = 0;
		int itemId = attachedItemObjId;
		long count = attachedItemCount;

		if (itemId != 0) {
			Item senderItem = ItemFactory.newItem(itemId, count);
			if (senderItem != null) {
				senderItem.setEquipped(false);
				senderItem.setEquipmentSlot(0);
				senderItem.setItemLocation(StorageType.MAILBOX.getId());
				attachedItem = senderItem;
			}
		}

		if (attachedKinahCount > 0)
			finalAttachedKinahCount = attachedKinahCount;

		String finalSender = sender;
		Timestamp time = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Letter newLetter = new Letter(IDFactory.getInstance().nextId(), recipientCommonData.getPlayerObjId(), attachedItem, finalAttachedKinahCount,
			title, message, finalSender, time, true, letterType);

		if (!DAOManager.getDAO(MailDAO.class).storeLetter(time, newLetter))
			return false;

		if (attachedItem != null)
			if (!DAOManager.getDAO(InventoryDAO.class).store(attachedItem, recipientCommonData.getPlayerObjId()))
				return false;

		/**
		 * Send mail update packets
		 */
		if (recipient != null && recipient.getMailbox() != null) {
			Mailbox recipientMailbox = recipient.getMailbox();
			recipientMailbox.putLetterToMailbox(newLetter);

			PacketSendUtility.sendPacket(recipient, new SM_MAIL_SERVICE(recipient.getMailbox()));
			recipientMailbox.isMailListUpdateRequired = true;

			// if recipient have opened mail list we should update it
			if (recipientMailbox.mailBoxState != 0) {
				boolean isPostman = (recipientMailbox.mailBoxState & PlayerMailboxState.EXPRESS) == PlayerMailboxState.EXPRESS;
				PacketSendUtility.sendPacket(recipient, new SM_MAIL_SERVICE(recipient, recipientMailbox.getLetters(), isPostman));
			}

			if (letterType == LetterType.EXPRESS)
				PacketSendUtility.sendPacket(recipient, SM_SYSTEM_MESSAGE.STR_POSTMAN_NOTIFY);
			/*
			 * else if (letterType == LetterType.BLACKCLOUD) PacketSendUtility.sendPacket(recipient, SM_SYSTEM_MESSAGE.STR_MAIL_CASHITEM_BUY(itemId));
			 */
		}

		/**
		 * Update loaded common data and db if player is offline
		 */
		if (!recipientCommonData.isOnline()) {
			recipientCommonData.setMailboxLetters(recipientCommonData.getMailboxLetters() + 1);
			DAOManager.getDAO(MailDAO.class).updateOfflineMailCounter(recipientCommonData);
		}
		if (LoggingConfig.LOG_SYSMAIL)
			log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] RETURN ITEM ID:" + itemId + " ITEM COUNT "
				+ attachedItemCount + " KINAH COUNT " + attachedKinahCount + " MESSAGE SUCCESSFULLY SENDED ");
		return true;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final SystemMailService instance = new SystemMailService();
	}

}
