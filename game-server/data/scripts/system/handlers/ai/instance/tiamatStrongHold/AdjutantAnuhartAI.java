package ai.instance.tiamatStrongHold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 * @modified Estrayl
 */
@AIName("adjutantanuhart")
public class AdjutantAnuhartAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private List<Integer> percents = new ArrayList<>();

	public AdjutantAnuhartAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
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

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 50:
						chooseBuff(20938);
						break;
					case 25:
						chooseBuff(20939);
						break;
					case 10:
						chooseBuff(20940);
						break;
				}

				break;
			}
		}
	}

	private void chooseBuff(int buff) {
		AIActions.targetSelf(this);
		AIActions.useSkill(this, buff);
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 50, 25, 10 });
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		addPercent();
		isHome.set(true);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
	}
}
