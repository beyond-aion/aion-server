package ai.worlds;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.OneDmgAI;

/**
 * @author xTz, Neon
 */
@AIName("agrint")
public class AgrintAI extends OneDmgAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(50);

	public AgrintAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		switch (getNpcId()) {
			case 218862, 218850 -> PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_HF_SpringAgrintAppear());
			case 218863, 218851 -> PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_HF_SummerAgrintAppear());
			case 218864, 218852 -> PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_HF_FallAgrintAppear());
			case 218865, 218853 -> PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_HF_WinterAgrintAppear());
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (getNpcId()) {
			case 218850, 218851, 218852, 218853 -> {
				int npcId = getNpcId() + 320;
				rndSpawnInRange(npcId, 1, 2);
				rndSpawnInRange(npcId, 1, 2);
				rndSpawnInRange(npcId, 1, 2);
				rndSpawnInRange(npcId, 1, 2);
				rndSpawnInRange(npcId, 1, 2);
			}
			case 218862, 218863, 218864, 218865 -> {
				int npcId = getNpcId() + 308;
				rndSpawnInRange(npcId, 1, 2);
				rndSpawnInRange(npcId, 1, 2);
				rndSpawnInRange(npcId, 1, 2);
				rndSpawnInRange(npcId, 1, 2);
				rndSpawnInRange(npcId, 1, 2);
			}
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
	}

	private void spawnChests(int npcId) {
		rndSpawnInRange(npcId, 1, 6);
		rndSpawnInRange(npcId, 1, 6);
		rndSpawnInRange(npcId, 1, 6);
		rndSpawnInRange(npcId, 1, 6);
		rndSpawnInRange(npcId, 1, 6);
		rndSpawnInRange(npcId, 1, 6);
	}

	@Override
	protected void handleDied() {
		switch (getNpcId()) {
			case 218850 -> spawnChests(218874);
			case 218851 -> spawnChests(218876);
			case 218852 -> spawnChests(218878);
			case 218853 -> spawnChests(218880);
			case 218862 -> spawnChests(218882);
			case 218863 -> spawnChests(218884);
			case 218864 -> spawnChests(218886);
			case 218865 -> spawnChests(218888);
		}
		super.handleDied();
	}

}
