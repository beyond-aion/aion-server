package ai.instance.drakenspire;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Estrayl
 */
@AIName("beritra_human_lv3")
public class Lv3HumanBeritraAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().applyEffect(21610, getOwner(), getOwner());
		SkillEngine.getInstance().applyEffect(21611, getOwner(), getOwner());
		SkillEngine.getInstance().applyEffect(21612, getOwner(), getOwner());
	}
}
