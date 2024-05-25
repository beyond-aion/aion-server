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
@AIName("drakenspire_dimensional_wave_small")
public class DimensionalWaveSmallAI extends UseSkillAndDieAI {

	public DimensionalWaveSmallAI(Npc owner) {
		super(owner);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21620) {
			ThreadPoolManager.getInstance().schedule(this::calculateAndApplyDamage, 1200); // Aligns visual hit and damage
		}
	}

	private void calculateAndApplyDamage() {
		getKnownList().getKnownPlayers().values().stream().filter(p -> !p.isDead() && PositionUtil.isInRange(getOwner(), p, 22, true))
			.forEach(p -> SkillEngine.getInstance().applyEffect(21874, getOwner(), p));
	}
}
