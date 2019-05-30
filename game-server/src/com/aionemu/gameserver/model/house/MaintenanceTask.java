package com.aionemu.gameserver.model.house;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_ACQUIRE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_OWNER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.mail.MailFormatter;
import com.aionemu.gameserver.taskmanager.AbstractCronTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class MaintenanceTask extends AbstractCronTask {

	private static final Logger log = LoggerFactory.getLogger(MaintenanceTask.class);
	private static final MaintenanceTask instance = new MaintenanceTask();

	private final List<House> maintainedHouses = new ArrayList<>();

	public static final MaintenanceTask getInstance() {
		return instance;
	}

	private MaintenanceTask() {
		super(HousingConfig.HOUSE_MAINTENANCE_TIME);
	}

	@Override
	protected long getRunDelay() {
		int left = (int) (getRunTime() - System.currentTimeMillis() / 1000);
		if (left < 0)
			return 0;
		return left * 1000;
	}

	@Override
	protected String getServerTimeVariable() {
		return "houseMaintainTime";
	}

	@Override
	protected boolean canRunOnInit() {
		return false;
	}

	public boolean isMaintainTime() {
		return (getRunTime() - System.currentTimeMillis() / 1000) <= 0;
	}

	@Override
	protected void preInit() {
		log.info("Initializing House maintenance task...");
	}

	@Override
	protected void preRun() {
		updateMaintainedHouses();
		log.info("Executing House maintenance. Maintained Houses: " + maintainedHouses.size());
	}

	private void updateMaintainedHouses() {
		maintainedHouses.clear();

		if (!HousingConfig.ENABLE_HOUSE_PAY)
			return;

		Date now = new Date();
		List<House> houses = HousingService.getInstance().getCustomHouses();
		for (House house : houses) {
			if (house.getStatus() == HouseStatus.INACTIVE)
				continue;
			if (house.getOwnerId() == 0)
				continue;
			if (house.isFeePaid()) {
				if (house.getNextPay() == null || house.getNextPay().before(now)) {
					house.setFeePaid(false);
					// if never paid, just set time to the next period
					if (house.getNextPay() == null)
						house.setNextPay(new Timestamp((long) getRunTime() * 1000));
					house.save();
				} else
					continue;
			}
			maintainedHouses.add(house);
		}
	}

	@Override
	protected void executeTask() {
		if (!HousingConfig.ENABLE_HOUSE_PAY)
			return;

		// Get times based on configuration values
		long now = System.currentTimeMillis();
		long previousRun = now - getPeriod(); // usually week ago
		long beforePreviousRun = previousRun - getPeriod(); // usually two weeks ago

		for (House house : maintainedHouses) {
			if (house.isFeePaid())
				continue; // player already paid, don't check

			long payTime = house.getNextPay().getTime();
			long impoundTime = 0;
			int warnCount = 0;

			PlayerCommonData pcd = null;
			Player player = World.getInstance().findPlayer(house.getOwnerId());
			if (player == null)
				pcd = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonData(house.getOwnerId());
			else
				pcd = player.getCommonData();

			if (pcd == null) {
				// player doesn't exist already for some reasons
				log.warn("House " + house.getAddress().getId() + " had player assigned but no player exists. Auctioned.");
				putHouseToAuction(house, null);
				continue;
			}

			if (payTime <= beforePreviousRun) {
				long plusDay = beforePreviousRun - TimeUnit.DAYS.toMillis(1);
				if (payTime <= plusDay) {
					// player didn't pay after the second warning and one day passed
					impoundTime = now;
					warnCount = 3;
					putHouseToAuction(house, pcd);
				} else {
					// impoundTime = now.plusDays(1).getMillis();
					impoundTime = now + getPeriod();
					warnCount = 2;
				}
			} else if (payTime <= previousRun) {
				// player didn't pay 1 period
				// impoundTime = now.plus(getPeriod()).plusDays(1).getMillis();
				impoundTime = now + getPeriod() * 2;
				warnCount = 1;
			} else {
				continue; // should not happen
			}

			if (pcd.isOnline()) {
				if (warnCount == 3)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_SEQUESTRATE());
				else
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OVERDUE());
			}
			MailFormatter.sendHouseMaintenanceMail(house, warnCount, impoundTime);
		}
	}

	private void putHouseToAuction(House house, PlayerCommonData playerCommonData) {
		house.revokeOwner();
		HousingBidService.getInstance().addHouseToAuction(house);
		house.save();
		log.info("House " + house.getAddress().getId() + " overdued and put to auction.");
		if (playerCommonData == null)
			return;
		if (playerCommonData.isOnline()) {
			Player player = playerCommonData.getPlayer();
			// TODO: check this
			PacketSendUtility.sendPacket(player, new SM_HOUSE_ACQUIRE(player.getObjectId(), house.getAddress().getId(), false));
			PacketSendUtility.sendPacket(player, new SM_HOUSE_OWNER_INFO(player));
		}
	}

}
