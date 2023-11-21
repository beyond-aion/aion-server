package com.aionemu.gameserver.services.instance;

import static com.aionemu.gameserver.configs.main.AutoGroupConfig.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.autogroup.AutoGroup;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author ViAl, Sykra, Estrayl
 */
public class PeriodicInstanceManager {

	private static final Logger log = LoggerFactory.getLogger(PeriodicInstanceManager.class);
	private static final PeriodicInstanceManager INSTANCE = new PeriodicInstanceManager();
	private final Set<Integer> openedRegistrations = new HashSet<>();
	private final Map<Integer, Future<?>> registrationCloseTasksByMaskId = new HashMap<>();

	public static PeriodicInstanceManager getInstance() {
		return INSTANCE;
	}

	private PeriodicInstanceManager() {
		if (AutoGroupConfig.AUTO_GROUP_ENABLE) {
			scheduleRegistration(DREDGION_TIMES, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDAB1_DREADGION(), 1, DREDGION_REGISTRATION_PERIOD);
			scheduleRegistration(DREDGION_TIMES, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDDREADGION_02(), 2, DREDGION_REGISTRATION_PERIOD);
			scheduleRegistration(DREDGION_TIMES, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDDREADGION_03(), 3, DREDGION_REGISTRATION_PERIOD);
			scheduleRegistration(KAMAR_BATTLEFIELD_TIMES, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDKamar(), 107, KAMAR_BATTLEFIELD_REGISTRATION_PERIOD);
			scheduleRegistration(ENGULFED_OPHIDAN_BRIDGE_TIMES, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDLDF5_Under_01_War(), 108,
				ENGULFED_OPHIDAN_BRIDGE_REGISTRATION_PERIOD);
			scheduleRegistration(IRON_WALL_WARFRONT_TIMES, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDF5_TD_war(), 109,
				IRON_WALL_WARFRONT_REGISTRATION_PERIOD);
			scheduleRegistration(IDGEL_DOME_TIMES, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDLDF5_Fortress_Re(), 111, IDGEL_DOME_REGISTRATION_PERIOD);
		}
	}

	private void scheduleRegistration(CronExpression[] startExpressions, SM_SYSTEM_MESSAGE openingMsg, int maskId, long registrationPeriod) {
		for (CronExpression startExpression : startExpressions) {
			CronService.getInstance().schedule(() -> openRegistration(openingMsg, maskId, registrationPeriod), startExpression);
			log.info("Scheduled registration opening for {} based on cron expression: {}", AutoGroupType.getAGTByMaskId(maskId), startExpression);
			log.info("Scheduled " + AutoGroupType.getAGTByMaskId(maskId) + ": based on cron expression: " + startExpression + " Duration: "
				+ registrationPeriod + " in minutes");
		}
	}

	public synchronized boolean openRegistration(SM_SYSTEM_MESSAGE openingMsg, int maskId, long registrationPeriod) {
		if (openedRegistrations.contains(maskId))
			return false;

		openedRegistrations.add(maskId);
		broadcastRegistrationUpdate(openingMsg, maskId, false);

		registrationCloseTasksByMaskId.put(maskId, ThreadPoolManager.getInstance().schedule(() -> closeRegistration(maskId), registrationPeriod * 60000));
		log.info("Scheduled registration closing for {} in {} minutes", AutoGroupType.getAGTByMaskId(maskId), registrationPeriod);
		return true;
	}

	public synchronized boolean closeRegistration(int maskId) {
		if (!openedRegistrations.contains(maskId))
			return false;
		openedRegistrations.remove(maskId);
		broadcastRegistrationUpdate(null, maskId, true);
		AutoGroupService.getInstance().stopRegistrationsByMaskId(maskId);

		if (registrationCloseTasksByMaskId.containsKey(maskId)) {
			Future<?> closeTask = registrationCloseTasksByMaskId.remove(maskId);
			if (closeTask != null && !closeTask.isDone())
				closeTask.cancel(false);
		}
		return true;
	}

	private void broadcastRegistrationUpdate(SM_SYSTEM_MESSAGE msg, int maskId, boolean isClosed) {
		AutoGroupType type = AutoGroupType.getAGTByMaskId(maskId);
		if (type != null) {
			World.getInstance().forEachPlayer(player -> {
				if (isInLvlRange(player.getLevel(), type.getTemplate().getMinLvl(), type.getTemplate().getMaxLvl())) {
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.WND_ENTRY_ICON, isClosed));
					if (msg != null)
						PacketSendUtility.sendPacket(player, msg);
				}
			});
		}
	}

	public void checkAndSendOpenRegistrations(int objectId) {
		Player player = World.getInstance().getPlayer(objectId);
		if (player != null)
			checkAndSendOpenRegistrations(player);
	}

	public void checkAndSendOpenRegistrations(Player player) {
		for (int maskId : openedRegistrations) {
			AutoGroupType agt = AutoGroupType.getAGTByMaskId(maskId);
			if (agt != null) {
				AutoGroup data = agt.getTemplate();
				if (isInLvlRange(player.getLevel(), data.getMinLvl(), data.getMaxLvl())
					&& !player.getPortalCooldownList().isPortalUseDisabled(data.getInstanceMapId()))
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.WND_ENTRY_ICON, false));
			}
		}
	}

	public boolean isRegistrationOpen(int maskId) {
		return openedRegistrations.contains(maskId);
	}

	private boolean isInLvlRange(int playerLvl, int minLvl, int maxLvl) {
		return playerLvl >= minLvl && playerLvl <= maxLvl;
	}

	public void handleRequest(Player player, int maskId) {
		if (openedRegistrations.contains(maskId)) {
			AutoGroupType type = AutoGroupType.getAGTByMaskId(maskId);
			if (type != null) {
				AutoGroup template = type.getTemplate();
				if (isInLvlRange(player.getLevel(), template.getMinLvl(), template.getMaxLvl()))
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId));
			}
		}
	}
}
