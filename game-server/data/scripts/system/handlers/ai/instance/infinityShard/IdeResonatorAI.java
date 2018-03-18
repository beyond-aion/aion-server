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
 * @author Cheatkiller
 * @modified Luzien, Estrayl March 6th, 2018
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
		AIActions.targetCreature(IdeResonatorAI.this, hyperion);
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && hyperion != null && !hyperion.isDead()) {
				int firstBuff = 0;
				switch (getNpcId()) {
					case 231093:
						firstBuff = 21381;
						break;
					case 231094:
						firstBuff = 21383;
						break;
					default: // NCSoft only implemented 3 different skill IDs for those temporary buffs
						firstBuff = 21257;
						break;
				}
				AIActions.useSkill(IdeResonatorAI.this, firstBuff);
			}
		}, 8000);
		task = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && !getOwner().getLifeStats().isAboutToDie() && hyperion != null && !hyperion.isDead()) {
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
					AIActions.useSkill(this, secondBuff);
			}
		}, 18000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		switch (skillTemplate.getSkillId()) {
			case 21258:
			case 21382:
			case 21384:
			case 21416:
				SkillEngine.getInstance().applyEffectDirectly(21371, getOwner(), getOwner());
				break;
		}
	}

	@Override
	protected void handleDied() {
		task.cancel(true);
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		if (task != null && !task.isCancelled())
			task.cancel(true);
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
