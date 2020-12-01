package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Created by Yeats on 20.02.2016.
 */
@AIName("aggressive_boss_summon")
public class AggressiveBossSummonNpcAI extends AggressiveNpcAI {

	public AggressiveBossSummonNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleAttackComplete() {
		super.handleAttackComplete();
		if (!isCreatorStillFighting())
			getOwner().getController().delete();
	}

	@Override
	public void handleFinishAttack() {
		getOwner().getController().delete();
	}

	private boolean isCreatorStillFighting() {
		return getKnownList().getObject(getCreatorId()) instanceof Creature creator && !creator.isDead() && creator.getAggroList().getMostHated() != null;
	}

	@Override
	public void handleDied() {
		super.handleDied();
		getOwner().getController().delete();
	}
}
