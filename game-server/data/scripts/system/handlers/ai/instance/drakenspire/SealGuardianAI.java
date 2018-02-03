package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("seal_guardian")
public class SealGuardianAI extends AggressiveNpcAI {

	public SealGuardianAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		Player killer = getAggroList().getMostPlayerDamage();
		if (killer.isDead())
			killer = getAggroList().getList().stream()
				.filter(aggroInfo -> aggroInfo.getAttacker() instanceof Player && !((Player) aggroInfo.getAttacker()).isDead()).findFirst()
				.map(aggroInfo -> (Player) aggroInfo.getAttacker()).orElseGet(null);
		if (killer != null)
			SkillEngine.getInstance().applyEffectDirectly(21625, getOwner(), killer);
		super.handleDied();
		getOwner().getController().delete();
	}
}
