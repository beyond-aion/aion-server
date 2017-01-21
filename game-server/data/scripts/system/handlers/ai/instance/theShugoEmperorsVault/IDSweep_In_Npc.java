package ai.instance.theShugoEmperorsVault;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.handler.TalkEventHandler;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;


/**
 * @author Yeats
 *
 */
@AIName("idsweep_in_npc")
public class IDSweep_In_Npc extends GeneralNpcAI {

	@Override
	protected void handleDialogStart(Player player) {
		TalkEventHandler.onSimpleTalk((NpcAI) getOwner().getAi(), player);
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getOwner().getObjectId(), 1011));
	}
	
	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == 104) {
			TeleportService.teleportTo(player, 301400000, player.getInstanceId(), 423.715f, 700.375f, 399f, (byte) 44, com.aionemu.gameserver.model.animations.TeleportAnimation.FADE_OUT_BEAM);
		}
		return true;
	}

}
