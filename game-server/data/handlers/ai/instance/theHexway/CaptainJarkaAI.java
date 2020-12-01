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
		// spawn smoke on death
		WorldPosition currentPos = getPosition();
		NpcActions.delete(spawn(282465, currentPos.getX(), currentPos.getY(), currentPos.getZ(), (byte) 0));

		// delete all npcs in range
		currentPos.getWorldMapInstance().getNpcs().stream().filter(npc -> !npc.equals(getOwner()))
			.filter(npc -> PositionUtil.isInRange(getOwner(), npc, 15)).forEach(NpcActions::delete);
	}

}
