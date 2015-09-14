package ai.instance.linkgateFoundry;

import ai.ActionItemNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Сheatkiller
 */
@AIName("linkgateFoundrySecretRoomTeleport")
public class LinkgateFoundrySecretTeleportAI2 extends ActionItemNpcAI2 {

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		switchFloor(player, dialogId);
		return true;
	}

	private void switchFloor(Player player, int dialogId) {
		switch (DialogAction.getActionByDialogId(dialogId)) {
			case SETPRO1:
				TeleportService2.teleportTo(player, 301270000, 177.65f, 258.15f, 313, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
				break;
			case SETPRO2:
				TeleportService2.teleportTo(player, 301270000, 176.11f, 258.44f, 354, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
				break;
			case SETPRO3:
				TeleportService2.teleportTo(player, 301270000, 176.11f, 258.44f, 394, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
				break;
		}
	}
}
