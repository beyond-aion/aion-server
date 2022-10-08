package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("vasharti_assassin")
public class VashartiAssassinAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);

	public VashartiAssassinAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		if (isHome.compareAndSet(true, false)) {
			WorldPosition p = getPosition();
			Npc smoke = (Npc) spawn(282465, p.getX(), p.getY(), p.getZ(), p.getHeading());
			smoke.getController().delete();
		}
		super.handleCreatureAggro(creature);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				SkillEngine.getInstance().getSkill(getOwner(), 19915, 60, getOwner()).useNoAnimationSkill();
		}, 2000);
	}

	@Override
	protected void handleBackHome() {
		isHome.set(true);
		super.handleBackHome();
		getEffectController().removeEffect(19915);
		getEffectController().removeEffect(19916);
		SkillEngine.getInstance().getSkill(getOwner(), 19915, 60, getOwner()).useNoAnimationSkill();
	}
}
