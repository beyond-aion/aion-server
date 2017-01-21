package ai.portals;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Tibald
 */
@AIName("occupy_rentus_portal")
public class OccupiedRentusBasePortalAI extends PortalDialogAI {

	@Override
	protected void checkDialog(Player player) {
		if (getOwner().getNpcId() == 832991) {
			if (player.getRace() == Race.ASMODIANS) {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogAction.SELECT_ACTION_1011.id()));
			} else {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogAction.SELECTED_QUEST_REWARD3.id()));
			}
		} else if (getOwner().getNpcId() == 832992) {
			if (player.getRace() == Race.ASMODIANS) {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogAction.SELECTED_QUEST_REWARD3.id()));
			} else {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogAction.SELECT_ACTION_1011.id()));
			}
		} else {
			super.checkDialog(player);
		}
	}
}
