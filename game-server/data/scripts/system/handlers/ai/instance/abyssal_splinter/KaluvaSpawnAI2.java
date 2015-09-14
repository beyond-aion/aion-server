package ai.instance.abyssal_splinter;

import java.util.concurrent.Future;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.world.WorldPosition;

@AIName("kaluvaspawn")
public class KaluvaSpawnAI2 extends NpcAI2 {

	private Future<?> task;

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
		if (kaluva != null && !kaluva.getLifeStats().isAlreadyDead()) {
			kaluva.getEffectController().removeEffect(19152);
		}
		AI2Actions.deleteOwner(this);
	}

	private void scheduleHatch() {
		task = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					hatchAdds();
					checkKaluva();
				}
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

}
