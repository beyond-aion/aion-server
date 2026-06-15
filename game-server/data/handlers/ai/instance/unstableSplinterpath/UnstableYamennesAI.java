package ai.instance.unstableSplinterpath;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu, Luzien, Cheatkiller
 */
@AIName("unstableyamennes")
public class UnstableYamennesAI extends AggressiveNpcAI {

	private Future<?> portalTask;
	private Future<?> enrageTask;
	private final AtomicBoolean isStart = new AtomicBoolean();

	public UnstableYamennesAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isStart.compareAndSet(false, true)) {
			PacketSendUtility.broadcastMessage(getOwner(), 1500013); // Those who threaten the artefact shall be returned to the flow of Aether!
			startTasks();
		}
	}

	private void startTasks() {
		enrageTask = ThreadPoolManager.getInstance().schedule(() -> getOwner().queueSkill(19098, 55, 0), 600000);
		portalTask = ThreadPoolManager.getInstance().schedule(() -> spawnPortals(false), 60000);
	}

	private void onHealingDebuff() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		deleteNpcs(instance.getNpcs(219586));
		getOwner().queueSkill(19282, 55);
		spawn(219586, getOwner().getX() + 10, getOwner().getY() - 10, getOwner().getZ(), (byte) 0);
		spawn(219586, getOwner().getX() - 10, getOwner().getY() + 10, getOwner().getZ(), (byte) 0);
		spawn(219586, getOwner().getX() + 10, getOwner().getY() + 10, getOwner().getZ(), (byte) 0);
		getOwner().clearAttackedCount();
		PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdD_ResetAggro());
	}

	private void spawnPortals(boolean isTopSpawn) {
		Npc portalA = getPosition().getWorldMapInstance().getNpc(219567);
		Npc portalB = getPosition().getWorldMapInstance().getNpc(219579);
		Npc portalC = getPosition().getWorldMapInstance().getNpc(219580);
		if (portalA == null && portalB == null && portalC == null) {
			PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdD_SummonStart());
			if (isTopSpawn) {
				spawn(219567, 288.10f, 741.95f, 216.81f, (byte) 3);
				spawn(219579, 375.05f, 750.67f, 216.82f, (byte) 59);
				spawn(219580, 341.33f, 699.38f, 216.86f, (byte) 59);
			} else {
				spawn(219567, 303.69f, 736.35f, 198.7f, (byte) 0);
				spawn(219579, 335.19f, 708.92f, 198.9f, (byte) 35);
				spawn(219580, 360.23f, 741.07f, 198.7f, (byte) 0);
			}
		}
		ThreadPoolManager.getInstance().schedule(this::onHealingDebuff, 3000);
		portalTask = ThreadPoolManager.getInstance().schedule(() -> spawnPortals(!isTopSpawn), 60000);
	}

	private void deleteNpcs(List<Npc> npcs) {
		npcs.stream().filter(Objects::nonNull).forEach(n -> n.getController().delete());
	}

	private void cancelTasks() {
		if (portalTask != null && !portalTask.isDone())
			portalTask.cancel(true);
		if (enrageTask != null && !enrageTask.isDone())
			enrageTask.cancel(true);
	}

	@Override
	protected void handleBackHome() {
		cancelTasks();
		super.handleBackHome();
		getOwner().getController().delete();
	}

	@Override
	protected void handleDespawned() {
		cancelTasks();
		deleteNpcs(getPosition().getWorldMapInstance().getNpcs(219586));
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelTasks();
		deleteNpcs(getPosition().getWorldMapInstance().getNpcs(219586));
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}
}
