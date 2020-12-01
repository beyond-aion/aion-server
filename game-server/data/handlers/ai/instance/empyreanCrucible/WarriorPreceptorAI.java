package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien
 */
@AIName("warrior_preceptor")
public class WarriorPreceptorAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> task;

	public WarriorPreceptorAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	public void handleDied() {
		cancelTask();
		PacketSendUtility.broadcastMessage(getOwner(), 1500208);
		super.handleDied();
	}

	@Override
	public void handleBackHome() {
		cancelTask();
		isHome.set(true);
		super.handleBackHome();
	}

	@Override
	public void handleAttack(Creature creature) {

		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			startSkillTask();
		}
	}

	private void startSkillTask() {
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead()) {
					cancelTask();
				} else {
					startSkillEvent();
				}
			}

		}, 30000, 30000);
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
	}

	private void startSkillEvent() {
		PacketSendUtility.broadcastMessage(getOwner(), 1500207);
		SkillEngine.getInstance().getSkill(getOwner(), 19595, 10, getTargetPlayer()).useNoAnimationSkill();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 19596, 15, getOwner()).useNoAnimationSkill();
				}
			}

		}, 6000);
	}

	private Player getTargetPlayer() {
		List<Player> players = new ArrayList<>();
		getKnownList().forEachPlayer(player -> {
			if (!player.isDead() && PositionUtil.isInRange(player, getOwner(), 15)) {
				players.add(player);
			}
		});
		return Rnd.get(players);
	}
}
