package ai.instance.beshmundirTemple;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Gigi
 */
@AIName("bigorb")
public class BigOrbAI extends NpcAI {

	@Override
	protected void handleDialogStart(Player player) {
		if (!isSpawned(730276)) { // Portal isn't spawned
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
		} else { // Portal is already spawned
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			spawn(730276, 1604.6683f, 1606.5886f, 306.8665f, (byte) 90);
		}
		return true;
	}

	private boolean isSpawned(int npcId) {
		return !getPosition().getWorldMapInstance().getNpcs(npcId).isEmpty();
	}
}
