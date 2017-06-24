package ai.instance.aturamSkyFortress;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("explosion_shadows")
public class ExplosionShadowsAI extends AggressiveNpcAI {

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
		if (!isDead()) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (!isDead()) {
						SkillEngine.getInstance().getSkill(getOwner(), 19425, 49, getOwner()).useNoAnimationSkill();

						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								if (!isDead()) {
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
		getKnownList().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player player) {
				if (player.getEffectController().hasAbnormalEffect(19502)) {
					final Npc npc = (Npc) spawn(799657, player.getX(), player.getY(), player.getZ(), player.getHeading());
					player.getEffectController().removeEffect(19502);
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (npc != null && !npc.isDead()) {
								npc.getController().delete();
							}
						}

					}, 4000);
				}
			}

		});
		AIActions.deleteOwner(this);
	}

}
