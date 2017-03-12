package ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz
 * @modified vlog
 */
@AIName("useitem")
public class ActionItemNpcAI extends NpcAI {

	protected int startBarAnimation = 1;
	protected int cancelBarAnimation = 2;
	private List<ItemUseObserver> observers = new ArrayList<>();

	@Override
	protected void handleDialogStart(Player player) {
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
					observers.remove(this);
					player.getObserveController().removeObserver(this);
				}

			};

			player.getObserveController().addObserver(observer);
			observers.add(observer);
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), getTalkDelay(), startBarAnimation));
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), getTalkDelay(), cancelBarAnimation));
					player.getObserveController().removeObserver(observer);
					observers.remove(observer);
					handleUseItemFinish(player);
				}

			}, delay));
		} else {
			handleUseItemFinish(player);
		}
	}

	protected void handleUseItemFinish(Player player) {
		if (getOwner().isInInstance())
			AIActions.handleUseItemFinish(this, player);
	}

	protected int getTalkDelay() {
		return getObjectTemplate().getTalkDelay() * 1000;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		Iterator<ItemUseObserver> iter = observers.iterator();
		while (iter.hasNext()) {
			ItemUseObserver observer = iter.next();
			observer.abort();
		}
	}

}
