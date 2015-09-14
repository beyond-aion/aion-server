package ai.instance.kromedesTrial;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Gigi
 */
@AIName("krprisoners")
public class KromedesPrisonersAI2 extends NpcAI2 {

	@Override
	public boolean onDialogSelect(final Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == DialogAction.SETPRO1.id()) {
			AI2Actions.deleteOwner(this);
		} else if (dialogId == DialogAction.SELECT_ACTION_1012.id())
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
		return true;
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}
}
