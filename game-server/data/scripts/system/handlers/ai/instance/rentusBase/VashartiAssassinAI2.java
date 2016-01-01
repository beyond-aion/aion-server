package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@AIName("vasharti_assassin")
public class VashartiAssassinAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isHome = new AtomicBoolean(true);

	@Override
	protected void handleCreatureAggro(Creature creature) {
		if (isHome.compareAndSet(true, false)) {
			WorldPosition p = getPosition();
			Npc smoke = (Npc) spawn(282465, p.getX(), p.getY(), p.getZ(), p.getHeading());
			NpcActions.delete(smoke);
		}
		super.handleCreatureAggro(creature);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 19915, 60, getOwner()).useNoAnimationSkill();
				}
			}

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
