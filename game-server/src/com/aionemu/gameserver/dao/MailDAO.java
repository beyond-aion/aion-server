package com.aionemu.gameserver.dao;

import java.sql.Timestamp;

import com.aionemu.gameserver.model.gameobjects.Letter;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;

/**
 * @author kosyachok
 */
public abstract class MailDAO implements IDFactoryAwareDAO {

	@Override
	public String getClassName() {
		return MailDAO.class.getName();
	}

	public abstract boolean storeLetter(Timestamp time, Letter letter);

	public abstract Mailbox loadPlayerMailbox(Player player);

	public abstract void storeMailbox(Player player);

	public abstract boolean deleteLetter(int letterId);

	public abstract void updateOfflineMailCounter(PlayerCommonData recipientCommonData);

	public abstract boolean haveUnread(int playerId);
	
	public abstract boolean cleanMail(final String recipient);
}
