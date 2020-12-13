package ai.instance.idgelDome;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu, Estrayl
 */
@AIName("unstable_id_energy")
public class UnstableIdeEnergyAI extends NpcAI {

	private Future<?> skillTask;

	public UnstableIdeEnergyAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleSkill(Rnd.get(10000, 30000));
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 0;
	}

	private void scheduleSkill(int delay) {
		skillTask = ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(this, 21559), delay);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21559:
				scheduleSkill(Rnd.get(10000, 30000));
				break;
		}
	}

	@Override
	protected void handleDespawned() {
		skillTask.cancel(true);
		super.handleDespawned();
	}
}
