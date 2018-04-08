package com.aionemu.gameserver.services.mail;

import java.sql.Timestamp;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dao.BlockListDAO;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Letter;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.templates.item.Disposition;
import com.aionemu.gameserver.model.templates.mail.MailMessage;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MAIL_SERVICE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AdminService;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.player.PlayerMailboxState;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * @author kosyachok
 */
public class MailService {

	private static final Logger log = LoggerFactory.getLogger("MAIL_LOG");

	public static final MailService getInstance() {
		return SingletonHolder.instance;
	}

	private MailService() {
	}

	/**
	 * TODO split this method
	 * 
	 * @param sender
	 * @param recipientName
	 * @param title
	 * @param message
	 * @param attachedItemObjId
	 * @param attachedItemCount
	 * @param attachedKinahCount
	 * @param letterType
	 */
	public void sendMail(Player sender, String recipientName, String title, String message, int attachedItemObjId, long attachedItemCount,
		long attachedKinahCount, LetterType letterType) {

		if (letterType == LetterType.BLACKCLOUD || recipientName.length() > 16)
			return;

		if (title.length() > 20)
			title = title.substring(0, 20);

		if (message.length() > 1000)
			message = message.substring(0, 1000);

		PlayerCommonData recipientCommonData = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonDataByName(recipientName);

		if (recipientCommonData == null) {
			PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(MailMessage.NO_SUCH_CHARACTER_NAME));
			return;
		}

		int recipientObjId = recipientCommonData.getPlayerObjId();

		if ((recipientCommonData.getRace() != sender.getRace()) && !sender.isStaff()) {
			PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(MailMessage.MAIL_IS_ONE_RACE_ONLY));
			return;
		}

		Player recipient = World.getInstance().findPlayer(recipientObjId);
		if (recipient != null) {
			if (!recipient.getMailbox().haveFreeSlots()) {
				PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(MailMessage.RECIPIENT_MAILBOX_FULL));
				return;
			} else if (recipient.getBlockList().contains(sender.getObjectId())) {
				PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(MailMessage.YOU_ARE_IN_RECIPIENT_IGNORE_LIST));
				return;
			}
		} else if (recipientCommonData.getMailboxLetters() > 99) {
			PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(MailMessage.RECIPIENT_MAILBOX_FULL));
			return;
		} else if (DAOManager.getDAO(BlockListDAO.class).load(recipientObjId).contains(sender.getObjectId())) {
			PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(MailMessage.YOU_ARE_IN_RECIPIENT_IGNORE_LIST));
			return;
		}

		Item senderItem = null;
		int baseCost = letterType == LetterType.EXPRESS ? 500 : 10;
		int costFactor = letterType == LetterType.EXPRESS ? 5 : 1;
		long kinahMailCommission = 0;
		long itemMailCommission = 0;

		Storage senderInventory = sender.getInventory();

		if (attachedItemObjId != 0 && attachedItemCount > 0) {
			senderItem = senderInventory.getItemByObjId(attachedItemObjId);

			if (senderItem == null || senderItem.getItemCount() < attachedItemCount) {
				PacketSendUtility.sendPacket(sender, SM_SYSTEM_MESSAGE.STR_MAIL_SEND_USED_ITEM());
				return;
			}

			if (senderItem.isEquipped()) {
				PacketSendUtility.sendPacket(sender, SM_SYSTEM_MESSAGE.STR_MAIL_SEND_CAN_NOT_SEND_EQUIPPED_ITEM());
				return;
			}

			if (!AdminService.getInstance().canOperate(sender, null, senderItem, "mail"))
				return;

			float qualityPriceRate = 0.02f;
			switch (senderItem.getItemTemplate().getItemQuality()) {
				case JUNK:
				case COMMON:
					qualityPriceRate = 0.02f;
					break;
				case RARE:
					qualityPriceRate = 0.03f;
					break;
				case LEGEND:
				case UNIQUE:
					qualityPriceRate = 0.04f;
					break;
				case MYTHIC:
				case EPIC:
					qualityPriceRate = 0.05f;
					break;
			}

			itemMailCommission = (long) (senderItem.getItemTemplate().getPrice() * qualityPriceRate * attachedItemCount * costFactor);
		}

		if (attachedKinahCount > 0)
			kinahMailCommission = (long) (attachedKinahCount * 0.01f * costFactor);

		long finalMailKinah = PricesService.getPriceForService(baseCost + kinahMailCommission + itemMailCommission, sender.getRace()) + attachedKinahCount;

		if (senderInventory.getKinah() < finalMailKinah) {
			PacketSendUtility.sendPacket(sender, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
			return;
		}

		Item attachedItem = null;
		if (senderItem != null) {
			// Check Mailing untradables with Cash items (Special courier passes)
			if (senderItem.getPackCount() <= 0 && !senderItem.isTradeable(sender)) {
				Disposition dispo = senderItem.getItemTemplate().getDisposition();
				if (dispo == null || dispo.getId() == 0 || dispo.getCount() == 0) // can not be traded, hack
					return;

				if (senderInventory.getItemCountByItemId(dispo.getId()) >= dispo.getCount()) {
					senderInventory.decreaseByItemId(dispo.getId(), dispo.getCount());
				} else
					return;
			}

			// reuse item in case of full decrease of count
			if (senderItem.getItemCount() == attachedItemCount) {
				senderInventory.remove(senderItem);
				PacketSendUtility.sendPacket(sender, new SM_DELETE_ITEM(attachedItemObjId));
				attachedItem = senderItem;
			} else if (senderItem.getItemCount() > attachedItemCount) {
				attachedItem = ItemFactory.newItem(senderItem.getItemTemplate().getTemplateId(), attachedItemCount);
				senderInventory.decreaseItemCount(senderItem, attachedItemCount);
			}

			if (attachedItem == null)
				return;

			// unpack
			if (attachedItem.getPackCount() > 0)
				attachedItem.setPackCount(attachedItem.getPackCount() - 1);

			attachedItem.setItemLocation(StorageType.MAILBOX.getId());
		}

		senderInventory.decreaseKinah(finalMailKinah);
		Timestamp time = new Timestamp(System.currentTimeMillis());
		Letter newLetter = new Letter(IDFactory.getInstance().nextId(), recipientObjId, attachedItem, attachedKinahCount, title, message,
			sender.getName(), time, true, letterType);

		// first save attached item for FK consistency
		if (attachedItem != null) {
			if (!DAOManager.getDAO(InventoryDAO.class).store(attachedItem, recipientObjId)) {
				return;
			}
			// save item stones too.
			DAOManager.getDAO(ItemStoneListDAO.class).save(Arrays.asList(attachedItem));
		}
		// save letter
		if (!DAOManager.getDAO(MailDAO.class).storeLetter(time, newLetter))
			return;

		/**
		 * Send mail update packets
		 */
		if (recipient != null) {
			Mailbox recipientMailbox = recipient.getMailbox();
			recipientMailbox.putLetterToMailbox(newLetter);

			// packets for sender
			PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(MailMessage.MAIL_SEND_SUCCESS));

			// packets for recipient
			PacketSendUtility.sendPacket(recipient, new SM_MAIL_SERVICE(recipientMailbox));
			recipientMailbox.isMailListUpdateRequired = true;

			// if recipient have opened mail list we should update it
			if (recipientMailbox.mailBoxState != 0) {
				boolean isPostman = (recipientMailbox.mailBoxState & PlayerMailboxState.EXPRESS) == PlayerMailboxState.EXPRESS;
				PacketSendUtility.sendPacket(recipient, new SM_MAIL_SERVICE(recipient, recipientMailbox.getLetters(), isPostman));
			}

			if (letterType == LetterType.EXPRESS)
				PacketSendUtility.sendPacket(recipient, SM_SYSTEM_MESSAGE.STR_POSTMAN_NOTIFY());
		}

		if (attachedItem != null) {
			if (LoggingConfig.LOG_MAIL)
				log.info("Player: " + sender.getName() + " sent item " + attachedItem.getItemId() + " [" + attachedItem.getItemName() + "] (count: "
					+ attachedItem.getItemCount() + ") to player " + recipientName);
		}

		/**
		 * Update loaded common data and db if player is offline
		 */
		if (!recipientCommonData.isOnline()) {
			PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(MailMessage.MAIL_SEND_SUCCESS));
			recipientCommonData.setMailboxLetters(recipientCommonData.getMailboxLetters() + 1);
			DAOManager.getDAO(MailDAO.class).updateOfflineMailCounter(recipientCommonData);
		}
	}

	/**
	 * Read letter with specified letter id
	 * 
	 * @param player
	 * @param letterId
	 */
	public void readMail(Player player, int letterId) {
		Letter letter = player.getMailbox().getLetterFromMailbox(letterId);
		if (letter == null) {
			log.warn("Cannot read mail " + player.getObjectId() + " " + letterId);
			return;
		}

		PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(player, letter, letter.getTimeStamp().getTime()));
		letter.setReadLetter();
	}

	/**
	 * @param player
	 * @param letterId
	 * @param attachmentType
	 */
	public void getAttachments(Player player, int letterId, byte attachmentType) {
		Letter letter = player.getMailbox().getLetterFromMailbox(letterId);

		if (letter == null)
			return;

		switch (attachmentType) {
			case 0:
				Item attachedItem = letter.getAttachedItem();
				if (attachedItem == null)
					return;
				if (player.getInventory().isFull()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MAIL_TAKE_ALL_CANCEL());
					return;
				}
				if (attachedItem.getExpireTime() != 0 && attachedItem.getExpireTime() <= System.currentTimeMillis() / 1000) {
					attachedItem.setPersistentState(PersistentState.DELETED);
					DAOManager.getDAO(InventoryDAO.class).store(attachedItem, player);
				} else {
					if (player.getInventory().add(attachedItem, ItemPacketService.ItemAddType.MAIL) == null)
						return;
					ExpireTimerTask.getInstance().registerExpirable(attachedItem, player);
				}
				PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(letterId, attachmentType));
				letter.setAttachedItem(null);
				break;
			case 1:
				// TODO normal fix
				// fix for kinah dupe
				long attachedKinahCount = letter.getAttachedKinah();
				letter.removeAttachedKinah();
				if (!DAOManager.getDAO(MailDAO.class).storeLetter(letter.getTimeStamp(), letter)) {
					AuditLogger.log(player, "tried to use kinah mail exploit. Location: " + player.getPosition() + ", kinah count: " + attachedKinahCount);
					return;
				}
				player.getInventory().increaseKinah(attachedKinahCount);
				PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(letterId, attachmentType));
				break;
		}
	}

	/**
	 * @param player
	 * @param mailObjId
	 */
	public void deleteMail(Player player, int[] mailObjId) {
		Mailbox mailbox = player.getMailbox();

		for (int letterId : mailObjId) {
			mailbox.removeLetter(letterId);
			DAOManager.getDAO(MailDAO.class).deleteLetter(letterId);
		}
		PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(mailObjId));
	}

	/**
	 * @param player
	 */
	public void onPlayerLogin(Player player) {
		player.setMailbox(DAOManager.getDAO(MailDAO.class).loadPlayerMailbox(player));
		PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(player.getMailbox()));
	}

	public void refreshMail(Player player) {
		PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(player.getMailbox()));
		PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(player, player.getMailbox().getLetters(), false));
	}

	private static class SingletonHolder {

		protected static final MailService instance = new MailService();
	}

}
