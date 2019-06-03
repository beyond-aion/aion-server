package com.aionemu.gameserver.taskmanager.tasks;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseStatus;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_ACQUIRE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_OWNER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.mail.MailFormatter;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.taskmanager.AbstractCronTask;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas, Neon
 */
public class MaintenanceTask extends AbstractCronTask {

	private static final Logger log = LoggerFactory.getLogger(MaintenanceTask.class);
	private static final MaintenanceTask instance = new MaintenanceTask();

	public static MaintenanceTask getInstance() {
		return instance;
	}

	private MaintenanceTask() {
		super(HousingConfig.HOUSE_MAINTENANCE_TIME);
		log.info("Initialized house maintenance task");
	}

	@Override
	protected void executeTask() {
		List<House> housesToMaintain = findHousesToMaintain();
		log.info("Executing house maintenance for " + housesToMaintain.size() + " houses");

		Date now = new Date();
		for (House house : housesToMaintain) {
			if (house.getNextPay() == null) { // the first week is free for newly acquired houses
				house.setNextPay(getNextRun());
				house.save();
				continue;
			} else if (house.getNextPay().after(now))
				continue;

			PlayerCommonData pcd = PlayerService.getOrLoadPlayerCommonData(house.getOwnerId());
			if (pcd == null) { // player got deleted
				putHouseToAuction(house, null);
				continue;
			}

			long compensationKinah = 0;
			Date impoundDate = calculateImpoundDate(house.getNextPay());
			if (impoundDate.getTime() <= System.currentTimeMillis()) {
				putHouseToAuction(house, pcd);
				// return 90% of the house cost (https://aion.fandom.com/wiki/Housing)
				compensationKinah = (long) (house.getDefaultAuctionPrice() * 0.9f);
			}

			MailFormatter.sendHouseMaintenanceMail(house, impoundDate.getTime(), compensationKinah);
		}
	}

	private Date calculateImpoundDate(Timestamp housePaidUntil) {
		Date paymentDueDate = Date.from(housePaidUntil.toInstant().plus(14, ChronoUnit.DAYS)); // player must pay within two weeks
		Date impoundDate = new Date();
		while (impoundDate.before(paymentDueDate)) {
			impoundDate = getNextRunAfter(impoundDate);
		}
		return impoundDate;
	}

	private List<House> findHousesToMaintain() {
		if (!HousingConfig.ENABLE_HOUSE_PAY)
			return Collections.emptyList();
		return HousingService.getInstance().getCustomHouses().stream()
			.filter(house -> house.getStatus() != HouseStatus.INACTIVE && house.getOwnerId() != 0).collect(Collectors.toList());
	}

	private void putHouseToAuction(House house, PlayerCommonData owner) {
		house.revokeOwner();
		HousingBidService.getInstance().addHouseToAuction(house);
		log.info("Auctioned house " + house.getAddress().getId() + " because " + (owner == null ? "owner got deleted." : "maintenance fee was overdue."));
		if (owner != null && owner.isOnline()) {
			Player player = owner.getPlayer();
			// TODO: check this
			PacketSendUtility.sendPacket(player, new SM_HOUSE_ACQUIRE(player.getObjectId(), house.getAddress().getId(), false));
			PacketSendUtility.sendPacket(player, new SM_HOUSE_OWNER_INFO(player));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_SEQUESTRATE());
		}
	}

}
