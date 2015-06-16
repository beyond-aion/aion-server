package ai.instance.danuarSanctuary;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.GeneralNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.manager.EmoteManager;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 *
 * @author xTz
 */
@AIName("siege_drill")
public class SiegeDrill extends GeneralNpcAI2 {

	protected int startBarAnimation = 1;
	protected int cancelBarAnimation = 2;
	private AtomicBoolean isUsed = new AtomicBoolean(false);

	@Override
	protected void handleDialogStart(Player player) {
		if (isUsed.get()) {
			return;
		}
		handleUseItemStart(player);
	}

	protected void handleUseItemStart(final Player player) {
		final int delay = getTalkDelay();
		if (delay > 1) {
			final ItemUseObserver observer = new ItemUseObserver() {
				@Override
				public void abort() {
					player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, cancelBarAnimation));
					player.getObserveController().removeObserver(this);
				}

			};

			player.getObserveController().attach(observer);
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), getTalkDelay(), startBarAnimation));
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), getTalkDelay(), cancelBarAnimation));
					player.getObserveController().removeObserver(observer);
					handleUseItemFinish(player);
				}

			}, delay));
		}
		else {
			handleUseItemFinish(player);
		}
	}

	protected void handleUseItemFinish(Player player) {
		if (!player.getInventory().decreaseByItemId(185000174, 1)) {
			NpcShoutsService.getInstance().sendMsg(getOwner(), 1401932);
			return;
		}
		if (isUsed.compareAndSet(false, true)) {
			getOwner().getSpawn().setWalkerId("2A2D7F6EA351DCCEAA3CD097B9311ED308DCD2EC");
			WalkManager.startWalking(this);
			getOwner().setState(1);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					Npc npc = getPosition().getWorldMapInstance().getNpc(233189);
					if (npc != null) {
						getOwner().setTarget(npc);

						getOwner().getController().useSkill(20778);
						ThreadPoolManager.getInstance().schedule(new Runnable() {
							@Override
							public void run() {
								getOwner().getSpawn().setWalkerId(null);
								getMoveController().abortMove();
								setStateIfNot(AIState.IDLE);
								setSubStateIfNot(AISubState.NONE);
								EmoteManager.emoteStopWalking(getOwner());
								AI2Actions.deleteOwner(SiegeDrill.this);
							}

						}, 3500);
					}
				}

			}, 1500);
		}
	}

	protected int getTalkDelay() {
		return getObjectTemplate().getTalkDelay() * 1000;
	}

}
