package ai.instance.elementisForest;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@AIName("seed_hetgolem")
public class SeedHetgolemAI2 extends AggressiveNpcAI2 {

	@Override
	public void handleDied() {
		WorldPosition p = getPosition();
		if (p != null && p.getWorldMapInstance() != null) {
			spawn(282441, p.getX(), p.getY(), p.getZ(), p.getHeading());
			Npc npc = (Npc) spawn(282465, p.getX(), p.getY(), p.getZ(), p.getHeading());
			NpcActions.delete(npc);
		}
		super.handleDied();
		AI2Actions.deleteOwner(this);

	}

}
