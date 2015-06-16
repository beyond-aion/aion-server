package ai.instance.danuarReliquary;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 *
 * @author Ritsu
 * @rework Luzien
 */
@AIName("enraged_queen_modor")
public class EnragedQueenModorAI2 extends AggressiveNpcAI2 {

   private Future<?> skillTask;
   private AtomicBoolean isHome = new AtomicBoolean(true);

   @Override
   protected void handleCreatureAggro(Creature creature) {
	  if (isHome.compareAndSet(true, false)) {
		 ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
			   sendMsg(1500743);
			   onSpawnSkills();
			}

		 }, 500);
	  }
   }

   private void startStage(int stage) {
	  if (!canAct())
		 return;
	  switch (stage) {
		 case 1:
			sendMsg(1500750);
			rendSpace(true);
			spawn(284659, 256.57727f, 278.18225f, 241.54623f, (byte) 90);
			spawn(284660, 246.65663f, 275.51996f, 241.54623f, (byte) 96);
			spawn(284660, 246.65663f, 275.51996f, 241.54623f, (byte) 96);
			spawn(284661, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
			startIceTask();
			ThreadPoolManager.getInstance().schedule(new Runnable() {
			   @Override
			   public void run() {
				  if (canAct()) {
					 startStage(2);
				  }
			   }

			}, 70000);
			break;
		 case 2:
			rendSpace(false);
			startGroundTask();
			ThreadPoolManager.getInstance().schedule(new Runnable() {
			   @Override
			   public void run() {
				  if (canAct()) {
					 cancelSkillTask();
					 startStage(3);
				  }

			   }

			}, 70000);
			break;
		 case 3:
			SkillEngine.getInstance().getSkill(getOwner(), 21170, 10, getTarget()).useSkill();
			ThreadPoolManager.getInstance().schedule(new Runnable() {
			   @Override
			   public void run() {
				  rendSpace(true);
				  spawn(284663, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
				  spawn(284663, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
				  spawn(284662, 256.57727f, 278.18225f, 241.54623f, (byte) 90);
				  spawn(284664, 246.65663f, 275.51996f, 241.54623f, (byte) 96);
				  startIceTask();
				  ThreadPoolManager.getInstance().schedule(new Runnable() {
					 @Override
					 public void run() {
						if (canAct()) {
						   cancelSkillTask();
						   rendSpace(false);
						   electrocute();
						   ThreadPoolManager.getInstance().schedule(new Runnable() {
							  @Override
							  public void run() {
								 if (canAct()) {
									startStage(1);
								 }
							  }

						   }, 8000);
						}
					 }

				  }, 35000);
			   }

			}, 3000);
			break;

	  }
   }

   private void rendSpace(boolean up) {
	  final float x = up ? 255.49063f : 255.98627f;
	  final float y = up ? 293.35785f : 259.0136f;
	  final float z = up ? 253.79933f : 241.73842f;
	  SkillEngine.getInstance().getSkill(getOwner(), 21165, 1, getOwner()).useSkill();
	  ThreadPoolManager.getInstance().schedule(new Runnable() {
		 @Override
		 public void run() {
			if (canAct()) {
			   World.getInstance().updatePosition(getOwner(), x, y, z, (byte) 90);
			   PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
			}

		 }

	  }, 2000);
   }

   private void onSpawnSkills() {
	  SkillEngine.getInstance().getSkill(getOwner(), 21171, 60, getOwner()).useSkill();

	  ThreadPoolManager.getInstance().schedule(new Runnable() {
		 @Override
		 public void run() {
			if (canAct()) {
			   SkillEngine.getInstance().getSkill(getOwner(), 21169, 60, getOwner()).useSkill();
			}
		 }

	  }, 2000);
	  ThreadPoolManager.getInstance().schedule(new Runnable() {
		 @Override
		 public void run() {
			if (canAct()) {
			   SkillEngine.getInstance().getSkill(getOwner(), 21181, 60, getOwner()).useSkill();
			}
		 }

	  }, 3000);

	  ThreadPoolManager.getInstance().schedule(new Runnable() {
		 @Override
		 public void run() {
			if (canAct()) {
			   SkillEngine.getInstance().getSkill(getOwner(), 21174, 60, getTarget()).useSkill();
			}
		 }

	  }, 6000);
	  ThreadPoolManager.getInstance().schedule(new Runnable() {
		 @Override
		 public void run() {
			if (canAct()) {
			   SkillEngine.getInstance().getSkill(getOwner(), 21175, 60, getTarget()).useSkill();
			}
		 }

	  }, 10000);
	  ThreadPoolManager.getInstance().schedule(new Runnable() {
		 @Override
		 public void run() {
			if (canAct()) {
			   startStage(1);
			}
		 }

	  }, 15000);
   }

   private void startGroundTask() {
	  cancelSkillTask();
	  skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

		 @Override
		 public void run() {
			SkillEngine.getInstance().getSkill(getOwner(), 21172, 60, getOwner()).useSkill();
			ThreadPoolManager.getInstance().schedule(new Runnable() {
			   @Override
			   public void run() {
				  if (canAct()) {
					 SkillEngine.getInstance().getSkill(getOwner(), 21173, 60, getOwner()).useSkill();
				  }
			   }

			}, 2500);
		 }

	  }, 3000, 20000);
   }

   private void electrocute() {
	  ThreadPoolManager.getInstance().schedule(new Runnable() {
		 @Override
		 public void run() {
			if (canAct()) {
			   SkillEngine.getInstance().getSkill(getOwner(), 21176, 60, getOwner()).useSkill();
			}
		 }

	  }, 3000);
	  ThreadPoolManager.getInstance().schedule(new Runnable() {
		 @Override
		 public void run() {
			if (canAct()) {
			   SkillEngine.getInstance().getSkill(getOwner(), 21229, 60, getOwner()).useSkill();
			}
		 }

	  }, 4500);
   }

   private void startIceTask() {
	  cancelSkillTask();
	  skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

		 @Override
		 public void run() {
			final Creature creature = getAggroList().getMostHated();
			SkillEngine.getInstance().getSkill(getOwner(), 21179, 1, getOwner()).useSkill();
			ThreadPoolManager.getInstance().schedule(new Runnable() {
			   @Override
			   public void run() {
				  spawn(284385, creature.getX(), creature.getY(), creature.getZ(), (byte) creature.getHeading());
			   }

			}, 1000);

		 }

	  }, 12000, 20000);
   }

   private void sendMsg(int msg) {
	  NpcShoutsService.getInstance().sendMsg(getOwner(), msg, getObjectId(), 0, 0);
   }

   private void cancelSkillTask() {
	  if (skillTask != null && !skillTask.isDone()) {
		 skillTask.cancel(true);
	  }
   }

   private boolean canAct() {
	  return (getOwner() != null && !isAlreadyDead() && !isHome.get());
   }

   @Override
   protected void handleDied() {
	  cancelSkillTask();
	  super.handleDied();
   }

   @Override
   protected void handleDespawned() {
	  super.handleDespawned();
	  cancelSkillTask();
   }

   @Override
   protected void handleBackHome() {
	  super.handleBackHome();
	  cancelSkillTask();
	  isHome.set(true);
   }
}
