package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.CharacterBanInfo;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.PlayerInfo;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * In this packet Server is sending Character List to client.
 * 
 * @author Nemesiss, AEJTester
 * @modified Neon
 */
public class SM_CHARACTER_LIST extends PlayerInfo {

	/**
	 * PlayOk2 - we dont care...
	 */
	private final int playOk2;

	/**
	 * Constructs new <tt>SM_CHARACTER_LIST </tt> packet
	 */
	public SM_CHARACTER_LIST(int playOk2) {
		this.playOk2 = playOk2;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Account account = con.getAccount();

		writeD(playOk2);
		writeC(account.size()); // character count
		for (PlayerAccountData playerData : account.getSortedAccountsList()) {
			PlayerCommonData pcd = playerData.getPlayerCommonData();
			Player player = PlayerService.getPlayer(pcd.getPlayerObjId(), account);
			CharacterBanInfo cbi = playerData.getCharBanInfo();
			boolean isBanned = (cbi != null && cbi.getEnd() > System.currentTimeMillis() / 1000);

			writePlayerInfo(playerData);
			writeD(0);
			writeD(0);
			writeD(0); // 4.5
			writeD(0); // 4.5
			writeD(0); // 4.5
			writeD(0); // 4.5
			writeB(new byte[68]);
			writeD(playerData.getDeletionTimeInSeconds());
			writeH(player.getPlayerSettings().getDisplay()); // display helmet 0 show, 5 dont show , possible bit operation
			writeH(0);
			writeD(0);
			writeD(DAOManager.getDAO(MailDAO.class).haveUnread(pcd.getPlayerObjId()) ? 1 : 0); // mail
			writeD(0); // unk
			writeD(0); // unk
			writeQ(BrokerService.getInstance().getCollectedMoney(pcd)); // collected money from broker
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			// client wants int so let's hope we do not reach long limit with timestamp while this server is used :P
			writeD(isBanned ? (int) cbi.getStart() : 0); // startPunishDate
			writeD(isBanned ? (int) cbi.getEnd() : 0); // endPunishDate
			writeS(isBanned ? cbi.getReason() : "");
		}
	}
}
