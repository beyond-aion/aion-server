package ai.instance.dragonLordsRefuge;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author bobobear
 * @modified Estrayl March 10th, 2018
 */
@AIName("IDTiamat_2_Kahrun_Talk")
public class IDTiamat_2_KahrunTalkAI extends NpcAI {

	public IDTiamat_2_KahrunTalkAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (getPosition().getWorldMapInstance().getNpcs(730625).isEmpty())
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			spawn(800430, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
			AIActions.deleteOwner(this);
		}
		return true;
	}
}
