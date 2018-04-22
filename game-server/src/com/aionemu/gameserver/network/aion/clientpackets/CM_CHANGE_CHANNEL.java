package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author ATracer
 */
public class CM_CHANGE_CHANNEL extends AionClientPacket {

	private int channel;

	public CM_CHANGE_CHANNEL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		channel = readD();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		WorldMapInstance instance = activePlayer.getPosition().getWorldMapInstance();
		if (WorldConfig.WORLD_EMULATE_FASTTRACK && !instance.isBeginnerInstance()) {
			WorldMapTemplate template = instance.getTemplate();
			// channel index starts from there
			channel += template.getTwinCount() - 1;
		}
		TeleportService.changeChannel(activePlayer, channel);
	}
}
