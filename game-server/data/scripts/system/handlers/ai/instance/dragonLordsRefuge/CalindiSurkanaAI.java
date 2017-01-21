package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("calindisurkana")
public class CalindiSurkanaAI extends NpcAI {

	private Future<?> skillTask;
	Npc calindi;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		calindi = getPosition().getWorldMapInstance().getNpc(219359);
		reflect();
	}

	private void reflect() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastToMap(getOwner(), 1401543);
				SkillEngine.getInstance().applyEffectDirectly(20891, getOwner(), calindi, 0);
			}
		}, 3000, 10000);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
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
