package ai.instance.empyreanCrucible;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;

/**
 * @author Luzien
 */
@AIName("spectral_warrior")
public class SpectralWarriorAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isDone = new AtomicBoolean(false);

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50 && isDone.compareAndSet(false, true)) {
			getPosition().getWorldMapInstance().getInstanceHandler().onChangeStage(StageType.START_STAGE_6_ROUND_5);

			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					resurrectAllies();
				}

			}, 2000);
		}
	}

	private void resurrectAllies() {
		getKnownList().forEachNpc(npc -> {
			if (NpcActions.isAlreadyDead(npc))
				return;

			switch (npc.getNpcId()) {
				case 205413:
					spawn(217576, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
					NpcActions.delete(npc);
					break;
				case 205414:
					spawn(217577, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
					NpcActions.delete(npc);
					break;
			}
		});
	}
}
