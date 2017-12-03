package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Luzien
 */
@AIName("ultimateatrocity")
public class UltimateAtrocityAI extends NpcAI {

	private Future<?> task;

	public UltimateAtrocityAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		final int skill;
		switch (getNpcId()) {
			case 283237:
				skill = 20598;
				break;
			case 283244:
				skill = 21160;
				break;
			case 283240:
				skill = 21156;
				break;
			default:
				skill = 0;
		}

		if (skill == 0)
			return;

		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(UltimateAtrocityAI.this, skill);
			}
		}, 0, 2000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.deleteOwner(UltimateAtrocityAI.this);
			}
		}, 11000);
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
