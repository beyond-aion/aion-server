package ai.worlds.inggison;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

import ai.AggressiveNpcAI;

/**
 * This AI handles mobs which are blocking geysers in Inggison
 * 
 * @author Neon
 */
@AIName("geyserblocker")
public class GeyserBlockerAI extends AggressiveNpcAI {

	private int staticId = -1;

	public GeyserBlockerAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (staticId == -1) // no geyser was despawned on spawn, so we cannot respawn it
			return;
		SpawnTemplate spawnPoint = getOwner().getSpawn();
		spawn(700545, spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ(), (byte) 0, staticId);
		PacketSendUtility.broadcastPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_WINDBOX_TRIGGER_ON_INFO(),
			player -> PositionUtil.isInRange(getOwner(), player, 30));
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		super.handleCreatureSee(creature);
		if (creature instanceof Npc) {
			Npc npc = (Npc) creature;
			if (npc.getNpcId() == 700545 && PositionUtil.isInRange(getOwner(), npc, 5)) {
				staticId = npc.getSpawn().getStaticId();
				npc.getController().delete();
			}
		}
	}

}
