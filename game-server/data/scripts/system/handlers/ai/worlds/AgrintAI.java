package ai.worlds;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.OneDmgAI;

/**
 * @author xTz
 * @modified Neon
 */
@AIName("agrint")
public class AgrintAI extends OneDmgAI {

	private AtomicBoolean isSpawned = new AtomicBoolean(false);

	public AgrintAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		switch (getNpcId()) {
			case 218862:
			case 218850:
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_HF_SpringAgrintAppear());
				break;
			case 218863:
			case 218851:
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_HF_SummerAgrintAppear());
				break;
			case 218864:
			case 218852:
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_HF_FallAgrintAppear());
				break;
			case 218865:
			case 218853:
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_HF_WinterAgrintAppear());
				break;
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50) {
			if (isSpawned.compareAndSet(false, true)) {
				int npcId;
				switch (getNpcId()) {
					case 218850:
					case 218851:
					case 218852:
					case 218853:
						npcId = getNpcId() + 320;
						rndSpawnInRange(npcId, 1, 2);
						rndSpawnInRange(npcId, 1, 2);
						rndSpawnInRange(npcId, 1, 2);
						rndSpawnInRange(npcId, 1, 2);
						rndSpawnInRange(npcId, 1, 2);
						break;
					case 218862:
					case 218863:
					case 218864:
					case 218865:
						npcId = getNpcId() + 308;
						rndSpawnInRange(npcId, 1, 2);
						rndSpawnInRange(npcId, 1, 2);
						rndSpawnInRange(npcId, 1, 2);
						rndSpawnInRange(npcId, 1, 2);
						rndSpawnInRange(npcId, 1, 2);
						break;
				}
			}
		}
	}

	@Override
	protected void handleBackHome() {
		isSpawned.set(false);
		super.handleBackHome();
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
			case 218850:
				spawnChests(218874);
				break;
			case 218851:
				spawnChests(218876);
				break;
			case 218852:
				spawnChests(218878);
				break;
			case 218853:
				spawnChests(218880);
				break;
			case 218862:
				spawnChests(218882);
				break;
			case 218863:
				spawnChests(218884);
				break;
			case 218864:
				spawnChests(218886);
				break;
			case 218865:
				spawnChests(218888);
				break;
		}
		super.handleDied();
	}

}
