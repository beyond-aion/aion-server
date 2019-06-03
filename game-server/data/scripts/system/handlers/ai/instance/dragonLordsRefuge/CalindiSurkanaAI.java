package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 * @modified Estrayl March 8th, 2018
 */
@AIName("calindi_surkana")
public class CalindiSurkanaAI extends NpcAI {

	private Future<?> reflectTask;

	public CalindiSurkanaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getNpcId() == 730695)
			startReflectBuffTask(20590);
		else if (getNpcId() == 730696)
			startReflectBuffTask(20591);
	}

	private void startReflectBuffTask(int buffId) {
		reflectTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			Npc calindi = getPosition().getWorldMapInstance().getNpc(219359);
			if (isDead() || calindi == null || calindi.isDead()) {
				reflectTask.cancel(false);
				return;
			}
			SkillEngine.getInstance().applyEffectDirectly(buffId, getOwner(), calindi);
		}, 2500, 5000);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (reflectTask != null && !reflectTask.isCancelled())
			reflectTask.cancel(false);
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
