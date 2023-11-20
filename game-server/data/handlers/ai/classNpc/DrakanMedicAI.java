package ai.classNpc;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npc.NpcRating;

/**
 * @author Cheatkiller
 */
@AIName("drakanmedic")
public class DrakanMedicAI extends DrakanPriestAI {

	public DrakanMedicAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (Rnd.chance() < 3) {
			spawnServants(getOwner().getObjectTemplate().getRating() == NpcRating.NORMAL ? 281621 : 281839, 1);
		}
	}
}
