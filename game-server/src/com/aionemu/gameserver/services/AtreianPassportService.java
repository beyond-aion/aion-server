package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.dao.PlayerPassportsDAO;
import com.aionemu.gameserver.dataholders.AtreianPassportData;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.gameobjects.player.passport.Passport;
import com.aionemu.gameserver.model.templates.event.AtreianPassport;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATREIAN_PASSPORT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author ViAl
 */
public class AtreianPassportService {
	private static final AtreianPassportData DATA = DataManager.ATREIAN_PASSPORT_DATA;
	private static final Calendar calendar = Calendar.getInstance();
	
	private AtreianPassportService() {
		CronService.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				DAOManager.getDAO(PlayerPassportsDAO.class).resetAllPassports();
				if(calendar.get(Calendar.DAY_OF_MONTH) == 1)
					DAOManager.getDAO(PlayerPassportsDAO.class).resetAllStamps();
			}
		}, "0 0 9 ? * *");
	}
	
	public void takeReward(Player player, List<Integer> timestamps, List<Integer> passportIds) {
		for (int i = 0; i < passportIds.size(); i++) {
			int passId = passportIds.get(i);
			int time = timestamps.get(i);
			Passport passport = player.getCommonData().getPassportsList().getPassport(passId, time);
			if(passport == null) {
				//AuditLogger.info(player, "Trying to get non-existing passport!");
				continue;
			}
			if(passport.isRewarded()) {
				//AuditLogger.info(player, "Trying to get passport which is already rewarded!");
				continue;
			}
			AtreianPassport atp = DATA.getAtreianPassportId(passId);
			ItemService.addItem(player, atp.getRewardItem(), atp.getRewardItemNum(), new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT, ItemUpdateType.INC_PASSPORT_ADD));
			player.getCommonData().increasePassportStamps();
			passport.setRewarded(true);
			passport.setState(PersistentState.DELETED);
			DAOManager.getDAO(PlayerPassportsDAO.class).store(player);
			player.getCommonData().getPassportsList().removePassport(passport);
		}
		onLogin(player);
	}

	public void onLogin(Player player) {
		PlayerCommonData pcd = player.getCommonData();
		PlayerAccountData pad = player.getPlayerAccount().getPlayerAccountData(player.getObjectId());
		boolean hasNewStamp = false;
		checkForNewMonth(pcd);
		
		for (AtreianPassport atp : DATA.getAll().values()) {
			if (atp.getPeriodStart().isBeforeNow() && atp.getPeriodEnd().isAfterNow()) {
				switch (atp.getAttendType()) {
					case DAILY: {
						if (checkOnlineDate(pcd)) {
							hasNewStamp = true;
							Passport passport = new Passport(atp.getId(), false, new Timestamp(System.currentTimeMillis()));
							passport.setState(PersistentState.NEW);
							pcd.getPassportsList().addPassport(passport);
						}
					}
						break;
					case CUMULATIVE: {
						if (atp.getAttendNum() == pcd.getPassportStamps() && checkOnlineDate(pcd)) {
							hasNewStamp = true;
							Passport passport = new Passport(atp.getId(), false, new Timestamp(System.currentTimeMillis()));
							passport.setState(PersistentState.NEW);
							pcd.getPassportsList().addPassport(passport);
						}
						else {
							Passport passport = new Passport(atp.getId(), false, new Timestamp(System.currentTimeMillis()));
							passport.setFakeStamp(true);
							pcd.getPassportsList().addPassport(passport);
						}
					}
						break;
					case ANNIVERSARY:
						// TODO
						break;
				}
			}
		}
		pcd.setLastStamp(new Timestamp(System.currentTimeMillis()));
		if(hasNewStamp)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NEW_PASSPORT_AVAIBLE);
		PacketSendUtility.sendPacket(player, new SM_ATREIAN_PASSPORT(pcd.getPassportsList(), pcd.getPassportStamps(), pad.getCreationDate().getYear() + 1900, pad.getCreationDate().getMonth() + 1));
	}
	
	public void sendPassport(Player player) {
		PlayerCommonData pcd = player.getCommonData();
		PlayerAccountData pad = player.getPlayerAccount().getPlayerAccountData(player.getObjectId());
		PacketSendUtility.sendPacket(player, new SM_ATREIAN_PASSPORT(pcd.getPassportsList(), pcd.getPassportStamps(), pad.getCreationDate().getYear() + 1900, pad.getCreationDate().getMonth() + 1));
	}
	
	@SuppressWarnings("deprecation")
	private void checkForNewMonth(PlayerCommonData pcd) {
		if (pcd.getLastStamp() == null) 
			return;
		
		if (pcd.getLastStamp().getMonth() != calendar.getTime().getMonth()) {
			pcd.setPassportStamps(0);
		}
	}

	private boolean checkOnlineDate(PlayerCommonData pcd) {
		return pcd.getLastStamp() == null;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final AtreianPassportService instance = new AtreianPassportService();
	}

	public static final AtreianPassportService getInstance() {
		return SingletonHolder.instance;
	}

}