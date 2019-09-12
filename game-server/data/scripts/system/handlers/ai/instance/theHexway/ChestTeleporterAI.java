package ai.instance.theHexway;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;

import ai.ActionItemNpcAI;

/**
 * @author Sykra
 */
@AIName("chest_teleporter")
public class ChestTeleporterAI extends ActionItemNpcAI {

	public ChestTeleporterAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (player.getWorldId() == 300700000)
			TeleportService.teleportTo(player, 300700000, 485.59f, 585.42f, 357f, (byte) 60);
	}

}
