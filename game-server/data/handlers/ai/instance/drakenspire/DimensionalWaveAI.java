package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.UseSkillAndDieAI;

/**
 * @author Estrayl
 */
@AIName("drakenspire_dimensional_wave")
public class DimensionalWaveAI extends UseSkillAndDieAI {

	public DimensionalWaveAI(Npc owner) {
		super(owner);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21620) {
			ThreadPoolManager.getInstance().schedule(this::calculateAndApplyDamage, 1200); // Aligns visual hit and damage
		}
	}

	private void calculateAndApplyDamage() {
		getKnownList().streamPlayers().filter(p -> !p.isDead() && isInRange(p, 29)).forEach(p -> {
			int headingTowardsPlayer = PositionUtil.getHeadingTowards(getPosition().getX(), getPosition().getY(), p.getX(), p.getY());
			int headingMax = getPosition().getHeading(); // 30 or 90
			int headingMin = headingMax - 60;
			if (headingMin < 0) {
				headingMin += 120;
			}

			boolean isHit;
			if (headingMin <= headingMax) {
				isHit = headingTowardsPlayer >= headingMin && headingTowardsPlayer <= headingMax;
			} else {
				isHit = headingTowardsPlayer >= headingMin || headingTowardsPlayer <= headingMax;
			}

			if (isHit) {
				SkillEngine.getInstance().applyEffect(21874, getOwner(), p);
			}
		});
	}
}
