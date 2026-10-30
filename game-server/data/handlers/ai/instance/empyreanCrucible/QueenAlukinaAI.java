package ai.instance.empyreanCrucible;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Luzien, w4terbomb
 */
@AIName("alukina_emp")
public class QueenAlukinaAI extends CrucibleBossAI {

	private static final int FIRST_PIERCE = 12, PIERCE_WITH_SHOUT = 25, PIERCE_QUIET = 9;
	private int cycleIn, pierceStep;

	public QueenAlukinaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void onFightStart() {
		cast(19687, 1);
		say(341124);
		cast(17928, 41);
		cycleIn = 18;
		pierceStep = 0;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (passed(80)) {
			cast(19687, 1);
			say(341125);
			cast(17899, 41);
			cast(17900, 41);
			switchTarget(randomAttacker());
			cycleIn = 25;
		} else if (passed(55)) {
			cast(17899, 41);
			cast(19687, 1);
			say(341126);
			cast(17280, 41);
			switchTarget(randomAttacker());
			cycleIn = FIRST_PIERCE;
			pierceStep = 0;
		} else if (passed(25)) {
			cast(17899, 41);
			cast(17901, 41);
			cast(17902, 41, NpcSkillTargetAttribute.RANDOM);
			switchTarget(weakestAttacker());
			cast(17902, 41);
			cycleIn = 20;
		} else if (--cycleIn <= 0) {
			if (hp > 80) {
				cast(17928, 41);
				cycleIn = 18;
			} else if (hp > 55) {
				say(340487);
				cast(17900, 41);
				cast(17900, 41);
				switchTarget(weakestAttacker());
				cast(17899, 41);
				cycleIn = 25;
			} else if (hp > 25) {
				boolean shout = pierceStep % 2 == 1; // after 12 s the pierces alternate 25 s (with the shout) and 9 s
				if (shout)
					say(341126);
				cast(17280, 41);
				switchTarget(randomAttacker());
				pierceStep++;
				cycleIn = pierceStep % 2 == 1 ? PIERCE_WITH_SHOUT : PIERCE_QUIET;
			} else {
				cast(17901, 41);
				cast(17902, 41);
				cast(17902, 41);
				cast(17902, 41);
				cycleIn = 20;
			}
		}
	}

	@Override
	protected void handleDied() {
		WorldPosition p = getPosition();
		for (int i = 0; i < 7; i++) {
			if (spawn(280713, p.getX() + Rnd.get(-5, 5), p.getY() + Rnd.get(-5, 5), p.getZ(), p.getHeading()) instanceof Npc spawnling)
				ThreadPoolManager.getInstance().schedule(() -> spawnling.getController().deleteIfAliveOrCancelRespawn(), 30000);
		}
		super.handleDied();
	}
}
