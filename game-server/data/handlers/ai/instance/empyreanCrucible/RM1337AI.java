package ai.instance.empyreanCrucible;

import java.util.stream.IntStream;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Luzien, w4terbomb
 */
@AIName("rm_1337")
public class RM1337AI extends CrucibleBossAI {

	private static final int FIRE = 282373; // S8_Summon_Fire_55_Ae
	private int attackIn, followUpIn, sparksIn;

	public RM1337AI(Npc owner) {
		super(owner);
	}

	@Override
	protected void onFightStart() {
		say(1500229);
		attackIn = 10;
		followUpIn = 0;
		sparksIn = 30;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (--attackIn <= 0) {
			if (hp > 50) {
				cast(19549, 10);
				attackIn = 15;
			} else {
				cast(19552, 10, NpcSkillTargetAttribute.RANDOM_EXCEPT_CURRENT_TARGET);
				cast(19553, 10);
				attackIn = 18;
			}
			followUpIn = 8;
		}
		if (followUpIn > 0 && --followUpIn == 0) {
			cast(19550, 10, NpcSkillTargetAttribute.RANDOM);
			rndSpawnInRange(FIRE, 0, 2);
		}
		if (--sparksIn <= 0) {
			boolean enraged = hp <= 50;
			if (enraged)
				cast(19550, 10);
			say(1500230);
			cast(19551, 10);
			ThreadPoolManager.getInstance().schedule(() -> spawnSparks(enraged), 5000);
			sparksIn = enraged ? 50 : 60;
		}
	}

	private void spawnSparks(boolean enraged) {
		if (isDead())
			return;
		IntStream.range(0, enraged ? 8 : 4).forEach(i -> rndSpawnInRange(FIRE, 0, 5));
		IntStream.range(0, enraged ? 10 : 5).forEach(i -> rndSpawnInRange(FIRE, 5, 15));
		if (enraged) {
			for (int i = 0; i < 5; i++) {
				if (randomAttacker() instanceof Creature target)
					spawn(FIRE, target.getX(), target.getY(), target.getZ(), (byte) 0);
			}
		}
	}

	@Override
	protected void handleDied() {
		say(1500231);
		super.handleDied();
	}
}
