package ai.portals;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_HOUSING_TELEPORT_CANT_USE;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.SummonedHouseNpc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_LIST;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
@AIName("friendportal")
public class FriendPortalAI extends NpcAI {

	public FriendPortalAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		SummonedHouseNpc me = (SummonedHouseNpc) getOwner();
		int playerOwner = me.getCreator().getOwnerId();

		boolean allowed = playerOwner == 0 || player.getObjectId() == playerOwner || player.getFriendList().getFriend(playerOwner) != null
			|| (player.getLegion() != null && player.getLegion().isMember(playerOwner));

		if (allowed) {
			PacketSendUtility.sendPacket(player, new SM_FRIEND_LIST()); // client needs friendlist info to list entries in housing friendlist
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getOwner().getObjectId(), DialogPage.HOUSING_FRIENDLIST.id()));
		} else {
			PacketSendUtility.sendPacket(player, STR_HOUSING_TELEPORT_CANT_USE());
		}
	}

	@Override
	protected void handleDialogFinish(Player player) {
	}
}
