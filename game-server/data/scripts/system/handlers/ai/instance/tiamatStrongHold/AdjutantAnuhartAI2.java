package ai.instance.tiamatStrongHold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.SkillEngine;


/**
 * @author Cheatkiller
 *
 */
@AIName("adjutantanuhart")
public class AdjutantAnuhartAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> bladeStormTask;
	protected List<Integer> percents = new ArrayList<Integer>();
	
	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if(isHome.compareAndSet(true, false))
			startBladeStormTask();
		checkPercentage(getLifeStats().getHpPercentage());
	}
	
	private void startBladeStormTask()	{
		bladeStormTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run()	{
				if (isAlreadyDead())
					cancelTask();
				else {
					startBladeStormEvent();
				}
			}
		}, 5000, 40000);
	}
	
	
	private void startBladeStormEvent() {
		shield();
		SkillEngine.getInstance().getSkill(getOwner(), 20747, 55, getOwner()).useNoAnimationSkill();
		spawn(283099, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
	}
	
	private void cancelTask() {
		if (bladeStormTask != null && !bladeStormTask.isCancelled()) {
			bladeStormTask.cancel(true);
		}
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
			   percents.remove(percent);
				switch(percent){
					case 50:
						chooseBuff(20938);
						break;
					case 25:
						chooseBuff(20939);
						break;
					case 10:
						chooseBuff(20940);
						break;
				}
				
				break;
			}
		}
	}
	
	private void chooseBuff(int buff) {
		AI2Actions.targetSelf(this);
		AI2Actions.useSkill(this, buff);
	}
	
	private void shield() {
		AI2Actions.targetSelf(this);
		AI2Actions.useSkill(this, 20749);
	}
	
	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[]{50, 25, 10});
	}
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}
	
	@Override
	protected void handleBackHome() {
		addPercent();
		super.handleBackHome();
		cancelTask();
		isHome.set(true);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	@Override
	protected void handleDied() {
		percents.clear();
		super.handleDied();
		cancelTask();
	}
}