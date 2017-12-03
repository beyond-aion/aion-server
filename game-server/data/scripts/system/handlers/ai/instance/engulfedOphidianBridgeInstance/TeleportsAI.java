package ai.instance.engulfedOphidianBridgeInstance;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author cheatkiller
 */
@AIName("engulfedophidianteleport")
public class TeleportsAI extends ActionItemNpcAI {

	public TeleportsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (checkScroll(player)) {
			if (dialogActionId == SETPRO1) {
				// TP North
				TeleportService.teleportTo(player, 301210000, 576.3873f, 462.34897f, 618.9187f, (byte) 26);
				player.getInventory().decreaseByItemId(164000279, 1);
			} else if (dialogActionId == SETPRO2) {
				// TP South
				TeleportService.teleportTo(player, 301210000, 608.5137f, 518.11066f, 591.4151f, (byte) 0);
				player.getInventory().decreaseByItemId(164000279, 1);
			}
		} else {
			PacketSendUtility.broadcastToMap(getOwner(), 1402004);
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

	private boolean checkScroll(Player player) {
		Item key = player.getInventory().getFirstItemByItemId(164000279);
		if (key != null && key.getItemCount() >= 1) {
			return true;
		}
		return false;
	}
}
