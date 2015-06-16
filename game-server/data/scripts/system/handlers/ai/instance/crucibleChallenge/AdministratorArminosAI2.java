package ai.instance.crucibleChallenge;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 *
 * @author xTz
 */
@AIName("administratorarminos")
public class AdministratorArminosAI2 extends NpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == DialogAction.SETPRO1.id()) {
			if (player.isInsideZone(ZoneName.get("ILLUSION_STADIUM_1_300320000"))) {
				spawn(217827, 1250.1598f, 237.97736f, 405.3968f, (byte) 0);
				spawn(217828, 1250.1598f, 239.97736f, 405.3968f, (byte) 0);
				spawn(217829, 1250.1598f, 235.97736f, 405.3968f, (byte) 0);
			}
			else if (player.isInsideZone(ZoneName.get("ILLUSION_STADIUM_6_300320000"))) {
				spawn(217827, 1265.9661f, 793.5348f, 436.64008f, (byte) 0);
				spawn(217828, 1265.9661f, 789.5348f, 436.6402f, (byte) 0);
				spawn(217829, 1265.9661f, 791.5348f, 436.64014f, (byte) 0);
			}
			AI2Actions.deleteOwner(this);
		}
		return true;
	}
}
