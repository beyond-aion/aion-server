package ai.instance.empyreanCrucible;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Golden eye mantutu of stage 5: every 40 seconds it grows hungry or thirsty. The feed and water supply devices, pulled by two players at once, put
 * out feed or water that it goes after; eating the right one removes its buff, the wrong one or one killed by players makes it crave even more.
 */
@AIName("empyrean_manduri_giant")
public class ManduriGiantAI extends CrucibleBossAI {

	public static final int FEED = 281128, WATER = 281129;
	private volatile boolean hungry, thirsty;
	private boolean lowHp, midHp, highHp;

	public ManduriGiantAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void onFightStart() {
		hungry = thirsty = false;
		lowHp = midHp = highHp = false;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (every(seconds, 5, 40)) {
			boolean hunger = Rnd.nextBoolean();
			cast(hunger ? 18180 : 18181, 36, NpcSkillTargetAttribute.ME); // Hunger / Thirst
			ThreadPoolManager.getInstance().schedule(() -> {
				if (hunger)
					hungry = true;
				else
					thirsty = true;
			}, 1000);
		}
		if (every(seconds, 5, 5)) {
			if (hp < 25 && !lowHp) {
				lowHp = true;
				cast(18185, 36);
				cast(18186, 36);
			} else if (hp >= 26 && hp <= 50 && !midHp) {
				midHp = true;
				cast(18187, 36);
			} else if (hp >= 51 && hp <= 75 && !highHp) {
				highHp = true;
				cast(18184, 36);
			}
		}
		if (every(seconds, 20, 20)) {
			if (hp < 50 && Rnd.nextBoolean()) {
				cast(18187, 36);
			} else if (hp < 25) {
				cast(18185, 36);
				cast(18186, 36);
			} else if (hp <= 50) {
				cast(18184, 36);
			} else if (hp <= 75) {
				cast(Rnd.nextBoolean() ? 18188 : 18184, 36);
			} else {
				cast(18188, 36);
			}
		}
	}

	/**
	 * The giant goes after feed or water as soon as it appears.
	 */
	public void onFoodSpawned(Npc food) {
		if (isInFight())
			getAggroList().addHate(food, 5000000);
	}

	public void onFoodDied(int foodNpcId, boolean eatenByGiant) {
		if (isDead())
			return;
		if (eatenByGiant && foodNpcId == FEED && hungry) {
			hungry = false;
			castOnSelf(18183, 36); // Satiation
		} else if (eatenByGiant && foodNpcId == WATER && thirsty) {
			thirsty = false;
			castOnSelf(18561, 36); // Quench Thirst
		} else {
			castOnSelf(18182, 36); // Craving
		}
	}
}
