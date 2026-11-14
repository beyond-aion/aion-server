package ai.instance.empyreanCrucible;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * Assistant record keepers and sensors left behind in the previous arena and waiting room: they send stragglers after the group, the ones in the
 * arena to the arena of the next stage and the ones in the waiting room to its waiting room.
 */
@AIName("empyrean_straggler_pickup")
public class StragglerPickupAI extends NpcAI {

	public StragglerPickupAI(Npc owner) {
		super(owner);
	}

	// arena of the next stage for the arena helpers (205506…205511, 206185…206190), its waiting room for the others
	private static final float[][] TARGETS = {
		{ 1260.15f, 812.34f, 358.6056f, 90 }, { 1616.02f, 154.44f, 126f, 10 }, { 1793.92f, 796.92f, 469.36542f, 60 },
		{ 1776.79f, 1755.59f, 303.69553f, 30 }, { 1320.45f, 1738.48f, 316.1746f, 67 }, { 1762.43f, 1284.99f, 394.23764f, 15 },
		{ 1260.14f, 832.06f, 358.60562f, 43 }, { 1596.60f, 148.47f, 128.81355f, 77 }, { 1819.54f, 796.74f, 470.18304f, 27 },
		{ 1776.38f, 1724.55f, 304.039f, 113 }, { 1359.50f, 1751.80f, 319.59406f, 30 }, { 1748.26f, 1254.75f, 394.2378f, 105 } };

	private boolean isSensor() {
		return getNpcId() >= 206185;
	}

	private void sendOn(Player player) {
		float[] t = TARGETS[getNpcId() - (isSensor() ? 206185 : 205506)];
		TeleportService.teleportTo(player, getPosition().getWorldMapInstance(), t[0], t[1], t[2], (byte) t[3]);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (!isSensor())
			PacketSendUtility.broadcastMessage(getOwner(), 1111506, 1000);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (isSensor() && creature instanceof Player player && !player.isDead() && PositionUtil.isInRange(getOwner(), player, 10))
			sendOn(player);
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (!isSensor())
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			sendOn(player);
		}
		return true;
	}
}
