package com.aionemu.gameserver.network.loginserver.clientpackets;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;
import com.aionemu.gameserver.services.transfers.PlayerTransfer;
import com.aionemu.gameserver.services.transfers.PlayerTransferService;

/**
 * @author KID
 */
public class CM_PTRANSFER_RESPONSE extends LsClientPacket {

	public CM_PTRANSFER_RESPONSE(int opCode) {
		super(opCode);
	}

	@Override
	protected void readImpl() {
		int actionId = this.readD();
		switch (actionId) {
			case 20: // send info
			{
				int targetAccount = readD();
				int taskId = readD();
				String name = readS();
				String account = readS();
				int len = readD();
				byte[] db = this.readB(len);
				PlayerTransfer transfer = new PlayerTransfer(taskId, targetAccount, account, name);
				transfer.setCommonData(db);
				PlayerTransferService.getInstance().putTransfer(taskId, transfer);
			}
				break;
			case 24: // send items
			{
				int taskId = readD();
				int len = readD();
				byte[] db = this.readB(len);
				PlayerTransfer transfer = PlayerTransferService.getInstance().getTransfer(taskId);
				transfer.setItemsData(db);
			}
				break;
			case 25: // send data
			{
				int taskId = readD();
				int len = readD();
				byte[] db = this.readB(len);
				PlayerTransfer transfer = PlayerTransferService.getInstance().getTransfer(taskId);
				transfer.setData(db);
			}
				break;
			case 26: // send skill
			{
				int taskId = readD();
				int len = readD();
				byte[] db = this.readB(len);
				PlayerTransfer transfer = PlayerTransferService.getInstance().getTransfer(taskId);
				transfer.setSkillData(db);
			}
				break;
			case 27: // send recipe
			{
				int taskId = readD();
				int len = readD();
				byte[] db = this.readB(len);
				PlayerTransfer transfer = PlayerTransferService.getInstance().getTransfer(taskId);
				transfer.setRecipeData(db);
			}
				break;
			case 28: // send quest
			{
				int taskId = readD();
				int len = readD();
				byte[] db = this.readB(len);
				PlayerTransfer transfer = PlayerTransferService.getInstance().getTransfer(taskId);
				transfer.setQuestData(db);
				PlayerTransferService.getInstance().cloneCharacter(taskId, transfer);
			}
				break;
			case 21:// ok
			{
				int taskId = readD();
				PlayerTransferService.getInstance().onOk(taskId);
			}
				break;
			case 22:// error
			{
				int taskId = readD();
				String reason = readS();
				PlayerTransferService.getInstance().onError(taskId, reason);
			}
				break;
			case 23:
				byte serverId = readC();
				if (NetworkConfig.GAMESERVER_ID != serverId) {
					try {
						throw new Exception("Requesting player transfer for server id " + serverId + " but this is " + NetworkConfig.GAMESERVER_ID + " omgshit!");
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					byte targetServerId = readC();
					int account = readD();
					int targetAccount = readD();
					int playerId = readD();
					int taskId = readD();
					PlayerTransferService.getInstance().startTransfer(account, targetAccount, playerId, targetServerId, taskId);
				}
				break;
		}
	}

	@Override
	protected void runImpl() {

	}
}
