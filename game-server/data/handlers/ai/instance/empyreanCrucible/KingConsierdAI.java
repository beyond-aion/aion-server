package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.attack.AggroTarget;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Luzien, w4terbomb
 */
@AIName("king_consierd")
public class KingConsierdAI extends CrucibleBossAI {

	private static final int BABY_CONSIERD = 282378; // IDArena_Summon_Condor_55_An
	private int cycleIn, frenzyStep;

	public KingConsierdAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void onFightStart() {
		cast(19691, 1);
		cast(17954, 29);
		cycleIn = 25;
		frenzyStep = 0;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (seconds == 240)
			cast(19624, 10, NpcSkillTargetAttribute.ME);
		if (passed(80)) {
			stunAndTurn();
			cycleIn = 25;
		} else if (passed(55)) {
			callBabies();
			cycleIn = 30;
		} else if (passed(25)) {
			cast(19690, 1);
			switchTarget(weakestAttacker());
			cycleIn = 12;
			frenzyStep = 0;
		} else if (--cycleIn <= 0) {
			if (hp > 80) {
				cast(17950, 29);
				cycleIn = 20;
			} else if (hp > 55) {
				stunAndTurn();
				cycleIn = 25;
			} else if (hp > 25) {
				callBabies();
				cycleIn = 30;
			} else {
				if (frenzyStep % 2 == 0) {
					cast(17957, 29);
					cast(17954, 29);
				} else {
					cast(19691, 1);
					cast(17956, 29);
					switchTarget(weakestAttacker());
					cast(17956, 29);
				}
				frenzyStep++;
				cycleIn = frenzyStep % 2 == 1 ? 25 : 20; // after 12 s the two steps alternate 25 s and 20 s apart
			}
		}
	}

	private void stunAndTurn() {
		cast(17951, 29);
		switchTarget(attacker(AggroTarget.THIRD_MOST_HATED));
		cast(17952, 29);
		cast(17954, 29);
	}

	private void callBabies() {
		cast(17951, 29);
		for (int i = 0; i < 2; i++) {
			if (rndSpawnInRange(BABY_CONSIERD, 1, 10) instanceof Npc baby)
				ThreadPoolManager.getInstance().schedule(() -> baby.getController().deleteIfAliveOrCancelRespawn(), 600000);
		}
		switchTarget(attacker(AggroTarget.THIRD_MOST_HATED));
		cast(17952, 29);
	}

	@Override
	protected void onFightEnd() {
		getPosition().getWorldMapInstance().getNpcs(BABY_CONSIERD).forEach(npc -> npc.getController().delete());
		if (!isDead())
			castOnSelf(19160, 1);
	}
}
