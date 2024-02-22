package ai.instance.infinityShard;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller, Luzien, Estrayl
 */
@AIName("ide_resonator")
public class IdeResonatorAI extends NpcAI {

	private Future<?> task;

	public IdeResonatorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		Npc hyperion = getPosition().getWorldMapInstance().getNpc(231073);
		AIActions.targetCreature(this, hyperion);
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && hyperion != null && !hyperion.isDead()) {
				int firstBuff = switch (getNpcId()) {
					case 231093 -> 21381;
					case 231094 -> 21383;
					default -> 21257;// NCSoft only implemented 3 different skill IDs for those temporary buffs
				};
				AIActions.useSkill(this, firstBuff);
			}
		}, 8000);

		task = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && hyperion != null && !hyperion.isDead()) {
				int secondBuff = 0;
				if (!hyperion.getEffectController().hasAbnormalEffect(21258))
					secondBuff = 21258;
				else if (!hyperion.getEffectController().hasAbnormalEffect(21382))
					secondBuff = 21382;
				else if (!hyperion.getEffectController().hasAbnormalEffect(21384))
					secondBuff = 21384;
				else if (!hyperion.getEffectController().hasAbnormalEffect(21416))
					secondBuff = 21416;

				if (secondBuff != 0)
					AIActions.useSkill(IdeResonatorAI.this, secondBuff);
			}
		}, 18000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21258, 21382, 21384, 21416 -> SkillEngine.getInstance().applyEffectDirectly(21371, getOwner(), getOwner());
		}
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}

	@Override
	protected void handleDied() {
		cancelTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
