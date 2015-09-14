package ai.instance.idgelResearchCenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.NoActionAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author Ritsu
 * @rework Luzien
 */
@AIName("lightningengine")
public class LightningEngineAI2 extends NoActionAI2 {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private AtomicBoolean isSpawned = new AtomicBoolean(false);
	private List<Integer> percents = new ArrayList<>();

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isHome.set(true);
		percents.clear();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		percents.clear();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		percents.clear();
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
		SkillEngine.getInstance().getSkill(getOwner(), 21121, 30, getOwner()).useWithoutPropSkill();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (!isHome.get() || isHome.compareAndSet(true, false))
			checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 90:
						SkillEngine.getInstance().getSkill(getOwner(), 21125, 5, getOwner()).useNoAnimationSkill();
						rndSpawn(284310, 1);
						break;
					case 40:
						SkillEngine.getInstance().getSkill(getOwner(), 21125, 5, getOwner()).useNoAnimationSkill();
						rndSpawn(284310, 2);
						break;
					case 30:
						SkillEngine.getInstance().getSkill(getOwner(), 21125, 5, getOwner()).useNoAnimationSkill();
						rndSpawn(284310, 1);
						rndSpawn(284308, 1);
						break;
					case 20:
						SkillEngine.getInstance().getSkill(getOwner(), 21125, 5, getOwner()).useNoAnimationSkill();
						rndSpawn(284308, 2);
						break;
				}

				break;
			}
		}
	}

	private void rndSpawn(int npcId, int count) {
		float direction = Rnd.get(0, 199) / 100f;
		int distance = Rnd.get(1, 3);
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		for (int i = 0; i < count; i++) {
			spawn(npcId, getOwner().getX() + x1, getOwner().getY() + y1, getOwner().getZ(), (byte) 0);
		}
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		checkDistance(this, creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		checkDistance(this, creature);
	}

	private void checkDistance(NpcAI2 ai, Creature creature) {
		Npc unmaker = getPosition().getWorldMapInstance().getNpc(230110);
		if (creature instanceof Npc) {
			if (unmaker != null && MathUtil.isIn3dRange(getOwner(), unmaker, 8) && unmaker.getEffectController().hasAbnormalEffect(21121)
				&& isSpawned.compareAndSet(false, true)) {
				SkillEngine.getInstance().getSkill(unmaker, 21122, 30, unmaker).useSkill();
				SkillEngine.getInstance().getSkill(getOwner(), 21122, 30, getOwner()).useSkill();
				rndSpawn(284310, 1);
				SkillEngine.getInstance().getSkill(getOwner(), 21125, 5, getOwner()).useNoAnimationSkill();
			}
		}
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 90, 40, 30, 20 });
	}
}
