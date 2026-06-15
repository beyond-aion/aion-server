package ai.instance.dragonLordsRefuge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller, Luzien, Estrayl
 */
@AIName("tiamat_weakened_dragon")
public class TiamatWeakenedDragonAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	protected HpPhases hpPhases = new HpPhases(50, 25, 15, 5);
	protected AtomicBoolean hasAggro = new AtomicBoolean();
	protected List<Future<?>> spawnTasks = new ArrayList<>();

	public TiamatWeakenedDragonAI(Npc owner) {
		super(owner);
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_FIRE;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (hasAggro.compareAndSet(false, true)) {
			offerAtrocityEvent();
			scheduleSinkingSand();
		}
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 50:
				scheduleDivisiveCreations(60000);
				break;
			case 25:
				scheduleInfinitePain();
				spawnGravityCrusher();
				break;
			case 15:
			case 5:
				spawnGravityCrusher();
				break;
		}
	}

	private void offerAtrocityEvent() {
		if (!isDead() && hasAggro.get()) {
			if (getLifeStats().getHpPercentage() <= 50)
				getOwner().queueSkill(20921, 1, 0);
			getOwner().queueSkill(calculateAtrocitySkillId(), 1);
		}
	}

	protected int calculateAtrocitySkillId() {
		return 20922 + (Rnd.nextInt(3) * 2);
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20922: // Ultimate Atrocity
				spawn(283237, 445.0f, 550.70f, 417.41f, (byte) 0);
				spawn(283241, 469.8f, 543.01f, 417.41f, (byte) 0);
				break;
			case 20924: // Ultimate Atrocity
				spawn(283240, 457.8f, 514.6f, 417.41f, (byte) 0);
				spawn(283240, 462.3f, 514.6f, 417.41f, (byte) 0);
				spawn(283240, 469.7f, 514.6f, 417.41f, (byte) 0);
				spawn(283240, 466.6f, 514.6f, 417.41f, (byte) 0);
				spawn(283240, 473.6f, 514.6f, 417.41f, (byte) 0);
				spawn(283240, 479.3f, 514.6f, 417.41f, (byte) 0);
				spawn(283241, 475.8f, 514.6f, 417.41f, (byte) 0);
				spawn(283241, 491.2f, 514.6f, 417.41f, (byte) 0);
				spawn(283241, 482.7f, 514.6f, 417.41f, (byte) 0);
				spawn(283241, 485.2f, 514.6f, 417.41f, (byte) 0);
				spawn(283241, 488.1f, 514.6f, 417.41f, (byte) 0);
				break;
			case 20926: // Ultimate Atrocity
				spawn(283244, 458.67f, 480.76f, 417.41f, (byte) 0);
				break;
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20922: // Ultimate Atrocity
			case 20924:
			case 20926:
				offerAtrocityEvent();
				break;
		}
	}

	protected void scheduleSinkingSand() {
		spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				int delay = 0;
				for (float degree = -25.0f; degree <= 25.0f; degree += 4) {
					double radian = Math.toRadians(degree);
					spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> spawnSinkingSand(radian), delay += 1800));
				}
				scheduleSinkingSand();
			}
		}, 120000));
	}

	private void spawnSinkingSand(double radian) {
		float dist = 25.0f;
		for (int i = 0; i < 7; i++) {
			dist += 2.5f;
			float x = (float) (Math.cos(radian) * dist);
			float y = (float) (Math.sin(radian) * dist);
			spawn(283135, getPosition().getX() + x, getPosition().getY() + y, getPosition().getZ(), (byte) 0);
		}
	}

	/**
	 * Limited to 3 times.
	 */
	protected void scheduleDivisiveCreations(int delay) {
		spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			if (hasAggro.get() && !isDead() && delay > 30000) {
				spawn(283139, 464.24f, 462.26f, 417.4f, (byte) 18);
				spawn(283139, 542.79f, 465.03f, 417.4f, (byte) 43);
				spawn(283139, 541.79f, 563.71f, 417.4f, (byte) 74);
				spawn(283139, 465.79f, 565.43f, 417.4f, (byte) 100);
				scheduleDivisiveCreations(delay - 10000);
			}
		}, delay));
	}

	protected void spawnGravityCrusher() {
		spawn(283141, 464.24f, 462.26f, 417.4f, (byte) 18);
		spawn(283141, 542.79f, 465.03f, 417.4f, (byte) 43);
		spawn(283141, 541.79f, 563.71f, 417.4f, (byte) 74);
		spawn(283141, 465.79f, 565.43f, 417.4f, (byte) 100);
	}

	protected void scheduleInfinitePain() {
		spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			if (hasAggro.get() && !isDead()) {
				spawnInfinitePain();
				scheduleInfinitePain();
			}
		}, 90000));
	}

	protected void spawnInfinitePain() {
		spawn(283143, 508.32f, 515.18f, 417.4f, (byte) 0);
	}

	protected void despawnAdds() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		deleteNpcs(instance.getNpcs(283141));
		deleteNpcs(instance.getNpcs(283139));
		deleteNpcs(instance.getNpcs(283140));
	}

	protected void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs)
			if (npc != null)
				npc.getController().delete();
	}

	private void cancelTasks() {
		for (Future<?> task : spawnTasks)
			if (task != null && !task.isCancelled())
				task.cancel(true);
	}

	@Override
	protected void handleDied() {
		cancelTasks();
		despawnAdds();
		RespawnService.scheduleDecayTask(getOwner(), 3000);
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		despawnAdds();
		cancelTasks();
	}

	@Override
	protected void handleBackHome() {
		cancelTasks();
		hasAggro.set(false);
		despawnAdds();
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT -> false;
			case CONSIDER_BOUNDS_IN_CAN_SEE_CHECK_WHEN_ATTACKED, CONSIDER_BOUNDS_IN_CAN_SEE_CHECK_WHEN_ATTACKING -> true;
			default -> super.ask(question);
		};
	}
}
