package com.aionemu.gameserver.services.transfers;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.PlayerTransferConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.LegionMemberDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_PTRANSFER_CONTROL;
import com.aionemu.gameserver.services.AccountService;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * @author KID
 */
public class PlayerTransferService {

	private final Logger log = LoggerFactory.getLogger(PlayerTransferService.class);
	private final Logger textLog = LoggerFactory.getLogger("PLAYERTRANSFER");

	private static PlayerTransferService instance = new PlayerTransferService();

	public static PlayerTransferService getInstance() {
		return instance;
	}

	private Map<Integer, TransferablePlayer> transfers = new LinkedHashMap<>();
	private HashMap<Integer, PlayerTransfer> playerTransfers = new HashMap<>();
	private List<Integer> rsList = new ArrayList<>();

	public PlayerTransferService() {
		if (!PlayerTransferConfig.REMOVE_SKILL_LIST.equals("*")) {
			for (String skillId : PlayerTransferConfig.REMOVE_SKILL_LIST.split(","))
				rsList.add(Integer.parseInt(skillId));
		}
		log.info("PlayerTransferService loaded. With " + rsList.size() + " restricted skills.");
	}

	public void startTransfer(int accountId, int targetAccountId, int playerId, byte targetServerId, int taskId) {
		boolean exist = false;
		for (int id : PlayerDAO.getPlayerOidsOnAccount(accountId))
			if (id == playerId) {
				exist = true;
				break;
			}

		if (!exist) {
			log.warn("transfer #" + taskId + " player " + playerId + " is not present on account " + accountId + ".");
			LoginServer.getInstance().sendPacket(
				new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.TASK_STOP, taskId, "player " + playerId + " is not present on account " + accountId));
			return;
		}

		if (LegionMemberDAO.isIdUsed(playerId)) {
			log.warn("cannot transfer #" + taskId + " player with existing legion " + playerId + ".");
			LoginServer.getInstance().sendPacket(
				new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.TASK_STOP, taskId, "cannot transfer player with existing legion " + playerId));
			return;
		}

		PlayerCommonData common = PlayerService.getOrLoadPlayerCommonData(playerId);
		if (common.isOnline()) {
			log.warn("cannot transfer #" + taskId + " online players " + playerId + ".");
			LoginServer.getInstance().sendPacket(
				new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.TASK_STOP, taskId, "cannot transfer online players " + playerId));
			return;
		}

		if (PlayerTransferConfig.REUSE_HOURS > 0
			&& common.getLastTransferTime() + PlayerTransferConfig.REUSE_HOURS * 3600000 > System.currentTimeMillis()) {
			log.warn("cannot transfer #" + taskId + " that player so often " + playerId + ".");
			LoginServer.getInstance().sendPacket(
				new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.TASK_STOP, taskId, "cannot transfer that player so often " + playerId));
			return;
		}

		Player player = PlayerService.getPlayer(playerId, AccountService.loadAccount(accountId));
		long kinah = player.getInventory().getKinah() + player.getWarehouse().getKinah();
		if (PlayerTransferConfig.MAX_KINAH > 0 && kinah >= PlayerTransferConfig.MAX_KINAH) {
			log.warn("cannot transfer #" + taskId + " players with " + kinah + " kinah in inventory/wh.");
			LoginServer.getInstance().sendPacket(
				new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.TASK_STOP, taskId, "cannot transfer players with " + kinah + " kinah in inventory/wh."));
			return;
		}

		if (BrokerService.getInstance().hasRegisteredItems(player)) {
			log.warn("cannot transfer #" + taskId + " player while he own some items in broker.");
			LoginServer.getInstance().sendPacket(
				new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.TASK_STOP, taskId, "cannot transfer player while he own some items in broker."));
			return;
		}

		TransferablePlayer tp = new TransferablePlayer(playerId, accountId, targetAccountId);
		tp.player = player;
		tp.targetServerId = targetServerId;
		tp.accountId = accountId;
		tp.targetAccountId = targetAccountId;
		tp.taskId = taskId;
		transfers.put(taskId, tp);

		textLog.info("taskId:" + taskId + "; [StartTransfer]");
		LoginServer.getInstance().sendPacket(new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.CHARACTER_INFORMATION, tp));
		LoginServer.getInstance().sendPacket(new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.ITEMS_INFORMATION, tp));
		LoginServer.getInstance().sendPacket(new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.DATA_INFORMATION, tp));
		LoginServer.getInstance().sendPacket(new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.SKILL_INFORMATION, tp));
		LoginServer.getInstance().sendPacket(new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.RECIPE_INFORMATION, tp));
		LoginServer.getInstance().sendPacket(new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.QUEST_INFORMATION, tp));
	}

	/**
	 * sent from login to target server with character information from source server
	 */
	public void cloneCharacter(int taskId, PlayerTransfer transfer) {
		playerTransfers.remove(taskId);
		String name = transfer.getName();
		String account = transfer.getAccount();
		int targetAccountId = transfer.getTargetAccount();
		if (PlayerService.isNameUsedOrReserved(null, name)) {
			if (PlayerTransferConfig.BLOCK_SAMENAME) {
				LoginServer.getInstance().sendPacket(new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.ERROR, taskId, "Name is already in use"));
				return;
			}

			log.info("Name is already in use `" + name + "`");
			textLog.info("taskId:" + taskId + "; [CloneCharacter:!isFreeName]");
			String newName = name;

			int i = 0;
			while (PlayerService.isNameUsedOrReserved(null, newName)) {
				newName = name + "_" + ++i;
			}
			name = newName;
		}
		if (AccountService.loadAccount(targetAccountId).size() >= GSConfig.CHARACTER_LIMIT_COUNT) {
			LoginServer.getInstance().sendPacket(new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.ERROR, taskId, "No free character slots"));
			return;
		}

		Player cha = new CMT_CHARACTER_INFORMATION(transfer.getDB()).readInfo(name, targetAccountId, account, rsList, textLog);

		if (cha == null) { // something went wrong!
			log.error("clone failed #" + taskId + " `" + name + "`");
			LoginServer.getInstance().sendPacket(
				new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.ERROR, taskId, "unexpected sql error while creating a clone"));
		} else {
			if (!transfer.getName().equals(cha.getName()))
				InventoryDAO.store(ItemFactory.newItem(169670001), cha); // [Event] Name Change Ticket
			PlayerDAO.setPlayerLastTransferTime(cha.getObjectId(), System.currentTimeMillis());
			LoginServer.getInstance().sendPacket(new SM_PTRANSFER_CONTROL(SM_PTRANSFER_CONTROL.OK, taskId));
			log.info("clone successful #" + taskId + " `" + name + "`");
			textLog.info("taskId:" + taskId + "; [CloneCharacter:Done]");
		}
	}

	/**
	 * from login server to source, after response from target server
	 */
	public void onOk(int taskId) {
		TransferablePlayer tplayer = this.transfers.remove(taskId);
		textLog.info("taskId:" + taskId + "; [TransferComplete]");
		PlayerService.deletePlayerFromDB(tplayer.playerId);
	}

	/**
	 * from login server to source, after response from target server
	 */
	public void onError(int taskId, String reason) {
		this.transfers.remove(taskId);
		textLog.info("taskId:" + taskId + "; [Error. Transfer failed] " + reason);
	}

	public void putTransfer(int taskId, PlayerTransfer playerTransfer) {
		playerTransfers.put(taskId, playerTransfer);
	}

	public PlayerTransfer getTransfer(int taskId) {
		return playerTransfers.get(taskId);
	}

}
