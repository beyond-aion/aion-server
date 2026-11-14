package ai.instance.empyreanCrucible;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.attack.AggroTarget;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Takun and Gojira of stage 9 fight each other as well. After three minutes of fighting both fly into a rage, and from 70 seconds on the arena
 * keeps drawing their attention to the players in it.
 *
 * @author Luzien
 */
@AIName("takun_gojira")
public class TakunGojiraAI extends CrucibleBossAI {

	private static final int TAKUN = 217596;
	private int attackIn, rageAttackIn, attentionIn;

	public TakunGojiraAI(Npc owner) {
		super(owner);
	}

	private boolean isTakun() {
		return getNpcId() == TAKUN;
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> {
			Npc counterpart = getPosition().getWorldMapInstance().getNpc(isTakun() ? 217597 : TAKUN);
			if (counterpart != null)
				getAggroList().addHate(counterpart, 1000000);
		}, 500);
	}

	@Override
	protected void onFightStart() {
		if (isTakun())
			cast(17477, 38);
		else
			cast(16828, 38, NpcSkillTargetAttribute.ME);
		attackIn = isTakun() ? 8 : 18;
		rageAttackIn = 0;
		attentionIn = 70;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (--attackIn <= 0) {
			if (isTakun()) {
				int roll = Rnd.get(1, 100);
				if (roll <= 15) {
					cast(19625, 38);
					attackIn = 25;
				} else if (roll <= 24) { // 10 % of the remaining 85 %
					cast(16859, 38);
					attackIn = 25;
				} else {
					cast(16859, 38);
					attackIn = 15;
				}
			} else {
				int roll = Rnd.get(1, 100);
				if (roll <= 30) {
					cast(19626, 1);
					attackIn = 30;
				} else if (roll <= 37) { // 10 % of the remaining 70 %
					cast(19626, 1);
					attackIn = 40;
				} else {
					cast(17968, 38);
					cast(17981, 38);
					attackIn = 30;
				}
			}
		}
		if (seconds == 180) {
			cast(19624, 10, NpcSkillTargetAttribute.ME);
			if (attacker(AggroTarget.RANDOM_EXCEPT_CURRENT_TARGET) instanceof Creature target)
				getAggroList().addHate(target, isTakun() ? 1000000 : 10000000);
			rageAttackIn = 8;
		}
		if (rageAttackIn > 0 && --rageAttackIn == 0) {
			if (isTakun()) {
				cast(16859, 38);
				cast(16859, 38);
			} else {
				cast(17968, 38);
				cast(17981, 38);
			}
			rageAttackIn = 10;
		}
		if (--attentionIn <= 0) {
			getKnownList().forEachPlayer(player -> {
				if (!player.isDead() && PositionUtil.isInRange(getOwner(), player, 100))
					getAggroList().addHate(player, 100);
			});
			attentionIn = seconds < 170 ? 100 : 30;
		}
	}
}
