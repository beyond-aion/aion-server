package ai.instance.empyreanCrucible;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import java.util.function.Consumer;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.model.instance.playerreward.CruciblePlayerReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("empyreanarbiter")
public class EmpyreanArbiterAI extends NpcAI {

	public EmpyreanArbiterAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (player.getInventory().getFirstItemByItemId(186000124) != null) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
		} else {
			// to do
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		int instanceId = getPosition().getInstanceId();

		if (dialogActionId == SETPRO1 && player.getInventory().decreaseByItemId(186000124, 1)) {
			switch (getNpcId()) {
				case 799573:
					TeleportService.teleportTo(player, 300300000, instanceId, 358.2547f, 349.26443f, 96.09108f, (byte) 59);
					break;
				case 205426:
					TeleportService.teleportTo(player, 300300000, instanceId, 1260.15f, 812.34f, 358.6056f, (byte) 90);
					break;
				case 205427:
					TeleportService.teleportTo(player, 300300000, instanceId, 1616.0248f, 154.43837f, 126f, (byte) 10);
					break;
				case 205428:
					TeleportService.teleportTo(player, 300300000, instanceId, 1793.9233f, 796.92f, 469.36542f, (byte) 60);
					break;
				case 205429:
					TeleportService.teleportTo(player, 300300000, instanceId, 1776.4169f, 1749.9952f, 303.69553f, (byte) 0);
					break;
				case 205430:
					TeleportService.teleportTo(player, 300300000, instanceId, 1328.935f, 1742.0771f, 316.74188f, (byte) 0);
					break;
				case 205431:
					TeleportService.teleportTo(player, 300300000, instanceId, 1760.9441f, 1278.033f, 394.23764f, (byte) 0);
					break;
			}
			InstanceScore<?> instance = getPosition().getWorldMapInstance().getInstanceHandler().getInstanceScore();
			if (instance != null) {
				CruciblePlayerReward reward = (CruciblePlayerReward) instance.getPlayerReward(player.getObjectId());
				if (reward != null) {
					reward.setPlayerDefeated(false);
				}
			}
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));

			getPosition().getWorldMapInstance().forEachPlayer(new Consumer<Player>() {

				@Override
				public void accept(Player p) {
					PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_MSG_FRIENDLY_MOVE_COMBATAREA_IDARENA(player.getName()));
				}

			});
		}
		return true;
	}
}
