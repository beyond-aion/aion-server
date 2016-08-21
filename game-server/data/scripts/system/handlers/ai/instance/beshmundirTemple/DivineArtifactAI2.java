package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;

/**
 * @author Luzien
 */
@AIName("divineartifact")
public class DivineArtifactAI2 extends AggressiveNpcAI2 {

	private boolean cooldown = false;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (!cooldown) {
			AI2Actions.useSkill(this, 18915);
			setCD();
		}
	}

	private void setCD() { // ugly hack to prevent overflow TODO: remove on AI improve
		cooldown = true;

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				cooldown = false;
			}
		}, 1000);
	}
}
