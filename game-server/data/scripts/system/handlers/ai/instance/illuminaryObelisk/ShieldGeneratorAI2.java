package ai.instance.illuminaryObelisk;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javolution.util.FastTable;
import ai.GeneralNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Estrayl
 */
public abstract class ShieldGeneratorAI2 extends GeneralNpcAI2 {

	private AtomicBoolean isUnderCharge = new AtomicBoolean(false);
	private List<Future<?>> spawnTasks = new FastTable<>();
	private List<Npc> assaulter = new FastTable<>();
	protected List<Npc> support = new FastTable<>();
	protected int chargeCount = 0;
	private int attackCount;

	protected abstract int getAttackMsg();

	protected abstract int getChargeMsg();

	protected abstract int getGateMsg();

	protected abstract int getDestructionMsg();

	protected abstract void handleChargeComplete();

	protected abstract void phaseAttack();

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		attackCount++;
		if (attackCount == 10) {
			attackCount = 0;
			shout(getAttackMsg());
		}
	}

	protected void shout(int msgId) {
		PacketSendUtility.broadcastToMap(getOwner(), msgId);
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == 10000) {
			if (chargeCount > 2) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402203));
				return false;
			}
			if (player.getInventory().getItemCountByItemId(164000289) == 0) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402211));
				return false;
			}
			if (isUnderCharge.compareAndSet(false, true))
				handleCharging(player);
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

	private void handleCharging(final Player player) {
		try {
			final ItemUseObserver observer = new ItemUseObserver() {

				@Override
				public void abort() {
					player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, 2));
					player.getObserveController().removeObserver(this);
				}
			};
			final int delay = 20000;
			player.getObserveController().attach(observer);
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 1));
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
			shout(getChargeMsg());
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 2));
					player.getObserveController().removeObserver(observer);
					player.getInventory().decreaseByItemId(164000289, 1);
					handleChargeComplete();
					phaseAttack();
					chargeCount++;
				}
			}, delay));
		} finally {
			isUnderCharge.set(false);
		}
	}

	protected void sp(final int npcId, final float x, final float y, final float z, final byte h, final int delay, final String walkerId) {
		Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					Npc npc = (Npc) spawn(npcId, x, y, z, h);
					assaulter.add(npc);
					npc.getSpawn().setWalkerId(walkerId);
					WalkManager.startWalking((NpcAI2) npc.getAi2());
					npc.setState(CreatureState.WALKING);
					PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
				}
			}
		}, delay);
		spawnTasks.add(task);
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null && !npc.getLifeStats().isAlreadyDead())
				npc.getController().onDelete();
		}
	}

	@Override
	protected void handleDespawned() {
		for (Future<?> task : spawnTasks) {
			if (task != null && !task.isCancelled())
				task.cancel(true);
		}
		deleteNpcs(support);
		deleteNpcs(assaulter);
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		deleteNpcs(support);
		deleteNpcs(assaulter);
		shout(getDestructionMsg());
		super.handleDied();
	}
}
