package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.UpgradeArcadeService;

/**
 * @author ginho1
 */
public class CM_UPGRADE_ARCADE extends AionClientPacket {

	private byte action;
	private int sessionId;

	public CM_UPGRADE_ARCADE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		action = readC();
		sessionId = readD();
	}

	@Override
	protected void runImpl() {
		if (!EventsConfig.ENABLE_EVENT_ARCADE)
			return;
		Player player = getConnection().getActivePlayer();
		switch (action) {
			case 0:// get start upgrade arcade info
				UpgradeArcadeService.getInstance().start(player, sessionId);
				break;
			case 1:// open upgrade arcade
				UpgradeArcadeService.getInstance().open(player);
				break;
			case 2:// try upgrade arcade
				UpgradeArcadeService.getInstance().startTry(player);
				break;
			case 3:// get reward
				UpgradeArcadeService.getInstance().getReward(player);
				break;
			case 4:// resume upgrade arcade
				UpgradeArcadeService.getInstance().resume(player);
				break;
			case 5:// get reward list
				UpgradeArcadeService.getInstance().showRewardList(player);
				break;
			default:
				LoggerFactory.getLogger(CM_UPGRADE_ARCADE.class).warn("Unhandled arcade action " + action);
		}
	}
}
