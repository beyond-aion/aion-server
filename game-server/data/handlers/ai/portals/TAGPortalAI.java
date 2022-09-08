package ai.portals;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("t_a_g_portal")
public class TAGPortalAI extends PortalDialogAI {

	public TAGPortalAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (questId != 0) {
			super.onDialogSelect(player, dialogActionId, questId, extendedRewardIndex);
			return true;
		}
		int worldId = switch (dialogActionId) {
			case SETPRO1 -> 300430000;
			case SETPRO2 -> 300420000;
			case SETPRO3 -> 300570000;
			default -> 0;
		};
		AutoGroupType agt = AutoGroupType.getAutoGroupByWorld(player.getLevel(), worldId);
		if (agt != null) {
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(agt.getTemplate().getMaskId()));
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

}
