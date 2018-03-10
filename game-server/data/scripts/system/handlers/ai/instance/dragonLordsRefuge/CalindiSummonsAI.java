package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 * @modified Luzien, Estrayl March 8th, 2018
 */
@AIName("calindisummon")
public class CalindiSummonsAI extends NpcAI {

	private Future<?> task;

	public CalindiSummonsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		int skill = getOwner().getNpcId() == 283132 ? 20914 : 20916;
		int delay = getNpcId() == 283132 ? 500 : 2000;
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> AIActions.useSkill(this, skill), delay, delay);
		ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(this), 15000);
	}

	@Override
	public void handleDespawned() {
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
