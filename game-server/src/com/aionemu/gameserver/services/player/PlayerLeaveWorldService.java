package com.aionemu.gameserver.services.player;

import java.sql.Timestamp;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.dao.*;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData.LocationData;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.BindPointPosition;
import com.aionemu.gameserver.model.gameobjects.player.FriendList;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.clientpackets.CM_QUIT;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.*;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.services.drop.DropService;
import com.aionemu.gameserver.services.findgroup.FindGroupService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.GMService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 * @modified Neon
 */
public class PlayerLeaveWorldService {

	private static final Logger log = LoggerFactory.getLogger(PlayerLeaveWorldService.class);

	/**
	 * This method is called when a player loses client connection, e.g. when killing the process, or due to bad network connectivity.<br>
	 * <br>
	 * <b><font color='red'>NOTICE:</font> This method must only be called from {@link AionConnection#onDisconnect()} and not from anywhere else</b>
	 * 
	 * @see #leaveWorld(Player)
	 */
	public static void leaveWorldDelayed(Player player, long delayInMillis) {
		Future<?> leaveWorldTask = ThreadPoolManager.getInstance().schedule(() -> leaveWorld(player), delayInMillis);
		player.getController().addTask(TaskId.DESPAWN, leaveWorldTask);
	}

	/**
	 * This method saves a player and removes him from the world. It is called when a player leaves the game, which includes just two cases: either
	 * he goes back to char selection screen or is leaving the game (closing client).<br>
	 * <br>
	 * <b><font color='red'>NOTICE:</font> This method is called only from {@link CM_QUIT} and must not be called from anywhere else</b>
	 */
	public static void leaveWorld(Player player) {
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
		player.getResponseRequester().denyAll();
		player.getFriendList().setStatus(FriendList.Status.OFFLINE, player.getCommonData());
		BrokerService.getInstance().removePlayerCache(player);
		ExchangeService.getInstance().cancelExchange(player);
		RepurchaseService.getInstance().removeRepurchaseItems(player);
		if (AutoGroupConfig.AUTO_GROUP_ENABLE)
			AutoGroupService.getInstance().onPlayerLogOut(player);
		ConquerorAndProtectorService.getInstance().onLeaveMap(player);
		InstanceService.onLogOut(player);
		GMService.getInstance().onPlayerLogout(player);
		KiskService.getInstance().onLogout(player);

		if (player.isLooting())
			DropService.getInstance().closeDropList(player, player.getLootingNpcOid());

		if (player.isDead()) {
			if (player.isInInstance() || player.getWorldId() == 400030000)
				PlayerReviveService.instanceRevive(player);
			else
				PlayerReviveService.bindRevive(player);
		} else if (DuelService.getInstance().isDueling(player)) {
			DuelService.getInstance().loseDuel(player);
		}
		// store current effects
		DAOManager.getDAO(PlayerEffectsDAO.class).storePlayerEffects(player);
		DAOManager.getDAO(PlayerCooldownsDAO.class).storePlayerCooldowns(player);
		DAOManager.getDAO(ItemCooldownsDAO.class).storeItemCooldowns(player);
		DAOManager.getDAO(PlayerLifeStatsDAO.class).updatePlayerLifeStat(player);

		PlayerGroupService.onPlayerLogout(player);
		PlayerAllianceService.onPlayerLogout(player);
		// fix legion warehouse exploits
		LegionService.getInstance().LegionWhUpdate(player);
		player.getEffectController().removeAllEffects(true);
		player.getLifeStats().cancelAllTasks();

		Summon summon = player.getSummon();
		if (summon != null)
			SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.LOGOUT);
		if (player.getPet() != null)
			player.getPet().getController().delete();
		if (player.getPostman() != null)
			player.getPostman().getController().delete();

		ExpireTimerTask.getInstance().unregisterExpirables(player);
		if (player.getCraftingTask() != null)
			player.getCraftingTask().stop();

		if (player.isLegionMember())
			LegionService.getInstance().onLogout(player);

		QuestEngine.getInstance().onLogOut(new QuestEnv(null, player, 0));
		Timestamp lastOnline = new Timestamp(System.currentTimeMillis());
		player.getController().delete();
		player.getCommonData().setOnline(false);
		player.getCommonData().setLastOnline(lastOnline);

		ChatServer.getInstance().sendPlayerLogout(player);

		PlayerService.storePlayer(player);

		player.getInventory().setOwner(null);
		player.getWarehouse().setOwner(null);
		player.getAccount().getAccountWarehouse().setOwner(null);

		DAOManager.getDAO(PlayerDAO.class).storeOldCharacterLevel(player.getObjectId(), player.getLevel());
		DAOManager.getDAO(PlayerDAO.class).storeLastOnlineTime(player.getObjectId(), lastOnline);
		DAOManager.getDAO(PlayerDAO.class).onlinePlayer(player, false); // marks that player was fully saved and may enter world again

		con.setActivePlayer(null);
	}
}
