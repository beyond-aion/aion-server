package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai2.AIName;

/**
 * @author Yeats
 *
 */
@AIName("ahserion_defence_weapon")
public class AhserionDefenceWeapon extends AhserionNpcAI2 {
/*
	@Override
	public AttackIntention chooseAttackIntention() {
		VisibleObject currentTarget = getTarget();
		Creature mostHated = getAggroList().getMostHated();

		if (mostHated == null || mostHated.getLifeStats().isAlreadyDead()) {
			return AttackIntention.FINISH_ATTACK;
		}

		if (currentTarget == null || !currentTarget.getObjectId().equals(mostHated.getObjectId())) {
			onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);
		}
		
		skillId = Rnd.get(0, 1) == 0 ? 20383 : 20378;
		skillLevel = 65;
		
		return AttackIntention.SKILL_ATTACK;
	} */
}
