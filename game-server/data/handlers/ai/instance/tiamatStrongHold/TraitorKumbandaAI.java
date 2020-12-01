package ai.instance.tiamatStrongHold;

import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("traitorkumbanda")
public class TraitorKumbandaAI extends AggressiveNpcAI {

	private boolean isFinalBuff;

	public TraitorKumbandaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (Rnd.chance() < 5) {
			spawnTimeAccelerator();
			spawnKumbandaGhost();
		}
		if (!isFinalBuff && getOwner().getLifeStats().getHpPercentage() <= 5) {
			isFinalBuff = true;
			AIActions.useSkill(this, 20942);
		}
	}

	private void spawnTimeAccelerator() {
		if (getPosition().getWorldMapInstance().getNpc(283086) == null) {
			SkillEngine.getInstance().getSkill(getOwner(), 20726, 55, getOwner()).useNoAnimationSkill();
			spawn(283086, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
			rndSpawn(283086, 6);
		}
	}

	private void spawnKumbandaGhost() {
		if (getPosition().getWorldMapInstance().getNpc(283085) == null && getOwner().getLifeStats().getHpPercentage() <= 50) {
			spawn(283085, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
		}
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().delete();
			}
		}
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		deleteNpcs(instance.getNpcs(283086));
		deleteNpcs(instance.getNpcs(283088));
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isFinalBuff = false;
	}

	private void rndSpawn(int npcId, int count) {
		for (int i = 0; i < count; i++)
			rndSpawnInRange(npcId, 10, 20);
	}
}
