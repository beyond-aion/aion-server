package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller, Luzien, Estrayl
 */
@AIName("calindi_summon")
public class CalindiSummonsAI extends NpcAI {

	private Future<?> task;

	public CalindiSummonsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		int delay = getDelay();
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> AIActions.useSkill(this, getSkillId()), 500, delay);
		ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(this), 15000);
	}

	private int getSkillId() {
		switch (getNpcId()) {
			case 283130:
				return 20916;
			case 283132:
				return 20914;
			case 856298:
				return 21891;
			case 856299:
				return 21892;
			default:
				return 0;
		}
	}

	private int getDelay() {
		switch (getNpcId()) {
			case 283130:
			case 856299:
				return 2000;
			case 283132:
			case 856298:
				return 500;
			default:
				return 0;
		}
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
