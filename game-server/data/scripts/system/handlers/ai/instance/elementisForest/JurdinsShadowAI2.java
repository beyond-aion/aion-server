package ai.instance.elementisForest;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;

/**
 * @author Luzien
 */
@AIName("jurdinshadow")
public class JurdinsShadowAI2 extends AggressiveNpcAI2 {

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		AI2Actions.useSkill(this, 19404);
	}
}
