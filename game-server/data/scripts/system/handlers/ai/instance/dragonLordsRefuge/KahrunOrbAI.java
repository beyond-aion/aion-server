package ai.instance.dragonLordsRefuge;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author bobobear
 */

@AIName("kahrunOrb")
public class KahrunOrbAI extends NpcAI {

	public KahrunOrbAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (!isSpawned(730625)) { // Portal isn't spawned
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			spawn(730625, 503.2197f, 516.6517f, 242.6040f, (byte) 0, 4);
		}
		return true;
	}

	private boolean isSpawned(int npcId) {
		return !getPosition().getWorldMapInstance().getNpcs(npcId).isEmpty();
	}
}
