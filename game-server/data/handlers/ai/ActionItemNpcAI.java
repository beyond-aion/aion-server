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
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.services.DialogService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz, vlog
 */
@AIName("useitem")
public class ActionItemNpcAI extends NpcAI {

	protected final int startBarAnimation = 1;
	protected final int cancelBarAnimation = 2;
	private final List<ItemUseObserver> observers = new ArrayList<>();

	public ActionItemNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (DialogService.isInteractionAllowed(player, getOwner()))
			handleUseItemStart(player);
	}

	protected void handleUseItemStart(Player player) {
		final int talkDelayInMs = getTalkDelayInMs();
		if (talkDelayInMs > 0) {
			final ItemUseObserver observer = new ItemUseObserver() {

				@Override
				public void abort() {
					player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, cancelBarAnimation));
					synchronized (observers) {
						observers.remove(this);
					}
					player.getObserveController().removeObserver(this);
				}

			};

			player.getObserveController().addObserver(observer);
			synchronized (observers) {
				observers.add(observer);
			}
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), talkDelayInMs, startBarAnimation));
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(() -> {
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), talkDelayInMs, cancelBarAnimation));
				player.getObserveController().removeObserver(observer);
				synchronized (observers) {
					observers.remove(observer);
				}
				handleUseItemFinish(player);
			}, talkDelayInMs));
		} else {
			handleUseItemFinish(player);
		}
	}

	protected void handleUseItemFinish(Player player) {
		AIActions.handleUseItemFinish(this, player);
	}

	protected int getTalkDelayInMs() {
		return getObjectTemplate().getTalkDelay() * 1000;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		synchronized (observers) {
			for (Iterator<ItemUseObserver> iter = observers.iterator(); iter.hasNext();) {
				ItemUseObserver observer = iter.next();
				iter.remove();
				observer.abort();
			}
		}
	}

}
