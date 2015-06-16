package ai.instance.unstableSplinterpath;

import java.util.concurrent.Future;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.WorldPosition;

@AIName("unstablekaluvaspawn")
public class UnstableKaluvaSpawnAI2 extends NpcAI2 {

	private Future<?> task;
	
	@Override
	protected void handleDied() {
		super.handleDied();
		if (task != null && !task.isDone())
			task.cancel(true);
		checkKaluva();
	}
	
	@Override
  protected void handleCreatureSee(Creature creature) {
      checkDistance(this, creature);
  }
  
  @Override
  protected void handleCreatureMoved(Creature creature) {
      checkDistance(this, creature);
  }

  private void checkDistance(NpcAI2 ai, Creature creature) {
  	Npc kaluva = getPosition().getWorldMapInstance().getNpc(219553);
     if (creature instanceof Npc) {
    	if (MathUtil.isIn3dRange(getOwner(), kaluva, 7) && task == null) {
    		kaluva.getEffectController().removeEffect(19152);
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

}
