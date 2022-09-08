package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.autogroup.EntryRequestType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.instance.PeriodicInstanceManager;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Shepper, Guapo, nrg
 */
public class CM_AUTO_GROUP extends AionClientPacket {

	private int instanceMaskId;
	private byte windowId;
	private byte entryRequestId;

	public CM_AUTO_GROUP(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		instanceMaskId = readD();
		windowId = readC();
		entryRequestId = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!AutoGroupConfig.AUTO_GROUP_ENABLE) {
			PacketSendUtility.sendMessage(player, "Auto Group is disabled");
			return;
		}
		switch (windowId) {
			case 100:
				EntryRequestType ert = EntryRequestType.getTypeById(entryRequestId);
				if (ert == null) {
					return;
				}
				AutoGroupService.getInstance().startLooking(player, instanceMaskId, ert);
				break;
			case 101:
				AutoGroupService.getInstance().cancelRegistration(player, instanceMaskId);
				break;
			case 102:
				AutoGroupService.getInstance().pressEnter(player, instanceMaskId);
				break;
			case 103:
				AutoGroupService.getInstance().cancelEnter(player, instanceMaskId);
				break;
			case 104:
				// is sent if a player clicks the icon
				PeriodicInstanceManager.getInstance().handleRequest(player, instanceMaskId);
				break;
			case 105:
				// DredgionRegService.getInstance().failedEnterDredgion(player);
				break;
		}
	}

}
