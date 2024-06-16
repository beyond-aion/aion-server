package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import org.quartz.JobDetail;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.dao.AccountPassportsDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.AttendType;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.Passport;
import com.aionemu.gameserver.model.account.PassportsList;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.event.AtreianPassport;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATREIAN_PASSPORT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.World;

/**
 * @author ViAl, Luzien
 */
public class AtreianPassportService {

	private LocalDateTime expireDate;
	private JobDetail cronInfo;

	private AtreianPassportService() {
		expireDate = calculatePassportExpireDate();
		if (!isAtreianPassportDisabled()) {
			cronInfo = CronService.getInstance().schedule(() -> {
				// stop cron task if atreian passport is disabled between scheduled runs
				if (isAtreianPassportDisabled()) {
					CronService.getInstance().cancel(cronInfo);
					cronInfo = null;
					return;
				}
				boolean isFirstDayOfMonth = ServerTime.now().getDayOfMonth() == 1;
				AccountPassportsDAO.resetAllPassports();
				if (isFirstDayOfMonth)
					AccountPassportsDAO.resetAllStamps();

				World.getInstance().forEachPlayer(player -> {
					player.getAccount().setLastStamp(null);
					if (isFirstDayOfMonth)
						player.getAccount().setPassportStamps(0);
					onLogin(player);
				});
			}, "0 0 9 ? * *");
		}
	}

	public boolean isAtreianPassportDisabled() {
		return isAtreianPassportDisabled(ServerTime.now().toLocalDateTime());
	}

	private boolean isAtreianPassportDisabled(LocalDateTime checkDateTime) {
		return expireDate != null && checkDateTime.isAfter(expireDate);
	}

	private LocalDateTime findLastRewardTime() {
		Optional<AtreianPassport> lastPossibleReward = DataManager.ATREIAN_PASSPORT_DATA.getAll().values().stream()
			.filter(v -> v.getAttendType() == AttendType.DAILY || v.getAttendType() == AttendType.CUMULATIVE)
			.max(Comparator.comparing(AtreianPassport::getPeriodEnd));
		if (lastPossibleReward.isEmpty())
			return null;
		return lastPossibleReward.get().getPeriodEnd();
	}

	private LocalDateTime calculatePassportExpireDate() {
		LocalDateTime disableDateTime = findLastRewardTime();
		if (disableDateTime == null)
			return null;
		return disableDateTime.toLocalDate().atTime(LocalTime.MAX).plusDays(14);
	}

	public void takeReward(Player player, Map<Integer, Set<Integer>> passports) {
		if (isAtreianPassportDisabled())
			return;
		List<Passport> toRemove = new ArrayList<>();
		PassportsList ppl = player.getAccount().getPassportsList();
		for (Map.Entry<Integer, Set<Integer>> e : passports.entrySet()) {
			int passId = e.getKey();
			for (int time : e.getValue()) {
				Passport passport = ppl.getPassport(passId, time);
				if (passport == null) {
					AuditLogger.log(player, "tried to get non-existing passport (ID: " + passId + ", time: " + time + ")");
					continue;
				}
				if (passport.isRewarded() || passport.getPersistentState() == PersistentState.DELETED) {
					AuditLogger.log(player, "tried to get passport which is already rewarded (ID: " + passId + ")");
					continue;
				}

				if (player.getInventory().isFull()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_FULL_INVENTORY());
					break;
				}

				AtreianPassport atp = DataManager.ATREIAN_PASSPORT_DATA.getAtreianPassportId(passId);
				ItemService.addItem(player, atp.getRewardItemId(), atp.getRewardItemCount(), true,
					new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT, ItemUpdateType.INC_PASSPORT_ADD));
				passport.setRewarded(true);
				passport.setPersistentState(PersistentState.DELETED);
				ppl.removePassport(passport);
				toRemove.add(passport);
			}
		}
		if (!toRemove.isEmpty())
			AccountPassportsDAO.storePassportList(player.getAccount().getId(), toRemove);
		onLogin(player);
	}

	public void onLogin(Player player) {
		LocalDateTime now = ServerTime.now().toLocalDateTime();
		if (isAtreianPassportDisabled(now))
			return;
		Account pa = player.getAccount();
		boolean doReward = checkOnlineDate(pa) && pa.getPassportStamps() < 28;

		for (AtreianPassport atp : DataManager.ATREIAN_PASSPORT_DATA.getAll().values()) {
			if (atp.isActive() && atp.getPeriodStart().isBefore(now) && atp.getPeriodEnd().isAfter(now)) {
				switch (atp.getAttendType()) {
					case DAILY:
						if (doReward) {
							Passport passport = new Passport(atp.getId(), false, new Timestamp(System.currentTimeMillis()));
							passport.setPersistentState(PersistentState.NEW);
							pa.getPassportsList().addPassport(passport);
						}
						break;
					case CUMULATIVE:
						if (doReward && atp.getAttendNum() == pa.getPassportStamps() + 1) {
							Passport passport = new Passport(atp.getId(), false, new Timestamp(System.currentTimeMillis()));
							passport.setPersistentState(PersistentState.NEW);
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
			AccountPassportsDAO.storePassport(pa); // save account
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NEW_PASSPORT_AVAIBLE());
		}
		sendPassport(player);
	}

	private void sendPassport(Player player) {
		Account pa = player.getAccount();
		// player creation date in the servers time zone (transforming the timestamp directly would instead apply the system time zone)
		LocalDate playerCreationDate = ServerTime.atDate(player.getCreationDate()).toLocalDate();
		PacketSendUtility.sendPacket(player, new SM_ATREIAN_PASSPORT(pa.getPassportsList(), pa.getPassportStamps(), playerCreationDate));
	}

	private boolean checkOnlineDate(Account pa) {
		return pa.getLastStamp() == null;
	}

	/*
	 * Maximum 50 Passports are saved
	 */
	private void checkPassportLimit(Player player) {
		List<Passport> pl = player.getAccount().getPassportsList().getAllPassports();
		if (pl.size() <= 50)
			return;

		List<Passport> toRemove = new ArrayList<>();

		int realSize = 0;
		for (int i = pl.size() - 1; i >= 0; i--) {
			Passport pp = pl.get(i);
			if (!pp.isFakeStamp() && !pp.isRewarded()) {
				if (++realSize > 50) {
					pp.setPersistentState(PersistentState.DELETED);
					toRemove.add(pp);
				}
			}
		}
		if (!toRemove.isEmpty()) {
			pl.removeAll(toRemove);
			AccountPassportsDAO.storePassportList(player.getAccount().getId(), toRemove);
			toRemove.clear();
		}

	}

	private static class SingletonHolder {

		protected static final AtreianPassportService instance = new AtreianPassportService();
	}

	public static AtreianPassportService getInstance() {
		return SingletonHolder.instance;
	}

}
