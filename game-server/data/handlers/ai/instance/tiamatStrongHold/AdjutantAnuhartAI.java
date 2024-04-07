package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller, Estrayl
 */
@AIName("adjutantanuhart")
public class AdjutantAnuhartAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(50, 25, 10);

	public AdjutantAnuhartAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 20747) { // Blade Storm
			SkillEngine.getInstance().applyEffect(20749, getOwner(), getOwner());
			getEffectController().setAbnormal(AbnormalState.SANCTUARY);
			spawn(283099, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 20747) // Blade Storm
			getEffectController().unsetAbnormal(AbnormalState.SANCTUARY);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 50 -> useSelfBuff(20938);
			case 25 -> useSelfBuff(20939);
			case 10 -> useSelfBuff(20940);
		}
	}

	private void useSelfBuff(int buffSkillId) {
		AIActions.targetSelf(this);
		AIActions.useSkill(this, buffSkillId);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
	}
}
