package ai.portals.abyssalsplinter;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;

import ai.ActionItemNpcAI;

/**
 * @author Ritsu
 */
@AIName("teleportation_device")
public class AbyssalSplinterPortalAI extends ActionItemNpcAI {

	public AbyssalSplinterPortalAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		float x = getOwner().getX();
		if (x == 302.201f)
			TeleportService.teleportTo(player, getOwner().getWorldId(), 294.632f, 732.189f, 215.854f);
		else if (x == 334.001f)
			TeleportService.teleportTo(player, getOwner().getWorldId(), 338.475f, 701.417f, 215.916f);
		else if (x == 362.192f)
			TeleportService.teleportTo(player, getOwner().getWorldId(), 373.611f, 739.125f, 215.903f);
	}

}
