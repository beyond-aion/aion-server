package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.dao.AccountPassportsDAO;
import com.aionemu.gameserver.dataholders.AtreianPassportData;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.Passport;
import com.aionemu.gameserver.model.account.PassportsList;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.event.AtreianPassport;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATREIAN_PASSPORT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author ViAl
 * @rework Luzien
 */
public class AtreianPassportService {

	private static final AtreianPassportData DATA = DataManager.ATREIAN_PASSPORT_DATA;

	private AtreianPassportService() {
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				Calendar calendar = Calendar.getInstance();
				DAOManager.getDAO(AccountPassportsDAO.class).resetAllPassports();
				if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
					DAOManager.getDAO(AccountPassportsDAO.class).resetAllStamps();
				}

				World.getInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player p) {
						if (p.getCommonData() != null) {
							p.getPlayerAccount().setLastStamp(null);

							if (calendar.get(Calendar.DAY_OF_MONTH) == 1)
								p.getPlayerAccount().setPassportStamps(0);

							onLogin(p);
						}
					}
				});
			}
		}, "0 0 9 ? * *");
	}

	public void takeReward(Player player, List<Integer> timestamps, List<Integer> passportIds) {
		List<Passport> toRemove = new FastTable<>();
		PassportsList ppl = player.getPlayerAccount().getPassportsList();
		for (int i = 0; i < passportIds.size(); i++) {
			int passId = passportIds.get(i);
			int time = timestamps.get(i);
			Passport passport = ppl.getPassport(passId, time);
			if (passport == null) {
				// AuditLogger.info(player, "Trying to get non-existing passport!");
				continue;
			}
			if (passport.isRewarded() || passport.getState().equals(PersistentState.DELETED)) {
				// AuditLogger.info(player, "Trying to get passport which is already rewarded!");
				continue;
			}

			if (player.getInventory().isFull()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_FULL_INVENTORY);
				break;
			}

			AtreianPassport atp = DATA.getAtreianPassportId(passId);
			ItemService.addItem(player, atp.getRewardItem(), atp.getRewardItemNum(), true, new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT,
				ItemUpdateType.INC_PASSPORT_ADD));
			passport.setRewarded(true);
			passport.setState(PersistentState.DELETED);
			ppl.removePassport(passport);
			toRemove.add(passport);
		}
		if (!toRemove.isEmpty()) {
			DAOManager.getDAO(AccountPassportsDAO.class).storePassportList(player.getPlayerAccount().getId(), toRemove);
			toRemove.clear();
		}
		onLogin(player);
	}

	public void onLogin(Player player) {
		Account pa = player.getPlayerAccount();
		boolean doReward = checkOnlineDate(pa) && pa.getPassportStamps() < 28;

		for (AtreianPassport atp : DATA.getAll().values()) {
			if (atp.getPeriodStart().isBeforeNow() && atp.getPeriodEnd().isAfterNow()) {
				switch (atp.getAttendType()) {
					case DAILY: {
						if (doReward) {
							Passport passport = new Passport(atp.getId(), false, new Timestamp(System.currentTimeMillis()));
							passport.setState(PersistentState.NEW);
							pa.getPassportsList().addPassport(passport);
						}
						break;
					}
					case CUMULATIVE: {
						if (doReward && atp.getAttendNum() == pa.getPassportStamps() + 1) {
							Passport passport = new Passport(atp.getId(), false, new Timestamp(System.currentTimeMillis()));
							passport.setState(PersistentState.NEW);
							pa.getPassportsList().addPassport(passport);
						} else {
							if (!pa.getPassportsList().isPassportPresent(atp.getId())) {
								Passport passport = new Passport(atp.getId(), false, new Timestamp(System.currentTimeMillis()));
								passport.setFakeStamp(true);
								if (atp.getAttendNum() <= pa.getPassportStamps())
									passport.setRewarded(true);
								pa.getPassportsList().addPassport(passport);
							}
						}
						break;
					}
					case ANNIVERSARY:
						// TODO
						break;
				}
			}
		}
		if (doReward) {
			pa.increasePassportStamps();
			pa.setLastStamp(new Timestamp(System.currentTimeMillis()));
			checkPassportLimit(player);
			DAOManager.getDAO(AccountPassportsDAO.class).storePassport(pa); // save account
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NEW_PASSPORT_AVAIBLE);
		}
		sendPassport(player);
	}

	private void sendPassport(Player player) {
		Account pa = player.getPlayerAccount();
		PlayerAccountData pad = pa.getPlayerAccountData(player.getObjectId());
		PacketSendUtility.sendPacket(player, new SM_ATREIAN_PASSPORT(pa.getPassportsList(), pa.getPassportStamps(), pad.getCreationDate().toLocalDateTime()));
	}

	private boolean checkOnlineDate(Account pa) {
		return pa.getLastStamp() == null;
	}

	/*
	 * Maximum 50 Passports are saved
	 */
	private void checkPassportLimit(Player player) {
		List<Passport> pl = player.getPlayerAccount().getPassportsList().getAllPassports();
		if (pl.size() <= 50)
			return;

		List<Passport> toRemove = new FastTable<>();

		int realSize = 0;
		for (int i = pl.size() - 1; i >= 0; i--) {
			Passport pp = pl.get(i);
			if (!pp.isFakeStamp() && !pp.isRewarded()) {
				if (++realSize > 50) {
					pp.setState(PersistentState.DELETED);
					toRemove.add(pp);
				}
			}
		}
		if (!toRemove.isEmpty()) {
			pl.removeAll(toRemove);
			DAOManager.getDAO(AccountPassportsDAO.class).storePassportList(player.getPlayerAccount().getId(), toRemove);
			toRemove.clear();
		}

	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final AtreianPassportService instance = new AtreianPassportService();
	}

	public static final AtreianPassportService getInstance() {
		return SingletonHolder.instance;
	}

}
