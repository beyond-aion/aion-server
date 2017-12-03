package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("beritra_human_lv3")
public class Lv3HumanBeritraAI extends AggressiveNpcAI {

	public Lv3HumanBeritraAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().applyEffect(21610, getOwner(), getOwner());
		SkillEngine.getInstance().applyEffect(21611, getOwner(), getOwner());
		SkillEngine.getInstance().applyEffect(21612, getOwner(), getOwner());
	}
}
