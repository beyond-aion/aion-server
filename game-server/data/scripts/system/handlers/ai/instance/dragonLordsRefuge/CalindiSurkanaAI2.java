package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;


/**
 * @author Cheatkiller
 *
 */
@AIName("calindisurkana")
public class CalindiSurkanaAI2 extends NpcAI2 {
	
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
				 NpcShoutsService.getInstance().sendMsg(getOwner(), 1401543);
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
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.NEGATIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}

}