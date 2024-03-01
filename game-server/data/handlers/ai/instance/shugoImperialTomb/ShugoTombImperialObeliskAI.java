package ai.instance.shugoImperialTomb;

import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.GeneralNpcAI;

/**
 * @author Ritsu
 */
@AIName("shugo_tomb_imperial_obelisk")
public class ShugoTombImperialObeliskAI extends GeneralNpcAI {

	private final AtomicInteger damageLevel = new AtomicInteger();

	public ShugoTombImperialObeliskAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage > 35 && hpPercentage <= 70 && damageLevel.compareAndSet(0, 1))
			SkillEngine.getInstance().applyEffectDirectly(21098, getOwner(), getOwner());
		if (hpPercentage > 0 && hpPercentage <= 35 && damageLevel.compareAndSet(1, 2))
			SkillEngine.getInstance().applyEffectDirectly(21099, getOwner(), getOwner());
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().applyEffectDirectly(21097, getOwner(), getOwner());
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}
}
