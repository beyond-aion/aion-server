package ai.instance.tallocsHollow;

import static com.aionemu.gameserver.model.DialogAction.SELECT1_1;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("writhingcocoon")
public class WrithingCocoonAI extends NpcAI {

	public WrithingCocoonAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SELECT1_1 && player.getInventory().decreaseByItemId(185000088, 1)) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			switch (getNpcId()) {
				case 730232:
					Npc npc = getPosition().getWorldMapInstance().getNpc(730233);
					if (npc != null) {
						npc.getController().delete();
					}
					spawn(799500, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(390510)); // Will you accompany me? Tell me if you will.
					break;
				case 730233:
					Npc npc1 = getPosition().getWorldMapInstance().getNpc(730232);
					if (npc1 != null) {
						npc1.getController().delete();
					}
					spawn(799501, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(390511)); // Let me know if you need my help.
					break;
			}
			AIActions.deleteOwner(this);
		} else if (dialogActionId == SELECT1_1) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1097));
		}
		return true;
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}
}
