package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.quartz.JobDetail;

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
import com.aionemu.gameserver.services.cron.CronService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.World;

/**
 * @author ViAl, Luzien, SVDNESS
 */
public class AtreianPassportService {

	private static final String DAILY_CRON_AT_09_00 = "0 0 9 ? * *";
	private static final int ATTEND_RESET_HOUR = 9;
	private final LocalDateTime expireDate;
	private JobDetail cronInfo;

	private AtreianPassportService() {
		expireDate = calculatePassportExpireDate();
		if (!isAtreianPassportDisabled()) {
			cronInfo = CronService.getInstance().schedule(() -> {
				if (isAtreianPassportDisabled()) {
					CronService.getInstance().cancel(cronInfo);
					cronInfo = null;
					return;
				}
				final boolean isFirstDayOfMonth = ServerTime.now().getDayOfMonth() == 1;
				AccountPassportsDAO.resetAllLastStamps();
				if (isFirstDayOfMonth) {
					AccountPassportsDAO.resetAllStamps();
				}
				World.getInstance().forEachPlayer(player -> {
					var acc = player.getAccount();
					acc.setLastStamp(null);
					if (isFirstDayOfMonth) {
						acc.setPassportStamps(0);
					}
					onLogin(player);
				});
			}, DAILY_CRON_AT_09_00);
		}
	}

	public boolean isAtreianPassportDisabled() {
		return isAtreianPassportDisabled(ServerTime.now().toLocalDateTime());
	}

	private boolean isAtreianPassportDisabled(LocalDateTime checkDateTime) {
		return expireDate != null && checkDateTime.isAfter(expireDate);
	}

	private LocalDateTime findLastRewardTime() {
		var lastPossibleReward = DataManager.ATREIAN_PASSPORT_DATA.getAll().values().stream()
				.filter(v -> v.getAttendType() == AttendType.DAILY || v.getAttendType() == AttendType.CUMULATIVE)
				.max(Comparator.comparing(AtreianPassport::getPeriodEnd));
		return lastPossibleReward.map(AtreianPassport::getPeriodEnd).orElse(null);
	}

	private LocalDateTime calculatePassportExpireDate() {
		var disableDateTime = findLastRewardTime();
		if (disableDateTime == null) {
			return null;
		}
		return disableDateTime.toLocalDate().atTime(LocalTime.MAX).plusDays(14);
	}

	public void takeReward(Player player, Map<Integer, Set<Integer>> passports) {
		if (isAtreianPassportDisabled()) {
			return;
		}
		List<Passport> toRemove = new ArrayList<>();
		final PassportsList ppl = player.getAccount().getPassportsList();
		for (var entry : passports.entrySet()) {
			final int passId = entry.getKey();
			for (var time : entry.getValue()) {
				var passport = ppl.getPassport(passId, time);
				if (passport == null) {
					AuditLogger.log(player, "tried to get non-existing passport (ID: " + passId + ", time: " + time + ").");
					continue;
				}
				if (passport.isRewarded() || passport.getPersistentState() == PersistentState.DELETED) {
					AuditLogger.log(player, "tried to get passport which is already rewarded (ID: " + passId + ").");
					continue;
				}
				if (player.getInventory().isFull()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_FULL_INVENTORY());
					break;
				}
				var atp = DataManager.ATREIAN_PASSPORT_DATA.getAtreianPassportId(passId);
				int minLevel = atp.getRewardPermitLevel();
				if (minLevel > 0 && player.getLevel() < minLevel) {
					String itemName = "";
					var itemTemplate = DataManager.ITEM_DATA.getItemTemplate(atp.getRewardItemId());
					if (itemTemplate != null && itemTemplate.getL10n() != null) {
						itemName = itemTemplate.getL10n();
					}
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ATTEND_REWARD_INVALID_LEVEL(minLevel, itemName));
					continue;
				}
				int expireMin = atp.getRewardExpireMinutes();
				if (expireMin > 0) {
					final Instant deadline = passport.getArriveDate().toInstant().plusSeconds(expireMin * 60L);
					if (Instant.now().isAfter(deadline)) {
						passport.setPersistentState(PersistentState.DELETED);
						ppl.removePassport(passport);
						toRemove.add(passport);
						continue;
					}
				}
				ItemService.addItem(player, atp.getRewardItemId(), atp.getRewardItemCount(), true, new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT, ItemUpdateType.INC_PASSPORT_ADD));
				passport.setRewarded(true);
				passport.setPersistentState(PersistentState.UPDATE_REQUIRED);
				toRemove.add(passport);
			}
		}
		if (!toRemove.isEmpty()) {
			AccountPassportsDAO.storePassportList(player.getAccount().getId(), toRemove);
		}
		onLogin(player);
	}

	public void onLogin(Player player) {
		final var now = ServerTime.now().toLocalDateTime();
		if (isAtreianPassportDisabled(now)) {
			return;
		}
		purgeExpiredPassports(player);
		Account pa = player.getAccount();
		final boolean doReward = checkOnlineDate(pa, now) && pa.getPassportStamps() < 28;
		for (var atp : DataManager.ATREIAN_PASSPORT_DATA.getAll().values()) {
			if (atp.isActive() && atp.getPeriodStart().isBefore(now) && atp.getPeriodEnd().isAfter(now)) {
				switch (atp.getAttendType()) {
					case DAILY -> {
						if (doReward) {
							final LocalDate attendDay = getAttendDay(now);
							if (!pa.getPassportsList().hasPassportForDay(atp.getId(), attendDay)) {
								var ts = nowTs();
								var passport = new Passport(atp.getId(), false, ts);
								passport.setPersistentState(PersistentState.NEW);
								pa.getPassportsList().addPassport(passport);
							}
						}
					}
					case CUMULATIVE -> {
						if (doReward && atp.getAttendNum() == pa.getPassportStamps() + 1) {
							var ts = nowTs();
							var passport = new Passport(atp.getId(), false, ts);
							passport.setPersistentState(PersistentState.NEW);
							pa.getPassportsList().addPassport(passport);
						} else if (!pa.getPassportsList().isPassportPresent(atp.getId())) {
							var ts = nowTs();
							var passport = new Passport(atp.getId(), false, ts);
							passport.setFakeStamp(true);
							if (atp.getAttendNum() <= pa.getPassportStamps()) {
								passport.setRewarded(true);
							}
							pa.getPassportsList().addPassport(passport);
						}
					}
					case ANNIVERSARY -> {
						int monthsAlive = getAccountAgeInMonths(player, now.toLocalDate());
						int target = atp.getAttendNum();
						if (pa.getPassportsList().isPassportPresent(atp.getId())) {
							break;
						}
						if (monthsAlive == target) {
							var ts = nowTs();
							var passport = new Passport(atp.getId(), false, ts);
							passport.setPersistentState(PersistentState.NEW);
							pa.getPassportsList().addPassport(passport);
						} else if (monthsAlive > target) {
							var ts = nowTs();
							var passport = new Passport(atp.getId(), false, ts);
							passport.setFakeStamp(true);
							passport.setRewarded(true);
							pa.getPassportsList().addPassport(passport);
						}
					}
				}
			}
		}
		if (doReward) {
			pa.increasePassportStamps();
			pa.setLastStamp(nowTs());
			checkPassportLimit(player);
			AccountPassportsDAO.storePassport(pa);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ATTEND_MSG_ATTEND_REWARD_GET());
		}
		sendPassport(player);
	}

	private void sendPassport(Player player) {
		Account pa = player.getAccount();
		LocalDate playerCreationDate = ServerTime.atDate(player.getCreationDate()).toLocalDate();
		PacketSendUtility.sendPacket(player, new SM_ATREIAN_PASSPORT(pa.getPassportsList(), pa.getPassportStamps(), playerCreationDate));
	}

	private boolean checkOnlineDate(Account pa, LocalDateTime now) {
		Timestamp last = pa.getLastStamp();
		if (last == null) {
			return true;
		}
		var lastAttendDay = getAttendDay(ServerTime.atDate(last).toLocalDateTime());
		LocalDate currentAttendDay = getAttendDay(now);
		return !currentAttendDay.equals(lastAttendDay);
	}

	private LocalDate getAttendDay(LocalDateTime serverTime) {
		return serverTime.minusHours(ATTEND_RESET_HOUR).toLocalDate();
	}

	private void checkPassportLimit(Player player) {
		Account pa = player.getAccount();
		var pl = pa.getPassportsList().getAllPassports();
		//More than 50 passports cannot be saved.
		if (pl.size() < 50) {
			return;
		}
		var oldest = pl.stream()
				.filter(pp -> !pp.isFakeStamp())
				.min(Comparator.comparing(Passport::getArriveDate))
				.orElse(null);
		if (oldest == null) {
			oldest = pl.stream()
					.min(Comparator.comparing(Passport::getArriveDate))
					.orElse(null);
		}
		if (oldest != null) {
			oldest.setPersistentState(PersistentState.DELETED);
			pa.getPassportsList().removePassport(oldest);
			AccountPassportsDAO.storePassportList(pa.getId(), List.of(oldest));
			var itemTemplate = DataManager.ITEM_DATA.getItemTemplate(oldest.getTemplate().getRewardItemId());
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ATTEND_REWARD_REMOVE_EXCESS(itemTemplate.getL10n()));
		}
	}

	private void purgeExpiredPassports(Player player) {
		Account pa = player.getAccount();
		PassportsList ppl = pa.getPassportsList();
		Instant now = ServerTime.now().toInstant();
		List<Passport> toRemove = new ArrayList<>();
		for (var pp : new ArrayList<>(ppl.getAllPassports())) {
			if (pp.isRewarded() || pp.isFakeStamp()) {
				continue;
			}
			var atp = DataManager.ATREIAN_PASSPORT_DATA.getAtreianPassportId(pp.getId());
			if (atp == null) {
				continue;
			}
			int expireMin = atp.getRewardExpireMinutes();
			if (expireMin <= 0) {
				continue;
			}
			Instant deadline = pp.getArriveDate().toInstant().plusSeconds(expireMin * 60L);
			if (now.isAfter(deadline)) {
				pp.setPersistentState(PersistentState.DELETED);
				ppl.removePassport(pp);
				toRemove.add(pp);
			}
		}
		if (!toRemove.isEmpty()) {
			AccountPassportsDAO.storePassportList(pa.getId(), toRemove);
		}
	}

	/**
	 * Calculates the number of full months between the account creation date and the given date.
	 * The calculation is based on year and month difference. If the day of the given date is
	 * earlier than the day of the creation date, the current month is considered incomplete
	 * and is not counted.
	 * The returned value is always non-negative.
	 */
	private int getAccountAgeInMonths(Player player, LocalDate now) {
		LocalDate creationDate = ServerTime.atDate(player.getCreationDate()).toLocalDate();
		int months = (now.getYear() - creationDate.getYear()) * 12 + (now.getMonthValue() - creationDate.getMonthValue());
		if (now.getDayOfMonth() < creationDate.getDayOfMonth()) {
			months--;
		}
		return Math.max(0, months);
	}

	private static Timestamp nowTs() {
		return Timestamp.from(ServerTime.now().toInstant().truncatedTo(ChronoUnit.SECONDS));
	}

	private static class SingletonHolder {
		protected static final AtreianPassportService instance = new AtreianPassportService();
	}

	public static AtreianPassportService getInstance() {
		return SingletonHolder.instance;
	}
}