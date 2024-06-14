package com.aionemu.gameserver.services.mail;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * @author xTz
 */
public class SystemMailService {

	private static final Logger log = LoggerFactory.getLogger("SYSMAIL_LOG");

	private SystemMailService() {
	}

	public static boolean sendMail(String sender, String recipientName, String title, String message, int attachedItemId, long attachedItemCount,
		long attachedKinahCount, LetterType letterType) {

		if (attachedItemId != 0) {
			if (attachedItemCount <= 0)
				return false;
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(attachedItemId);
			if (itemTemplate == null) {
				log.warn("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] RETURN ITEM ID:" + attachedItemId
					+ " ITEM COUNT " + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " ITEM TEMPLATE IS MISSING ");
				return false;
			}
		}

		if (recipientName.length() > 16) {
			log.warn("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] ITEM RETURN" + attachedItemId + " ITEM COUNT "
				+ attachedItemCount + " KINAH COUNT " + attachedKinahCount + " RECIPIENT NAME LENGTH > 16 ");
			return false;
		}

		if (!sender.startsWith("$$") && sender.length() > 16) {
			log.warn("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] ITEM RETURN" + attachedItemId + " ITEM COUNT "
				+ attachedItemCount + " KINAH COUNT " + attachedKinahCount + " SENDER NAME LENGTH > 16 ");
			return false;
		}

		if (title.length() > 20)
			title = title.substring(0, 20);

		if (message.length() > 1000)
			message = message.substring(0, 1000);

		PlayerCommonData recipientCommonData = PlayerDAO.loadPlayerCommonDataByName(recipientName);

		if (recipientCommonData == null) {
			log.info("[SYSMAILSERVICE] > [RecipientName: " + recipientName + "] NO SUCH CHARACTER NAME.");
			return false;
		}

		if (recipientCommonData.getMailboxLetters() > 199) {
			log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientCommonData.getName() + "] ITEM RETURN" + attachedItemId
				+ " ITEM COUNT " + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " MAILBOX FULL ");
			return false;
		}
		Item attachedItem = null;
		long finalAttachedKinahCount = 0;

		if (attachedItemId != 0) {
			Item senderItem = ItemFactory.newItem(attachedItemId, attachedItemCount);
			if (senderItem != null) {
				senderItem.setEquipped(false);
				senderItem.setEquipmentSlot(0);
				senderItem.setItemLocation(StorageType.MAILBOX.getId());
				attachedItem = senderItem;
			}
		}

		if (attachedKinahCount > 0)
			finalAttachedKinahCount = attachedKinahCount;

		Letter newLetter = new Letter(IDFactory.getInstance().nextId(), recipientCommonData.getPlayerObjId(), attachedItem, finalAttachedKinahCount,
			title, message, sender, new Timestamp(System.currentTimeMillis()), true, letterType);

		if (!MailDAO.storeLetter(newLetter))
			return false;

		if (attachedItem != null)
			if (!InventoryDAO.store(attachedItem, recipientCommonData.getPlayerObjId()))
				return false;

		if (LoggingConfig.LOG_SYSMAIL)
			log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] RETURN ITEM ID:" + attachedItemId
				+ " ITEM COUNT " + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " MESSAGE SUCCESSFULLY SENDED ");

		updateRecipientMailbox(recipientCommonData, newLetter);
		return true;
	}

	static void updateRecipientMailbox(PlayerCommonData recipientCommonData, Letter newLetter) {
		Player recipient = recipientCommonData.getPlayer();
		if (recipient == null) {
			recipientCommonData.setMailboxLetters(recipientCommonData.getMailboxLetters() + 1);
			MailDAO.updateOfflineMailCounter(recipientCommonData);
		} else if (recipient.getMailbox() != null) { // Send mail update packets
			Mailbox mailbox = recipient.getMailbox();
			mailbox.putLetterToMailbox(newLetter);
			recipientCommonData.setMailboxLetters(mailbox.size());

			PacketSendUtility.sendPacket(recipient, new SM_MAIL_SERVICE());

			// refresh letters if recipient is currently looking into his mailbox
			if (mailbox.mailBoxState != 0) {
				boolean isPostman = (mailbox.mailBoxState & PlayerMailboxState.EXPRESS) == PlayerMailboxState.EXPRESS;
				MailService.sendMailList(recipient, isPostman, false);
			}

			if (newLetter.getLetterType() == LetterType.EXPRESS)
				PacketSendUtility.sendPacket(recipient, SM_SYSTEM_MESSAGE.STR_POSTMAN_NOTIFY());
			// else if (newLetter.getLetterType() == LetterType.BLACKCLOUD) PacketSendUtility.sendPacket(recipient, SM_SYSTEM_MESSAGE.STR_MAIL_CASHITEM_BUY(itemId));
		}
	}
}
