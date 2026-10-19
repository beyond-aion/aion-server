package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;

/**
 * @author w4terbomb
 */
@AIName("ranger_preceptor")
public class RangerPreceptorAI extends CrucibleBossAI {

	private int focusIn;

	public RangerPreceptorAI(Npc owner) {
		super(owner);
	}

	private boolean isLight() {
		return getNpcId() == 217579;
	}

	@Override
	protected void onFightStart() {
		say(isLight() ? 1500211 : 1500213);
		focusIn = 30;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (every(seconds, 10, 15)) {
			if (passed(60) || passed(30))
				windWalk();
			else
				cast(19600, 10);
		}
		if (every(seconds, 20, 40))
			cast(19601, 15, NpcSkillTargetAttribute.RANDOM);
		if (hp < 50 && --focusIn <= 0) {
			cast(19602, 10, NpcSkillTargetAttribute.ME);
			focusIn = 30;
		}
	}

	/**
	 * Runs about for 6 seconds and comes back at a random attacker.
	 */
	private void windWalk() {
		castOnSelf(19604, 1);
		CrucibleRun.start(this, null, 6000, () -> {
			switchTarget(randomAttacker());
			cast(19579, 10);
			cast(19578, 10);
		});
	}

	@Override
	protected void handleDied() {
		say(isLight() ? 1500212 : 1500214);
		super.handleDied();
	}
}
