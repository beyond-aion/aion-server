package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;
import java.util.function.Function;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Letter;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.mail.MailMessage;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob;

/**
 * @author kosyachok, Source, Neon
 */
public class SM_MAIL_SERVICE extends AionServerPacket {

	public static final int STATIC_BODY_SIZE = 8;
	public static final Function<Letter, Integer> DYNAMIC_BODY_PART_SIZE_CALCULATOR = (letter) -> 22 + byteLengthForString(letter.getSenderName())
		+ byteLengthForString(letter.getTitle());

	private Player player;
	private int serviceId;
	private List<Letter> letters;
	private int mailMessage;
	private Letter letter;
	private long time;
	private int letterId;
	private int[] letterIds;
	private byte attachmentType;
	private boolean isLastPacket;

	public SM_MAIL_SERVICE() {
		this.serviceId = 0;
	}

	/**
	 * Send mailMessage(ex. Send OK, Mailbox full etc.)
	 */
	public SM_MAIL_SERVICE(MailMessage mailMessage) {
		this.serviceId = 1;
		this.mailMessage = mailMessage.getId();
	}

	/**
	 * Send mailbox info
	 */
	public SM_MAIL_SERVICE(Player player, List<Letter> letters, boolean isLastPacket) {
		this.player = player;
		this.serviceId = 2;
		this.letters = letters;
		this.isLastPacket = isLastPacket;
	}

	/**
	 * used when reading letter
	 */
	public SM_MAIL_SERVICE(Player player, Letter letter, long time) {
		this.player = player;
		this.serviceId = 3;
		this.letter = letter;
		this.time = time;
	}

	/**
	 * used when getting attached items
	 */
	public SM_MAIL_SERVICE(int letterId, byte attachmentType) {
		this.serviceId = 5;
		this.letterId = letterId;
		this.attachmentType = attachmentType;
	}

	/**
	 * used when deleting letter
	 */
	public SM_MAIL_SERVICE(int[] letterIds) {
		this.serviceId = 6;
		this.letterIds = letterIds;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Mailbox mailbox = con.getActivePlayer().getMailbox();
		int totalCount = mailbox.size();
		int unreadCount = mailbox.getUnreadCount();
		int unreadExpressCount = mailbox.getUnreadCountByType(LetterType.EXPRESS);
		int unreadBlackCloudCount = mailbox.getUnreadCountByType(LetterType.BLACKCLOUD);
		writeC(serviceId);
		switch (serviceId) {
			case 0 -> writeMailboxState(totalCount, unreadCount, unreadExpressCount, unreadBlackCloudCount);
			case 1 -> writeMailMessage(mailMessage);
			case 2 -> writeLettersList(letters);
			case 3 -> writeLetterRead(letter, time, totalCount, unreadCount, unreadExpressCount, unreadBlackCloudCount);
			case 5 -> writeLetterState(letterId, attachmentType);
			case 6 -> writeLetterDelete(totalCount, unreadCount, unreadExpressCount, unreadBlackCloudCount, letterIds);
		}
	}

	private void writeLettersList(List<Letter> letters) {
		writeD(player.getObjectId());
		writeC(0);
		writeH(isLastPacket ? letters.size() * -1 : letters.size());
		for (Letter letter : letters) {
			writeD(letter.getObjectId());
			writeS(letter.getSenderName());
			writeS(letter.getTitle());
			writeC(letter.isUnread() ? 0 : 1); // isRead
			writeD(letter.getAttachedItem() == null ? 0 : letter.getAttachedItem().getObjectId());
			writeD(letter.getAttachedItem() == null ? 0 : letter.getAttachedItem().getItemTemplate().getTemplateId());
			writeQ(letter.getAttachedKinah());
			writeC(letter.getLetterType().getId());
		}
	}

	private void writeMailMessage(int messageId) {
		writeC(messageId);
	}

	private void writeMailboxState(int totalCount, int unreadCount, int expressCount, int blackCloudCount) {
		writeH(totalCount);
		writeH(unreadCount);
		writeH(expressCount);
		writeH(blackCloudCount);
	}

	private void writeLetterRead(Letter letter, long time, int totalCount, int unreadCount, int expressCount, int blackCloudCount) {
		writeD(letter.getRecipientId());
		writeD(totalCount + unreadCount * 0x10000); // total count + unread hex
		writeD(expressCount + blackCloudCount); // unread express + BC letters count
		writeD(letter.getObjectId());
		writeD(letter.getRecipientId());
		writeS(letter.getSenderName());
		writeS(letter.getTitle());
		writeS(letter.getMessage());

		Item item = letter.getAttachedItem();
		if (item != null) {
			ItemTemplate itemTemplate = item.getItemTemplate();

			writeD(item.getObjectId());
			writeD(itemTemplate.getTemplateId());
			writeD(1); // unk
			writeD(0); // unk
			writeS(itemTemplate.getL10n());

			ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
			itemInfoBlob.writeMe(getBuf());
		} else {
			writeQ(0);
			writeQ(0);
			writeD(0);
		}

		writeD((int) letter.getAttachedKinah());
		writeD(0); // AP reward for castle assault/defense (in future)
		writeC(0);
		writeD((int) (time / 1000));
		writeC(letter.getLetterType().getId()); // mail type
	}

	private void writeLetterState(int letterId, byte attachmentType) {
		writeD(letterId);
		writeC(attachmentType);
		writeC(1);
	}

	private void writeLetterDelete(int totalCount, int unreadCount, int expressCount, int blackCloudCount, int... letterIds) {
		writeD(totalCount + unreadCount * 0x10000); // total count + unread hex
		writeD(expressCount + blackCloudCount); // unread express + BC letters count
		writeH(letterIds.length);
		for (int letterId : letterIds)
			writeD(letterId);
	}
}
