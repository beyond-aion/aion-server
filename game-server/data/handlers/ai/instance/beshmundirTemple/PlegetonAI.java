package ai.instance.beshmundirTemple;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Tiger0319, xTz, Gigi
 */
@AIName("plegeton")
public class PlegetonAI extends NpcAI {

	private boolean isStartTimer = false;

	public PlegetonAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			switch (getNpcId()) {
				case 799517:
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(false, 0, 0, 448, false));
					if (!isStartTimer) {
						isStartTimer = true;
						sendTimer();
						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								Npc npc = getPosition().getWorldMapInstance().getNpc(216586);
								if (npc != null && !npc.isDead()) {
									npc.getController().delete();
									PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, 0));
									getPosition().getWorldMapInstance().setDoorState(467, true);
								}
							}
						}, 420000);

					}
					TeleportService.teleportTo(player, 300170000, 958.45233f, 430.4892f, 219.80301f);
					break;
				case 799518:
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(false, 0, 0, 449, false));
					TeleportService.teleportTo(player, 300170000, 822.0199f, 465.1819f, 220.29918f);
					break;
				case 799519:
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(false, 0, 0, 450, false));
					TeleportService.teleportTo(player, 300170000, 777.1054f, 300.39005f, 219.89926f);
					break;
				case 799520:
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(false, 0, 0, 451, false));
					TeleportService.teleportTo(player, 300170000, 942.3092f, 270.91855f, 219.86185f);
					break;
			}
		}
		return true;
	}

	private void sendTimer() {
		PacketSendUtility.broadcastToMap(getOwner(), new SM_QUEST_ACTION(0, 420));
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}
}
