package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller, Estrayl
 */
@AIName("calindi_surkana")
public class CalindiSurkanaAI extends NpcAI {

	private Future<?> reflectTask;
	private int calindiId;

	public CalindiSurkanaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		calindiId = getPosition().getMapId() == 300520000 ? 219359 : 236274;
		switch (getNpcId()) {
			case 730695:
			case 731629:
				startReflectBuffTask(20590);
				break;
			case 730696:
			case 731630:
				startReflectBuffTask(20591);
				break;
		}
	}

	private void startReflectBuffTask(int buffId) {
		reflectTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			Npc calindi = getPosition().getWorldMapInstance().getNpc(calindiId);
			if (isDead() || calindi == null || calindi.isDead()) {
				reflectTask.cancel(false);
				return;
			}
			SkillEngine.getInstance().applyEffectDirectly(buffId, getOwner(), getPosition().getWorldMapInstance().getNpc(calindiId));
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
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}

}
