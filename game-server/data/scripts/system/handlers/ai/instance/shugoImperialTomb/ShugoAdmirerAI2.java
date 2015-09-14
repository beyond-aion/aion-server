package ai.instance.shugoImperialTomb;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu
 */
@AIName("shugoadmirer")
public class ShugoAdmirerAI2 extends NpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		InstanceHandler instanceHandler = getPosition().getWorldMapInstance().getInstanceHandler();
		if (dialogId == 10000) {
			switch (getNpcId()) {
				case 831110: // start stage 1
					instanceHandler.onChangeStageList(StageList.START_STAGE_1_PHASE_1);
					break;
				case 831111: // start stage 2
					instanceHandler.onChangeStageList(StageList.START_STAGE_2_PHASE_1);
					break;
				case 831112: // start stage 3
					instanceHandler.onChangeStageList(StageList.START_STAGE_3_PHASE_1);
					break;
			}
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		AI2Actions.deleteOwner(this);
		return true;
	}
}
