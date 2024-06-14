package com.aionemu.gameserver.services.mail;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dao.*;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Letter;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.BlockList;
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
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.collections.DynamicServerPacketBodySplitList;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * @author kosyachok
 */
public class MailService {

	private static final Logger log = LoggerFactory.getLogger("MAIL_LOG");

	private MailService() {
	}

	public static void sendMail(Player sender, String recipientName, String title, String message, int attachedItemObjId, long attachedItemCount,
		long attachedKinah, LetterType letterType) {
		if (sender.isTrading() || recipientName.length() > 16)
			return;
		if (letterType == LetterType.BLACKCLOUD || attachedKinah < 0) {
			AuditLogger.log(sender, "tried to send letter of type " + letterType + " with " + attachedKinah + " Kinah");
			return;
		}

		if (title.length() > 20)
			title = title.substring(0, 20);

		if (message.length() > 1000)
			message = message.substring(0, 1000);

		PlayerCommonData recipientCommonData = PlayerDAO.loadPlayerCommonDataByName(recipientName);
		MailMessage status = validateRecipient(sender, recipientCommonData);
		if (status != MailMessage.MAIL_SEND_SUCCESS) {
			PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(status));
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

			itemMailCommission = (long) (senderItem.getItemTemplate().getPrice() * getQualityPriceRate(senderItem) * attachedItemCount * costFactor);
		}

		if (attachedKinah > 0)
			kinahMailCommission = (long) (attachedKinah * 0.01f * costFactor);

		long finalMailKinah = PricesService.getPriceForService(baseCost + kinahMailCommission + itemMailCommission, sender.getRace()) + attachedKinah;

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
				attachedItem.setPackCount(attachedItem.getPackCount() * -1);

			attachedItem.setItemLocation(StorageType.MAILBOX.getId());
		}

		senderInventory.decreaseKinah(finalMailKinah);
		Letter newLetter = new Letter(IDFactory.getInstance().nextId(), recipientCommonData.getPlayerObjId(), attachedItem, attachedKinah, title, message,
			sender.getName(), new Timestamp(System.currentTimeMillis()), true, letterType);

		// first save attached item for FK consistency
		if (attachedItem != null) {
			if (!InventoryDAO.store(attachedItem, recipientCommonData.getPlayerObjId()))
				return;
			// save item stones too
			ItemStoneListDAO.save(Collections.singletonList(attachedItem));
		}
		// save letter
		if (!MailDAO.storeLetter(newLetter))
			return;

		if (attachedItem != null && LoggingConfig.LOG_MAIL)
			log.info("Player: " + sender.getName() + " sent item " + attachedItem.getItemId() + " [" + attachedItem.getItemName() + "] (count: "
				+ attachedItem.getItemCount() + ") to player " + recipientName);

		PacketSendUtility.sendPacket(sender, new SM_MAIL_SERVICE(status));
		SystemMailService.updateRecipientMailbox(recipientCommonData, newLetter);
	}

	private static MailMessage validateRecipient(Player sender, PlayerCommonData recipientCommonData) {
		if (recipientCommonData == null)
			return MailMessage.NO_SUCH_CHARACTER_NAME;
		if (recipientCommonData.getRace() != sender.getRace() && !sender.isStaff())
			return MailMessage.MAIL_IS_ONE_RACE_ONLY;
		if (recipientCommonData.getMailboxLetters() >= 100)
			return MailMessage.RECIPIENT_MAILBOX_FULL;
		Player p = World.getInstance().getPlayer(recipientCommonData.getPlayerObjId());
		BlockList blockList = p != null ? p.getBlockList() : BlockListDAO.load(recipientCommonData.getPlayerObjId());
		if (blockList.contains(sender.getObjectId()))
			return MailMessage.YOU_ARE_IN_RECIPIENT_IGNORE_LIST;
		return MailMessage.MAIL_SEND_SUCCESS;
	}

	private static float getQualityPriceRate(Item senderItem) {
		switch (senderItem.getItemTemplate().getItemQuality()) {
			case MYTHIC:
			case EPIC:
				return 0.05f;
			case UNIQUE:
			case LEGEND:
				return 0.04f;
			case RARE:
				return 0.03f;
			default:
				return 0.02f;
		}
	}

	/**
	 * Read letter with specified letter id
	 */
	public static void readMail(Player player, int letterId) {
		Letter letter = player.getMailbox().getLetterFromMailbox(letterId);
		if (letter == null) {
			log.warn("Cannot read mail " + player.getObjectId() + " " + letterId);
			return;
		}

		PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(player, letter, letter.getTimeStamp().getTime()));
		letter.setReadLetter();
	}

	public static void getAttachments(Player player, int letterId, byte attachmentType) {
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
					InventoryDAO.store(attachedItem, player);
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
				if (!MailDAO.storeLetter(letter)) {
					AuditLogger.log(player, "tried to use kinah mail exploit. Location: " + player.getPosition() + ", kinah count: " + attachedKinahCount);
					return;
				}
				player.getInventory().increaseKinah(attachedKinahCount);
				PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(letterId, attachmentType));
				break;
		}
	}

	public static void deleteMail(Player player, int[] mailObjId) {
		Mailbox mailbox = player.getMailbox();
		for (int letterId : mailObjId) {
			mailbox.removeLetter(letterId);
			MailDAO.deleteLetter(letterId);
		}
		PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(mailObjId));
	}

	public static void onPlayerLogin(Player player) {
		player.setMailbox(MailDAO.loadPlayerMailbox(player));
		PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE());
	}

	public static void sendMailList(Player player, boolean isExpress, boolean sendRefreshPacket) {
		if (sendRefreshPacket)
			PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE());

		Stream<Letter> letterStream = player.getMailbox().getLetters().stream();
		if (isExpress)
			letterStream = letterStream.filter(letter -> letter.isExpress() && letter.isUnread());
		List<Letter> letters = letterStream.sorted(Comparator.comparing(Letter::getTimeStamp).reversed()).collect(Collectors.toList());
		SplitList<Letter> mailSplitList = new DynamicServerPacketBodySplitList<>(letters, true, SM_MAIL_SERVICE.STATIC_BODY_SIZE,
			SM_MAIL_SERVICE.DYNAMIC_BODY_PART_SIZE_CALCULATOR);
		mailSplitList.forEach(part -> PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(player, part, part.isLast())));
	}

}
