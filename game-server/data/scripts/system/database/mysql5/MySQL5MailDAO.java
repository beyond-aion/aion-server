package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Letter;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.services.item.ItemService;

/**
 * @author kosyachok
 */
public class MySQL5MailDAO extends MailDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5MailDAO.class);

	@Override
	public Mailbox loadPlayerMailbox(Player player) {
		final Mailbox mailbox = new Mailbox(player);
		Map<Letter, Integer> letters = new HashMap<>();
		List<Item> mailboxItems = null;

		DB.select("SELECT * FROM mail WHERE mail_recipient_id = ?", new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getObjectId());
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int mailUniqueId = rset.getInt("mail_unique_id");
					int recipientId = rset.getInt("mail_recipient_id");
					String senderName = rset.getString("sender_name");
					String mailTitle = rset.getString("mail_title");
					String mailMessage = rset.getString("mail_message");
					boolean unread = rset.getInt("unread") == 1;
					int attachedItemObjId = rset.getInt("attached_item_id");
					long attachedKinahCount = rset.getLong("attached_kinah_count");
					LetterType letterType = LetterType.getLetterTypeById(rset.getInt("express"));
					Timestamp receivedTime = rset.getTimestamp("recieved_time");
					letters.put(new Letter(mailUniqueId, recipientId, null, attachedKinahCount, mailTitle, mailMessage, senderName, receivedTime, unread,
						letterType), attachedItemObjId);
				}
			}
		});

		for (Entry<Letter, Integer> e : letters.entrySet()) {
			Letter letter = e.getKey();
			int attachedItemObjId = e.getValue();

			if (attachedItemObjId > 0) {
				if (mailboxItems == null) { // lazy initialization to minimize DB io
					mailboxItems = loadMailboxItems(player.getObjectId());
					ItemService.loadItemStones(mailboxItems);
				}
				for (Item item : mailboxItems)
					if (item.getObjectId() == attachedItemObjId)
						letter.setAttachedItem(item);
			}
			letter.setPersistState(PersistentState.UPDATED);
			mailbox.putLetterToMailbox(letter);
		}

		return mailbox;
	}

	@Override
	public boolean haveUnread(int playerId) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM mail WHERE mail_recipient_id = ? ORDER BY recieved_time")) {

			stmt.setInt(1, playerId);
			ResultSet rset = stmt.executeQuery();
			while (rset.next()) {
				int unread = rset.getInt("unread");
				if (unread == 1) {
					return true;
				}
			}
			rset.close();
		} catch (Exception e) {
			log.error("Could not read mail for player: " + playerId + " from DB: " + e.getMessage(), e);
		}
		return false;
	}

	private List<Item> loadMailboxItems(final int playerId) {
		final List<Item> mailboxItems = new ArrayList<>();

		DB.select("SELECT * FROM inventory WHERE `item_owner` = ? AND `item_location` = 127", new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerId);
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int itemUniqueId = rset.getInt("item_unique_id");
					int itemId = rset.getInt("item_id");
					long itemCount = rset.getLong("item_count");
					Integer itemColor = (Integer) rset.getObject("item_color"); // accepts null (which means not dyed)
					int colorExpireTime = rset.getInt("color_expires");
					String itemCreator = rset.getString("item_creator");
					int expireTime = rset.getInt("expire_time");
					int activationCount = rset.getInt("activation_count");
					int isEquiped = rset.getInt("is_equiped");
					int isSoulBound = rset.getInt("is_soul_bound");
					int slot = rset.getInt("slot");
					int enchant = rset.getInt("enchant");
					int enchantBonus = rset.getInt("enchant_bonus");
					int itemSkin = rset.getInt("item_skin");
					int fusionedItem = rset.getInt("fusioned_item");
					int optionalSocket = rset.getInt("optional_socket");
					int optionalFusionSocket = rset.getInt("optional_fusion_socket");
					int charge = rset.getInt("charge");
					int randomBonus = rset.getInt("rnd_bonus");
					int rndCount = rset.getInt("rnd_count");
					int tempering = rset.getInt("tempering");
					int packCount = rset.getInt("pack_count");
					int isAmplified = rset.getInt("is_amplified");
					int buffSkill = rset.getInt("buff_skill");
					int rndPlumeBonusValue = rset.getInt("rnd_plume_bonus");

					Item item = new Item(itemUniqueId, itemId, itemCount, itemColor, colorExpireTime, itemCreator, expireTime, activationCount, isEquiped == 1,
						isSoulBound == 1, slot, StorageType.MAILBOX.getId(), enchant, enchantBonus, itemSkin, fusionedItem, optionalSocket, optionalFusionSocket,
						charge, randomBonus, rndCount, tempering, packCount, isAmplified == 1, buffSkill, rndPlumeBonusValue);
					item.setPersistentState(PersistentState.UPDATED);
					mailboxItems.add(item);
				}
			}
		});

		return mailboxItems;
	}

	@Override
	public void storeMailbox(Player player) {
		Mailbox mailbox = player.getMailbox();
		if (mailbox == null)
			return;
		Collection<Letter> letters = mailbox.getLetters();
		for (Letter letter : letters) {
			storeLetter(letter.getTimeStamp(), letter);
		}
	}

	@Override
	public boolean storeLetter(Timestamp time, Letter letter) {
		boolean result = false;
		switch (letter.getLetterPersistentState()) {
			case NEW:
				result = saveLetter(time, letter);
				break;

			case UPDATE_REQUIRED:
				result = updateLetter(time, letter);
				break;
			/*
			 * case DELETED: return deleteLetter(letter);
			 */
		}
		letter.setPersistState(PersistentState.UPDATED);

		return result;
	}

	private boolean saveLetter(final Timestamp time, final Letter letter) {
		int attachedItemId = 0;
		if (letter.getAttachedItem() != null)
			attachedItemId = letter.getAttachedItem().getObjectId();

		final int fAttachedItemId = attachedItemId;

		return DB.insertUpdate(
			"INSERT INTO `mail` (`mail_unique_id`, `mail_recipient_id`, `sender_name`, `mail_title`, `mail_message`, `unread`, `attached_item_id`, `attached_kinah_count`, `express`, `recieved_time`) VALUES(?,?,?,?,?,?,?,?,?,?)",
			new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
					stmt.setInt(1, letter.getObjectId());
					stmt.setInt(2, letter.getRecipientId());
					stmt.setString(3, letter.getSenderName());
					stmt.setString(4, letter.getTitle());
					stmt.setString(5, letter.getMessage());
					stmt.setBoolean(6, letter.isUnread());
					stmt.setInt(7, fAttachedItemId);
					stmt.setLong(8, letter.getAttachedKinah());
					stmt.setInt(9, letter.getLetterType().getId());
					stmt.setTimestamp(10, time);
					stmt.execute();
				}
			});
	}

	private boolean updateLetter(final Timestamp time, final Letter letter) {
		int attachedItemId = 0;
		if (letter.getAttachedItem() != null)
			attachedItemId = letter.getAttachedItem().getObjectId();

		final int fAttachedItemId = attachedItemId;

		return DB.insertUpdate(
			"UPDATE mail SET  unread=?, attached_item_id=?, attached_kinah_count=?, `express`=?, recieved_time=? WHERE mail_unique_id=?", new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
					stmt.setBoolean(1, letter.isUnread());
					stmt.setInt(2, fAttachedItemId);
					stmt.setLong(3, letter.getAttachedKinah());
					stmt.setInt(4, letter.getLetterType().getId());
					stmt.setTimestamp(5, time);
					stmt.setInt(6, letter.getObjectId());
					stmt.execute();
				}
			});
	}

	@Override
	public boolean deleteLetter(final int letterId) {
		return DB.insertUpdate("DELETE FROM mail WHERE mail_unique_id=?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, letterId);
				stmt.execute();
			}
		});
	}

	@Override
	public void updateOfflineMailCounter(final PlayerCommonData recipientCommonData) {
		DB.insertUpdate("UPDATE players SET mailbox_letters=? WHERE name=?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, recipientCommonData.getMailboxLetters());
				stmt.setString(2, recipientCommonData.getName());
				stmt.execute();
			}
		});
	}

	@Override
	public int[] getUsedIDs() {
		PreparedStatement statement = DB.prepareStatement("SELECT mail_unique_id FROM mail", ResultSet.TYPE_SCROLL_INSENSITIVE,
			ResultSet.CONCUR_READ_ONLY);

		try {
			ResultSet rs = statement.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; i < count; i++) {
				rs.next();
				ids[i] = rs.getInt("mail_unique_id");
			}
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of id's from mail table", e);
		} finally {
			DB.close(statement);
		}

		return new int[0];
	}

	@Override
	public boolean cleanMail(final String recipient) {
		return DB.insertUpdate(
			"DELETE FROM mail WHERE mail_recipient_id=(SELECT id FROM players WHERE name=?) AND attached_item_id=0 AND attached_kinah_count=0",
			new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
					stmt.setString(1, recipient);
					stmt.execute();
				}
			});
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
