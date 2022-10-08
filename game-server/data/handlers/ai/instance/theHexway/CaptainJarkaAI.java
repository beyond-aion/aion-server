package ai.instance.theHexway;

import com.aionemu.gameserver.ai.AIName;
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
		// spawn smoke on death
		WorldPosition currentPos = getPosition();
		Npc smoke = (Npc) spawn(282465, currentPos.getX(), currentPos.getY(), currentPos.getZ(), (byte) 0);
		smoke.getController().delete();

		// delete all npcs in range
		getKnownList().forEachNpc(npc -> {
			if (PositionUtil.isInRange(getOwner(), npc, 15))
				npc.getController().delete();
		});
	}

}
