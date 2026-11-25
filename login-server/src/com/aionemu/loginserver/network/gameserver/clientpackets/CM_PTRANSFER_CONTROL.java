package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.service.PlayerTransferService;

import io.netty.buffer.ByteBuf;

/**
 * @author KID
 */
public class CM_PTRANSFER_CONTROL extends GsClientPacket {

	private byte actionId;

	public CM_PTRANSFER_CONTROL(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		actionId = readC();
		switch (actionId) {
			case 1: // request transfer
			{
				int taskId = readD();
				String name = readS();
				int bytes = getRemainingBytes();
				byte[] db = readB(bytes);
				PlayerTransferService.getInstance().requestTransfer(taskId, name, db);
			}
				break;
			case 2: // ERROR
			{
				int taskId = readD();
				String reason = readS();
				PlayerTransferService.getInstance().onError(taskId, reason);
			}
				break;
			case 3: // ok
			{
				int taskId = readD();
				PlayerTransferService.getInstance().onOk(taskId);
			}
				break;
			case 4: // Task stop
			{
				int taskId = readD();
				String reason = readS();
				PlayerTransferService.getInstance().onTaskStop(taskId, reason);
			}
		}
	}

	@Override
	protected void runImpl() {
		// no actions required
	}
}
