package ai.instance.tiamatStrongHold;

import java.util.concurrent.Future;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;


/**
 * @author Cheatkiller
 *
 */
@AIName("bladestorm")
public class BladeStormAI2 extends NpcAI2 {

	private Future<?> spinTask;
  
  @Override
  protected void handleSpawned() {
  	super.handleSpawned();
		spinTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run()	{
					AI2Actions.useSkill(BladeStormAI2.this, 20748);
				}
			},0, 1000);
  	despawn();
  }
  
  private void despawn() {
  	ThreadPoolManager.getInstance().schedule(new Runnable() {

  		@Override
  		public void run() {
  			getOwner().getController().onDelete();
  		}
  	}, 10000);
  }
	
	@Override
	public void handleDespawned() {
		spinTask.cancel(true);
		super.handleDespawned();
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
