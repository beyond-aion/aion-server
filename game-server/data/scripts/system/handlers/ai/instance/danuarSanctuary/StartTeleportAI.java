package ai.instance.danuarSanctuary;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Cheatkiller
 */
@AIName("dsiportal")
public class StartTeleportAI extends NpcAI {

	public StartTeleportAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		switchWay(player);
		return true;
	}

	private void switchWay(Player player) {
		Item key = null;
		float x = 0;
		float y = 0;
		float z = 0;
		byte h = 0;
		switch (getOwner().getNpcId()) {
			case 701871:
				key = player.getInventory().getFirstItemByItemId(185000183);
				x = 1003.27f;
				y = 1371.26f;
				z = 338;
				h = 101;
				break;
			case 701872:
				key = player.getInventory().getFirstItemByItemId(185000182);
				x = 716.89f;
				y = 980.95f;
				z = 318.71f;
				h = 108;
				break;
			case 701873:
				key = player.getInventory().getFirstItemByItemId(185000181);
				x = 1014.81f;
				y = 275.87f;
				z = 310.47f;
				h = 0;
				break;
		}

		if (key != null) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			TeleportService.teleportTo(player, player.getWorldId(), player.getInstanceId(), x, y, z, h, TeleportAnimation.FADE_OUT_BEAM);
		} else {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id()));
		}
	}
}
