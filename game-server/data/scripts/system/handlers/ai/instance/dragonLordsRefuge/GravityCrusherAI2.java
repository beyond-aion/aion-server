package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Cheatkiller
 *
 */
@AIName("gravitycrusher")
public class GravityCrusherAI2 extends AggressiveNpcAI2 {
   
   private Future<?> skillTask;
   private Future<?> transformTask;
   
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		final WorldMapInstance inastance = this.getPosition().getWorldMapInstance();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

		  @Override
		  public void run() {
		  	AI2Actions.targetCreature(GravityCrusherAI2.this, inastance.getPlayersInside().get(Rnd.get(inastance.getPlayersInside().size() - 1)));
		  	setStateIfNot(AIState.WALKING);
		  	getOwner().setState(1);
		  	getMoveController().moveToTargetObject();
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
				transform();
		  }
	  }, 2000);
	}
	
	private void transform() {
		transformTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (!isAlreadyDead()) {
				   if (skillTask != null)
					  skillTask.cancel(true);
					AI2Actions.useSkill(GravityCrusherAI2.this, 20967); //self destruct

					ThreadPoolManager.getInstance().schedule(new Runnable() {
						
						@Override
						public void run() {
							NpcShoutsService.getInstance().sendMsg(getOwner(), 1401554);
							spawn(283140, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) getOwner().getHeading());
							AI2Actions.deleteOwner(GravityCrusherAI2.this);
						}
					}, 3000);
					
				}
			}
		}, 30000);
	}
	
	@Override
	public void handleMoveArrived() {
	   super.handleMoveArrived();
	   skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run()	{
					AI2Actions.useSkill(GravityCrusherAI2.this, 20987);
				}
			}, 0, 5000);
	}

	private void cancelTask() {
	   if (skillTask != null && !skillTask.isCancelled())
		  skillTask.cancel(true);
	   if (transformTask != null && !transformTask.isCancelled())
		  transformTask.cancel(true);
	}
	
	@Override
	public void handleDied() {
	   super.handleDied();
	   cancelTask();
	}
	
	@Override
	public void handleDespawned() {
	   super.handleDespawned();
	   cancelTask();
	}
	
	@Override
	public int modifyMaccuracy(int value) {
		return 1200;
	}
	
	@Override
	public boolean canThink() {
	   return false;
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
