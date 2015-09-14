package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Gigi
 */
@AIName("bigorb")
public class BigOrbAI2 extends NpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		if (!isSpawned(730276)) { // Portal isn't spawned
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
		} else { // Portal is already spawned
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
		}
	}

	@Override
	public boolean onDialogSelect(final Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == DialogAction.SETPRO1.id()) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			spawn(730276, 1604.6683f, 1606.5886f, 306.8665f, (byte) 90);
		}
		return true;
	}

	private boolean isSpawned(int npcId) {
		return !getPosition().getWorldMapInstance().getNpcs(npcId).isEmpty();
	}
}
