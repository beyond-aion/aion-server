package ai.events;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
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
		player.getController().onAttack(getOwner(), player.getLifeStats().getMaxHp(), null);
		ThreadPoolManager.getInstance().schedule(() -> PacketSendUtility.sendMessage(player, "WASTED!", ChatType.BRIGHT_YELLOW_CENTER), 6000);
	}
}
