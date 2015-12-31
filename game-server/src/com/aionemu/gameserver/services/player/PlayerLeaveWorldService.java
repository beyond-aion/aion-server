package com.aionemu.gameserver.services.player;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.custom.BattleService;
import com.aionemu.gameserver.dao.HouseObjectCooldownsDAO;
import com.aionemu.gameserver.dao.ItemCooldownsDAO;
import com.aionemu.gameserver.dao.PlayerCooldownsDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.PlayerEffectsDAO;
import com.aionemu.gameserver.dao.PlayerLifeStatsDAO;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.FriendList;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.clientpackets.CM_QUIT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.ChatService;
import com.aionemu.gameserver.services.DuelService;
import com.aionemu.gameserver.services.ExchangeService;
import com.aionemu.gameserver.services.KiskService;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.services.RepurchaseService;
import com.aionemu.gameserver.services.SerialKillerService;
import com.aionemu.gameserver.services.drop.DropService;
import com.aionemu.gameserver.services.findgroup.FindGroupService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.services.toypet.PetSpawnService;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.GMService;

/**
 * @author ATracer
 * @modified Neon
 */
public class PlayerLeaveWorldService {

	private static final Logger log = LoggerFactory.getLogger(PlayerLeaveWorldService.class);

	/**
	 * This method is called when player leaves the game, which includes just two cases: either player goes back to char selection screen or it's
	 * leaving the game [closing client].<br>
	 * <br>
	 * <b><font color='red'>NOTICE:</font> This method is called only from {@link AionConnection} and {@link CM_QUIT} and must not be called from
	 * anywhere else</b>
	 */
	public static final void leaveWorld(Player player) {
		final AionConnection con = player.getClientConnection();
		log.info("Player logged out: " + player.getName() + " Account: " + (con != null ? con.getAccount().getName() : "[disconnected]"));

		BattleService.getInstance().onPlayerLogout(player);
		FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x00, player.getObjectId());
		FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x04, player.getObjectId());
		PacketSendUtility.broadcastPacket(player, new SM_DELETE(player));
		player.getResponseRequester().denyAll();
		player.getFriendList().setStatus(FriendList.Status.OFFLINE, player.getCommonData());
		BrokerService.getInstance().removePlayerCache(player);
		ExchangeService.getInstance().cancelExchange(player);
		RepurchaseService.getInstance().removeRepurchaseItems(player);
		if (AutoGroupConfig.AUTO_GROUP_ENABLE) {
			AutoGroupService.getInstance().onPlayerLogOut(player);
		}
		SerialKillerService.getInstance().onLogout(player);
		InstanceService.onLogOut(player);
		GMService.getInstance().onPlayerLogout(player);
		KiskService.getInstance().onLogout(player);	
		player.getMoveController().abortMove();

		if (player.isLooting())
			DropService.getInstance().closeDropList(player, player.getLootingNpcOid());

		// Update prison timer
		if (player.isInPrison()) {
			long prisonTimer = System.currentTimeMillis() - player.getStartPrison();
			prisonTimer = player.getPrisonTimer() - prisonTimer;
			player.setPrisonTimer(prisonTimer);
			log.debug("Update prison timer to " + prisonTimer / 1000 + " seconds !");
		}
		// store current effects
		DAOManager.getDAO(PlayerEffectsDAO.class).storePlayerEffects(player);
		DAOManager.getDAO(PlayerCooldownsDAO.class).storePlayerCooldowns(player);
		DAOManager.getDAO(ItemCooldownsDAO.class).storeItemCooldowns(player);
		DAOManager.getDAO(HouseObjectCooldownsDAO.class).storeHouseObjectCooldowns(player);
		DAOManager.getDAO(PlayerLifeStatsDAO.class).updatePlayerLifeStat(player);

		PlayerGroupService.onPlayerLogout(player);
		PlayerAllianceService.onPlayerLogout(player);
		// fix legion warehouse exploits
		LegionService.getInstance().LegionWhUpdate(player);
		player.getEffectController().removeAllEffects(true);
		player.getLifeStats().cancelAllTasks();

		if (player.getLifeStats().isAlreadyDead()) {
			if (player.isInInstance())
				PlayerReviveService.instanceRevive(player);
			else
				PlayerReviveService.bindRevive(player);
		} else if (DuelService.getInstance().isDueling(player.getObjectId())) {
			DuelService.getInstance().loseDuel(player);
		}
		Summon summon = player.getSummon();
		if (summon != null) {
			SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.LOGOUT);
		}
		PetSpawnService.dismissPet(player, true);

		if (player.getPostman() != null)
			player.getPostman().getController().onDelete();
		player.setPostman(null);

		player.setEditMode(false);

		PunishmentService.stopPrisonTask(player, true);
		PunishmentService.stopGatherableTask(player, true);

		if (player.isLegionMember())
			LegionService.getInstance().onLogout(player);

		QuestEngine.getInstance().onLogOut(new QuestEnv(null, player, 0, 0));
		Timestamp lastOnline = new Timestamp(System.currentTimeMillis());
		player.getController().delete();
		player.getCommonData().setOnline(false);
		player.getCommonData().setLastOnline(lastOnline);

		DAOManager.getDAO(PlayerDAO.class).onlinePlayer(player, false);
		DAOManager.getDAO(PlayerDAO.class).storeLastOnlineTime(player.getObjectId(), lastOnline);

		if (GSConfig.ENABLE_CHAT_SERVER)
			ChatService.onPlayerLogout(player);

		PlayerService.storePlayer(player);

		ExpireTimerTask.getInstance().removePlayer(player);
		if (player.getCraftingTask() != null)
			player.getCraftingTask().stop(true);
		player.getEquipment().setOwner(null);
		player.getInventory().setOwner(null);
		player.getWarehouse().setOwner(null);
		player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId()).setOwner(null);

		player.setClientConnection(null);
		if (con != null)
			con.setActivePlayer(null);
	}

	/**
	 * @param player
	 *          the player that left the game
	 * @param delay
	 *          the delay in seconds
	 */
	public static final void leaveWorldAfterDelay(final Player player, int delay) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				leaveWorld(player);
			}

		}, delay * 1000);
	}
}
