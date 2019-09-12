package ai.instance.theHexway;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.WorldPosition;

import ai.SummonerAI;

/**
 * @author Sykra
 */
@AIName("captain_jarka")
public class CaptainJarkaAI extends SummonerAI {

	public CaptainJarkaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		Npc owner = getOwner();
		// spawn smoke on death
		WorldPosition currentPos = getPosition();
		Npc smoke = (Npc) spawn(282465, currentPos.getX(), currentPos.getY(), currentPos.getZ(), (byte) 0);
		NpcActions.delete(smoke);

		// delete all npcs in range
		for (Npc npc : currentPos.getWorldMapInstance().getNpcs())
			if (PositionUtil.isInRange(owner, npc, 15) && !npc.equals(owner))
				NpcActions.delete(npc);
	}

}
