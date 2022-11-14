package ai.events;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.audit.AuditLogger;

import ai.ChestAI;

/**
 * @author Estrayl
 */
@AIName("pandoras_box")
public class FakeCakeAI extends ChestAI {

	public FakeCakeAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		punishment(player);
	}

	private void punishment(Player player) {
		AuditLogger.log(player, String.format("%s used the fake cake at %s.", player, player.getPosition()));
		getOwner().getController().die();
	}
}
