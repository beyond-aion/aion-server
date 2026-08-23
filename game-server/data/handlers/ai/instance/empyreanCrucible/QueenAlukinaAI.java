package ai.instance.empyreanCrucible;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien, w4terbomb
 */
@AIName("alukina_emp")
public class QueenAlukinaAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(75, 50, 25);
	private Future<?> task;

	public QueenAlukinaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelTask();
		PacketSendUtility.broadcastMessage(getOwner(), 1500231);
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		cancelTask();
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		getOwner().queueSkill(17899, 41, 4500);
		switch (phaseHpPercent) {
			case 75 -> {
				getOwner().queueSkill(17900, 41);
				PacketSendUtility.broadcastMessage(getOwner(), 1500229);
				ThreadPoolManager.getInstance().schedule(() -> {
					if (getLifeStats().getHpPercentage() > 50) {
						getOwner().queueSkill(17899, 41, 4000);
						getOwner().queueSkill(17900, 41);
					}
				}, 14000);
			}
			case 50 -> {
				getOwner().queueSkill(17280, 41, 3500);
				getOwner().queueSkill(17902, 41);
			}
			case 25 -> {
				PacketSendUtility.broadcastMessage(getOwner(), 1500230);
				task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
					if (isDead()) {
						cancelTask();
					} else {
						getOwner().queueSkill(17901, 41, 5500);
						getOwner().queueSkill(17902, 41, 2000);
						getOwner().queueSkill(17902, 41);
					}
				}, 4500, 20000);
			}
		}
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}
}
