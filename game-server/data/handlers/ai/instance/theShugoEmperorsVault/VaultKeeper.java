package ai.instance.theShugoEmperorsVault;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;

/**
 * @author Yeats
 */
@AIName("IDSweep_Treasure_Room")
public class VaultKeeper extends GeneralNpcAI {

	public VaultKeeper(Npc owner) {
		super(owner);
	}

	private int room = 0;
	private Map<Integer, Integer> playerAndRoom = new HashMap<>();

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		checkEntryConditions(player, dialogActionId);
		return true;
	}

	private synchronized void checkEntryConditions(Player player, int dialogActionId) {
		if (dialogActionId == SETPRO1) {
			int roomNo = room;

			if (playerAndRoom.containsKey(player.getObjectId())) {
				roomNo = playerAndRoom.get(player.getObjectId());
			}

			if (roomNo == 0) {
				playerAndRoom.put(player.getObjectId(), 0);
				room++;
				TeleportService.teleportTo(player, 301400000, player.getInstanceId(), 171.741f, 230.466f, 395f, (byte) 88, TeleportAnimation.FADE_OUT_BEAM);
			} else if (roomNo == 1) {
				playerAndRoom.put(player.getObjectId(), 1);
				room++;
				TeleportService.teleportTo(player, 301400000, player.getInstanceId(), 171.741f, 384.591f, 395f, (byte) 88, TeleportAnimation.FADE_OUT_BEAM);
			} else if (roomNo == 2) {
				playerAndRoom.put(player.getObjectId(), 2);
				room++;
				TeleportService.teleportTo(player, 301400000, player.getInstanceId(), 171.741f, 541.754f, 395f, (byte) 88, TeleportAnimation.FADE_OUT_BEAM);
			} else {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			}
		}
	}

}
