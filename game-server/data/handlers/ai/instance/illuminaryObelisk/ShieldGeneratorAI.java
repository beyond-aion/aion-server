package ai.instance.illuminaryObelisk;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * On retail you can fake charge waves - means waves will spawn when players begin to charge.
 * But there is still no logical need to implement double-checks for this special case, so we
 * spawn them event-based via {@link instance.IlluminaryObeliskInstance#onSpawn()}.
 * 
 * @author Estrayl
 */
public abstract class ShieldGeneratorAI extends GeneralNpcAI {

	private final AtomicBoolean isUnderCharge = new AtomicBoolean();
	private final AtomicBoolean isVortexSpawned = new AtomicBoolean();
	protected final List<Npc> charges = new ArrayList<>();
	protected int chargeCount = 0;
	private long lastAttackedTime;

	protected abstract SM_SYSTEM_MESSAGE getAttackMsg();

	protected abstract SM_SYSTEM_MESSAGE getChargeMsg();

	protected abstract SM_SYSTEM_MESSAGE getGateMsg();

	protected abstract SM_SYSTEM_MESSAGE getDestructionMsg();

	protected abstract void handleChargeComplete();

	protected abstract void handleVortexSpawn();

	protected abstract void handleVortexDespawn();

	public ShieldGeneratorAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (System.currentTimeMillis() - lastAttackedTime > 10000) {
			lastAttackedTime = System.currentTimeMillis();
			shout(getAttackMsg());
		}
	}

	protected void shout(SM_SYSTEM_MESSAGE msg) {
		PacketSendUtility.broadcastToMap(getOwner(), msg);
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1) {
			if (chargeCount > 2) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_END());
				return false;
			}
			if (player.getInventory().getItemCountByItemId(164000289) == 0) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_NOITEM());
				return false;
			}
			if (isVortexSpawned.compareAndSet(false, true))
				handleVortexSpawn();
			if (isUnderCharge.compareAndSet(false, true))
				handleCharging(player);
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

	private void handleCharging(final Player player) {
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, 2));
				player.getObserveController().removeObserver(this);
				isUnderCharge.set(false);
			}
		};
		player.getObserveController().attach(observer);
		PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 20000, 1));
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
		shout(getChargeMsg());
		player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(() -> {
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 20000, 2));
			player.getObserveController().removeObserver(observer);
			if (player.getInventory().decreaseByItemId(164000289, 1)) {
				handleChargeComplete();
				chargeCount++;
			}
			isUnderCharge.set(false);
		}, 20000));
	}

	private void deleteNpcs() {
		charges.forEach(s -> s.getController().delete());
	}

	@Override
	protected void handleDespawned() {
		deleteNpcs();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		deleteNpcs();
		shout(getDestructionMsg());
		handleVortexDespawn();
		super.handleDied();
	}
}
