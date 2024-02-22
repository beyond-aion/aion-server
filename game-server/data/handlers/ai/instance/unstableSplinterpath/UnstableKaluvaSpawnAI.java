package ai.instance.unstableSplinterpath;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

@AIName("unstablekaluvaspawn")
public class UnstableKaluvaSpawnAI extends NpcAI {

	private Future<?> task;

	public UnstableKaluvaSpawnAI(Npc owner) {
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
	protected void handleCreatureSee(Creature creature) {
		onMoved(creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		onMoved(creature);
	}

	private void onMoved(Creature creature) {
		if (task == null && creature instanceof Npc && ((Npc) creature).getNpcId() == 219553) {
			if (PositionUtil.isInRange(getOwner(), creature, 7)) {
				creature.getEffectController().removeEffect(19152);
				scheduleHatch();
			}
		}
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 19222, 55, getOwner()).useNoAnimationSkill();
	}

	private void checkKaluva() {
		Npc kaluva = getPosition().getWorldMapInstance().getNpc(219553);
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
		}, 28000); // schedule hatch when debuff ends(20s)
	}

	private void hatchAdds() { // 4 different spawn-formations; See Powerwiki for more information
		WorldPosition p = getPosition();
		switch (Rnd.get(1, 4)) {
			case 1:
				spawn(219572, p.getX(), p.getY(), p.getZ(), p.getHeading());
				spawn(219572, p.getX(), p.getY(), p.getZ(), p.getHeading());
				break;
			case 2:
				for (int i = 0; i < 12; i++) {
					spawn(219573, p.getX(), p.getY(), p.getZ(), p.getHeading());
				}
				break;
			case 3:
				spawn(219584, p.getX(), p.getY(), p.getZ(), p.getHeading());
				break;
			case 4:
				spawn(219572, p.getX(), p.getY(), p.getZ(), p.getHeading());
				spawn(219573, p.getX(), p.getY(), p.getZ(), p.getHeading());
				spawn(219573, p.getX(), p.getY(), p.getZ(), p.getHeading());
				spawn(219573, p.getX(), p.getY(), p.getZ(), p.getHeading());
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
