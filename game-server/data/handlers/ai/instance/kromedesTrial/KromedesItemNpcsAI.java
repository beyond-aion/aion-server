package ai.instance.kromedesTrial;

import static com.aionemu.gameserver.model.DialogAction.SELECT1_1;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Gigi, xTz
 */
@AIName("krobject")
public class KromedesItemNpcsAI extends ActionItemNpcAI {

	public KromedesItemNpcsAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SELECT1_1) {
			switch (getNpcId()) {
				case 730325:
					if (player.getInventory().getItemCountByItemId(164000142) < 1) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
						ItemService.addItem(player, 164000142, 1);
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDCROMEDE_SKILL_01()); // TODO: more sys messages
					} else
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id()));
					break;
				case 730340:
					if (player.getInventory().getItemCountByItemId(164000140) < 1) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
						ItemService.addItem(player, 164000140, 1);
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDCROMEDE_SKILL_01()); // TODO: more sys messages
					} else
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id()));
					break;
				case 730341:
					if (player.getInventory().getItemCountByItemId(164000143) < 1) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
						ItemService.addItem(player, 164000143, 1);
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDCROMEDE_SKILL_01()); // TODO: more sys messages
					} else
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id()));
					break;
			}
		}
		return true;
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}
}
