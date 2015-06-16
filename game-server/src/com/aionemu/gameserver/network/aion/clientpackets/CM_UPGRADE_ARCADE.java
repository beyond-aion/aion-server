package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.ArcadeUpgradeService;

/**
 * @author ginho1
 */
public class CM_UPGRADE_ARCADE extends AionClientPacket {

	private int action;
	private int sessionId;

	public CM_UPGRADE_ARCADE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		action = readC();
		sessionId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		switch(action) {
			case 0://get start upgrade arcade info
				ArcadeUpgradeService.getInstance().startArcadeUpgrade(player);
			break;
			case 1://open upgrade arcade
				ArcadeUpgradeService.getInstance().openArcadeUpgrade(player);
			break;
			case 2://try upgrade arcade
				ArcadeUpgradeService.getInstance().tryArcadeUpgrade(player);
			break;
			case 3://get reward
				ArcadeUpgradeService.getInstance().getReward(player);
			break;
			case 5://get reward list
				ArcadeUpgradeService.getInstance().showRewardList(player);
			break;
		}
	}
}
