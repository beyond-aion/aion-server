package ai.instance.kromedesTrial;

import static com.aionemu.gameserver.model.DialogAction.SELECT1_1;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Gigi
 */
@AIName("krcorpse")
public class KromedesCorpseAI extends NpcAI {

	public KromedesCorpseAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SELECT1_1) {
			if (player.getInventory().getItemCountByItemId(164000141) < 1) {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400701)); // TODO: more sys messages, but for now
																																							// not needed!
				ItemService.addItem(player, 164000141, 1);
			} else
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id()));
		}
		return true;
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}
}
