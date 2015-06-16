package ai.instance.darkPoeta;

import java.util.concurrent.Future;

import ai.GeneralNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 *
 * @author Ritsu
 */
@AIName("summonsgargoyle")
public class tahabatasummonsgargoyleAI2 extends GeneralNpcAI2 {

	private Future<?> eventTask;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startTimer();
	}

	@Override
	protected void handleDied() {
		cancelEventTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelEventTask();
		super.handleDespawned();
	}

	private void startTimer()
	{
		eventTask = ThreadPoolManager.getInstance().schedule(new Runnable() 
		{

			@Override
			public void run() 
			{
				SkillEngine.getInstance().getSkill(getOwner(), 18219, 50, getTarget()).useSkill();
				ThreadPoolManager.getInstance().schedule(new Runnable() 
				{

					@Override
					public void run() 
					{
						getOwner().getController().onDelete();
					}
				}, 5000);
			}
		}, 10000);
	}

	private void cancelEventTask()
	{
		if (eventTask != null && !eventTask.isDone())
		{
			eventTask.cancel(true);
		}
	}
}