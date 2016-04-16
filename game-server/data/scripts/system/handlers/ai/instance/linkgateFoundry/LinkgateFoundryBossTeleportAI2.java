package ai.instance.linkgateFoundry;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI2;

/**
 * @author Cheatkiller
 */
@AIName("linkgateFoundryBossTeleport")
public class LinkgateFoundryBossTeleportAI2 extends ActionItemNpcAI2 {

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		switchWay(player, dialogId);
		return true;
	}

	private void switchWay(Player player, int dialogId) {
		switch (DialogAction.getActionByDialogId(dialogId)) {
			case SELECT_BOSS_LEVEL2:
				checkKeys(player, 1);
				break;
			case SELECT_BOSS_LEVEL3:
				checkKeys(player, 5);
				break;
			case SELECT_BOSS_LEVEL4:
				checkKeys(player, 7);
				break;
		}
	}

	private void checkKeys(Player player, long keyCount) {
		Item keys = player.getInventory().getFirstItemByItemId(185000196);
		int bossId = 0;
		int msg = 0;
		if (keys != null && keys.getItemCount() >= keyCount) {
			if (keyCount == 1) {
				bossId = 234990;
				msg = 1402440;
				spawn(player.getRace() == Race.ELYOS ? 855087 : 855088, 226.76f, 256.7708f, 312.577f, (byte) 0);
			} else if (keyCount == 5) {
				bossId = 234542;
				msg = 1402441;
				spawn(player.getRace() == Race.ELYOS ? 855087 : 855088, 226.76f, 256.7708f, 312.577f, (byte) 0);
			} else if (keyCount == 7) {
				bossId = 234991;
				msg = 1402442;
			}
			player.getInventory().decreaseByItemId(185000196, keys.getItemCount());
			spawn(bossId, 252.2439f, 259.3866f, 312.3536f, (byte) 41);
			spawn(702592, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0, 51);
			getOwner().getPosition().getWorldMapInstance().getNpc(233898).getController().onDelete();
			TeleportService2.teleportTo(player, 301270000, 211.32f, 260, 314, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			PacketSendUtility.broadcastToMap(getOwner(), msg);
			AI2Actions.deleteOwner(this);
		} else {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
		}
	}
}
