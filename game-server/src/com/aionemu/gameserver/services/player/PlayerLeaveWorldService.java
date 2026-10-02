package com.aionemu.gameserver.services.player;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.dao.*;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData.LocationData;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.BindPointPosition;
import com.aionemu.gameserver.model.gameobjects.player.FriendList;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.*;
import com.aionemu.gameserver.services.RecallService.CancelReason;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.services.findgroup.FindGroupService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.GMService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer, Neon
 */
public class PlayerLeaveWorldService {

	private static final Logger log = LoggerFactory.getLogger(PlayerLeaveWorldService.class);
	private static final Map<Player, LeaveWorldTask> leaveWorldTaskByPlayer = new ConcurrentHashMap<>();

	/**
	 * Registers a player to leave the world 10 seconds after their last action. If their last action was more than 10 seconds ago, the player will
	 * leave the world immediately and the task will be run synchronously. If the player has already been registered, the existing task will not be
	 * rescheduled.
	 */
	public static void registerLeaveWorld(Player player) {
		long lastActionTimeMillis = Math.max(player.getMoveController().getLastMoveUpdate(), player.getController().getLastCombatTime());
		long millisSinceLastPlayerAction = System.currentTimeMillis() - lastActionTimeMillis;
		long waitTimeMillis = Duration.ofSeconds(10).toMillis();
		long delayMillis = Math.max(0, waitTimeMillis - millisSinceLastPlayerAction);
		leaveWorldTaskByPlayer.computeIfAbsent(player, LeaveWorldTask::new).scheduleOrRun(delayMillis);
	}

	public static boolean isLeavingWorld(int playerObjectId) {
		return leaveWorldTaskByPlayer.keySet().stream().anyMatch(p -> p.getObjectId() == playerObjectId);
	}

	public static boolean isLeavingWorld(Player player) {
		return leaveWorldTaskByPlayer.containsKey(player);
	}

	public static void processPendingLeaveWorldTasks() {
		leaveWorldTaskByPlayer.values().forEach(LeaveWorldTask::run);
	}

	private static void leaveWorld(Player player) {
		AionConnection con = player.getClientConnection();
		player.setClientConnection(null); // this sets the player semi-offline, PacketSendUtility will not send packets anymore

		WorldPosition pos = player.getPosition();
		if (pos == null || pos.getMapRegion() == null) { // ensure safe logout
			log.warn(player + " had invalid position: " + pos + " so he was reset to bind point");
			BindPointPosition bp = player.getBindPoint();
			if (bp != null)
				pos = World.getInstance().createPosition(bp.getMapId(), bp.getX(), bp.getY(), bp.getZ(), bp.getHeading(), 1);
			else {
				LocationData ld = DataManager.PLAYER_INITIAL_DATA.getSpawnLocation(player.getRace());
				pos = World.getInstance().createPosition(ld.getMapId(), ld.getX(), ld.getY(), ld.getZ(), ld.getHeading(), 1);
			}
			player.setPosition(pos);
		}

		FindGroupService.getInstance().onLogout(player);
		RecallService.getInstance().cancel(player, CancelReason.CANCELLED);
		player.getResponseRequester().denyAll();
		player.getFriendList().setStatus(FriendList.Status.OFFLINE, player.getCommonData());
		BrokerService.getInstance().removePlayerCache(player);
		ExchangeService.getInstance().cancelExchange(player);
		RepurchaseService.getInstance().removeRepurchaseItems(player);
		if (AutoGroupConfig.AUTO_GROUP_ENABLE)
			AutoGroupService.getInstance().onLogout(player);
		ConquerorAndProtectorService.getInstance().onLeaveMap(player);
		MultiClientingService.onLeaveWorld(player);
		InstanceService.onLogout(player);
		GMService.getInstance().onPlayerLogout(player);
		KiskService.getInstance().onLogout(player);

		if (player.isDead()) {
			if (player.isInInstance() || player.getWorldId() == 400030000)
				PlayerReviveService.instanceRevive(player);
			else
				PlayerReviveService.bindRevive(player);
		} else if (DuelService.getInstance().isDueling(player)) {
			DuelService.getInstance().loseDuel(player);
		}
		player.getEffectController().removeNonStorableEffectsForLogout();
		PlayerEffectsDAO.storePlayerEffects(player);
		ItemCooldownsDAO.storeItemCooldowns(player);
		PlayerLifeStatsDAO.updatePlayerLifeStat(player);

		PlayerGroupService.onPlayerLogout(player);
		PlayerAllianceService.onPlayerLogout(player);
		// fix legion warehouse exploits
		LegionService.getInstance().LegionWhUpdate(player);
		player.getEffectController().removeAllEffects(true);
		player.getLifeStats().cancelAllTasks();

		Summon summon = player.getSummon();
		if (summon != null)
			SummonsService.release(summon, UnsummonType.LOGOUT); // puts the summoning skill on cooldown, so store cooldowns afterwards
		PlayerCooldownsDAO.storePlayerCooldowns(player);
		if (player.getPet() != null)
			player.getPet().getController().delete();
		if (player.getPostman() != null)
			player.getPostman().getController().delete();

		ExpireTimerTask.getInstance().unregisterExpirables(player);
		if (player.getInteractionTask() != null)
			player.getInteractionTask().abort();

		QuestEngine.getInstance().onLogOut(new QuestEnv(null, player, 0));
		Timestamp lastOnline = new Timestamp(System.currentTimeMillis());
		player.getController().delete();
		player.getCommonData().setOnline(false);
		player.getCommonData().setLastOnline(lastOnline);
		if (player.isLegionMember()) // must be called after setOnline and setLastOnline
			LegionService.getInstance().onLogout(player);
		player.getCommonData().setX(player.getX());
		player.getCommonData().setY(player.getY());
		player.getCommonData().setZ(player.getZ());
		player.getCommonData().setHeading(player.getHeading());

		ChatServer.getInstance().sendPlayerLogout(player);

		PlayerService.storePlayer(player);

		player.getInventory().setOwner(null);
		player.getWarehouse().setOwner(null);
		player.getAccount().getAccountWarehouse().setOwner(null);

		PlayerDAO.storeOldCharacterLevel(player.getObjectId(), player.getLevel());
		PlayerDAO.storeLastOnlineTime(player.getObjectId(), lastOnline);
		PlayerDAO.onlinePlayer(player, false);

		con.setActivePlayer(null);
	}

	private static class LeaveWorldTask implements Runnable {

		private final Player player;
		private Future<?> future;
		private boolean finished;

		LeaveWorldTask(Player player) {
			this.player = player;
		}

		synchronized void scheduleOrRun(long delayMillis) {
			if (finished || future != null)
				return;
			if (delayMillis <= 0)
				run();
			else
				future = ThreadPoolManager.getInstance().schedule(this, delayMillis);
		}

		@Override
		public synchronized void run() {
			if (finished)
				return;
			finished = true;
			try {
				if (player.isOnline())
					leaveWorld(player);
				if (future != null)
					future.cancel(false);
			} catch (Exception e) {
				log.error("Error while processing leave world task for " + player, e);
			} finally {
				leaveWorldTaskByPlayer.remove(player, this);
			}
		}
	}
}
