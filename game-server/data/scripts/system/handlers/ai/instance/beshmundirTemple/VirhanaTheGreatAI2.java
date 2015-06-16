package ai.instance.beshmundirTemple;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;


/**
 * @author Luzien
 *
 */
@AIName("virhana")
public class VirhanaTheGreatAI2 extends AggressiveNpcAI2 {

	private boolean isStart;
	private int count;
	
	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (!isStart){
			isStart = true;
			scheduleRage();
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isStart = false;
	}

	private void scheduleRage() {
		if (isAlreadyDead() || !isStart) {
			return;
		}
		AI2Actions.useSkill(this, 19121);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				startRage();
			}

		}, 70000);
	}

	private void startRage() {
		if (isAlreadyDead() || !isStart) {
			return;
		}
		if (count < 12) {
			AI2Actions.useSkill(this, 18897);
			count++;

			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					startRage();
				}

			}, 10000);
		}
		else { //restart after a douzen casts
			count = 0;
			scheduleRage();
		}
	}
}
