package ai.instance.custom.eternalChallenge;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.AggressiveNpcAI;

/**
 * @author Jo, Estrayl
 */
@AIName("custom_instance_scapi")
public class CustomInstanceScapiAI extends AggressiveNpcAI {

	public CustomInstanceScapiAI(Npc owner) {
		super(owner);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 1;
	}
}
