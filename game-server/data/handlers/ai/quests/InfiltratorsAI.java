package ai.quests;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("infiltrator")
public class InfiltratorsAI extends AggressiveNpcAI {

	public InfiltratorsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		int owner = getOwner().getNpcId();
		int spawnNpc = 0;
		switch (owner) {
			case 282913:
				spawnNpc = 282914;
				break;
			case 282918:
				spawnNpc = 282920;
				break;
			case 282920:
				spawnNpc = 282922;
				break;
			case 282917:
				spawnNpc = 282915;
				break;
			case 282915:
				spawnNpc = 282916;
				break;
			case 282919:
				spawnNpc = 282921;
				break;
			case 282921:
				spawnNpc = 282923;
				break;
		}
		spawn(spawnNpc, getOwner().getSpawn().getX(), getOwner().getSpawn().getY(), getOwner().getSpawn().getZ(), getOwner().getSpawn().getHeading());
	}
}
