package ai.instance.aturamSkyFortress;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.knownlist.Visitor;

import ai.AggressiveNpcAI2;

/**
 * @author xTz
 */
@AIName("explosion_shadows")
public class ExplosionShadowsAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isHome = new AtomicBoolean(true);

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (isHome.compareAndSet(true, false)) {
			SkillEngine.getInstance().getSkill(getOwner(), 19428, 1, getOwner()).useNoAnimationSkill();
			getPosition().getWorldMapInstance().getDoors().get(2).setOpen(true);
			getPosition().getWorldMapInstance().getDoors().get(17).setOpen(true);
			doSchedule();
		}
	}

	@Override
	protected void handleBackHome() {
		isHome.set(true);
		super.handleBackHome();
	}

	private void doSchedule() {
		if (!isAlreadyDead()) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (!isAlreadyDead()) {
						SkillEngine.getInstance().getSkill(getOwner(), 19425, 49, getOwner()).useNoAnimationSkill();

						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								if (!isAlreadyDead()) {
									check();
								}
							}

						}, 1500);
					}
				}

			}, 3000);

		}
	}

	private void check() {
		getPosition().getWorldMapInstance().getDoors().get(17).setOpen(false);
		getPosition().getWorldMapInstance().getDoors().get(2).setOpen(false);
		getKnownList().forEachPlayer(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				if (player.getEffectController().hasAbnormalEffect(19502)) {
					final Npc npc = (Npc) spawn(799657, player.getX(), player.getY(), player.getZ(), player.getHeading());
					player.getEffectController().removeEffect(19502);
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (npc != null && !npc.getLifeStats().isAlreadyDead()) {
								npc.getController().onDelete();
							}
						}

					}, 4000);
				}
			}

		});
		AI2Actions.deleteOwner(this);
	}

}
