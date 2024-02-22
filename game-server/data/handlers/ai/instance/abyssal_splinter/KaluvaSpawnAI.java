package ai.instance.abyssal_splinter;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

@AIName("kaluvaspawn")
public class KaluvaSpawnAI extends NpcAI {

	private Future<?> task;

	public KaluvaSpawnAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (task != null && !task.isDone())
			task.cancel(true);
		checkKaluva();
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleHatch();
	}

	private void checkKaluva() {
		Npc kaluva = getPosition().getWorldMapInstance().getNpc(216950);
		if (kaluva != null && !kaluva.isDead()) {
			kaluva.getEffectController().removeEffect(19152);
		}
		AIActions.deleteOwner(this);
	}

	private void scheduleHatch() {
		task = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				hatchAdds();
				checkKaluva();
			}
		}, 22000); // schedule hatch when debuff ends(20s)
	}

	private void hatchAdds() { // 4 different spawn-formations; See Powerwiki for more information
		WorldPosition p = getPosition();
		switch (Rnd.get(1, 4)) {
			case 1:
				spawn(281911, p.getX(), p.getY(), p.getZ(), p.getHeading());
				spawn(281911, p.getX(), p.getY(), p.getZ(), p.getHeading());
				break;
			case 2:
				for (int i = 0; i < 12; i++) {
					spawn(281912, p.getX(), p.getY(), p.getZ(), p.getHeading());
				}
				break;
			case 3:
				spawn(282057, p.getX(), p.getY(), p.getZ(), p.getHeading());
				break;
			case 4:
				spawn(281911, p.getX(), p.getY(), p.getZ(), p.getHeading());
				spawn(281912, p.getX(), p.getY(), p.getZ(), p.getHeading());
				spawn(281912, p.getX(), p.getY(), p.getZ(), p.getHeading());
				spawn(281912, p.getX(), p.getY(), p.getZ(), p.getHeading());
				break;
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}

}
