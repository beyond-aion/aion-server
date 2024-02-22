package ai.worlds.eltnen;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.OneDmgNoActionAI;

/**
 * Spawns Chaos Dracus after the Mysterious Crate dies, and schedules Crate respawn after Dracus dies.
 * 
 * @author Neon
 */
@AIName("dracusbox")
public class DracusBox extends OneDmgNoActionAI {

	private static final int DRACUS_ID = 211800;

	public DracusBox(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		int spawnId = switch (Rnd.get(1, 3)) {
			case 1 -> 211792; // elroco
			case 2 -> 211799; // oozing clodworm
			default -> DRACUS_ID; // chaos dracus
		};

		Npc mysteriousCrate = getOwner();
		Npc spawn = (Npc) spawn(spawnId, mysteriousCrate.getX(), mysteriousCrate.getY(), mysteriousCrate.getZ(), mysteriousCrate.getHeading());
		AIActions.deleteOwner(this); // delete the huge box instantly so we can see the spawned mob
		if (spawn.getNpcId() == DRACUS_ID) {
			spawn.getObserveController().attach(new ActionObserver(ObserverType.DEATH) {

				@Override
				public void died(Creature creature) {
					AIActions.scheduleRespawn(DracusBox.this);
				}
			});
		} else {
			AIActions.scheduleRespawn(this);
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_RESPAWN -> false;
			default -> super.ask(question);
		};
	}
}
