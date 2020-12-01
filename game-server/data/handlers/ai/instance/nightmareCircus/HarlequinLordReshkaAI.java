package ai.instance.nightmareCircus;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("harlequinlordreshka")
public class HarlequinLordReshkaAI extends AggressiveNpcAI {

	private Future<?>[] openBoxesTasks = new Future<?>[2];
	private Future<?> spawnBoxesTask;

	public HarlequinLordReshkaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		spawnTerrorsBox();
		spawnNightmaresBox();
		openBoxes();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTasks();
		despawnNpcs(831348, 831349);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTasks();
	}

	private void cancelTasks() {
		for (Future<?> openBoxesTask : openBoxesTasks) {
			if (openBoxesTask != null && !openBoxesTask.isDone()) {
				openBoxesTask.cancel(true);
			}
		}
		if (spawnBoxesTask != null && !spawnBoxesTask.isDone()) {
			spawnBoxesTask.cancel(true);
		}
	}

	private void spawnTerrorsBox() {
		spawn(831348, 507.42712f, 570.53394f, 199.50775f, (byte) 30);
		spawn(831348, 507.6568f, 560.18787f, 199.50775f, (byte) 30);
		spawn(831348, 512.9201f, 552.13983f, 199.50775f, (byte) 30);
		spawn(831348, 513.1767f, 577.7641f, 199.50775f, (byte) 30);
		spawn(831348, 521.76257f, 548.68469f, 199.50775f, (byte) 30);
		spawn(831348, 521.97913f, 583.61383f, 199.50775f, (byte) 30);
		spawn(831348, 529.46533f, 582.23792f, 199.50775f, (byte) 30);
		spawn(831348, 530.72772f, 549.67969f, 199.50775f, (byte) 30);
		spawn(831348, 536.98706f, 578.31433f, 199.50775f, (byte) 30);
		spawn(831348, 539.05304f, 552.27924f, 199.50775f, (byte) 30);
		spawn(831348, 542.64111f, 560.79907f, 200.3221f, (byte) 30);
		spawn(831348, 543.323f, 572.56665f, 199.50775f, (byte) 30);
	}

	private void spawnNightmaresBox() {
		spawnBoxesTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				spawn(831349, 507.42712f, 570.53394f, 199.50775f, (byte) 30);
				spawn(831349, 507.6568f, 560.18787f, 199.50775f, (byte) 30);
				spawn(831349, 512.9201f, 552.13983f, 199.50775f, (byte) 30);
				spawn(831349, 513.1767f, 577.7641f, 199.50775f, (byte) 30);
				spawn(831349, 521.76257f, 548.68469f, 199.50775f, (byte) 30);
				spawn(831349, 521.97913f, 583.61383f, 199.50775f, (byte) 30);
				spawn(831349, 529.46533f, 582.23792f, 199.50775f, (byte) 30);
				spawn(831349, 530.72772f, 549.67969f, 199.50775f, (byte) 30);
				spawn(831349, 536.98706f, 578.31433f, 199.50775f, (byte) 30);
				spawn(831349, 539.05304f, 552.27924f, 199.50775f, (byte) 30);
				spawn(831349, 542.64111f, 560.79907f, 200.3221f, (byte) 30);
				spawn(831349, 543.323f, 572.56665f, 199.50775f, (byte) 30);
			}
		}, 36000);
	}

	private void openBoxes() {
		openBoxesTasks[0] = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				AIActions.useSkill(HarlequinLordReshkaAI.this, 21477);
				PacketSendUtility.broadcastMessage(getOwner(), 1501146);
			}
		}, 30000);
		openBoxesTasks[1] = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				AIActions.useSkill(HarlequinLordReshkaAI.this, 21477);
				PacketSendUtility.broadcastMessage(getOwner(), 1501147);
				PacketSendUtility.broadcastMessage(getOwner(), Rnd.nextBoolean() ? 1501145 : 1501137, 2000);
			}
		}, 60000);
	}

	private void despawnNpcs(int... npcIds) {
		for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs(npcIds)) {
			npc.getController().delete();
		}
	}
}
