package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author bobobear
 */

@AIName("kahrunOrb")
public class KahrunOrbAI extends NpcAI {

	@Override
	protected void handleDialogStart(Player player) {
		if (!isSpawned(730625)) { // Portal isn't spawned
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
		}
	}

	@Override
	public boolean onDialogSelect(final Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == DialogAction.SETPRO1.id()) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			spawn(730625, 503.2197f, 516.6517f, 242.6040f, (byte) 0, 4);
		}
		return true;
	}

	private boolean isSpawned(int npcId) {
		return !getPosition().getWorldMapInstance().getNpcs(npcId).isEmpty();
	}
}
