package ai.instance.danuarReliquary;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu, Estrayl, Yeats
 */
@AIName("vengeful_orb")
public class VengefulOrbAI extends NpcAI {

	private Future<?> skillTask;

	public VengefulOrbAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		skillTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (!getOwner().isDead())
					AIActions.useSkill(VengefulOrbAI.this, 21178);
			}
		}, 100);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21178)
			getOwner().getController().delete();
	}

	@Override
	protected void handleDespawned() {
		if (skillTask != null) {
			skillTask.cancel(false);
		}
		super.handleDespawned();
	}
}
