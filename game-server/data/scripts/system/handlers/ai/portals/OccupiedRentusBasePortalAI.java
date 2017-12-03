package ai.portals;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Tibald
 */
@AIName("occupy_rentus_portal")
public class OccupiedRentusBasePortalAI extends PortalDialogAI {

	public OccupiedRentusBasePortalAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void checkDialog(Player player) {
		if (getOwner().getNpcId() == 832991) {
			if (player.getRace() == Race.ASMODIANS) {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
			} else {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
			}
		} else if (getOwner().getNpcId() == 832992) {
			if (player.getRace() == Race.ASMODIANS) {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
			} else {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
			}
		} else {
			super.checkDialog(player);
		}
	}
}
