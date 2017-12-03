package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
@AIName("housesign")
public class HouseSignAI extends GeneralNpcAI {

	public HouseSignAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		DialogPage page = DialogPage.getByActionId(dialogActionId);
		if (page == DialogPage.NULL)
			return false;

		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getOwner().getObjectId(), page.id()));
		return true;
	}

}
